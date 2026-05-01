package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.poxiao.app.campus.HitaAcademicGateway
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.security.SecurePrefs
import java.time.LocalDateTime

@Composable
internal fun ScheduleBootstrapEffect(
    restored: Boolean,
    prefs: SharedPreferences,
    repository: AcademicRepository,
    savedStudentId: String,
    savedPassword: String,
    onRestoredChange: (Boolean) -> Unit,
) {
    LaunchedEffect(Unit) {
        if (!restored) {
            onRestoredChange(true)
            loadCachedScheduleUiState(prefs)?.let {
                if (repository is com.poxiao.app.schedule.HitaScheduleRepository) {
                    repository.restoreCachedState(it)
                }
            }
            if (savedStudentId.isNotBlank() && savedPassword.isNotBlank()) {
                repository.login(savedStudentId, savedPassword)
            }
        }
    }
}

@Composable
internal fun ScheduleGradeTrendEffect(
    uiState: AcademicUiState,
    prefs: SharedPreferences,
    selectedTrendTerm: String?,
    onGradeTrendChange: (List<GradeTrendPoint>) -> Unit,
    onGradeTrendLoadingChange: (Boolean) -> Unit,
    onGradeTrendStatusChange: (String) -> Unit,
    onSelectedTrendTermChange: (String?) -> Unit,
) {
    LaunchedEffect(uiState.loggedIn, uiState.terms, uiState.studentId) {
        val currentStudentId = SecurePrefs.getString(prefs, "student_id_secure", "student_id")
        val currentPassword = SecurePrefs.getString(prefs, "password_secure", "password")
        val academicGateway = if (currentStudentId.isNotBlank() && currentPassword.isNotBlank()) {
            HitaAcademicGateway(currentStudentId, currentPassword)
        } else {
            null
        }
        if (!uiState.loggedIn || academicGateway == null) {
            onGradeTrendChange(emptyList())
            onGradeTrendStatusChange("登录后可查看成绩趋势。")
            return@LaunchedEffect
        }
        onGradeTrendLoadingChange(true)
        onGradeTrendStatusChange("正在整理各学期成绩趋势...")
        runCatching {
            val cardsByTerm = uiState.terms.mapNotNull { term ->
                val cards = academicGateway.fetchGradesForTerm(term)
                if (cards.isEmpty()) null else term.name to cards
            }
            buildGradeTrendPoints(cardsByTerm)
        }.onSuccess { points ->
            onGradeTrendChange(points)
            onSelectedTrendTermChange(
                selectedTrendTerm?.takeIf { target -> points.any { it.termName == target } }
                    ?: points.firstOrNull()?.termName,
            )
            onGradeTrendStatusChange(
                if (points.isEmpty()) {
                    "当前还没有可用于分析的成绩记录。"
                } else {
                    "已生成 ${points.size} 个学期的成绩趋势。"
                },
            )
        }.onFailure {
            onGradeTrendChange(emptyList())
            onGradeTrendStatusChange(it.message ?: "成绩趋势加载失败。")
        }
        onGradeTrendLoadingChange(false)
    }
}

@Composable
internal fun ScheduleUiStatePersistenceEffect(
    uiState: AcademicUiState,
    prefs: SharedPreferences,
    onLastSyncTimeChange: (String) -> Unit,
) {
    LaunchedEffect(
        uiState.loggedIn,
        uiState.loading,
        uiState.currentTerm,
        uiState.currentWeek,
        uiState.selectedDate,
        uiState.weekSchedule,
        uiState.selectedDateCourses,
    ) {
        if (uiState.loggedIn && !uiState.loading) {
            saveCachedScheduleUiState(prefs, uiState)
            val syncText = formatSyncTime(LocalDateTime.now())
            onLastSyncTimeChange(syncText)
            prefs.edit().putString("schedule_last_sync", syncText).apply()
        }
    }
}

@Composable
internal fun ScheduleReminderRefreshEffect(
    context: Context,
    uiState: AcademicUiState,
    extraEvents: List<ScheduleExtraEvent>,
    completedExamWeekIds: List<String>,
) {
    LaunchedEffect(
        uiState.weekSchedule.week.title,
        uiState.selectedDate,
        extraEvents.joinToString("|") { "${it.id}:${it.date}:${it.time}:${it.title}:${it.type}" },
        completedExamWeekIds.joinToString("|"),
    ) {
        refreshLocalReminderSchedule(context)
    }
}
