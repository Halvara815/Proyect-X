# Remodelado visual basado en el ZIP autorizado

## Fuente y alcance

- Artefacto inspeccionado estáticamente: `docs/architecture/proyect-x-—-blovix-focus-&-blocker.zip`
- SHA-256: `dc8de375f8564a30852d9e75fee0e76752817e070817f7bb66dbecac092ec924`
- Inventario REA: `ev_3a6798a81e67ad015cfbaf69259d51584a39f8997c4bf566bb45bc5de80d5f88`
- Extracción segura REA: `ev_14918308aa5e3e8314bbaa4bf54ad11a97788e7fe624e9f5fd3c00c81448f219`

La evidencia anterior es estática: enumera y permite estudiar los archivos del ZIP, pero no
demuestra ejecución de la aplicación de referencia.

## Fuentes de layout estudiadas

| Archivo del ZIP | Aporte visual reutilizado | Fuera de alcance |
| --- | --- | --- |
| `src/App.tsx` | Cascarón con cabecera persistente y destinos inferiores | Estados, datos y textos de ejemplo |
| `src/components/BloqueoScreen.tsx` | Tarjetas compactas, superficies y CTA jerarquizados | Lógica y contenido de la referencia |
| `src/components/ListaBloqueadaScreen.tsx` | Agrupación de controles y estado visible de una regla | Reglas de muestra |
| `src/components/ForoYMenuScreen.tsx` | Proporción y jerarquía del panel lateral local | Cualquier texto, enlaces o contenido remoto |
| `src/components/DiagnosticoScreen.tsx` | Flujo inicial omitible, progreso y resultado de calibración | Preguntas, opciones, puntuación y datos de ejemplo |
| `src/index.css` | Contraste entre cabecera oscura, fondo frío y superficies claras | CSS, tipografías y tokens literales |

## Implementación propia y fidelidad visual

`MainActivity` implementa la jerarquía visual de las cinco vistas de referencia manteniendo el 100 %
de la lógica y almacenes locales de Proyect X (`RuleStore`, `BudgetStore`, `FocusSessionStore`,
`HabitStreakStore`, `FaithModeStore`, `CalibrationStore`, `RecoveryStore`, `DnsVpnService`):

1. **Cabecera superior blanca persistente:** Botón de menú con icono vectorial limpio en azul sobre
   superficie `#EEF4FF`, marca `ProyectX` (`ENFOQUE LOCAL · SIN CUENTA`) y píldora de estado
   `BLINDADO` (`#081026`) / `FLEXIBLE` (`#DCE8FF`).
2. **Inicio (superior e inferior):** Tarjeta de estado de Accesibilidad con botón
   `ACCESIBILIDAD ACTIVA · REVISAR` / `ACCESIBILIDAD · REVISAR`, cuadrícula de dos columnas
   (`REGLAS ACTIVAS` y `COBERTURA` en azul `#2563EB`), `Tu mapa de reglas` con tres mosaicos
   (`DOMINIOS`, `APPS`, `PALABRAS`) y tarjeta azul suave (`#DCE8FF`) de `Sesión de enfoque` /
   `En enfoque ahora` con píldoras de intención (`Estudio`, `Trabajo`, `Código`, `Lectura`),
   selector de duración (`− 25m +`, `25 min`, `50 min`, `90 min`) y botón principal en `#081026`.
3. **Barreras:** Tarjeta `PERFIL DE PROTECCIÓN ACTIVO` con interruptor maestro y selector
   segmentado (`Modo Flexible` | `Modo Blindado` con icono de candado vectorial), tarjeta
   `Capas de intercepción local` (`SIN NUBE`) con iconos vectoriales limpios (`Filtro DNS` conectado
   al ciclo de vida real de `DnsVpnService`, `Cobertura en todos los navegadores` y `Cortafuegos de
   redes y videos cortos` con selector `Solo Reels y Shorts` | `Apps sociales completas`), además de
   los controles locales de presupuesto diario y diagnóstico DNS (`REINTENTAR FILTRO DNS`).
4. **Reinicio:** Selector superior de tres segmentos (`Constancia` | `Bitácora` | `Guías y Foro`),
   tarjeta `TIEMPO CONTINUO SIN INTERRUPCIONES` con tres contadores digitales blancos (`DÍAS`,
   `HORAS` en azul `#2563EB`, `MINUTOS`), mejor marca local y botones `Reiniciar contador` y
   `Abrir Bitácora`, tarjeta `MOTIVACIÓN NORMAL PARA TU ESTADO` / `VERSÍCULO PARA TU ESTADO`,
   `Balance de sesiones y barreras` y `Hitos de estabilidad`.
5. **Panel lateral (`Centro local`):** Cabecera oscura `#081026` de borde a borde con insignia `PX`,
   tarjeta interna `MENSAJES DE FIRMEZA` (`Configurar modo` y `Accesibilidad`), cinco accesos con
   iconografía vectorial limpia y divisores de `1dp`, y pie `ProyectX · En desarrollo`.

La validación posterior se realizó con `spotlessCheck`, `:app:lintDebug`, `:app:assembleDebug`,
`:app:assembleRelease`, las 23 pruebas smoke JVM y capturas en vivo sobre `emulator-5554`.

## Calibración local propia

Se añadió `CalibrationActivity`, presentada en el primer arranque hasta que el usuario la termina
u omite. Su perfil usa tres preferencias de enfoque no clínicas (intención, momento preferido y
modo de inicio), persiste únicamente mediante `SharedPreferences` y no modifica reglas, permisos,
DNS, VPN ni aplicaciones. El panel local permite repetirla y Inicio muestra el perfil guardado.
