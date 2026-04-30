package com.poxiao.app.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.poxiao.app.todo.TodoTask

@Composable
internal fun TodoScreen(initialFilter: TodoFilter = TodoFilter.All) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("todo_board", Context.MODE_PRIVATE) }
    val draftPrefs = remember { context.getSharedPreferences("todo_draft", Context.MODE_PRIVATE) }
    val tasks = remember { mutableStateListOf<TodoTask>().apply { addAll(loadTodoTasks(prefs)) } }
    val editorState = rememberTodoEditorState()
    var searchQuery by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val restoredTodoDraft = remember { loadTodoDraft(draftPrefs) }
    var editingTaskId by remember { mutableStateOf(restoredTodoDraft?.editingTaskId) }
    var todoHint by remember { mutableStateOf("") }
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE) }
    var viewMode by remember { mutableStateOf(TodoViewMode.Flat) }
    val editingTask = tasks.firstOrNull { it.id == editingTaskId }
    var listFilter by remember { mutableStateOf("全部清单") }
    var focusGoalFilter by remember { mutableStateOf(TodoFocusGoalFilter.All) }
    var draftReady by remember { mutableStateOf(false) }
    var draftRestored by remember { mutableStateOf(false) }
    val snapshot = remember(tasks.toList(), filter, listFilter, searchQuery, selectedTags.toList(), focusGoalFilter) {
        buildTodoScreenSnapshot(
            tasks = tasks,
            filter = filter,
            listFilter = listFilter,
            searchQuery = searchQuery,
            selectedTags = selectedTags,
            focusGoalFilter = focusGoalFilter,
        )
    }
    var archiveExpanded by remember { mutableStateOf(false) }
    TodoDraftRestoreEffect(
        editingTaskId,
        editingTask = editingTask,
        draftPrefs = draftPrefs,
        editorState = editorState,
        draftRestored = draftRestored,
        onTodoHintChange = { todoHint = it },
        onDraftRestoredChange = { draftRestored = it },
        onDraftReadyChange = { draftReady = it },
    )
    TodoDraftPersistenceEffect(
        editingTaskId = editingTaskId,
        editorState = editorState,
        draftReady = draftReady,
        draftPrefs = draftPrefs,
    )
    TodoReminderRefreshEffect(
        context = context,
        tasks = tasks,
    )

    CompositionLocalProvider(LocalStaticGlassMode provides true) {
        ScreenColumn {
            item {
                TodoOverviewCard(
                    tasks = tasks,
                    completedCount = snapshot.completedCount,
                    focusCount = snapshot.focusCount,
                    quadrantCounts = snapshot.quadrantCounts,
                )
            }
            item {
                TodoEditorCard(
                    editingTask = editingTask,
                    title = editorState.title,
                    onTitleChange = { editorState.title = it },
                    note = editorState.note,
                    onNoteChange = { editorState.note = it },
                    duePreset = editorState.duePreset,
                    onDuePresetChange = {
                        editorState.duePreset = it
                    },
                    dueClock = editorState.dueClock,
                    onDueClockChange = {
                        editorState.dueClock = it
                    },
                    listName = editorState.listName,
                    onListNameChange = { editorState.listName = it },
                    reminderPreset = editorState.reminderPreset,
                    onReminderPresetChange = {
                        editorState.reminderPreset = it
                    },
                    repeatText = editorState.repeatText,
                    onRepeatTextChange = { editorState.repeatText = it },
                    quadrant = editorState.quadrant,
                    onQuadrantChange = { editorState.quadrant = it },
                    priority = editorState.priority,
                    onPriorityChange = { editorState.priority = it },
                    focusGoal = editorState.focusGoal,
                    onFocusGoalChange = { editorState.focusGoal = it },
                    tagDraft = editorState.tagDraft,
                    onTagDraftChange = { editorState.tagDraft = it },
                    draftTags = editorState.draftTags,
                    onAddTag = editorState::addTag,
                    onRemoveTag = editorState::removeTag,
                    subtaskDraft = editorState.subtaskDraft,
                    onSubtaskDraftChange = { editorState.subtaskDraft = it },
                    draftSubtasks = editorState.draftSubtasks,
                    onAddSubtask = editorState::addSubtask,
                    onToggleDraftSubtask = editorState::toggleDraftSubtask,
                    onRemoveDraftSubtask = editorState::removeDraftSubtask,
                    onSubmit = {
                        if (editorState.title.isNotBlank()) {
                            todoHint = submitTodoEditor(
                                tasks = tasks,
                                prefs = prefs,
                                draftPrefs = draftPrefs,
                                editorState = editorState,
                                editingTask = editingTask,
                            )
                            editingTaskId = null
                            draftRestored = false
                            editorState.reset()
                        } else {
                            todoHint = "请先填写任务标题"
                        }
                    },
                    onDelete = {
                        if (editingTask != null) {
                            todoHint = deleteTodoTask(
                                tasks = tasks,
                                prefs = prefs,
                                draftPrefs = draftPrefs,
                                task = editingTask,
                            )
                            editingTaskId = null
                            draftRestored = false
                            editorState.reset()
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
                    allTags = snapshot.allTags,
                    selectedTags = selectedTags,
                    listOptions = snapshot.listOptions,
                    listFilter = listFilter,
                    onListFilterChange = { listFilter = it },
                    focusGoalFilter = focusGoalFilter,
                    onFocusGoalFilterChange = { focusGoalFilter = it },
                )
                TodoListSections(
                    viewMode = viewMode,
                    tasks = tasks,
                    finalVisibleTasks = snapshot.finalVisibleTasks,
                    groupedTasks = snapshot.groupedTasks,
                    archivedTasks = snapshot.archivedTasks,
                    archiveExpanded = archiveExpanded,
                    onToggleArchiveExpanded = { archiveExpanded = !archiveExpanded },
                    onClearCompleted = {
                        todoHint = clearCompletedTodoTasks(tasks, prefs)
                        archiveExpanded = false
                    },
                    onToggleTask = { task ->
                        toggleTodoTaskAction(tasks, prefs, task)?.let { todoHint = it }
                    },
                    onPostponeTask = { task ->
                        postponeTodoTaskAction(tasks, prefs, task)?.let { todoHint = it }
                    },
                    onEditTask = { task ->
                        editingTaskId = task.id
                        todoHint = "正在编辑 ${task.title}"
                    },
                    onBindPomodoroTask = { task ->
                        todoHint = bindTodoTaskToPomodoro(focusPrefs, task)
                    },
                    onNotifyTask = { task ->
                        todoHint = notifyTodoTask(context, task)
                    },
                    onToggleSubtask = { task, subtaskIndex ->
                        toggleTodoSubtask(tasks, prefs, task, subtaskIndex)
                    },
                    onMoveUpTask = { task ->
                        moveTodoTask(tasks, prefs, task, -1)
                    },
                    onMoveDownTask = { task ->
                        moveTodoTask(tasks, prefs, task, 1)
                    },
                    canMoveUp = { task ->
                        canMoveTodoTask(tasks, task, -1)
                    },
                    canMoveDown = { task ->
                        canMoveTodoTask(tasks, task, 1)
                    },
                )
            }
        }
    }
}
