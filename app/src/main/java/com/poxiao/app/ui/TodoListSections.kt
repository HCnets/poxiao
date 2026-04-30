package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun TodoListSections(
    viewMode: TodoViewMode,
    tasks: List<TodoTask>,
    finalVisibleTasks: List<TodoTask>,
    groupedTasks: Map<String, List<TodoTask>>,
    archivedTasks: List<TodoTask>,
    archiveExpanded: Boolean,
    onToggleArchiveExpanded: () -> Unit,
    onClearCompleted: () -> Unit,
    onToggleTask: (TodoTask) -> Unit,
    onPostponeTask: (TodoTask) -> Unit,
    onEditTask: (TodoTask) -> Unit,
    onBindPomodoroTask: (TodoTask) -> Unit,
    onNotifyTask: (TodoTask) -> Unit,
    onToggleSubtask: (TodoTask, Int) -> Unit,
    onMoveUpTask: (TodoTask) -> Unit,
    onMoveDownTask: (TodoTask) -> Unit,
    canMoveUp: (TodoTask) -> Boolean,
    canMoveDown: (TodoTask) -> Boolean,
) {
    GlassCard {
        Spacer(modifier = Modifier.height(12.dp))
        if (viewMode == TodoViewMode.Flat) {
            finalVisibleTasks.forEachIndexed { index, task ->
                TodoTaskCard(
                    task = task,
                    onToggle = { onToggleTask(task) },
                    onPostpone = { onPostponeTask(task) },
                    onEdit = { onEditTask(task) },
                    onBindPomodoro = { onBindPomodoroTask(task) },
                    onNotify = { onNotifyTask(task) },
                    onToggleSubtask = { onToggleSubtask(task, it) },
                    onMoveUp = { onMoveUpTask(task) },
                    onMoveDown = { onMoveDownTask(task) },
                    canMoveUp = canMoveUp(task),
                    canMoveDown = canMoveDown(task),
                )
                if (index != finalVisibleTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
            }
        } else if (viewMode == TodoViewMode.Grouped) {
            groupedTasks.entries.forEachIndexed { groupIndex, entry ->
                Text(entry.key, style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                entry.value.forEachIndexed { index, task ->
                    TodoTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onPostpone = { onPostponeTask(task) },
                        onEdit = { onEditTask(task) },
                        onBindPomodoro = { onBindPomodoroTask(task) },
                        onNotify = { onNotifyTask(task) },
                        onToggleSubtask = { onToggleSubtask(task, it) },
                        onMoveUp = { onMoveUpTask(task) },
                        onMoveDown = { onMoveDownTask(task) },
                        canMoveUp = canMoveUp(task),
                        canMoveDown = canMoveDown(task),
                    )
                    if (index != entry.value.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                }
                if (groupIndex != groupedTasks.entries.size - 1) Spacer(modifier = Modifier.height(14.dp))
            }
        } else {
            val calendarGroups = finalVisibleTasks.groupBy { it.dueText }.toList()
            calendarGroups.forEachIndexed { groupIndex, entry ->
                Text(entry.first, style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                entry.second.forEachIndexed { index, task ->
                    TodoTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onPostpone = { onPostponeTask(task) },
                        onEdit = { onEditTask(task) },
                        onBindPomodoro = { onBindPomodoroTask(task) },
                        onNotify = { onNotifyTask(task) },
                        onToggleSubtask = { onToggleSubtask(task, it) },
                        onMoveUp = {},
                        onMoveDown = {},
                        canMoveUp = false,
                        canMoveDown = false,
                    )
                    if (index != entry.second.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                }
                if (groupIndex != calendarGroups.lastIndex) Spacer(modifier = Modifier.height(14.dp))
            }
        }
        if (archivedTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("完成归档", style = MaterialTheme.typography.titleMedium, color = PineInk)
                ActionPill("${archivedTasks.size}", MossGreen, onClick = {})
                ActionPill(if (archiveExpanded) "收起" else "展开", WarmMist, onClick = onToggleArchiveExpanded)
                ActionPill("清空已完成", CloudWhite, onClick = onClearCompleted)
            }
            if (archiveExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                archivedTasks.forEachIndexed { index, task ->
                    TodoTaskCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onPostpone = {},
                        onEdit = { onEditTask(task) },
                        onBindPomodoro = { onBindPomodoroTask(task) },
                        onNotify = { onNotifyTask(task) },
                        onToggleSubtask = { onToggleSubtask(task, it) },
                        onMoveUp = {},
                        onMoveDown = {},
                        canMoveUp = false,
                        canMoveDown = false,
                    )
                    if (index != archivedTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("清单统计", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tasks.groupBy { it.listName.ifBlank { "未分组" } }.forEach { (name, grouped) ->
                MetricCard(name, "${grouped.count { !it.done }}/${grouped.size}", if (grouped.any { !it.done }) ForestGreen else MossGreen)
            }
        }
    }
}
