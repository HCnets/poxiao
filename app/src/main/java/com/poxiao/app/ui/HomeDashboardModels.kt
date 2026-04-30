package com.poxiao.app.ui

import com.poxiao.app.review.ReviewItem
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.todo.TodoTask

internal data class HomeDashboardSnapshot(
    val pendingTodoCount: Int,
    val todayClassCount: Int,
    val focusedMinutes: Int,
    val focusTaskStats: List<FocusTaskStat>,
    val nextCourse: HitaCourseBlock?,
    val priorityTodo: TodoTask?,
    val pendingGoalTodo: TodoTask?,
    val pendingReviewItems: List<ReviewItem>,
    val urgentReviewItem: ReviewItem?,
    val homeExamItems: List<ExamWeekItem>,
    val pendingExamItems: List<ExamWeekItem>,
    val urgentExamItem: ExamWeekItem?,
    val topFocusTask: FocusTaskStat?,
)
