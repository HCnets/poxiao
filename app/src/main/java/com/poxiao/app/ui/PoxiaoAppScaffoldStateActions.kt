package com.poxiao.app.ui

import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.review.ReviewPlannerSeed

internal fun PoxiaoAppScaffoldState.toggleSideNav() {
    sideNavExpanded = !sideNavExpanded
}

internal fun PoxiaoAppScaffoldState.dismissDrawer() {
    pendingDrawerAction = null
    sideNavExpanded = false
}

internal fun PoxiaoAppScaffoldState.queueSectionSelection(target: PrimarySection) {
    pendingDrawerAction = {
        overlayPage = null
        section = target
    }
    sideNavExpanded = false
}

internal fun PoxiaoAppScaffoldState.queueOverlayOpen(target: OverlayPage) {
    pendingDrawerAction = {
        when (target) {
            OverlayPage.CourseNotes -> openCourseNotes()
            OverlayPage.ReviewPlanner -> openReviewPlanner()
            else -> overlayPage = target
        }
    }
    sideNavExpanded = false
}

internal fun PoxiaoAppScaffoldState.selectBottomSection(target: PrimarySection) {
    section = target
}

internal fun PoxiaoAppScaffoldState.closeOverlay() {
    overlayPage = null
}

internal fun PoxiaoAppScaffoldState.openCampusMap() {
    overlayPage = OverlayPage.CampusMap
}

internal fun PoxiaoAppScaffoldState.openCampusServices() {
    overlayPage = OverlayPage.CampusServices
}

internal fun PoxiaoAppScaffoldState.openAcademicAccount() {
    overlayPage = OverlayPage.AcademicAccount
}

internal fun PoxiaoAppScaffoldState.openCalculator() {
    overlayPage = OverlayPage.Calculator
}

internal fun PoxiaoAppScaffoldState.openCourseNotes(seed: CourseNoteSeed? = null) {
    courseNoteSeed = seed
    overlayPage = OverlayPage.CourseNotes
}

internal fun PoxiaoAppScaffoldState.openReviewPlanner(seed: ReviewPlannerSeed? = null) {
    reviewPlannerSeed = seed
    overlayPage = OverlayPage.ReviewPlanner
}

internal fun PoxiaoAppScaffoldState.openExportCenter() {
    overlayPage = OverlayPage.ExportCenter
}

internal fun PoxiaoAppScaffoldState.openNotificationPreferences() {
    overlayPage = OverlayPage.NotificationPreferences
}

internal fun PoxiaoAppScaffoldState.openLearningDashboard() {
    overlayPage = OverlayPage.LearningDashboard
}

internal fun PoxiaoAppScaffoldState.openPreferences() {
    overlayPage = OverlayPage.Preferences
}

internal fun PoxiaoAppScaffoldState.openAssistantPermissions() {
    overlayPage = OverlayPage.AssistantPermissions
}

internal fun PoxiaoAppScaffoldState.focusAssistantHistory(executedAt: Long) {
    assistantHistoryFocusAt = executedAt
    overlayPage = null
    section = PrimarySection.Home
}

internal fun PoxiaoAppScaffoldState.consumeAssistantHistoryFocus() {
    assistantHistoryFocusAt = null
}

internal fun PoxiaoAppScaffoldState.openScheduleDay() {
    scheduleEntryWorkbench = ScheduleWorkbench.Timetable
    scheduleEntryMode = ScheduleMode.Day
    section = PrimarySection.Schedule
}

internal fun PoxiaoAppScaffoldState.openScheduleExamWeek() {
    scheduleEntryWorkbench = ScheduleWorkbench.ExamWeek
    section = PrimarySection.Schedule
}

internal fun PoxiaoAppScaffoldState.openTodoPending() {
    todoEntryFilter = TodoFilter.All
    section = PrimarySection.Todo
}

internal fun PoxiaoAppScaffoldState.openPomodoro() {
    section = PrimarySection.Pomodoro
}

internal fun PoxiaoAppScaffoldState.openMoreSection() {
    section = PrimarySection.More
}
