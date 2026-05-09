package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask

private const val DefaultTodoDueClock = "21:00"
private const val DefaultTodoListName = "收集箱"
private const val DefaultTodoRepeatText = "不重复"

@Stable
internal class TodoEditorState {
    var title by mutableStateOf("")
    var note by mutableStateOf("")
    var duePreset by mutableStateOf(TodoDuePreset.Today)
    var dueClock by mutableStateOf(DefaultTodoDueClock)
    var listName by mutableStateOf(DefaultTodoListName)
    var reminderPreset by mutableStateOf(TodoReminderPreset.Before30Min)
    var repeatText by mutableStateOf(DefaultTodoRepeatText)
    var quadrant by mutableStateOf(TodoQuadrant.ImportantNotUrgent)
    var priority by mutableStateOf(TodoPriority.Medium)
    var focusGoal by mutableIntStateOf(0)
    var tagDraft by mutableStateOf("")
    val draftTags = mutableStateListOf<String>()
    var subtaskDraft by mutableStateOf("")
    val draftSubtasks = mutableStateListOf<TodoSubtask>()

    fun reset() {
        title = ""
        note = ""
        duePreset = TodoDuePreset.Today
        dueClock = DefaultTodoDueClock
        listName = DefaultTodoListName
        reminderPreset = TodoReminderPreset.Before30Min
        repeatText = DefaultTodoRepeatText
        quadrant = TodoQuadrant.ImportantNotUrgent
        priority = TodoPriority.Medium
        focusGoal = 0
        tagDraft = ""
        draftTags.clear()
        subtaskDraft = ""
        draftSubtasks.clear()
    }

    fun restoreFromDraft(draft: TodoDraft) {
        title = draft.title
        note = draft.note
        duePreset = draft.duePreset
        dueClock = draft.dueClock
        listName = draft.listName
        reminderPreset = draft.reminderPreset
        repeatText = draft.repeatText
        quadrant = draft.quadrant
        priority = draft.priority
        focusGoal = draft.focusGoal
        tagDraft = draft.tagDraft
        draftTags.clear()
        draftTags.addAll(draft.tags)
        subtaskDraft = draft.subtaskDraft
        draftSubtasks.clear()
        draftSubtasks.addAll(draft.subtasks)
    }

    fun restoreFromTask(task: TodoTask) {
        title = task.title
        note = task.note
        duePreset = inferTodoDuePreset(task.dueText)
        dueClock = extractTodoDueClock(task.dueText)
        listName = task.listName
        reminderPreset = inferTodoReminderPreset(task.reminderText)
        repeatText = task.repeatText
        quadrant = task.quadrant
        priority = task.priority
        focusGoal = task.focusGoal
        tagDraft = ""
        draftTags.clear()
        draftTags.addAll(task.tags)
        subtaskDraft = ""
        draftSubtasks.clear()
        draftSubtasks.addAll(task.subtasks)
    }

    fun hasDraftContent(): Boolean {
        return title.isNotBlank() ||
            note.isNotBlank() ||
            listName != DefaultTodoListName ||
            repeatText != DefaultTodoRepeatText ||
            focusGoal > 0 ||
            duePreset != TodoDuePreset.Today ||
            dueClock != DefaultTodoDueClock ||
            reminderPreset != TodoReminderPreset.Before30Min ||
            quadrant != TodoQuadrant.ImportantNotUrgent ||
            priority != TodoPriority.Medium ||
            tagDraft.isNotBlank() ||
            draftTags.isNotEmpty() ||
            subtaskDraft.isNotBlank() ||
            draftSubtasks.isNotEmpty()
    }

    fun toDraft(editingTaskId: String?): TodoDraft {
        return TodoDraft(
            editingTaskId = editingTaskId,
            title = title,
            note = note,
            duePreset = duePreset,
            dueClock = dueClock,
            listName = listName,
            reminderPreset = reminderPreset,
            repeatText = repeatText,
            quadrant = quadrant,
            priority = priority,
            focusGoal = focusGoal,
            tagDraft = tagDraft,
            tags = draftTags.toList(),
            subtaskDraft = subtaskDraft,
            subtasks = draftSubtasks.toList(),
        )
    }

    fun buildTask(existingTask: TodoTask?): TodoTask {
        return TodoTask(
            id = existingTask?.id ?: "todo-${System.currentTimeMillis()}",
            title = title,
            note = note,
            quadrant = quadrant,
            priority = priority,
            dueText = buildTodoDueText(duePreset, dueClock),
            tags = draftTags.toList(),
            listName = listName,
            reminderText = reminderPreset.title,
            repeatText = repeatText,
            subtasks = draftSubtasks.toList(),
            focusCount = existingTask?.focusCount ?: 0,
            focusGoal = focusGoal,
            done = existingTask?.done ?: false,
        )
    }

    fun addTag() {
        val normalized = tagDraft.trim()
        if (normalized.isNotBlank() && normalized !in draftTags) {
            draftTags.add(normalized)
            tagDraft = ""
        }
    }

    fun removeTag(index: Int) {
        if (index in draftTags.indices) {
            draftTags.removeAt(index)
        }
    }

    fun addSubtask() {
        val normalized = subtaskDraft.trim()
        if (normalized.isNotBlank()) {
            draftSubtasks.add(TodoSubtask(normalized))
            subtaskDraft = ""
        }
    }

    fun toggleDraftSubtask(index: Int) {
        if (index in draftSubtasks.indices) {
            draftSubtasks[index] = draftSubtasks[index].copy(done = !draftSubtasks[index].done)
        }
    }

    fun removeDraftSubtask(index: Int) {
        if (index in draftSubtasks.indices) {
            draftSubtasks.removeAt(index)
        }
    }
}

@Composable
internal fun rememberTodoEditorState(): TodoEditorState {
    return remember { TodoEditorState() }
}
