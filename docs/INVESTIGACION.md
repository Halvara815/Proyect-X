# Investigación de referencia para Proyect X: BlockerX Lite

Fecha de corte: 2026-11-08 (America/Tegucigalpa).

## Alcance y método

Esta investigación identifica el producto, separa hechos públicos de inferencias y prepara un análisis estático reproducible. Se usó búsqueda web y REA (Reverse Engineering Assistant) sin ejecutar la aplicación ni interceptar tráfico.

El objetivo es una reconstrucción independiente de comportamientos de producto, no recuperar ni reutilizar código, recursos, marca o servicios del proveedor.

## Identidad confirmada

| Campo | Valor | Confianza |
| --- | --- | --- |
| Nombre público | BlockerX Lite: No distractions / Bloquear sitios | Alta |
| Paquete Android de la ficha actual | `org.atmana.blockerx` | Alta |
| Desarrollador publicado | Atmana Tech / Atmana Tech - FZCO | Alta |
| Categoría | Productividad | Alta |
| Plataforma | Android; la familia también tiene ficha de iOS | Alta |
| Versión mostrada | 1.0.25 | Media: es una ficha de tienda cambiante |

Fuentes primarias de producto:

- [Ficha de Google Play en español](https://play.google.com/store/apps/details?hl=es&id=org.atmana.blockerx)
- [Ficha de Google Play en inglés](https://play.google.com/store/apps/details?hl=en_CA&id=org.atmana.blockerx)
- [Ficha de App Store](https://apps.apple.com/dk/app/blockerx-adult-content-blocker/id1522854086)

## Capacidades declaradas públicamente

Las siguientes son afirmaciones del editor en Google Play; no equivalen a una verificación de implementación.

| Capacidad | Estado | Fuente |
| --- | --- | --- |
| Bloquear contenido adulto, apps, sitios y palabras clave | Declarada | Google Play |
| Listas de bloqueo y permitidos | Declarada | Google Play |
| Búsqueda segura | Declarada | Google Play |
| Bloqueo entre navegadores con `VpnService`, sólo si se activa | Declarada | Google Play |
| Servicio de accesibilidad para bloquear contenido | Declarada | Google Play |
| Ventana de superposición para mostrar la pantalla de bloqueo | Declarada | Google Play |
| Mensaje personalizado y redirección tras bloqueo | Declarada como Premium | Google Play |
| Socio de responsabilidad e informe de historial | Declarada como Premium | Google Play |
| Sincronización de listas entre dispositivos | Declarada como Premium | Google Play |

La ficha indica que la aplicación puede recoger y compartir ciertos datos; el desglose, contratos de red, destinatarios y condiciones reales no están demostrados por esta investigación.

## Resultado de REA

| Operación | Resultado | Consecuencia |
| --- | --- | --- |
| `list_adb_devices` | No ejecutable: no se encontró `adb` configurado en el entorno | No se inspeccionó ningún dispositivo ni paquete instalado |
| `inspect_android_package` | Ejecutado sobre un APK recibido | El artefacto resultó ser `com.uptodown`, no el objetivo `org.atmana.blockerx` |

Las búsquedas públicas identificaron descargas de terceros, pero sus sitios bloquearon la descarga automatizada. No se intentó eludir esos controles. Un APK debe provenir de una fuente autorizada y conservarse sólo localmente.

## Artefacto recibido y descartado como objetivo

El 2026-11-08 se analizó de forma estática el archivo proporcionado `C:\Users\veget\Downloads\uptodown-io.funswitch.blocker.apk`.

| Campo | Observación |
| --- | --- |
| SHA-256 | `8f8a7b2416d84d64d418dc4e194cd8ab6f4394d51651bf2e1aac47207503fd92` |
| Paquete declarado | `com.uptodown` |
| Versión declarada | `7.44` (código `744`) |
| Clase de aplicación | `com.uptodown.UptodownApp` |
| Resultado | **Descartado** para la investigación de BlockerX Lite, cuyo paquete esperado es `org.atmana.blockerx` |
| Evidencia REA | `ev_709f19030cb68a43c2d74e81d63549c076db70765662f2d4472d3084e80078ce`; manifiesto: `ev_cc9478d6e4cf3b25cf6ec59cd7db16f05753d3ca252b9594b1250898ef0c765d` |

El manifiesto observado solicita capacidades de instalación y gestión de paquetes, almacenamiento, cuentas y otras propias de una tienda de aplicaciones; no se atribuyen a BlockerX Lite. Las firmas del APK no fueron verificadas y el análisis estático no ejecutó el archivo.

## Artefacto de referencia analizado

El archivo `C:\Users\veget\Downloads\BlockerX-5.0.61.apk` es coherente con una versión distinta/anterior de BlockerX y se usa sólo como evidencia estática de comportamiento, no como fuente de código o recursos.

| Campo | Observación |
| --- | --- |
| SHA-256 | `fdd8b3020d9dbd1a9983ee32b1a3b29c4b34aa88b1c00a289321f679c21faa5a` |
| Paquete | `io.funswitch.blocker` |
| Versión | 5.0.61, código 5061 |
| SDK | mínimo 23, objetivo 36 |
| Inventario | 24 804 clases y 2 789 recursos |
| Firma | No verificada por REA |
| Evidencia base REA | `ev_2cfdea6a722e6aacf6134914681befbc2d2c562b90ff06d7fca2811e5ad0a28f` |

### Hallazgos estáticos relevantes

- El manifiesto declara permisos de uso, superposición, cámara, micrófono, red, facturación, notificaciones, inicio tras reinicio y otros. También permite tráfico en claro. Esto describe el APK observado; no prueba su uso en tiempo de ejecución. Evidencia: `ev_5eddd238049125fb1f327d6992e299ab91b7c244ab9e7f54ca418c8095c82b60`.
- El paquete contiene `MyAccessibilityService`, con listas de dominios, palabras y permitidos, y nombres de rutas para bloqueo de navegador, contenido y apps. Evidencia: `ev_bf65d0e999c3880cb5a598c54ebb9f641f0de8eecadfe734f9ad760b1881b801`, `ev_1272520ce0ff05205fe7cff664a507421cfa25368946fadf9920c204496c6261`.
- Su método `onAccessibilityEvent` lee nombre de paquete, clase, texto y descripciones de eventos de accesibilidad antes de delegar su tratamiento. Es una observación de código estático, no una captura de datos real. Evidencia: `ev_566e7c6b3cd8c0772af50360fd30e19ad8764bfd1e7b48f26009d37a9c710187`.
- El paquete contiene `MyVpnService`, con una construcción de `VpnService`, notificación de primer plano y una hebra VPN. Evidencia: `ev_515ca9e7020c22789b32ec4b7f07a659026c904837f1e1a8b246adfc54ce66d6`, `ev_db7a63ec452dc60a1e375ccbb8eb68ae94896437445795ee952244e701b43189`, `ev_c8fb525b127360d88818fa3149184b8fa0469befef76438db436ea6161f80a03`.

### Decisión para Proyect X

Proyect X no replica la VPN ni los permisos de red, superposición, cámara, micrófono, almacenamiento o uso del APK observado. La primera entrega incluye únicamente un servicio de accesibilidad opcional y protegido por `BIND_ACCESSIBILITY_SERVICE`: recibe cambios de ventana y procesa sólo el nombre del paquete, sin acceso al contenido de ventanas. Su manifiesto no declara permisos solicitables, bloquea tráfico en claro, prohíbe copias de seguridad y conserva reglas sólo dentro del espacio privado de la aplicación. Cualquier futura propuesta de VPN o de acceso al contenido de accesibilidad requiere una ADR nueva, consentimiento granular, pruebas en dispositivo aislado y revisión independiente de privacidad y abuso.

## Siguiente evidencia requerida

Con un archivo autorizado cuyo manifiesto declare `org.atmana.blockerx`, usar REA de forma estática:

```powershell
# El análisis no ejecuta el APK.
# En una sesión de Codex con REA/JADX configurados:
inspect_android_package(path="artifacts/blockerx-lite.apk", detail="summary")
```

Después de comprobar la identidad, investigar por hipótesis, una cada vez:

1. Manifest: permisos, servicios, actividades, receptores y `VpnService`.
2. Clases relacionadas con listas, coincidencia de URL/palabra clave, accesibilidad y superposición.
3. Recursos y textos de la pantalla de bloqueo.
4. Persistencia local, configuración de privacidad y clientes de red.
5. Sólo si el análisis estático no basta, observación controlada en un dispositivo de prueba autorizado, sin cuentas personales ni tráfico de terceros fuera del alcance.

Cada conclusión futura debe registrar: hash SHA-256 del artefacto, versión, operación REA, identificador de evidencia, ubicación, limitaciones y nivel de confianza.

## Límites y decisiones

- Se inspeccionó estáticamente un APK recibido, pero se descartó por no corresponder al paquete objetivo; no se ejecutó ningún APK.
- Los datos de tiendas pueden cambiar por región y fecha.
- No se reproduce el mecanismo de desinstalación/“accountability partner” hasta definir requisitos de consentimiento, recuperación y privacidad.
- La futura app usará identidad visual y nombre propios.
