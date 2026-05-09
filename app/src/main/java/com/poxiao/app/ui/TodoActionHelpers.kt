package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoTask

internal fun submitTodoEditor(
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    draftPrefs: SharedPreferences,
    editorState: TodoEditorState,
    editingTask: TodoTask?,
): String {
    val updatedTask = editorState.buildTask(editingTask)
    if (editingTask == null) {
        tasks.add(0, updatedTask)
    } else {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        if (index >= 0) tasks[index] = updatedTask
    }
    saveTodoTasks(prefs, tasks)
    clearTodoDraft(draftPrefs)
    return if (editingTask == null) {
        "已加入待办：${updatedTask.title}"
    } else {
        "已更新任务：${updatedTask.title}"
    }
}

internal fun deleteTodoTask(
    context: Context,
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    draftPrefs: SharedPreferences,
    task: TodoTask,
): String {
    if (task.id.startsWith("assistant-review-")) {
        val reviewItemId = task.id.removePrefix("assistant-review-")
        clearReviewExecutionLink(context, reviewItemId)
    }
    tasks.removeAll { it.id == task.id }
    saveTodoTasks(prefs, tasks)
    clearTodoDraft(draftPrefs)
    return "已删除任务：${task.title}"
}

internal fun clearCompletedTodoTasks(
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
): String {
    val before = tasks.size
    tasks.removeAll { it.done }
    saveTodoTasks(prefs, tasks)
    return "已清空 ${before - tasks.size} 条已完成任务。"
}

internal fun toggleTodoTaskAction(
    context: Context,
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    task: TodoTask,
): String? {
    val taskIndex = tasks.indexOfFirst { it.id == task.id }
    if (taskIndex < 0) return null
    val oldDone = tasks[taskIndex].done
    val hint = toggleTodoTask(tasks, taskIndex)
    val newDone = tasks[taskIndex].done

    var finalHint = hint
    if (!oldDone && newDone) {
        val linkageHint = handleReviewTaskCompletion(context, task.id)
        if (linkageHint != null) {
            finalHint = (hint ?: "") + " · " + linkageHint
        }
    }

    saveTodoTasks(prefs, tasks)
    return finalHint
}

internal fun toggleTodoSubtask(
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    task: TodoTask,
    subtaskIndex: Int,
) {
    val taskIndex = tasks.indexOfFirst { it.id == task.id }
    if (taskIndex < 0 || subtaskIndex !in tasks[taskIndex].subtasks.indices) return
    val subtasks = tasks[taskIndex].subtasks.toMutableList()
    subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
    tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
    saveTodoTasks(prefs, tasks)
}

internal fun postponeTodoTaskAction(
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    task: TodoTask,
): String? {
    val taskIndex = tasks.indexOfFirst { it.id == task.id }
    if (taskIndex < 0) return null
    val hint = postponeTodoTask(tasks, taskIndex)
    saveTodoTasks(prefs, tasks)
    return hint
}

internal fun moveTodoTask(
    tasks: MutableList<TodoTask>,
    prefs: SharedPreferences,
    task: TodoTask,
    offset: Int,
) {
    val taskIndex = tasks.indexOfFirst { it.id == task.id }
    val targetIndex = taskIndex + offset
    if (taskIndex !in tasks.indices || targetIndex !in tasks.indices) return
    tasks.swap(taskIndex, targetIndex)
    saveTodoTasks(prefs, tasks)
}

internal fun bindTodoTaskToPomodoro(
    focusPrefs: SharedPreferences,
    task: TodoTask,
): String {
    focusPrefs.edit()
        .remove("bound_task_title")
        .remove("bound_task_list")
        .apply()
    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
    return "已绑定到番茄钟：${task.title}"
}

internal fun notifyTodoTask(
    context: Context,
    task: TodoTask,
): String {
    sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
    return "已发送提醒：${task.title}"
}

internal fun canMoveTodoTask(
    tasks: List<TodoTask>,
    task: TodoTask,
    offset: Int,
): Boolean {
    val taskIndex = tasks.indexOfFirst { it.id == task.id }
    val targetIndex = taskIndex + offset
    return taskIndex >= 0 && targetIndex in tasks.indices
}
