package com.poxiao.app.ui

internal enum class HomeSearchCategory(val title: String) {
    Course("课程"),
    Note("笔记"),
    Todo("待办"),
    Grade("成绩"),
    Building("楼栋"),
}

internal data class HomeSearchResult(
    val id: String,
    val category: HomeSearchCategory,
    val title: String,
    val subtitle: String,
    val detail: String,
)

internal enum class HomeDestination {
    ScheduleDay,
    ScheduleExamWeek,
    Todo,
    Pomodoro,
}
