plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.cyclonedx.bom") version "3.5.1"
    id("com.diffplug.spotless") version "8.10.4"
}

spotless {
    format("misc") {
        target("*.gradle.kts", "*.md", ".gitignore", ".github/**/*.yml")
        trimTrailingWhitespace()
        endWithNewline()
    }
    java {
        target("app/src/**/*.java", "tools/**/*.java")
        palantirJavaFormat()
        removeUnusedImports()
        endWithNewline()
    }
}
