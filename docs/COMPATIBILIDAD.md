# Matriz de compatibilidad y cobertura

**Versión evaluada:** `0.1.0` (`versionCode` 1)
**Fecha de actualización:** 2026-11-09
**Commit público:** `aea22f7c6266458c0e2905a75ff611764fb10ed8`

Esta matriz distingue evidencia observada de hipótesis. No se debe extrapolar una prueba de emulador a dispositivos físicos.

| Área | Entorno probado | Estado | Límite conocido |
| --- | --- | --- | --- |
| Instalación y lanzamiento | AVD `Blovix_API_34`, Android API 34 | Verificado | No sustituye una prueba en fabricante físico |
| Fuente y orientación | AVD `Blovix_API_34`, fuente 1.30 y horizontal | Verificado para lanzamiento | No prueba lector de pantalla, layout visual completo ni gama baja |
| Compilación | JDK 17, compile SDK 37, min SDK 23, target SDK 37 | Verificado | No prueba comportamiento runtime en cada API/OEM; el AVD disponible sigue en API 34 |
| Reglas y horarios | Smoke tests JVM | Verificado | No ejercita interfaz ni servicio Android |
| Presupuesto diario | Build e integración AVD sin conceder permiso | Parcial | Falta conceder/revocar Usage Access y medir precisión en OEM físicos |
| Accesibilidad | Flujo instalado en AVD | Parcial | Fabricantes pueden limitar eventos o lanzamientos desde servicios |
| Navegadores y web | WebView AVD API 34 (`113.0.5672.136`) + resolvedor nativo | Parcial (verificado en AVD) | Bloqueó `1-1-1-1.nip.io` (`ERR_NAME_NOT_RESOLVED`) y `7-7-7-7.nip.io` (`unknown host`) con regla `nip.io` y permitió `example.com`; faltan varios navegadores comerciales y pruebas en redes físicas. No cubre DoH/DoT, QUIC, TCP DNS ni IP directa |
| Wi-Fi, datos, IPv4/IPv6 | Códec JVM IPv4/IPv6 | Parcial | Falta validar la interfaz VPN, el upstream y recuperación en AVD para cada red |
| Revocación VPN | AVD API 34, Ajustes del sistema | Verificado | Desconectar Proyect X invoca `onRevoke`, persiste `REVOKED` y detiene el servicio; faltan reinicios/red física |
| Modo DNS sin Accesibilidad | AVD API 34 | Verificado en AVD | `enabled_accessibility_services=null` con `DnsVpnService` activo; esto prueba independencia de permisos, no cobertura web |

Cada futura entrada debe incluir versión exacta, fecha, condiciones, caso de prueba y resultado; “compatible” requiere evidencia reproducible, no solo compilación.
