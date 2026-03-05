package dev.fishies.ranim2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.DrawerDefaults.shape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withCompositionLocal
import androidx.compose.ui.Alignment
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.SkiaGraphicsContext
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.GenericFontFamily
import androidx.compose.ui.text.font.SystemFontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.fishies.ranim2.core.animation
import dev.fishies.ranim2.core.tween
import dev.fishies.ranim2.core.yield
import dev.fishies.ranim2.elements.makeText
import dev.fishies.ranim2.languages.kotlin.TreeSitterKotlin
//import dev.fishies.ranim2.languages.kotlin.TreeSitterKotlin
//import dev.fishies.ranim2.languages.kotlin.TreeSitterKotlin
import dev.fishies.ranim2.ranim2.generated.resources.Res
import dev.fishies.ranim2.syntax.SyntaxHighlighterTheme
import dev.fishies.ranim2.tweener.Out
import dev.fishies.ranim2.tweener.quadratic
import dev.fishies.ranim2.syntax.highlight
import io.github.treesitter.ktreesitter.QueryMatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.decodeToSvgPainter
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

fun String.toComposeColor() = removePrefix("#").run {
    when (length) {
        6 -> Color(toLong(16) or 0xFF000000)
        else -> error("Invalid color string $this")
    }
}

val catppuccinMocha = loadJson<SyntaxHighlighterTheme>("files/catppuccin-mocha.json")

fun Sequence<QueryMatch>.toAnnotations() = mapNotNull { q ->
    q.captures.firstOrNull()?.let {
        AnnotatedString.Range(catppuccinMocha[it.name], it.node.startByte.toInt(), it.node.endByte.toInt())
    }
}.sortedBy { it.start }

@OptIn(ExperimentalTextApi::class)
val anim = animation {
    val code = $$"""
    |fun main() {
    |    @OptIn(ExperimentalTextApi::class)
    |    val anim = animation {
    |        val code = ""$${'"'}fun main() {
    |               val x = "world!"
    |               println("Hello ${x}")
    |            }""$${'"'}.trimMargin()
    |        val shape = makeText(code, FontFamily("Iosevka Nerd Font"))
    |        shape.annotations = TreeSitterKotlin.highlight(shape.text).toAnnotations().toList()
    |        val length = 120
    |        yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
    |        //while (true) {
    |            //yield(shape::position.tween(to = Offset(20f, 100f), length = length, tweener = quadratic(In)))
    |        //}
    |    }
    |}""".trimMargin()
    println(code)
    val shape = makeText(code, FontFamily("Iosevka Nerd Font"), color = Color.Red)
    shape.annotations = TreeSitterKotlin.highlight(shape.text).toAnnotations().toList()
    val length = 120
    yield(shape::position.tween(to = Offset(20f, 40f), length = length, tweener = quadratic(Out)))
    //while (true) {
        //yield(shape::position.tween(to = Offset(20f, 100f), length = length, tweener = quadratic(In)))
    //}
}

private fun loadSvg(path: String): Painter =
    runBlocking { Res.readBytes(path) }.decodeToSvgPainter(Density(1.0f))

private fun loadImage(path: String, filterQuality: FilterQuality = FilterQuality.Low): Painter = BitmapPainter(
    runBlocking { Res.readBytes(path) }.decodeToImageBitmap(),
    filterQuality = filterQuality
)

private fun loadJsonElement(path: String) =
    Json.parseToJsonElement(runBlocking { Res.readBytes(path) }.toString(Charsets.UTF_8))

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> loadJson(path: String) =
    Json.decodeFromStream<T>(runBlocking { Res.readBytes(path) }.inputStream())

@OptIn(InternalComposeUiApi::class)
fun main() = application {
    val layer = withCompositionLocal(LocalGraphicsContext provides SkiaGraphicsContext()) {
        val graphicsLayer = rememberGraphicsLayer()
        graphicsLayer.record(Density(3f), LayoutDirection.Ltr, IntSize(300, 300)) {
            with(anim) { draw() }
        }
        graphicsLayer
    }
    Window(onCloseRequest = ::exitApplication, title = "My Desktop App") {
        MaterialTheme(MaterialTheme.colors.copy(background = Color(0xFF1E1E2E))) {
            LaunchedEffect(Unit) {
                while (!anim.isFinished) {
                    anim.tick()
                    delay(3.57.milliseconds)
                }
            }

            Box(Modifier.background(MaterialTheme.colors.background).fillMaxSize())

            Column {
                Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    drawLayer(layer)
                }
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Button({ runBlocking { layer.saveImage() } }) {
                        Text("Save image")
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
