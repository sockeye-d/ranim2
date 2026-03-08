@file:UseSerializers(Theme.ColorSerializer::class)

package dev.fishies.ranim2.theming

import androidx.compose.material.Colors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import dev.fishies.ranim2.core.Element
import dev.fishies.ranim2.core.attached
import dev.fishies.ranim2.core.fromHtmlColor
import dev.fishies.ranim2.core.toHtmlColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class Background {
    PRIMARY,
    PRIMARY_VARIANT,
    SECONDARY,
    SECONDARY_VARIANT,
    BACKGROUND,
    SURFACE,
    ERROR,
}

@Serializable
data class Theme(
    val primary: Color,
    @SerialName("primary_variant") val primaryVariant: Color,
    val secondary: Color,
    @SerialName("secondary_variant") val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    @SerialName("on_primary") val onPrimary: Color,
    @SerialName("on_secondary") val onSecondary: Color,
    @SerialName("on_background") val onBackground: Color,
    @SerialName("on_surface") val onSurface: Color,
    @SerialName("on_error") val onError: Color,
    @SerialName("is_light") val isLight: Boolean,
    val syntax: SyntaxHighlighterTheme,
) {
    object ColorSerializer : KSerializer<Color> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("dev.fishies.ranim2.theming.Theme.ColorSerializer", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Color) {
            encoder.encodeString(value.toHtmlColor())
        }

        override fun deserialize(decoder: Decoder): Color {
            return decoder.decodeString().fromHtmlColor()
        }
    }

    fun contentColorFor(background: Background) = when (background) {
        Background.PRIMARY, Background.PRIMARY_VARIANT -> onPrimary
        Background.SECONDARY, Background.SECONDARY_VARIANT -> onSecondary
        Background.BACKGROUND -> onBackground
        Background.SURFACE -> onSurface
        Background.ERROR -> onError
    }
}

@Serializable
data class SyntaxStyle(
    val color: Color,
    @SerialName("font_style") val fontStyle: String? = null,
    @SerialName("font_weight") val fontWeight: Int? = null,
) {
    fun toSpanStyle() = SpanStyle(
        color = color,
        fontStyle = when (fontStyle) {
            "normal" -> FontStyle.Normal
            "italic", "oblique" -> FontStyle.Italic
            else -> null
        },
        fontWeight = fontWeight?.let(::FontWeight)
    )
}

@Serializable(SyntaxHighlighterTheme.Serializer::class)
data class SyntaxHighlighterTheme(
    val syntax: Map<String, SyntaxStyle>,
) {
    operator fun get(name: String): SpanStyle {
        var name = name
        repeat(name.count { it == '.' }) {
            syntax[name]?.toSpanStyle()?.let { return it }
            name = name.substringBeforeLast('.')
        }
        return syntax["text"]?.toSpanStyle() ?: SpanStyle(color = Color.Blue)
    }

    internal object Serializer : KSerializer<SyntaxHighlighterTheme> {
        val delegate = MapSerializer(String.serializer(), SyntaxStyle.serializer())
        override val descriptor: SerialDescriptor = delegate.descriptor

        override fun serialize(
            encoder: Encoder,
            value: SyntaxHighlighterTheme,
        ) = delegate.serialize(encoder, value.syntax)

        override fun deserialize(decoder: Decoder) = SyntaxHighlighterTheme(syntax = delegate.deserialize(decoder))
    }
}

fun Theme.toComposeColors() = Colors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    secondaryVariant = secondaryVariant,
    background = background,
    surface = surface,
    error = error,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onBackground = onBackground,
    onSurface = onSurface,
    onError = onError,
    isLight = isLight
)

val defaultTheme = Theme(
    primary = Color(0xFFFFFF86),
    primaryVariant = Color(0xFFFFDB77),
    secondary = Color(0xFF53D3FF),
    secondaryVariant = Color(0xFF5278FF),
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF181818),
    error = Color(0xFFFF7D68),
    onPrimary = Color(0xFF0F0F0F),
    onSecondary = Color(0xFF0F0F0F),
    onBackground = Color(0xFFF0F0F0),
    onSurface = Color(0xFFF0F0F0),
    onError = Color(0xFF0F0F0F),
    isLight = false,
    syntax = SyntaxHighlighterTheme(
        syntax = mapOf("text" to SyntaxStyle(Color.White))
    )
)

internal class ThemeProperties {
    var theme by mutableStateOf(defaultTheme)
}

internal class BackgroundProperties {
    var backgroundColor by mutableStateOf(Background.BACKGROUND)
}

var Element.theme by attached<_, _, Element?>(ThemeProperties::theme, recursive = true) { defaultTheme }
var Element.backgroundColor by attached<_, _, Element?>(
    BackgroundProperties::backgroundColor,
    recursive = true
) { Background.BACKGROUND }
