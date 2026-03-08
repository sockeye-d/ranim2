package dev.fishies.ranim2.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.reflect.KClass

interface Element {
    var attachedProperties: Map<KClass<*>, Any?>

    var children: List<Element>
    var parent: Element?
    var position: Offset
    var size: Size
    val minimumSize: Size
    var visible: Boolean
    fun DrawScope.draw()

    fun runLayoutPass()
}
