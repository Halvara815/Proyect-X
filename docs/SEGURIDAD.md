# Seguridad de Proyect X

## Postura

La seguridad se reduce con capas; no existe una afirmación honesta de que una app sea imposible de vulnerar. El objetivo actual es disminuir la superficie de ataque y limitar el impacto de un fallo.

## Límites de la versión 0.1.0

- No pide `INTERNET`, VPN, superposición, cámara, micrófono, contactos, almacenamiento compartido ni notificaciones. Declara `PACKAGE_USAGE_STATS` para un presupuesto local: solo después de divulgación, configuración explícita y habilitación manual consulta el agregado de uso del día. No almacena ni transmite esos totales.
- Declara `ACCESS_NETWORK_STATE` únicamente para detectar la presencia de un transporte VPN activo y advertir que Proyect X no puede competir con él; no inspecciona tráfico ni configuración de la VPN.
- Incluye un servicio de accesibilidad **opcional**, protegido por `BIND_ACCESSIBILITY_SERVICE`. Solo recibe cambios de ventana y evalúa el nombre de paquete; no puede recuperar contenido de ventanas. Si ambas autorizaciones están activas y hay presupuesto configurado, compara ese paquete con el agregado local de hoy.
- `AccessibilityPrivacyBoundarySmokeTest` verifica en CI que la configuración siga limitada a `typeWindowStateChanged`, `canRetrieveWindowContent=false` y que el servicio no introduzca APIs de contenido de ventana.
- No tiene cuenta, backend, anuncios, pagos, analítica ni SDK de terceros.
- Almacena las reglas en preferencias privadas de la app; no guarda historial de navegación ni URLs visitadas.
- No existen secretos de servicio, cuentas ni claves privadas en la app. CI rechaza patrones obvios de claves/credenciales; una capacidad futura que requiera secreto deberá usar gestión de claves de Android y una migración revisada.
- Desactiva copia de seguridad, transferencia de datos y tráfico HTTP en claro desde el manifiesto.
- Expone `MainActivity`, necesaria para el lanzador, y el servicio de accesibilidad que solo el sistema puede enlazar por `BIND_ACCESSIBILITY_SERVICE`. `DnsVpnService` no es exportado y exige `BIND_VPN_SERVICE`.

## Amenazas y controles

| Riesgo | Control actual | Límite residual |
| --- | --- | --- |
| Entrada malformada o ambigua | Normalización estricta de dominios con IDN y validación de palabras | Las reglas se administran por la propia persona usuaria |
| Una regla de bloqueo demasiado amplia | Las excepciones `Permitir` tienen precedencia explícita; motor probado | La semántica debe revisarse antes de ampliar tipos de regla |
| Exfiltración de datos | Sin permiso ni cliente de red | Un dispositivo comprometido puede leer datos de cualquier app sin sus protecciones normales |
| Lectura sensible de tiempo de uso | Divulgación previa, autorización de sistema separada, consulta solo para paquete/presupuesto configurado y sin persistir totales | OEM puede reportar agregados con diferencias; si se revoca el permiso se falla abierta |
| Copia/transferencia del respaldo | `allowBackup=false` y reglas de extracción que excluyen preferencias | No sustituye el cifrado integral del dispositivo |
| Componentes Android invocados por terceros | La actividad lanzadora es exportada; el servicio de accesibilidad también, pero sólo el sistema puede enlazarlo por `BIND_ACCESSIBILITY_SERVICE` | Toda nueva actividad, servicio o receptor requiere revisión de exportación y permisos |
| Manipulación del APK | Minificación en release y firma de distribución pendiente | La firma/Play Integrity no protege un dispositivo rooteado ni evita modificación fuera de canales confiables |

## Reglas para capacidades sensibles futuras

VPN y cualquier ampliación de accesibilidad son capacidades de alto impacto. La integración actual no lee contenido; no se añadirá acceso a contenido ni VPN sin:

1. ADR que demuestre por qué el bloqueo local no basta.
2. Consentimiento independiente y explicación visible de los datos observables.
3. Procesamiento local por defecto; sin registrar texto, URL completa o contenido de eventos.
4. Interruptor claro, desinstalación posible y recuperación segura.
5. Pruebas de abuso, revisión de privacidad y verificación en un dispositivo de pruebas.

## Gates antes de publicar

1. `RuleEngineSmokeTest` y `:app:assembleDebug` deben pasar.
2. Ejecutar análisis de dependencias y secretos en CI.
3. Firmar release con una clave almacenada fuera del repositorio; nunca versionar el keystore.
4. Revisar `AndroidManifest.xml` y el APK final para permisos y componentes exportados.
5. Hacer prueba manual de creación, borrado y precedencia de reglas en un emulador o dispositivo de prueba.
6. El workflow de GitHub ejecuta el motor de reglas y compila debug/release con permisos de token limitados a lectura de contenido.
7. Verificar presupuesto diario en dispositivos físicos antes de declarar compatibilidad OEM o precisión de minutos.
8. Gradle verifica SHA-256 de dependencias y plugins mediante `gradle/verification-metadata.xml`. Al actualizar dependencias, regenerar con `--write-verification-metadata sha256`, revisar el diff y ejecutar los builds sin caché de configuración.

## Validación registrada

- `RuleEngineSmokeTest`: **PASS**. Cubre normalización, bloqueo de subdominio, excepción permitida y rechazo de un dominio con puerto.
- `:app:assembleDebug`: **PASS**.
- `:app:assembleRelease`: **PASS**, incluyendo R8, lint vital y verificación de checksums de dependencias.
- La validación actual del APK con `aapt` confirma `PACKAGE_USAGE_STATS` y la ausencia de `QUERY_ALL_PACKAGES`. La próxima inspección REA debe regenerar hashes y evidencia del manifiesto antes de publicación.

La firma criptográfica del APK no fue validada por REA. La publicación exige una clave de release gestionada fuera de Git y una verificación independiente del artefacto firmado.
