package com.poxiao.app.ui

import androidx.compose.ui.graphics.Color
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaScheduleUiState
import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.schedule.HitaWeekSchedule
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import java.time.LocalDate

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
            labels = if (freeSlots.isEmpty()) listOf("当天课程已满") else freeSlots.map { it.label },
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
            .map { (majorIndex, items) -> "冲突 第 ${majorIndex} 大节 ${items.size} 门" }
        val contiguousTags = dayCourses.zipWithNext()
            .filter { (left, right) -> right.majorIndex - left.majorIndex == 1 }
            .map { (left, right) -> "连堂 ${left.courseName} → ${right.courseName}" }
        val tags = (conflictTags + contiguousTags).distinct()
        if (tags.isEmpty()) null else DayAnalysisSummary(day.label, tags)
    }
}

internal fun dayCourseTags(
    courses: List<HitaCourseBlock>,
    course: HitaCourseBlock,
): List<String> {
    val sameSlotCount = courses.count { it.majorIndex == course.majorIndex }
    val hasPrev = courses.any { it.majorIndex == course.majorIndex - 1 }
    val hasNext = courses.any { it.majorIndex == course.majorIndex + 1 }
    val tags = mutableListOf<String>()
    if (sameSlotCount > 1) tags += "冲突 ${sameSlotCount} 门"
    if (hasPrev || hasNext) tags += "连堂"
    return tags
}

internal fun buildDayTimelineEntries(
    courses: List<HitaCourseBlock>,
    events: List<ScheduleExtraEvent>,
): List<DayTimelineEntry> {
    val courseEntries = courses.map { course ->
        DayTimelineEntry(
            sortKey = course.majorIndex * 100,
            title = course.courseName,
            subtitle = "第 ${course.majorIndex} 时段",
            detail = "${course.classroom.ifBlank { "教室待补充" }} · ${course.teacher.ifBlank { "教师待补充" }}",
            accent = Color(course.accent),
            tags = emptyList(),
            selectableCourse = course,
        )
    }
    val eventEntries = events.map { event ->
        DayTimelineEntry(
            sortKey = eventSortKey(event.time),
            title = event.title,
            subtitle = "${event.time} · ${event.type}",
            detail = event.note.ifBlank { "已加入当天学习时间线" },
            accent = when (event.type) {
                "考试" -> Ginkgo
                "作业" -> MossGreen
                else -> ForestGreen
            },
            tags = listOf(event.type),
            extraEvent = event,
        )
    }
    return (courseEntries + eventEntries).sortedBy { it.sortKey }
}

internal fun buildExamWeekItems(
    schedule: HitaWeekSchedule,
    events: List<ScheduleExtraEvent>,
    completedIds: List<String>,
): List<ExamWeekItem> {
    val scheduleItems = schedule.days.flatMap { day ->
        schedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
            .map { course ->
                ExamWeekItem(
                    id = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}",
                    date = day.fullDate,
                    title = course.courseName,
                    subtitle = "${day.label} · 第 ${course.majorIndex} 大节",
                    detail = buildString {
                        append(course.classroom.ifBlank { "教室待补充" })
                        append(" · ")
                        append(course.teacher.ifBlank { "教师待补充" })
                    },
                    accent = Color(course.accent),
                    priority = countdownPriority(day.fullDate),
                    countdownLabel = countdownLabel(day.fullDate),
                    finished = "course-${day.fullDate}-${course.courseName}-${course.majorIndex}" in completedIds,
                )
            }
    }
    val eventItems = events
        .filter { it.type == "考试" || it.type == "作业" || it.type == "复习" }
        .sortedWith(compareBy<ScheduleExtraEvent> { it.date }.thenBy { eventSortKey(it.time) })
        .map { event ->
            ExamWeekItem(
                id = event.id,
                date = event.date,
                title = event.title,
                subtitle = "${event.date.substringAfterLast("-")} · ${event.time} · ${event.type}",
                detail = event.note.ifBlank { "已加入考试周冲刺列表" },
                accent = when (event.type) {
                    "考试" -> Ginkgo
                    "作业" -> MossGreen
                    else -> ForestGreen
                },
                priority = countdownPriority(event.date, event.type),
                countdownLabel = countdownLabel(event.date),
                finished = event.id in completedIds,
            )
        }
    return (eventItems + scheduleItems)
        .sortedWith(compareBy<ExamWeekItem> { it.priority }.thenBy { it.date }.thenBy { it.subtitle })
}

internal fun buildScheduleShareText(
    state: HitaScheduleUiState,
): String {
    val courseLines = state.weekSchedule.days.joinToString("\n") { day ->
        val dayCourses = state.weekSchedule.courses
            .filter { it.dayOfWeek == day.weekDay }
            .sortedBy { it.majorIndex }
        if (dayCourses.isEmpty()) {
            "${day.label} ${day.date}：无课程"
        } else {
            val body = dayCourses.joinToString("；") { course ->
                "${course.courseName}（第${course.majorIndex}大节 ${course.classroom.ifBlank { "教室待补充" }} ${course.teacher.ifBlank { "教师待补充" }}）"
            }
            "${day.label} ${day.date}：$body"
        }
    }
    val freeLines = weeklyFreeTimeSummary(
        slots = state.weekSchedule.timeSlots,
        days = state.weekSchedule.days,
        courses = state.weekSchedule.courses,
    ).joinToString("\n") { item ->
        "${item.dayLabel}：${item.labels.joinToString("、")}"
    }
    return buildString {
        appendLine("${state.currentTerm.name} ${state.currentWeek.title} 课表摘要")
        appendLine()
        appendLine("课程安排")
        appendLine(courseLines)
        appendLine()
        appendLine("空闲时段")
        appendLine(freeLines)
    }.trim()
}

private fun countdownLabel(date: String): String {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return "待定"
    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        days < 0 -> "已结束"
        days == 0 -> "今天"
        days == 1 -> "明天"
        else -> "还有 ${days} 天"
    }
}

private fun countdownPriority(date: String, type: String = ""): Int {
    val target = runCatching { LocalDate.parse(date) }.getOrNull() ?: return 2
    val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), target).toInt()
    return when {
        type == "考试" && days <= 1 -> 0
        days <= 2 -> 1
        else -> 2
    }
}

internal fun eventSortKey(time: String): Int {
    val parts = time.split(":")
    if (parts.size != 2) return Int.MAX_VALUE / 2
    val hour = parts[0].toIntOrNull() ?: return Int.MAX_VALUE / 2
    val minute = parts[1].toIntOrNull() ?: 0
    return hour * 60 + minute
}
