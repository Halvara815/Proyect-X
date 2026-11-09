# Investigación REA — BlockerX 5.0.61

Fecha: 2026-11-08

## Alcance y método

Artefacto analizado estáticamente: `BlockerX-5.0.61.apk`.

- Paquete observado: `io.funswitch.blocker`.
- SHA-256: `fdd8b3020d9dbd1a9983ee32b1a3b29c4b34aa88b1c00a289321f679c21faa5a`.
- Motor: JADX 0.7.1 a través de REA. El APK **no fue ejecutado**.
- Esta investigación no extrae ni reutiliza código, recursos, marcas, listas ni secretos. Las conclusiones son requisitos y riesgos de ingeniería independientes.

## Hallazgos observados

| Hallazgo estático | Evidencia REA | Implicación propia |
| --- | --- | --- |
| El APK contiene una implementación `MyVpnService` con `VpnService.Builder`, interfaz VPN, hilo propio y servicio en primer plano. | `ev_b30ac7e15a16e0dee7563b89b1e2cd4f6dd6a1854308ef90db864c96815ab2c2`, `ev_76e6d55087b0b2fb1d0b0d3efac28e587c5c36ec963c685db7dd2e23a0bc8453`, `ev_313e9a58587e6e37e25f64a27c0d6d6da3d682969ceddf553408e6fed381c1ce` | Un filtro de DNS local es una capa razonable para investigar, con ciclo de vida, cancelación y fallos diseñados desde el inicio. |
| La clase VPN tiene referencias estáticas desde el flujo de accesibilidad, arranque y utilidades de VPN. | `ev_f241cd566445d651b01a490ba1a252669c3c71b46b37981489c7ee8792138d85` | Proyect X debe separar `policy`, transporte y UI para que una capa no obligue a habilitar otra. |
| El servicio de accesibilidad incluye rutas que manejan paquete, texto, descripción y nodo fuente de eventos. | `ev_92acc7d954e54961c55f80dcf654be81200ab030027b3d508d24791ae3b8da86` y decompilación `ev_f30bcd394bffd9c0b861e92807607ed566540bc0cef2973bd3893f486e380b68` | No adoptaremos lectura amplia de texto/URLs como requisito base. El servicio actual de Proyect X continuará limitado al paquete en primer plano hasta tener consentimiento específico, política de datos y motivo verificable. |
| Hay clases de listas de sitios/palabras y una clase de ventana de bloqueo para contenido adulto. | `ev_0ad8b449c5aab16b08d85f1357381a95d4ca415727d8c642781cdaabaa7ed411`, `ev_8d6f3497c8c26eb6e3850eb590c2332bbcbbaf3606a68ee91599a98018cfa61d` | Una UX de explicación y lista permitida es necesaria para errores de clasificación; se diseñará de forma propia. |
| La ventana de bloqueo observada usa `WindowManager` y registra eventos de analítica de un tercero. | `ev_c406c87af64bed436933b41a6b1c033ff17a20cca4443fdb5f7824d6ce68cad6`, `ev_fa61ca639cdb2dbf42b4b0894fd7a7b51ab1eb70e8b0916231447526f8cd01e1` | Proyect X no copiará esa implementación ni añadirá analítica. Si luego se requiere una pantalla de intervención, será una UI propia con consentimiento y sin transmitir eventos. |
| Hay clases estáticas para seleccionar/listar aplicaciones y gestionar listas de apps bloqueadas. | `ev_f7ac3ecb87039c9e2e26187264c06db90fbe8bbdcb52232cb7d086b86c0117a7` | Proyect X incorpora un selector propio limitado a actividades de inicio visibles para Android, local y sin `QUERY_ALL_PACKAGES`; el inventario no se persiste. |
| Hay una clase estática de datos de uso de aplicación para estadísticas. | `ev_ed92b4516e97df56a636f540e0020a1e9dd1c73eade9034cc30060494faa45a6` | Separamos primero el cálculo de presupuestos de cualquier captura de uso. No se añadirá Usage Access sin divulgación y consentimiento propios. |

## Interpretación responsable

Estas observaciones estáticas no prueban cobertura real, eficacia, reglas de red ni comportamiento en tiempo de ejecución. Ofuscación, reflexión, cargas dinámicas y cambios de Android/OEM pueden dejar rutas sin observar. La investigación continuará por hipótesis concretas y cada nueva afirmación se anotará con evidencia REA y una prueba independiente.

## Fuentes de plataforma que condicionan el diseño

- Android documenta que un `VpnService` puede configurarse como always-on por la persona usuaria, pero el ciclo de vida y la conexión siguen siendo responsabilidad de la app: [VPN en Android](https://developer.android.com/develop/connectivity/vpn).
- El bloqueo de conexiones fuera de VPN es una opción del sistema; una app de consumo no puede prometer que nadie la desactive. El control estricto por política corresponde a un dispositivo o perfil administrado: [DevicePolicyManager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager).
- El uso de accesibilidad para automatización determinista exige propósito estrecho, divulgación prominente, consentimiento y declaración precisa para publicar en Play: [política de AccessibilityService](https://support.google.com/googleplay/android-developer/answer/10964491).
- La API agregada de uso de Android exige `PACKAGE_USAGE_STATS`; los intervalos pueden ser ampliados y, desde Android R, la API puede devolver `null` mientras el dispositivo está bloqueado: [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager).
