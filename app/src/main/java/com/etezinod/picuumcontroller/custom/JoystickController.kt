package com.etezinod.picuumcontroller.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun JoystickCompose(
    modifier: Modifier,
    onChange: (x: Float, y: Float, m: Float) -> Unit = { _, _, _ -> }
) {
    var center = remember { Offset.Zero }
    var position by remember { mutableStateOf(Offset.Infinite) }

    Canvas(
        modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val current = change.position
                    val relative = current - center

                    val angle = atan2(relative.y, relative.x)
                    val cos = cos(angle)
                    val sin = sin(angle)

                    val radius = min(relative.getDistance(), center.x)
                    val actual = Offset(radius * cos, radius * sin) + center
                    val multiplier = radius / center.x

                    onChange(cos, -sin, multiplier)

                    position = actual
                }
            }
            .onGloballyPositioned {
                val bounds = it.boundsInRoot()
                center = bounds.bottomRight / 2F
                position = center
            }
    ) {
        drawCircle(Color.Gray)
        drawCircle(
            Color.White,
            size.minDimension / 2.05F
        )
        drawCircle(
            Color.Black,
            size.minDimension * .125F,
            center = position
        )
    }
}