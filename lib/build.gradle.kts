plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
}

version = findProperty("uvccamera.version") as String? ?: "0.0.0-SNAPSHOT"

android {
    namespace = "org.uvccamera.lib"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPOSITORY"))
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        maven {
            name = "OSSRH"
            url = uri(
                if (project.version.toString().endsWith("-SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_TOKEN")
            }
        }
    }
}

val gpgPassphrase: String? = System.getenv("GPG_PASSPHRASE")
val gpgPrivateKey: String? = System.getenv("GPG_PRIVATE_KEY")
if (gpgPassphrase != null && gpgPrivateKey != null) {
    signing {
        useInMemoryPgpKeys(gpgPrivateKey, gpgPassphrase)
        sign(publishing.publications["release"])
    }
}
