// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    base
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jreleaser)
}

version = findProperty("uvccamera.version") as String? ?: "0.0.0-SNAPSHOT"

jreleaser {
    configFile = file("jreleaser.yml")
}
