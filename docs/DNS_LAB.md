# Laboratorio de cobertura DNS controlado

Versión de política: fixture propio `test-domain-policy-v1`.

## Resultado

| Casos controlados | Esperados | Aprobados | Cobertura del laboratorio |
| --- | ---: | ---: | ---: |
| Dominio bloqueado, subdominio, segundo bloqueo, allowlist y dominio sin coincidencia | 5 | 5 | 100% |

La medición se ejecuta en `DnsCoverageLabSmokeTest` con dominios reservados `.invalid`. Verifica la semántica del motor local: coincidencia de subdominio, prioridad `allow` y decisión de delegar cuando no corresponde bloquear.

## Condiciones y huecos

- No hay `VpnService`, socket, upstream real ni navegador en este laboratorio.
- No mide contenido adulto real, DoH/DoT, QUIC, Private DNS, IP directa, resolutores internos o VPN de terceros.
- “100%” se refiere únicamente a estos cinco casos sintéticos. No es una tasa de bloqueo web ni una promesa de cobertura ilimitada.

Un cambio de normalización, reglas o respuestas DNS debe mantener esta prueba y actualizar la tabla solo con evidencia reproducible.

## Validación de transporte AVD (2026-11-09)

El AVD API 34 confirmó el consentimiento VPN, el servicio en primer plano, la notificación y su parada limpia. También confirmó una limitación bloqueante: reglas temporales para `example.com`, `iana.org` y `nip.io` no impidieron cargas nuevas en WebView ni una resolución del sistema, incluso con Private DNS del AVD desactivado. La prueba limpió reglas y estado al terminar.

En una repetición posterior, AVD API 34 confirmó además la revocación de Android: con el servicio activo, Ajustes mostró `Proyect X — Connected`; al elegir **Disconnect**, `onRevoke()` persistió el estado `REVOKED`, informó que la VPN ya no estaba activa y el servicio dejó de aparecer en `dumpsys activity services`. Esto no sustituye las pruebas pendientes de reinicio del proceso, teléfono, Wi-Fi/datos y navegadores.

La simulación de crash con `adb shell am crash com.proyectx.focus` no recuperó el proceso durante la ventana observada: Android dejó un `ServiceRecord` con `app=null` y la notificación previa visible. Tanto `START_STICKY` como un experimento con redelivery explícito resultaron insuficientes en este AVD; se retiró el experimento y el criterio de recuperación permanece abierto. No se interpreta este resultado como recuperación correcta.

No se contabiliza como cobertura DNS. La implementación de transporte debe sustituirse o ampliarse con encaminamiento verificable antes de reabrir pruebas de bloqueo en navegador.
