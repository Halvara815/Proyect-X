# ADR-003: No observar texto ni URL de navegadores

- Estado: aceptado
- Fecha: 2026-11-08

## Contexto

Bloquear por contenido dentro de un navegador requeriría observar texto, URL u otros nodos de accesibilidad. Esos datos son especialmente sensibles y su acceso no es necesario para las capacidades locales ya entregadas.

## Decisión

Proyect X no solicitará contenido de ventanas ni procesará URL, texto, pulsaciones o jerarquías de navegadores. El servicio de accesibilidad se limita al nombre de paquete en primer plano. Si en el futuro se introduce cobertura web, deberá evaluarse separadamente como filtro DNS local opcional y documentar sus límites; no será una excusa para ampliar la captura de accesibilidad.

## Consecuencias

- No existe bloqueo de páginas ni de texto dentro de navegadores en esta versión.
- La privacidad es más fácil de explicar y verificar: no hay historial de navegación que almacenar o transmitir.
- Una propuesta de inspección de navegador requerirá una ADR nueva, consentimiento afirmativo y revisión de política antes de escribirse código.
