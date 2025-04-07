plugins {
    id("com.android.library")
}

version = run {
    val pubspecContent = file("../pubspec.yaml").readText()
    val pubspecVersionMatch = Regex("version:\\s+(.*)").find(pubspecContent)
    val pubspecVersion = pubspecVersionMatch?.groupValues?.get(1)
    pubspecVersion ?: "0.0.0-SNAPSHOT"
}

android {
    namespace = "org.uvccamera.flutter"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("org.uvccamera:lib:$version") // NOTE: Use org.uvccamera:lib of the same version as the plugin.
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.0.0")
}
