package com.poxiao.app.ui

import com.poxiao.app.review.ReviewItem
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoTask

internal fun buildHomeDashboardSnapshot(
    cachedSchedule: AcademicUiState?,
    scheduleEvents: List<ScheduleExtraEvent>,
    todoTasks: List<TodoTask>,
    reviewItems: List<ReviewItem>,
    focusRecords: List<FocusRecord>,
    completedExamWeekIds: List<String>,
): HomeDashboardSnapshot {
    val pendingTodoCount = todoTasks.count { !it.done }
    val todayClassCount = cachedSchedule?.selectedDateCourses?.size ?: 0
    val focusedMinutes = focusRecords.map { it.seconds }.sum() / 60
    val focusTaskStats = buildFocusTaskStats(focusRecords)
    val nextCourse = cachedSchedule?.selectedDateCourses
        ?.sortedBy { it.majorIndex }
        ?.firstOrNull()
    val priorityTodo = todoTasks.firstOrNull { !it.done && it.priority == TodoPriority.High }
        ?: todoTasks.firstOrNull { !it.done }
    val pendingGoalTodo = todoTasks.firstOrNull { !it.done && it.focusGoal > 0 && it.focusCount < it.focusGoal }
    val tomorrowStart = java.time.LocalDate.now()
        .plusDays(1)
        .atStartOfDay(java.time.ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val pendingReviewItems = reviewItems.filter { it.nextReviewAt < tomorrowStart }
    val urgentReviewItem = pendingReviewItems.minByOrNull { it.nextReviewAt }
    val homeExamItems = cachedSchedule?.weekSchedule
        ?.let { buildExamWeekItems(it, scheduleEvents, completedExamWeekIds) }
        .orEmpty()
    val pendingExamItems = homeExamItems.filter { !it.finished }
    val urgentExamItem = pendingExamItems.firstOrNull()
    val topFocusTask = focusTaskStats.firstOrNull()
    return HomeDashboardSnapshot(
        pendingTodoCount = pendingTodoCount,
        todayClassCount = todayClassCount,
        focusedMinutes = focusedMinutes,
        focusTaskStats = focusTaskStats,
        nextCourse = nextCourse,
        priorityTodo = priorityTodo,
        pendingGoalTodo = pendingGoalTodo,
        pendingReviewItems = pendingReviewItems,
        urgentReviewItem = urgentReviewItem,
        homeExamItems = homeExamItems,
        pendingExamItems = pendingExamItems,
        urgentExamItem = urgentExamItem,
        topFocusTask = topFocusTask,
    )
}
