package com.poxiao.app.ui.components.charts

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState

@Composable
fun AssistantActionPill(
    action: ParsedAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val palette = PoxiaoThemeState.palette

    // 根据 action.type 解析不同 UI
    val (icon, title, subtitle) = when (action.type) {
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.primary.copy(alpha = 0.1f))
            .clickable {
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
                    .background(palette.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = palette.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.primary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink,
                    maxLines = 1
                )
            }
        }
    }
}
