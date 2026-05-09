package com.poxiao.app.ui

import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.todo.TodoTask
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal fun buildScheduleReviewBlocks(
    tasks: List<TodoTask>,
    days: List<HitaWeekDay>,
    slots: List<HitaTimeSlot>,
    courses: List<com.poxiao.app.schedule.HitaCourseBlock>,
): List<ScheduleReviewBlock> {
    return tasks.filter { it.listName == "复习计划" && !it.done }
        .mapNotNull { task ->
            val (dayOfWeek, time) = parseTodoDueToScheduleTime(task.dueText, days) ?: return@mapNotNull null
            val majorIndex = findMajorIndexForTime(time, slots) ?: return@mapNotNull null
            
            val conflictCourse = courses.firstOrNull { it.dayOfWeek == dayOfWeek && it.majorIndex == majorIndex }
            val suggestedIndex = if (conflictCourse != null) {
                findAlternativeSlot(dayOfWeek, courses, slots)
            } else null

            ScheduleReviewBlock(
                taskId = task.id,
                title = task.title.removePrefix("复习："),
                dayOfWeek = dayOfWeek,
                majorIndex = majorIndex,
                focusGoal = task.focusGoal,
                focusCount = task.focusCount,
                done = task.done,
                conflictCourseName = conflictCourse?.courseName,
                suggestedMajorIndex = suggestedIndex
            )
        }
}

private fun findAlternativeSlot(
    dayOfWeek: Int,
    courses: List<com.poxiao.app.schedule.HitaCourseBlock>,
    slots: List<HitaTimeSlot>,
): Int? {
    val occupied = courses.filter { it.dayOfWeek == dayOfWeek }.map { it.majorIndex }.toSet()
    return slots.filter { it.majorIndex !in occupied }
        .minByOrNull { it.majorIndex }?.majorIndex
}

private fun parseTodoDueToScheduleTime(
    dueText: String,
    days: List<HitaWeekDay>,
): Pair<Int, LocalTime>? {
    val parts = dueText.split(" ")
    if (parts.size < 2) return null
    
    val datePart = parts[0]
    val timePart = parts[1]
    
    val dayOfWeek = when {
        datePart == "今天" -> java.time.LocalDate.now().dayOfWeek.value
        datePart == "明天" -> java.time.LocalDate.now().plusDays(1).dayOfWeek.value
        else -> {
            // 尝试匹配 MM-dd
            days.firstOrNull { it.date == datePart }?.weekDay ?: return null
        }
    }
    
    val time = runCatching {
        LocalTime.parse(timePart, DateTimeFormatter.ofPattern("HH:mm"))
    }.getOrNull() ?: return null
    
    return dayOfWeek to time
}

private fun findMajorIndexForTime(
    time: LocalTime,
    slots: List<HitaTimeSlot>,
): Int? {
    // 优先匹配包含该时间的时段
    slots.firstOrNull { slot ->
        val range = slot.timeRange.split(" - ")
        if (range.size == 2) {
            val start = LocalTime.parse(range[0])
            val end = LocalTime.parse(range[1])
            !time.isBefore(start) && !time.isAfter(end)
        } else false
    }?.let { return it.majorIndex }
    
    // 如果不在任何时段内，寻找最接近的（例如在两个大节之间）
    return slots.minByOrNull { slot ->
        val range = slot.timeRange.split(" - ")
        if (range.size == 2) {
            val start = LocalTime.parse(range[0])
            java.time.Duration.between(time, start).abs().toMinutes()
        } else Long.MAX_VALUE
    }?.majorIndex
}
