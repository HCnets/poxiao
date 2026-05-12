package com.poxiao.app.ui.interactions

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 为组件添加具有物理阻尼感 (Advanced Spring) 的按压缩放动效。
 * 替代原生生硬的 Ripple 点击效果，提升"高级物理实感"。
 *
 * 极致性能优化版：放弃底层的 pointerInput 拦截，改用 interactionSource.collectIsPressedAsState()，
 * 避免了 Compose 手势分发系统的冲突与卡顿。
 */
fun Modifier.bouncyClick(
    scaleDown: Float = 0.94f,
    hapticManager: HapticManager? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下时触发轻微震动
    if (isPressed) {
        try {
            hapticManager?.playLightClick()
        } catch (e: Exception) {}
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = if (isPressed) {
            spring(stiffness = 600f, dampingRatio = 0.6f)
        } else {
            spring(stiffness = 400f, dampingRatio = 0.5f)
        },
        label = "bouncyClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // 去除原生波纹，完全依赖物理缩放
            onClick = onClick
        )
}