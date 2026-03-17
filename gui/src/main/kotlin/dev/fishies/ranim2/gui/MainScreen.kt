package dev.fishies.ranim2.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.fishies.ranim2.*
import dev.fishies.ranim2.util.saveImage
import kotlinx.coroutines.runBlocking

@Composable
fun MainScreen(
    animations: List<AnimationProvider>,
    paused: Boolean,
    setPaused: (Boolean) -> Unit,
    activeAnimation: Animation?,
    setActiveAnimation: (Animation?) -> Unit
) {
    var paused by remember { mutableStateOf(true) }
    LaunchedEffect(paused) {
        if (!paused && activeAnimation != null) {
            while (!activeAnimation.isFinished) {
                withFrameMillis {
                    activeAnimation.tick()
                }
            }
        }
    }
    var layerSize by remember { mutableStateOf(IntSize.Zero) }
    val graphicsLayer = rememberGraphicsLayer().configureAnimation(activeAnimation, layerSize)

    Box(Modifier.background(MaterialTheme.colors.background).fillMaxSize().onSizeChanged { layerSize = it }) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onBackground) {
            Column {
                LayerDisplay(graphicsLayer, Modifier.fillMaxWidth().weight(1f)) { layerSize = it }
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button({ paused = !paused }) {
                        Text(if (paused) "Play" else "Pause")
                    }

                    Button({ runBlocking { graphicsLayer.saveImage() } }) {
                        Text("Save image")
                    }

                    Button({}) {
                        Text("Save")
                    }

                    // Button({ println(anim?.treeString()) }) {
                    //     Text("Dump scene tree")
                    // }

                    Row {
                        Text("Debug layout bounds", Modifier.align(Alignment.CenterVertically))
                        with(Container) {
                            Switch(drawContainerOutlines, { drawContainerOutlines = it })
                        }
                    }
                }
            }
        }
    }
}
