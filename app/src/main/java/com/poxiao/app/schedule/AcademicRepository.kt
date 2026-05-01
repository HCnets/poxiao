package com.poxiao.app.schedule

import kotlinx.coroutines.flow.StateFlow

/**
 * 通用教务学术数据仓库接口
 * 用于支持多校（Multi-School）适配架构
 */
interface AcademicRepository {
    /**
     * 观察教务状态流
     */
    fun observeUiState(): StateFlow<AcademicUiState>

    /**
     * 登录教务系统
     */
    suspend fun login(studentId: String, password: String)

    /**
     * 刷新当前课表数据
     */
    suspend fun refresh()

    /**
     * 切换学期
     */
    suspend fun selectTerm(term: HitaTerm)

    /**
     * 切换周次
     */
    suspend fun selectWeek(week: HitaWeek)

    /**
     * 切换选中日期
     */
    suspend fun selectDate(date: String)

    /**
     * 退出登录并清除缓存
     */
    suspend fun logout()

    /**
     * 同步数据至云端（如果支持）
     */
    suspend fun syncToCloud()

    /**
     * 从云端导入数据（如果支持）
     */
    suspend fun importFromCloud()
}

/**
 * 通用教务 UI 状态模型
 */
data class AcademicUiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val authExpired: Boolean = false,
    val studentId: String = "",
    val studentName: String = "",
    val terms: List<HitaTerm> = HitaSchedulePreview.terms,
    val currentTerm: HitaTerm = HitaSchedulePreview.currentTerm,
    val weeks: List<HitaWeek> = HitaSchedulePreview.weeks,
    val currentWeek: HitaWeek = HitaSchedulePreview.weeks.first(),
    val weekSchedule: HitaWeekSchedule = HitaSchedulePreview.schedule,
    val selectedDate: String = HitaSchedulePreview.schedule.days.first().fullDate,
    val selectedDateCourses: List<HitaCourseBlock> = HitaSchedulePreview.schedule.courses.filter { it.dayOfWeek == 1 },
    val status: String = "等待连接教务系统...",
)
