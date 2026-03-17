dependencyResolutionManagement.repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

includeBuild("..") {
    dependencySubstitution {
        substitute(module("dev.fishies.ranim2:core:1.0.0")).using(project(":core"))
        substitute(module("dev.fishies.ranim2:gui:1.0.0")).using(project(":gui"))
    }
}

rootProject.name = "test"
