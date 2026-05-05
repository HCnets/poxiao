package com.poxiao.app.schedule

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DisabledAcademicRepository : AcademicRepository {
    private val uiState = MutableStateFlow(
        AcademicUiState(
            status = "当前版本未启用教务能力。",
        ),
    )

    override fun observeUiState(): StateFlow<AcademicUiState> = uiState

    override suspend fun login(studentId: String, password: String) = Unit

    override suspend fun refresh() = Unit

    override suspend fun selectTerm(term: HitaTerm) = Unit

    override suspend fun selectWeek(week: HitaWeek) = Unit

    override suspend fun selectDate(date: String) = Unit

    override fun logout() = Unit

    override suspend fun syncToCloud() = Unit

    override suspend fun importFromCloud() = Unit
}
