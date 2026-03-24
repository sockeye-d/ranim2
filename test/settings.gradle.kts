dependencyResolutionManagement.repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.fishies.sailfish:core:1.0.0")).using(project(":core"))
        substitute(module("dev.fishies.sailfish:gui:1.0.0")).using(project(":gui"))
    }
}

rootProject.name = "test"
