# ADR-004: VPN DNS local opcional y sin servidor remoto

- Estado: prototipo local; no apto para publicación hasta implementar un encaminador verificable y repetir validación de red
- Fecha: 2026-11-08

## Contexto

La política de dominio puede decidir una respuesta local, pero Android no permite interceptar el DNS de otras aplicaciones sin una capa de red. Un futuro `VpnService` sería una capacidad sensible y compite por el único túnel VPN de Android.

## Decisión de diseño

Proyect X declara un único `VpnService` protegido por `BIND_VPN_SERVICE`, preparado solamente tras consentimiento del sistema. El servicio acepta paquetes DNS UDP no fragmentados para IPv4/IPv6, los evalúa localmente, responde NXDOMAIN para bloqueos y delega el resto a `1.1.1.1:53` mediante un socket protegido que no conserva historial.

El servicio no tendrá servidor VPN remoto, inspección TLS ni contenido de navegadores. Su estado se muestra en una notificación de primer plano y pantalla local. El código limita paquetes a 4096 bytes DNS, timeout a 3 segundos, no persiste consultas y cierra la interfaz al detenerse o revocarse. Antes de declarar cobertura se debe demostrar red, cancelación, cierre de recursos y recuperación de fallos en AVD aislado.

La prueba controlada en AVD del 2026-11-09 aprobó consentimiento, servicio en primer plano, notificación y parada; sin embargo, una regla local de bloqueo para dominios controlados no bloqueó solicitudes nuevas desde WebView ni desde el resolvedor del sistema. La causa observada es que un túnel limitado a rutas del servidor DNS virtual no obliga de forma fiable a esos clientes a entregar sus consultas a la interfaz. Un túnel de ruta por defecto sin reenvío TCP/UDP completo rompería el tráfico restante y no es una solución aceptable. Hasta disponer de un encaminador completo, pruebas de red repetibles y revisión de privacidad, esta capacidad queda fuera de la publicación.

## Always-on y revocación

La primera entrega VPN declarará explícitamente que no soporta always-on hasta que el ciclo de vida esté validado. Android puede revocar el servicio; `onRevoke()` deberá cerrar la interfaz y resolver de forma idempotente. No se activará “bloquear conexiones sin VPN” desde la app ni se presentará como protección disponible para consumo general.

## Consecuencias

- El manifiesto declara `INTERNET`, servicio VPN y foreground service `specialUse`; la app solicita notificaciones antes de iniciarlo para hacer el estado visible.
- DNS TCP/cifrado, DoH/DoT, QUIC, IP directa, resolutores propios y VPN de terceros permanecen fuera de cobertura.
- La implementación deberá respetar las condiciones de Android sobre `BIND_VPN_SERVICE`, preparación de la persona y ciclo de vida. Fuente: [guía oficial de VPN](https://developer.android.com/develop/connectivity/vpn).
