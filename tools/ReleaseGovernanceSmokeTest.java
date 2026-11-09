package com.proyectx.focus;

import java.nio.file.Files;
import java.nio.file.Path;

/** Guards the release signing, release notes, and rollback/fix-forward contract without committing keystores. */
public final class ReleaseGovernanceSmokeTest {
    public static void main(String[] args) throws Exception {
        String gradle = Files.readString(Path.of("app/build.gradle.kts"));
        String gitignore = Files.readString(Path.of(".gitignore"));
        String releaseDoc = Files.readString(Path.of("docs/RELEASE.md"));
        String osvWorkflow = Files.readString(Path.of(".github/workflows/osv-scheduled.yml"));

        require(gradle.contains("PROYECT_X_KEYSTORE_PATH"));
        require(gradle.contains("PROYECT_X_KEYSTORE_PASSWORD"));
        require(gradle.contains("PROYECT_X_KEY_ALIAS"));
        require(gradle.contains("PROYECT_X_KEY_PASSWORD"));
        require(gradle.contains("enableV1Signing = true"));
        require(gradle.contains("enableV2Signing = true"));
        require(gradle.contains("enableV3Signing = true"));
        require(gradle.contains("includeInApk = false"));
        require(gradle.contains("includeConfigs = listOf(\"releaseRuntimeClasspath\")"));
        require(osvWorkflow.contains(":app:cyclonedxDirectBom"));
        require(osvWorkflow.contains("app/build/reports/cyclonedx-direct/bom.json"));
        require(osvWorkflow.contains("fail-on-vuln: true"));
        require(gitignore.contains("*.jks") && gitignore.contains("*.keystore"));
        require(releaseDoc.contains("## Firma de release"));
        require(releaseDoc.contains("## Verificación de artefactos"));
        require(releaseDoc.contains("## Notas de versión"));
        require(releaseDoc.contains("## Plan de rollback y fix-forward"));
        System.out.println("Release governance smoke tests: PASS");
    }

    private static void require(boolean condition) {
        if (!condition) throw new AssertionError("Release governance contract failed");
    }
}
