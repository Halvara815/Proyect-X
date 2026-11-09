# Build plan

## Slice 1 — Panel local de enfoque

- Estado: implementado.
- Alcance: dashboard visual propio, estado del servicio de accesibilidad, selector de aplicaciones de inicio visible a Android, edición de reglas existente y temporizador de sesión local.
- Protege: claridad de la decisión y privacidad por defecto; no añade permisos.
- Evidencia: `:app:assembleDebug`, `:app:assembleRelease`, `RuleEngineSmokeTest`, `FocusSessionSmokeTest` e `InstalledAppSmokeTest`.
- Fix-forward: la sesión se puede terminar localmente; las reglas siguen con el formato compatible previo.

## Slice 2 — Reglas temporales verificables

- Estado: implementado para reglas de aplicaciones, dominios y palabras clave.
- Alcance: modelo explícito de ventana semanal, selección de días/horas en la UI, evaluación pura y aplicación por el servicio de accesibilidad usando hora local.
- Seguridad de migración: las reglas anteriores no contienen horario y se interpretan como “Siempre”, por lo que no pierden protección al actualizar.
- Evidencia: `ScheduleWindowSmokeTest` cubre bordes, cruce de medianoche, precedencia de permitir y formato heredado; build debug/release verificado.
- Fuera de alcance: presupuestos diarios, Usage Access y ejecución persistente adicional; requerirán consentimiento y validación OEM propia.

## Slice 3 — Límites diarios con consentimiento

- Alcance propuesto: presupuestos por paquete mediante Usage Access, solo después de mostrar propósito, datos leídos y alternativa sin permiso.
- Riesgo: datos de uso sensibles y discrepancias OEM.
- Aceptación: ninguna lectura ocurre sin concesión explícita; la contabilidad se prueba contra fixtures y se puede desactivar.

## Slice 4 — Experiencia de intervención

- Estado: implementado para la ruta de accesibilidad.
- Alcance: pantalla local de pausa intencional, retorno al inicio y acceso a las reglas; solo recibe el paquete ya observado por el servicio y no lee contenido ni URLs.
- Restricción: no prometer bloqueo irreversible; todavía requiere prueba manual con el servicio de Accesibilidad habilitado y dispositivos OEM.

## Slice 5 — Política de dominio explicable

- Estado: implementado como biblioteca pura, aún sin transporte de red.
- Alcance: evaluación de dominios normalizados, coincidencia exacta/subdominio, horarios y precedencia de allowlist, conservando la regla que produjo la decisión.
- Evidencia: `DomainPolicySmokeTest` compara la política con el motor existente, incluidos IDN y cruces de medianoche.
- Fuera de alcance: listas de categorías y filtrado DNS; una lista de dominios no entrará hasta verificar su licencia, procedencia y ciclo de actualización.

### Base de verificación de bundles

- `PolicyBundleVerifier` valida SHA-256 y firma `SHA256withECDSA` antes de que un futuro actualizador pueda usar un bundle.
- `PolicyBundleVerifierSmokeTest` cubre bundle íntegro, contenido alterado y hash malformado.
- `PolicyBundleState` acepta solo una versión mayor que haya pasado la verificación, conserva un único rollback y al restaurarlo desactiva actualizaciones hasta nueva acción local. `PolicyBundleStateSmokeTest` cubre avance, downgrade rechazado, rollback y reactivación.
- No hay endpoint, clave de producción, almacenamiento de bundles, activación de filtrado ni descarga automática todavía.

## Slice 6 — Presupuesto diario local con acceso de uso voluntario

- Estado: implementado de forma local, pendiente de validación OEM.
- Alcance: la persona elige una app y un límite; tras habilitar manualmente Acceso de uso y Accesibilidad, el servicio consulta el agregado local de hoy y abre la intervención si el presupuesto se agotó. Una regla explícita `Permitir` mantiene prioridad. Los totales no se guardan ni se transmiten.
- Evidencia: `DailyBudgetSmokeTest` cubre borde exacto, excedente y entradas inválidas; `LocalDayWindowSmokeTest` verifica el límite de día local; `:app:assembleDebug` y `:app:assembleRelease` compilan la integración Android.
- Fuera de alcance: no hay historial, telemetría, bloqueo sin Accesibilidad, ni afirmación de exactitud idéntica entre OEM. Una revocación del permiso falla abierta y debe verificarse en dispositivos físicos.

## Slice 7 — Parser DNS defensivo

- Estado: la biblioteca pura está integrada en una beta de VPN DNS opcional; el transporte UDP y la red siguen pendientes de validación en AVD aislado.
- Alcance: acepta una sola consulta DNS estándar de tamaño acotado y extrae un nombre ASCII normalizado; rechaza respuestas, compresión no admitida, preguntas múltiples, clases no-IN y truncamientos.
- Incluye: una política DNS local enlaza la pregunta con `DomainPolicy` y devuelve NXDOMAIN mínimo solo al bloquear; permitir/sin coincidencia requiere un resolver aún no implementado.
- Incluye: `DnsResolutionExecutor` delega únicamente consultas permitidas/no coincidentes a un upstream inyectado, con límite de 5 segundos, cancelación y validación básica de respuesta/ID. No crea sockets ni retiene paquetes.
- Evidencia: `DnsQuestionParserSmokeTest`, `DnsResponseBuilderSmokeTest`, `LocalDnsPolicySmokeTest` y `DnsResolutionExecutorSmokeTest` cubren consulta ordinaria, paquetes malformados, respuesta local, precedencia allow/block, cancelación, timeout y respuesta upstream inválida.
- Laboratorio: `DnsCoverageLabSmokeTest` comprueba 5/5 casos `.invalid`; su porcentaje es del motor puro y no representa cobertura de navegación.
- Robustez: `DnsParserRobustnessSmokeTest` recorre de forma determinista 12 000 paquetes aleatorios de hasta 4 200 bytes; solo acepta preguntas que siguen el contrato estricto o rechaza con `IllegalArgumentException`.
- Fuera de alcance de esta beta: registro de consultas, inspección TLS/contenido, DNS TCP/DoH/DoT, un servidor VPN remoto y soporte always-on. El transporte DNS UDP requiere consentimiento, ciclo de vida y pruebas de red separadas antes de anunciar cobertura.
