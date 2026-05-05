package com.poxiao.app.ui.interactions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 为组件添加具有物理阻尼感 (Spring) 的按压缩放动效。
 * 替代原生生硬的 Ripple 点击效果，提升“高级物理实感”。
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
    
    // 使用 spring 动画打造 Q 弹感
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncy_click_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null, // 移除系统默认的水波纹
            onClick = { 
                hapticManager?.playLightClick() // 释放时可能伴随操作反馈，由具体业务决定，此处默认不再震动避免重复
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
                        // 忽略震动异常
                    }
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}