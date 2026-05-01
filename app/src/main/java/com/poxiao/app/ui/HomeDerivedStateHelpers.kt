package com.poxiao.app.ui

import com.poxiao.app.data.FeedCard
import com.poxiao.app.notes.CourseNote
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.TeaGreen

internal fun buildHomeTodayTimeline(
    nextCourse: HitaCourseBlock?,
    urgentExamItem: ExamWeekItem?,
    urgentReviewItem: ReviewItem?,
    priorityTodo: TodoTask?,
    pendingGoalTodo: TodoTask?,
    boundTask: String,
    topFocusTask: FocusTaskStat?,
): List<HomeLineData> {
    return buildList {
        nextCourse?.let { add(HomeLineData("今日课程", it.courseName, it.classroom.ifBlank { "教室待补充" })) }
        urgentExamItem?.let { add(HomeLineData("考试周", it.title, "${it.countdownLabel} · ${it.subtitle}")) }
        urgentReviewItem?.let { add(HomeLineData("今日复习", it.noteTitle, "${it.courseName} · 建议 ${it.recommendedMinutes} 分钟")) }
        priorityTodo?.let { add(HomeLineData("待办优先", it.title, "${it.listName} · ${it.dueText}")) }
        pendingGoalTodo?.let {
            add(HomeLineData("专注目标", it.title, "还差 ${it.focusGoal - it.focusCount} 轮 · ${it.dueText}"))
        }
        if (boundTask.isNotBlank()) {
            add(HomeLineData("专注绑定", boundTask, "番茄钟已同步当前任务"))
        } else {
            topFocusTask?.let { add(HomeLineData("专注趋势", it.title, "累计 ${it.minutes} 分钟 · ${it.count} 轮")) }
        }
    }
}

internal fun buildHomeHeroState(
    urgentReviewItem: ReviewItem?,
    priorityTodo: TodoTask?,
    nextCourse: HitaCourseBlock?,
    pendingGoalTodo: TodoTask?,
    focusedMinutes: Int,
    pendingTodoCount: Int,
): HomeHeroState {
    return when {
        urgentReviewItem != null -> HomeHeroState(
            badge = "今日复习",
            headline = "先收住这轮记忆窗口",
            detail = "${urgentReviewItem.courseName} · ${urgentReviewItem.noteTitle} · 建议 ${urgentReviewItem.recommendedMinutes} 分钟",
            accent = ForestGreen,
        )

        priorityTodo != null -> HomeHeroState(
            badge = "高优先待办",
            headline = "先推进最关键的一项",
            detail = "${priorityTodo.title} · ${priorityTodo.dueText} · ${priorityTodo.listName}",
            accent = Ginkgo,
        )

        nextCourse != null -> HomeHeroState(
            badge = "下一门课",
            headline = "课程节奏已经排好",
            detail = "${nextCourse.courseName} · ${nextCourse.classroom.ifBlank { "教室待补充" }} · ${nextCourse.teacher.ifBlank { "教师待补充" }}",
            accent = MossGreen,
        )

        pendingGoalTodo != null -> HomeHeroState(
            badge = "专注目标",
            headline = "继续完成这组专注轮次",
            detail = "${pendingGoalTodo.title} · 还差 ${(pendingGoalTodo.focusGoal - pendingGoalTodo.focusCount).coerceAtLeast(0)} 轮",
            accent = TeaGreen,
        )

        else -> HomeHeroState(
            badge = "今日总览",
            headline = "课表、待办和专注已汇成一张面板",
            detail = "当前待办 $pendingTodoCount 项，累计专注 $focusedMinutes 分钟。",
            accent = ForestGreen,
        )
    }
}

internal fun buildHomeWelcomeSummary(
    nextCourse: com.poxiao.app.schedule.HitaCourseBlock?,
    priorityTodo: TodoTask?,
    urgentReviewItem: ReviewItem?,
    boundTask: String,
    topFocusTask: FocusTaskStat?,
): HomeWelcomeSummary {
    return HomeWelcomeSummary(
        nextCourseSubtitle = nextCourse?.let {
            "${it.courseName} · ${it.classroom.ifBlank { "教室待补充" }}"
        } ?: "打开今日课程与考试周",
        priorityTodoTitle = priorityTodo?.title ?: "打开待办查看当前优先项",
        urgentReviewTitle = urgentReviewItem?.noteTitle ?: "查看今天应推进的复习项",
        pomodoroSubtitle = boundTask.ifBlank {
            topFocusTask?.let { "${it.minutes} 分钟累计" } ?: "开始一轮专注"
        },
    )
}

internal fun buildHomeLocalSearchResults(
    searchQuery: String,
    cachedSchedule: HitaScheduleUiState?,
    todoTasks: List<TodoTask>,
    buildingCandidates: List<String>,
    courseNotes: List<CourseNote>,
): List<HomeSearchResult> {
    if (searchQuery.isBlank()) return emptyList()
    val keyword = searchQuery.trim()
    return buildList {
        cachedSchedule?.weekSchedule?.courses
            ?.filter {
                it.courseName.contains(keyword, ignoreCase = true) ||
                    it.teacher.contains(keyword, ignoreCase = true) ||
                    it.classroom.contains(keyword, ignoreCase = true)
            }
            ?.distinctBy { "${it.courseName}_${it.classroom}_${it.teacher}_${it.dayOfWeek}_${it.majorIndex}" }
            ?.take(6)
            ?.forEach { course ->
                add(
                    HomeSearchResult(
                        id = "course_${course.courseName}_${course.dayOfWeek}_${course.majorIndex}",
                        category = HomeSearchCategory.Course,
                        title = course.courseName,
                        subtitle = course.teacher.ifBlank { "教师待补充" },
                        detail = course.classroom.ifBlank { "教室待补充" },
                    ),
                )
            }
        courseNotes.filter {
            it.courseName.contains(keyword, ignoreCase = true) ||
                it.title.contains(keyword, ignoreCase = true) ||
                it.content.contains(keyword, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(keyword, ignoreCase = true) }
        }.take(6).forEach { note ->
            add(
                HomeSearchResult(
                    id = "note_${note.id}",
                    category = HomeSearchCategory.Note,
                    title = note.courseName,
                    subtitle = note.title,
                    detail = note.tags.joinToString(" · ").ifBlank { "课程笔记" },
                ),
            )
        }
        todoTasks.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                it.note.contains(keyword, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(keyword, ignoreCase = true) }
        }.take(6).forEach { task ->
            add(
                HomeSearchResult(
                    id = "todo_${task.id}",
                    category = HomeSearchCategory.Todo,
                    title = task.title,
                    subtitle = task.listName,
                    detail = task.dueText,
                ),
            )
        }
        buildingCandidates.filter { it.contains(keyword, ignoreCase = true) }
            .take(6)
            .forEach { building ->
                add(
                    HomeSearchResult(
                        id = "building_$building",
                        category = HomeSearchCategory.Building,
                        title = building,
                        subtitle = "校园地图",
                        detail = "点击前往地图与导航",
                    ),
                )
            }
    }
}

internal fun buildHomeGradeSearchResults(
    searchQuery: String,
    liveCards: List<FeedCard>,
    cachedCards: List<FeedCard>,
): List<HomeSearchResult> {
    if (searchQuery.isBlank()) return emptyList()
    val keyword = searchQuery.trim()
    val liveMatches = liveCards.filter {
        it.title.contains(keyword, ignoreCase = true) ||
            it.description.contains(keyword, ignoreCase = true) ||
            it.source.contains(keyword, ignoreCase = true)
    }
    val fallbackMatches = cachedCards.filter {
        it.title.contains(keyword, ignoreCase = true) ||
            it.description.contains(keyword, ignoreCase = true) ||
            it.source.contains(keyword, ignoreCase = true)
    }
    return (if (liveMatches.isNotEmpty()) liveMatches else fallbackMatches).take(6).map { card ->
        HomeSearchResult(
            id = "grade_${card.id}",
            category = HomeSearchCategory.Grade,
            title = card.title,
            subtitle = card.source,
            detail = card.description,
        )
    }
}
