package com.poxiao.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask

@Composable
internal fun TodoScreen(initialFilter: TodoFilter = TodoFilter.All) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("todo_board", android.content.Context.MODE_PRIVATE) }
    val draftPrefs = remember { context.getSharedPreferences("todo_draft", android.content.Context.MODE_PRIVATE) }
    val tasks = remember { mutableStateListOf<TodoTask>().apply { addAll(loadTodoTasks(prefs)) } }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("今天 21:00") }
    var duePreset by remember { mutableStateOf(TodoDuePreset.Today) }
    var dueClock by remember { mutableStateOf("21:00") }
    var listName by remember { mutableStateOf("收集箱") }
    var reminder by remember { mutableStateOf("提前 30 分钟") }
    var reminderPreset by remember { mutableStateOf(TodoReminderPreset.Before30Min) }
    var repeatText by remember { mutableStateOf("不重复") }
    var quadrant by remember { mutableStateOf(TodoQuadrant.ImportantNotUrgent) }
    var priority by remember { mutableStateOf(TodoPriority.Medium) }
    var focusGoal by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var tagDraft by remember { mutableStateOf("") }
    val draftTags = remember { mutableStateListOf<String>() }
    val selectedTags = remember { mutableStateListOf<String>() }
    var subtaskDraft by remember { mutableStateOf("") }
    val draftSubtasks = remember { mutableStateListOf<TodoSubtask>() }
    val restoredTodoDraft = remember { loadTodoDraft(draftPrefs) }
    var editingTaskId by remember { mutableStateOf(restoredTodoDraft?.editingTaskId) }
    var todoHint by remember { mutableStateOf("") }
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", android.content.Context.MODE_PRIVATE) }
    var viewMode by remember { mutableStateOf(TodoViewMode.Flat) }
    val editingTask = tasks.firstOrNull { it.id == editingTaskId }
    val quadrantCounts = TodoQuadrant.entries.associateWith { item -> tasks.count { !it.done && it.quadrant == item } }
    val listOptions = remember(tasks.toList()) {
        listOf("全部清单") + tasks.map { it.listName }.filter { it.isNotBlank() }.distinct()
    }
    val allTags = remember(tasks.toList()) { tasks.flatMap { it.tags }.distinct() }
    var listFilter by remember { mutableStateOf("全部清单") }
    var focusGoalFilter by remember { mutableStateOf(TodoFocusGoalFilter.All) }
    var draftReady by remember { mutableStateOf(false) }
    var draftRestored by remember { mutableStateOf(false) }

    val visibleTasks = when (filter) {
        TodoFilter.All -> tasks.toList()
        TodoFilter.Focus -> tasks.filter { !it.done && it.quadrant != TodoQuadrant.Neither }
        TodoFilter.Today -> tasks.filter { it.dueText.contains("今天") || it.dueText.contains("今晚") }
        TodoFilter.Done -> tasks.filter { it.done }
    }
    val finalVisibleTasks = visibleTasks.filter { task ->
        (listFilter == "全部清单" || task.listName == listFilter) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.note.contains(searchQuery, ignoreCase = true)) &&
            (selectedTags.isEmpty() || selectedTags.all { it in task.tags }) &&
            when (focusGoalFilter) {
                TodoFocusGoalFilter.All -> true
                TodoFocusGoalFilter.WithGoal -> task.focusGoal > 0
                TodoFocusGoalFilter.Pending -> task.focusGoal > 0 && task.focusCount < task.focusGoal
                TodoFocusGoalFilter.Reached -> task.focusGoal > 0 && task.focusCount >= task.focusGoal
            }
    }
    val groupedTasks = finalVisibleTasks.groupBy { it.listName.ifBlank { "未分组" } }
    val completedCount = tasks.count { it.done }
    val focusCount = tasks.map { it.focusCount }.sum()
    val archivedTasks = tasks.filter { it.done }
    var archiveExpanded by remember { mutableStateOf(false) }
    val resetEditorForm = {
        title = ""
        note = ""
        due = "今天 21:00"
        duePreset = TodoDuePreset.Today
        dueClock = "21:00"
        listName = "收集箱"
        reminder = "提前 30 分钟"
        reminderPreset = TodoReminderPreset.Before30Min
        repeatText = "不重复"
        quadrant = TodoQuadrant.ImportantNotUrgent
        priority = TodoPriority.Medium
        focusGoal = 0
        tagDraft = ""
        draftTags.clear()
        subtaskDraft = ""
        draftSubtasks.clear()
    }
    LaunchedEffect(editingTaskId) {
        val persistedDraft = if (!draftRestored) loadTodoDraft(draftPrefs) else null
        if (persistedDraft != null && persistedDraft.editingTaskId == editingTaskId) {
            title = persistedDraft.title
            note = persistedDraft.note
            duePreset = persistedDraft.duePreset
            dueClock = persistedDraft.dueClock
            due = buildTodoDueText(duePreset, dueClock)
            listName = persistedDraft.listName
            reminderPreset = persistedDraft.reminderPreset
            reminder = reminderPreset.title
            repeatText = persistedDraft.repeatText
            quadrant = persistedDraft.quadrant
            priority = persistedDraft.priority
            focusGoal = persistedDraft.focusGoal
            tagDraft = persistedDraft.tagDraft
            draftTags.clear()
            draftTags.addAll(persistedDraft.tags)
            subtaskDraft = persistedDraft.subtaskDraft
            draftSubtasks.clear()
            draftSubtasks.addAll(persistedDraft.subtasks)
            if (
                persistedDraft.title.isNotBlank() ||
                persistedDraft.note.isNotBlank() ||
                persistedDraft.tags.isNotEmpty() ||
                persistedDraft.subtasks.isNotEmpty()
            ) {
                todoHint = "已恢复上次未提交的待办草稿。"
            }
            draftRestored = true
        } else if (editingTask != null) {
            title = editingTask.title
            note = editingTask.note
            due = editingTask.dueText
            duePreset = inferTodoDuePreset(editingTask.dueText)
            dueClock = extractTodoDueClock(editingTask.dueText)
            listName = editingTask.listName
            reminder = editingTask.reminderText.ifBlank { "不提醒" }
            reminderPreset = inferTodoReminderPreset(editingTask.reminderText)
            repeatText = editingTask.repeatText
            quadrant = editingTask.quadrant
            priority = editingTask.priority
            focusGoal = editingTask.focusGoal
            draftTags.clear()
            draftTags.addAll(editingTask.tags)
            draftSubtasks.clear()
            draftSubtasks.addAll(editingTask.subtasks)
            todoHint = "正在编辑 ${editingTask.title}"
        } else if (!draftRestored) {
            val freshDraft = loadTodoDraft(draftPrefs)
            if (freshDraft != null && freshDraft.editingTaskId.isNullOrBlank()) {
                title = freshDraft.title
                note = freshDraft.note
                duePreset = freshDraft.duePreset
                dueClock = freshDraft.dueClock
                due = buildTodoDueText(duePreset, dueClock)
                listName = freshDraft.listName
                reminderPreset = freshDraft.reminderPreset
                reminder = reminderPreset.title
                repeatText = freshDraft.repeatText
                quadrant = freshDraft.quadrant
                priority = freshDraft.priority
                focusGoal = freshDraft.focusGoal
                tagDraft = freshDraft.tagDraft
                draftTags.clear()
                draftTags.addAll(freshDraft.tags)
                subtaskDraft = freshDraft.subtaskDraft
                draftSubtasks.clear()
                draftSubtasks.addAll(freshDraft.subtasks)
                if (freshDraft.title.isNotBlank() || freshDraft.note.isNotBlank() || freshDraft.tags.isNotEmpty() || freshDraft.subtasks.isNotEmpty()) {
                    todoHint = "已恢复上次未提交的待办草稿。"
                }
                draftRestored = true
            } else {
                resetEditorForm()
            }
        }
        draftReady = true
    }

    LaunchedEffect(
        editingTaskId,
        title,
        note,
        duePreset,
        dueClock,
        listName,
        reminderPreset,
        repeatText,
        quadrant,
        priority,
        focusGoal,
        tagDraft,
        draftTags.joinToString("|"),
        subtaskDraft,
        draftSubtasks.joinToString("|") { "${it.title}:${it.done}" },
        draftReady,
    ) {
        if (!draftReady) return@LaunchedEffect
        val hasDraftContent =
            title.isNotBlank() ||
                note.isNotBlank() ||
                listName != "收集箱" ||
                repeatText != "不重复" ||
                focusGoal > 0 ||
                duePreset != TodoDuePreset.Today ||
                dueClock != "21:00" ||
                reminderPreset != TodoReminderPreset.Before30Min ||
                quadrant != TodoQuadrant.ImportantNotUrgent ||
                priority != TodoPriority.Medium ||
                tagDraft.isNotBlank() ||
                draftTags.isNotEmpty() ||
                subtaskDraft.isNotBlank() ||
                draftSubtasks.isNotEmpty()
        if (!hasDraftContent) {
            clearTodoDraft(draftPrefs)
        } else {
            saveTodoDraft(
                draftPrefs,
                TodoDraft(
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
                ),
            )
        }
    }

    LaunchedEffect(
        tasks.joinToString("|") {
            "${it.id}:${it.title}:${it.dueText}:${it.reminderText}:${it.done}:${it.focusGoal}:${it.focusCount}"
        },
    ) {
        refreshLocalReminderSchedule(context)
    }

    CompositionLocalProvider(LocalStaticGlassMode provides true) {
        ScreenColumn {
            item {
                TodoOverviewCard(
                    tasks = tasks,
                    completedCount = completedCount,
                    focusCount = focusCount,
                    quadrantCounts = quadrantCounts,
                )
            }
            item {
                TodoEditorCard(
                    editingTask = editingTask,
                    title = title,
                    onTitleChange = { title = it },
                    note = note,
                    onNoteChange = { note = it },
                    duePreset = duePreset,
                    onDuePresetChange = {
                        duePreset = it
                        due = buildTodoDueText(it, dueClock)
                    },
                    dueClock = dueClock,
                    onDueClockChange = {
                        dueClock = it
                        due = buildTodoDueText(duePreset, dueClock)
                    },
                    listName = listName,
                    onListNameChange = { listName = it },
                    reminderPreset = reminderPreset,
                    onReminderPresetChange = {
                        reminderPreset = it
                        reminder = it.title
                    },
                    repeatText = repeatText,
                    onRepeatTextChange = { repeatText = it },
                    quadrant = quadrant,
                    onQuadrantChange = { quadrant = it },
                    priority = priority,
                    onPriorityChange = { priority = it },
                    focusGoal = focusGoal,
                    onFocusGoalChange = { focusGoal = it },
                    tagDraft = tagDraft,
                    onTagDraftChange = { tagDraft = it },
                    draftTags = draftTags,
                    onAddTag = {
                        val normalized = tagDraft.trim()
                        if (normalized.isNotBlank() && normalized !in draftTags) {
                            draftTags.add(normalized)
                            tagDraft = ""
                        }
                    },
                    onRemoveTag = { draftTags.removeAt(it) },
                    subtaskDraft = subtaskDraft,
                    onSubtaskDraftChange = { subtaskDraft = it },
                    draftSubtasks = draftSubtasks,
                    onAddSubtask = {
                        if (subtaskDraft.isNotBlank()) {
                            draftSubtasks.add(TodoSubtask(subtaskDraft))
                            subtaskDraft = ""
                        }
                    },
                    onToggleDraftSubtask = { index ->
                        draftSubtasks[index] = draftSubtasks[index].copy(done = !draftSubtasks[index].done)
                    },
                    onRemoveDraftSubtask = { draftSubtasks.removeAt(it) },
                    onSubmit = {
                        if (title.isNotBlank()) {
                            val updatedTask = TodoTask(
                                id = editingTask?.id ?: "todo-${System.currentTimeMillis()}",
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
                                focusCount = editingTask?.focusCount ?: 0,
                                focusGoal = focusGoal,
                                done = editingTask?.done ?: false,
                            )
                            if (editingTask == null) {
                                tasks.add(0, updatedTask)
                                todoHint = "已加入待办：${updatedTask.title}"
                            } else {
                                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                                if (index >= 0) tasks[index] = updatedTask
                                todoHint = "已更新任务：${updatedTask.title}"
                            }
                            saveTodoTasks(prefs, tasks)
                            clearTodoDraft(draftPrefs)
                            editingTaskId = null
                            draftRestored = false
                            resetEditorForm()
                        } else {
                            todoHint = "请先填写任务标题"
                        }
                    },
                    onDelete = {
                        if (editingTask != null) {
                            tasks.removeAll { it.id == editingTask.id }
                            saveTodoTasks(prefs, tasks)
                            clearTodoDraft(draftPrefs)
                            todoHint = "已删除任务：${editingTask.title}"
                            editingTaskId = null
                            draftRestored = false
                            resetEditorForm()
                        }
                    },
                    todoHint = todoHint,
                )
            }
            item {
                TodoFilterPanel(
                    filter = filter,
                    onFilterChange = { filter = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    allTags = allTags,
                    selectedTags = selectedTags,
                    listOptions = listOptions,
                    listFilter = listFilter,
                    onListFilterChange = { listFilter = it },
                    focusGoalFilter = focusGoalFilter,
                    onFocusGoalFilterChange = { focusGoalFilter = it },
                )
                TodoListSections(
                    viewMode = viewMode,
                    tasks = tasks,
                    finalVisibleTasks = finalVisibleTasks,
                    groupedTasks = groupedTasks,
                    archivedTasks = archivedTasks,
                    archiveExpanded = archiveExpanded,
                    onToggleArchiveExpanded = { archiveExpanded = !archiveExpanded },
                    onClearCompleted = {
                        val before = tasks.size
                        tasks.removeAll { it.done }
                        saveTodoTasks(prefs, tasks)
                        archiveExpanded = false
                        todoHint = "已清空 ${before - tasks.size} 条已完成任务。"
                    },
                    onToggleTask = { task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        if (taskIndex >= 0) {
                            todoHint = toggleTodoTask(tasks, taskIndex)
                            saveTodoTasks(prefs, tasks)
                        }
                    },
                    onPostponeTask = { task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        if (taskIndex >= 0) {
                            todoHint = postponeTodoTask(tasks, taskIndex)
                            saveTodoTasks(prefs, tasks)
                        }
                    },
                    onEditTask = { task ->
                        editingTaskId = task.id
                        todoHint = "正在编辑 ${task.title}"
                    },
                    onBindPomodoroTask = { task ->
                        focusPrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                        todoHint = "已绑定到番茄钟：${task.title}"
                    },
                    onNotifyTask = { task ->
                        sendAppNotification(context, "待办提醒", "${task.title} · ${task.dueText}")
                        todoHint = "已发送提醒：${task.title}"
                    },
                    onToggleSubtask = { task, subtaskIndex ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                            val subtasks = tasks[taskIndex].subtasks.toMutableList()
                            subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                            tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                            saveTodoTasks(prefs, tasks)
                        }
                    },
                    onMoveUpTask = { task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        if (taskIndex > 0) {
                            tasks.swap(taskIndex, taskIndex - 1)
                            saveTodoTasks(prefs, tasks)
                        }
                    },
                    onMoveDownTask = { task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        if (taskIndex >= 0 && taskIndex < tasks.lastIndex) {
                            tasks.swap(taskIndex, taskIndex + 1)
                            saveTodoTasks(prefs, tasks)
                        }
                    },
                    canMoveUp = { task ->
                        tasks.indexOfFirst { it.id == task.id } > 0
                    },
                    canMoveDown = { task ->
                        tasks.indexOfFirst { it.id == task.id } in 0 until tasks.lastIndex
                    },
                )
            }
        }
    }
}
