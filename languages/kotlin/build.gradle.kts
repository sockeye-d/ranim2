import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import kotlin.text.replace

val langPackage = "dev.fishies.ranim2.languages"
val langName = "kotlin"

val bigLangName = langName.uppercaseFirstChar()
val grammarDir = projectDir.resolve("tree-sitter-$langName")

version = grammarDir.resolve("Makefile").readLines().first { it.startsWith("VERSION := ") }.removePrefix("VERSION := ")

plugins {
    kotlin("multiplatform")
    id("io.github.tree-sitter.ktreesitter-plugin")
}

val langClassName = "TreeSitter$bigLangName"
grammar {
    baseDir = grammarDir
    interopName = "grammar"
    grammarName = langName
    className = langClassName
    libraryName = "ktreesitter-$langName"
    packageName = "$langPackage.$langName"
    files = arrayOf(
        grammarDir.resolve("src/scanner.c"),
        grammarDir.resolve("src/parser.c"),
    )
}

val generateGrammarFilesTask = tasks.generateGrammarFiles.get()

kotlin {
    jvm()
    jvmToolchain(21)

    sourceSets {
        val generatedSrc = generateGrammarFilesTask.generatedSrc.get()
        configureEach {
            kotlin.srcDir(generatedSrc.dir(name).dir("kotlin"))
            compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }
        }
        jvmMain {
            resources.srcDir(generatedSrc.dir("jvmMain").dir("resources"))
            dependencies {
                implementation(libs.treesitter)
                api(projects.languages.common)
            }
        }
    }
}

val makeLangInclude = tasks.register("makeLangInclude") {
    dependsOn(generateGrammarFilesTask)

    val inputFile = grammarDir.resolve("bindings/c/tree-sitter.h.in")
    val outputFile = generateGrammarFilesTask.generatedSrc.get().file("jni/tree_sitter/tree-sitter-$langName.h")
    inputs.file(inputFile)
    outputs.file(outputFile)

    doLast {
        outputFile.asFile.parentFile.mkdirs()
        outputFile.asFile.writeText(
            inputFile.readText().replace("@UPPER_PARSERNAME@", langName.uppercase()).replace("@PARSERNAME@", langName)
        )
    }
}

val compileGrammarTask = tasks.register<Exec>("compileGrammar") {
    dependsOn(generateGrammarFilesTask)
    dependsOn(makeLangInclude)

    val outDir = generateGrammarFilesTask.generatedSrc.get().dir("jvmMain/resources/lib/linux/x64")
    outDir.asFile.mkdirs()

    val libFile = outDir.file("libktreesitter-$langName.so")
    val grammarSrcDir = grammarDir.resolve("src")
    val jniDir = generateGrammarFilesTask.generatedSrc.get().dir("jni")

    inputs.dir(grammarSrcDir)
    outputs.file(libFile)

    commandLine(
        "gcc", "-shared", "-fPIC", "-O2", "-std=c11",
        "-I$grammarSrcDir",
        "-I${jniDir.file("tree_sitter")}",
        "-I${System.getProperty("java.home")}/include",
        "-I${System.getProperty("java.home")}/include/linux",
        "-o", libFile,
        grammarDir.resolve("src/parser.c"),
        grammarDir.resolve("src/scanner.c"),
        jniDir.file("binding.c"),
    )

    logging.captureStandardOutput(LogLevel.ERROR)
}

fun StringBuilder.appendBlock(
    indent: String = "    ",
    open: String = "{",
    close: String = "}",
    block: StringBuilder.() -> Unit,
) {
    appendLine(open)
    append(buildString { block() }.prependIndent(indent))
    appendLine(close)
}

val baseInterface = "dev.fishies.ranim2.languages.common.TreeSitterLanguage"
val knownQueries = mapOf(
    "highlights" to "dev.fishies.ranim2.languages.common.TreeSitterLanguage.Highlightable",
    "tags" to "dev.fishies.ranim2.languages.common.TreeSitterLanguage.Taggable",
)

val modifyGeneratedFileTask = tasks.register("modifyGeneratedFile") {
    dependsOn(generateGrammarFilesTask)

    val queriesFolder = grammarDir.resolve("queries/")
    if (!queriesFolder.exists()) {
        return@register
    }

    val generatedSrc = generateGrammarFilesTask.generatedSrc.get()
    val folder = generatedSrc.dir("jvmMain/kotlin/${langPackage.replace(".", "/")}/$langName")
    val generatedFile = folder.file("$langClassName.kt")
    val files = queriesFolder.listFiles()

    inputs.file(generatedFile)
    outputs.file(generatedFile)

    doLast {
        val text = generatedFile.asFile.readText()
        val implString = buildString {
            for (query: File in files) {
                val rawString = "\"\"\"${query.readText().escapeString()}\"\"\""
                val queryName = query.nameWithoutExtension

                if (queryName in knownQueries) {
                    appendLine("override val $queryName = $rawString")
                } else {
                    appendLine("const val $queryName = $rawString")
                }
            }
        }
        val baseInterfaces = files.mapNotNull { knownQueries[it.nameWithoutExtension] } + baseInterface
        val newText = text.replace(
            "actual object $langClassName {",
            "actual object $langClassName : ${baseInterfaces.joinToString()} {\n$implString\n",
        ).replace("actual fun language()", "actual override fun language()")
        generatedFile.asFile.writeText(newText)
    }
}

fun String.escapeString() = replace("\${", "\${\"$\"}{").replace("\"\"\"", "$" + """{"\"\"\""}""")

tasks.named("jvmProcessResources") { dependsOn(compileGrammarTask) }
tasks.named("compileKotlinJvm") {
    dependsOn(generateGrammarFilesTask)
    dependsOn(modifyGeneratedFileTask)
}
