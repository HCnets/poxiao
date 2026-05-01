package com.poxiao.app.schedule

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class HitaScheduleRepository(
    private val gateway: HitaScheduleGateway = HitaScheduleGatewayImpl(),
) : AcademicRepository {
    private val uiState = MutableStateFlow(AcademicUiState())
    private var session: HitaAuthSession? = null
    private var authRequestVersion: Long = 0

    override fun observeUiState(): StateFlow<AcademicUiState> = uiState

    fun restoreCachedState(cached: AcademicUiState) {
        uiState.value = cached.copy(
            loading = false,
            loggedIn = false,
            authExpired = false,
            status = "已恢复上次缓存课表，正在等待同步最新数据。",
        )
    }

    override suspend fun login(studentId: String, password: String) {
        val requestVersion = ++authRequestVersion
        if (studentId.isBlank() || password.isBlank()) {
            uiState.value = uiState.value.copy(status = "请输入学号和密码。")
            return
        }

        uiState.value = uiState.value.copy(loading = true, status = "正在连接教务系统...")
        runCatching {
            val auth = gateway.login(studentId, password)
            val terms = filterTerms(gateway.fetchTerms(auth), auth.studentId.ifBlank { studentId })
            val currentTerm = terms.firstOrNull { it.isCurrent } ?: terms.firstOrNull() ?: HitaSchedulePreview.currentTerm
            val weeks = gateway.fetchWeeks(auth, currentTerm)
            val currentWeek = findCurrentWeek(auth, currentTerm, weeks)
            val schedule = gateway.fetchWeekSchedule(auth, currentTerm, currentWeek)
            val currentDate = resolveCurrentDate(schedule)
            auth to uiState.value.copy(
                loading = false,
                loggedIn = true,
                authExpired = false,
                studentId = auth.studentId.ifBlank { studentId },
                studentName = auth.studentName,
                terms = if (terms.isEmpty()) uiState.value.terms else terms,
                currentTerm = currentTerm,
                weeks = if (weeks.isEmpty()) uiState.value.weeks else weeks,
                currentWeek = currentWeek,
                weekSchedule = schedule,
                selectedDate = currentDate,
                selectedDateCourses = gateway.fetchDaySchedule(auth, currentDate).ifEmpty {
                    schedule.courses.filter { it.dayOfWeek == resolveCurrentWeekday(schedule, currentDate) }
                },
                status = "已连接 ${auth.studentName.ifBlank { auth.studentId }} 的教务数据。",
            )
        }.onSuccess { (auth, state) ->
            if (requestVersion != authRequestVersion) return@onSuccess
            session = auth
            uiState.value = state
        }.onFailure { error ->
            if (requestVersion != authRequestVersion) return@onFailure
            val message = error.message.orEmpty()
            val authFailure = isAuthExpired(error) ||
                message.contains("密码") ||
                message.contains("账号") ||
                message.contains("登录") ||
                message.contains("认证")
            uiState.value = uiState.value.copy(
                loading = false,
                loggedIn = false,
                authExpired = isAuthExpired(error),
                status = if (authFailure) {
                    "账号或密码不正确，请检查后重试。"
                } else {
                    error.message ?: "教务连接失败，请检查网络后重试。"
                },
            )
        }
    }

    override fun logout() {
        authRequestVersion++
        session = null
        uiState.value = uiState.value.copy(
            loading = false,
            loggedIn = false,
            authExpired = false,
            studentId = "",
            studentName = "",
            status = "已退出教务账号。",
        )
    }

    override suspend fun refresh() {
        val auth = session ?: run {
            uiState.value = uiState.value.copy(status = "当前还没有可刷新的登录会话。")
            return
        }
        val current = uiState.value
        uiState.value = current.copy(loading = true, status = "正在刷新课表数据...")
        runCatching {
            val schedule = gateway.fetchWeekSchedule(auth, current.currentTerm, current.currentWeek)
            val currentDate = if (schedule.days.any { it.fullDate == current.selectedDate }) {
                current.selectedDate
            } else {
                resolveCurrentDate(schedule)
            }
            val courses = gateway.fetchDaySchedule(auth, currentDate).ifEmpty {
                schedule.courses.filter { it.dayOfWeek == resolveCurrentWeekday(schedule, currentDate) }
            }
            uiState.value = current.copy(
                loading = false,
                loggedIn = true,
                authExpired = false,
                weekSchedule = schedule,
                selectedDate = currentDate,
                selectedDateCourses = courses,
                status = "课表已刷新。",
            )
        }.onFailure { error ->
            uiState.value = current.copy(
                loading = false,
                authExpired = isAuthExpired(error),
                status = error.message ?: "课表刷新失败。",
            )
        }
    }

    override suspend fun selectTerm(term: HitaTerm) {
        val auth = session ?: run {
            uiState.value = uiState.value.copy(currentTerm = term, status = "当前为预览模式，登录后可加载真实学期。")
            return
        }
        uiState.value = uiState.value.copy(loading = true, currentTerm = term, status = "正在切换学期...")
        runCatching {
            val weeks = gateway.fetchWeeks(auth, term)
            val currentWeek = findCurrentWeek(auth, term, weeks)
            val schedule = gateway.fetchWeekSchedule(auth, term, currentWeek)
            val currentDate = resolveCurrentDate(schedule)
            uiState.value = uiState.value.copy(
                loading = false,
                authExpired = false,
                weeks = if (weeks.isEmpty()) uiState.value.weeks else weeks,
                currentWeek = currentWeek,
                weekSchedule = schedule,
                selectedDate = currentDate,
                selectedDateCourses = gateway.fetchDaySchedule(auth, currentDate).ifEmpty {
                    schedule.courses.filter { it.dayOfWeek == resolveCurrentWeekday(schedule, currentDate) }
                },
                status = "已切换到 ${term.name}。",
            )
        }.onFailure {
            uiState.value = uiState.value.copy(
                loading = false,
                authExpired = isAuthExpired(it),
                status = it.message ?: "学期切换失败。",
            )
        }
    }

    override suspend fun selectWeek(week: HitaWeek) {
        val auth = session ?: run {
            uiState.value = uiState.value.copy(currentWeek = week, status = "当前为预览模式，登录后可加载真实周课表。")
            return
        }
        uiState.value = uiState.value.copy(loading = true, currentWeek = week, status = "正在加载 ${week.title}...")
        runCatching {
            val schedule = gateway.fetchWeekSchedule(auth, uiState.value.currentTerm, week)
            val currentDate = resolveCurrentDate(schedule)
            uiState.value = uiState.value.copy(
                loading = false,
                authExpired = false,
                currentWeek = week,
                weekSchedule = schedule,
                selectedDate = currentDate,
                selectedDateCourses = gateway.fetchDaySchedule(auth, currentDate).ifEmpty {
                    schedule.courses.filter { it.dayOfWeek == resolveCurrentWeekday(schedule, currentDate) }
                },
                status = "已加载 ${week.title} 课表。",
            )
        }.onFailure {
            uiState.value = uiState.value.copy(
                loading = false,
                authExpired = isAuthExpired(it),
                status = it.message ?: "周课表加载失败。",
            )
        }
    }

    override suspend fun selectDate(date: String) {
        val auth = session ?: return
        runCatching {
            val courses = gateway.fetchDaySchedule(auth, date)
            uiState.value = uiState.value.copy(
                selectedDate = date,
                authExpired = false,
                selectedDateCourses = courses.ifEmpty {
                    val fallbackDay = resolveCurrentWeekday(uiState.value.weekSchedule, date)
                    uiState.value.weekSchedule.courses.filter { it.dayOfWeek == fallbackDay }
                },
            )
        }.onFailure {
            uiState.value = uiState.value.copy(
                authExpired = isAuthExpired(it),
                status = it.message ?: "日课表加载失败。",
            )
        }
    }

    override suspend fun syncToCloud() {
        // HITA 暂时不支持云端同步
    }

    override suspend fun importFromCloud() {
        // HITA 暂时不支持云端导入
    }

    private fun filterTerms(terms: List<HitaTerm>, studentId: String): List<HitaTerm> {
        val enrollmentYear = studentId.take(4).toIntOrNull() ?: return terms
        val filtered = terms.filter { term ->
            term.year.take(4).toIntOrNull()?.let { it >= enrollmentYear } ?: true
        }
        return filtered.ifEmpty { terms }
    }

    private suspend fun findCurrentWeek(auth: HitaAuthSession, term: HitaTerm, weeks: List<HitaWeek>): HitaWeek {
        val today = LocalDate.now().toString()
        weeks.forEach { week ->
            val schedule = gateway.fetchWeekSchedule(auth, term, week)
            if (schedule.days.any { it.fullDate == today }) return week
        }
        return weeks.firstOrNull() ?: HitaSchedulePreview.weeks.first()
    }

    private fun resolveCurrentDate(schedule: HitaWeekSchedule): String {
        val today = LocalDate.now().toString()
        return schedule.days.firstOrNull { it.fullDate == today }?.fullDate
            ?: schedule.days.firstOrNull()?.fullDate
            ?: today
    }

    private fun resolveCurrentWeekday(schedule: HitaWeekSchedule, date: String): Int {
        return schedule.days.firstOrNull { it.fullDate == date }?.weekDay
            ?: schedule.days.firstOrNull()?.weekDay
            ?: 1
    }

    private fun isAuthExpired(error: Throwable): Boolean {
        val message = error.message.orEmpty().lowercase()
        return listOf("401", "403", "token", "expired", "session", "login", "auth", "认证", "登录", "会话").any {
            message.contains(it)
        }
    }
}
