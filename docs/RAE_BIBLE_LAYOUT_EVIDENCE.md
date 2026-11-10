# Evidencia RAE: módulo Refugio

Actualizado: 2026-11-09.

## Artefacto inspeccionado

- ZIP aportado por el titular: `proyect-x-—-blovix-focus-&-blocker.zip`.
- SHA-256: `dc8de375f8564a30852d9e75fee0e76752817e070817f7bb66dbecac092ec924`.
- Inventario RAE: 52 archivos, 54 nodos y 63 ocurrencias. Evidencia de resumen: `ev_3a6798a81e67ad015cfbaf69259d51584a39f8997c4bf566bb45bc5de80d5f88`.
- Análisis TypeScript estático, sin ejecutar el ZIP: `ev_dc8878210dba0ddc8a585560e66c5376ccb128b6224f8d0aca479bd8158cef6b`.

## Archivos relevantes observados

| Archivo del ZIP | Observación estática | Adaptación propia en Proyect X |
| --- | --- | --- |
| `src/App.tsx` | Estado booleano opcional para activar contenido de fe. | `FaithModeStore` con `SharedPreferences`, desactivado por defecto. |
| `src/data/proyectXData.ts` | Modelo de mensajes y corpus contextual de Reina-Valera 1960. | Parser local `BibleRepository` y RV1909 de dominio público; no se usa RVR1960. |
| `src/components/ForoYMenuScreen.tsx` | Tarjeta de configuración con switch y divulgación. | Ajustes locales en el Centro local, con switch nativo y divulgación de alcance. |
| `src/components/BloqueoScreen.tsx` | Jerarquía de tarjeta, etiqueta, selector por contexto y bloque de lectura. | Pantalla `Refugio` con tarjeta fija de enfriador, dos pestañas y accesos de emergencia. |

## Límites y decisiones

- La evidencia del ZIP es estática; no demuestra ejecución, efectos de sus temporizadores ni seguridad de su código.
- Se conserva la jerarquía útil de tarjeta, switch y contenidos por contexto autorizada por el titular, pero se mantiene la paleta ya existente de Proyect X: el lector reutiliza los tonos oscuros de `InterventionActivity`.
- No se importaron imágenes, marcas, código TypeScript, corpus RVR1960 ni dependencias del ZIP.
- La extensión “enfriador”, las dos pestañas, la navegación de libro/capítulo, los accesos de emergencia y la racha de lectura son implementación propia y local.
