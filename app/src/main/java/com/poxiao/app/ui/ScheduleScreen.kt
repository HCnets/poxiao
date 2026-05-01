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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.launch

@Composable
internal fun ScheduleScreen(
    repository: AcademicRepository,
    active: Boolean = true,
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
    var gradeTrendStatus by remember { mutableStateOf("登录后可查看成绩趋势。") }
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
    val todoTasks = remember { mutableStateListOf<TodoTask>() }
    val reviewBlocks = remember(todoTasks, uiState.weekSchedule) {
        buildScheduleReviewBlocks(
            todoTasks,
            uiState.weekSchedule.days,
            uiState.weekSchedule.timeSlots,
            uiState.weekSchedule.courses
        )
    }

    LaunchedEffect(active) {
        if (active) {
            todoTasks.clear()
            todoTasks.addAll(loadTodoTasks(todoPrefs))
        }
    }

    ScheduleBootstrapEffect(
        restored = restored,
        prefs = prefs,
        repository = repository,
        savedStudentId = savedStudentId,
        savedPassword = savedPassword,
        onRestoredChange = { restored = it },
    )
    ScheduleGradeTrendEffect(
        uiState = uiState,
        prefs = prefs,
        selectedTrendTerm = selectedTrendTerm,
        onGradeTrendChange = { gradeTrend = it },
        onGradeTrendLoadingChange = { gradeTrendLoading = it },
        onGradeTrendStatusChange = { gradeTrendStatus = it },
        onSelectedTrendTermChange = { selectedTrendTerm = it },
    )
    ScheduleUiStatePersistenceEffect(
        uiState = uiState,
        prefs = prefs,
        onLastSyncTimeChange = { lastSyncTime = it },
    )
    ScheduleReminderRefreshEffect(
        context = context,
        uiState = uiState,
        extraEvents = extraEvents,
        completedExamWeekIds = completedExamWeekIds,
    )

    ScreenColumn {
        item {
            GlassCard {
                Text("课程中心", style = MaterialTheme.typography.headlineMedium, color = PineInk)
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                if (lastSyncTime.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("上次同步：$lastSyncTime", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.64f))
                }
                if (uiState.authExpired) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "当前登录会话可能已失效，请重新连接教务系统后再刷新。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9A5B34),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { scope.launch { repository.refresh() } },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MossGreen),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.loggedIn && !uiState.loading,
                ) {
                    Text(if (uiState.loading) "正在刷新..." else "立即刷新课表")
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ActionPill("账号", WarmMist, onClick = onOpenAcademicAccount)
                    ActionPill("复制摘要", MossGreen) {
                        val text = buildScheduleShareText(uiState)
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(ClipData.newPlainText("课表摘要", text))
                        exportHint = "已复制当前周课表摘要。"
                    }
                    ActionPill("系统分享", Ginkgo) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, buildScheduleShareText(uiState))
                        }
                        context.startActivity(Intent.createChooser(intent, "分享课表摘要"))
                        exportHint = "已打开系统分享面板。"
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
                        reviewBlocks = reviewBlocks,
                        selectedCourse = selectedCourse,
                        onSelectCourse = { selectedCourse = it },
                        onSelectReview = { block ->
                            if (block.suggestedMajorIndex != null) {
                                val hint = applyScheduleReviewOptimization(context, block)
                                exportHint = hint
                                // 刷新本地列表
                                todoTasks.clear()
                                todoTasks.addAll(loadTodoTasks(todoPrefs))
                            }
                        }
                    )
                    ScheduleMode.Day -> DayScheduleCard(
                        dates = uiState.weekSchedule.days.map { it.fullDate },
                        selectedDate = uiState.selectedDate,
                        courses = uiState.selectedDateCourses,
                        events = selectedDateEvents,
                        reviewBlocks = reviewBlocks,
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
                        onSelectReview = { block ->
                            if (block.suggestedMajorIndex != null) {
                                val hint = applyScheduleReviewOptimization(context, block)
                                exportHint = hint
                                todoTasks.clear()
                                todoTasks.addAll(loadTodoTasks(todoPrefs))
                            }
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
                        activeDates = (
                            uiState.weekSchedule.days.map { it.fullDate } + 
                            extraEvents.map { it.date } +
                            reviewBlocks.map { 
                                java.time.LocalDate.now().plusDays(
                                    (it.dayOfWeek - java.time.LocalDate.now().dayOfWeek.value).toLong()
                                ).toString()
                            }
                        ).distinct(),
                        selectedCourses = uiState.selectedDateCourses,
                        selectedEvents = selectedDateEvents,
                        selectedReviewBlocks = reviewBlocks.filter {
                            val iso = java.time.LocalDate.now().plusDays(
                                (it.dayOfWeek - java.time.LocalDate.now().dayOfWeek.value).toLong()
                            ).toString()
                            iso == uiState.selectedDate
                        },
                        selectedCourse = selectedCourse,
                        onSelectDate = {
                            selectedCourse = null
                            scope.launch { repository.selectDate(it) }
                        },
                        onSelectCourse = { selectedCourse = it },
                        onSelectReview = { block ->
                            if (block.suggestedMajorIndex != null) {
                                val hint = applyScheduleReviewOptimization(context, block)
                                exportHint = hint
                                todoTasks.clear()
                                todoTasks.addAll(loadTodoTasks(todoPrefs))
                            }
                        }
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
                                tags = listOf("考试周"),
                                listName = "学习",
                                reminderText = if (item.priority <= 1) "今天 20:00" else "",
                                repeatText = "不重复",
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
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "考试周")
                    },
                    onBindFocusGroup = { date, groupedItems ->
                        val headline = groupedItems.firstOrNull()?.title ?: "考试周冲刺"
                        focusBridgePrefs.edit()
                            .remove("bound_task_title")
                            .remove("bound_task_list")
                            .apply()
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_title_secure", "$headline 等 ${groupedItems.size} 项")
                        SecurePrefs.putString(focusBridgePrefs, "bound_task_list_secure", "考试周 ${date.substringAfterLast("-")}")
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
                                tags = listOf("考试周", date),
                                listName = "考试周 ${date.substringAfterLast("-")}",
                                reminderText = if (item.priority <= 1) "今天 20:00" else "",
                                repeatText = "不重复",
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
                        } else if (item.id !in completedExamWeekIds) {
                            completedExamWeekIds.add(item.id)
                        }
                        saveStringList(examWeekPrefs, "completed_ids", completedExamWeekIds)
                    },
                    onClearFinished = {
                        completedExamWeekIds.removeAll { id ->
                            examWeekItems.any { it.id == id && it.finished && (it.countdownLabel == "已结束" || it.finished) }
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
                                        courseLabel = "第 ${course.majorIndex} 大节",
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

@Composable
private fun WeekScheduleCard(
    title: String,
    slots: List<HitaTimeSlot>,
    days: List<HitaWeekDay>,
    courses: List<HitaCourseBlock>,
    reviewBlocks: List<ScheduleReviewBlock> = emptyList(),
    selectedCourse: HitaCourseBlock?,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectReview: (ScheduleReviewBlock) -> Unit = {},
) {
    val headerHeight = 60.dp
    val rowHeight = 108.dp
    val freeTimeSummary = remember(slots, days, courses) { weeklyFreeTimeSummary(slots, days, courses) }
    val weeklyAnalysis = remember(days, courses) { weeklyCourseAnalysis(days, courses) }
    GlassCard {
        Text(title, style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(modifier = Modifier.height(headerHeight))
                slots.forEach { slot ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.24f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
                        modifier = Modifier
                            .width(94.dp)
                            .requiredHeight(rowHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
                            Text(slot.label, style = MaterialTheme.typography.labelLarge, color = PineInk)
                            Text(slot.timeRange, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                        }
                    }
                }
            }
            days.forEach { day ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.width(132.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.26f),
                        border = BorderStroke(
                            1.dp,
                            if (selectedCourse?.dayOfWeek == day.weekDay) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.16f),
                        ),
                        modifier = Modifier.requiredHeight(headerHeight),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(day.label, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(day.date, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        }
                    }
                    slots.forEach { slot ->
                        val matches = courses.filter { it.dayOfWeek == day.weekDay && it.majorIndex == slot.majorIndex }
                        val reviews = reviewBlocks.filter { it.dayOfWeek == day.weekDay && it.majorIndex == slot.majorIndex }
                        CourseCell(
                            courses = matches,
                            reviewBlocks = reviews,
                            selectedCourse = selectedCourse,
                            onClick = { course -> onSelectCourse(course) },
                            onReviewClick = onSelectReview
                        )
                    }
                }
            }
        }
        if (weeklyAnalysis.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text("本周课表分析", style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                weeklyAnalysis.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item.highlights.forEach { label ->
                                    SelectionChip(text = label, chosen = false, onClick = {})
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("本周空闲时段", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            freeTimeSummary.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.14f)),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(item.dayLabel, style = MaterialTheme.typography.titleSmall, color = PineInk)
                            Text("${item.freeCount} 个空闲大节", style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.7f))
                        }
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item.labels.forEach { label ->
                                SelectionChip(text = label, chosen = false, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayScheduleCard(
    dates: List<String>,
    selectedDate: String,
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
    reviewBlocks: List<ScheduleReviewBlock> = emptyList(),
    selectedCourse: HitaCourseBlock?,
    selectedEventId: String?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectEvent: (ScheduleExtraEvent) -> Unit,
    onAddEvent: (ScheduleExtraEvent) -> Unit,
    onUpdateEvent: (ScheduleExtraEvent) -> Unit,
    onDeleteEvent: (String) -> Unit,
    onSelectReview: (ScheduleReviewBlock) -> Unit = {},
) {
    val context = LocalContext.current
    val draftPrefs = remember { context.getSharedPreferences("schedule_event_draft", Context.MODE_PRIVATE) }
    var draftTitle by remember(selectedDate) { mutableStateOf("") }
    var draftTime by remember(selectedDate) { mutableStateOf(DefaultScheduleEventTime) }
    var draftType by remember(selectedDate) { mutableStateOf(DefaultScheduleEventType) }
    var draftNote by remember(selectedDate) { mutableStateOf("") }
    var eventHint by remember(selectedDate) { mutableStateOf("") }
    var draftReady by remember(selectedDate) { mutableStateOf(false) }
    val timelineEntries = buildDayTimelineEntries(courses, events)
    val editingEvent = events.firstOrNull { it.id == selectedEventId }
    val selectedDayReviews = reviewBlocks.filter {
        val iso = java.time.LocalDate.now().plusDays(
            (it.dayOfWeek - java.time.LocalDate.now().dayOfWeek.value).toLong()
        ).toString()
        iso == selectedDate
    }

    DayScheduleDraftRestoreEffect(
        selectedDate = selectedDate,
        editingEventId = editingEvent?.id,
        draftPrefs = draftPrefs,
        onDraftTitleChange = { draftTitle = it },
        onDraftTimeChange = { draftTime = it },
        onDraftTypeChange = { draftType = it },
        onDraftNoteChange = { draftNote = it },
        onEventHintChange = { eventHint = it },
        onDraftReadyChange = { draftReady = it },
        findEditingEvent = { id -> events.firstOrNull { it.id == id } },
    )
    DayScheduleDraftPersistenceEffect(
        selectedDate = selectedDate,
        editingEventId = editingEvent?.id,
        draftTitle = draftTitle,
        draftTime = draftTime,
        draftType = draftType,
        draftNote = draftNote,
        draftReady = draftReady,
        draftPrefs = draftPrefs,
    )
    GlassCard {
        Text("当天安排", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(options = dates, selected = selectedDate, label = { it.substringAfterLast("-") }, onSelect = onSelectDate)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (selectedDayReviews.isNotEmpty()) {
            Text("智能复习建议", style = MaterialTheme.typography.titleSmall, color = PineInk)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                selectedDayReviews.forEach { block ->
                    DaySummaryCard(
                        title = block.title,
                        body = "建议时段：第 ${block.majorIndex} 大节",
                        onClick = { onSelectReview(block) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (timelineEntries.isEmpty()) {
            Text("当天暂时没有课程。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
        } else {
            timelineEntries.forEachIndexed { index, entry ->
                val course = entry.selectableCourse
                val extraEvent = entry.extraEvent
                val isSelected = course != null && selectedCourse?.let {
                    it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
                } == true
                val isEventSelected = extraEvent?.id == selectedEventId
                val relationTags = if (course != null) remember(courses, course) { dayCourseTags(courses, course) } else entry.tags
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = if (isSelected || isEventSelected) Color.White.copy(alpha = 0.32f) else Color.White.copy(alpha = 0.24f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected || isEventSelected) Color.White.copy(alpha = 0.26f) else BambooStroke.copy(alpha = 0.16f),
                    ),
                    modifier = Modifier.clickable(enabled = course != null || extraEvent != null) {
                        when {
                            course != null -> onSelectCourse(course)
                            extraEvent != null -> onSelectEvent(extraEvent)
                        }
                    },
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(64.dp)
                                .clip(CircleShape)
                                .background(entry.accent),
                        )
                        Column {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                            Text(entry.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                            Text(entry.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
                            if (relationTags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    relationTags.forEach { tag ->
                                        SelectionChip(text = tag, chosen = false, onClick = {})
                                    }
                                }
                            }
                        }
                    }
                }
                if (index != timelineEntries.lastIndex) Spacer(modifier = Modifier.height(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(if (editingEvent == null) "添加学习事件" else "编辑学习事件", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftTitle, onValueChange = { draftTitle = it }, label = { Text("事件标题") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftTime, onValueChange = { draftTime = it }, label = { Text("时间") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftType, onValueChange = { draftType = it }, label = { Text("类型") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = draftNote, onValueChange = { draftNote = it }, label = { Text("说明") }, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                eventHint = submitScheduleEventDraft(
                    selectedDate = selectedDate,
                    editingEvent = editingEvent,
                    draftTitle = draftTitle,
                    draftTime = draftTime,
                    draftType = draftType,
                    draftNote = draftNote,
                    draftPrefs = draftPrefs,
                    onAddEvent = onAddEvent,
                    onUpdateEvent = onUpdateEvent,
                )
                if (draftTitle.isNotBlank()) {
                    resetScheduleEventDraftState(
                        onDraftTitleChange = { draftTitle = it },
                        onDraftTimeChange = { draftTime = it },
                        onDraftTypeChange = { draftType = it },
                        onDraftNoteChange = { draftNote = it },
                    )
                }
            },
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (editingEvent == null) "加入当天时间线" else "保存修改")
        }
        if (editingEvent != null) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    eventHint = deleteScheduleEventDraft(
                        editingEvent = editingEvent,
                        draftPrefs = draftPrefs,
                        onDeleteEvent = onDeleteEvent,
                    )
                    resetScheduleEventDraftState(
                        onDraftTitleChange = { draftTitle = it },
                        onDraftTimeChange = { draftTime = it },
                        onDraftTypeChange = { draftType = it },
                        onDraftNoteChange = { draftNote = it },
                    )
                },
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除事件")
            }
        }
        if (eventHint.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = eventHint,
                style = MaterialTheme.typography.bodyMedium,
                color = ForestDeep.copy(alpha = 0.76f),
            )
        }
    }
}

@Composable
private fun GradeTrendCard(
    loading: Boolean,
    status: String,
    points: List<GradeTrendPoint>,
    selectedTerm: String?,
    onSelectTerm: (String) -> Unit,
) {
    val selectedPoint = points.firstOrNull { it.termName == selectedTerm } ?: points.firstOrNull()
    var courseKeyword by remember(selectedPoint?.termName) { mutableStateOf("") }
    val bestPoint = points.maxByOrNull { it.averageScore }
    val riskyPoint = points.maxByOrNull {
        val total = it.courseCount.takeIf { count -> count > 0 } ?: 1
        (it.warningCount.toDouble() / total) + if (it.warningCount > 0) 1.0 else 0.0
    }
    var detailExpanded by remember(selectedPoint?.termName) { mutableStateOf(false) }
    val visibleCards = remember(selectedPoint?.termName, selectedPoint?.rawCards, courseKeyword) {
        val cards = selectedPoint?.rawCards.orEmpty()
        if (courseKeyword.isBlank()) cards else cards.filter {
            listOf(it.title, it.source, it.description).any { text -> text.contains(courseKeyword, ignoreCase = true) }
        }
    }
    GlassCard {
        Text("成绩趋势", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text(status, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        if (loading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("正在整理平均分、绩点和预警课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.68f))
            return@GlassCard
        }
        if (points.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("当前还没有可用于分析的成绩记录。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.7f))
            return@GlassCard
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = points, selected = selectedPoint ?: points.first(), label = { it.termName }, onSelect = { onSelectTerm(it.termName) })
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("平均分", formatTrendValue(selectedPoint?.averageScore), ForestGreen)
            MetricCard("平均绩点", formatTrendValue(selectedPoint?.averageGradePoint), Ginkgo)
            MetricCard("学分", formatTrendValue(selectedPoint?.credits), MossGreen)
            MetricCard("优秀率", formatPercent(selectedPoint?.excellentCount, selectedPoint?.courseCount), TeaGreen)
            MetricCard("预警率", formatPercent(selectedPoint?.warningCount, selectedPoint?.courseCount), WarmMist)
        }
        if (bestPoint != null || riskyPoint != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                bestPoint?.let { point ->
                    TrendInsightCard(
                        title = "最佳学期",
                        headline = point.termName,
                        body = "平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}",
                        accent = ForestGreen,
                    )
                }
                riskyPoint?.let { point ->
                    TrendInsightCard(
                        title = "风险提醒",
                        headline = point.termName,
                        body = if (point.warningCount > 0) "共有 ${point.warningCount} 门预警课程，建议优先复盘。" else "当前没有明显预警课程，可继续保持。",
                        accent = WarmMist,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("学期对比", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        val maxScore = points.maxOfOrNull { it.averageScore }?.takeIf { it > 0.0 } ?: 100.0
        points.forEachIndexed { index, point ->
            val scoreRatio = (point.averageScore / maxScore).coerceIn(0.08, 1.0).toFloat()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(point.termName, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.76f))
                    Text(formatTrendValue(point.averageScore), style = MaterialTheme.typography.labelLarge, color = PineInk)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(scoreRatio)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (point.termName == selectedPoint?.termName) ForestGreen else MossGreen),
                    )
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "课程 ${selectedPoint?.courseCount ?: 0} 门 · 优秀 ${selectedPoint?.excellentCount ?: 0} 门 · 预警 ${selectedPoint?.warningCount ?: 0} 门",
            style = MaterialTheme.typography.bodyLarge,
            color = ForestDeep.copy(alpha = 0.76f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ActionPill(if (detailExpanded) "收起详情" else "展开单科", if (detailExpanded) MossGreen else ForestGreen) {
                detailExpanded = !detailExpanded
            }
        }
        if (detailExpanded && selectedPoint != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = courseKeyword,
                onValueChange = { courseKeyword = it },
                label = { Text("搜索当前学期课程") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (visibleCards.isEmpty()) {
                Text("当前搜索条件下没有匹配课程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.7f))
            }
            visibleCards.forEachIndexed { index, card ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.26f)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(card.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text(card.source, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                        Text(card.description, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                    }
                }
                if (index != visibleCards.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        points.forEachIndexed { index, point ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (point.termName == selectedPoint?.termName) Color.White.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, if (point.termName == selectedPoint?.termName) ForestGreen.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.08f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(point.termName, style = MaterialTheme.typography.titleMedium, color = PineInk)
                        Text("平均分 ${formatTrendValue(point.averageScore)} · 绩点 ${formatTrendValue(point.averageGradePoint)}", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                    }
                    Text("${point.warningCount} 项预警", style = MaterialTheme.typography.labelLarge, color = if (point.warningCount > 0) Color(0xFF9A5B34) else ForestDeep.copy(alpha = 0.68f))
                }
            }
            if (index != points.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExamWeekModeCard(
    weekTitle: String,
    selectedDate: String,
    items: List<ExamWeekItem>,
    onCreateTodo: (ExamWeekItem) -> Unit,
    onBindFocus: (ExamWeekItem) -> Unit,
    onBindFocusGroup: (String, List<ExamWeekItem>) -> Unit,
    onCreateTodoGroup: (String, List<ExamWeekItem>) -> Unit,
    onToggleFinishedGroup: (List<ExamWeekItem>, Boolean) -> Unit,
    onToggleFinished: (ExamWeekItem) -> Unit,
    onClearFinished: () -> Unit,
) {
    var filter by remember { mutableStateOf(ExamWeekFilter.All) }
    var typeFilter by remember { mutableStateOf(ExamWeekTypeFilter.All) }
    val collapsedDates = remember { mutableStateListOf<String>() }
    val filteredItems = remember(items, filter, typeFilter) {
        val base = when (filter) {
            ExamWeekFilter.All -> items
            ExamWeekFilter.Pending -> items.filterNot { it.finished }
            ExamWeekFilter.Urgent -> items.filter { !it.finished && (it.priority <= 1 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
            ExamWeekFilter.Finished -> items.filter { it.finished }
        }
        base.filter { item ->
            when (typeFilter) {
                ExamWeekTypeFilter.All -> true
                ExamWeekTypeFilter.Exam -> item.subtitle.contains("考试") || item.title.contains("考试")
                ExamWeekTypeFilter.Assignment -> item.subtitle.contains("作业") || item.title.contains("作业")
                ExamWeekTypeFilter.Review -> item.subtitle.contains("复习") || item.title.contains("复习")
            }
        }
    }
    val grouped = filteredItems
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
        .groupBy { it.date }
    val urgentPending = items.firstOrNull { !it.finished && (it.priority == 0 || it.countdownLabel == "今天" || it.countdownLabel == "明天") }
    GlassCard {
        Text("考试周模式", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Text("把考试、作业和本周课程压成一条冲刺视图，便于临近考试时快速排程。", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            MetricCard("当前周", weekTitle, ForestGreen)
            MetricCard("冲刺项", items.size.toString(), Ginkgo)
            MetricCard("今日", selectedDate.substringAfterLast("-"), MossGreen)
            MetricCard("已完成", items.count { it.finished }.toString(), TeaGreen)
        }
        urgentPending?.let { item ->
            Spacer(modifier = Modifier.height(14.dp))
            TrendInsightCard(
                title = "临近事项",
                headline = item.title,
                body = "${item.countdownLabel} · ${item.subtitle}",
                accent = item.accent,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(
            options = ExamWeekFilter.entries.toList(),
            selected = filter,
            label = { it.title },
            onSelect = { filter = it },
        )
        Spacer(modifier = Modifier.height(10.dp))
        SelectionRow(
            options = ExamWeekTypeFilter.entries.toList(),
            selected = typeFilter,
            label = { it.title },
            onSelect = { typeFilter = it },
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (items.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ActionPill("清空已完成", WarmMist, onClick = onClearFinished)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (filteredItems.isEmpty()) {
            Text("当前还没有考试、作业或复习事件。可以先在日视图里加入“考试”或“作业”事件。", style = MaterialTheme.typography.bodyLarge, color = ForestDeep.copy(alpha = 0.72f))
        } else {
            grouped.entries.forEachIndexed { index, entry ->
                val collapsed = entry.key in collapsedDates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        entry.key,
                        modifier = Modifier.clickable {
                            if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = PineInk,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (collapsed) "展开 ${entry.value.size} 项" else "收起 ${entry.value.size} 项",
                            modifier = Modifier.clickable {
                                if (collapsed) collapsedDates.remove(entry.key) else collapsedDates.add(entry.key)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.72f),
                        )
                        Text(
                            "整组转待办",
                            modifier = Modifier.clickable { onCreateTodoGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = PineInk,
                        )
                        Text(
                            "整组绑定专注",
                            modifier = Modifier.clickable { onBindFocusGroup(entry.key, entry.value) },
                            style = MaterialTheme.typography.labelMedium,
                            color = ForestDeep.copy(alpha = 0.82f),
                        )
                        Text(
                            if (entry.value.all { it.finished }) "整组恢复" else "整组完成",
                            modifier = Modifier.clickable {
                                onToggleFinishedGroup(entry.value, !entry.value.all { it.finished })
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.value.all { it.finished }) ForestDeep.copy(alpha = 0.74f) else TeaGreen.copy(alpha = 0.92f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!collapsed) {
                    entry.value.forEachIndexed { itemIndex, item ->
                        Surface(shape = RoundedCornerShape(22.dp), color = if (item.finished) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.34f)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(24.dp).height(4.dp).clip(CircleShape).background(item.accent))
                                        Text(item.title, style = MaterialTheme.typography.titleMedium, color = if (item.finished) ForestDeep.copy(alpha = 0.58f) else PineInk)
                                    }
                                    SelectionChip(text = if (item.finished) "已完成" else item.countdownLabel, chosen = item.priority == 0 || item.finished, onClick = {})
                                }
                                Text(
                                    when (item.priority) {
                                        0 -> "最高优先"
                                        1 -> "临近处理"
                                        else -> "常规安排"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ForestDeep.copy(alpha = 0.68f),
                                )
                                Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
                                Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    ActionPill(if (item.finished) "恢复" else "完成", if (item.finished) WarmMist else TeaGreen) { onToggleFinished(item) }
                                    ActionPill("转待办", MossGreen) { onCreateTodo(item) }
                                    ActionPill("绑定专注", ForestGreen) { onBindFocus(item) }
                                }
                            }
                        }
                        if (itemIndex != entry.value.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.18f)) {
                        Text(
                            "当前分组已折叠，保留 ${entry.value.size} 项摘要。",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestDeep.copy(alpha = 0.7f),
                        )
                    }
                }
                if (index != grouped.entries.size - 1) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MonthScheduleCard(
    month: YearMonth,
    selectedDate: String,
    activeDates: List<String>,
    selectedCourses: List<HitaCourseBlock>,
    selectedEvents: List<ScheduleExtraEvent>,
    selectedReviewBlocks: List<ScheduleReviewBlock> = emptyList(),
    selectedCourse: HitaCourseBlock?,
    onSelectDate: (String) -> Unit,
    onSelectCourse: (HitaCourseBlock) -> Unit,
    onSelectReview: (ScheduleReviewBlock) -> Unit = {},
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value % 7
    val cells = remember(month) {
        val values = mutableListOf<LocalDate?>()
        repeat(leading) { values.add(null) }
        for (day in 1..month.lengthOfMonth()) {
            values.add(month.atDay(day))
        }
        values
    }
    val rows = remember(cells) { cells.chunked(7) }

    GlassCard {
        Text("${month.year} 年 ${month.monthValue} 月", style = MaterialTheme.typography.titleLarge, color = PineInk)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { label ->
                Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = ForestDeep.copy(alpha = 0.68f))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { date ->
                    val iso = date?.toString().orEmpty()
                    val selected = iso == selectedDate
                    val active = iso in activeDates
                    val isToday = iso == LocalDate.now().toString()
                    MonthCell(
                        date = date,
                        selected = selected,
                        active = active,
                        isToday = isToday,
                        onClick = {
                            if (date != null) onSelectDate(date.toString())
                        },
                    )
                }
                repeat(7 - row.size) {
                    Spacer(modifier = Modifier.width(40.dp).height(52.dp))
                }
            }
            if (rowIndex != rows.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text("当日详情", style = MaterialTheme.typography.titleMedium, color = PineInk)
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (selectedCourses.isEmpty() && selectedEvents.isEmpty() && selectedReviewBlocks.isEmpty()) {
                DaySummaryCard(title = "暂无课程", body = "选中的日期当前没有返回课程安排。")
            } else {
                selectedCourses.forEach { course ->
                    DaySummaryCard(
                        title = course.courseName,
                        body = "${course.classroom} · ${course.teacher.ifBlank { "教师待补充" }}",
                        selected = selectedCourse?.let {
                            it.courseName == course.courseName && it.dayOfWeek == course.dayOfWeek && it.majorIndex == course.majorIndex
                        } == true,
                        onClick = { onSelectCourse(course) },
                    )
                }
                selectedEvents.forEach { event ->
                    DaySummaryCard(
                        title = event.title,
                        body = "${event.time} · ${event.type}${if (event.note.isBlank()) "" else " · ${event.note}"}",
                    )
                }
                selectedReviewBlocks.forEach { block ->
                    DaySummaryCard(
                        title = block.title,
                        body = "智能复习 · 第 ${block.majorIndex} 大节",
                        onClick = { onSelectReview(block) }
                    )
                }
            }
        }
    }
}
