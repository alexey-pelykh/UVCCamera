plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

version = findProperty("uvccamera.version") as String? ?: "0.0.0-SNAPSHOT"

android {
    namespace = "org.uvccamera.lib"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = 35  // Android 15 for 16 KB page size compliance

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += mutableSetOf("armeabi-v7a", "arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.support.test.runner)
    androidTestImplementation(libs.support.espresso.core)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = "org.uvccamera"
            artifactId = project.name
            version = project.version.toString()

            pom {
                name = "org.uvccamera:${project.name}"
                description = "USB Video (UVC) Camera Library for Android"
                url = "https://uvccamera.org"

                licenses {
                    license {
                        name = "Apache License Version 2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        name = "saki"
                        email = "t_saki@serenegiant.com"
                    }
                    developer {
                        name = "Alexey Pelykh"
                        email = "alexey.pelykh@gmail.com"
                        organization = "The UVCCamera Project"
                        organizationUrl = "https://uvccamera.org"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/alexey-pelykh/UVCCamera.git"
                    developerConnection = "scm:git:ssh://github.com:alexey-pelykh/UVCCamera.git"
                    url = "https://github.com/alexey-pelykh/UVCCamera"
                }
            }
        }
    }

    repositories {
        maven {
            name = "StagingDeploy"
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}
