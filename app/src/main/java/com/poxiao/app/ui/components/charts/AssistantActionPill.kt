package com.poxiao.app.ui.components.charts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

@Composable
fun AssistantActionPill(
    action: ParsedAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val palette = PoxiaoThemeState.palette
    var isExecuted by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // 根据 action.type 解析不同 UI
    val (baseIcon, baseTitle, subtitle) = when (action.type) {
        "create_todo" -> {
            val taskTitle = action.payload.optString("title", "未命名任务")
            val priority = action.payload.optString("priority", "Medium")
            Triple(Icons.Rounded.AddTask, "新建待办", "[$priority] $taskTitle")
        }
        "start_focus" -> {
            val taskTitle = action.payload.optString("title", "自习")
            val minutes = action.payload.optInt("minutes", 25)
            Triple(Icons.Rounded.PlayArrow, "开始专注", "$minutes 分钟 · $taskTitle")
        }
        else -> {
            Triple(Icons.Rounded.Build, "执行动作", action.type)
        }
    }

    val icon = if (isExecuted) Icons.Rounded.CheckCircle else baseIcon
    val title = if (isExecuted) "已执行" else baseTitle

    // 引入颜色过渡动画
    val backgroundColor by animateColorAsState(
        targetValue = if (isExecuted) palette.ink.copy(alpha = 0.05f) else palette.primary.copy(alpha = 0.1f),
        animationSpec = spring(stiffness = 500f)
    )
    
    val iconBackgroundColor by animateColorAsState(
        targetValue = if (isExecuted) palette.ink.copy(alpha = 0.1f) else palette.primary.copy(alpha = 0.2f),
        animationSpec = spring(stiffness = 500f)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isExecuted) palette.ink.copy(alpha = 0.6f) else palette.primary,
        animationSpec = spring(stiffness = 500f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(enabled = !isExecuted) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExecuted = true
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isExecuted) palette.ink.copy(alpha = 0.4f) else palette.ink,
                    maxLines = 1
                )
            }
        }
    }
}
