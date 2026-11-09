# Matriz RAE de capacidades para Proyect X

Actualizada: 2026-11-09. Esta matriz transforma evidencia estática y capturas aportadas por el titular en decisiones de producto propias. No autoriza reutilizar código, imágenes, textos, marca, iconos, navegación ni mecanismos comerciales de terceros.

## Evidencia revisada

| Fuente | Identidad | Alcance y límite |
| --- | --- | --- |
| APK externo | `BlockerX-5.0.61.apk`, SHA-256 `fdd8b3020d9dbd1a9983ee32b1a3b29c4b34aa88b1c00a289321f679c21faa5a` | REA/JADX estático; no se ejecutó y la firma no se verificó. |
| ZIP de arquitectura aportado | SHA-256 `55401a5bf3c803fc1d891971db241e2116d00e17d107bd945d51da40a91c9b66` | Inventario REA completo: 53 nodos, 62 ocurrencias. Se analizaron de forma inerte siete fuentes TypeScript seleccionadas, sin ejecutarlas. |
| Capturas aportadas | 19 imágenes locales del 2026-11-09 | Evidencia de estados fotografiados; no demuestra todas las rutas ni su lógica. |

## Hallazgos que orientan necesidades, no diseño

| Área observada | Evidencia | Decisión propia | Estado |
| --- | --- | --- | --- |
| Navegación con destinos y menú | APK: `ev_49dc75f196a48c7e4264d53f53a3620765fd9319ffbbb12c20a56c7003f43d69`; ZIP: `ev_5c8797fee477966f94e7df1f62e828fe2953612458b690e02689e976f33b3804` | Cuatro destinos propios y un centro local de herramientas, sin panel comercial ni comunidad. | Implementado |
| Reglas de dominio, paquete y palabra | APK: `ev_34c1588f06ad08dbfa1879b016dc4ed6557720d04abee9dce752cda66f90d3c9`, `ev_dd6faee971d4a4980b253799db612d9e412fc89815063047351e84c5c4651ff6` | Regla local con precedencia Allow, horario, eliminación y switch real de estado. | Implementado |
| Accesibilidad y cobertura | Capturas aportadas; APK de accesibilidad documentado en `INVESTIGACION_REA_BLOCKERX_5_0_61.md` | Aviso visible en Inicio, aprobación manual de Android y alcance limitado a paquete en primer plano. | Implementado |
| Reinicio y motivación | APK: `ev_1c67a8ad34362d94247457382b7b22309b04ac40b314974815491e14c4ee5baa`, `ev_11760351db0b8445dbc434cb6d6d5a4b70aae1845c6b730fc7011c9ebaae60b7` | Reinicio no punitivo y racha local: cuenta sólo días con al menos una sesión de enfoque iniciada y guardada; no usa navegación, compras ni afirmaciones de recuperación. | Implementado y cubierto por smoke test JVM |
| Exportar o compartir configuración | ZIP: traza estática `ev_0fea86807360828058343bc83487eef0988ef7b7e2a2253503a5cb32626ba84b` tiene cobertura parcial | Exportación JSON elegida por la persona y selector nativo para compartir; sin sincronización remota. | Implementado |
| Privacidad, ayuda e idioma | Capturas aportadas y ZIP: traza de accesibilidad `ev_6b1ef3d92c19dfff223c05f86845d2b1a72b1cb7a60c7673db52fdca76a99b22` | Diálogos propios de límites, ayuda y ajustes de idioma del sistema. La única traducción actual es español; no se simula soporte multilingüe. | Implementado / traducciones pendientes |
| Feedback | APK: `ev_6f4c4fa7d95ea96a17d754f511c1a64a734e12d6add2ecef3f351e8c1cf03916` | Compositor local que abre el selector de Android sólo tras confirmación. No hay envío automático ni telemetría. | Implementado |

## Exclusiones expresas

- Cuenta, autenticación, comunidad, monedas, ofertas, pago, suscripciones, perfiles remotos y analítica.
- Cuestionarios o porcentajes de adicción, promesas de recuperación, comparaciones clínicas o diagnósticos.
- Reglas que pretendan cobertura total de la web; las exclusiones DNS y de accesibilidad siguen visibles.
- Recursos del ZIP: incluye imágenes y nombres de componentes de referencia, por lo que sus assets y composición no se importan ni se versionan.

## Siguientes slices verificables

1. Crear gráfico de barras propio a partir de un historial local de sesiones, sin datos de navegación ni afirmaciones de eficacia.
2. Incorporar importación validada de la exportación JSON, con vista previa, confirmación y rollback ante archivo inválido.
3. Internacionalizar recursos reales, empezando por inglés, y activar el selector de idioma sólo cuando existan todas las cadenas revisadas.
4. Probar el menú, la exportación y switches en dispositivo físico antes de beta.

## Límites de certeza

La evidencia del APK y el ZIP es estática y parcial. Los nombres de clases, capturas y trazas no prueban ejecución. Cada capacidad de Proyect X se considera implementada sólo cuando su código propio, pruebas y comportamiento observado la respaldan.
