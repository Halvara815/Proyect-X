# Firma de release, verificación de artefactos, notas de versión y plan de rollback/fix-forward

## Firma de release

Proyect X nunca almacena keystores, contraseñas ni certificados privados dentro del repositorio (`.gitignore` excluye `*.jks`, `*.keystore`, `*.pem`, `*.p12` y `*.apk`, y el workflow de CI falla si detecta claves privadas o tokens).

El módulo [`app/build.gradle.kts`](../app/build.gradle.kts) configura la variante `release` con ofuscación/reducción R8 (`isMinifyEnabled = true`, `isShrinkResources = true`), desactiva el bloque opaco de metadatos de dependencias de Play (`dependenciesInfo.includeInApk = false`) y activa la firma `release` únicamente cuando están presentes las cuatro variables de entorno de construcción:

- `PROYECT_X_KEYSTORE_PATH`: ruta absoluta a un keystore PKCS12/JKS fuera del árbol Git.
- `PROYECT_X_KEYSTORE_PASSWORD`: contraseña del almacén.
- `PROYECT_X_KEY_ALIAS`: alias de la clave de firma.
- `PROYECT_X_KEY_PASSWORD`: contraseña de la clave.

Cuando esas variables no están definidas (como en los PRs ordinarios de CI), `:app:assembleRelease` compila y valida R8/lint vital generando `app-release-unsigned.apk` sin requerir secretos en el entorno. Cuando están presentes, genera `app-release.apk` firmado con APK Signature Scheme v1, v2 y v3 (`enableV1Signing = true`, `enableV2Signing = true`, `enableV3Signing = true`).

## Verificación de artefactos

Antes de distribuir o etiquetar cualquier artefacto `release`, el responsable de publicación debe ejecutar:

```powershell
# 1. Compilar y verificar calidad estática y pruebas puras
.\gradlew.bat --no-daemon --no-configuration-cache spotlessCheck :app:lintDebug :app:assembleRelease cyclonedxBom

# 2. Verificar esquemas de firma APK (v1, v2 y v3) y certificado del firmante
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --verbose --print-certs app\build\outputs\apk\release\app-release.apk

# 3. Registrar el digest SHA-256 del APK y de la SBOM CycloneDX
Get-FileHash app\build\outputs\apk\release\app-release.apk -Algorithm SHA256
Get-FileHash app\build\reports\bom.json -Algorithm SHA256
```

Criterios obligatorios de aceptación del artefacto:

1. `apksigner verify --verbose --print-certs` devuelve `Verifies` con `Verified using v1 scheme: true`, `Verified using v2 scheme: true` y `Verified using v3 scheme: true`.
2. El certificado SHA-256 impreso por `apksigner` coincide con la huella oficial del titular del proyecto.
3. El manifiesto empaquetado coincide con [`PLAY_POLICY_REVIEW.md`](PLAY_POLICY_REVIEW.md) y pasa `AccessibilityPrivacyBoundarySmokeTest` y `DnsVpnPrivacyBoundarySmokeTest`.

## Notas de versión

### `0.1.0` (`versionCode` 1) — Baseline verificable y prototipo local

- **Enfoque y bloqueo de aplicaciones (local-first):**
  - Dashboard local sin cuentas, analítica ni telemetría.
  - Temporizador voluntario de sesiones de enfoque (`FocusSession`, `FocusSessionStore`).
  - Motor puro de reglas (`RuleEngine`, `DomainPolicy`, `ScheduleWindow`) para paquetes Android, dominios (con normalización IDN/punycode) y palabras clave, donde `ALLOW` siempre prevalece sobre `BLOCK`.
  - Selector local de aplicaciones instaladas con icono y búsqueda (`InstalledAppRepository`), sin permiso `QUERY_ALL_PACKAGES`.
  - Servicio de accesibilidad opcional (`AppBlockAccessibilityService`) restringido estrictamente a `TYPE_WINDOW_STATE_CHANGED` y nombre de paquete en primer plano; jamás lee jerarquía de ventanas, texto ni URLs (ADR-003).
  - Pantalla propia de intervención (`InterventionActivity`) con retorno al inicio o revisión de reglas.
- **Presupuestos diarios por aplicación:**
  - Evaluación pura (`DailyBudgetEngine`, `LocalDayWindow`) e integración opcional con `UsageStatsManager` tras divulgación explícita y doble consentimiento (Acceso de uso + Accesibilidad). No persiste historial de uso.
- **Política de dominios y filtro DNS local mediante VPN explícita:**
  - Verificador criptográfico (`PolicyBundleVerifier` con SHA-256 y `SHA256withECDSA`) y máquina de estados anti-downgrade con rollback de un paso (`PolicyBundleState`).
  - Política incorporada de prueba `test-domain-policy-v1` (CC0, dominios `.invalid`) verificada por SHA-256 en el arranque.
  - Servicio `DnsVpnService` opcional que evalúa únicamente paquetes DNS UDP IPv4/IPv6 no fragmentados contra las reglas locales, responde `NXDOMAIN` local para dominios bloqueados y delega el resto a `1.1.1.1:53` mediante socket protegido sin guardar historial.
  - Corrección verificada en AVD API 34 para el mapeo de día de la semana (`ScheduleWindow.dayIndexFromCalendarDayOfWeek`) y reconciliación de estado tras reinicio del proceso.
- **Límites conocidos declarados:**
  - No bloquea DoH/DoT, Private DNS, DNS sobre TCP, QUIC, acceso por IP directa, resolutores internos ni compite con otra VPN activa.
  - La validación en dispositivos físicos OEM, lector de pantalla, redes móviles reales y varios navegadores comerciales continúa abierta en [`COMPATIBILIDAD.md`](COMPATIBILIDAD.md).

## Plan de rollback y fix-forward

Dado que Android no permite instalar un APK con `versionCode` inferior sobre una instalación existente sin desinstalar primero (`INSTALL_FAILED_VERSION_DOWNGRADE`), la recuperación ante fallos en producción combina reversión de políticas en caliente y **fix-forward** de código:

1. **Mitigación inmediata en el dispositivo (sin esperar actualización):**
   - **Reglas o presupuestos erróneos:** la persona usuaria puede quitar la regla/presupuesto desde el panel principal o añadir una regla `Permitir` (que tiene precedencia determinista sobre `Bloquear`).
   - **Servicio de Accesibilidad:** desactivable en cualquier momento desde Ajustes de Accesibilidad de Android.
   - **Filtro DNS VPN:** detenible desde el botón `DETENER FILTRO DNS` en la app, la acción `DETENER` de la notificación persistente o desconectando/revocando la VPN en Ajustes de Android (`onRevoke` persiste `REVOKED` y libera el descriptor TUN).
2. **Rollback de paquetes de política (`PolicyBundleState`):**
   - Si una actualización firmada de política de dominios produce falsos positivos, `PolicyBundleState.rollback()` restaura inmediatamente el bundle anterior verificado y desactiva las actualizaciones automáticas hasta nueva decisión manual.
3. **Fix-forward de aplicación (nuevo `versionCode`):**
   - Si un defecto requiere cambio de binario, no se reutiliza ni reduce `versionCode`. Se crea un commit correctivo sobre `main` (o rama de mantenimiento), se incrementa `versionCode` (`N + 1`), se mantiene compatible el esquema JSON de `SharedPreferences` (`RuleStore` conserva compatibilidad hacia atrás con reglas sin bloque `schedule` tratándolas como `Siempre`, y falla cerrado a lista vacía ante corrupción), se verifican las suites JVM y lint vital, y se firma y publica el nuevo artefacto verificando su SHA-256.
