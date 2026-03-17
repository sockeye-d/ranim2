package dev.fishies.ranim2.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AnimationMetadata(
    val jarFileOutputPath: String,
    val animations: List<String>,
)

class AnimationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = AnimationProviderProcessor(environment)
}

class AnimationProviderProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    val json = Json {
        prettyPrint = true
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val jsonFile = File(environment.options["jsonFile"] as String)
        val annotated = resolver.getSymbolsWithAnnotation("dev.fishies.ranim2.AnimationProvider")
            .filterIsInstance<KSFunctionDeclaration>()
        val animations = annotated.mapNotNull { it.qualifiedName }.map(KSName::asString)
        jsonFile.writeText(
            json.encodeToString(
                AnimationMetadata(
                    jarFileOutputPath = "",
                    animations = animations.toList(),
                )
            )
        )
        return emptyList()
    }
}
