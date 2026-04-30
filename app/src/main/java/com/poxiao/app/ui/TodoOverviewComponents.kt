package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen

@Composable
internal fun TodoOverviewCard(
    tasks: List<TodoTask>,
    completedCount: Int,
    focusCount: Int,
    quadrantCounts: Map<TodoQuadrant, Int>,
) {
    GlassCard {
        Text("待办工作台", style = MaterialTheme.typography.headlineMedium, color = PineInk)
        Spacer(modifier = Modifier.height(8.dp))
        Text("支持四象限、自定义任务、提醒、重复与清单归类。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.78f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("总任务", tasks.size.toString(), ForestGreen)
            MetricCard("已完成", completedCount.toString(), MossGreen)
            MetricCard("专注回写", "${focusCount} 次", Ginkgo)
            MetricCard("目标达成", tasks.count { it.focusGoal > 0 && it.focusCount >= it.focusGoal }.toString(), TeaGreen)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            TodoQuadrant.entries.forEach { item ->
                Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.54f), modifier = Modifier.width(164.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, color = PineInk, modifier = Modifier.fillMaxWidth(0.72f))
                            Text("${quadrantCounts[item] ?: 0}", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f), maxLines = 2)
                    }
                }
            }
        }
    }
}
