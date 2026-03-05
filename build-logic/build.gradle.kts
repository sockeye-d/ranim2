group = "build-logic"

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("io.github.tree-sitter:ktreesitter-plugin:0.24.1")
}

gradlePlugin {
    plugins {
        create("tsLanguage") {
            id = "ts-language"
            implementationClass = "TsLanguagePlugin"
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}
