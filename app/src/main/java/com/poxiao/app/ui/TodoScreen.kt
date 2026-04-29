package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.json.JSONArray
import org.json.JSONObject
internal enum class TodoFilter(val title: String) {
    All("鍏ㄩ儴"),
    Focus("鑱氱劍"),
    Today("浠婂ぉ"),
    Done("宸插畬锟?),
}

private enum class TodoViewMode(val title: String) {
    Flat("娓呭崟"),
    Grouped("鍒嗙粍"),
    Calendar("鏃ュ巻"),
}

private enum class TodoDuePreset(val title: String) {
    Today("浠婂ぉ"),
    Tonight("浠婃櫄"),
    Tomorrow("鏄庡ぉ"),
    ThisWeek("鏈懆锟?),
    NextWeek("涓嬪懆"),
}

private enum class TodoReminderPreset(val title: String) {
    None("涓嶆彁锟?),
    Before10Min("鎻愬墠 10 鍒嗛挓"),
    Before30Min("鎻愬墠 30 鍒嗛挓"),
    Before1Hour("鎻愬墠 1 灏忔椂"),
    PreviousNight("鍓嶄竴澶╂櫄 20:00"),
}

private val TodoFocusGoalOptions = listOf(0, 1, 2, 3, 4, 6, 8)

private enum class TodoFocusGoalFilter(val title: String) {
    All("鍏ㄩ儴浠诲姟"),
    WithGoal("鏈夌洰锟?),
    Pending("鏈揪锟?),
    Reached("宸茶揪锟?),
}

internal data class FocusRecord(
    val taskTitle: String,
    val modeTitle: String,
    val seconds: Int,
    val finishedAt: String,
)

private data class TodoDraft(
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

@Composable
internal fun TodoScreen(initialFilter: TodoFilter = TodoFilter.All) {
    var filter by remember(initialFilter) { mutableStateOf(initialFilter) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("todo_board", android.content.Context.MODE_PRIVATE) }
    val draftPrefs = remember { context.getSharedPreferences("todo_draft", android.content.Context.MODE_PRIVATE) }
    val tasks = remember { mutableStateListOf<TodoTask>().apply { addAll(loadTodoTasks(prefs)) } }
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("浠婂ぉ 21:00") }
    var duePreset by remember { mutableStateOf(TodoDuePreset.Today) }
    var dueClock by remember { mutableStateOf("21:00") }
    var listName by remember { mutableStateOf("鏀堕泦锟?) }
    var reminder by remember { mutableStateOf("鎻愬墠 30 鍒嗛挓") }
    var reminderPreset by remember { mutableStateOf(TodoReminderPreset.Before30Min) }
    var repeatText by remember { mutableStateOf("涓嶉噸锟?) }
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
        listOf("鍏ㄩ儴娓呭崟") + tasks.map { it.listName }.filter { it.isNotBlank() }.distinct()
    }
    val allTags = remember(tasks.toList()) { tasks.flatMap { it.tags }.distinct() }
    var listFilter by remember { mutableStateOf("鍏ㄩ儴娓呭崟") }
    var focusGoalFilter by remember { mutableStateOf(TodoFocusGoalFilter.All) }
    var draftReady by remember { mutableStateOf(false) }
    var draftRestored by remember { mutableStateOf(false) }

    val visibleTasks = when (filter) {
        TodoFilter.All -> tasks.toList()
        TodoFilter.Focus -> tasks.filter { !it.done && it.quadrant != TodoQuadrant.Neither }
        TodoFilter.Today -> tasks.filter { it.dueText.contains("浠婂ぉ") || it.dueText.contains("浠婃櫄") }
        TodoFilter.Done -> tasks.filter { it.done }
    }
    val finalVisibleTasks = visibleTasks.filter { task ->
        (listFilter == "鍏ㄩ儴娓呭崟" || task.listName == listFilter) &&
            (searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true) || task.note.contains(searchQuery, ignoreCase = true)) &&
            (selectedTags.isEmpty() || selectedTags.all { it in task.tags }) &&
            when (focusGoalFilter) {
                TodoFocusGoalFilter.All -> true
                TodoFocusGoalFilter.WithGoal -> task.focusGoal > 0
                TodoFocusGoalFilter.Pending -> task.focusGoal > 0 && task.focusCount < task.focusGoal
                TodoFocusGoalFilter.Reached -> task.focusGoal > 0 && task.focusCount >= task.focusGoal
            }
    }
    val groupedTasks = finalVisibleTasks.groupBy { it.listName.ifBlank { "鏈垎锟? } }
    val completedCount = tasks.count { it.done }
    val focusCount = tasks.map { it.focusCount }.sum()
    val archivedTasks = tasks.filter { it.done }
    var archiveExpanded by remember { mutableStateOf(false) }
    val resetEditorForm = {
        title = ""
        note = ""
        due = "浠婂ぉ 21:00"
        duePreset = TodoDuePreset.Today
        dueClock = "21:00"
        listName = "鏀堕泦锟?
        reminder = "鎻愬墠 30 鍒嗛挓"
        reminderPreset = TodoReminderPreset.Before30Min
        repeatText = "涓嶉噸锟?
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
                todoHint = "宸叉仮澶嶄笂娆℃湭鎻愪氦鐨勫緟鍔炶崏绋匡拷?
            }
            draftRestored = true
        } else if (editingTask != null) {
            title = editingTask.title
            note = editingTask.note
            due = editingTask.dueText
            duePreset = inferTodoDuePreset(editingTask.dueText)
            dueClock = extractTodoDueClock(editingTask.dueText)
            listName = editingTask.listName
            reminder = editingTask.reminderText.ifBlank { "涓嶆彁锟? }
            reminderPreset = inferTodoReminderPreset(editingTask.reminderText)
            repeatText = editingTask.repeatText
            quadrant = editingTask.quadrant
            priority = editingTask.priority
            focusGoal = editingTask.focusGoal
            draftTags.clear()
            draftTags.addAll(editingTask.tags)
            draftSubtasks.clear()
            draftSubtasks.addAll(editingTask.subtasks)
            todoHint = "姝ｅ湪缂栬緫 ${editingTask.title}"
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
                    todoHint = "宸叉仮澶嶄笂娆℃湭鎻愪氦鐨勫緟鍔炶崏绋匡拷?
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
                listName != "鏀堕泦锟? ||
                repeatText != "涓嶉噸锟? ||
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
            GlassCard {
                Text("寰呭姙宸ヤ綔锟?, style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text("鏀寔鍥涜薄闄愩€佽嚜瀹氫箟浠诲姟銆佹彁閱掋€侀噸澶嶄笌娓呭崟褰掔被锟?, style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.78f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    MetricCard("鎬讳换锟?, tasks.size.toString(), ForestGreen)
                    MetricCard("宸插畬锟?, completedCount.toString(), MossGreen)
                    MetricCard("涓撴敞鍥炲啓", "${focusCount} 锟?, Ginkgo)
                    MetricCard("鐩爣杈炬垚", tasks.count { it.focusGoal > 0 && it.focusCount >= it.focusGoal }.toString(), TeaGreen)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    TodoQuadrant.entries.forEach { item ->
                        Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.54f), modifier = Modifier.width(164.dp)) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.title, style = MaterialTheme.typography.titleMedium, color = PineInk, modifier = Modifier.weight(1f, fill = false))
                                    Text("${quadrantCounts[item] ?: 0}", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f), maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
        item {
            GlassCard {
                Text(if (editingTask == null) "鏂板缓浠诲姟" else "缂栬緫浠诲姟", style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("浠诲姟鏍囬") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("琛ュ厖璇存槑") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoDuePreset.entries.toList(), selected = duePreset, label = { it.title }, onSelect = {
                    duePreset = it
                    due = buildTodoDueText(it, dueClock)
                })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = dueClock,
                    onValueChange = {
                        dueClock = it
                        due = buildTodoDueText(duePreset, dueClock)
                    },
                    label = { Text("鏃堕棿") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = listName, onValueChange = { listName = it }, label = { Text("鎵€灞炴竻锟?) }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoReminderPreset.entries.toList(), selected = reminderPreset, label = { it.title }, onSelect = {
                    reminderPreset = it
                    reminder = it.title
                })
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = repeatText, onValueChange = { repeatText = it }, label = { Text("閲嶅") }, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(options = TodoQuadrant.entries.toList(), selected = quadrant, label = { it.title }, onSelect = { quadrant = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = TodoPriority.entries.toList(), selected = priority, label = { it.title }, onSelect = { priority = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(
                    options = TodoFocusGoalOptions,
                    selected = focusGoal,
                    label = { if (it == 0) "鏃犱笓娉ㄧ洰锟? else "鐩爣 ${it} 锟? },
                    onSelect = { focusGoal = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tagDraft,
                        onValueChange = { tagDraft = it },
                        label = { Text("鏍囩") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill("鍔犲叆", Ginkgo) {
                        val normalized = tagDraft.trim()
                        if (normalized.isNotBlank() && normalized !in draftTags) {
                            draftTags.add(normalized)
                            tagDraft = ""
                        }
                    }
                }
                if (draftTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        draftTags.forEachIndexed { index, tag ->
                            ActionPill(tag, WarmMist) { draftTags.removeAt(index) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = subtaskDraft,
                        onValueChange = { subtaskDraft = it },
                        label = { Text("瀛愪换锟?) },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f),
                    )
                    ActionPill(
                        text = "鍔犲叆",
                        background = ForestGreen,
                        onClick = {
                            if (subtaskDraft.isNotBlank()) {
                                draftSubtasks.add(TodoSubtask(subtaskDraft))
                                subtaskDraft = ""
                            }
                        },
                    )
                }
                if (draftSubtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        draftSubtasks.forEachIndexed { index, subtask ->
                            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(subtask.title, style = MaterialTheme.typography.bodyMedium, color = PineInk, modifier = Modifier.weight(1f))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ActionPill(
                                            text = if (subtask.done) "宸插畬锟? else "寰呭畬锟?,
                                            background = if (subtask.done) MossGreen else TeaGreen,
                                            onClick = {
                                                draftSubtasks[index] = draftSubtasks[index].copy(done = !draftSubtasks[index].done)
                                            },
                                        )
                                        ActionPill(
                                            text = "鍒犻櫎",
                                            background = WarmMist,
                                            onClick = { draftSubtasks.removeAt(index) },
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
                        onClick = {
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
                                    todoHint = "宸插姞鍏ュ緟鍔烇細${updatedTask.title}"
                                } else {
                                    val index = tasks.indexOfFirst { it.id == updatedTask.id }
                                    if (index >= 0) tasks[index] = updatedTask
                                    todoHint = "宸叉洿鏂颁换鍔★細${updatedTask.title}"
                                }
                                saveTodoTasks(prefs, tasks)
                                clearTodoDraft(draftPrefs)
                                editingTaskId = null
                                draftRestored = false
                                resetEditorForm()
                            } else {
                                todoHint = "璇峰厛濉啓浠诲姟鏍囬"
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (editingTask == null) "鍔犲叆寰呭姙" else "淇濆瓨淇敼")
                    }
                    if (editingTask != null) {
                        OutlinedButton(
                            onClick = {
                                tasks.removeAll { it.id == editingTask.id }
                                saveTodoTasks(prefs, tasks)
                                clearTodoDraft(draftPrefs)
                                todoHint = "宸插垹闄や换鍔★細${editingTask.title}"
                                editingTaskId = null
                                draftRestored = false
                                resetEditorForm()
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("鍒犻櫎浠诲姟")
                        }
                    }
                }
                if (todoHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(todoHint, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                }
            }
        }
        item {
            GlassCard {
                SelectionRow(options = TodoFilter.entries.toList(), selected = filter, label = { it.title }, onSelect = { filter = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = TodoViewMode.entries.toList(), selected = viewMode, label = { it.title }, onSelect = { viewMode = it })
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("鎼滅储浠诲姟") },
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (allTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        allTags.forEach { tag ->
                            SelectionChip(
                                text = tag,
                                chosen = tag in selectedTags,
                                onClick = {
                                    if (tag in selectedTags) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(options = listOptions, selected = listFilter, label = { it }, onSelect = { listFilter = it })
                Spacer(modifier = Modifier.height(12.dp))
                SelectionRow(
                    options = TodoFocusGoalFilter.entries.toList(),
                    selected = focusGoalFilter,
                    label = { it.title },
                    onSelect = { focusGoalFilter = it },
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (viewMode == TodoViewMode.Flat) {
                    finalVisibleTasks.forEachIndexed { index, task ->
                        val taskIndex = tasks.indexOfFirst { it.id == task.id }
                        TodoTaskCard(
                            task = task,
                            onToggle = {
                                if (taskIndex >= 0) {
                                    todoHint = toggleTodoTask(tasks, taskIndex)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onPostpone = {
                                if (taskIndex >= 0) {
                                    todoHint = postponeTodoTask(tasks, taskIndex)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onEdit = {
                                editingTaskId = task.id
                                todoHint = "姝ｅ湪缂栬緫 ${task.title}"
                            },
                            onBindPomodoro = {
                                focusPrefs.edit()
                                    .remove("bound_task_title")
                                    .remove("bound_task_list")
                                    .apply()
                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                todoHint = "宸茬粦瀹氬埌鐣寗閽燂細${task.title}"
                            },
                            onNotify = {
                                sendAppNotification(context, "寰呭姙鎻愰啋", "${task.title} 路 ${task.dueText}")
                                todoHint = "宸插彂閫佹彁閱掞細${task.title}"
                            },
                            onToggleSubtask = { subtaskIndex ->
                                if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                    val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                    subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                    tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onMoveUp = {
                                if (taskIndex > 0) {
                                    tasks.swap(taskIndex, taskIndex - 1)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            onMoveDown = {
                                if (taskIndex >= 0 && taskIndex < tasks.lastIndex) {
                                    tasks.swap(taskIndex, taskIndex + 1)
                                    saveTodoTasks(prefs, tasks)
                                }
                            },
                            canMoveUp = taskIndex > 0,
                            canMoveDown = taskIndex in 0 until tasks.lastIndex,
                        )
                        if (index != finalVisibleTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                    }
                } else if (viewMode == TodoViewMode.Grouped) {
                    groupedTasks.entries.forEachIndexed { groupIndex, entry ->
                        Text(entry.key, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        entry.value.forEachIndexed { index, task ->
                            val taskIndex = tasks.indexOfFirst { it.id == task.id }
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {
                                    if (taskIndex >= 0) {
                                        todoHint = postponeTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "姝ｅ湪缂栬緫 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "宸茬粦瀹氬埌鐣寗閽燂細${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "寰呭姙鎻愰啋", "${task.title} 路 ${task.dueText}")
                                    todoHint = "宸插彂閫佹彁閱掞細${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {
                                    if (taskIndex > 0) {
                                        tasks.swap(taskIndex, taskIndex - 1)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveDown = {
                                    if (taskIndex >= 0 && taskIndex < tasks.lastIndex) {
                                        tasks.swap(taskIndex, taskIndex + 1)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                canMoveUp = taskIndex > 0,
                                canMoveDown = taskIndex in 0 until tasks.lastIndex,
                            )
                            if (index != entry.value.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (groupIndex != groupedTasks.entries.size - 1) Spacer(modifier = Modifier.height(14.dp))
                    }
                } else {
                    val calendarGroups = finalVisibleTasks.groupBy { it.dueText }.toList()
                    calendarGroups.forEachIndexed { groupIndex, entry ->
                        Text(entry.first, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Spacer(modifier = Modifier.height(8.dp))
                        entry.second.forEachIndexed { index, task ->
                            val taskIndex = tasks.indexOfFirst { it.id == task.id }
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {
                                    if (taskIndex >= 0) {
                                        todoHint = postponeTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "姝ｅ湪缂栬緫 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "宸茬粦瀹氬埌鐣寗閽燂細${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "寰呭姙鎻愰啋", "${task.title} 路 ${task.dueText}")
                                    todoHint = "宸插彂閫佹彁閱掞細${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {},
                                onMoveDown = {},
                                canMoveUp = false,
                                canMoveDown = false,
                            )
                            if (index != entry.second.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (groupIndex != calendarGroups.lastIndex) Spacer(modifier = Modifier.height(14.dp))
                    }
                }
                if (archivedTasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("瀹屾垚褰掓。", style = MaterialTheme.typography.titleMedium, color = PineInk)
                        ActionPill("${archivedTasks.size}", MossGreen, onClick = {})
                        ActionPill(if (archiveExpanded) "鏀惰捣" else "灞曞紑", WarmMist) { archiveExpanded = !archiveExpanded }
                        ActionPill("娓呯┖宸插畬锟?, CloudWhite) {
                            val before = tasks.size
                            tasks.removeAll { it.done }
                            saveTodoTasks(prefs, tasks)
                            archiveExpanded = false
                            todoHint = "宸叉竻锟?${before - tasks.size} 鏉″凡瀹屾垚浠诲姟锟?
                        }
                    }
                    if (archiveExpanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        archivedTasks.forEachIndexed { index, task ->
                            TodoTaskCard(
                                task = task,
                                onToggle = {
                                    val taskIndex = tasks.indexOfFirst { it.id == task.id }
                                    if (taskIndex >= 0) {
                                        todoHint = toggleTodoTask(tasks, taskIndex)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onPostpone = {},
                                onEdit = {
                                    editingTaskId = task.id
                                    todoHint = "姝ｅ湪缂栬緫 ${task.title}"
                                },
                                onBindPomodoro = {
                                    focusPrefs.edit()
                                        .remove("bound_task_title")
                                        .remove("bound_task_list")
                                        .apply()
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", task.title)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", task.listName)
                                    todoHint = "宸茬粦瀹氬埌鐣寗閽燂細${task.title}"
                                },
                                onNotify = {
                                    sendAppNotification(context, "寰呭姙鎻愰啋", "${task.title} 路 ${task.dueText}")
                                    todoHint = "宸插彂閫佹彁閱掞細${task.title}"
                                },
                                onToggleSubtask = { subtaskIndex ->
                                    val taskIndex = tasks.indexOfFirst { it.id == task.id }
                                    if (taskIndex >= 0 && subtaskIndex in tasks[taskIndex].subtasks.indices) {
                                        val subtasks = tasks[taskIndex].subtasks.toMutableList()
                                        subtasks[subtaskIndex] = subtasks[subtaskIndex].copy(done = !subtasks[subtaskIndex].done)
                                        tasks[taskIndex] = tasks[taskIndex].copy(subtasks = subtasks)
                                        saveTodoTasks(prefs, tasks)
                                    }
                                },
                                onMoveUp = {},
                                onMoveDown = {},
                                canMoveUp = false,
                                canMoveDown = false,
                            )
                            if (index != archivedTasks.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("娓呭崟缁熻", style = MaterialTheme.typography.titleMedium, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    tasks.groupBy { it.listName.ifBlank { "鏈垎锟? } }.forEach { (name, grouped) ->
                        MetricCard(name, "${grouped.count { !it.done }}/${grouped.size}", if (grouped.any { !it.done }) ForestGreen else MossGreen)
                    }
                }
            }
        }
        }
    }
}



@Composable
private fun TodoTaskCard(
    task: TodoTask,
    onToggle: () -> Unit,
    onPostpone: () -> Unit,
    onEdit: () -> Unit,
    onBindPomodoro: () -> Unit,
    onNotify: () -> Unit,
    onToggleSubtask: (Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
) {
    val dueStatus = remember(task.dueText, task.done) { todoDueStatus(task) }
    val focusProgressLabel = if (task.focusGoal > 0) "涓撴敞 ${task.focusCount}/${task.focusGoal} 锟? else "涓撴敞 ${task.focusCount} 锟?
    val focusGoalReached = task.focusGoal > 0 && task.focusCount >= task.focusGoal
    Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.56f)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.fillMaxWidth(0.78f)) {
                    Text(task.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                    Text(task.note.ifBlank { "鏃犺ˉ鍏呰锟? }, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                }
                ActionPill(if (task.done) "宸插畬锟? else "瀹屾垚", if (task.done) MossGreen else ForestGreen, onClick = onToggle)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${task.listName} 路 ${task.dueText}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
                ActionPill(dueStatus.label, dueStatus.color, onClick = {})
            }
            Text("${task.reminderText.ifBlank { "涓嶆彁锟? }} 路 ${task.repeatText}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionPill(task.priority.title, when (task.priority) {
                    TodoPriority.High -> Ginkgo
                    TodoPriority.Medium -> TeaGreen
                    TodoPriority.Low -> WarmMist
                }, onClick = {})
                ActionPill(task.quadrant.title, TeaGreen, onClick = {})
                ActionPill(focusProgressLabel, if (focusGoalReached) MossGreen else BambooGlass, onClick = {})
                if (!task.done) ActionPill("椤哄欢", CloudWhite, onClick = onPostpone)
                ActionPill("缂栬緫", WarmMist, onClick = onEdit)
                ActionPill("缁戝畾涓撴敞", ForestGreen, onClick = onBindPomodoro)
                ActionPill("鎻愰啋", Ginkgo, onClick = onNotify)
                if (canMoveUp) ActionPill("涓婄Щ", CloudWhite, onClick = onMoveUp)
                if (canMoveDown) ActionPill("涓嬬Щ", CloudWhite, onClick = onMoveDown)
            }
            if (task.focusGoal > 0) {
                Text(
                    if (focusGoalReached) "鏈换鍔＄殑涓撴敞鐩爣宸插畬鎴愶拷? else "璺濈涓撴敞鐩爣杩樺樊 ${task.focusGoal - task.focusCount} 杞拷?,
                    style = MaterialTheme.typography.bodySmall,
                    color = ForestDeep.copy(alpha = 0.72f),
                )
            }
            if (task.tags.isNotEmpty()) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.tags.forEach { tag ->
                        ActionPill(tag, Ginkgo, onClick = {})
                    }
                }
            }
            if (task.subtasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.subtasks.forEachIndexed { index, subtask ->
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.34f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ForestDeep.copy(alpha = if (subtask.done) 0.56f else 0.82f),
                                    modifier = Modifier.weight(1f),
                                )
                                ActionPill(
                                    text = if (subtask.done) "宸插畬锟? else "瀹屾垚",
                                    background = if (subtask.done) MossGreen else ForestGreen,
                                    onClick = { onToggleSubtask(index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



internal fun loadTodoTasks(prefs: android.content.SharedPreferences): List<TodoTask> {
    val raw = prefs.getString("todo_tasks", null) ?: return emptyList()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    TodoTask(
                        id = item.optString("id").ifBlank { "todo-$index" },
                        title = item.optString("title").ifBlank { "鏈懡鍚嶄换锟? },
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
                        listName = item.optString("listName", "鏀堕泦锟?),
                        reminderText = item.optString("reminderText"),
                        repeatText = item.optString("repeatText", "涓嶉噸锟?),
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
    prefs: android.content.SharedPreferences,
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
    prefs.edit().putString("todo_tasks", array.toString()).apply()
}

private fun loadTodoDraft(prefs: android.content.SharedPreferences): TodoDraft? {
    val raw = prefs.getString("todo_draft_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val item = JSONObject(raw)
        TodoDraft(
            editingTaskId = item.optString("editingTaskId").ifBlank { null },
            title = item.optString("title"),
            note = item.optString("note"),
            duePreset = runCatching { TodoDuePreset.valueOf(item.optString("duePreset", TodoDuePreset.Today.name)) }.getOrDefault(TodoDuePreset.Today),
            dueClock = item.optString("dueClock", "21:00"),
            listName = item.optString("listName", "鏀堕泦锟?),
            reminderPreset = runCatching {
                TodoReminderPreset.valueOf(item.optString("reminderPreset", TodoReminderPreset.Before30Min.name))
            }.getOrDefault(TodoReminderPreset.Before30Min),
            repeatText = item.optString("repeatText", "涓嶉噸锟?),
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

private fun saveTodoDraft(
    prefs: android.content.SharedPreferences,
    draft: TodoDraft,
) {
    prefs.edit()
        .putString(
            "todo_draft_v1",
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

private fun clearTodoDraft(prefs: android.content.SharedPreferences) {
    prefs.edit().remove("todo_draft_v1").apply()
}

private fun <T> MutableList<T>.swap(left: Int, right: Int) {
    if (left !in indices || right !in indices || left == right) return
    val temp = this[left]
    this[left] = this[right]
    this[right] = temp
}

private data class TodoDueStatus(
    val label: String,
    val color: Color,
)

private val TodoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun buildTodoDueText(
    preset: TodoDuePreset,
    clock: String,
): String {
    val normalizedClock = clock.trim()
    return if (normalizedClock.isBlank()) preset.title else "${preset.title} $normalizedClock"
}

private fun inferTodoDuePreset(dueText: String): TodoDuePreset {
    return when {
        dueText.contains("浠婃櫄") -> TodoDuePreset.Tonight
        dueText.contains("鏄庡ぉ") -> TodoDuePreset.Tomorrow
        dueText.contains("鏈懆") -> TodoDuePreset.ThisWeek
        dueText.contains("涓嬪懆") -> TodoDuePreset.NextWeek
        else -> TodoDuePreset.Today
    }
}

private fun extractTodoDueClock(dueText: String): String {
    val parts = dueText.split(" ")
    return parts.getOrNull(1) ?: "21:00"
}

private fun inferTodoReminderPreset(reminderText: String): TodoReminderPreset {
    return TodoReminderPreset.entries.firstOrNull { it.title == reminderText } ?: TodoReminderPreset.Before30Min
}

private fun todoDueStatus(task: TodoTask): TodoDueStatus {
    if (task.done) return TodoDueStatus("宸插畬锟?, MossGreen)
    val now = LocalDateTime.now()
    val dueAt = parseTodoDateTime(task.dueText, now)
    val isGoalTask = task.focusGoal > 0 && task.focusCount < task.focusGoal
    if (dueAt != null) {
        val today = now.toLocalDate()
        val dueDate = dueAt.toLocalDate()
        val thisWeekEnd = today.plusDays((7 - today.dayOfWeek.value).coerceAtLeast(0).toLong())
        return when {
            dueAt.isBefore(now.minusMinutes(1)) -> if (isGoalTask) TodoDueStatus("鐩爣瓒呮湡", Ginkgo) else TodoDueStatus("宸查€炬湡", Ginkgo)
            dueDate == today -> if (isGoalTask) TodoDueStatus("鐩爣涓存湡", Ginkgo) else TodoDueStatus("涓磋繎鎴", Ginkgo)
            dueDate == today.plusDays(1) -> if (isGoalTask) TodoDueStatus("鐩爣灏嗚嚦", TeaGreen) else TodoDueStatus("鍗冲皢鍒版湡", TeaGreen)
            !dueDate.isAfter(thisWeekEnd) -> if (isGoalTask) TodoDueStatus("鐩爣鎺ㄨ繘锟?, ForestGreen) else TodoDueStatus("鏈懆澶勭悊", ForestGreen)
            else -> if (isGoalTask) TodoDueStatus("鐩爣鎺ㄨ繘锟?, ForestGreen) else TodoDueStatus("宸插畨锟?, WarmMist)
        }
    }
    if (isGoalTask) {
        return when {
            task.dueText.contains("浠婂ぉ") || task.dueText.contains("浠婃櫄") -> TodoDueStatus("鐩爣涓存湡", Ginkgo)
            task.dueText.contains("鏄庡ぉ") || task.dueText.contains("鏄庢櫄") -> TodoDueStatus("鐩爣灏嗚嚦", TeaGreen)
            else -> TodoDueStatus("鐩爣鎺ㄨ繘锟?, ForestGreen)
        }
    }
    return when {
        task.dueText.contains("浠婂ぉ") || task.dueText.contains("浠婃櫄") -> TodoDueStatus("涓磋繎鎴", Ginkgo)
        task.dueText.contains("鏄庡ぉ") || task.dueText.contains("鏄庢櫄") -> TodoDueStatus("鍗冲皢鍒版湡", TeaGreen)
        task.dueText.contains("锟?) || task.dueText.contains("鏈懆") -> TodoDueStatus("鏈懆澶勭悊", ForestGreen)
        else -> TodoDueStatus("宸插畨锟?, WarmMist)
    }
}

private fun isRepeatingTodo(repeatText: String): Boolean {
    val normalized = repeatText.trim()
    return normalized.isNotBlank() && normalized != "涓嶉噸锟?
}

private fun formatTodoDateTime(dateTime: LocalDateTime): String = dateTime.format(TodoDateTimeFormatter)

private fun nextRepeatedDueAt(
    repeatText: String,
    currentDueAt: LocalDateTime,
): LocalDateTime? {
    val normalized = repeatText.trim()
    return when {
        normalized == "涓嶉噸锟? || normalized.isBlank() -> null
        normalized.contains("宸ヤ綔锟?) -> {
            var candidate = currentDueAt.plusDays(1)
            while (candidate.dayOfWeek.value >= 6) {
                candidate = candidate.plusDays(1)
            }
            candidate
        }
        normalized.contains("锟?锟?) || normalized.contains("姣忎袱锟?) -> currentDueAt.plusDays(2)
        normalized.contains("姣忓ぉ") || normalized.contains("姣忔棩") -> currentDueAt.plusDays(1)
        normalized.contains("锟?锟?) || normalized.contains("姣忎袱锟?) -> currentDueAt.plusWeeks(2)
        normalized.contains("姣忓懆") -> currentDueAt.plusWeeks(1)
        normalized.contains("姣忔湀") -> currentDueAt.plusMonths(1)
        normalized.contains("姣忓勾") -> currentDueAt.plusYears(1)
        else -> null
    }
}

private fun advanceTodoReminderText(
    reminderText: String,
    currentDueAt: LocalDateTime,
    nextDueAt: LocalDateTime,
    now: LocalDateTime,
): String {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return trimmed
    if (TodoReminderPreset.entries.any { it.title == trimmed }) return trimmed
    val currentReminderAt = parseReminderDateTime(trimmed, currentDueAt, now) ?: return trimmed
    val offset = java.time.Duration.between(currentReminderAt, currentDueAt)
    if (offset.isNegative) return trimmed
    return formatTodoDateTime(nextDueAt.minus(offset))
}

private fun advanceRepeatingTodoTask(
    task: TodoTask,
    now: LocalDateTime = LocalDateTime.now(),
): TodoTask? {
    if (!isRepeatingTodo(task.repeatText)) return null
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(task.dueText))
                ?: LocalTime.of(21, 0),
        )
    val nextDueAt = nextRepeatedDueAt(task.repeatText, currentDueAt) ?: return null
    return task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
        subtasks = task.subtasks.map { it.copy(done = false) },
        focusCount = 0,
        done = false,
    )
}

private fun postponeTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) return "宸插畬鎴愪换鍔℃棤闇€椤哄欢锟?{task.title}"
    val currentDueAt = parseTodoDateTime(task.dueText, now)
        ?: LocalDateTime.of(
            now.toLocalDate(),
            parseClock(
                Regex("""(\d{1,2}:\d{2})""").find(task.dueText)?.groupValues?.getOrNull(1)
                    ?: extractTodoClockFallback(task.dueText),
            ) ?: LocalTime.of(21, 0),
        )
    val nextDueAt = currentDueAt.plusDays(1)
    tasks[taskIndex] = task.copy(
        dueText = formatTodoDateTime(nextDueAt),
        reminderText = advanceTodoReminderText(task.reminderText, currentDueAt, nextDueAt, now),
    )
    return "宸查『寤朵换鍔★細${task.title}"
}

private fun toggleTodoTask(
    tasks: MutableList<TodoTask>,
    taskIndex: Int,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val task = tasks.getOrNull(taskIndex) ?: return ""
    if (task.done) {
        tasks[taskIndex] = task.copy(done = false)
        return "宸叉仮澶嶄换鍔★細${task.title}"
    }
    val repeatedTask = advanceRepeatingTodoTask(task, now)
    return if (repeatedTask != null) {
        tasks[taskIndex] = repeatedTask
        "宸茬画鏈熼噸澶嶄换鍔★細${task.title}"
    } else {
        tasks[taskIndex] = task.copy(done = true)
        "宸插畬鎴愪换鍔★細${task.title}"
    }
}

internal fun recordTodoFocusProgress(
    prefs: android.content.SharedPreferences,
    boundTaskTitle: String,
) {
    if (boundTaskTitle.isBlank()) return
    val tasks = loadTodoTasks(prefs).toMutableList()
    val taskIndex = tasks.indexOfFirst { it.title == boundTaskTitle }
    if (taskIndex < 0) return
    val task = tasks[taskIndex]
    val subtasks = task.subtasks.toMutableList()
    val pendingIndex = subtasks.indexOfFirst { !it.done }
    if (pendingIndex >= 0) {
        subtasks[pendingIndex] = subtasks[pendingIndex].copy(done = true)
    }
    val updatedFocusCount = task.focusCount + 1
    val focusGoalReached = task.focusGoal <= 0 || updatedFocusCount >= task.focusGoal
    val updatedTask = task.copy(
        subtasks = subtasks,
        focusCount = updatedFocusCount,
        done = if (subtasks.isNotEmpty()) subtasks.all { it.done } && focusGoalReached else (task.done || focusGoalReached && task.focusGoal > 0),
    )
    tasks[taskIndex] = if (updatedTask.done) advanceRepeatingTodoTask(updatedTask) ?: updatedTask else updatedTask
    saveTodoTasks(prefs, tasks)
}



internal data class ScheduledReminderPlan(
    val id: String,
    val title: String,
    val body: String,
    val triggerAtMillis: Long,
)


internal fun buildTodoReminderPlans(
    prefs: android.content.SharedPreferences,
    preferenceState: com.poxiao.app.settings.NotificationPreferenceState,
): List<ScheduledReminderPlan> {
    if (!preferenceState.todoEnabled) return emptyList()
    if (!prefs.contains("todo_tasks")) return emptyList()
    val now = LocalDateTime.now()
    return loadTodoTasks(prefs)
        .filterNot { it.done }
        .mapNotNull { task ->
            val dueAt = parseTodoDateTime(task.dueText, now) ?: return@mapNotNull null
            val remindAt = parseReminderDateTime(task.reminderText, dueAt, now) ?: return@mapNotNull null
            ScheduledReminderPlan(
                id = "todo_${task.id}",
                title = "寰呭姙鎻愰啋",
                body = "${task.title} 路 ${task.dueText}",
                triggerAtMillis = remindAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
        }
}


private fun parseTodoDateTime(
    text: String,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = text.trim()
    val explicitDate = Regex("""(\d{4}-\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
    val clock = parseClock(Regex("""(\d{1,2}:\d{2})""").find(trimmed)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(trimmed))
        ?: LocalTime.of(21, 0)
    val date = when {
        explicitDate != null -> runCatching { LocalDate.parse(explicitDate) }.getOrNull()
        trimmed.contains("浠婃櫄") || trimmed.contains("浠婂ぉ") -> now.toLocalDate()
        trimmed.contains("鏄庢櫄") || trimmed.contains("鏄庡ぉ") -> now.toLocalDate().plusDays(1)
        trimmed.contains("鏈懆") -> now.toLocalDate().plusDays((7 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        trimmed.contains("涓嬪懆") -> now.toLocalDate().plusDays(7)
        trimmed.contains("鍛ㄦ湯") -> now.toLocalDate().plusDays((6 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        else -> now.toLocalDate()
    } ?: return null
    return LocalDateTime.of(date, clock)
}

private fun parseReminderDateTime(
    reminderText: String,
    dueAt: LocalDateTime,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return null
    val reminderTime = when (trimmed) {
        TodoReminderPreset.Before10Min.title -> dueAt.minusMinutes(10)
        TodoReminderPreset.Before30Min.title -> dueAt.minusMinutes(30)
        TodoReminderPreset.Before1Hour.title -> dueAt.minusHours(1)
        TodoReminderPreset.PreviousNight.title -> LocalDateTime.of(dueAt.toLocalDate().minusDays(1), LocalTime.of(20, 0))
        else -> parseTodoDateTime(trimmed, now)
    } ?: return null
    return if (reminderTime.isAfter(now)) reminderTime else null
}

private fun extractTodoClockFallback(text: String): String {
    return when {
        text.contains("浠婃櫄") -> "20:00"
        text.contains("鏄庢櫄") -> "20:00"
        else -> "21:00"
    }
}


