# Revisión de dependencias y licencias

Fecha: 2026-11-09. Fuente: `cyclonedxBom` sobre el grafo Gradle resuelto.

## Resultado reproducible

- Formato: CycloneDX 1.6.
- Componentes resueltos: 95.
- Componentes con licencia declarada por la SBOM: 94.
- Componente sin licencia declarada: la aplicación raíz `Proyect X`.

El código y la documentación propios del repositorio están publicados bajo `Apache-2.0` (`LICENSE`).

## Proceso

1. CI ejecuta `cyclonedxBom` y produce `build/reports/cyclonedx/bom.json` y XML.
2. La revisión de una actualización debe comparar la nueva SBOM, licencias y componentes transitivos contra la versión anterior.
3. `gradle/verification-metadata.xml` fija SHA-256 de artefactos/plugins actualmente resueltos (incluidos los binarios `aapt2` para Windows y Linux); regenerarlo requiere revisión humana del diff.

## Automatización en CI

Las acciones del workflow están fijadas a commits de las etiquetas revisadas y `dependency-review-action` se ejecuta en pull requests para contrastar cambios de dependencias con los advisories de GitHub. `osv-scheduled.yml` genera primero una SBOM CycloneDX del grafo Gradle resuelto y entrega sólo ese artefacto al workflow reutilizable de OSV-Scanner fijado al commit de `v2.7.0`, cada lunes a las 12:30 UTC y manualmente. Mantiene `fail-on-vuln: true` y publica resultados SARIF en la pestaña de seguridad de GitHub.

### Primera ejecución remota observada (2026-11-09)

- Ejecución manual verificada: run `42647220155` (`osv-scheduled.yml`).
- La ejecución inicial inspeccionó `gradle/verification-metadata.xml` (332 paquetes del classpath de build/plugins) y subió correctamente el artefacto SARIF (`13687727663`) a GitHub Code Scanning.
- Con `fail-on-vuln: true`, ese job falló de forma cerrada al detectar 15 advisories sobre 12 paquetes transitivos de herramientas de build (`jackson-core`, `jackson-databind`, `guava`, `commons-lang3`, `httpclient`, `jose4j`, `bcpkix-jdk18on`, `bcprov-jdk18on`, `jdom2` y `kotlin-gradle-plugin`). Ninguno de esos paquetes forma parte del APK distribuido (`app` no declara dependencias externas de runtime), pero el resultado tampoco describe de forma precisa el grafo actual.
- El gate posterior escanea la SBOM resuelta `bom.json`, no ese historial de integridad. Una vulnerabilidad presente en las dependencias resueltas sigue fallando el workflow; `verification-metadata.xml` continúa siendo el control independiente de integridad SHA-256.

`spotlessCheck` también se ejecuta en CI; aplica el mismo formato a Java, Gradle, Markdown y workflows. Los cambios mecánicos se generan con `spotlessApply` y se validan antes de compilar.
