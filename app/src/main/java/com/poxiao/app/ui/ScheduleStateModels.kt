package com.poxiao.app.ui

internal enum class ScheduleMode(val label: String) {
    Week("周视图"),
    Day("日视图"),
    Month("月视图"),
}

internal enum class ScheduleWorkbench(val title: String) {
    Timetable("课表"),
    Grades("成绩趋势"),
    ExamWeek("考试周"),
}

internal enum class ExamWeekFilter(val title: String) {
    All("全部"),
    Pending("待处理"),
    Urgent("临近"),
    Finished("已完成"),
}

internal enum class ExamWeekTypeFilter(val title: String) {
    All("全部类型"),
    Exam("只看考试"),
    Assignment("只看作业"),
    Review("只看复习"),
}
