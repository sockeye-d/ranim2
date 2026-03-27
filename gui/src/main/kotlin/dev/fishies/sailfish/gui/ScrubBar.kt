package dev.fishies.sailfish.gui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.fishies.sailfish.gui.util.onDragAbsolute
import dev.fishies.sailfish.util.exp10
import kotlin.math.*

@Stable
class ScrubBarState {
    var scroll by mutableFloatStateOf(0f)
    var zoom by mutableFloatStateOf(1f)

    val speed = 2.0f
}

val timelineElementAnimation: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessHigh)
val tickShape = RoundedCornerShape(2.0.dp)
private val knobShape = GenericShape { (width, height), _ ->
    lineTo(width, 0.0f)
    lineTo(width * 0.5f, height)
    close()
}

val timelineHandleShape =
    RoundedCornerShape(topStartPercent = 0, bottomStartPercent = 50, topEndPercent = 50, bottomEndPercent = 50)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ScrubBar(
    state: ScrubBarState,
    cursorFrame: Float,
    setCursorFrame: (Int) -> Unit,
    modifier: Modifier = Modifier,
    drawContent: DrawScope.(getPosition: (frame: Float) -> Float, getFrame: (position: Float) -> Float) -> Unit = { _, _ -> },
    content: @Composable BoxScope.(getPosition: (frame: Float) -> Float, getFrame: (position: Float) -> Float) -> Unit = { _, _ -> },
) {
    val contentColor = LocalContentColor.current
    val tickColor = lerp(LocalContentColor.current, MaterialTheme.colors.surface, 0.5f)
    val primaryColor = MaterialTheme.colors.primary
    val measurer = rememberTextMeasurer(1000)
    var mouseX by remember { mutableFloatStateOf(0f) }
    var pressed by remember { mutableStateOf(false) }

    val smoothed by animateOffsetAsState(Offset(state.scroll, state.zoom), spring(stiffness = 800f))
    val localFontSize = LocalTextStyle.current.fontSize

    fun updateCursorFrame() {
        setCursorFrame(((mouseX + state.scroll) / 50f / state.zoom).roundToInt())
    }

    val modifier = modifier.scrollable(rememberScrollableState {
        state.scroll = (state.scroll - it * state.speed).coerceAtLeast(-100f)
        it
    }, Orientation.Vertical).transformable(rememberTransformableState { zoomChange, _, _ ->
        val zoomChange = zoomChange.pow(state.speed)
        state.zoom *= zoomChange
        state.scroll = (-mouseX + (mouseX + state.scroll) * zoomChange).coerceAtLeast(-100f)
    })
    val textSize by derivedStateOf { measurer.measure("0", TextStyle(fontSize = localFontSize)).multiParagraph.height }
    Box(modifier) {
        Canvas(Modifier.matchParentSize().pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val e = awaitPointerEvent()
                    if (e.type == PointerEventType.Move) {
                        mouseX = e.changes.first().position.x
                        if (pressed) {
                            updateCursorFrame()
                        }
                    }
                    if (e.button == PointerButton.Primary && e.type == PointerEventType.Press) {
                        pressed = true
                        updateCursorFrame()
                    }
                    if (e.button == PointerButton.Primary && e.type == PointerEventType.Release) {
                        pressed = false
                    }
                }
            }
        }) {
            val (scroll, zoom) = smoothed
            val scale = log10(3f / zoom)
            val scaleFloor = scale.toInt().coerceAtLeast(0)
            val powerScaleFloor = exp10(scaleFloor)
            val powerScaleCeil = exp10(scaleFloor + 1)
            val scaleFrac = ((3f / zoom) - powerScaleFloor) / (powerScaleFloor * 9)
            val progression = SnappedIntProgression(
                // A little padding to make sure numbers don't get cut off at the ends
                (scroll / 50.0f / zoom).toInt() - powerScaleFloor,
                ((scroll + size.width) / 50.0f / zoom).toInt() + 1 + powerScaleFloor,
                powerScaleFloor
            )
            for (frame in progression) {
                val alpha = if (frame % powerScaleCeil == 0) 1.0f else (1.0f - scaleFrac).pow(2.0f)
                val color = contentColor.copy(alpha = alpha)

                val x = frame * 50f * zoom - scroll
                val tickSize = Size(2.0f, (size.height - textSize) * alpha)
                withTransform({ translate(left = x - 1.0f, top = textSize) }) {
                    drawOutline(
                        tickShape.createOutline(tickSize, layoutDirection, Density(density)),
                        tickColor.copy(alpha = alpha)
                    )
                }
                val result = measurer.measure("$frame", TextStyle(fontSize = localFontSize * (alpha * 0.5 + 0.5)))
                drawText(
                    result, color, Offset(x - result.multiParagraph.width * 0.5f, 20.0f - result.multiParagraph.height)
                )
            }
            val cursorX = cursorFrame * 50f * zoom - scroll
            withTransform({ translate(left = cursorX - 1.0f, top = textSize) }) {
                drawOutline(
                    tickShape.createOutline(Size(2.0f, size.height - textSize), layoutDirection, Density(density)),
                    primaryColor
                )
            }
            withTransform({ translate(left = cursorX - 4.0f, top = textSize) }) {
                drawOutline(
                    knobShape.createOutline(Size(8.0f, 8.0f * sqrt(3.0f) / 2.0f), layoutDirection, Density(density)),
                    primaryColor
                )
            }
            withTransform({ translate(top = textSize) }) {
                drawContent({ it * 50f * zoom - scroll }, { (it + scroll) / 50f / zoom })
            }
        }

        val smoothScroll by derivedStateOf { smoothed.x }
        val smoothZoom by derivedStateOf { smoothed.y }

        content({ it * 50f * smoothZoom - smoothScroll }, { (it + smoothScroll) / 50f / smoothZoom })
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimelineHandle(
    getPosition: (frame: Float) -> Float,
    getFrame: (position: Float) -> Float,
    frame: Int,
    setFrame: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val getPosition by rememberUpdatedState(getPosition)
    val getFrame by rememberUpdatedState(getFrame)
    val frame by rememberUpdatedState(frame)
    val animatedFrame by animateFloatAsState(frame.toFloat(), timelineElementAnimation)
    val x by derivedStateOf { getPosition(animatedFrame) - 1f }
    val modifier = modifier.absoluteOffset(x = x.dp).onDragAbsolute(
        PointerMatcher.Primary,
        initialOffset = { Offset(getPosition(animatedFrame) - 1f, 0f) },
    ) { (x, _) ->
        setFrame(getFrame(x).roundToInt())
    }.shadow(8.dp, timelineHandleShape)
    Surface(
        shape = timelineHandleShape,
        color = MaterialTheme.colors.primary,
        modifier = modifier,
        elevation = 8.dp,
    ) {
        Box(Modifier.padding(horizontal = 8.dp)) {
            content()
        }
    }
}

class SnappedIntProgression(val min: Int, val max: Int, val step: Int) : Iterable<Int> {
    override fun iterator(): IntIterator = Iterator(min, max, step)

    private class Iterator(min: Int, val max: Int, val step: Int, sign: Int = if (min < 0) 0 else 1) : IntIterator() {
        var current: Int = if (min / step * step == min) min else (min / step + sign) * step
        override fun nextInt(): Int {
            val last = current
            current += step
            return last
        }

        override fun hasNext() = current <= max
    }
}
