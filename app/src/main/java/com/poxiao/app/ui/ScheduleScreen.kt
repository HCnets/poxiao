package com.poxiao.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.hita.HitaAcademicGateway
import com.poxiao.app.hita.HitaCourseBlock
import com.poxiao.app.hita.HitaScheduleRepository
import com.poxiao.app.hita.HitaScheduleUiState
import com.poxiao.app.hita.HitaTerm
import com.poxiao.app.hita.HitaTimeSlot
import com.poxiao.app.hita.HitaWeek
import com.poxiao.app.hita.HitaWeekDay
import com.poxiao.app.hita.HitaWeekSchedule
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.json.JSONArray
import org.json.JSONObject

internal enum class ScheduleMode(val label: String) {
    Week("鍛ㄨ锟?),
    Day("鏃ヨ锟?),
    Month("鏈堣锟?),
}

internal enum class ScheduleWorkbench(val title: String) {
    Timetable("璇捐〃"),
    Grades("鎴愮哗瓒嬪娍"),
    ExamWeek("鑰冭瘯锟?),
}

@Composable
internal fun ScheduleScreen(
    repository: HitaScheduleRepository,
    initialMode: ScheduleMode = ScheduleMode.Week,
    initialWorkbench: ScheduleWorkbench = ScheduleWorkbench.Timetable,
    onOpenAcademicAccount: () -> Unit = {},
    onOpenCourseNotes: (CourseNoteSeed) -> Unit = {},
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("schedule_auth", 0) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", Context.MODE_PRIVATE) }
    val focusBridgePrefs = remember { context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE) }
    val examWeekPrefs = remember { context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val uiState by repository.observeUiState().collectAsState()
    val savedStudentId = remember { SecurePrefs.getString(prefs, "student_id_secure", "student_id") }
    val savedPassword = remember { SecurePrefs.getString(prefs, "password_secure", "password") }
    var restored by remember { mutableStateOf(false) }
    var mode by remember(initialMode) { mutableStateOf(initialMode) }
    var workbench by remember(initialWorkbench) { mutableStateOf(initialWorkbench) }
    var selectedCourse by remember { mutableStateOf<HitaCourseBlock?>(null) }
    var lastSyncTime by remember { mutableStateOf(prefs.getString("schedule_last_sync", "").orEmpty()) }
    var exportHint by remember { mutableStateOf("") }
    val extraEvents = remember { mutableStateListOf<ScheduleExtraEvent>().apply { addAll(loadScheduleExtraEvents(prefs)) } }
    var gradeTrend by remember { mutableStateOf<List<GradeTrendPoint>>(emptyList()) }
    var gradeTrendLoading by remember { mutableStateOf(false) }
    var gradeTrendStatus by remember { mutableStateOf("鐧诲綍鍚庡彲鏌ョ湅鎴愮哗瓒嬪娍锟?) }
    var selectedTrendTerm by remember { mutableStateOf<String?>(null) }
    val completedExamWeekIds = remember { mutableStateListOf<String>().apply { addAll(loadStringList(examWeekPrefs, "completed_ids")) } }
    var selectedExtraEventId by remember(uiState.selectedDate) { mutableStateOf<String?>(null) }
    val selectedDateEvents = extraEvents
        .filter { it.date == uiState.selectedDate }
        .sortedBy { eventSortKey(it.time) }
    val examWeekItems = remember(uiState.weekSchedule, extraEvents) {
        buildExamWeekItems(uiState.weekSchedule, extraEvents, completedExamWeekIds)
    }
    val month = remember(uiState.selectedDate) {
        runCatching { YearMonth.from(LocalDate.parse(uiState.selectedDate)) }.getOrElse { YearMonth.now() }
    }

    LaunchedEffect(Unit) {
        if (!restored) {
            restored = true
            loadCachedScheduleUiState(prefs)?.let { repository.restoreCachedState(it) }
            if (savedStudentId.isNotBlank() && savedPassword.isNotBlank()) {
                repository.connectAndLoad(savedStudentId, savedPassword)
            }
        }
    }

    LaunchedEffect(uiState.loggedIn, uiState.terms, uiState.studentId) {
        val currentStudentId = SecurePrefs.getString(prefs, "student_id_secure", "student_id")
        val currentPassword = SecurePrefs.getString(prefs, "password_secure", "password")
        val academicGateway = if (currentStudentId.isNotBlank() && currentPassword.isNotBlank()) {
            HitaAcademicGateway(currentStudentId, currentPassword)
        } else {
            null
        }
        if (!uiState.loggedIn || academicGateway == null) {
            gradeTrend = emptyList()
            gradeTrendStatus = "鐧诲綍鍚庡彲鏌ョ湅鎴愮哗瓒嬪娍锟?
            return@LaunchedEffect
        }
        gradeTrendLoading = true
        gradeTrendStatus = "姝ｅ湪鏁寸悊鍚勫鏈熸垚缁╄秼锟?.."
        runCatching {
            val cardsByTerm = uiState.terms.mapNotNull { term ->
                val cards = academicGateway.fetchGradesForTerm(term)
                if (cards.isEmpty()) null else term.name to cards
            }
            buildGradeTrendPoints(cardsByTerm)
        }.onSuccess { points ->
            gradeTrend = points
            selectedTrendTerm = selectedTrendTerm?.takeIf { target -> points.any { it.termName == target } }
                ?: points.firstOrNull()?.termName
            gradeTrendStatus = if (points.isEmpty()) "褰撳墠杩樻病鏈夊彲鐢ㄤ簬鍒嗘瀽鐨勬垚缁╄褰曪拷? else "宸茬敓锟?${points.size} 涓鏈熺殑鎴愮哗瓒嬪娍锟?
        }.onFailure {
            gradeTrend = emptyList()
            gradeTrendStatus = it.message ?: "鎴愮哗瓒嬪娍鍔犺浇澶辫触锟?
        }
        gradeTrendLoading = false
    }

    LaunchedEffect(
        uiState.loggedIn,
        uiState.loading,
        uiState.currentTerm,
        uiState.currentWeek,
        uiState.selectedDate,
        uiState.weekSchedule,
        uiState.selectedDateCourses,
    ) {
        if (uiState.loggedIn && !uiState.loading) {
            saveCachedScheduleUiState(prefs, uiState)
            val syncText = formatSyncTime(LocalDateTime.now())
            lastSyncTime = syncText
            prefs.edit().putString("schedule_last_sync", syncText).apply()
        }
    }

    LaunchedEffect(
        uiState.weekSchedule.week.title,
        uiState.selectedDate,
        extraEvents.joinToString("|") { "${it.id}:${it.date}:${it.time}:${it.title}:${it.type}" },
        completedExamWeekIds.joinToString("|"),
    ) {
        refreshLocalReminderSchedule(context)
    }

    ScreenColumn {
        item {
            GlassCard {
                Text("璇剧▼涓績", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                if (lastSyncTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("涓婃鍚屾锟?lastSyncTime", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                }
                if (uiState.authExpired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "褰撳墠鐧诲綍浼氳瘽鍙兘宸插け鏁堬紝璇烽噸鏂拌繛鎺ユ暀鍔＄郴缁熷悗鍐嶅埛鏂帮拷?,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A5B34),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { scope.launch { repository.refreshCurrent() } },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MossGreen),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.loggedIn && !uiState.loading,
                ) {
                    Text(if (uiState.loading) "姝ｅ湪鍒锋柊..." else "绔嬪嵆鍒锋柊璇捐〃")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActionPill("璐﹀彿", WarmMist, onClick = onOpenAcademicAccount)
                    ActionPill("澶嶅埗鎽樿", MossGreen) {
                        val text = buildScheduleShareText(uiState)
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("璇捐〃鎽樿", text))
                        exportHint = "宸插鍒跺綋鍓嶅懆璇捐〃鎽樿锟?
                    }
                    ActionPill("绯荤粺鍒嗕韩", Ginkgo) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, buildScheduleShareText(uiState))
                        }
                        context.startActivity(Intent.createChooser(intent, "鍒嗕韩璇捐〃鎽樿"))
                        exportHint = "宸叉墦寮€绯荤粺鍒嗕韩闈㈡澘锟?
                    }
                }
                if (exportHint.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(exportHint, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
        item {
            GlassCard {
                Text(uiState.currentTerm.name, style = MaterialTheme.typography.titleLarge, color = PineInk)
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = ScheduleWorkbench.entries.toList(),
                    selected = workbench,
                    label = { it.title },
                    onSelect = { workbench = it },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = uiState.terms,
                    selected = uiState.currentTerm,
                    label = { it.name },
                    onSelect = { scope.launch { repository.selectTerm(it) } },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = uiState.weeks,
                    selected = uiState.currentWeek,
                    label = { it.title },
                    onSelect = { scope.launch { repository.selectWeek(it) } },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SelectionRow(
                    options = ScheduleMode.entries.toList(),
                    selected = mode,
                    label = { it.label },
                    onSelect = { mode = it },
                )
            }
        }
        item {
            when (workbench) {
                ScheduleWorkbench.Timetable -> when (mode) {
                    ScheduleMode.Week -> WeekScheduleCard(
                        title = uiState.currentWeek.title,
                        slots = uiState.weekSchedule.timeSlots,
                        days = uiState.weekSchedule.days,
                        courses = uiState.weekSchedule.courses,
                        selectedCourse = selectedCourse,
                        onSelectCourse = { selectedCourse = it },
                    )
                    ScheduleMode.Day -> DayScheduleCard(
                        dates = uiState.weekSchedule.days.map { it.fullDate },
                        selectedDate = uiState.selectedDate,
                        courses = uiState.selectedDateCourses,
                        events = selectedDateEvents,
                        selectedCourse = selectedCourse,
                        selectedEventId = selectedExtraEventId,
                        onSelectDate = {
                            selectedCourse = null
                            selectedExtraEventId = null
                            scope.launch { repository.selectDate(it) }
                        },
                        onSelectCourse = {
                            selectedExtraEventId = null
                            selectedCourse = it
                        },
                        onSelectEvent = { event ->
                            selectedCourse = null
                            selectedExtraEventId = event.id
                        },
                        onAddEvent = { event ->
                            extraEvents.add(0, event)
                            selectedExtraEventId = event.id
                            selectedCourse = null
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                        onUpdateEvent = { event ->
                            val index = extraEvents.indexOfFirst { it.id == event.id }
                            if (index >= 0) {
                                extraEvents[index] = event
                                selectedExtraEventId = event.id
                            }
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                        onDeleteEvent = { eventId ->
                            extraEvents.removeAll { it.id == eventId }
                            if (selectedExtraEventId == eventId) selectedExtraEventId = null
                            saveScheduleExtraEvents(prefs, extraEvents)
                        },
                    )
                    ScheduleMode.Month -> MonthScheduleCard(
                        month = month,
                        selectedDate = uiState.selectedDate,
                        activeDates = (uiState.weekSchedule.days.map { it.fullDate } + extraEvents.map { it.date }).distinct(),
                        selectedCourses = uiState.selectedDateCourses,
                        selectedEvents = selectedDateEvents,
                        selectedCourse = selectedCourse,
                        onSelectDate = {
                            selectedCourse = null
                            scope.launch { repository.selectDate(it) }
                        },
                        onSelectCourse = { selectedCourse = it },
                    )
                }
                ScheduleWorkbench.Grades -> GradeTrendCard(
                    loading = gradeTrendLoading,
                    status = gradeTrendStatus,
                    points = gradeTrend,
                    selectedTerm = selectedTrendTerm,
                    onSelectTerm = { selectedTrendTerm = it },
                )
                ScheduleWorkbench.ExamWeek -> ExamWeekModeCard(
                    weekTitle = uiState.currentWeek.title,
                    selectedDate = uiState.selectedDate,
                    items = examWeekItems,
                    onCreateTodo = { item ->
                        val tasks = loadTodoTasks(todoPrefs).toMutableList()
                        tasks.add(
                            0,
                            TodoTask(
                                id = "exam-${System.currentTimeMillis()}",
                                title = item.title,
                                note = item.detail,
                                quadrant = if (item.priority == 0) TodoQuadrant.ImportantUrgent else TodoQuadrant.ImportantNotUrgent,
                                priority = when (item.priority) {
                                    0 -> TodoPriority.High
                                    1 -> TodoPriority.Medium
                                    else -> TodoPriority.Low
                                },
                                dueText = "${item.date} ${item.countdownLabel}",
                                tags = listOf("鑰冭瘯锟?),
                                listName = "瀛︿範",
                                reminderText = if (item.priority <= 1) "浠婂ぉ 20:00" else "",
                                repeatText = "涓嶉噸锟?,
                            ),
                        )
                        saveTodoTasks(todoPrefs, tasks)
                    },
                    onBindFocus = { item ->
                        focusBridgePrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_title_secure", item.title)
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "鑰冭瘯锟?)
                    },
                    onBindFocusGroup = { date, groupedItems ->
                        val headline = groupedItems.firstOrNull()?.title ?: "鑰冭瘯鍛ㄥ啿锟?
                        focusBridgePrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_title_secure", "$headline 锟?${groupedItems.size} 锟?)
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "鑰冭瘯锟?${date.substringAfterLast("-")}")
                    },
                    onCreateTodoGroup = { date, groupedItems ->
                        val tasks = loadTodoTasks(todoPrefs).toMutableList()
                        val newTasks = groupedItems.mapIndexed { index, item ->
                            TodoTask(
                                id = "exam-group-${date}-${index}-${System.currentTimeMillis()}",
                                title = item.title,
                                note = item.detail,
                                quadrant = if (item.priority == 0) TodoQuadrant.ImportantUrgent else TodoQuadrant.ImportantNotUrgent,
                                priority = when (item.priority) {
                                    0 -> TodoPriority.High
                                    1 -> TodoPriority.Medium
                                    else -> TodoPriority.Low
                                },
                                dueText = "${item.date} ${item.countdownLabel}",
                                tags = listOf("鑰冭瘯锟?, date),
                                listName = "鑰冭瘯锟?${date.substringAfterLast("-")}",
                                reminderText = if (item.priority <= 1) "浠婂ぉ 20:00" else "",
                                repeatText = "涓嶉噸锟?,
                            )
                        }
                        tasks.addAll(0, newTasks)
                        saveTodoTasks(todoPrefs, tasks)
                    },
                    onToggleFinishedGroup = { groupedItems, finished ->
                        groupedItems.forEach { item ->
                            if (finished) {
                                if (item.id !in completedExamWeekIds) completedExamWeekIds.add(item.id)
                            } else {
                                completedExamWeekIds.remove(item.id)
                            }
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                    onToggleFinished = { item ->
                        if (item.finished) {
                            completedExamWeekIds.remove(item.id)
                        } else {
                            if (item.id !in completedExamWeekIds) completedExamWeekIds.add(item.id)
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                    onClearFinished = {
                        completedExamWeekIds.removeAll { id ->
                            examWeekItems.any { it.id == id && it.finished && (it.countdownLabel == "宸茬粨锟? || it.finished) }
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                )
            }
        }
        if (workbench == ScheduleWorkbench.Timetable && selectedCourse != null) {
            item {
                AnimatedVisibility(
                    visible = selectedCourse != null,
                    enter = fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.96f),
                    exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.98f),
                ) {
                    selectedCourse?.let { course ->
                        CourseDetailCard(
                            course = course,
                            weekTitle = uiState.currentWeek.title,
                            dayLabel = uiState.weekSchedule.days.firstOrNull { it.weekDay == course.dayOfWeek }?.label.orEmpty(),
                            onOpenNotes = {
                                onOpenCourseNotes(
                                    CourseNoteSeed(
                                        courseName = course.courseName,
                                        teacher = course.teacher,
                                        classroom = course.classroom,
                                        courseLabel = "锟?${course.majorIndex} 澶ц妭",
                                    ),
                                )
                            },
                            onClose = { selectedCourse = null },
                        )
                    }
                }
            }
        }
    }
}

internal data class FreeTimeDaySummary(
    val dayLabel: String,
    val freeCount: Int,
    val labels: List<String>,
)

internal data class DayAnalysisSummary(
    val dayLabel: String,
    val highlights: List<String>,
)

internal fun weeklyFreeTimeSummary(
    slots: List<HitaTimeSlot>,
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
): List<FreeTimeDaySummary> {
    return days.map { day ->
        val occupied = courses.filter { it.dayOfWeek == day.weekDay }.map { it.majorIndex }.toSet()
        val freeSlots = slots.filter { it.majorIndex !in occupied }
        FreeTimeDaySummary(
            dayLabel = day.label,
            freeCount = freeSlots.size,
            labels = if (freeSlots.isEmpty()) listOf("褰撳ぉ璇剧▼宸叉弧") else freeSlots.map { it.label },
        )
    }
}

internal fun weeklyCourseAnalysis(
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
): List<DayAnalysisSummary> {
    return days.mapNotNull { day ->
        val dayCourses = courses.filter { it.dayOfWeek == day.weekDay }.sortedBy { it.majorIndex }
        if (dayCourses.isEmpty()) return@mapNotNull null
        val conflictTags = dayCourses.groupBy { it.majorIndex }
            .filterValues { it.size > 1 }
            .map { (majorIndex, items) -> "鍐茬獊 锟?${majorIndex} 澶ц妭 ${items.size} 锟? }
        val contiguousTags = dayCourses.zipWithNext()
            .filter { (left, right) -> right.majorIndex - left.majorIndex == 1 }
            .map { (left, right) -> "杩炲爞 ${left.courseName} 锟?${right.courseName}" }
        val tags = (conflictTags + contiguousTags).distinct()
        if (tags.isEmpty()) null else DayAnalysisSummary(day.label, tags)
    }
}

internal fun eventSortKey(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) return Int.MAX_VALUE / 2
    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE / 2
    val minute = parts[1].toIntOrNull() ?: 0
    return hour * 60 + minute
}

internal fun loadScheduleExtraEvents(
    prefs: android.content.SharedPreferences,
): List<ScheduleExtraEvent> {
    val raw = prefs.getString("schedule_extra_events_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.getJSONObject(index)
                add(
                    ScheduleExtraEvent(
                        id = json.optString("id"),
                        date = json.optString("date"),
                        title = json.optString("title"),
                        time = json.optString("time"),
                        type = json.optString("type"),
                        note = json.optString("note"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveScheduleExtraEvents(
    prefs: android.content.SharedPreferences,
    events: List<ScheduleExtraEvent>,
) {
    val array = JSONArray().apply {
        events.forEach { event ->
            put(
                JSONObject().apply {
                    put("id", event.id)
                    put("date", event.date)
                    put("title", event.title)
                    put("time", event.time)
                    put("type", event.type)
                    put("note", event.note)
                },
            )
        }
    }
    prefs.edit().putString("schedule_extra_events_v1", array.toString()).apply()
}

private fun buildScheduleShareText(
    state: HitaScheduleUiState,
): String {
    val courseLines = state.weekSchedule.days.joinToString("\n") { day ->
        val dayCourses = state.weekSchedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
        if (dayCourses.isEmpty()) {
            "${day.label} ${day.date}锛氭棤璇剧▼"
        } else {
            val body = dayCourses.joinToString("锟?) { course ->
                "${course.courseName}锛堢${course.majorIndex}澶ц妭 ${course.classroom.ifBlank { "鏁欏寰呰ˉ锟? }} ${course.teacher.ifBlank { "鏁欏笀寰呰ˉ锟? }}锟?
            }
            "${day.label} ${day.date}锟?body"
        }
    }
    val freeLines = weeklyFreeTimeSummary(
        slots = state.weekSchedule.timeSlots,
        days = state.weekSchedule.days,
        courses = state.weekSchedule.courses,
    ).joinToString("\n") { item ->
        "${item.dayLabel}锟?{item.labels.joinToString("锟?)}"
    }
    return buildString {
        appendLine("${state.currentTerm.name} ${state.currentWeek.title} 璇捐〃鎽樿")
        appendLine()
        appendLine("璇剧▼瀹夋帓")
        appendLine(courseLines)
        appendLine()
        appendLine("绌洪棽鏃舵")
        appendLine(freeLines)
    }.trim()
}

private fun saveCachedScheduleUiState(
    prefs: android.content.SharedPreferences,
    state: HitaScheduleUiState,
) {
    val root = JSONObject().apply {
        put("loggedIn", state.loggedIn)
        put("studentId", state.studentId)
        put("studentName", state.studentName)
        put("selectedDate", state.selectedDate)
        put("terms", JSONArray().apply { state.terms.forEach { put(termToJson(it)) } })
        put("currentTerm", termToJson(state.currentTerm))
        put("weeks", JSONArray().apply { state.weeks.forEach { put(weekToJson(it)) } })
        put("currentWeek", weekToJson(state.currentWeek))
        put("weekSchedule", weekScheduleToJson(state.weekSchedule))
        put("selectedDateCourses", JSONArray().apply { state.selectedDateCourses.forEach { put(courseToJson(it)) } })
    }
    prefs.edit().putString("schedule_cache_v1", root.toString()).apply()
}

internal fun loadCachedScheduleUiState(
    prefs: android.content.SharedPreferences,
): HitaScheduleUiState? {
    val raw = prefs.getString("schedule_cache_v1", "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val root = JSONObject(raw)
        val terms = root.optJSONArray("terms")?.let(::termsFromJson).orEmpty()
        val weeks = root.optJSONArray("weeks")?.let(::weeksFromJson).orEmpty()
        val weekSchedule = root.optJSONObject("weekSchedule")?.let(::weekScheduleFromJson) ?: return null
        val currentTerm = root.optJSONObject("currentTerm")?.let(::termFromJson) ?: weekSchedule.term
        val currentWeek = root.optJSONObject("currentWeek")?.let(::weekFromJson) ?: weekSchedule.week
        val selectedDate = root.optString("selectedDate", weekSchedule.days.firstOrNull()?.fullDate.orEmpty())
        val selectedDateCourses = root.optJSONArray("selectedDateCourses")?.let(::coursesFromJson).orEmpty()
        HitaScheduleUiState(
            loading = false,
            loggedIn = root.optBoolean("loggedIn", false),
            studentId = root.optString("studentId", ""),
            studentName = root.optString("studentName", ""),
            terms = if (terms.isEmpty()) listOf(currentTerm) else terms,
            currentTerm = currentTerm,
            weeks = if (weeks.isEmpty()) listOf(currentWeek) else weeks,
            currentWeek = currentWeek,
            weekSchedule = weekSchedule,
            selectedDate = selectedDate,
            selectedDateCourses = if (selectedDateCourses.isEmpty()) {
                val day = weekSchedule.days.firstOrNull { it.fullDate == selectedDate }?.weekDay ?: 1
                weekSchedule.courses.filter { it.dayOfWeek == day }
            } else {
                selectedDateCourses
            },
            status = "宸叉仮澶嶄笂娆＄紦瀛樿琛拷?,
        )
    }.getOrNull()
}

internal fun formatSyncTime(time: LocalDateTime): String {
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}

private fun termToJson(term: HitaTerm): JSONObject {
    return JSONObject().apply {
        put("year", term.year)
        put("term", term.term)
        put("name", term.name)
        put("isCurrent", term.isCurrent)
    }
}

private fun termFromJson(json: JSONObject): HitaTerm {
    return HitaTerm(
        year = json.optString("year"),
        term = json.optString("term"),
        name = json.optString("name"),
        isCurrent = json.optBoolean("isCurrent"),
    )
}

private fun weekToJson(week: HitaWeek): JSONObject {
    return JSONObject().apply {
        put("index", week.index)
        put("title", week.title)
    }
}

private fun weekFromJson(json: JSONObject): HitaWeek {
    return HitaWeek(
        index = json.optInt("index"),
        title = json.optString("title"),
    )
}

private fun weekDayToJson(day: HitaWeekDay): JSONObject {
    return JSONObject().apply {
        put("weekDay", day.weekDay)
        put("label", day.label)
        put("date", day.date)
        put("fullDate", day.fullDate)
    }
}

private fun weekDayFromJson(json: JSONObject): HitaWeekDay {
    return HitaWeekDay(
        weekDay = json.optInt("weekDay"),
        label = json.optString("label"),
        date = json.optString("date"),
        fullDate = json.optString("fullDate"),
    )
}

private fun timeSlotToJson(slot: HitaTimeSlot): JSONObject {
    return JSONObject().apply {
        put("majorIndex", slot.majorIndex)
        put("label", slot.label)
        put("timeRange", slot.timeRange)
        put("startSection", slot.startSection)
        put("endSection", slot.endSection)
    }
}

private fun timeSlotFromJson(json: JSONObject): HitaTimeSlot {
    return HitaTimeSlot(
        majorIndex = json.optInt("majorIndex"),
        label = json.optString("label"),
        timeRange = json.optString("timeRange"),
        startSection = json.optInt("startSection"),
        endSection = json.optInt("endSection"),
    )
}

private fun courseToJson(course: HitaCourseBlock): JSONObject {
    return JSONObject().apply {
        put("courseName", course.courseName)
        put("classroom", course.classroom)
        put("teacher", course.teacher)
        put("dayOfWeek", course.dayOfWeek)
        put("majorIndex", course.majorIndex)
        put("accent", course.accent)
    }
}

private fun courseFromJson(json: JSONObject): HitaCourseBlock {
    return HitaCourseBlock(
        courseName = json.optString("courseName"),
        classroom = json.optString("classroom"),
        teacher = json.optString("teacher"),
        dayOfWeek = json.optInt("dayOfWeek"),
        majorIndex = json.optInt("majorIndex"),
        accent = json.optLong("accent"),
    )
}

private fun weekScheduleToJson(schedule: HitaWeekSchedule): JSONObject {
    return JSONObject().apply {
        put("term", termToJson(schedule.term))
        put("week", weekToJson(schedule.week))
        put("days", JSONArray().apply { schedule.days.forEach { put(weekDayToJson(it)) } })
        put("timeSlots", JSONArray().apply { schedule.timeSlots.forEach { put(timeSlotToJson(it)) } })
        put("courses", JSONArray().apply { schedule.courses.forEach { put(courseToJson(it)) } })
    }
}

private fun weekScheduleFromJson(json: JSONObject): HitaWeekSchedule {
    return HitaWeekSchedule(
        term = termFromJson(json.getJSONObject("term")),
        week = weekFromJson(json.getJSONObject("week")),
        days = weekDaysFromJson(json.getJSONArray("days")),
        timeSlots = timeSlotsFromJson(json.getJSONArray("timeSlots")),
        courses = coursesFromJson(json.getJSONArray("courses")),
    )
}

private fun termsFromJson(array: JSONArray): List<HitaTerm> {
    return buildList {
        for (index in 0 until array.length()) add(termFromJson(array.getJSONObject(index)))
    }
}

private fun weeksFromJson(array: JSONArray): List<HitaWeek> {
    return buildList {
        for (index in 0 until array.length()) add(weekFromJson(array.getJSONObject(index)))
    }
}

private fun weekDaysFromJson(array: JSONArray): List<HitaWeekDay> {
    return buildList {
        for (index in 0 until array.length()) add(weekDayFromJson(array.getJSONObject(index)))
    }
}

private fun timeSlotsFromJson(array: JSONArray): List<HitaTimeSlot> {
    return buildList {
        for (index in 0 until array.length()) add(timeSlotFromJson(array.getJSONObject(index)))
    }
}

private fun coursesFromJson(array: JSONArray): List<HitaCourseBlock> {
    return buildList {
        for (index in 0 until array.length()) add(courseFromJson(array.getJSONObject(index)))
    }
}
