package dev.fishies.sailfish.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.*
import dev.fishies.sailfish.Animation

@Composable
@OptIn(InternalComposeUiApi::class)
fun LayerDisplay(
    graphicsLayer: GraphicsLayer,
    modifier: Modifier = Modifier,
    layerSizeChanged: (IntSize) -> Unit,
) {
    Canvas(modifier = modifier.onSizeChanged(layerSizeChanged)) {
        drawLayer(graphicsLayer)
    }
}

@Composable
fun GraphicsLayer.configureAnimation(
    animation: Animation?, layerSize: IntSize
): GraphicsLayer {
    record(Density(1f), LayoutDirection.Ltr, layerSize) {
        animation?.run {
            runLayoutPass()
            draw()
        }
    }
    return this
}
