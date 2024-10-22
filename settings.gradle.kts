pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UVCCamera"
include(":libuvccamera")
include(":usbCameraCommon")
include(":usbCameraTest")
include(":usbCameraTest0")
include(":usbCameraTest2")
include(":usbCameraTest3")
include(":usbCameraTest4")
include(":usbCameraTest5")
include(":usbCameraTest6")
include(":usbCameraTest7")
include(":usbCameraTest8")
