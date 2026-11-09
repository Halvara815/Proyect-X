# Revisión de permisos y política de publicación

Fecha de revisión: 2026-11-08. Alcance: versión 0.1.0 y manifiesto actual.

## Declaraciones actuales

| Capacidad | Estado | Revisión requerida antes de publicar |
| --- | --- | --- |
| `AccessibilityService` | Declarado, opcional; observa solo paquete de ventana | Explicar el propósito de bloqueo de apps, divulgación in-app y confirmar que no se usa para contenido/URLs/pulsaciones |
| `PACKAGE_USAGE_STATS` | Declarado, opcional; total local de día para presupuesto elegido | Divulgación previa, enlace voluntario a Ajustes y política de privacidad consistente |
| VPN / `VpnService` | Declarado; DNS UDP opcional tras consentimiento del sistema | Confirmar declaración de VPN/FGS special-use en Play, consentimiento separado, notificación visible, límites explícitos y pruebas de ciclo de vida antes de publicar |
| `INTERNET` | Declarado solo para el upstream DNS `1.1.1.1:53` cuando la VPN DNS se activa | Confirmar política y divulgación del resolvedor; no hay backend, anuncios ni analítica |
| Notificaciones | Solicitadas solo antes de iniciar la VPN DNS | Verificar que el servicio no inicie si la notificación visible es rechazada |

## Gate antes de cualquier cambio sensible

1. Comparar el manifiesto y APK final contra esta tabla.
2. Actualizar `PRIVACIDAD.md`, pantallas de divulgación y formulario de Data Safety de Play según datos realmente procesados.
3. Revisar la política de Google Play aplicable a AccessibilityService, VPN y foreground service `specialUse`.
4. Verificar que no se active always-on, Device Owner ni captura de contenido de forma implícita.
5. Registrar fecha, versión, revisor y evidencia de pruebas en la matriz de compatibilidad.

Fuentes para la próxima revisión: [política de servicios de accesibilidad de Google Play](https://support.google.com/googleplay/android-developer/answer/10964491) y [guía oficial de VPN Android](https://developer.android.com/develop/connectivity/vpn).
