package com.poxiao.app.ui

import androidx.compose.ui.graphics.Color
import com.poxiao.app.schedule.HitaCourseBlock

internal data class ScheduleExtraEvent(
    val id: String,
    val date: String,
    val title: String,
    val time: String,
    val type: String,
    val note: String,
)

internal data class ExamWeekItem(
    val id: String,
    val date: String,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val priority: Int,
    val countdownLabel: String,
    val finished: Boolean = false,
)

internal data class DayTimelineEntry(
    val sortKey: Int,
    val title: String,
    val subtitle: String,
    val detail: String,
    val accent: Color,
    val tags: List<String>,
    val selectableCourse: HitaCourseBlock? = null,
    val extraEvent: ScheduleExtraEvent? = null,
)

internal data class ScheduleReviewBlock(
    val taskId: String,
    val title: String,
    val dayOfWeek: Int,
    val majorIndex: Int,
    val focusGoal: Int,
    val focusCount: Int,
    val done: Boolean,
    val conflictCourseName: String? = null,
    val suggestedMajorIndex: Int? = null,
)
