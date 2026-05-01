package com.poxiao.app.data

import android.content.Context
import com.poxiao.app.security.SecurePrefs
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import org.json.JSONArray
import org.json.JSONObject

data class AssistantContextSummary(
    val id: String,
    val title: String,
    val body: String,
    val source: String,
    val priority: Int,
)

class AppSummaryProvider(context: Context) {
    private val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
    private val focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE)
    private val focusRecordPrefs = context.getSharedPreferences("focus_records", Context.MODE_PRIVATE)
    private val scheduleAuthPrefs = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
    private val scheduleCachePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
    private val campusPrefs = context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE)
    private val assistantBridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE)

    fun loadSummaries(): List<AssistantContextSummary> {
        return buildList {
            loadScheduleSummary()?.let { add(it) }
            loadTodoSummary()?.let { add(it) }
            loadConflictSummary()?.let { add(it) }
            loadFocusSummary()?.let { add(it) }
            loadGradeSummary()?.let { add(it) }
            loadBoundTaskSummary()?.let { add(it) }
            loadReviewBridgeSummary()?.let { add(it) }
            loadReviewExamSummary()?.let { add(it) }
        }.sortedByDescending { it.priority }
    }

    private fun loadConflictSummary(): AssistantContextSummary? {
        val state = loadScheduleState() ?: return null
        val todayCourses = resolveTodayCourses(state)
        val todoRaw = todoPrefs.getString("todo_tasks", "").orEmpty()
        if (todoRaw.isBlank()) return null
        val todoArray = runCatching { JSONArray(todoRaw) }.getOrNull() ?: return null
        
        val slotsArray = state.optJSONObject("weekSchedule")?.optJSONArray("timeSlots") ?: JSONArray()
        val slots = mutableListOf<JSONObject>()
        for (i in 0 until slotsArray.length()) { slots.add(slotsArray.optJSONObject(i)) }
        
        val conflicts = mutableListOf<String>()
        val today = LocalDate.now().toString()
        
        for (i in 0 until todoArray.length()) {
            val task = todoArray.optJSONObject(i) ?: continue
            if (task.optBoolean("done") || task.optString("listName") != "复习计划") continue
            
            val dueText = task.optString("dueText")
            if (!dueText.contains("今天")) continue
            
            val timePart = dueText.substringAfterLast(" ").trim()
            val time = runCatching { 
                LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm")) 
            }.getOrNull() ?: continue
            
            // 查找对应的时段
            val majorIndex = slots.firstOrNull { slot ->
                val range = slot.optString("timeRange").split(" - ")
                if (range.size == 2) {
                    val start = runCatching { LocalTime.parse(range[0], DateTimeFormatter.ofPattern("HH:mm")) }.getOrNull()
                    val end = runCatching { LocalTime.parse(range[1], DateTimeFormatter.ofPattern("HH:mm")) }.getOrNull()
                    if (start != null && end != null) {
                        !time.isBefore(start) && !time.isAfter(end)
                    } else false
                } else false
            }?.optInt("majorIndex") ?: continue
            
            // 检查是否有课
            for (j in 0 until todayCourses.length()) {
                val course = todayCourses.optJSONObject(j) ?: continue
                if (course.optInt("majorIndex") == majorIndex) {
                    conflicts.add("${task.optString("title").removePrefix("复习：")} 与 ${course.optString("courseName")} 冲突")
                    break
                }
            }
        }
        
        if (conflicts.isEmpty()) return null
        
        return AssistantContextSummary(
            id = "schedule_conflicts",
            title = "行程冲突提醒",
            body = "检测到 ${conflicts.size} 项复习任务与今日课程时间重叠：${conflicts.joinToString("；")}。建议进入课表使用“魔法调优”功能一键插空。",
            source = "智能排程",
            priority = 9 // 最高优先级
        )
    }

    private fun loadScheduleSummary(): AssistantContextSummary? {
        val state = loadScheduleState() ?: return null
        val courses = resolveTodayCourses(state)
        val firstCourse = courses.optJSONObject(0)
        val summary = if (courses.length() == 0) {
            "\u4eca\u5929\u6682\u65f6\u6ca1\u6709\u8bfe\u7a0b\u3002"
        } else {
            val title = firstCourse?.optString("courseName").orEmpty()
            val room = firstCourse?.optString("classroom").orEmpty().ifBlank { "\u6559\u5ba4\u5f85\u8865\u5145" }
            "\u4eca\u5929\u6709 ${courses.length()} \u95e8\u8bfe\uff0c\u5f53\u524d\u53ef\u5148\u5904\u7406 $title\uff0c\u5730\u70b9 $room\u3002"
        }
        return AssistantContextSummary(
            id = "schedule_today",
            title = "\u4eca\u65e5\u65e5\u7a0b",
            body = "${LocalDate.now()} \u00b7 $summary",
            source = "\u8bfe\u8868",
            priority = if (courses.length() > 0) 5 else 2,
        )
    }

    private fun loadTodoSummary(): AssistantContextSummary? {
        val raw = todoPrefs.getString("todo_tasks", "").orEmpty()
        if (raw.isBlank()) return null
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return null
        var pendingCount = 0
        var urgentTitle = ""
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            if (!item.optBoolean("done")) {
                pendingCount += 1
                if (urgentTitle.isBlank() && item.optString("priority") == "High") {
                    urgentTitle = item.optString("title")
                }
            }
        }
        if (pendingCount == 0) return null
        return AssistantContextSummary(
            id = "todo_pending",
            title = "\u5f85\u529e\u63a8\u8fdb",
            body = if (urgentTitle.isBlank()) {
                "\u5f53\u524d\u8fd8\u6709 $pendingCount \u9879\u5f85\u529e\u672a\u5b8c\u6210\u3002"
            } else {
                "\u5f53\u524d\u8fd8\u6709 $pendingCount \u9879\u5f85\u529e\uff0c\u4f18\u5148\u4e8b\u9879\u662f $urgentTitle\u3002"
            },
            source = "\u5f85\u529e",
            priority = if (urgentTitle.isBlank()) 4 else 6,
        )
    }

    private fun loadFocusSummary(): AssistantContextSummary? {
        val raw = focusRecordPrefs.getString("focus_records", "").orEmpty()
        if (raw.isBlank()) return null
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return null
        var totalMinutes = 0
        val taskMinutes = linkedMapOf<String, Int>()
        val today = LocalDate.now()
        var todayMinutes = 0
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val minutes = item.optInt("seconds") / 60
            totalMinutes += minutes
            val task = item.optString("taskTitle").ifBlank { "\u672a\u547d\u540d\u4e13\u6ce8" }
            taskMinutes[task] = (taskMinutes[task] ?: 0) + minutes
            if (parseFocusFinishedDate(item.optString("finishedAt")) == today) {
                todayMinutes += minutes
            }
        }
        if (totalMinutes == 0) return null
        val topTask = taskMinutes.maxByOrNull { it.value }?.key.orEmpty()
        return AssistantContextSummary(
            id = "focus_summary",
            title = "\u4e13\u6ce8\u8bb0\u5f55",
            body = if (topTask.isBlank()) {
                "\u7d2f\u8ba1\u4e13\u6ce8 $totalMinutes \u5206\u949f\uff0c\u4eca\u65e5 $todayMinutes \u5206\u949f\u3002"
            } else {
                "\u7d2f\u8ba1\u4e13\u6ce8 $totalMinutes \u5206\u949f\uff0c\u4eca\u65e5 $todayMinutes \u5206\u949f\uff0c\u5f53\u524d\u6295\u5165\u6700\u591a\u7684\u662f $topTask\u3002"
            },
            source = "\u756a\u8304\u949f",
            priority = 3,
        )
    }

    private fun loadGradeSummary(): AssistantContextSummary? {
        val raw = campusPrefs.getString("grade_cache_v1", "").orEmpty()
        if (raw.isBlank()) return null
        val array = runCatching { JSONArray(raw) }.getOrNull() ?: return null
        if (array.length() == 0) return null
        val first = array.optJSONObject(0)
        return AssistantContextSummary(
            id = "grade_summary",
            title = "\u6210\u7ee9\u7f13\u5b58",
            body = if (first == null) {
                "\u6700\u8fd1\u540c\u6b65\u4e86 ${array.length()} \u6761\u6210\u7ee9\u8bb0\u5f55\u3002"
            } else {
                "\u6700\u8fd1\u540c\u6b65\u4e86 ${array.length()} \u6761\u6210\u7ee9\u8bb0\u5f55\uff0c\u53ef\u5148\u67e5\u770b ${first.optString("title")}\u3002"
            },
            source = "\u6821\u56ed\u670d\u52a1",
            priority = 2,
        )
    }

    private fun loadBoundTaskSummary(): AssistantContextSummary? {
        val boundTask = SecurePrefs.getString(focusPrefs, "bound_task_title_secure", "bound_task_title")
        if (boundTask.isBlank()) return null
        return AssistantContextSummary(
            id = "focus_binding",
            title = "\u5f53\u524d\u7ed1\u5b9a\u4efb\u52a1",
            body = "\u756a\u8304\u949f\u5f53\u524d\u7ed1\u5b9a\u7684\u662f $boundTask\uff0c\u53ef\u76f4\u63a5\u7ee7\u7eed\u4e13\u6ce8\u6216\u62c6\u89e3\u4efb\u52a1\u3002",
            source = "\u756a\u8304\u949f",
            priority = 4,
        )
    }

    private fun loadReviewBridgeSummary(): AssistantContextSummary? {
        val raw = assistantBridgePrefs.getString("pending_review_plan_v1", "").orEmpty()
        if (raw.isBlank()) return null
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return null
        val items = json.optJSONArray("items") ?: JSONArray()
        if (items.length() == 0) return null
        val first = items.optJSONObject(0)
        val reason = json.optString("reason")
        val progressRaw = assistantBridgePrefs.getString("pending_review_progress_v1", "").orEmpty()
        val progressJson = if (progressRaw.isBlank()) null else runCatching { JSONObject(progressRaw) }.getOrNull()
        val headline = first?.optString("noteTitle").orEmpty().ifBlank { "\u5f53\u524d\u590d\u4e60\u8ba1\u5212" }
        val course = first?.optString("courseName").orEmpty()
        return AssistantContextSummary(
            id = "review_bridge",
            title = "\u5f85\u63a5\u7ba1\u590d\u4e60\u8ba1\u5212",
            body = buildString {
                append("\u5f53\u524d\u5df2\u4e3a\u667a\u80fd\u4f53\u6865\u63a5 ${items.length()} \u9879\u590d\u4e60\u4efb\u52a1")
                if (course.isNotBlank()) append("\uff0c\u4f18\u5148\u9879\u662f $course \u00b7 $headline")
                progressJson?.let {
                    append("\u3002\u5f53\u524d\u5df2\u63a8\u8fdb ${it.optInt("completed")}/${it.optInt("total")} \u9879")
                    if (it.optInt("overdue") > 0) append("\uff0c\u903e\u671f ${it.optInt("overdue")} \u9879")
                }
                if (reason.isNotBlank()) append("\u3002\u6865\u63a5\u539f\u56e0\uff1a$reason")
            },
            source = "\u590d\u4e60\u8ba1\u5212",
            priority = 7,
        )
    }

    private fun loadReviewExamSummary(): AssistantContextSummary? {
        val raw = assistantBridgePrefs.getString("pending_review_plan_v1", "").orEmpty()
        if (raw.isBlank()) return null
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return null
        val examLinkedCount = json.optInt("examLinkedCount")
        if (examLinkedCount <= 0) return null
        val urgentCount = json.optInt("examUrgentCount")
        val totalMinutes = json.optInt("examTotalMinutes")
        val signals = json.optJSONArray("examSignals") ?: JSONArray()
        val first = signals.optJSONObject(0)
        return AssistantContextSummary(
            id = "review_exam_sync",
            title = "\u8003\u8bd5\u5468\u590d\u4e60\u51b2\u523a",
            body = buildString {
                append("\u5f53\u524d\u6709 $examLinkedCount \u9879\u590d\u4e60\u77e5\u8bc6\u70b9\u4e0e\u8003\u8bd5\u5468\u4e8b\u4ef6\u76f4\u63a5\u76f8\u5173")
                if (urgentCount > 0) append("\uff0c\u5176\u4e2d $urgentCount \u9879\u5df2\u8fdb\u5165\u4e34\u8fd1\u7a97\u53e3")
                if (totalMinutes > 0) append("\uff0c\u5efa\u8bae\u6295\u5165 $totalMinutes \u5206\u949f")
                first?.let {
                    val type = it.optString("type")
                    val title = it.optString("title")
                    val label = it.optString("label")
                    if (title.isNotBlank()) {
                        append("\uff0c\u6700\u9700\u8981\u4f18\u5148\u5904\u7406\u7684\u662f")
                        if (type.isNotBlank()) append(type)
                        append("\u300a$title\u300b")
                        if (label.isNotBlank()) append("\uff08$label\uff09")
                    }
                }
                append("\u3002\u9002\u5408\u5148\u63a5\u7ba1\u8fd9\u7ec4\u51b2\u523a\u9879\uff0c\u518d\u51b3\u5b9a\u5f85\u529e\u4e0e\u756a\u8304\u949f\u5206\u914d\u3002")
            },
            source = "\u590d\u4e60\u8ba1\u5212",
            priority = 8,
        )
    }

    private fun loadScheduleState(): JSONObject? {
        val primary = scheduleAuthPrefs.getString("schedule_cache_v1", "").orEmpty()
        val fallback = scheduleCachePrefs.getString("schedule_cache_v1", "").orEmpty()
        val raw = if (primary.isNotBlank()) primary else fallback
        if (raw.isBlank()) return null
        return runCatching { JSONObject(raw) }.getOrNull()
    }

    private fun resolveTodayCourses(state: JSONObject): JSONArray {
        val week = state.optJSONObject("weekSchedule") ?: return state.optJSONArray("selectedDateCourses") ?: JSONArray()
        val days = week.optJSONArray("days") ?: JSONArray()
        val today = LocalDate.now().toString()
        val todayWeekDay = (0 until days.length())
            .mapNotNull { index -> days.optJSONObject(index) }
            .firstOrNull { day ->
                day.optString("fullDate").ifBlank { day.optString("date") } == today
            }
            ?.optInt("weekDay", -1)
            ?: -1
        if (todayWeekDay <= 0) return state.optJSONArray("selectedDateCourses") ?: JSONArray()
        val courses = week.optJSONArray("courses") ?: JSONArray()
        return JSONArray().apply {
            for (index in 0 until courses.length()) {
                val item = courses.optJSONObject(index) ?: continue
                if (item.optInt("dayOfWeek") == todayWeekDay) {
                    put(item)
                }
            }
        }
    }

    private fun parseFocusFinishedDate(value: String): LocalDate? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
        }.recoverCatching {
            val currentYear = LocalDate.now().year
            LocalDateTime.parse(
                "$currentYear-$value",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            ).toLocalDate()
        }.getOrNull()
    }
}
