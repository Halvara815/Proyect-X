# Política de privacidad de Proyect X (prelanzamiento)

Última actualización: 2026-11-08.

## Principio

Proyect X funciona localmente por defecto, sin cuentas, anuncios, analítica ni SDK de terceros. Una persona puede activar de forma afirmativa un filtro DNS opcional mediante el consentimiento VPN de Android. Esta política describe la versión de prueba actual y debe revisarse antes de publicar.

## Datos que permanecen en el dispositivo

- Reglas creadas por la persona (dominio, palabra o paquete, acción y horario).
- Estado local de la sesión de enfoque (inicio y final).
- Presupuestos elegidos por paquete (paquete y minutos diarios).
- Estado mínimo del filtro DNS (activo, detenido, revocado o error y su fecha). No contiene dominios, URLs ni paquetes DNS.

Estos datos se guardan en almacenamiento privado de la app. La configuración de copia de seguridad y transferencia de Android está deshabilitada para la app.

## Datos que no se recopilan ni transmiten

- URLs, historial de navegación, texto de páginas, pulsaciones o contenido de ventanas.
- Inventario completo de aplicaciones: el selector consulta solo aplicaciones con lanzador visibles para elegir una y no guarda esa lista.
- Totales de uso: si se habilita manualmente Acceso de uso, la app consulta el agregado local del día para el paquete con presupuesto y lo descarta tras compararlo.
- Identificadores publicitarios, ubicación, contactos, archivos compartidos, audio, cámara o diagnósticos remotos.

## Tráfico DNS opcional

El prototipo DNS se mantiene detenido hasta que la persona lee la divulgación, concede notificaciones y aprueba el diálogo VPN de Android. Cuando un paquete DNS UDP alcanza la interfaz local, se evalúa contra reglas de dominio. Una coincidencia bloqueada recibe una respuesta NXDOMAIN local y no sale del dispositivo.

Las consultas permitidas que alcanzan el prototipo se envían a `1.1.1.1:53` mediante un socket protegido para que no entren de nuevo en el túnel. Proyect X no las registra ni las usa para analítica, pero el resolvedor puede recibir la dirección IP y la consulta conforme a su propia política. La validación AVD de 2026-11-09 mostró que el resolvedor del sistema y WebView pueden no entregar consultas a un túnel de rutas DNS parciales; por ello este componente no se presenta como filtro web ni se publica aún. El transporte no está cifrado y no cubre DNS TCP, DoH/DoT, QUIC, IP directa ni resolutores internos de otras aplicaciones.

## Permisos opcionales

- **Accesibilidad:** se habilita desde Ajustes para detectar el paquete en primer plano y mostrar una intervención por regla o presupuesto. No se solicita contenido de ventanas.
- **Acceso de uso:** se habilita desde Ajustes únicamente después de una divulgación dentro de la app. Sin este acceso, los presupuestos no se aplican.
- **VPN DNS:** se solicita únicamente después de una divulgación separada y del consentimiento del sistema. El servicio se ejecuta en primer plano con una notificación visible y puede detenerse desde la app, notificación o Ajustes.
- **Notificaciones:** se solicitan solo antes de iniciar la VPN, para que el estado de protección no quede oculto.

Ambos permisos pueden revocarse en cualquier momento en Ajustes. Quitar una regla o presupuesto detiene su uso; deshabilitar Accesibilidad detiene la intervención. Desinstalar elimina los datos locales conforme a Android.

## Cambios futuros

Una actualización de políticas, otro upstream DNS, soporte de DNS cifrado o telemetría opcional requerirá una versión actualizada de esta política, divulgación dentro de la app y una decisión de arquitectura publicada antes de activarse.
