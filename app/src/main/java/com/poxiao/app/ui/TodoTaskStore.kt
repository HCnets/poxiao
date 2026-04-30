package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import org.json.JSONArray
import org.json.JSONObject

private const val TodoTasksKey = "todo_tasks"

internal fun loadTodoTasks(prefs: SharedPreferences): List<TodoTask> {
    val raw = prefs.getString(TodoTasksKey, null) ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    TodoTask(
                        id = item.optString("id").ifBlank { "todo-$index" },
                        title = item.optString("title").ifBlank { "未命名任务" },
                        note = item.optString("note"),
                        quadrant = runCatching {
                            TodoQuadrant.valueOf(item.optString("quadrant", TodoQuadrant.ImportantNotUrgent.name))
                        }.getOrDefault(TodoQuadrant.ImportantNotUrgent),
                        priority = runCatching {
                            TodoPriority.valueOf(item.optString("priority", TodoPriority.Medium.name))
                        }.getOrDefault(TodoPriority.Medium),
                        dueText = item.optString("dueText"),
                        tags = buildList {
                            val tagsArray = item.optJSONArray("tags") ?: JSONArray()
                            for (tagIndex in 0 until tagsArray.length()) {
                                tagsArray.optString(tagIndex).takeIf(String::isNotBlank)?.let(::add)
                            }
                        },
                        listName = item.optString("listName", "收集箱"),
                        reminderText = item.optString("reminderText"),
                        repeatText = item.optString("repeatText", "不重复"),
                        subtasks = buildList {
                            val subtasksArray = item.optJSONArray("subtasks") ?: JSONArray()
                            for (subtaskIndex in 0 until subtasksArray.length()) {
                                val subtaskItem = subtasksArray.optJSONObject(subtaskIndex) ?: continue
                                val title = subtaskItem.optString("title").trim()
                                if (title.isBlank()) continue
                                add(
                                    TodoSubtask(
                                        title = title,
                                        done = subtaskItem.optBoolean("done"),
                                    ),
                                )
                            }
                        },
                        done = item.optBoolean("done"),
                        focusCount = item.optInt("focusCount"),
                        focusGoal = item.optInt("focusGoal"),
                    ),
                )
            }
        }
    }.getOrElse { emptyList() }
}

internal fun saveTodoTasks(
    prefs: SharedPreferences,
    tasks: List<TodoTask>,
) {
    val array = JSONArray()
    tasks.forEach { task ->
        array.put(
            JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("note", task.note)
                put("quadrant", task.quadrant.name)
                put("priority", task.priority.name)
                put("dueText", task.dueText)
                put("tags", JSONArray(task.tags))
                put("listName", task.listName)
                put("reminderText", task.reminderText)
                put("repeatText", task.repeatText)
                put(
                    "subtasks",
                    JSONArray().apply {
                        task.subtasks.forEach { subtask ->
                            put(
                                JSONObject().apply {
                                    put("title", subtask.title)
                                    put("done", subtask.done)
                                },
                            )
                        }
                    },
                )
                put("focusCount", task.focusCount)
                put("focusGoal", task.focusGoal)
                put("done", task.done)
            },
        )
    }
    prefs.edit().putString(TodoTasksKey, array.toString()).apply()
}
