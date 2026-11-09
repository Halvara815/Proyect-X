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

El AVD API 34 confirmó el consentimiento VPN, el servicio en primer plano, la notificación y su parada limpia. En la primera prueba (realizada con fecha domingo en el emulador), las reglas de dominio parecieron no aplicarse por dos causas combinadas que fueron diagnosticadas y corregidas:

1. **Defecto de índice de día en domingo (`Calendar.SUNDAY = 1`):** `DnsVpnService` pasaba `now.get(Calendar.DAY_OF_WEEK) - 1` (`0` en domingo, que en `ScheduleWindow` es `MONDAY`), pero si además ocurría en otra zona o ventana, difería del cálculo `(DAY_OF_WEEK + 5) % 7` de `AppBlockAccessibilityService`. Se unificaron ambos servicios mediante `ScheduleWindow.dayIndexFromCalendarDayOfWeek(int)` con prueba unitaria para los 7 días.
2. **Estado obsoleto tras `adb install -r` o muerte del proceso:** Android detenía el proceso de `DnsVpnService` al reinstalar o tras `am crash`, pero `proyect_x_dns_vpn.xml` conservaba `STATE_ACTIVE`, haciendo que la UI mostrara el filtro como activo cuando ningún túnel `tun0` estaba vivo. Se añadió reconciliación en `DnsVpnService.readStatus` frente al estado real en memoria del servicio.

Tras corregir ambas causas y activar limpiamente `DnsVpnService` en `emulator-5554` (AVD API 34, `Private DNS = off`, red `netId=112`, interfaz `tun0` con `10.239.0.2/32` y `fd12:3456:789a::2/128`):

- `ping -c 1 -W 2 7-7-7-7.nip.io` (cubierto por regla local `BLOCK nip.io`) devolvió `ping: unknown host 7-7-7-7.nip.io` (`NXDOMAIN` local).
- `WebView Browser Tester 113.0.5672.136` al abrir `http://1-1-1-1.nip.io/` devolvió `Webpage not available — net::ERR_NAME_NOT_RESOLVED`.
- `WebView Browser Tester 113.0.5672.136` al abrir `http://example.com/` (no bloqueado) resolvió vía `1.1.1.1:53` con socket protegido y cargó `Example Domain`.

En una prueba separada, AVD API 34 confirmó la revocación desde Ajustes de Android (`onRevoke` persistió `REVOKED` y detuvo el servicio) y la reconciliación visual tras `adb shell am crash com.proyectx.focus` (`Filtro DNS detenido tras reinicio del proceso; puedes reactivarlo cuando quieras`). Siguen abiertos los criterios que exigen pruebas de reinicio completo del teléfono, conmutación Wi-Fi/datos móviles reales y varios navegadores comerciales fuera del WebView del emulador.
