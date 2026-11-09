import org.cyclonedx.gradle.CyclonedxDirectTask
import org.cyclonedx.model.Component

plugins {
    id("com.android.application")
}

android {
    namespace = "com.proyectx.focus"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.proyectx.focus"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    val releaseKeystorePath = providers.environmentVariable("PROYECT_X_KEYSTORE_PATH").orNull
    val releaseKeystorePassword = providers.environmentVariable("PROYECT_X_KEYSTORE_PASSWORD").orNull
    val releaseKeyAlias = providers.environmentVariable("PROYECT_X_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("PROYECT_X_KEY_PASSWORD").orNull
    val hasReleaseSigning =
        !releaseKeystorePath.isNullOrBlank() &&
            !releaseKeystorePassword.isNullOrBlank() &&
            !releaseKeyAlias.isNullOrBlank() &&
            !releaseKeyPassword.isNullOrBlank()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

tasks.named<CyclonedxDirectTask>("cyclonedxDirectBom") {
    componentName = "Proyect X"
    componentVersion = "0.1.0"
    projectType = Component.Type.APPLICATION
    // This SBOM describes the dependency boundary of the distributed release APK.
    includeConfigs = listOf("releaseRuntimeClasspath")
}
