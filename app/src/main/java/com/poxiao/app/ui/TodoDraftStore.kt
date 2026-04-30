package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import org.json.JSONArray
import org.json.JSONObject

private const val TodoDraftKey = "todo_draft_v1"

internal fun loadTodoDraft(prefs: SharedPreferences): TodoDraft? {
    val raw = prefs.getString(TodoDraftKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val item = JSONObject(raw)
        TodoDraft(
            editingTaskId = item.optString("editingTaskId").ifBlank { null },
            title = item.optString("title"),
            note = item.optString("note"),
            duePreset = runCatching {
                TodoDuePreset.valueOf(item.optString("duePreset", TodoDuePreset.Today.name))
            }.getOrDefault(TodoDuePreset.Today),
            dueClock = item.optString("dueClock", "21:00"),
            listName = item.optString("listName", "收集箱"),
            reminderPreset = runCatching {
                TodoReminderPreset.valueOf(
                    item.optString("reminderPreset", TodoReminderPreset.Before30Min.name),
                )
            }.getOrDefault(TodoReminderPreset.Before30Min),
            repeatText = item.optString("repeatText", "不重复"),
            quadrant = runCatching {
                TodoQuadrant.valueOf(item.optString("quadrant", TodoQuadrant.ImportantNotUrgent.name))
            }.getOrDefault(TodoQuadrant.ImportantNotUrgent),
            priority = runCatching {
                TodoPriority.valueOf(item.optString("priority", TodoPriority.Medium.name))
            }.getOrDefault(TodoPriority.Medium),
            focusGoal = item.optInt("focusGoal"),
            tagDraft = item.optString("tagDraft"),
            tags = buildList {
                val tagsArray = item.optJSONArray("tags") ?: JSONArray()
                for (index in 0 until tagsArray.length()) add(tagsArray.optString(index))
            },
            subtaskDraft = item.optString("subtaskDraft"),
            subtasks = buildList {
                val subtasksArray = item.optJSONArray("subtasks") ?: JSONArray()
                for (index in 0 until subtasksArray.length()) {
                    val subtask = subtasksArray.optJSONObject(index) ?: continue
                    add(TodoSubtask(subtask.optString("title"), subtask.optBoolean("done")))
                }
            },
        )
    }.getOrNull()
}

internal fun saveTodoDraft(
    prefs: SharedPreferences,
    draft: TodoDraft,
) {
    prefs.edit()
        .putString(
            TodoDraftKey,
            JSONObject().apply {
                put("editingTaskId", draft.editingTaskId)
                put("title", draft.title)
                put("note", draft.note)
                put("duePreset", draft.duePreset.name)
                put("dueClock", draft.dueClock)
                put("listName", draft.listName)
                put("reminderPreset", draft.reminderPreset.name)
                put("repeatText", draft.repeatText)
                put("quadrant", draft.quadrant.name)
                put("priority", draft.priority.name)
                put("focusGoal", draft.focusGoal)
                put("tagDraft", draft.tagDraft)
                put("tags", JSONArray(draft.tags))
                put("subtaskDraft", draft.subtaskDraft)
                put(
                    "subtasks",
                    JSONArray().apply {
                        draft.subtasks.forEach { subtask ->
                            put(
                                JSONObject().apply {
                                    put("title", subtask.title)
                                    put("done", subtask.done)
                                },
                            )
                        }
                    },
                )
            }.toString(),
        )
        .apply()
}

internal fun clearTodoDraft(prefs: SharedPreferences) {
    prefs.edit().remove(TodoDraftKey).apply()
}
