pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

include(":features:Drone")
include(":libraries:core")
include(":libraries:permission")
include(":libraries:database")
include(":app")
rootProject.name = "DeliveryDrone"
