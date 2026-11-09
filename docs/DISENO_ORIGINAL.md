# Diseño original de Proyect X

## Decisión

Proyect X tendrá una identidad visual y una interfaz propias. Las referencias externas pueden ayudar a detectar necesidades de uso —por ejemplo, explicar un permiso o mostrar el estado de protección—, pero no son una fuente de diseño, código, recursos ni textos.

Esta regla cubre tanto el APK analizado mediante REA como el archivo de arquitectura recibido el 9 de noviembre de 2026. Este último se inspeccionó sólo como evidencia estática; se identificó como una maqueta TypeScript de AI Studio que incluye imágenes y menciones de una aplicación de terceros. No se ejecutó, no se incorporó al repositorio y sus archivos extraídos permanecen fuera del espacio de trabajo.

## Límites de no incorporación

- No usar marcas, nombres, logotipos, capturas, ilustraciones, textos, iconos ni recursos de las referencias.
- No trasladar sus jerarquías de pantalla, menús, promociones, mecánicas de moneda, patrones decorativos ni composición de tarjetas.
- No reutilizar sus colores exactos, tokens CSS, nombres de componentes, código TypeScript o estructura de navegación.
- No representar la protección como absoluta ni copiar afirmaciones comerciales. La interfaz debe describir la cobertura real según el modo activado y el dispositivo.
- No importar el ZIP de referencia al código, a los assets ni al historial de Git.

Mencionar una referencia en un documento de investigación para delimitarla no concede permiso para reutilizar su expresión creativa.

## Dirección visual propia

La dirección de Proyect X será **calma práctica**: un espacio de trabajo sereno, cálido y legible que ayude a tomar una decisión, en vez de una consola de vigilancia o un producto de premios.

- Fondo lino: `#F6F2EA`.
- Superficie clara: `#FFFFFF`.
- Tinta bosque: `#16332D`.
- Acción principal verde río: `#167A68`.
- Acción de enfoque lima suave: `#C7F36B`.
- Riesgo o advertencia terracota: `#CC5B38`.
- Texto secundario salvia gris: `#66756F`.

Los valores son un punto de partida independiente, no una extracción de la referencia. Antes de implementarlos se validará el contraste de cada combinación de texto, icono y estado; ningún color será el único indicador de error, bloqueo o éxito.

## Modelo de navegación y componentes

La navegación inicial tendrá cuatro destinos explícitos: **Inicio**, **Protección**, **Reglas** y **Privacidad**. Cada uno debe resolver una tarea distinta y permitir volver al estado anterior sin rutas ocultas.

- **Inicio:** una síntesis breve de la protección activa, la siguiente sesión y una acción clara para revisar el plan.
- **Protección:** permisos, presupuestos y alcance real del filtro DNS local antes de activarlo.
- **Reglas:** crear y revisar reglas de dominio o categoría, con prioridad visible y un resultado de ejemplo que no exponga historial sensible.
- **Privacidad:** mostrar permisos concedidos, alcance real de la VPN local, datos guardados y acciones de borrar, exportar o desactivar.

Los componentes propios serán: indicador de cobertura, tarjeta de plan del día, selector de sesión, fila de regla con precedencia, panel de consentimiento y pantalla de recuperación. No habrá una tienda, puntos, monedas, ofertas, perfil promocional ni mascotización derivada de otra aplicación.

## Voz del producto

La voz debe ser directa, humana y comprobable. Ejemplos de copy propio:

- “Tu día, con menos interrupciones.”
- “Elige qué quieres proteger durante esta sesión.”
- “La protección está activa para las conexiones que este dispositivo enruta por el modo seleccionado.”
- “Puedes detenerla cuando lo necesites.”

Antes de publicar una pantalla, se revisará que su texto no proceda de capturas, APKs, repositorios ajenos ni maqueta de referencia.

## Accesibilidad y comportamiento

- Objetivos táctiles de al menos 48 dp cuando aplique y orden de foco predecible.
- Etiquetas semánticas para lector de pantalla, estados anunciables y acciones reversibles.
- Escalado de texto, modo oscuro, contraste y reducción de movimiento verificados en dispositivo real antes de beta.
- Las transiciones comunican cambios de estado; no deben ocultar una desactivación, permiso denegado ni error de cobertura.

## Flujo de implementación

1. Argon o el equipo propone wireframes y tokens nuevos, sin pegar código ni imágenes de la referencia.
2. Se revisa la propuesta contra estos límites y contra el alcance funcional documentado.
3. Se implementa de forma nativa en Android con componentes y copy propios, preservando los contratos de seguridad existentes.
4. Se comparan capturas únicamente con los golden tests de Proyect X, no con la aplicación ni maqueta de terceros.
5. Antes de beta, una revisión manual verifica identidad, accesibilidad, afirmaciones de cobertura y ausencia de assets no autorizados.

## Criterio de aceptación de originalidad

Una pantalla sólo está lista cuando su identidad, navegación, texto, iconografía y composición se pueden explicar desde los objetivos de Proyect X sin depender de una referencia externa; sus recursos tienen procedencia conocida; y su comportamiento comunica límites reales de la protección.

## Estado

Este documento fija la dirección y las barreras de diseño. No cambia todavía la interfaz nativa existente. La implementación visual se programará cuando la propuesta independiente de diseño esté lista, para no mezclar trabajo concurrente ni introducir similitudes accidentales.
