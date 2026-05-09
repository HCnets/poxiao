package com.poxiao.app.ui

import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoTask

internal data class TodoScreenSnapshot(
    val finalVisibleTasks: List<TodoTask>,
    val groupedTasks: Map<String, List<TodoTask>>,
    val archivedTasks: List<TodoTask>,
    val completedCount: Int,
    val focusCount: Int,
    val quadrantCounts: Map<TodoQuadrant, Int>,
    val listOptions: List<String>,
    val allTags: List<String>,
)

internal fun buildTodoScreenSnapshot(
    tasks: List<TodoTask>,
    filter: TodoFilter,
    listFilter: String,
    searchQuery: String,
    selectedTags: List<String>,
    focusGoalFilter: TodoFocusGoalFilter,
): TodoScreenSnapshot {
    val visibleTasks = when (filter) {
        TodoFilter.All -> tasks
        TodoFilter.Focus -> tasks.filter { !it.done && it.quadrant != TodoQuadrant.Neither }
        TodoFilter.Today -> tasks.filter { it.dueText.contains("今天") || it.dueText.contains("今晚") }
        TodoFilter.Done -> tasks.filter { it.done }
    }
    val finalVisibleTasks = visibleTasks.filter { task ->
        (listFilter == "全部清单" || task.listName == listFilter) &&
            (searchQuery.isBlank() ||
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.note.contains(searchQuery, ignoreCase = true)) &&
            (selectedTags.isEmpty() || selectedTags.all { it in task.tags }) &&
            when (focusGoalFilter) {
                TodoFocusGoalFilter.All -> true
                TodoFocusGoalFilter.WithGoal -> task.focusGoal > 0
                TodoFocusGoalFilter.Pending -> task.focusGoal > 0 && task.focusCount < task.focusGoal
                TodoFocusGoalFilter.Reached -> task.focusGoal > 0 && task.focusCount >= task.focusGoal
            }
    }
    return TodoScreenSnapshot(
        finalVisibleTasks = finalVisibleTasks,
        groupedTasks = finalVisibleTasks.groupBy { it.listName.ifBlank { "未分组" } },
        archivedTasks = tasks.filter { it.done },
        completedCount = tasks.count { it.done },
        focusCount = tasks.sumOf { it.focusCount },
        quadrantCounts = TodoQuadrant.entries.associateWith { quadrant ->
            tasks.count { !it.done && it.quadrant == quadrant }
        },
        listOptions = listOf("全部清单") + tasks.map { it.listName }.filter { it.isNotBlank() }.distinct(),
        allTags = tasks.flatMap { it.tags }.distinct(),
    )
}
