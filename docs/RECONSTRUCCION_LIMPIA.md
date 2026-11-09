# Proyect X — especificación de reconstrucción limpia

## Objetivo

Crear **Proyect X**, una aplicación Android propia de bienestar digital que permita a una persona configurar reglas locales para reducir distracciones. Esta especificación deriva sólo de funcionalidades expuestas públicamente y deberá actualizarse cuando haya evidencia estática o de pruebas controladas.

## Primera entrega propuesta

| Slice | Incluye | Criterio de aceptación |
| --- | --- | --- |
| Reglas locales | Crear, activar, desactivar y borrar reglas de dominio y palabra clave | Las reglas sobreviven a reiniciar la app y se validan entradas vacías o malformadas |
| Listas | Lista de permitidos con precedencia explícita | Una coincidencia permitida no queda bloqueada por una regla genérica |
| Intervención visible | Pantalla propia de bloqueo con motivo de la regla | No revela la URL ni palabras sensibles innecesariamente |
| Configuración | Activar/desactivar funciones de forma explícita | Ningún servicio sensible se inicia sin consentimiento informado |
| Auditoría local | Contadores agregados, no historial de navegación | Puede borrarse desde la app y no contiene URLs completas por defecto |

## Fuera de alcance inicial

- Copiar pantallas, textos, recursos, endpoints, modelo comercial o lógica interna del producto de referencia.
- Sincronización de cuentas, pagos, anuncios y telemetría remota.
- Bloqueo “imposible de desinstalar”: requiere un modelo de recuperación y una revisión de seguridad/uso indebido.
- Inspección de contenido HTTPS o captura de credenciales.

## Arquitectura propuesta

Un monolito Android modular, offline-first:

```text
UI ──> casos de uso ──> repositorio local ──> almacenamiento cifrado cuando corresponda
                 └──> adaptadores con consentimiento: VPN / accesibilidad / superposición
```

- `domain`: reglas, normalización de dominios, precedencia y resultados de coincidencia.
- `data`: almacenamiento local y migraciones reversibles.
- `platform`: adaptadores Android aislados tras interfaces; cada permiso es opt-in.
- `ui`: configuración, estado de protección y pantalla de bloqueo.

## Seguridad y privacidad por diseño

- Mínimo privilegio: cada permiso se solicita justo antes de usar la capacidad que lo requiere.
- Las reglas y datos de auditoría son locales por defecto.
- Sin logs de URLs completas, términos bloqueados ni datos personales en producción.
- La desactivación, exportación y eliminación de datos deben ser visibles y funcionales.
- Las funciones de control de terceros requieren consentimiento, mecanismo de recuperación y revisión específica de abuso.

## Evidencia que falta antes de declarar paridad funcional

No se puede afirmar paridad con BlockerX Lite sin APK identificado y escenarios controlados. El producto inicial será una implementación propia de las capacidades públicas anteriores, no un clon binario ni visual.
