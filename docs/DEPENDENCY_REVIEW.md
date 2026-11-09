# Revisión de dependencias y licencias

Fecha: 2026-11-09. Fuente: `cyclonedxBom` sobre el grafo Gradle resuelto.

## Resultado reproducible

- Formato: CycloneDX 1.6.
- Componentes resueltos: 95.
- Componentes con licencia declarada por la SBOM: 94.
- Componente sin licencia declarada: la aplicación raíz `Proyect X`.

La ausencia de licencia del componente raíz es esperada mientras la persona titular no elija una licencia de repositorio. No autoriza publicar ni redistribuir el proyecto con una licencia implícita.

## Proceso

1. CI ejecuta `cyclonedxBom` y produce `build/reports/cyclonedx/bom.json` y XML.
2. La revisión de una actualización debe comparar la nueva SBOM, licencias y componentes transitivos contra la versión anterior.
3. `gradle/verification-metadata.xml` fija SHA-256 de artefactos/pugins actualmente resueltos; regenerarlo requiere revisión humana del diff.
4. Esta revisión no reemplaza la aprobación legal de licencia ni el pinning por SHA de acciones GitHub: esos gates siguen pendientes.

## Automatización en CI

Las acciones del workflow están fijadas a commits de las etiquetas revisadas y `dependency-review-action` se ejecuta en pull requests para contrastar cambios de dependencias con los advisories de GitHub. `osv-scheduled.yml` ejecuta además cada lunes a las 12:30 UTC el workflow reutilizable de OSV-Scanner fijado al commit de `v2.7.0`, con `fail-on-vuln: true` y resultados SARIF en la pestaña de seguridad de GitHub. El flujo se puede disparar manualmente para comprobar la integración después de publicar el repositorio.

El escáner no se puede ejecutar desde esta copia local: necesita un repositorio GitHub con Code Scanning habilitado para recibir el SARIF. Hasta que se publique y se vea al menos una ejecución, la evidencia es de configuración y no de operación remota.

`spotlessCheck` también se ejecuta en CI; aplica el mismo formato a Java, Gradle, Markdown y workflows. Los cambios mecánicos se generan con `spotlessApply` y se validan antes de compilar.
