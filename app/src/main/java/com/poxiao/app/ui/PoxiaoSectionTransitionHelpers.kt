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

private const val TransitionSettleStart = 0.88f // 提前开始沉降，让过渡更丝滑
private const val TransitionSettleSpan = 0.12f
private const val TransitionAlphaBase = 0.98f // 初始透明度略低，增加渐显感
private const val TransitionAlphaRange = 0.02f
private const val TransitionRevealShiftY = 0.006f // 增加垂直位移幅度，更具动感
private const val TransitionSettleShiftY = 0.0012f
private const val TransitionScaleXBase = 0.988f // 初始缩放更小，增加空间拉伸感
private const val TransitionScaleXRange = 0.012f
private const val TransitionScaleXPulse = 0.0015f
private const val TransitionScaleYBase = 0.99f
private const val TransitionScaleYRange = 0.01f
private const val TransitionScaleYPulse = 0.0012f

@Composable
internal fun rememberPoxiaoSectionTransitionSnapshot(
    progress: Float,
): PoxiaoSectionTransitionSnapshot {
    // 使用更接近“流体”质感的自定义曲线：快速起步，极其柔和的终点
    val transitionEasing = remember { CubicBezierEasing(0.08f, 0.94f, 0.12f, 1f) }
    val easedTransition = transitionEasing.transform(progress.coerceIn(0f, 1f))
    val settleWindow = ((easedTransition - TransitionSettleStart) / TransitionSettleSpan).coerceIn(0f, 1f)
    
    // 优化后的沉降脉冲：增加了一点回弹感（Overshoot）
    val settlePulseBase = sin((settleWindow * Math.PI).toDouble()).toFloat().coerceAtLeast(0f)
    val settlePulse = (settlePulseBase * settlePulseBase * (1f - settleWindow * 0.75f)).coerceIn(0f, 1f)
    
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
            
            // 新增：极轻微的 X 轴旋转，增加空间纵深感
            rotationX = reveal * -1.5f 
            cameraDistance = 12f * density
            
            shadowElevation = 0f
        }
        .layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
}
