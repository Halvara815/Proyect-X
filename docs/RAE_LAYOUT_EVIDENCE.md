# RAE — mapa de capacidades y layout propio

Fecha de revisión: 2026-11-09
Estado: evidencia estática y capturas aportadas por el titular; no es una especificación de copia.

## Alcance

Se revisaron dos fuentes independientes:

1. El APK externo `BlockerX-5.0.61.apk`, SHA-256 `fdd8b3020d9dbd1a9983ee32b1a3b29c4b34aa88b1c00a289321f679c21faa5a`, mediante REA/JADX en modo estático. El APK no se ejecutó, sus firmas no se verificaron y ninguna conclusión establece comportamiento real.
2. Diecinueve capturas de una sesión de la aplicación externa, aportadas localmente el 2026-11-09 en `C:\Users\veget\Desktop\Proyect X Diseno`. Son evidencia visual de pantallas observadas, no assets reutilizables ni autorización para reproducirlas.

No se copió ninguna imagen, texto, recurso, lista, icono, marca, nombre de pantalla ni código al repositorio.

## Hallazgos y decisión de producto

| Evidencia observada | Certeza | Decisión independiente para Proyect X |
| --- | --- | --- |
| El `onCreate` de la actividad principal instala un contenedor de fragmento; su modelo de navegación crea una lista de destinos y mantiene una selección. REA: `ev_dbe0bee9cbdf9f79ea94973bc818bf64b193be770bfaa2004e828718d3859cbe`, `ev_49dc75f196a48c7e4264d53f53a3620765fd9319ffbbb12c20a56c7003f43d69`. | Observado, estático | Mantener destinos visibles de tarea única en Proyect X: Inicio, Barreras, Reglas y Reinicio. Su orden, rótulos, estado visual y composición son propios. |
| El APK tiene un módulo de lista de reglas con modelos y diálogos para sitio, aplicación y palabra clave. REA: `ev_34c1588f06ad08dbfa1879b016dc4ed6557720d04abee9dce752cda66f90d3c9`, `ev_dd6faee971d4a4980b253799db612d9e412fc89815063047351e84c5c4651ff6`. | Observado, estático | Conservar y mejorar la pantalla Reglas local: tipos explícitos, prioridad visible y resultado comprobable. No trasladar pestañas, filtros, copy ni estilo de la referencia. |
| Las capturas muestran control de barreras, una lista de elementos bloqueados, recuperación, ayuda y un consentimiento de accesibilidad. | Observado, visual aportado | Tratar cada necesidad como un flujo propio: Barreras explica permisos y cobertura; Reglas gestiona configuración local; Reinicio conserva protecciones; ayuda se añadirá como documentación local y comprobable. |
| Las capturas también incluyen cuenta, comunidad, monedas, promociones, plan premium, cuestionarios y afirmaciones de recuperación. | Observado, visual aportado | Excluido: no habrá economía, comunidad, publicidad, perfil remoto, diagnóstico, puntuación de adicción ni promesas clínicas/comerciales. |
| El paquete inspeccionado tiene 24.804 clases y 2.789 recursos; su firma no fue verificada. REA: `ev_53bf35e1c0b2878ed886413153f3a57b26fb8645be6883feb0006bb68e71bb28`. | Derivado, estático | La investigación queda limitada a preguntas concretas; no se infieren funciones a partir de nombres, recursos o marketing. |

## Layout propio: «mapa de atención»

La interfaz de Proyect X no reproducirá el tablero azul, la barra de cinco destinos, fondos fotográficos, iconos ni tarjetas de las capturas. Su layout original usa el sistema **calma práctica** de `DISENO_ORIGINAL.md`:

```text
Inicio
  saludo y contexto local
  ├─ franja de estado: reglas activas + cobertura comprobable
  ├─ tarjeta «sesión de enfoque»: duración, iniciar o cambiar plan
  └─ acceso textual a Barreras y Reglas

Barreras
  ├─ capacidades del dispositivo: accesibilidad, uso, DNS opcional
  ├─ consentimiento y límites de cada capacidad
  └─ privacidad local, junto a la configuración relevante

Reglas
  ├─ creación de regla
  ├─ resumen por tipo y precedencia
  └─ lista local editable, sin historial de navegación

Reinicio
  ├─ pausa deliberada y no punitiva
  ├─ confirmación de lo que se conserva: reglas, permisos y DNS
  └─ regreso a Inicio con el nuevo siguiente paso
```

Cada pantalla tendrá un encabezado propio, superficies de bordes suaves, color lino/bosque/lima/terracota y tipografía de lectura rápida. El significado de estado se expresará con texto y semántica, nunca sólo por color.

## Backlog derivado, no copiado

1. **Layout de Inicio:** sustituir espacios vacíos por un resumen de estado que lea valores reales de reglas, sesión y cobertura; no inventar métricas de bloqueo o rachas.
2. **Reglas como inventario:** presentar conteos reales por dominio, paquete y palabra, con edición local y prioridad verificable.
3. **Barreras explicables:** mantener el consentimiento por capacidad y expresar las exclusiones de DNS/Accesibilidad antes de activarlas.
4. **Ayuda local:** añadir preguntas frecuentes propias sobre cobertura, permisos y cómo desactivar la protección; sin importar el copy de la referencia.
5. **QA visual:** probar en AVD y dispositivo físico tamaños de texto, lector de pantalla, contraste, modo oscuro y navegación táctil antes de beta.

## Límites de la evidencia

- REA observó artefacto estático; no ejecutó ni verificó el APK externo.
- Las capturas reflejan solamente el estado que fue fotografiado; no prueban todas las rutas ni su lógica.
- La efectividad del filtro DNS, accesibilidad y presupuestos de Proyect X sigue dependiendo de pruebas propias documentadas en `README.md`, `DNS_LAB.md` y `COMPATIBILIDAD.md`.
- Ningún layout se acepta por parecido con una referencia: se acepta únicamente si satisface una tarea de Proyect X, es accesible y comunica cobertura real.
