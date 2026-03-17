group = "plugin"

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

val ktVersion = libs.versions.kotlin.get()

dependencies {
    implementation("org.jetbrains.compose:compose-gradle-plugin:${libs.versions.compose.asProvider().get()}")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${ktVersion}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${ktVersion}")
    compileOnly("com.google.devtools.ksp:symbol-processing-api:${ktVersion}")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:${ktVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serialization.json.get()}")
}

gradlePlugin {
    plugins {
        create("ranimAnimation") {
            id = "dev.fishies.ranim2.plugin"
            implementationClass = "dev.fishies.ranim2.plugin.AnimationProviderPlugin"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    }
}
