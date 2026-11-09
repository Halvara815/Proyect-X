# Fases para convertir Proyect X en una app completa

Estado inicial: panel de enfoque, reglas locales y bloqueo de paquetes Android opcional. Esta hoja de ruta está ordenada por riesgo técnico y privacidad, no por facilidad visual.

## Principio rector: cobertura honesta

El objetivo es bloquear una gran parte del contenido adulto conocido y reducir los caminos de evasión, pero una app de consumo no puede prometer “sin límites” ni “imposible de evadir”. DNS cifrado, IPs directas, proxies/VPN de terceros, aplicaciones con resolutores propios, cambios de Android/OEM y la capacidad de la persona propietaria para desactivar o desinstalar la app son límites reales.

Cada pantalla deberá comunicar: **qué capa está activa, qué cubre, qué no cubre y qué debe decidir manualmente la persona usuaria**.

Los criterios marcados **N/A** son condicionales y quedan fuera de la métrica: ADR-003 prohíbe expresamente observación de navegadores en este producto.

## Fase 0 — Baseline de ingeniería y gobierno

- [x] Repositorio Gradle, build debug/release y CI básica.
- [x] Motor de reglas JVM con pruebas de normalización y precedencia.
- [x] Investigación REA estática y libro de evidencia.
- [x] CI ejecuta lint vital de Android, rechaza patrones obvios de secretos y verifica SHA-256 de dependencias/plugins mediante `gradle/verification-metadata.xml`.
- [x] Revisar licencias declaradas por la SBOM, fijar acciones CI por commit y revisar cambios de dependencias contra advisories de GitHub.
- [x] Añadir formato uniforme verificable con Spotless para Java, Gradle, Markdown y workflows.
- [x] Añadir una fuente de advisories adicional para análisis periódico fuera de pull requests: OSV-Scanner programado cada lunes y ejecutable manualmente, fijado por SHA; ejecución verde del gate de dependencias del APK observada en GitHub Actions (`42700005626`) con subida SARIF a Code Scanning verificada.
- [x] Seleccionar licencia antes de publicar: el titular publica el código y documentación propios bajo Apache-2.0; `LICENSE` excluye artefactos de terceros, APKs y secretos que el repositorio no contiene.
- [x] Política de privacidad local, soporte y proceso de divulgación de vulnerabilidades, sin canal público que exponga datos sensibles.
- [x] Publicar una matriz de compatibilidad que distingue API/AVD verificado de fabricantes y navegadores aún no probados.

**Salida:** una base reproducible; ningún APK de terceros, clave ni dato personal entra al repositorio.

## Fase 1 — Producto de enfoque y bloqueo de apps

- [x] Dashboard local, sesión privada y reglas de paquete.
- [x] Servicio de accesibilidad que lee solo el paquete en primer plano.
- [x] Prueba estructural de privacidad del servicio: solo `typeWindowStateChanged`, paquete en primer plano y sin acceso a contenido/texto/jerarquía de ventanas.
- [x] Horarios y días de la semana: modelo puro, UI local, cruces de medianoche y pruebas de precedencia.
- [x] Selector local de aplicaciones instaladas, con búsqueda e icono, sin enviar inventario de apps ni pedir `QUERY_ALL_PACKAGES`.
- [x] Presupuestos diarios como lógica pura probada antes de pedir Usage Access.
- [x] Divulgación previa y enlace voluntario a los Ajustes de acceso de uso; no consulta datos hasta que la persona configure un presupuesto y habilite ambas autorizaciones.
- [x] Lectura local mediante Usage Access, separación por día local y aplicación de presupuestos desde el servicio de accesibilidad; no persiste totales ni historial.
- [ ] Validación OEM de presupuestos reales, consumo y exactitud de `UsageStatsManager` antes de declararlo compatible fuera del AVD.
- [x] Pantalla propia de intervención que explica el motivo, permite volver al inicio o revisar reglas y no registra actividad.
- [x] Smoke test AVD: lanzamiento de `MainActivity` con fuente 1.30 y orientación horizontal; el proceso permanece visible y se restauran los ajustes del emulador.
- [ ] Pruebas de lector de pantalla, tema, dispositivos de gama baja y fabricantes físicos.

**Criterio de aceptación:** una regla de paquete se comporta igual en el motor, UI y servicio; la persona sabe cómo apagarla y qué permiso habilitó.

## Fase 2 — Modelo de políticas para sitios y contenido adulto

- [x] Separar `DomainPolicy` explicable del código de UI; `Allowlist` y `SchedulePolicy` se apoyan en reglas existentes. `CategoryPolicy` espera una lista con licencia verificable.
- [x] Normalización internacionalizada, coincidencia exacta/subdominio y resolución determinista de conflictos (`allow` específico vence a `block` amplio).
- [x] Incluir un paquete inicial de dominios de prueba, pequeño, versionado y con licencia verificable; no importar listas opacas ni de procedencia incierta.
- [x] Diseñar actualizaciones de listas como artefactos firmados, con versión, hash, rollback y opción de desactivarlas; el estado puro rechaza downgrade y no descarga nada por sí solo.
- [x] Crear un conjunto de pruebas de falsos positivos, subdominios, dominios recién registrados, IDN/punycode y listas permitidas.

**Criterio de aceptación:** se puede explicar por qué un dominio fue bloqueado y revertir una actualización de reglas sin perder reglas personales.

## Fase 3 — Filtro DNS local mediante VPN explícita

- [x] Parser DNS de pregunta única puro, con límite de tamaño y rechazo de paquetes malformados; no crea conexiones ni registra dominios.
- [x] Diseñar un `VpnService` propio, opcional, sin servidor remoto y con una única responsabilidad: dirigir consultas DNS por un resolvedor local de política; ADR-004 mantiene la capacidad fuera del manifiesto hasta completar su transporte.
- [x] Implementar parser/ejecutor DNS en biblioteca pura, con límite de tamaño, timeout acotado, cancelación, respuesta correlacionada y pruebas de paquetes malformados. El upstream sigue inyectado, sin socket ni red.
- [x] Para dominios bloqueados responder NXDOMAIN local y consistente; para el resto delegar al upstream inyectado sin guardar historial de navegación.
- [x] Mostrar una notificación de servicio en primer plano y una pantalla de estado: activo, detenido, error de red, política cargada, versión de lista. El AVD confirmó notificación/estado activo y detenido; la política incorporada `test-domain-policy-v1` se verifica por SHA-256 antes de iniciar.
- [x] Manejar `onRevoke`, reinicios, recuperación de fallos y un “kill switch” de la propia app: `DnsVpnService` gestiona `onRevoke`, `VpnService.SERVICE_INTERFACE`, `recoverIfNeeded` tras reinicio del proceso y `killSwitch` síncrono desde `MainActivity`, con estado visible de reintento (`REINTENTAR FILTRO DNS`), verificado en AVD API 34 y protegido por `DnsVpnPrivacyBoundarySmokeTest`.
- [ ] Validar en Wi‑Fi/datos móviles, IPv4/IPv6 y varios navegadores sin inspeccionar contenido TLS.

**Criterio de aceptación:** una batería de dominios de prueba se bloquea en navegadores elegidos, no se registran consultas, y el fallo del servicio es visible y recuperable.

## Fase 4 — Cobertura y límites de evasión

- [x] Detectar y explicar cuando una VPN de terceros ocupa el único túnel VPN de Android; no competir ni reemplazarla silenciosamente.
- [x] Advertir antes de cualquier activación sobre Private DNS, DoH/DoT, QUIC, resolutores internos y acceso por IP: el filtro DNS no puede clasificar de forma fiable tráfico TLS cifrado.
- [x] Ofrecer modo “always-on” solo mediante los ajustes de Android y explicar las consecuencias de “bloquear conexiones sin VPN”. El enlace abre únicamente `Settings.ACTION_VPN_SETTINGS` tras una divulgación; la app no cambia la opción ni declara soporte hasta completar pruebas de ciclo de vida.
- [x] Para dispositivos administrados, documentar por separado la ruta Device Owner/Profile Owner; no presentar esos privilegios como disponibles para consumo general.
- [x] Medir cobertura del motor con un laboratorio de dominios controlados y publicar porcentaje, condiciones y huecos conocidos; nunca usar la palabra “ilimitado” sin alcance medido.

**Criterio de aceptación:** el estado de protección declara sus límites antes de activar el filtro y las pruebas reproducibles detectan regresiones de bypass conocidos.

## Fase 5 — Accesibilidad y bloqueos en navegador, sólo con consentimiento

- [x] Decidir si el producto necesita observar elementos de navegadores: no los observa; ADR-003 conserva el alcance en nombre de paquete y futura DNS local opcional.
- [n/a] Si se implementa: divulgación separada en-app, consentimiento afirmativo, declaración actualizada de Play y minimización estricta de datos. ADR-003 prohíbe esta implementación.
- [n/a] Limitar por navegador/versiones soportados y procesar localmente; no persistir URL, contenido ni pulsaciones. No hay observación de navegador.
- [n/a] Implementar pruebas de privacidad de inspección de navegador. En su lugar, `AccessibilityPrivacyBoundarySmokeTest` protege la ausencia de esa capacidad.
- [x] Mantener un modo completo sin accesibilidad para quien use exclusivamente DNS. AVD API 34 confirmó `enabled_accessibility_services=null` mientras `DnsVpnService` estuvo `ACTIVE` en primer plano; la parada siguió disponible desde la propia app.

**Criterio de aceptación:** revisión de política y pruebas verifican propósito estrecho, consentimiento revocable y ausencia de transmisión.

## Fase 6 — Calidad, seguridad y operación

- [ ] Migrar preferencias a una base local con migraciones probadas solo cuando el volumen/consultas lo justifiquen.
- [x] No introducir secretos locales; reglas, presupuestos y listas de prueba permanecen en almacenamiento privado, sin backup ni cliente de red, y CI rechaza patrones obvios de credenciales.
- [ ] Si se introducen secretos, cifrarlos con una clave gestionada por Android y añadir migración/revocación probadas.
- [x] Generar SBOM CycloneDX desde el grafo resuelto, revisar licencias declaradas y verificar SHA-256 de dependencias/plugins en CI.
- [x] Fijar acciones CI por commit y revisar cambios de dependencias contra advisories de GitHub.
- [x] Añadir análisis periódico de dependencias fuera de pull requests: OSV-Scanner programado cada lunes y ejecutable manualmente, fijado por SHA; ejecución verde del gate de dependencias del APK observada en GitHub Actions (`42700005626`) con subida SARIF a Code Scanning verificada.
- [x] Barrido determinista de 12 000 paquetes DNS malformados y límites de tamaño/cancelación en biblioteca pura.
- [ ] Pruebas en dispositivo de batería, memoria, pérdida de red, rotación, reinicio del proceso y reinicio del teléfono.
- [x] Telemetría desactivada: no hay permiso de red, SDK de analítica ni cliente de telemetría. Cualquier diagnóstica futura será opcional, agregada y sin dominios/URLs.
- [x] Runbook local de incidencias y canal de seguridad que evita enviar historial de navegación.

**Criterio de aceptación:** no hay secretos en APK/logs, regresiones críticas bloquean CI y los fallos de protección son observables sin exfiltrar datos.

## Fase 7 — Beta responsable y publicación

- [ ] Beta cerrada con dispositivos variados y plan de consentimiento informado.
- [x] Revisión de publicación de AccessibilityService, Usage Access y ausencia de VPN con gate documentado antes de todo cambio de permisos.
- [x] Manual de configuración y desactivación de reglas, presupuesto, Acceso de uso y Accesibilidad.
- [ ] Pruebas de restauración de configuración en dispositivos físicos.
- [x] Matriz pública de cobertura y limitaciones actualizada por versión: `docs/COMPATIBILIDAD.md` documenta `0.1.0`/commit público y distingue evidencia AVD, límites y pruebas físicas pendientes.
- [x] Firma de release, verificación de artefactos, notas de versión y plan de rollback/fix-forward: configurado en `app/build.gradle.kts` por variables de entorno sin secretos en Git, verificado con `apksigner` (v1/v2/v3), documentado en `docs/RELEASE.md` y protegido por `ReleaseGovernanceSmokeTest`.

**Criterio de aceptación:** el producto informa límites antes de vender seguridad, supera la revisión de privacidad y cuenta con un procedimiento de reversión probado.

## Orden de implementación inmediato

1. Validar presupuesto diario y pantalla de intervención en fabricantes reales, incluido permiso revocado y cambio de día.
2. Diseñar el ciclo de vida de bundles firmados y rollback de Fase 2.
3. Prototipar Fase 3 en un AVD aislado, con dominios propios de prueba y sin publicar ni habilitar always-on.
4. Hacer revisión de privacidad/Play antes de considerar Fase 5.

## Evidencia y decisiones

- Investigación estática del artefacto recibido: [Investigación REA de BlockerX 5.0.61](INVESTIGACION_REA_BLOCKERX_5_0_61.md).
- Estado actual del proyecto: [Architecture 0](architecture/ARCHITECTURE_0.md).
- Entrega incremental existente: [Build plan](BUILD_PLAN.md).
- Validación AVD 2026-11-09: consentimiento VPN, servicio/notificación, parada, revocación (`onRevoke`), reconciliación tras reinicio del proceso y bloqueo DNS efectivo en `WebView Browser Tester` (`net::ERR_NAME_NOT_RESOLVED`) y resolvedor del sistema (`unknown host`) comprobados en AVD API 34 tras corregir `ScheduleWindow.dayIndexFromCalendarDayOfWeek`. Véase [laboratorio DNS](DNS_LAB.md) y ADR-004. Los criterios de varios navegadores comerciales, reinicio del teléfono y redes físicas continúan abiertos.
