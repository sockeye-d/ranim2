package dev.fishies.ranim2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withCompositionLocal
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SkiaGraphicsContext
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.fishies.ranim2.containers.Axis
import dev.fishies.ranim2.containers.linearContainer
import dev.fishies.ranim2.containers.fraction
import dev.fishies.ranim2.core.*
import dev.fishies.ranim2.elements.makePainter
import dev.fishies.ranim2.elements.makeRectangle
import dev.fishies.ranim2.elements.makeText
import dev.fishies.ranim2.ranim2.generated.resources.Res
import dev.fishies.ranim2.theming.Theme
import dev.fishies.ranim2.theming.defaultTheme
import dev.fishies.ranim2.theming.theme
import dev.fishies.ranim2.theming.toComposeColors
import dev.fishies.ranim2.tweener.InOut
import dev.fishies.ranim2.tweener.Out
import dev.fishies.ranim2.tweener.cubic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import javax.imageio.ImageIO
import kotlin.time.Duration.Companion.milliseconds

//private fun svgPainter(resource: DrawableResource): Painter {
//    resource
//val resourceReader = LocalResourceReader.currentOrPreview
//val density = LocalDensity.current
//val svgPainter by rememberResourceState(resource, resourceReader, density, { emptySvgPainter }) { env ->
//    val path = resource.getResourceItemByEnvironment(env).path
//    val cached = loadImage(path, path, resourceReader) {
//        ImageCache.Svg(it.toSvgElement().toSvgPainter(density))
//    } as ImageCache.Svg
//    cached.painter
//}
//return svgPainter
//}

val catppuccinMocha = loadJson<Theme>(Res.getUri("files/catppuccin-mocha.json"))

fun subAnimation() = animation {
    //val circle = makeRectangle(Size(20f, 20f), Color.Red)
    val circle = makePainter(loadImage(Res.getUri("drawable/bug.png")))
    circle.size *= 0.1f

    repeat(50) {
        yield(circle::position.tween(to = Offset(10f, it * 10f), length = 50, tweener = cubic(InOut)))
        yield(frames = 20)
    }
}

@OptIn(ExperimentalTextApi::class)
val anim = animation {
    println(theme.primary)
    theme = catppuccinMocha
    println(theme.primary)
    println(catppuccinMocha.primary)
    //println(attachedProperties)
    //println(attachedProperties)
    //withAmbientValue(::theme to catppuccinMocha) {}
//    val code = """
//val shape = makeText(code, FontFamily("Iosevka Nerd Font"), color = catppuccinMocha["text"].color)
//shape.annotations = TreeSitterOdin.highlightToAnnotations(shape.text)
//val length = 120
//yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
//yield(subAnimation())
//while (true) {
//    yield(shape::position.tween(to = Offset(20f, 400f), length = length, tweener = quadratic(Out)))
//    yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
//}""".trimMargin()
//    val shape = makeText(code, FontFamily("Iosevka Nerd Font"), color = catppuccinMocha["text"].color)
//    shape.annotations = TreeSitterOdin.highlightToAnnotations(shape.text, catppuccinMocha)
//    val length = 120
//    yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
//    yield(subAnimation())
//    while (true) {
//        yield(shape::position.tween(to = Offset(20f, 400f), length = length, tweener = quadratic(Out)))
//        yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
//    }
//    val container = BoxContainer(Axis.X, 3.0f)
//    addChild(container)
    val container = linearContainer {
        size = Size(500f, 50f)
        separation = 5.0f
        val color = theme.primary
        println(theme.primary)
        makeText("This is some really cool text that is somewhat long and I'm just padding its length").apply {
            size = Size(100f, Float.NaN)
        }
        makeRectangle(Size(20f, 20f), color, radius = 2.0f)
    }

    val blueRect = container.linearContainer {
        axis = Axis.Y
        separation = 5.0f
        makeRectangle(Size(30f, 20f), theme.secondary, radius = 2.0f)
        makeRectangle(Size(30f, 10.0f), theme.primaryVariant, radius = 2.0f).apply {
            fraction = 0.5f
        }
        makeRectangle(Size(30f, 0.0f), theme.secondaryVariant, radius = 2.0f).apply {
            fraction = 1.0f
        }
    }

    val greenRect = container.makeRectangle(Size(20f, 20f), theme.surface, radius = 2.0f).apply {
        fraction = 1.0f
    }

    blueRect.fraction = 1.0f

    while (true) {
        yield(
            greenRect::fraction.tween(to = 0.5f, length = 500, tweener = cubic(InOut)),
            container::size.tween(to = Size(500f, 120f), length = 500, tweener = cubic(Out)),
        )
        yield(
            greenRect::fraction.tween(to = 1.0f, length = 500, tweener = cubic(InOut)),
            container::size.tween(to = Size(500f, 50f), length = 500, tweener = cubic(Out)),
        )
    }
}

@OptIn(InternalComposeUiApi::class)
fun main() = application {
    val layer = withCompositionLocal(LocalGraphicsContext provides SkiaGraphicsContext()) {
        val graphicsLayer = rememberGraphicsLayer()
        graphicsLayer.record(Density(3f), LayoutDirection.Ltr, IntSize(300, 300)) {
            anim.runLayoutPass()
            with(anim) { draw() }
        }
        graphicsLayer
    }

    MaterialTheme(colors = defaultTheme.toComposeColors()) {
        Window(onCloseRequest = ::exitApplication, title = "My Desktop App") {
            LaunchedEffect(Unit) {
                while (!anim.isFinished) {
                    withFrameMillis {
                        anim.tick()
                    }
                }
            }

            Box(Modifier.background(MaterialTheme.colors.background).fillMaxSize()) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onBackground) {
                    Column {
                        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            drawLayer(layer)
                        }
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button({ runBlocking { layer.saveImage() } }) {
                                Text("Save image")
                            }

                            Row {
                                Text("Debug layout bounds", Modifier.align(Alignment.CenterVertically))
                                Switch(Container.drawContainerOutlines, { Container.drawContainerOutlines = it })
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun GraphicsLayer.saveImage(file: File = File("/home/fish/Downloads/file.png")) =
    withContext(Dispatchers.IO) {
        ImageIO.write(
            toImageBitmap().toAwtImage(),
            file.extension,
            file,
        )
    }
