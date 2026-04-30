package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.poxiao.app.todo.TodoTask

@Composable
internal fun TodoDraftRestoreEffect(
    editingTaskId: String?,
    editingTask: TodoTask?,
    draftPrefs: SharedPreferences,
    editorState: TodoEditorState,
    draftRestored: Boolean,
    onTodoHintChange: (String) -> Unit,
    onDraftRestoredChange: (Boolean) -> Unit,
    onDraftReadyChange: (Boolean) -> Unit,
) {
    LaunchedEffect(editingTaskId) {
        val persistedDraft = if (!draftRestored) loadTodoDraft(draftPrefs) else null
        if (persistedDraft != null && persistedDraft.editingTaskId == editingTaskId) {
            editorState.restoreFromDraft(persistedDraft)
            if (persistedDraft.hasVisibleContent()) {
                onTodoHintChange("已恢复上次未提交的待办草稿。")
            }
            onDraftRestoredChange(true)
        } else if (editingTask != null) {
            editorState.restoreFromTask(editingTask)
            onTodoHintChange("正在编辑 ${editingTask.title}")
        } else if (!draftRestored) {
            val freshDraft = loadTodoDraft(draftPrefs)
            if (freshDraft != null && freshDraft.editingTaskId.isNullOrBlank()) {
                editorState.restoreFromDraft(freshDraft)
                if (freshDraft.hasVisibleContent()) {
                    onTodoHintChange("已恢复上次未提交的待办草稿。")
                }
                onDraftRestoredChange(true)
            } else {
                editorState.reset()
            }
        }
        onDraftReadyChange(true)
    }
}

@Composable
internal fun TodoDraftPersistenceEffect(
    editingTaskId: String?,
    editorState: TodoEditorState,
    draftReady: Boolean,
    draftPrefs: SharedPreferences,
) {
    LaunchedEffect(
        editingTaskId,
        editorState.title,
        editorState.note,
        editorState.duePreset,
        editorState.dueClock,
        editorState.listName,
        editorState.reminderPreset,
        editorState.repeatText,
        editorState.quadrant,
        editorState.priority,
        editorState.focusGoal,
        editorState.tagDraft,
        editorState.draftTags.joinToString("|"),
        editorState.subtaskDraft,
        editorState.draftSubtasks.joinToString("|") { "${it.title}:${it.done}" },
        draftReady,
    ) {
        if (!draftReady) return@LaunchedEffect
        if (!editorState.hasDraftContent()) {
            clearTodoDraft(draftPrefs)
        } else {
            saveTodoDraft(draftPrefs, editorState.toDraft(editingTaskId))
        }
    }
}

@Composable
internal fun TodoReminderRefreshEffect(
    context: Context,
    tasks: List<TodoTask>,
) {
    LaunchedEffect(tasks.buildReminderRefreshSignature()) {
        refreshLocalReminderSchedule(context)
    }
}

private fun TodoDraft.hasVisibleContent(): Boolean {
    return title.isNotBlank() ||
        note.isNotBlank() ||
        tags.isNotEmpty() ||
        subtasks.isNotEmpty()
}

private fun List<TodoTask>.buildReminderRefreshSignature(): String {
    return joinToString("|") { task ->
        "${task.id}:${task.title}:${task.dueText}:${task.reminderText}:${task.done}:${task.focusGoal}:${task.focusCount}"
    }
}
