# Runbook local de Proyect X

Este runbook no requiere recopilar URLs, historial ni registros de uso.

## Bloqueo erróneo de una aplicación

1. Abre Proyect X y quita la regla del paquete afectado, o añade una excepción `Permitir` si el bloqueo amplio es intencional.
2. Si no puedes abrir la app afectada, deshabilita temporalmente el servicio de Accesibilidad de Proyect X desde Ajustes de Android.
3. Para un reporte, anota solo paquete, versión de Proyect X, Android/API y pasos para reproducir. No adjuntes contenido de la app bloqueada.

## Presupuesto diario inesperado

1. Quita o ajusta el presupuesto desde Proyect X; también puedes revocar Acceso de uso desde Ajustes.
2. Verifica que no exista una regla de paquete adicional. Una regla `Permitir` prevalece sobre un presupuesto.
3. Registra fabricante, API, zona horaria y hora aproximada. Los agregados de `UsageStatsManager` pueden variar por OEM; no los presentes como evidencia exacta de actividad.

## Servicio detenido o sin intervención

1. Confirma que Accesibilidad sigue habilitada y que la regla/presupuesto continúa visible en Proyect X.
2. Reinicia manualmente el servicio desde Ajustes; no hay receptor de arranque que lo habilite sin decisión de la persona.
3. En un dispositivo con ahorro agresivo, documenta el fabricante y las condiciones. No pidas desactivar defensas del sistema como solución general.

## Filtro DNS detenido, revocado o con error

1. Revisa el estado en **Cobertura web**. Un error o una revocación significa que no hay filtrado DNS activo.
2. Detén el servicio desde la notificación, la app o Ajustes de VPN antes de cambiar otra VPN. Android permite un solo túnel VPN.
3. No intentes compensar un fallo activando always-on o “bloquear conexiones sin VPN”. Documenta red (Wi-Fi/datos), API, hora aproximada y estado mostrado, sin incluir dominios ni capturas de navegación.
4. No se debe activar una descarga o lista de terceros como respuesta urgente. Conserva la última política verificada y usa el rollback local si existe.

## Vulnerabilidad

Sigue `SECURITY.md`; usa el reporte privado de seguridad de GitHub al publicar el repositorio. Nunca compartas keystores, tokens, credenciales o datos de navegación en un issue.
