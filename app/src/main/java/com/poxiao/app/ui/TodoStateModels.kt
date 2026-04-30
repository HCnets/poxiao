package com.poxiao.app.ui

internal enum class TodoFilter(val title: String) {
    All("全部"),
    Focus("聚焦"),
    Today("今天"),
    Done("已完成"),
}

internal enum class TodoViewMode(val title: String) {
    Flat("清单"),
    Grouped("分组"),
    Calendar("日历"),
}

internal val TodoFocusGoalOptions = listOf(0, 1, 2, 3, 4, 6, 8)

internal enum class TodoFocusGoalFilter(val title: String) {
    All("全部任务"),
    WithGoal("有目标"),
    Pending("未达成"),
    Reached("已达成"),
}
