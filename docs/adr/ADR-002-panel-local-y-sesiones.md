# ADR-002: Panel local y sesiones de enfoque sin permisos nuevos

- Estado: aceptado
- Fecha: 2026-11-08

## Contexto

El MVP ya bloquea paquetes Android mediante un servicio de accesibilidad opcional, pero su interfaz era un formulario lineal. Se necesita una interfaz más clara sin afirmar capacidades que aún no existen ni ampliar la recolección de datos.

## Alternativas

### A. Introducir VPN, Usage Access y una superposición en el mismo cambio

Amplía el alcance funcional, pero eleva permisos, superficie de privacidad, compatibilidad OEM y dificultad de validación.

### B. Mejorar el panel y añadir un temporizador local independiente

Hace visible el estado y la intención de enfoque, conserva el contrato de reglas y no añade permisos ni dependencias.

## Decisión

Se elige B. La sesión persiste únicamente inicio/fin local y no altera reglas existentes. El panel consulta si la accesibilidad propia está habilitada, pero no lee contenido de otras aplicaciones.

## Consecuencias

- La experiencia es más clara y la migración es compatible con las reglas previas.
- El temporizador no se presenta como mecanismo de bloqueo adicional.
- Horarios, presupuestos y pantallas de intervención quedan como slices separados, con pruebas y consentimiento antes de incluir permisos nuevos.

## Trigger de revisión

Revisar al contar con pruebas en dispositivos físicos y un requisito confirmado para horarios o límites diarios.
