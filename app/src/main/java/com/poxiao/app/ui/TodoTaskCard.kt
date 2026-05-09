package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.interactions.bouncyClick
import com.poxiao.app.ui.interactions.rememberHapticManager
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun TodoTaskCard(
    task: TodoTask,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    onBindPomodoro: () -> Unit,
    onNotify: () -> Unit,
    onToggleSubtask: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
) {
    val dueStatus = remember(task.dueText, task.done) { todoDueStatus(task) }
    val focusProgressLabel = if (task.focusGoal > 0) "专注 ${task.focusCount}/${task.focusGoal} 轮" else "专注 ${task.focusCount} 轮"
    val focusGoalReached = task.focusGoal > 0 && task.focusCount >= task.focusGoal
    val hapticManager = rememberHapticManager()

    GlassCard(modifier = modifier.bouncyClick(hapticManager = hapticManager, onClick = onEdit)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.fillMaxWidth(0.72f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = task.title, 
                        style = MaterialTheme.typography.titleLarge, 
                        color = PineInk.copy(alpha = if (task.done) 0.5f else 1f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    if (task.note.isNotBlank()) {
                        Text(
                            text = task.note, 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = ForestDeep.copy(alpha = if (task.done) 0.38f else 0.6f)
                        )
                    }
                }
                ActionPill(
                    text = if (task.done) "已完成" else "完成",
                    background = if (task.done) MossGreen.copy(alpha = 0.6f) else ForestGreen,
                    onClick = {
                        if (!task.done) hapticManager.playSuccess() else hapticManager.playLightClick()
                        onToggle()
                    }
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ActionPill(dueStatus.label, dueStatus.color, onClick = {})
                Text(
                    text = "${task.listName} · ${task.dueText}", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = ForestDeep.copy(alpha = 0.5f)
                )
            }
            if (task.reminderText.isNotBlank() || task.repeatText.isNotBlank()) {
                Text(
                    text = "${task.reminderText.ifBlank { "不提醒" }} · ${task.repeatText}", 
                    style = MaterialTheme.typography.labelMedium, 
                    color = ForestDeep.copy(alpha = 0.4f)
                )
            }
            
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPill(task.priority.title, when (task.priority) {
                    TodoPriority.High -> Ginkgo
                    TodoPriority.Medium -> TeaGreen
                    TodoPriority.Low -> WarmMist
                }, onClick = {})
                ActionPill(task.quadrant.title, TeaGreen, onClick = {})
                ActionPill(focusProgressLabel, if (focusGoalReached) MossGreen else BambooGlass, onClick = {})
            }
            
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!task.done) ActionPill("顺延", CloudWhite.copy(alpha = 0.7f), onClick = onPostpone)
                ActionPill("绑定专注", ForestGreen.copy(alpha = 0.8f), onClick = onBindPomodoro)
                ActionPill("提醒", Ginkgo.copy(alpha = 0.8f), onClick = onNotify)
                if (canMoveUp) ActionPill("上移", CloudWhite.copy(alpha = 0.5f), onClick = onMoveUp)
                if (canMoveDown) ActionPill("下移", CloudWhite.copy(alpha = 0.5f), onClick = onMoveDown)
            }
            if (task.focusGoal > 0) {
                Text(
                    if (focusGoalReached) "本任务的专注目标已完成。" else "距离专注目标还差 ${task.focusGoal - task.focusCount} 轮。",
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.72f),
                )
            }
            if (task.tags.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.forEach { tag ->
                        ActionPill(tag, Ginkgo, onClick = {})
                    }
                }
            }
            if (task.subtasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.subtasks.forEachIndexed { index, subtask ->
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.34f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ForestDeep.copy(alpha = if (subtask.done) 0.56f else 0.82f),
                                    modifier = Modifier.fillMaxWidth(0.72f),
                                )
                                ActionPill(
                                    text = if (subtask.done) "已完成" else "完成",
                                    background = if (subtask.done) MossGreen else ForestGreen,
                                    onClick = { onToggleSubtask(index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
