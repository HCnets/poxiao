package com.poxiao.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import kotlin.math.sin

@Composable
internal fun rememberPoxiaoSectionTransitionSnapshot(
    progress: Float,
): PoxiaoSectionTransitionSnapshot {
    val transitionEasing = remember { CubicBezierEasing(0.14f, 0.98f, 0.22f, 1f) }
    val easedTransition = transitionEasing.transform(progress.coerceIn(0f, 1f))
    val settleWindow = ((easedTransition - 0.9f) / 0.1f).coerceIn(0f, 1f)
    val settlePulseBase = sin((settleWindow * Math.PI).toDouble()).toFloat().coerceAtLeast(0f)
    val settlePulse = (settlePulseBase * settlePulseBase * (1f - settleWindow * 0.68f)).coerceIn(0f, 1f)
    return PoxiaoSectionTransitionSnapshot(
        easedTransition = easedTransition,
        settlePulse = settlePulse,
    )
}

internal fun Modifier.poxiaoSectionHostModifier(
    isCurrentSection: Boolean,
    transition: PoxiaoSectionTransitionSnapshot,
): Modifier {
    if (!isCurrentSection) {
        return this
            .zIndex(-1f)
            .layout { measurable, constraints ->
                measurable.measure(constraints)
                layout(0, 0) {}
            }
    }

    return this
        .fillMaxSize()
        .zIndex(1f)
        .graphicsLayer {
            val reveal = 1f - transition.easedTransition
            alpha = 0.99f + (transition.easedTransition * 0.01f)
            translationX = 0f
            translationY = size.height * ((0.004f * reveal) - (0.0006f * transition.settlePulse))
            scaleX = 0.9935f + (transition.easedTransition * 0.0065f) + (0.001f * transition.settlePulse)
            scaleY = 0.994f + (transition.easedTransition * 0.006f) + (0.0008f * transition.settlePulse)
            shadowElevation = 0f
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
}
