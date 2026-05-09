package com.poxiao.app.review

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.notes.CourseNote
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoSubtask
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.LiquidGlassCard
import com.poxiao.app.ui.theme.PoxiaoPalette
import com.poxiao.app.ui.theme.PoxiaoThemeState
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.max

private val ReviewIntervals = intArrayOf(0, 1, 3, 7, 14, 30)

enum class ReviewFilter(val label: String) {
    Today("今日应复习"),
    Upcoming("即将到期"),
    Mastered("已形成记忆"),
    All("全部知识点"),
}

data class ReviewPlannerSeed(
    val query: String = "",
    val focusTitle: String = "",
)

private data class ReviewExecutionHint(
    val executedAt: Long,
    val summary: String,
    val isReplay: Boolean,
)

private enum class ReviewConfidence(val label: String) {
    Weak("模糊"),
    Normal("一般"),
    Strong("熟悉"),
}

private data class ReviewFreeSlotSuggestion(
    val dayLabel: String,
    val date: String,
    val slotLabel: String,
    val timeRange: String,
)

private data class ReviewTrendPoint(
    val label: String,
    val total: Int,
    val completed: Int,
)

private data class ReviewCalendarDay(
    val date: LocalDate,
    val dueCount: Int,
    val completedCount: Int,
    val overdueCount: Int,
)

private data class ReviewCoursePortrait(
    val courseName: String,
    val totalCount: Int,
    val dueCount: Int,
    val averageMastery: Float,
    val plannedMinutes: Int,
    val importantCount: Int,
    val coreCount: Int,
    val errorCount: Int,
    val qualityScore: Int,
)

private data class ReviewCourseRetrospective(
    val courseName: String,
    val dueCount: Int,
    val errorCount: Int,
    val coreCount: Int,
    val plannedMinutes: Int,
    val averageMastery: Float,
    val focusItem: ReviewItem?,
)

private data class ReviewExamSignal(
    val title: String,
    val type: String,
    val date: LocalDate,
)

private enum class ReviewExamSprintFilter(val label: String) {
    All("全部"),
    Exam("只看考试"),
    Assignment("只看作业"),
    Review("只看复习"),
}

private enum class ReviewSprintTemplate(val label: String) {
    Balanced("均衡推进"),
    ExamRush("考试冲刺"),
    ErrorRepair("错题回炉"),
    QuickRecall("快速回顾"),
    Custom("自定义模板"),
}

enum class ReviewImportance(val label: String, val score: Int) {
    Standard("常规", 0),
    Important("重点", 12),
    Core("核心", 24),
}

data class ReviewItem(
    val id: String,
    val noteId: String,
    val sourceTitle: String,
    val courseName: String,
    val noteTitle: String,
    val teacher: String,
    val tags: List<String>,
    val excerpt: String,
    val noteUpdatedAt: Long,
    val createdAt: Long,
    val stageIndex: Int,
    val reviewCount: Int,
    val lastReviewedAt: Long,
    val nextReviewAt: Long,
    val mastery: Float,
    val importance: ReviewImportance = ReviewImportance.Standard,
    val masteryBias: Float = 0f,
    val errorProne: Boolean = false,
    val recommendedMinutes: Int,
    val assistantExecutionAt: Long = 0L,
)

class ReviewPlannerStore(context: Context) {
    private val prefs = context.getSharedPreferences("review_planner", Context.MODE_PRIVATE)

    fun loadItems(): List<ReviewItem> {
        val raw = prefs.getString("review_items_v1", "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            val list = mutableListOf<ReviewItem>()
            var index = 0
            while (index < array.length()) {
                val item = array.optJSONObject(index)
                if (item != null) {
                    val tagArray = item.optJSONArray("tags") ?: JSONArray()
                    val tags = mutableListOf<String>()
                    var tagIndex = 0
                    while (tagIndex < tagArray.length()) {
                        val tag = tagArray.optString(tagIndex)
                        if (tag.isNotBlank()) tags += tag
                        tagIndex += 1
                    }
                    list += ReviewItem(
                        id = item.optString("id"),
                        noteId = item.optString("noteId"),
                        sourceTitle = item.optString("sourceTitle").ifBlank { item.optString("noteTitle") },
                        courseName = item.optString("courseName"),
                        noteTitle = item.optString("noteTitle"),
                        teacher = item.optString("teacher"),
                        tags = tags,
                        excerpt = item.optString("excerpt"),
                        noteUpdatedAt = item.optLong("noteUpdatedAt"),
                        createdAt = item.optLong("createdAt"),
                        stageIndex = item.optInt("stageIndex"),
                        reviewCount = item.optInt("reviewCount"),
                        lastReviewedAt = item.optLong("lastReviewedAt"),
                        nextReviewAt = item.optLong("nextReviewAt"),
                        mastery = item.optDouble("mastery").toFloat(),
                        importance = runCatching {
                            ReviewImportance.valueOf(item.optString("importance", ReviewImportance.Standard.name))
                        }.getOrDefault(ReviewImportance.Standard),
                        masteryBias = item.optDouble("masteryBias").toFloat(),
                        errorProne = item.optBoolean("errorProne"),
                        recommendedMinutes = item.optInt("recommendedMinutes", 25),
                        assistantExecutionAt = item.optLong("assistantExecutionAt"),
                    )
                }
                index += 1
            }
            normalizeReviewItems(list)
        }.getOrDefault(emptyList())
    }

    fun saveItems(items: List<ReviewItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("noteId", item.noteId)
                    put("sourceTitle", item.sourceTitle)
                    put("courseName", item.courseName)
                    put("noteTitle", item.noteTitle)
                    put("teacher", item.teacher)
                    put("tags", JSONArray(item.tags))
                    put("excerpt", item.excerpt)
                    put("noteUpdatedAt", item.noteUpdatedAt)
                    put("createdAt", item.createdAt)
                    put("stageIndex", item.stageIndex)
                    put("reviewCount", item.reviewCount)
                    put("lastReviewedAt", item.lastReviewedAt)
                    put("nextReviewAt", item.nextReviewAt)
                    put("mastery", item.mastery.toDouble())
                    put("importance", item.importance.name)
                    put("masteryBias", item.masteryBias.toDouble())
                    put("errorProne", item.errorProne)
                    put("recommendedMinutes", item.recommendedMinutes)
                    put("assistantExecutionAt", item.assistantExecutionAt)
                },
            )
        }
        prefs.edit()
            .putString("review_items_v1", array.toString())
            .putLong("last_sync_v1", System.currentTimeMillis())
            .apply()
    }

    fun loadLastSyncAt(): Long = prefs.getLong("last_sync_v1", 0L)

    fun loadLastSuggestion(): String = prefs.getString("last_review_suggestion_v1", "").orEmpty()

    fun saveLastSuggestion(value: String) {
        prefs.edit().putString("last_review_suggestion_v1", value).apply()
    }
}

@Composable
fun ReviewPlannerScreen(
    modifier: Modifier = Modifier,
    initialSeed: ReviewPlannerSeed? = null,
    onOpenAssistantHistory: (Long) -> Unit = {},
    onOpenCourseNoteSource: (CourseNoteSeed) -> Unit = {},
    onOpenExportCenter: () -> Unit = {},
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val palette = PoxiaoThemeState.palette
    val noteStore = remember { CourseNoteStore(context) }
    val reviewStore = remember { ReviewPlannerStore(context) }
    val focusPrefs = remember { context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE) }
    val todoPrefs = remember { context.getSharedPreferences("todo_board", Context.MODE_PRIVATE) }
    val scheduleAuthPrefs = remember { context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE) }
    val scheduleCachePrefs = remember { context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE) }
    val notes = remember { mutableStateListOf<CourseNote>() }
    val reviewItems = remember { mutableStateListOf<ReviewItem>() }
    var filter by remember { mutableStateOf(ReviewFilter.Today) }
    var searchText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("系统会根据课程笔记自动生成复习项，并按记忆节奏安排下一次回顾。") }
    var selectedCourse by remember { mutableStateOf("全部课程") }
    var lastSyncAt by remember { mutableStateOf(reviewStore.loadLastSyncAt()) }
    var expandedCourse by remember { mutableStateOf<String?>(null) }
    var hasTouchedExpansion by remember { mutableStateOf(false) }
    var lastSuggestion by remember { mutableStateOf(reviewStore.loadLastSuggestion()) }
    var examSprintFilter by remember { mutableStateOf(ReviewExamSprintFilter.All) }
    var sprintTemplate by remember { mutableStateOf(ReviewSprintTemplate.Balanced) }
    var highlightedTitle by remember { mutableStateOf(initialSeed?.focusTitle.orEmpty()) }
    var batchTagText by remember { mutableStateOf("") }
    var customSprintFocus by remember { mutableStateOf("错题优先") }
    var customSprintLimit by remember { mutableStateOf(4) }
    var selectedReviewDate by remember { mutableStateOf<LocalDate?>(null) }

    fun persist(updated: List<ReviewItem>) {
        val normalized = normalizeReviewItems(updated)
        reviewItems.clear()
        reviewItems.addAll(normalized)
        reviewStore.saveItems(reviewItems)
        lastSyncAt = reviewStore.loadLastSyncAt()
        updateReviewBridgeProgress(context, reviewItems.toList())
    }

    fun syncFromNotes(showHint: Boolean = false) {
        val latestNotes = runCatching {
            noteStore.loadNotes().sortedByDescending { it.updatedAt }
        }.getOrElse {
            statusText = "课程笔记读取失败，已保留当前复习计划。"
            return
        }
        notes.clear()
        notes.addAll(latestNotes)
        val synced = runCatching {
            syncReviewItems(latestNotes, reviewItems.toList())
        }.getOrElse {
            statusText = "复习计划同步失败，已保留上一次可用结果。"
            return
        }
        persist(synced)
        if (showHint) {
            statusText = if (latestNotes.isEmpty()) {
                "还没有课程笔记，先去课程笔记里整理知识点，这里会自动生成复习计划。"
            } else {
                "已同步 ${latestNotes.size} 份课程笔记，当前共有 ${synced.size} 个复习项。"
            }
        }
    }

    LaunchedEffect(Unit) {
        val existing = runCatching { reviewStore.loadItems() }.getOrDefault(emptyList())
        reviewItems.clear()
        reviewItems.addAll(existing)
        syncFromNotes(showHint = false)
    }

    val courseOptions = remember(reviewItems.toList()) {
        val options = mutableListOf("全部课程")
        reviewItems.mapTo(linkedSetOf()) { it.courseName }.forEach { options += it }
        options
    }
    val examSignals = remember {
        loadReviewExamSignals(scheduleAuthPrefs, scheduleCachePrefs)
    }
    val scopedItems = remember(reviewItems.toList(), filter, searchText, selectedCourse, examSignals, selectedReviewDate) {
        val now = System.currentTimeMillis()
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(defaultZoneId()).toInstant().toEpochMilli()
        reviewItems
            .filter { selectedCourse == "全部课程" || it.courseName == selectedCourse }
            .filter { item ->
                searchText.isBlank() ||
                    item.courseName.contains(searchText, true) ||
                    item.noteTitle.contains(searchText, true) ||
                    item.excerpt.contains(searchText, true) ||
                    item.tags.any { tag -> tag.contains(searchText, true) }
            }
            .filter { item ->
                when (filter) {
                    ReviewFilter.Today -> item.nextReviewAt < tomorrowStart
                    ReviewFilter.Upcoming -> item.nextReviewAt >= tomorrowStart && item.nextReviewAt <= now + 3L * 24L * 60L * 60L * 1000L
                    ReviewFilter.Mastered -> item.stageIndex >= ReviewIntervals.lastIndex && effectiveMastery(item) >= 0.92f
                    ReviewFilter.All -> true
                }
            }
            .filter { item ->
                selectedReviewDate == null ||
                    Instant.ofEpochMilli(item.nextReviewAt).atZone(defaultZoneId()).toLocalDate() == selectedReviewDate
            }
            .sortedWith(
                compareByDescending<ReviewItem> { reviewPriorityScore(it, examSignals) }
                    .thenBy { it.nextReviewAt }
                    .thenBy { it.courseName },
            )
    }
    val todayCount = remember(reviewItems.toList()) {
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(defaultZoneId()).toInstant().toEpochMilli()
        reviewItems.count { it.nextReviewAt < tomorrowStart }
    }
    val todayReviewItems = remember(reviewItems.toList()) {
        val tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay(defaultZoneId()).toInstant().toEpochMilli()
        reviewItems.filter { it.nextReviewAt < tomorrowStart }
            .sortedBy { it.nextReviewAt }
    }
    val completedTodayCount = remember(reviewItems.toList()) {
        val today = LocalDate.now()
        reviewItems.count { item ->
            item.lastReviewedAt > 0 && Instant.ofEpochMilli(item.lastReviewedAt).atZone(defaultZoneId()).toLocalDate() == today
        }
    }
    val urgentItem = remember(reviewItems.toList()) {
        reviewItems.filter { it.nextReviewAt <= System.currentTimeMillis() }.minByOrNull { it.nextReviewAt }
    }
    val groupedItems = remember(scopedItems) { scopedItems.groupBy { it.courseName } }
    val activeExpandedCourse = when {
        expandedCourse != null -> expandedCourse
        !hasTouchedExpansion -> groupedItems.keys.firstOrNull()
        else -> null
    }
    val overdueCount = remember(reviewItems.toList()) { reviewItems.count { it.nextReviewAt <= System.currentTimeMillis() } }
    val reviewMinutesToday = remember(scopedItems) { scopedItems.map { it.recommendedMinutes }.sum() }
    val recentReviewCount = remember(reviewItems.toList()) {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        reviewItems.count { it.lastReviewedAt >= sevenDaysAgo }
    }
    val completionRate = remember(todayCount, completedTodayCount) {
        if (todayCount <= 0) 1f else completedTodayCount.toFloat() / todayCount.toFloat()
    }
    val coverageCount = remember(reviewItems.toList()) { reviewItems.map { it.courseName }.distinct().size }
    val freeSlots = remember(reviewItems.toList()) {
        loadReviewFreeSlots(scheduleAuthPrefs, scheduleCachePrefs).take(3)
    }
    val upcomingFreeSlotTimes = remember(freeSlots) {
        freeSlots.mapNotNull { slot ->
            val startAt = parseReviewFreeSlotStart(slot) ?: return@mapNotNull null
            if (startAt <= System.currentTimeMillis() + 5L * 60L * 1000L) return@mapNotNull null
            slot to startAt
        }
    }
    val reviewHistoryHints = remember(reviewItems.toList()) {
        loadReviewExecutionHints(context, reviewItems.toList())
    }
    val trendPoints = remember(reviewItems.toList()) {
        buildReviewTrend(reviewItems.toList())
    }
    val coursePortraits = remember(reviewItems.toList()) {
        buildCoursePortraits(reviewItems.toList()).take(4)
    }
    val coursePortraitMap = remember(reviewItems.toList()) {
        buildCoursePortraits(reviewItems.toList()).associateBy { it.courseName }
    }
    val courseRetrospectives = remember(reviewItems.toList(), examSignals) {
        buildCourseRetrospectives(reviewItems.toList(), examSignals)
    }
    val errorProneItems = remember(reviewItems.toList(), examSignals) {
        reviewItems
            .filter { it.errorProne }
            .sortedWith(
                compareByDescending<ReviewItem> { reviewPriorityScore(it, examSignals) }
                    .thenBy { it.nextReviewAt },
            )
            .take(5)
    }
    val reviewQualitySummary = remember(reviewItems.toList()) {
        buildMap<String, Int> {
            put("core", reviewItems.count { it.importance == ReviewImportance.Core })
            put("important", reviewItems.count { it.importance == ReviewImportance.Important })
            put("standard", reviewItems.count { it.importance == ReviewImportance.Standard })
            put("error", reviewItems.count { it.errorProne })
        }
    }
    val smartSuggestion = remember(scopedItems, examSignals) {
        scopedItems.firstOrNull()?.let { item ->
            buildReviewPriorityReason(item, examSignals)
        }
    }
    val topRecommendedItems = remember(scopedItems) { scopedItems.take(3) }
    val examLinkedItems = remember(scopedItems, examSignals) {
        scopedItems.filter { findMatchingExamSignal(it, examSignals) != null }
    }
    val filteredExamLinkedItems = remember(examLinkedItems, examSignals, examSprintFilter, sprintTemplate, customSprintFocus, customSprintLimit) {
        val filtered = examLinkedItems.filter { item ->
            when (examSprintFilter) {
                ReviewExamSprintFilter.All -> true
                ReviewExamSprintFilter.Exam -> findMatchingExamSignal(item, examSignals)?.type == "考试"
                ReviewExamSprintFilter.Assignment -> findMatchingExamSignal(item, examSignals)?.type == "作业"
                ReviewExamSprintFilter.Review -> findMatchingExamSignal(item, examSignals)?.type == "复习"
            }
        }
        applySprintTemplate(filtered, examSignals, sprintTemplate, customSprintFocus, customSprintLimit)
    }
    val urgentExamLinkedItem = remember(filteredExamLinkedItems, examSignals) {
        filteredExamLinkedItems.firstOrNull()?.let { item ->
            item to findMatchingExamSignal(item, examSignals)
        }
    }
    val examSprintTotalMinutes = remember(filteredExamLinkedItems) {
        filteredExamLinkedItems.fold(0) { acc, item -> acc + item.recommendedMinutes }
    }
    val urgentExamLinkedCount = remember(filteredExamLinkedItems, examSignals) {
        filteredExamLinkedItems.count { item ->
            val signal = findMatchingExamSignal(item, examSignals) ?: return@count false
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), signal.date).toInt() <= 1
        }
    }
    val todaySprintItems = remember(filteredExamLinkedItems, examSignals) {
        filteredExamLinkedItems
            .sortedBy { item -> findMatchingExamSignal(item, examSignals)?.date ?: LocalDate.MAX }
            .take(3)
    }
    val sprintErrorCount = remember(filteredExamLinkedItems) { filteredExamLinkedItems.count { it.errorProne } }
    val sprintCoreCount = remember(filteredExamLinkedItems) { filteredExamLinkedItems.count { it.importance == ReviewImportance.Core } }
    val topErrorSources = remember(errorProneItems) {
        errorProneItems.groupBy { "${it.courseName} · ${it.sourceTitle}" }
            .map { (source, items) -> source to items.size }
            .sortedByDescending { it.second }
            .take(4)
    }
    val reviewTagPortrait = remember(reviewItems.toList()) {
        reviewItems.flatMap { it.tags }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(8)
    }
    val reviewCalendarDays = remember(reviewItems.toList()) {
        buildReviewCalendarDays(reviewItems.toList())
    }
    val selectedCalendarDay = remember(reviewCalendarDays, selectedReviewDate) {
        reviewCalendarDays.firstOrNull { it.date == selectedReviewDate }
    }
    val errorFocusItems = remember(errorProneItems, examSignals) {
        errorProneItems
            .sortedWith(
                compareByDescending<ReviewItem> { reviewPriorityScore(it, examSignals) }
                    .thenBy { it.nextReviewAt },
            )
            .take(4)
    }
    val errorFocusMinutes = remember(errorFocusItems) {
        errorFocusItems.fold(0) { acc, item -> acc + max(item.recommendedMinutes, 20) }
    }

    LaunchedEffect(initialSeed?.query, initialSeed?.focusTitle, reviewItems.size) {
        val seed = initialSeed ?: return@LaunchedEffect
        val normalizedFocus = seed.focusTitle.removePrefix("复习：").ifBlank { seed.query }.trim()
        filter = ReviewFilter.All
        selectedCourse = "全部课程"
        searchText = normalizedFocus
        highlightedTitle = normalizedFocus
        val matchedCourse = reviewItems.firstOrNull {
            it.noteTitle.contains(normalizedFocus, true) || it.courseName.contains(normalizedFocus, true)
        }?.courseName
        if (!matchedCourse.isNullOrBlank()) {
            hasTouchedExpansion = true
            expandedCourse = matchedCourse
            statusText = "已根据接管历史定位到“$normalizedFocus”对应的复习项。"
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 112.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ReviewGlassCard {
                    Text("复习计划", style = MaterialTheme.typography.headlineMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "课程笔记会自动拆成复习项，并按照记忆间隔安排下一轮。你只管每天推进，系统负责节奏。",
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.softText,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "今日应复习",
                            value = "$todayCount 项",
                            accent = palette.primary,
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "今日已完成",
                            value = "$completedTodayCount 项",
                            accent = palette.secondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "遗忘风险",
                            value = "$overdueCount 项",
                            accent = Color(0xFFD45B4A),
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "建议时长",
                            value = "$reviewMinutesToday 分钟",
                            accent = palette.primary.copy(alpha = 0.92f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    ReviewInsightCard(
                        title = urgentItem?.noteTitle ?: "今天节奏平稳",
                        body = urgentItem?.let {
                            "最该先复习的是 ${it.courseName} · ${it.noteTitle}，当前处于 ${stageLabel(it)}，建议先投入 ${it.recommendedMinutes} 分钟推进。"
                        } ?: "当前没有逾期项目，继续按计划推进即可。",
                        value = urgentItem?.let { dueLabel(it.nextReviewAt) } ?: "已清空",
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { syncFromNotes(showHint = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
                        ) {
                            Text("同步课程笔记")
                        }
                        Button(
                            onClick = {
                                val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                scopedItems.take(6).forEach { item ->
                                    if (tasks.none { it.id == "review-${item.id}" }) {
                                        tasks.add(
                                            0,
                                            buildReviewTodoTask(item),
                                        )
                                    }
                                }
                                saveReviewTodoTasks(todoPrefs, tasks)
                                statusText = "已把今日复习中的 ${scopedItems.take(6).size} 项加入待办，方便统一推进。"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.ink),
                        ) {
                            Text("批量加入今日计划")
                        }
                        Button(
                            onClick = {
                                val title = urgentItem?.let { "复习：${it.courseName} 今日计划" } ?: "复习：今日计划"
                                SecurePrefs.putString(focusPrefs, "bound_task_title_secure", title)
                                SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "复习计划")
                                statusText = "已把今日复习批量绑定到番茄钟，可按建议时长开始推进。"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.cardGlow.copy(alpha = 0.9f), contentColor = palette.ink),
                        ) {
                            Text("整组绑定专注")
                        }
                        Button(
                            onClick = {
                                filter = ReviewFilter.Today
                                selectedCourse = "全部课程"
                                searchText = ""
                                hasTouchedExpansion = false
                                expandedCourse = scopedItems.firstOrNull()?.courseName
                                statusText = smartSuggestion?.let {
                                    "已按遗忘风险、考试周临近度和课表空档重排今日复习顺序。当前建议先处理：$it"
                                } ?: "当前没有可重排的复习项。"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.primary.copy(alpha = 0.82f), contentColor = palette.pillOn),
                        ) {
                            Text("智能重排")
                        }
                        OutlinedButton(
                            onClick = onOpenExportCenter,
                            border = BorderStroke(1.dp, palette.cardBorder),
                        ) {
                            Text("导出复习", color = palette.ink)
                        }
                        OutlinedButton(
                            onClick = onBack,
                            border = BorderStroke(1.dp, palette.cardBorder),
                        ) {
                            Text("返回", color = palette.ink)
                        }
                    }
                }
            }
            if (lastSuggestion.isNotBlank()) {
                item {
                    ReviewGlassCard {
                        Text("下一轮建议", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        ReviewInsightCard(
                            title = "最近完成后的推荐动作",
                            body = lastSuggestion,
                            value = "已更新",
                        )
                    }
                }
            }
            item {
                ReviewGlassCard {
                    Text("今日复习中枢", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜索课程、知识点、标签") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ChipRow(options = courseOptions, selected = selectedCourse, onSelect = { selectedCourse = it })
                    Spacer(modifier = Modifier.height(10.dp))
                    ChipRow(
                        options = ReviewFilter.values().map { it.label },
                        selected = filter.label,
                        onSelect = { label -> filter = ReviewFilter.values().first { it.label == label } },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("上次同步：${formatDateTime(lastSyncAt)}", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(statusText, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                }
            }
            item {
                ReviewGlassCard {
                    Text("复习分析", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "今日完成率",
                            value = "${(completionRate * 100).toInt()}%",
                            accent = if (completionRate >= 0.7f) palette.secondary else Color(0xFFD45B4A),
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "近 7 天回顾",
                            value = "$recentReviewCount 次",
                            accent = palette.primary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "覆盖课程",
                            value = "$coverageCount 门",
                            accent = palette.secondary,
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "空档推荐",
                            value = "${freeSlots.size} 段",
                            accent = palette.primary.copy(alpha = 0.92f),
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        when {
                            overdueCount >= 4 -> "当前有较高遗忘风险，建议先处理逾期知识点，再进入课程任务。"
                            completionRate < 0.5f -> "今日完成率偏低，适合先用一轮番茄钟清掉最短复习项。"
                            else -> "当前节奏较稳定，可以把复习和待办交替推进。"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                }
            }
            item {
                ReviewGlassCard {
                    Text("知识点层级", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "核心",
                            value = "${reviewQualitySummary["core"] ?: 0}",
                            accent = palette.primary,
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "重点",
                            value = "${reviewQualitySummary["important"] ?: 0}",
                            accent = palette.secondary,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "常规",
                            value = "${reviewQualitySummary["standard"] ?: 0}",
                            accent = palette.cardBorder,
                        )
                        SummaryBadge(
                            modifier = Modifier.weight(1f),
                            title = "错题",
                            value = "${reviewQualitySummary["error"] ?: 0}",
                            accent = Color(0xFFD45B4A),
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "核心与错题知识点会在智能重排、考试周冲刺和待办生成里获得更高优先级，适合作为近期集中投入的主线。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                }
            }
            item {
                ReviewGlassCard {
                    Text("复习日历视图", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "把接下来两周的复习负载压成按天视图。点某一天后，下面列表会直接切到该日期，适合提前看哪天最适合安排系统复习。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        reviewCalendarDays.forEach { day ->
                            ReviewCalendarDayCard(
                                day = day,
                                selected = selectedReviewDate == day.date,
                                onClick = {
                                    val nextDate = if (selectedReviewDate == day.date) null else day.date
                                    selectedReviewDate = nextDate
                                    filter = ReviewFilter.All
                                    statusText = if (nextDate == null) {
                                        "已清除日期筛选，恢复查看完整复习列表。"
                                    } else {
                                        "已切到 ${day.date.format(DateTimeFormatter.ofPattern("MM-dd"))} 的复习视图，可按当天集中推进。"
                                    }
                                },
                            )
                        }
                    }
                    selectedCalendarDay?.let { day ->
                        Spacer(modifier = Modifier.height(12.dp))
                        ReviewInsightCard(
                            title = "${day.date.format(DateTimeFormatter.ofPattern("MM-dd"))} 复习摘要",
                            body = "应复习 ${day.dueCount} 项，已完成 ${day.completedCount} 项，逾期 ${day.overdueCount} 项。",
                            value = if (day.overdueCount > 0) "需补 ${day.overdueCount} 项" else "日程平稳",
                        )
                    }
                }
            }
            if (errorFocusItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("错题回炉专注模式", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "把高风险错题型知识点压成一轮专项专注。系统会优先挑最紧急、最影响考试周的错题知识点作为回炉主线。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            SummaryBadge(
                                modifier = Modifier.weight(1f),
                                title = "回炉规模",
                                value = "${errorFocusItems.size} 项",
                                accent = Color(0xFFD45B4A),
                            )
                            SummaryBadge(
                                modifier = Modifier.weight(1f),
                                title = "建议专注",
                                value = "$errorFocusMinutes 分钟",
                                accent = palette.primary,
                            )
                        }
                        errorFocusItems.firstOrNull()?.let { item ->
                            Spacer(modifier = Modifier.height(12.dp))
                            ReviewInsightCard(
                                title = "首要回炉项",
                                body = "${item.courseName} · ${item.noteTitle} · ${item.importance.label} · ${masteryLabel(effectiveMastery(item))}",
                                value = dueLabel(item.nextReviewAt),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    val focusTitle = errorFocusItems.firstOrNull()?.let { "错题回炉：${it.noteTitle}" } ?: "错题回炉模式"
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", focusTitle)
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", "错题回炉")
                                    statusText = "已开启错题回炉专注模式，番茄钟将优先绑定最紧急的错题知识点。"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD45B4A), contentColor = Color.White),
                            ) {
                                Text("开启回炉专注")
                            }
                            OutlinedButton(
                                onClick = {
                                    val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                    errorFocusItems.forEach { item ->
                                        val taskId = "review-error-${item.id}"
                                        if (tasks.none { it.id == taskId }) {
                                            tasks.add(
                                                0,
                                                buildReviewTodoTask(item).copy(
                                                    id = taskId,
                                                    title = "错题回炉：${item.noteTitle}",
                                                    tags = (item.tags + listOf("错题回炉", "复习强化", item.courseName)).distinct(),
                                                    listName = "错题回炉",
                                                ),
                                            )
                                        }
                                    }
                                    saveReviewTodoTasks(todoPrefs, tasks)
                                    statusText = "已生成 ${errorFocusItems.size} 条错题回炉待办，可直接按专项模式集中清理。"
                                },
                                border = BorderStroke(1.dp, palette.cardBorder),
                            ) {
                                Text("生成回炉计划", color = palette.ink)
                            }
                        }
                    }
                }
            }
            if (errorProneItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("错题复盘", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "这些知识点已被标记为错题型复习，系统会优先把它们推到今日计划和考试周冲刺里。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        errorProneItems.forEachIndexed { index, item ->
                            ErrorReviewTraceCard(
                                item = item,
                                examSignal = findMatchingExamSignal(item, examSignals),
                                onOpenNote = {
                                    onOpenCourseNoteSource(
                                        CourseNoteSeed(
                                            courseName = item.courseName,
                                            teacher = item.teacher,
                                            focusTitle = item.sourceTitle,
                                        ),
                                    )
                                    statusText = "已回到 ${item.courseName} 的课程笔记，并按来源笔记《${item.sourceTitle}》为你定位。"
                                },
                            )
                            if (index != errorProneItems.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            if (topErrorSources.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("错题来源统计", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "把高频错题来源按课程与笔记维度聚合，方便你判断到底是哪几份材料最需要回炉。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        topErrorSources.forEachIndexed { index, entry ->
                            ReviewInsightCard(
                                title = entry.first,
                                body = "该来源下累计有 ${entry.second} 条错题型知识点，适合优先回看原笔记与相关例题。",
                                value = "${entry.second} 项",
                            )
                            if (index != topErrorSources.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            if (errorProneItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("错题专题复盘", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "把高风险错题按处理顺序收成一组专题复盘清单，适合在冲刺期或低完成率时单独开一轮番茄处理。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        errorProneItems.take(5).forEachIndexed { index, item ->
                            ReviewInsightCard(
                                title = item.noteTitle,
                                body = "${item.courseName} · ${item.sourceTitle} · ${item.importance.label} · 建议 ${item.recommendedMinutes} 分钟",
                                value = if (item.nextReviewAt <= System.currentTimeMillis()) "优先回炉" else dueLabel(item.nextReviewAt),
                            )
                            if (index != minOf(errorProneItems.size, 5) - 1) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            if (reviewTagPortrait.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("复习标签画像", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "当前复习项里最常见的标签会集中显示出来，方便你观察最近的学习重心是否过于偏向某一类。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            reviewTagPortrait.forEach { entry ->
                                StatusPill(
                                    text = "${entry.key} ${entry.value}",
                                    background = palette.secondary.copy(alpha = 0.14f),
                                    textColor = palette.secondary,
                                )
                            }
                        }
                    }
                }
            }
            if (smartSuggestion != null) {
                item {
                    ReviewGlassCard {
                        Text("智能重排建议", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        ReviewInsightCard(
                            title = "当前建议优先项",
                            body = smartSuggestion,
                            value = "已重排",
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                topRecommendedItems.forEach { item ->
                                    val task = buildReviewTodoTask(item).copy(
                                        note = buildString {
                                            append(item.courseName)
                                            append(" · 来源《${item.sourceTitle}》")
                                            append("\n按智能重排已列入优先推进。")
                                            append("\n${item.excerpt}")
                                        },
                                    )
                                    if (tasks.none { it.id == task.id }) {
                                        tasks.add(0, task)
                                    }
                                }
                                saveReviewTodoTasks(todoPrefs, tasks)
                                statusText = "已将智能重排建议中的 ${topRecommendedItems.size} 项写入待办，方便后续统一推进。"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.ink),
                        ) {
                            Text("应用建议到待办")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                saveReviewAssistantBridge(
                                    context = context,
                                    items = topRecommendedItems,
                                    reason = smartSuggestion.orEmpty(),
                                )
                                statusText = "已把当前智能重排结果写入智能入口桥接区，后续接大模型时可直接接管这组复习计划。"
                            },
                            border = BorderStroke(1.dp, palette.cardBorder),
                        ) {
                            Text("提交到智能入口", color = palette.ink)
                        }
                    }
                }
            }
            if (examLinkedItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("考试周复习冲刺", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        urgentExamLinkedItem?.let { (item, signal) ->
                            ReviewInsightCard(
                                title = item.noteTitle,
                                body = buildString {
                                    append(item.courseName)
                                    append(" · ")
                                    append(signal?.type ?: "复习")
                                    append("《")
                                    append(signal?.title ?: item.sourceTitle)
                                    append("》")
                                    append(" · 建议 ")
                                    append(item.recommendedMinutes)
                                    append(" 分钟")
                                },
                                value = signal?.let { buildExamSignalLabel(it) } ?: dueLabel(item.nextReviewAt),
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Text(
                            "系统已把与考试、作业、复习事件直接相关的知识点单独收成冲刺队列，适合优先提交给智能体接管或立刻转成待办执行。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ReviewExamSprintFilter.entries.forEach { sprintFilter ->
                                SelectionChip(
                                    text = sprintFilter.label,
                                    chosen = examSprintFilter == sprintFilter,
                                    onClick = { examSprintFilter = sprintFilter },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ReviewSprintTemplate.entries.forEach { template ->
                                SelectionChip(
                                    text = template.label,
                                    chosen = sprintTemplate == template,
                                    onClick = { sprintTemplate = template },
                                )
                            }
                        }
                        if (sprintTemplate == ReviewSprintTemplate.Custom) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("错题优先", "核心优先", "短时优先", "临近优先").forEach { focus ->
                                    SelectionChip(
                                        text = focus,
                                        chosen = customSprintFocus == focus,
                                        onClick = { customSprintFocus = focus },
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf(3, 4, 5, 6).forEach { limit ->
                                    SelectionChip(
                                        text = "保留 $limit 项",
                                        chosen = customSprintLimit == limit,
                                        onClick = { customSprintLimit = limit },
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ReviewInsightCard(
                                    title = "冲刺规模",
                                    body = "当前筛选下共有 ${filteredExamLinkedItems.size} 项考试周相关复习知识点。",
                                    value = "${filteredExamLinkedItems.size} 项",
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ReviewInsightCard(
                                    title = "建议投入",
                                    body = "建议优先投入 $examSprintTotalMinutes 分钟完成这一轮冲刺。",
                                    value = if (urgentExamLinkedCount > 0) "$urgentExamLinkedCount 项临近" else "节奏平稳",
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ReviewInsightCard(
                                    title = "模板偏向",
                                    body = if (sprintTemplate == ReviewSprintTemplate.Custom) {
                                        "当前采用 ${sprintTemplate.label}，焦点是 $customSprintFocus，并保留前 $customSprintLimit 项。"
                                    } else {
                                        "当前采用 ${sprintTemplate.label}，会影响冲刺项排序、推荐时长和执行重心。"
                                    },
                                    value = if (sprintTemplate == ReviewSprintTemplate.Custom) customSprintFocus else sprintTemplate.label,
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                ReviewInsightCard(
                                    title = "质量聚焦",
                                    body = "核心 ${sprintCoreCount} 项 · 错题 ${sprintErrorCount} 项",
                                    value = if (sprintErrorCount > 0) "优先回炉" else "结构稳定",
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        filteredExamLinkedItems.forEachIndexed { index, item ->
                            val signal = findMatchingExamSignal(item, examSignals)
                            ReviewInsightCard(
                                title = item.noteTitle,
                                body = "${item.courseName} · ${signal?.type ?: "复习"}《${signal?.title ?: item.sourceTitle}》 · 建议 ${item.recommendedMinutes} 分钟",
                                value = signal?.let { buildExamSignalLabel(it) } ?: dueLabel(item.nextReviewAt),
                            )
                            if (index != filteredExamLinkedItems.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                    todaySprintItems.forEach { item ->
                                        val task = buildReviewTodoTask(item).copy(
                                            note = buildString {
                                                append(item.courseName)
                                                append(" · 来源《${item.sourceTitle}》")
                                                append("\n今日冲刺建议：优先推进这条考试周相关复习。")
                                                append("\n${item.excerpt}")
                                            },
                                            tags = (buildReviewTodoTask(item).tags + "考试周冲刺" + "今日冲刺").distinct(),
                                            priority = TodoPriority.High,
                                        )
                                        if (tasks.none { it.id == task.id }) tasks.add(0, task)
                                    }
                                    saveReviewTodoTasks(todoPrefs, tasks)
                                    statusText = "已按当前冲刺筛选生成 ${todaySprintItems.size} 项今日冲刺计划。"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
                            ) {
                                Text("生成今日冲刺")
                            }
                            Button(
                                onClick = {
                                    val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                    filteredExamLinkedItems.forEach { item ->
                                        val task = buildReviewTodoTask(item).copy(
                                            note = buildString {
                                                append(item.courseName)
                                                append(" · 来源《${item.sourceTitle}》")
                                                append("\n该知识点已进入考试周复习冲刺队列。")
                                                append("\n${item.excerpt}")
                                            },
                                            tags = (buildReviewTodoTask(item).tags + "考试周冲刺").distinct(),
                                            priority = TodoPriority.High,
                                        )
                                        if (tasks.none { it.id == task.id }) tasks.add(0, task)
                                    }
                                    saveReviewTodoTasks(todoPrefs, tasks)
                                    statusText = "已将当前筛选下的 ${filteredExamLinkedItems.size} 项复习知识点批量转成高优先待办。"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.ink),
                            ) {
                                Text("转考试周待办")
                            }
                            Button(
                                onClick = {
                                    urgentExamLinkedItem?.first?.let { item ->
                                        SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${item.noteTitle}")
                                        SecurePrefs.putString(focusPrefs, "bound_task_list_secure", item.courseName)
                                        statusText = "已把《${item.noteTitle}》设为考试周复习冲刺的首项专注任务。"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.cardGlow.copy(alpha = 0.9f), contentColor = palette.ink),
                            ) {
                                Text("绑定冲刺专注")
                            }
                            OutlinedButton(
                                onClick = {
                                    saveReviewAssistantBridge(
                                        context = context,
                                        items = filteredExamLinkedItems,
                                        reason = buildString {
                                            append("请优先接管这组考试周复习冲刺项。")
                                            urgentExamLinkedItem?.second?.let { signal ->
                                                append("当前最近的是${signal.type}《${signal.title}》，")
                                                append("时间节点为${buildExamSignalLabel(signal)}。")
                                            }
                                        },
                                    )
                                    statusText = "已把考试周复习冲刺队列提交到智能入口，后续可直接让智能体优先接管这部分计划。"
                                },
                                border = BorderStroke(1.dp, palette.cardBorder),
                            ) {
                                Text("提交冲刺上下文", color = palette.ink)
                            }
                        }
                    }
                }
            }
            if (trendPoints.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("完成率趋势", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            trendPoints.forEach { point ->
                                ReviewTrendBar(
                                    modifier = Modifier.weight(1f),
                                    point = point,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "系统会按最近 7 天的完成情况判断你当前的复习执行稳定性，连续低完成率时更适合先清理短知识点。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                    }
                }
            }
            if (coursePortraits.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("课程记忆画像", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        coursePortraits.forEachIndexed { index, portrait ->
                            CoursePortraitCard(portrait = portrait)
                            if (index != coursePortraits.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            if (courseRetrospectives.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("课程维度复盘", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "把每门课最需要补强的知识点、错题压力和建议投入集中到同一个面板里，更适合决定今天先处理哪门课。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        courseRetrospectives.forEachIndexed { index, retrospective ->
                            CourseRetrospectiveCard(
                                retrospective = retrospective,
                                onOpenNote = {
                                    retrospective.focusItem?.let { focus ->
                                        onOpenCourseNoteSource(
                                            CourseNoteSeed(
                                                courseName = focus.courseName,
                                                teacher = focus.teacher,
                                                focusTitle = focus.sourceTitle,
                                            ),
                                        )
                                        statusText = "已打开 ${focus.courseName} 的来源笔记《${focus.sourceTitle}》，可直接回看这门课当前最该补强的知识点。"
                                    }
                                },
                            )
                            if (index != courseRetrospectives.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            if (todayReviewItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("今日批量计划", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "今天优先处理 ${todayReviewItems.size} 项知识点，建议总投入 ${todayReviewItems.map { it.recommendedMinutes }.sum()} 分钟。先清逾期，再推进短项。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        todayReviewItems.take(4).forEachIndexed { index, item ->
                            ReviewInsightCard(
                                title = item.noteTitle,
                                body = "${item.courseName} · ${stageLabel(item)} · ${item.importance.label} · 建议 ${item.recommendedMinutes} 分钟",
                                value = dueLabel(item.nextReviewAt),
                            )
                            if (index != minOf(todayReviewItems.size, 4) - 1) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            if (scopedItems.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("复习项批量标签", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "把当前筛选或今日计划里的复习项批量打上同一标签，后续更方便搜索、转待办和交给智能体统一接管。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = batchTagText,
                            onValueChange = { batchTagText = it },
                            label = { Text("批量标签") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            singleLine = true,
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(
                                onClick = {
                                    val tag = batchTagText.trim()
                                    if (tag.isBlank()) {
                                        statusText = "请先输入一个批量标签，再决定应用到哪一组复习项。"
                                    } else {
                                        val targetIds = scopedItems.map { it.id }.toSet()
                                        persist(reviewItems.map { current ->
                                            if (targetIds.contains(current.id)) current.copy(tags = (current.tags + tag).distinct()) else current
                                        })
                                        statusText = "已把标签“$tag”应用到当前筛选下的 ${targetIds.size} 条复习项。"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
                            ) {
                                Text("应用到当前筛选")
                            }
                            OutlinedButton(
                                onClick = {
                                    val tag = batchTagText.trim()
                                    if (tag.isBlank()) {
                                        statusText = "请先输入一个批量标签，再应用到今日计划。"
                                    } else {
                                        val targetIds = todayReviewItems.map { it.id }.toSet()
                                        persist(reviewItems.map { current ->
                                            if (targetIds.contains(current.id)) current.copy(tags = (current.tags + tag).distinct()) else current
                                        })
                                        statusText = "已把标签“$tag”应用到今日应复习的 ${targetIds.size} 条复习项。"
                                    }
                                },
                                border = BorderStroke(1.dp, palette.cardBorder),
                            ) {
                                Text("应用到今日计划", color = palette.ink)
                            }
                            OutlinedButton(
                                onClick = {
                                    val tag = batchTagText.trim()
                                    if (tag.isBlank()) {
                                        statusText = "请先输入一个批量标签，再执行移除。"
                                    } else {
                                        val targetIds = scopedItems.map { it.id }.toSet()
                                        persist(reviewItems.map { current ->
                                            if (targetIds.contains(current.id)) current.copy(tags = current.tags.filterNot { it.equals(tag, true) }) else current
                                        })
                                        statusText = "已从当前筛选下的 ${targetIds.size} 条复习项中移除标签“$tag”。"
                                    }
                                },
                                border = BorderStroke(1.dp, palette.cardBorder),
                            ) {
                                Text("从当前筛选移除", color = palette.ink)
                            }
                        }
                    }
                }
            }
            if (freeSlots.isNotEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("课表空档推荐", style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                val planItems = todayReviewItems.take(freeSlots.size.coerceAtLeast(1))
                                planItems.forEachIndexed { index, item ->
                                    val slot = freeSlots.getOrNull(index)
                                    val task = buildReviewTodoTask(item).copy(
                                        note = buildString {
                                            append(item.courseName)
                                            append(" · 来源《${item.sourceTitle}》")
                                            if (slot != null) {
                                                append("\n推荐安排：${slot.dayLabel} ${slot.slotLabel} ${slot.timeRange}")
                                            }
                                            append("\n${item.excerpt}")
                                        },
                                        dueText = slot?.let { "${it.dayLabel} ${it.slotLabel}" } ?: buildReviewTodoTask(item).dueText,
                                    )
                                    if (tasks.none { it.id == task.id }) {
                                        tasks.add(0, task)
                                    }
                                }
                                saveReviewTodoTasks(todoPrefs, tasks)
                                statusText = "已根据课表空档生成 ${planItems.size} 项当天复习安排，可去待办统一推进。"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = palette.secondary, contentColor = palette.ink),
                        ) {
                            Text("按空档生成当天安排")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        freeSlots.forEachIndexed { index, slot ->
                            ReviewInsightCard(
                                title = "${slot.dayLabel} · ${slot.slotLabel}",
                                body = "${slot.date} ${slot.timeRange} 适合插入一轮 15-45 分钟复习。",
                                value = "空档",
                            )
                            if (index != freeSlots.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            if (scopedItems.isEmpty()) {
                item {
                    ReviewGlassCard {
                        Text("当前筛选下没有复习项", style = MaterialTheme.typography.titleMedium, color = palette.ink)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "可以先去课程笔记里沉淀几条重点，或者切到“全部知识点”查看完整计划。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.softText,
                        )
                    }
                }
            } else {
                groupedItems.forEach { (courseName, itemsForCourse) ->
                    item {
                        CourseSectionHeader(
                            courseName = courseName,
                            count = itemsForCourse.size,
                            strongestItem = itemsForCourse.maxByOrNull { it.mastery },
                            portrait = coursePortraitMap[courseName],
                            expanded = activeExpandedCourse == courseName,
                            onToggleExpand = {
                                hasTouchedExpansion = true
                                expandedCourse = if (activeExpandedCourse == courseName) null else courseName
                            },
                            onBatchComplete = {
                                val updated = reviewItems.map { current ->
                                    if (current.courseName == courseName && itemsForCourse.any { it.id == current.id }) {
                                        advanceReview(current, ReviewConfidence.Normal)
                                    } else {
                                        current
                                    }
                                }
                                persist(updated)
                                statusText = "已批量推进 ${courseName} 的 ${itemsForCourse.size} 条复习项，下一轮会自动重排。"
                            },
                            onBatchPostpone = {
                                val targetIds = itemsForCourse.map { it.id }.toSet()
                                val slotAssignments = itemsForCourse.mapIndexedNotNull { index, item ->
                                    upcomingFreeSlotTimes.getOrNull(index % max(upcomingFreeSlotTimes.size, 1))?.let { (_, startAt) ->
                                        item.id to startAt
                                    }
                                }.toMap()
                                val updated = reviewItems.map { current ->
                                    if (!targetIds.contains(current.id)) {
                                        current
                                    } else {
                                        slotAssignments[current.id]?.let { startAt ->
                                            current.copy(nextReviewAt = startAt)
                                        } ?: current.copy(nextReviewAt = current.nextReviewAt + 24L * 60L * 60L * 1000L)
                                    }
                                }
                                persist(updated)
                                statusText = if (slotAssignments.isNotEmpty()) {
                                    "已将 ${courseName} 的当前复习项优先安排到最近空档，剩余项顺延一天。"
                                } else {
                                    "当前没有可用空档，已将 ${courseName} 的当前复习项整体顺延一天。"
                                }
                            },
                        )
                    }
                    if (activeExpandedCourse == courseName) {
                        items(itemsForCourse, key = { "${it.noteId}:${it.id}" }) { item ->
                            ReviewTaskCard(
                                item = item,
                                highlighted = highlightedTitle.isNotBlank() && (
                                    item.noteTitle.contains(highlightedTitle, true) ||
                                        item.courseName.contains(highlightedTitle, true)
                                    ),
                                historyHint = reviewHistoryHints[item.id],
                                examSignal = findMatchingExamSignal(item, examSignals),
                                onReview = { confidence ->
                                    val updated = reviewItems.map { current ->
                                        if (current.id == item.id) advanceReview(current, confidence) else current
                                    }
                                persist(updated)
                                    val updatedItem = updated.firstOrNull { it.id == item.id }
                                    val suggestion = updatedItem?.let { nextReviewSuggestionText(it) } ?: "继续按计划推进。"
                                    lastSuggestion = suggestion
                                    reviewStore.saveLastSuggestion(suggestion)
                                    statusText = "已完成《${item.noteTitle}》的${confidence.label}复习，下一轮建议：$suggestion"
                                },
                                onPostpone = {
                                    val slotStartAt = upcomingFreeSlotTimes.firstOrNull()?.second
                                    val updated = reviewItems.map { current ->
                                        if (current.id == item.id) {
                                            current.copy(nextReviewAt = slotStartAt ?: (current.nextReviewAt + 24L * 60L * 60L * 1000L))
                                        } else current
                                    }
                                    persist(updated)
                                    statusText = if (slotStartAt != null) {
                                        val slot = upcomingFreeSlotTimes.first().first
                                        "《${item.noteTitle}》已安排到最近空档：${slot.dayLabel} ${slot.slotLabel}。"
                                    } else {
                                        "当前没有可用空档，《${item.noteTitle}》已顺延一天。"
                                    }
                                },
                                onBindFocus = {
                                    SecurePrefs.putString(focusPrefs, "bound_task_title_secure", "复习：${item.noteTitle}")
                                    SecurePrefs.putString(focusPrefs, "bound_task_list_secure", item.courseName)
                                    statusText = "已把《${item.noteTitle}》绑定到番茄钟，可直接开始专注。"
                                },
                                onAddTodo = {
                                    val tasks = loadReviewTodoTasks(todoPrefs).toMutableList()
                                    val task = buildReviewTodoTask(item)
                                    if (tasks.none { it.id == task.id }) {
                                        tasks.add(0, task)
                                        saveReviewTodoTasks(todoPrefs, tasks)
                                        statusText = "已把《${item.noteTitle}》加入待办，并带上 ${item.recommendedMinutes} 分钟复习目标。"
                                    } else {
                                        statusText = "《${item.noteTitle}》已经在待办里了，无需重复添加。"
                                    }
                                },
                                onCycleImportance = {
                                    persist(reviewItems.map { current ->
                                        if (current.id == item.id) cycleReviewImportance(current) else current
                                    })
                                    statusText = "已切换《${item.noteTitle}》的重要度。"
                                },
                                onToggleErrorProne = {
                                    persist(reviewItems.map { current ->
                                        if (current.id == item.id) toggleReviewErrorProne(current) else current
                                    })
                                    statusText = if (item.errorProne) {
                                        "已取消《${item.noteTitle}》的错题标记。"
                                    } else {
                                        "已把《${item.noteTitle}》标记为错题型复习。"
                                    }
                                },
                                onCalibrateMastery = { delta ->
                                    persist(reviewItems.map { current ->
                                        if (current.id == item.id) calibrateReviewItem(current, delta) else current
                                    })
                                    statusText = if (delta > 0f) {
                                        "已上调《${item.noteTitle}》的掌握度校准。"
                                    } else {
                                        "已下调《${item.noteTitle}》的掌握度校准。"
                                    }
                                },
                                onOpenAssistantHistory = { executedAt ->
                                    statusText = "已跳转到首页智能体中的接管历史，可继续查看本条复习项的来源执行。"
                                    onOpenAssistantHistory(executedAt)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun syncReviewItems(notes: List<CourseNote>, existing: List<ReviewItem>): List<ReviewItem> {
    val now = System.currentTimeMillis()
    val existingByNoteId = existing.associateBy { it.noteId }
    val synced = mutableListOf<ReviewItem>()
    notes.forEach { note ->
        val points = extractKnowledgePoints(note)
        points.forEachIndexed { index, point ->
            val pointKey = "${note.id}#$index"
            val current = existingByNoteId[pointKey]
            val excerpt = buildExcerpt(point)
            val recommendedMinutes = recommendMinutes(index, point)
            if (current == null) {
                synced += ReviewItem(
                    id = UUID.randomUUID().toString(),
                    noteId = pointKey,
                    sourceTitle = note.title,
                    courseName = note.courseName,
                    noteTitle = point,
                    teacher = note.teacher,
                    tags = note.tags,
                    excerpt = excerpt,
                    noteUpdatedAt = note.updatedAt,
                    createdAt = now,
                    stageIndex = 0,
                    reviewCount = 0,
                    lastReviewedAt = 0L,
                    nextReviewAt = note.updatedAt,
                    mastery = 0.12f,
                    importance = guessReviewImportance(note, point, index),
                    masteryBias = 0f,
                    errorProne = note.tags.any { it.contains("错题", true) || it.contains("易错", true) },
                    recommendedMinutes = recommendedMinutes,
                )
            } else {
                val noteChanged = note.updatedAt > current.noteUpdatedAt
                synced += current.copy(
                    sourceTitle = note.title,
                    courseName = note.courseName,
                    noteTitle = point,
                    teacher = note.teacher,
                    tags = note.tags,
                    excerpt = excerpt,
                    noteUpdatedAt = note.updatedAt,
                    stageIndex = if (noteChanged) max(0, current.stageIndex - 1) else current.stageIndex,
                    nextReviewAt = if (noteChanged) minOf(current.nextReviewAt, now) else current.nextReviewAt,
                    mastery = if (noteChanged) max(0.14f, current.mastery * 0.72f) else current.mastery,
                    importance = current.importance,
                    masteryBias = current.masteryBias,
                    errorProne = current.errorProne || note.tags.any { it.contains("错题", true) || it.contains("易错", true) },
                    recommendedMinutes = recommendedMinutes,
                )
            }
        }
    }
    return synced.sortedWith(compareBy<ReviewItem> { it.nextReviewAt }.thenBy { it.courseName })
}

private fun advanceReview(item: ReviewItem, confidence: ReviewConfidence): ReviewItem {
    val nextStage = when (confidence) {
        ReviewConfidence.Weak -> item.stageIndex
        ReviewConfidence.Normal -> minOf(item.stageIndex + 1, ReviewIntervals.lastIndex)
        ReviewConfidence.Strong -> minOf(item.stageIndex + 2, ReviewIntervals.lastIndex)
    }
    val days = ReviewIntervals[nextStage]
    val nextTime = LocalDateTime.now()
        .plusDays(days.toLong())
        .withHour(20)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toEpochMillis()
    val mastery = when (confidence) {
        ReviewConfidence.Weak -> max(0.15f, item.mastery + 0.04f)
        ReviewConfidence.Normal -> minOf(1f, item.mastery + 0.14f)
        ReviewConfidence.Strong -> minOf(1f, item.mastery + 0.22f)
    }
    return item.copy(
        stageIndex = nextStage,
        reviewCount = item.reviewCount + 1,
        lastReviewedAt = System.currentTimeMillis(),
        nextReviewAt = nextTime,
        mastery = mastery,
    )
}

private fun calibrateReviewItem(item: ReviewItem, delta: Float): ReviewItem {
    return item.copy(masteryBias = (item.masteryBias + delta).coerceIn(-0.4f, 0.4f))
}

private fun toggleReviewErrorProne(item: ReviewItem): ReviewItem {
    return item.copy(errorProne = !item.errorProne)
}

private fun cycleReviewImportance(item: ReviewItem): ReviewItem {
    val next = when (item.importance) {
        ReviewImportance.Standard -> ReviewImportance.Important
        ReviewImportance.Important -> ReviewImportance.Core
        ReviewImportance.Core -> ReviewImportance.Standard
    }
    return item.copy(importance = next)
}

private fun applySprintTemplate(
    items: List<ReviewItem>,
    examSignals: List<ReviewExamSignal>,
    template: ReviewSprintTemplate,
    customFocus: String,
    customLimit: Int,
): List<ReviewItem> {
    val sorted = when (template) {
        ReviewSprintTemplate.Balanced -> items.sortedByDescending { reviewPriorityScore(it, examSignals) }
        ReviewSprintTemplate.ExamRush -> items.sortedWith(
            compareBy<ReviewItem> {
                findMatchingExamSignal(it, examSignals)?.date ?: LocalDate.MAX
            }.thenByDescending {
                if (findMatchingExamSignal(it, examSignals)?.type == "考试") 1 else 0
            }.thenByDescending {
                reviewPriorityScore(it, examSignals)
            },
        )
        ReviewSprintTemplate.ErrorRepair -> items.sortedWith(
            compareByDescending<ReviewItem> { it.errorProne }
                .thenByDescending { it.importance == ReviewImportance.Core }
                .thenByDescending { reviewPriorityScore(it, examSignals) },
        )
        ReviewSprintTemplate.QuickRecall -> items.sortedWith(
            compareBy<ReviewItem> { it.recommendedMinutes }
                .thenBy { it.nextReviewAt }
                .thenByDescending { reviewPriorityScore(it, examSignals) },
        )
        ReviewSprintTemplate.Custom -> when (customFocus) {
            "核心优先" -> items.sortedWith(
                compareByDescending<ReviewItem> { it.importance == ReviewImportance.Core }
                    .thenByDescending { reviewPriorityScore(it, examSignals) },
            )
            "短时优先" -> items.sortedWith(
                compareBy<ReviewItem> { it.recommendedMinutes }
                    .thenByDescending { reviewPriorityScore(it, examSignals) },
            )
            "临近优先" -> items.sortedWith(
                compareBy<ReviewItem> { findMatchingExamSignal(it, examSignals)?.date ?: LocalDate.MAX }
                    .thenByDescending { reviewPriorityScore(it, examSignals) },
            )
            else -> items.sortedWith(
                compareByDescending<ReviewItem> { it.errorProne }
                    .thenByDescending { reviewPriorityScore(it, examSignals) },
            )
        }
    }
    return sorted.take(
        when (template) {
            ReviewSprintTemplate.Balanced -> 4
            ReviewSprintTemplate.ExamRush -> 5
            ReviewSprintTemplate.ErrorRepair -> 4
            ReviewSprintTemplate.QuickRecall -> 6
            ReviewSprintTemplate.Custom -> customLimit.coerceIn(2, 8)
        },
    )
}

@Composable
private fun SummaryBadge(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = accent.copy(alpha = 0.92f))
            Text(value, style = MaterialTheme.typography.titleLarge, color = accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReviewInsightCard(
    title: String,
    body: String,
    value: String,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardGlow.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
            }
            Spacer(modifier = Modifier.width(12.dp))
            StatusPill(text = value, background = palette.primary.copy(alpha = 0.12f), textColor = palette.primary)
        }
    }
}

@Composable
private fun ReviewTrendBar(
    modifier: Modifier = Modifier,
    point: ReviewTrendPoint,
) {
    val palette = PoxiaoThemeState.palette
    val ratio = if (point.total <= 0) 0f else point.completed.toFloat() / point.total.toFloat()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(palette.cardGlow.copy(alpha = 0.08f)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((24 + (72 * ratio)).dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                palette.secondary.copy(alpha = 0.78f),
                                palette.primary.copy(alpha = 0.92f),
                            ),
                        ),
                    ),
            )
        }
        Text(point.label, style = MaterialTheme.typography.bodySmall, color = palette.softText)
        Text("${point.completed}/${point.total}", style = MaterialTheme.typography.bodyMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SelectionChip(
    text: String,
    chosen: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (chosen) palette.primary.copy(alpha = 0.14f) else palette.cardGlow.copy(alpha = 0.10f),
        border = BorderStroke(
            1.dp,
            if (chosen) palette.primary.copy(alpha = 0.45f) else palette.cardBorder.copy(alpha = 0.55f),
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (chosen) palette.primary else palette.ink,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ReviewCalendarDayCard(
    day: ReviewCalendarDay,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    val weekday = when (day.date.dayOfWeek.value) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        else -> "周日"
    }
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) palette.primary.copy(alpha = 0.18f) else palette.cardGlow.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, if (selected) palette.primary.copy(alpha = 0.35f) else palette.cardBorder.copy(alpha = 0.45f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(day.date.format(DateTimeFormatter.ofPattern("MM-dd")), style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
            Text(weekday, style = MaterialTheme.typography.bodySmall, color = palette.softText)
            Text("应复习 ${day.dueCount}", style = MaterialTheme.typography.bodySmall, color = palette.ink)
            if (day.overdueCount > 0) {
                Text("逾期 ${day.overdueCount}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD45B4A), fontWeight = FontWeight.Medium)
            } else {
                Text("已完成 ${day.completedCount}", style = MaterialTheme.typography.bodySmall, color = palette.secondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CoursePortraitCard(
    portrait: ReviewCoursePortrait,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardGlow.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.48f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(portrait.courseName, style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                StatusPill(
                    text = if (portrait.dueCount > 0) "待处理${portrait.dueCount}" else "节奏稳定",
                    background = if (portrait.dueCount > 0) Color(0xFFD45B4A).copy(alpha = 0.12f) else palette.secondary.copy(alpha = 0.14f),
                    textColor = if (portrait.dueCount > 0) Color(0xFFD45B4A) else palette.secondary,
                )
            }
            Text(
                "共 ${portrait.totalCount} 项，平均记忆 ${masteryLabel(portrait.averageMastery)}，建议投入 ${portrait.plannedMinutes} 分钟。",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.softText,
            )
            Text(
                "质量 ${portrait.qualityScore} · 重点 ${portrait.importantCount} 项 · 核心 ${portrait.coreCount} 项 · 错题 ${portrait.errorCount} 项",
                style = MaterialTheme.typography.bodySmall,
                color = palette.softText,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(palette.cardBorder.copy(alpha = 0.28f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(portrait.averageMastery.coerceIn(0.08f, 1f))
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    palette.secondary.copy(alpha = 0.72f),
                                    palette.primary.copy(alpha = 0.9f),
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Composable
private fun CourseRetrospectiveCard(
    retrospective: ReviewCourseRetrospective,
    onOpenNote: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardGlow.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.48f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(retrospective.courseName, style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        "待处理 ${retrospective.dueCount} 项 · 错题 ${retrospective.errorCount} 项 · 核心 ${retrospective.coreCount} 项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                }
                StatusPill(
                    text = "建议 ${retrospective.plannedMinutes} 分钟",
                    background = palette.secondary.copy(alpha = 0.14f),
                    textColor = palette.secondary,
                )
            }
            retrospective.focusItem?.let { item ->
                ReviewInsightCard(
                    title = "本课当前最该补强",
                    body = "${item.noteTitle} · ${item.importance.label} · ${masteryLabel(effectiveMastery(item))}",
                    value = dueLabel(item.nextReviewAt),
                )
                OutlinedButton(
                    onClick = onOpenNote,
                    border = BorderStroke(1.dp, palette.cardBorder),
                ) {
                    Text("回到原笔记", color = palette.ink)
                }
            }
            Text(
                "平均记忆 ${masteryLabel(retrospective.averageMastery)}，适合把错题和核心知识点优先清掉，再推进常规复习。",
                style = MaterialTheme.typography.bodySmall,
                color = palette.softText,
            )
        }
    }
}

@Composable
private fun ErrorReviewTraceCard(
    item: ReviewItem,
    examSignal: ReviewExamSignal?,
    onOpenNote: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardGlow.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.48f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ReviewInsightCard(
                title = item.noteTitle,
                body = buildString {
                    append(item.courseName)
                    append(" · ")
                    append(item.importance.label)
                    append(" · 建议 ")
                    append(item.recommendedMinutes)
                    append(" 分钟")
                    examSignal?.let {
                        append("\n考试周联动：")
                        append(it.type)
                        append("《")
                        append(it.title)
                        append("》")
                    }
                },
                value = dueLabel(item.nextReviewAt),
            )
            Text(
                "来源笔记：${item.sourceTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.softText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            OutlinedButton(
                onClick = onOpenNote,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("回到原笔记", color = palette.ink)
            }
        }
    }
}

@Composable
private fun CourseSectionHeader(
    courseName: String,
    count: Int,
    strongestItem: ReviewItem?,
    portrait: ReviewCoursePortrait?,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onBatchComplete: () -> Unit,
    onBatchPostpone: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    ReviewGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(courseName, style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                Text("当前共有 $count 条复习项", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                portrait?.let {
                    Text(
                        "待处理 ${it.dueCount} 项 · 平均记忆 ${masteryLabel(it.averageMastery)} · 建议 ${it.plannedMinutes} 分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.softText,
                    )
                    Text(
                        "质量 ${it.qualityScore} · 核心 ${it.coreCount} · 错题 ${it.errorCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.softText,
                    )
                }
            }
            strongestItem?.let {
                StatusPill(
                    text = "最高记忆${masteryLabel(effectiveMastery(it))}",
                    background = palette.secondary.copy(alpha = 0.14f),
                    textColor = palette.secondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onToggleExpand,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
            ) {
                Text(if (expanded) "收起明细" else "展开明细")
            }
            OutlinedButton(
                onClick = onBatchComplete,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("整组完成", color = palette.ink)
            }
            OutlinedButton(
                onClick = onBatchPostpone,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("整组顺延", color = palette.ink)
            }
        }
    }
}

@Composable
private fun ReviewTaskCard(
    item: ReviewItem,
    highlighted: Boolean,
    historyHint: ReviewExecutionHint?,
    examSignal: ReviewExamSignal?,
    onReview: (ReviewConfidence) -> Unit,
    onPostpone: () -> Unit,
    onBindFocus: () -> Unit,
    onAddTodo: () -> Unit,
    onCycleImportance: () -> Unit,
    onToggleErrorProne: () -> Unit,
    onCalibrateMastery: (Float) -> Unit,
    onOpenAssistantHistory: (Long) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    ReviewGlassCard(
        modifier = if (highlighted) {
            Modifier.border(2.dp, palette.primary.copy(alpha = 0.42f), RoundedCornerShape(34.dp))
        } else {
            Modifier
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.noteTitle, style = MaterialTheme.typography.titleLarge, color = palette.ink, fontWeight = FontWeight.SemiBold)
                Text(
                    buildString {
                        append(item.courseName)
                        if (item.teacher.isNotBlank()) append(" · ${item.teacher}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.softText,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(text = stageLabel(item), background = palette.primary.copy(alpha = 0.12f), textColor = palette.primary)
                StatusPill(
                    text = dueLabel(item.nextReviewAt),
                    background = dueColor(item, palette).copy(alpha = 0.12f),
                    textColor = dueColor(item, palette),
                )
            }
        }
        if (item.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            ChipRow(options = item.tags.take(4), selected = "", onSelect = {}, passive = true)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "来源笔记：${item.sourceTitle}",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.softText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            item.excerpt,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.softText,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(14.dp))
        MemoryStageCard(item = item)
        Spacer(modifier = Modifier.height(12.dp))
        ReviewInsightCard(
            title = "知识点质量",
            body = buildString {
                append("重要度：${item.importance.label}")
                append(" · 校准后掌握度：${masteryLabel(effectiveMastery(item))}")
                if (item.errorProne) append(" · 已标记为错题复盘")
            },
            value = if (item.errorProne) "需反复打磨" else "状态正常",
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onCycleImportance,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("切换重要度", color = palette.ink)
            }
            OutlinedButton(
                onClick = onToggleErrorProne,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text(if (item.errorProne) "取消错题" else "标记错题", color = palette.ink)
            }
            OutlinedButton(
                onClick = { onCalibrateMastery(-0.1f) },
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("下调掌握度", color = palette.ink)
            }
            OutlinedButton(
                onClick = { onCalibrateMastery(0.1f) },
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("上调掌握度", color = palette.ink)
            }
        }
        if (examSignal != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ReviewInsightCard(
                title = "考试周联动",
                body = "${examSignal.type}《${examSignal.title}》与当前知识点高度相关，建议优先完成这一轮复习。",
                value = buildExamSignalLabel(examSignal),
            )
        }
        if (historyHint != null) {
            Spacer(modifier = Modifier.height(12.dp))
            ReviewInsightCard(
                title = "智能接管来源",
                body = buildString {
                    append("这条知识点最近一次由智能体于 ")
                    append(formatDateTime(historyHint.executedAt))
                    append(if (historyHint.isReplay) " 回放生成。" else " 接管生成。")
                    append("\n")
                    append(historyHint.summary)
                },
                value = if (historyHint.isReplay) "历史回放" else "智能接管",
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        ChipRow(
            options = ReviewConfidence.values().map { it.label },
            selected = "",
            onSelect = { label ->
                val confidence = ReviewConfidence.values().first { it.label == label }
                onReview(confidence)
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onPostpone,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("顺延安排", color = palette.ink)
            }
            OutlinedButton(
                onClick = onAddTodo,
                border = BorderStroke(1.dp, palette.cardBorder),
            ) {
                Text("加入待办", color = palette.ink)
            }
            historyHint?.let { hint ->
                OutlinedButton(
                    onClick = { onOpenAssistantHistory(hint.executedAt) },
                    border = BorderStroke(1.dp, palette.cardBorder),
                ) {
                    Text("查看接管历史", color = palette.ink)
                }
            }
            Button(
                onClick = onBindFocus,
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary, contentColor = palette.pillOn),
            ) {
                Text("绑定专注")
            }
        }
    }
}

private fun loadReviewExecutionHints(
    context: Context,
    items: List<ReviewItem>,
): Map<String, ReviewExecutionHint> {
    val prefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val raw = prefs.getString("execution_history_v1", "").orEmpty()
    if (raw.isBlank()) return emptyMap()
    return runCatching {
        val array = JSONArray(raw)
        val hintsByExecutionAt = buildMap<Long, ReviewExecutionHint> {
            for (index in 0 until array.length()) {
                val json = array.optJSONObject(index) ?: continue
                val executedAt = json.optLong("executedAt")
                if (executedAt <= 0L || executedAt in this) continue
                put(
                    executedAt,
                    ReviewExecutionHint(
                        executedAt = executedAt,
                        summary = json.optString("summary"),
                        isReplay = json.optLong("replaySourceExecutedAt") > 0L,
                    ),
                )
            }
        }
        buildMap {
            items.forEach { item ->
                if (item.assistantExecutionAt > 0L) {
                    hintsByExecutionAt[item.assistantExecutionAt]?.let { put(item.id, it) }
                }
            }
            if (isEmpty()) {
                val itemsByTitle = items.associateBy { it.noteTitle.trim() }
                for (index in 0 until array.length()) {
                    val json = array.optJSONObject(index) ?: continue
                    val hint = ReviewExecutionHint(
                        executedAt = json.optLong("executedAt"),
                        summary = json.optString("summary"),
                        isReplay = json.optLong("replaySourceExecutedAt") > 0L,
                    )
                    val titles = json.optJSONArray("createdTaskTitles") ?: JSONArray()
                    for (titleIndex in 0 until titles.length()) {
                        val title = titles.optString(titleIndex).removePrefix("复习：").trim()
                        val item = itemsByTitle[title] ?: continue
                        if (item.id !in this) put(item.id, hint)
                    }
                }
            }
        }
    }.getOrDefault(emptyMap())
}

@Composable
private fun MemoryStageCard(item: ReviewItem) {
    val palette = PoxiaoThemeState.palette
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = palette.cardGlow.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.45f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("记忆曲线", style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                Text("已复习 ${item.reviewCount} 次", style = MaterialTheme.typography.bodyMedium, color = palette.softText)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(palette.cardBorder.copy(alpha = 0.28f)),
            ) {
                Box(
                    modifier = Modifier
                    .fillMaxWidth(effectiveMastery(item).coerceIn(0.08f, 1f))
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    palette.secondary.copy(alpha = 0.7f),
                                    palette.primary.copy(alpha = 0.88f),
                                ),
                            ),
                        ),
                )
            }
            Text(
                "当前处于 ${stageLabel(item)}，记忆强度 ${masteryLabel(effectiveMastery(item))}，建议投入 ${item.recommendedMinutes} 分钟，下次复习安排在 ${formatDateTime(item.nextReviewAt)}。",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.softText,
            )
        }
    }
}

@Composable
private fun ReviewGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 34.dp,
        contentPadding = PaddingValues(20.dp),
        tint = palette.card.copy(alpha = 0.34f),
        borderColor = palette.cardBorder.copy(alpha = 0.82f),
        glowColor = palette.cardGlow.copy(alpha = 0.24f),
        blurRadius = 12.dp,
        refractionHeight = 12.dp,
        refractionAmount = 18.dp,
        content = content,
    )
}

@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    passive: Boolean = false,
) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val active = !passive && option == selected
            Surface(
                modifier = Modifier.clickable(enabled = !passive) { onSelect(option) },
                shape = RoundedCornerShape(20.dp),
                color = if (active) palette.primary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.38f),
                border = BorderStroke(1.dp, if (active) palette.primary.copy(alpha = 0.28f) else palette.cardBorder.copy(alpha = 0.72f)),
            ) {
                Text(
                    option,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) palette.primary else palette.ink,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    background: Color,
    textColor: Color,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = background) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun buildExcerpt(content: String): String {
    val normalized = content.replace("\r", "").trim()
    if (normalized.isBlank()) return "这条笔记还没有正文摘要，建议补充课堂重点、概念和易错点。"
    val line = normalized.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty().trim()
    return if (line.length <= 72) line else line.take(72) + "…"
}

private fun extractKnowledgePoints(note: CourseNote): List<String> {
    if (note.knowledgePoints.isNotEmpty()) {
        return note.knowledgePoints
            .map { it.replace(Regex("\\s+"), " ").trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(8)
    }
    val normalized = note.content
        .replace("\r", "\n")
        .replace("；", "\n")
        .replace("。", "\n")
        .replace("、", "\n")
    val candidates = mutableListOf<String>()
    candidates += note.title.trim()
    normalized.lineSequence()
        .map { it.trim().trimStart('-', '*', '•', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '。') }
        .filter { it.length >= 4 }
        .forEach { candidates += it }
    return candidates
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(5)
        .ifEmpty { listOf(note.title.ifBlank { note.courseName }) }
}

private fun recommendMinutes(index: Int, text: String): Int {
    val base = when {
        text.length >= 28 -> 35
        text.length >= 18 -> 25
        else -> 15
    }
    return minOf(45, base + index * 5)
}

private fun guessReviewImportance(note: CourseNote, point: String, index: Int): ReviewImportance {
    val joinedTags = note.tags.joinToString(" ")
    return when {
        joinedTags.contains("核心", true) || joinedTags.contains("重点", true) || point.contains("必考", true) -> ReviewImportance.Core
        joinedTags.contains("理解", true) || joinedTags.contains("公式", true) || index == 0 -> ReviewImportance.Important
        else -> ReviewImportance.Standard
    }
}

private fun reviewFocusGoal(minutes: Int): Int {
    return when {
        minutes >= 50 -> 3
        minutes >= 30 -> 2
        else -> 1
    }
}

private fun buildReviewTodoTask(item: ReviewItem): TodoTask {
    val priority = when {
        item.errorProne -> TodoPriority.High
        item.importance == ReviewImportance.Core -> TodoPriority.High
        item.nextReviewAt <= System.currentTimeMillis() -> TodoPriority.High
        item.importance == ReviewImportance.Important -> TodoPriority.Medium
        else -> TodoPriority.Medium
    }
    return TodoTask(
        id = "review-${item.id}",
        title = "复习：${item.noteTitle}",
        note = buildString {
            append("${item.courseName} · 来源《${item.sourceTitle}》")
            append("\n")
            append(item.excerpt)
            if (item.errorProne) append("\n这条知识点已标记为错题型复习。")
            append("\n重要度：${item.importance.label} · 掌握度：${masteryLabel(effectiveMastery(item))}")
        },
        quadrant = TodoQuadrant.ImportantNotUrgent,
        priority = priority,
        dueText = dueTextForReview(item.nextReviewAt),
        tags = (item.tags + listOf("复习", item.courseName, item.importance.label) + if (item.errorProne) listOf("错题复盘") else emptyList()).distinct(),
        listName = "复习计划",
        reminderText = if (item.nextReviewAt <= System.currentTimeMillis()) "今晚 20:00" else "",
        repeatText = "不重复",
        subtasks = listOf(
            TodoSubtask("回看笔记要点"),
            TodoSubtask("完成一轮口述或默写"),
        ),
        focusGoal = reviewFocusGoal(item.recommendedMinutes),
    )
}

private fun dueTextForReview(nextReviewAt: Long): String {
    val dueDateTime = Instant.ofEpochMilli(nextReviewAt).atZone(defaultZoneId()).toLocalDateTime()
    val today = LocalDate.now()
    return when (dueDateTime.toLocalDate()) {
        today -> "今天 ${dueDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        today.plusDays(1) -> "明天 ${dueDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        else -> dueDateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
    }
}

private fun parseReviewFreeSlotStart(slot: ReviewFreeSlotSuggestion): Long? {
    val date = runCatching { LocalDate.parse(slot.date) }.getOrNull() ?: return null
    val startText = Regex("""(\d{1,2}:\d{2})""").find(slot.timeRange)?.groupValues?.getOrNull(1) ?: return null
    val (hour, minute) = startText.split(":").mapNotNull { it.toIntOrNull() }.let {
        if (it.size == 2) it[0] to it[1] else return null
    }
    return runCatching {
        date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            .atZone(defaultZoneId())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
}

private fun loadReviewFreeSlots(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): List<ReviewFreeSlotSuggestion> {
    val raw = primaryPrefs.getString("schedule_cache_v1", "").orEmpty()
        .ifBlank { fallbackPrefs.getString("schedule_cache_v1", "").orEmpty() }
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val root = JSONObject(raw)
        val weekSchedule = root.optJSONObject("weekSchedule") ?: return@runCatching emptyList()
        val daysArray = weekSchedule.optJSONArray("days") ?: JSONArray()
        val slotsArray = weekSchedule.optJSONArray("timeSlots") ?: JSONArray()
        val coursesArray = weekSchedule.optJSONArray("courses") ?: JSONArray()
        val suggestions = mutableListOf<ReviewFreeSlotSuggestion>()
        val occupiedByDay = mutableMapOf<Int, MutableSet<Int>>()
        for (index in 0 until coursesArray.length()) {
            val course = coursesArray.optJSONObject(index) ?: continue
            val dayOfWeek = course.optInt("dayOfWeek", -1)
            val majorIndex = course.optInt("majorIndex", -1)
            if (dayOfWeek <= 0 || majorIndex <= 0) continue
            occupiedByDay.getOrPut(dayOfWeek) { mutableSetOf() }.add(majorIndex)
        }
        for (dayIndex in 0 until daysArray.length()) {
            val day = daysArray.optJSONObject(dayIndex) ?: continue
            val weekDay = day.optInt("weekDay", -1)
            val dayLabel = day.optString("label")
            val date = day.optString("fullDate").ifBlank { day.optString("date") }
            val occupied = occupiedByDay[weekDay].orEmpty()
            for (slotIndex in 0 until slotsArray.length()) {
                val slot = slotsArray.optJSONObject(slotIndex) ?: continue
                val majorIndex = slot.optInt("majorIndex", -1)
                if (majorIndex in occupied) continue
                suggestions += ReviewFreeSlotSuggestion(
                    dayLabel = dayLabel,
                    date = date,
                    slotLabel = slot.optString("label").ifBlank { "第${majorIndex}大节" },
                    timeRange = slot.optString("timeRange"),
                )
            }
        }
        val today = LocalDate.now()
        suggestions.sortedBy {
            val date = runCatching { LocalDate.parse(it.date) }.getOrDefault(today.plusDays(30))
            java.time.temporal.ChronoUnit.DAYS.between(today, date)
        }
    }.getOrDefault(emptyList())
}

private fun loadReviewExamSignals(
    primaryPrefs: android.content.SharedPreferences,
    fallbackPrefs: android.content.SharedPreferences,
): List<ReviewExamSignal> {
    val raw = primaryPrefs.getString("schedule_extra_events_v1", "").orEmpty()
        .ifBlank { fallbackPrefs.getString("schedule_extra_events_v1", "").orEmpty() }
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val type = item.optString("type")
                if (type != "考试" && type != "作业" && type != "复习") continue
                val date = runCatching { LocalDate.parse(item.optString("date")) }.getOrNull() ?: continue
                add(
                    ReviewExamSignal(
                        title = item.optString("title"),
                        type = type,
                        date = date,
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun reviewPriorityScore(
    item: ReviewItem,
    examSignals: List<ReviewExamSignal>,
): Int {
    val today = LocalDate.now()
    val dueDate = Instant.ofEpochMilli(item.nextReviewAt).atZone(defaultZoneId()).toLocalDate()
    val overdueDays = max(0, java.time.temporal.ChronoUnit.DAYS.between(dueDate, today).toInt())
    val dueSoonBoost = when {
        dueDate.isBefore(today) -> 30
        dueDate == today -> 22
        dueDate == today.plusDays(1) -> 14
        else -> 6
    }
    val masteryPenalty = ((1f - effectiveMastery(item)).coerceIn(0f, 1f) * 30f).toInt()
    val stageBoost = max(0, 8 - item.stageIndex * 2)
    val importanceBoost = item.importance.score
    val errorBoost = if (item.errorProne) 18 else 0
    val examBoost = examSignals
        .filter {
            item.courseName.contains(it.title, true) ||
                it.title.contains(item.courseName, true) ||
                item.noteTitle.contains(it.title, true) ||
                it.title.contains(item.noteTitle, true)
        }
        .map { signal ->
            val days = java.time.temporal.ChronoUnit.DAYS.between(today, signal.date).toInt()
            when {
                days < 0 -> 0
                days <= 1 -> if (signal.type == "考试") 34 else 26
                days <= 3 -> if (signal.type == "考试") 24 else 18
                days <= 7 -> 12
                else -> 0
            }
        }
        .maxOrNull() ?: 0
    return overdueDays * 12 + dueSoonBoost + masteryPenalty + stageBoost + importanceBoost + errorBoost + examBoost + item.recommendedMinutes / 5
}

private fun buildReviewPriorityReason(
    item: ReviewItem,
    examSignals: List<ReviewExamSignal>,
): String {
    val duePart = when (dueLabel(item.nextReviewAt)) {
        "已逾期" -> "这条知识点已经逾期"
        "今天" -> "这条知识点今天就该复习"
        "明天" -> "这条知识点明天即将到点"
        else -> "这条知识点进入了近期窗口"
    }
    val masteryPart = when {
        effectiveMastery(item) < 0.35f -> "当前记忆仍然偏弱"
        effectiveMastery(item) < 0.65f -> "当前还处在待巩固阶段"
        else -> "当前已经有一定记忆基础"
    }
    val examPart = examSignals.firstOrNull {
        item.courseName.contains(it.title, true) ||
            it.title.contains(item.courseName, true) ||
            item.noteTitle.contains(it.title, true) ||
            it.title.contains(item.noteTitle, true)
    }?.let { signal ->
        val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), signal.date).toInt()
        "，而且 ${signal.type}《${signal.title}》${if (days <= 0) "已进入处理窗口" else "还有 ${days} 天"}。"
    }.orEmpty()
    return "${item.courseName} · ${item.noteTitle}。$duePart，$masteryPart，建议先投入 ${item.recommendedMinutes} 分钟$examPart"
}


private fun findMatchingExamSignal(
    item: ReviewItem,
    examSignals: List<ReviewExamSignal>,
): ReviewExamSignal? {
    return examSignals.firstOrNull {
        item.courseName.contains(it.title, true) ||
            it.title.contains(item.courseName, true) ||
            item.noteTitle.contains(it.title, true) ||
            it.title.contains(item.noteTitle, true)
    }
}

private fun buildExamSignalLabel(signal: ReviewExamSignal): String {
    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), signal.date).toInt()
    return when {
        days < 0 -> "已进入窗口"
        days == 0 -> "今天"
        days == 1 -> "明天"
        else -> "$days 天后"
    }
}

private fun nextReviewSuggestionText(item: ReviewItem): String {
    return "${formatDateTime(item.nextReviewAt)} 回看《${item.noteTitle}》，建议继续投入 ${item.recommendedMinutes} 分钟。"
}

private fun saveReviewAssistantBridge(
    context: Context,
    items: List<ReviewItem>,
    reason: String,
) {
    val prefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val scheduleAuthPrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    val scheduleCachePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    val examSignals = loadReviewExamSignals(scheduleAuthPrefs, scheduleCachePrefs)
    val linkedSignals = items.mapNotNull { item ->
        findMatchingExamSignal(item, examSignals)?.let { signal -> item to signal }
    }
    val urgentExamCount = linkedSignals.count { (_, signal) ->
        java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), signal.date).toInt() <= 1
    }
    val examTotalMinutes = items.fold(0) { acc, item -> acc + item.recommendedMinutes }
    val payload = JSONObject().apply {
        put("source", "review_planner")
        put("generatedAt", System.currentTimeMillis())
        put("reason", reason)
        put("examLinkedCount", linkedSignals.size)
        put("examUrgentCount", urgentExamCount)
        put("examTotalMinutes", examTotalMinutes)
        put(
            "examSignals",
            JSONArray().apply {
                linkedSignals.take(3).forEach { (_, signal) ->
                    put(
                        JSONObject().apply {
                            put("title", signal.title)
                            put("type", signal.type)
                            put("date", signal.date.toString())
                            put("label", buildExamSignalLabel(signal))
                        },
                    )
                }
            },
        )
        put(
            "items",
            JSONArray().apply {
                items.forEach { item ->
                    put(
                        JSONObject().apply {
                            put("id", item.id)
                            put("courseName", item.courseName)
                            put("noteTitle", item.noteTitle)
                            put("sourceTitle", item.sourceTitle)
                            put("recommendedMinutes", item.recommendedMinutes)
                            put("nextReviewAt", item.nextReviewAt)
                            put("mastery", item.mastery.toDouble())
                            put("importanceScore", item.importance.score)
                            put("importance", item.importance.label)
                            put("errorProne", item.errorProne)
                        },
                    )
                }
            },
        )
        put(
            "prompt",
            buildString {
                if (linkedSignals.isNotEmpty()) {
                    append("请优先处理这组与考试周直接相关的复习冲刺项，并结合番茄钟、待办和考试节点给出最稳妥的推进方案。")
                } else {
                    append("请根据这组已重排的复习计划，继续细化今天的执行顺序，并结合番茄钟和待办给出最稳妥的推进方案。")
                }
                items.forEachIndexed { index, item ->
                    append("\n")
                    append(index + 1)
                    append(". ")
                    append(item.courseName)
                    append(" / ")
                    append(item.noteTitle)
                    append(" / 建议 ")
                    append(item.recommendedMinutes)
                    append(" 分钟")
                    findMatchingExamSignal(item, examSignals)?.let { signal ->
                        append(" / 关联${signal.type}《${signal.title}》${buildExamSignalLabel(signal)}")
                    }
                }
            },
        )
    }
    prefs.edit()
        .putString("pending_review_plan_v1", payload.toString())
        .putLong("updated_at_v1", System.currentTimeMillis())
        .apply()
    updateReviewBridgeProgress(context, items)
}

private fun updateReviewBridgeProgress(
    context: Context,
    items: List<ReviewItem>,
) {
    val prefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)
    val raw = prefs.getString("pending_review_plan_v1", "").orEmpty()
    if (raw.isBlank()) return
    runCatching {
        val root = JSONObject(raw)
        val pendingItems = root.optJSONArray("items") ?: JSONArray()
        val itemMap = items.associateBy { it.id }
        var completed = 0
        var overdue = 0
        val statusArray = JSONArray()
        for (index in 0 until pendingItems.length()) {
            val item = pendingItems.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val current = itemMap[id] ?: continue
            val dueLabel = dueLabel(current.nextReviewAt)
            val doneLike = effectiveMastery(current) >= 0.9f || current.stageIndex >= ReviewIntervals.lastIndex
            if (doneLike) completed += 1
            if (current.nextReviewAt <= System.currentTimeMillis()) overdue += 1
            statusArray.put(
                JSONObject().apply {
                    put("id", current.id)
                    put("courseName", current.courseName)
                    put("noteTitle", current.noteTitle)
                    put("mastery", current.mastery.toDouble())
                    put("stageIndex", current.stageIndex)
                    put("nextReviewAt", current.nextReviewAt)
                    put("dueLabel", dueLabel)
                    put("doneLike", doneLike)
                },
            )
        }
        prefs.edit()
            .putString(
                "pending_review_progress_v1",
                JSONObject().apply {
                    put("completed", completed)
                    put("overdue", overdue)
                    put("total", statusArray.length())
                    put("items", statusArray)
                    put("updatedAt", System.currentTimeMillis())
                }.toString(),
            )
            .apply()
    }
}

private fun buildReviewTrend(items: List<ReviewItem>): List<ReviewTrendPoint> {
    val today = LocalDate.now()
    return (6 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        val completed = items.count {
            it.lastReviewedAt > 0 && Instant.ofEpochMilli(it.lastReviewedAt).atZone(defaultZoneId()).toLocalDate() == day
        }
        val total = max(
            completed,
            items.count {
                val dueDate = Instant.ofEpochMilli(it.nextReviewAt).atZone(defaultZoneId()).toLocalDate()
                dueDate == day
            },
        )
        ReviewTrendPoint(
            label = day.format(DateTimeFormatter.ofPattern("M/d")),
            total = total,
            completed = completed,
        )
    }
}

private fun buildReviewCalendarDays(items: List<ReviewItem>): List<ReviewCalendarDay> {
    val today = LocalDate.now()
    return (0..13).map { offset ->
        val date = today.plusDays(offset.toLong())
        val dayItems = items.filter {
            Instant.ofEpochMilli(it.nextReviewAt).atZone(defaultZoneId()).toLocalDate() == date
        }
        ReviewCalendarDay(
            date = date,
            dueCount = dayItems.size,
            completedCount = dayItems.count {
                it.lastReviewedAt > 0 &&
                    Instant.ofEpochMilli(it.lastReviewedAt).atZone(defaultZoneId()).toLocalDate() == date
            },
            overdueCount = dayItems.count {
                it.nextReviewAt <= System.currentTimeMillis() && it.lastReviewedAt < it.nextReviewAt
            },
        )
    }
}

private fun buildCoursePortraits(items: List<ReviewItem>): List<ReviewCoursePortrait> {
    val now = System.currentTimeMillis()
    return items.groupBy { it.courseName }
        .map { (courseName, courseItems) ->
            val importantCount = courseItems.count { it.importance != ReviewImportance.Standard }
            val coreCount = courseItems.count { it.importance == ReviewImportance.Core }
            val errorCount = courseItems.count { it.errorProne }
            val qualityScore = courseItems.fold(0) { acc, item ->
                acc + (item.importance.score / 4) + if (item.errorProne) 8 else 0
            }
            ReviewCoursePortrait(
                courseName = courseName,
                totalCount = courseItems.size,
                dueCount = courseItems.count { it.nextReviewAt <= now },
                averageMastery = averageMasteryOf(courseItems),
                plannedMinutes = courseItems.map { it.recommendedMinutes }.sum(),
                importantCount = importantCount,
                coreCount = coreCount,
                errorCount = errorCount,
                qualityScore = qualityScore,
            )
        }
        .sortedWith(
            compareByDescending<ReviewCoursePortrait> { it.dueCount }
                .thenByDescending { it.totalCount }
                .thenBy { it.courseName },
        )
}

private fun buildCourseRetrospectives(
    items: List<ReviewItem>,
    examSignals: List<ReviewExamSignal>,
): List<ReviewCourseRetrospective> {
    val now = System.currentTimeMillis()
    return items.groupBy { it.courseName }
        .map { (courseName, courseItems) ->
            val focusItem = courseItems.maxByOrNull { item ->
                reviewPriorityScore(
                    item = item,
                    examSignals = examSignals,
                )
            }
            ReviewCourseRetrospective(
                courseName = courseName,
                dueCount = courseItems.count { it.nextReviewAt <= now },
                errorCount = courseItems.count { it.errorProne },
                coreCount = courseItems.count { it.importance == ReviewImportance.Core },
                plannedMinutes = courseItems.map { it.recommendedMinutes }.sum(),
                averageMastery = averageMasteryOf(courseItems),
                focusItem = focusItem,
            )
        }
        .sortedWith(
            compareByDescending<ReviewCourseRetrospective> { it.dueCount + it.errorCount }
                .thenByDescending { it.coreCount }
                .thenByDescending { it.plannedMinutes }
                .thenBy { it.courseName },
        )
        .take(4)
}

private fun loadReviewTodoTasks(prefs: android.content.SharedPreferences): List<TodoTask> {
    val raw = prefs.getString("todo_tasks", null).orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList<TodoTask> {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    TodoTask(
                        id = item.optString("id"),
                        title = item.optString("title"),
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
                            for (tagIndex in 0 until tagsArray.length()) add(tagsArray.optString(tagIndex))
                        },
                        listName = item.optString("listName", "收集箱"),
                        reminderText = item.optString("reminderText"),
                        repeatText = item.optString("repeatText", "不重复"),
                        subtasks = buildList<TodoSubtask> {
                            val subtasksArray = item.optJSONArray("subtasks") ?: JSONArray()
                            for (subtaskIndex in 0 until subtasksArray.length()) {
                                val subtaskItem = subtasksArray.optJSONObject(subtaskIndex) ?: continue
                                add(TodoSubtask(subtaskItem.optString("title"), subtaskItem.optBoolean("done")))
                            }
                        },
                        focusCount = item.optInt("focusCount"),
                        focusGoal = item.optInt("focusGoal"),
                        done = item.optBoolean("done"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveReviewTodoTasks(
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

private fun stageLabel(item: ReviewItem): String = "第 ${item.stageIndex + 1} 轮"

private fun effectiveMastery(item: ReviewItem): Float {
    val mastery = item.mastery.takeIf { it.isFinite() } ?: 0f
    val bias = item.masteryBias.takeIf { it.isFinite() } ?: 0f
    return (mastery + bias).coerceIn(0f, 1f)
}

private fun normalizeReviewItems(items: List<ReviewItem>): List<ReviewItem> {
    if (items.isEmpty()) return emptyList()
    val now = System.currentTimeMillis()
    val seenNoteIds = linkedSetOf<String>()
    val usedIds = linkedSetOf<String>()
    return items.mapIndexedNotNull { index, raw ->
        val noteId = raw.noteId.ifBlank { "legacy-note-$index" }
        if (!seenNoteIds.add(noteId)) {
            return@mapIndexedNotNull null
        }
        val baseId = raw.id.ifBlank { "review-$noteId" }
        var resolvedId = baseId
        var suffix = 1
        while (!usedIds.add(resolvedId)) {
            resolvedId = "$baseId-$suffix"
            suffix += 1
        }
        val noteTitle = raw.noteTitle.ifBlank { raw.sourceTitle.ifBlank { "未命名知识点" } }
        val sourceTitle = raw.sourceTitle.ifBlank { noteTitle }
        val courseName = raw.courseName.ifBlank { "未分类课程" }
        val createdAt = raw.createdAt.coerceAtLeast(0L).takeIf { it > 0L } ?: now
        val noteUpdatedAt = raw.noteUpdatedAt.coerceAtLeast(0L).takeIf { it > 0L } ?: createdAt
        raw.copy(
            id = resolvedId,
            noteId = noteId,
            sourceTitle = sourceTitle,
            courseName = courseName,
            noteTitle = noteTitle,
            teacher = raw.teacher.trim(),
            tags = raw.tags.map { it.trim() }.filter { it.isNotBlank() }.distinct(),
            excerpt = raw.excerpt.ifBlank { buildExcerpt(noteTitle) },
            noteUpdatedAt = noteUpdatedAt,
            createdAt = createdAt,
            stageIndex = raw.stageIndex.coerceIn(0, ReviewIntervals.lastIndex),
            reviewCount = raw.reviewCount.coerceAtLeast(0),
            lastReviewedAt = raw.lastReviewedAt.coerceAtLeast(0L),
            nextReviewAt = raw.nextReviewAt.coerceAtLeast(0L).takeIf { it > 0L } ?: noteUpdatedAt,
            mastery = raw.mastery.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0.12f,
            masteryBias = raw.masteryBias.takeIf { it.isFinite() }?.coerceIn(-0.4f, 0.4f) ?: 0f,
            recommendedMinutes = raw.recommendedMinutes.coerceIn(15, 45),
            assistantExecutionAt = raw.assistantExecutionAt.coerceAtLeast(0L),
        )
    }.sortedWith(
        compareBy<ReviewItem> { it.nextReviewAt }
            .thenBy { it.courseName }
            .thenBy { it.noteTitle },
    )
}

private fun averageMasteryOf(items: List<ReviewItem>): Float {
    if (items.isEmpty()) return 0f
    return items.map { effectiveMastery(it) }.average().toFloat()
}

private fun masteryLabel(value: Float): String {
    return when {
        value >= 0.9f -> "稳定"
        value >= 0.65f -> "清晰"
        value >= 0.35f -> "待巩固"
        else -> "易遗忘"
    }
}

private fun dueLabel(nextReviewAt: Long): String {
    val today = LocalDate.now()
    val dueDate = Instant.ofEpochMilli(nextReviewAt).atZone(defaultZoneId()).toLocalDate()
    return when {
        dueDate.isBefore(today) -> "已逾期"
        dueDate == today -> "今天"
        dueDate == today.plusDays(1) -> "明天"
        else -> "${java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)} 天后"
    }
}

private fun dueColor(item: ReviewItem, palette: PoxiaoPalette): Color {
    val dueDate = Instant.ofEpochMilli(item.nextReviewAt).atZone(defaultZoneId()).toLocalDate()
    val today = LocalDate.now()
    return when {
        dueDate.isBefore(today) || dueDate == today -> Color(0xFFD45B4A)
        dueDate == today.plusDays(1) -> palette.secondary
        else -> palette.primary
    }
}

private fun formatDateTime(time: Long): String {
    if (time <= 0L) return "尚未同步"
    return Instant.ofEpochMilli(time).atZone(defaultZoneId()).format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
}

private fun LocalDateTime.toEpochMillis(): Long {
    return atZone(defaultZoneId()).toInstant().toEpochMilli()
}

private fun defaultZoneId(): ZoneId = ZoneId.systemDefault()
