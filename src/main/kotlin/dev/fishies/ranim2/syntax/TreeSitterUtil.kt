package dev.fishies.ranim2.syntax

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.fishies.ranim2.languages.common.TreeSitterLanguage
import dev.fishies.ranim2.toComposeColor
import io.github.treesitter.ktreesitter.Language
import io.github.treesitter.ktreesitter.Parser
import io.github.treesitter.ktreesitter.Query
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class ThemeColor(
    val color: String,
    @SerialName("font_style") val fontStyle: String? = null,
    @SerialName("font_weight") val fontWeight: Int? = null,
)

@Serializable(SyntaxHighlighterTheme.Serializer::class)
data class SyntaxHighlighterTheme(
    @SerialName("syntax") val syntax: Map<String, ThemeColor>,
) {
    operator fun get(name: String): SpanStyle {
        var name = name
        for (i in name.split('.')) {
            syntax[name]?.let {
                val color = it.color.toComposeColor()
                val fontStyle = when (it.fontStyle) {
                    "normal" -> FontStyle.Normal
                    "italic", "oblique" -> FontStyle.Italic
                    else -> null
                }
                val fontWeight = it.fontWeight?.let(::FontWeight)
                return SpanStyle(color = color, fontStyle = fontStyle, fontWeight = fontWeight)
            }
            name = name.substringBeforeLast('.')
        }
        System.err.println("Style $name not found")
        return SpanStyle(color = Color.Blue)
    }

    internal object Serializer : KSerializer<SyntaxHighlighterTheme> {
        val delegate = MapSerializer(String.serializer(), ThemeColor.serializer())
        override val descriptor: SerialDescriptor = delegate.descriptor

        override fun serialize(
            encoder: Encoder,
            value: SyntaxHighlighterTheme,
        ) = delegate.serialize(encoder, value.syntax)

        override fun deserialize(decoder: Decoder) = SyntaxHighlighterTheme(delegate.deserialize(decoder))
    }
}

private val languageCache = ConcurrentHashMap<String, Language>()
private val parserCache = ConcurrentHashMap<String, Parser>()
private val hlQueryCache = ConcurrentHashMap<String, Query>()

private val Any.qualifiedName
    get() = this::class.qualifiedName ?: javaClass.toString()

fun TreeSitterLanguage.makeLanguage(): Language = languageCache.getOrPut(qualifiedName) { Language(language()) }

fun TreeSitterLanguage.makeParser(): Parser = parserCache.getOrPut(qualifiedName) { Parser(makeLanguage()) }

fun TreeSitterLanguage.Highlightable.highlight(text: String) =
    hlQueryCache.getOrPut(qualifiedName) { Query(makeLanguage(), highlights) }
        .matches(makeParser().parse(text).rootNode)
