
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.SkiaGraphicsContext
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.fishies.ranim2.gui.MainScreen
import dev.fishies.ranim2.theming.Theme
import dev.fishies.ranim2.theming.defaultTheme

fun main(args: Array<String>) = application {
    println(args)
    val scope = rememberCoroutineScope()
    @OptIn(InternalComposeUiApi::class)
    CompositionLocalProvider(LocalGraphicsContext provides remember { SkiaGraphicsContext() }) {
        MaterialTheme(colors = defaultTheme.toComposeColors()) {
            Window(onCloseRequest = ::exitApplication, title = "My Desktop App") {
                MainScreen(emptyList(), false, {}, null, {})
            }
        }
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
