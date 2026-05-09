package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist

@Composable
internal fun TodoEditorCard(
    editingTask: TodoTask?,
    title: String,
    onTitleChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    duePreset: TodoDuePreset,
    onDuePresetChange: (TodoDuePreset) -> Unit,
    dueClock: String,
    onDueClockChange: (String) -> Unit,
    listName: String,
    onListNameChange: (String) -> Unit,
    reminderPreset: TodoReminderPreset,
    onReminderPresetChange: (TodoReminderPreset) -> Unit,
    repeatText: String,
    onRepeatTextChange: (String) -> Unit,
    quadrant: TodoQuadrant,
    onQuadrantChange: (TodoQuadrant) -> Unit,
    priority: TodoPriority,
    onPriorityChange: (TodoPriority) -> Unit,
    focusGoal: Int,
    onFocusGoalChange: (Int) -> Unit,
    tagDraft: String,
    onTagDraftChange: (String) -> Unit,
    draftTags: SnapshotStateList<String>,
    onAddTag: () -> Unit,
    onRemoveTag: (Int) -> Unit,
    subtaskDraft: String,
    onSubtaskDraftChange: (String) -> Unit,
    draftSubtasks: SnapshotStateList<TodoSubtask>,
    onAddSubtask: () -> Unit,
    onToggleDraftSubtask: (Int) -> Unit,
    onRemoveDraftSubtask: (Int) -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit,
    todoHint: String,
) {
    GlassCard {
        Text(if (editingTask == null) "新建任务" else "编辑任务", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = title, onValueChange = onTitleChange, label = { Text("任务标题") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = note, onValueChange = onNoteChange, label = { Text("补充说明") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = TodoDuePreset.entries.toList(), selected = duePreset, label = { it.title }, onSelect = onDuePresetChange)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = dueClock,
            onValueChange = onDueClockChange,
            label = { Text("时间") },
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = listName, onValueChange = onListNameChange, label = { Text("所属清单") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = TodoReminderPreset.entries.toList(), selected = reminderPreset, label = { it.title }, onSelect = onReminderPresetChange)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = repeatText, onValueChange = onRepeatTextChange, label = { Text("重复") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = TodoQuadrant.entries.toList(), selected = quadrant, label = { it.title }, onSelect = onQuadrantChange)
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = TodoPriority.entries.toList(), selected = priority, label = { it.title }, onSelect = onPriorityChange)
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(
            options = TodoFocusGoalOptions,
            selected = focusGoal,
            label = { if (it == 0) "无专注目标" else "目标 ${it} 轮" },
            onSelect = onFocusGoalChange,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = tagDraft,
                onValueChange = onTagDraftChange,
                label = { Text("标签") },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(0.74f),
            )
            ActionPill("加入", Ginkgo, onClick = onAddTag)
        }
        if (draftTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                draftTags.forEachIndexed { index, tag ->
                    ActionPill(tag, WarmMist) { onRemoveTag(index) }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = subtaskDraft,
                onValueChange = onSubtaskDraftChange,
                label = { Text("子任务") },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(0.74f),
            )
            ActionPill(text = "加入", background = ForestGreen, onClick = onAddSubtask)
        }
        if (draftSubtasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                draftSubtasks.forEachIndexed { index, subtask ->
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.4f)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(subtask.title, style = MaterialTheme.typography.bodyMedium, color = PineInk, modifier = Modifier.fillMaxWidth(0.5f))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ActionPill(
                                    text = if (subtask.done) "已完成" else "待完成",
                                    background = if (subtask.done) MossGreen else TeaGreen,
                                    onClick = { onToggleDraftSubtask(index) },
                                )
                                ActionPill(
                                    text = "删除",
                                    background = WarmMist,
                                    onClick = { onRemoveDraftSubtask(index) },
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onSubmit,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            ) {
                Text(if (editingTask == null) "加入待办" else "保存修改")
            }
            if (editingTask != null) {
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text("删除任务")
                }
            }
        }
        if (todoHint.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(todoHint, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
        }
    }
}
