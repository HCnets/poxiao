package com.poxiao.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.PineInk
import java.time.Instant
import java.time.LocalDateTime

@Composable
internal fun ReviewExecutionDetailBlock(
    execution: ReviewBridgeExecutionSummary,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (execution.replaySourceExecutedAt > 0L) {
            Text(
                "回放自 ${formatSyncTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(execution.replaySourceExecutedAt), java.time.ZoneId.systemDefault()))}",
                style = MaterialTheme.typography.labelMedium,
                color = ForestGreen,
            )
        }
        if (execution.diffSummary.isNotBlank()) {
            Text(
                execution.diffSummary,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.78f),
            )
        }
        if (execution.createdTaskTitles.isNotEmpty()) {
            Text("生成待办", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
            execution.createdTaskTitles.take(3).forEach { title ->
                Text("• $title", style = MaterialTheme.typography.bodySmall, color = PineInk)
            }
        }
        if (execution.boundTaskTitle.isNotBlank()) {
            Text(
                "已绑定专注：${execution.boundTaskTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.78f),
            )
        }
    }
}
