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

private const val TransitionSettleStart = 0.9f
private const val TransitionSettleSpan = 0.1f
private const val TransitionAlphaBase = 0.99f
private const val TransitionAlphaRange = 0.01f
private const val TransitionRevealShiftY = 0.004f
private const val TransitionSettleShiftY = 0.0006f
private const val TransitionScaleXBase = 0.9935f
private const val TransitionScaleXRange = 0.0065f
private const val TransitionScaleXPulse = 0.001f
private const val TransitionScaleYBase = 0.994f
private const val TransitionScaleYRange = 0.006f
private const val TransitionScaleYPulse = 0.0008f

@Composable
internal fun rememberPoxiaoSectionTransitionSnapshot(
    progress: Float,
): PoxiaoSectionTransitionSnapshot {
    val transitionEasing = remember { CubicBezierEasing(0.14f, 0.98f, 0.22f, 1f) }
    val easedTransition = transitionEasing.transform(progress.coerceIn(0f, 1f))
    val settleWindow = ((easedTransition - TransitionSettleStart) / TransitionSettleSpan).coerceIn(0f, 1f)
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
        return hiddenPoxiaoSectionHostModifier()
    }

    return visiblePoxiaoSectionHostModifier(transition)
}

private fun Modifier.hiddenPoxiaoSectionHostModifier(): Modifier {
    return this
        .zIndex(-1f)
        .layout { measurable, constraints ->
            measurable.measure(constraints)
            layout(0, 0) {}
        }
}

private fun Modifier.visiblePoxiaoSectionHostModifier(
    transition: PoxiaoSectionTransitionSnapshot,
): Modifier {
    return this
        .fillMaxSize()
        .zIndex(1f)
        .graphicsLayer {
            val reveal = 1f - transition.easedTransition
            alpha = TransitionAlphaBase + (transition.easedTransition * TransitionAlphaRange)
            translationX = 0f
            translationY = size.height * ((TransitionRevealShiftY * reveal) - (TransitionSettleShiftY * transition.settlePulse))
            scaleX = TransitionScaleXBase + (transition.easedTransition * TransitionScaleXRange) + (TransitionScaleXPulse * transition.settlePulse)
            scaleY = TransitionScaleYBase + (transition.easedTransition * TransitionScaleYRange) + (TransitionScaleYPulse * transition.settlePulse)
            shadowElevation = 0f
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
}
