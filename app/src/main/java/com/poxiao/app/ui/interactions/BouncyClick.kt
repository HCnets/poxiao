package com.poxiao.app.ui.interactions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 为组件添加具有物理阻尼感 (Advanced Spring) 的按压缩放动效。
 * 替代原生生硬的 Ripple 点击效果，提升"高级物理实感"。
 *
 * v1.9.8 升级：采用方向感知的双弹簧规格：
 *   - 按下 (Down)：stiffness=600, dampingRatio=0.6 (极快响应，微阻尼)
 *   - 释放 (Up)：stiffness=400, dampingRatio=0.5 (Q 弹恢复)
 *
 * @param scaleDown 按压时的缩放比例 (默认 0.94f)
 * @param hapticManager 触觉反馈管理器，如果传入会在按下时触发轻微震动
 * @param onClick 点击事件回调
 */
fun Modifier.bouncyClick(
    scaleDown: Float = 0.94f,
    hapticManager: HapticManager? = null,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scale.animateTo(
                targetValue = scaleDown,
                animationSpec = spring(stiffness = 600f, dampingRatio = 0.6f),
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = 400f, dampingRatio = 0.5f),
            )
        }
    }

    this
        .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                onClick()
            }
        )
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    try {
                        hapticManager?.playLightClick()
                    } catch (e: Exception) {
                    }
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}