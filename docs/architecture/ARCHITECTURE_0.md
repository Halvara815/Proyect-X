# Architecture 0 — Proyect X

Fecha: 2026-11-08

## Evidencia observada

- Android nativo en Java 17, un único módulo `app`, `minSdk 23`, `targetSdk 37` y Gradle wrapper 9.9.0 con SHA-256 fijado.
- El motor puro (`Rule`, `RuleEngine`) normaliza dominios, palabras y paquetes; una regla `ALLOW` vence a una regla `BLOCK`.
- `RuleStore` guarda reglas como JSON en `SharedPreferences` privadas. `FocusSessionStore` persiste solo inicio/fin del temporizador local.
- `AppBlockAccessibilityService` recibe el evento de cambio de ventana y evalúa únicamente el nombre de paquete. No solicita contenido de ventanas, URLs ni texto de otras apps. Con Acceso de uso explícito y un presupuesto configurado, consulta solo el agregado local del día para ese paquete y no lo persiste.
- No existen permisos de cuentas, analítica, nube, superposición (`SYSTEM_ALERT_WINDOW`), `QUERY_ALL_PACKAGES` ni receptor de arranque. `PACKAGE_USAGE_STATS` es opcional y se usa exclusivamente para presupuestos locales tras divulgación y autorización manual. `BIND_VPN_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` e `INTERNET` existen únicamente para el resolvedor DNS UDP local opcional (`DnsVpnService`, ADR-004).
- La automatización de GitHub Actions compila las variantes debug/release y ejecuta las pruebas JVM del motor.

## Flujo crítico actual

Persona → crea una regla o presupuesto → validación pura → almacenamiento local → servicio de accesibilidad opcional recibe paquete en primer plano → motor decide → si hay bloqueo o presupuesto agotado con Acceso de uso autorizado, muestra una intervención local.

El temporizador de sesión es un artefacto local de intención: muestra el tiempo elegido, pero no modifica silenciosamente las reglas existentes.

## Límites y riesgos visibles

- La accesibilidad se debe activar manualmente y Android/OEM pueden interrumpir sus eventos.
- Volver al inicio es una intervención de mejor esfuerzo; no equivale a control parental ni evita ADB, modo seguro, desinstalación o restablecimiento.
- Bloqueo de dominios y palabras está modelado y probado, pero esta entrega no inspecciona navegación ni contenido de terceros para aplicarlos en runtime.
- Los bundles de política tienen verificación de firma/hash y una máquina de estados de rollback pura, pero no tienen endpoint, clave de producción ni almacenamiento de artefactos. Una beta DNS UDP usa reglas locales y no integra todavía un bundle de producción.
- `SharedPreferences` es suficiente para el volumen actual; una futura migración a Room requerirá una migración probada y reversible.
