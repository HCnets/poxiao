package com.poxiao.app.ui

import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask

internal enum class TodoDuePreset(val title: String) {
    Today("今天"),
    Tonight("今晚"),
    Tomorrow("明天"),
    ThisWeek("本周内"),
    NextWeek("下周"),
}

internal enum class TodoReminderPreset(val title: String) {
    None("不提醒"),
    Before10Min("提前 10 分钟"),
    Before30Min("提前 30 分钟"),
    Before1Hour("提前 1 小时"),
    PreviousNight("前一天晚 20:00"),
}

internal data class TodoDraft(
    val editingTaskId: String?,
    val title: String,
    val note: String,
    val duePreset: TodoDuePreset,
    val dueClock: String,
    val listName: String,
    val reminderPreset: TodoReminderPreset,
    val repeatText: String,
    val quadrant: TodoQuadrant,
    val priority: TodoPriority,
    val focusGoal: Int,
    val tagDraft: String,
    val tags: List<String>,
    val subtaskDraft: String,
    val subtasks: List<TodoSubtask>,
)
