package dev.fishies.sailfish.gui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.fishies.sailfish.gui.util.rememberSkiaGraphicsContext
import dev.fishies.sailfish.gui.util.toComposeColors
import dev.fishies.sailfish.theming.LocalTheme
import dev.fishies.sailfish.theming.Theme
import dev.fishies.sailfish.util.loadJson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.ExperimentalSerializationApi
import java.nio.file.Path
import kotlin.io.path.absolute

@OptIn(ExperimentalSerializationApi::class, ExperimentalCoroutinesApi::class)
fun main(args: Array<String>) = application {
    val scope = rememberCoroutineScope()
    val vm = remember {
        MainScreenViewModel(
            scope,
            args.firstOrNull()?.let { Path.of(it).absolute() },
        )
    }

    val animations by vm.animations.collectAsState(Outcome.Progress)
    val theme = loadJson<Theme>("catppuccin-mocha.json")
    val animationState by vm.animationState.collectAsState(null)

    LaunchedEffect(Unit) {
        vm.ready()
    }

    Window(onCloseRequest = ::exitApplication, title = "Animation viewer", onKeyEvent = {
        when {
            it.key == Key.Spacebar && it.type == KeyEventType.KeyDown -> {
                vm.togglePaused()
                true
            }
            else -> false
        }
    }) {

        MaterialTheme(colors = theme.toComposeColors()) {
            CompositionLocalProvider(
                LocalGraphicsContext provides rememberSkiaGraphicsContext(),
                LocalTheme provides theme,
            ) {
                MainScreen(
                    animations,
                    animationState,
                    vm.cursorFrame.collectAsState().value,
                    vm::setCursorFrame,
                    vm::setPaused,
                    vm::setLoop,
                    vm::setActiveAnimation,
                    vm::setMarker,
                    vm::seekToStart,
                    vm::seekToEnd,
                    vm::seekBy,
                )
            }
        }
    }
}
