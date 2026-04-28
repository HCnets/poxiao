package com.poxiao.app.schedule

import com.poxiao.app.data.FeedCard

data class HitaAuthSession(
    val accessToken: String = "",
    val refreshToken: String = "",
    val routeCookie: String = "",
    val jsessionId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val studentType: String = "1",
)

data class HitaTerm(
    val year: String,
    val term: String,
    val name: String,
    val isCurrent: Boolean,
)

data class HitaWeek(
    val index: Int,
    val title: String,
)

data class HitaWeekDay(
    val weekDay: Int,
    val label: String,
    val date: String,
    val fullDate: String = "",
)

data class HitaTimeSlot(
    val majorIndex: Int,
    val label: String,
    val timeRange: String,
    val startSection: Int,
    val endSection: Int,
)

data class HitaCourseBlock(
    val courseName: String,
    val classroom: String,
    val teacher: String,
    val dayOfWeek: Int,
    val majorIndex: Int,
    val accent: Long,
)

data class HitaWeekSchedule(
    val term: HitaTerm,
    val week: HitaWeek,
    val days: List<HitaWeekDay>,
    val timeSlots: List<HitaTimeSlot>,
    val courses: List<HitaCourseBlock>,
)

interface HitaScheduleGateway {
    suspend fun initializeRoute()
    suspend fun fetchRsaKey()
    suspend fun login(studentId: String, password: String): HitaAuthSession
    suspend fun fetchTerms(session: HitaAuthSession): List<HitaTerm>
    suspend fun fetchWeeks(session: HitaAuthSession, term: HitaTerm): List<HitaWeek>
    suspend fun fetchDaySchedule(session: HitaAuthSession, date: String): List<HitaCourseBlock>
    suspend fun fetchWeekSchedule(session: HitaAuthSession, term: HitaTerm, week: HitaWeek): HitaWeekSchedule
    suspend fun fetchGrades(session: HitaAuthSession, term: HitaTerm): List<FeedCard>
    suspend fun fetchTeachingBuildings(session: HitaAuthSession): List<FeedCard>
    suspend fun fetchEmptyClassrooms(session: HitaAuthSession, date: String, buildingId: String): List<FeedCard>
}

object HitaApiGuide {
    const val BaseUrl = "https://mjw.hitsz.edu.cn/incoSpringBoot"
    const val InitRoute = "/component/queryApplicationSetting/rsa"
    const val FetchRsa = "/c_raskey"
    const val LdapLogin = "/authentication/ldap"
    const val QueryTerms = "/app/commapp/queryxnxqlist"
    const val QueryWeeks = "/app/commapp/queryzclistbyxnxq"
    const val QueryWeekMatrix = "/app/Kbcx/query"
    const val QueryDayCourses = "/app/kbrcbyapp/querykbrcbyday"
    const val QuerySelectedCourses = "/app/Xsxk/queryYxkc?_lang=zh_CN"
    const val QueryGrades = "/app/cjgl/xscjList?_lang=zh_CN"
    const val QueryBuildings = "/app/commapp/queryjxllist"
    const val QueryEmptyClassrooms = "/app/kbrcbyapp/querycdzyxx"
}

object HitaSchedulePreview {
    val currentTerm = HitaTerm("2025-2026", "2", "2026 春季", true)
    val terms = listOf(
        currentTerm,
        HitaTerm("2025-2026", "1", "2025 秋季", false),
        HitaTerm("2024-2025", "3", "2025 夏季", false),
    )
    val weeks = (1..18).map { HitaWeek(it, "第 ${it} 周") }
    val schedule = HitaWeekSchedule(
        term = currentTerm,
        week = weeks.first(),
        days = listOf(
            HitaWeekDay(1, "周一", "03-09", "2026-03-09"),
            HitaWeekDay(2, "周二", "03-10", "2026-03-10"),
            HitaWeekDay(3, "周三", "03-11", "2026-03-11"),
            HitaWeekDay(4, "周四", "03-12", "2026-03-12"),
            HitaWeekDay(5, "周五", "03-13", "2026-03-13"),
            HitaWeekDay(6, "周六", "03-14", "2026-03-14"),
            HitaWeekDay(7, "周日", "03-15", "2026-03-15"),
        ),
        timeSlots = listOf(
            HitaTimeSlot(1, "第 1-2 节", "08:30 - 10:15", 1, 2),
            HitaTimeSlot(2, "第 3-4 节", "10:30 - 12:15", 3, 4),
            HitaTimeSlot(3, "第 5-6 节", "14:00 - 15:45", 5, 6),
            HitaTimeSlot(4, "第 7-8 节", "16:00 - 17:45", 7, 8),
            HitaTimeSlot(5, "第 9-10 节", "18:45 - 20:30", 9, 10),
        ),
        courses = listOf(
            HitaCourseBlock("机器学习", "A309", "陈老师", 1, 3, 0xFF7FAF74),
            HitaCourseBlock("交互设计", "T2102", "林老师", 2, 1, 0xFFC6A45A),
            HitaCourseBlock("设计史论", "A106", "顾老师", 3, 2, 0xFF5E9D7A),
            HitaCourseBlock("算法设计", "B201", "周老师", 4, 3, 0xFF4E8B68),
            HitaCourseBlock("学术英语", "T3203", "刘老师", 5, 1, 0xFF8EB76D),
        ),
    )
}
