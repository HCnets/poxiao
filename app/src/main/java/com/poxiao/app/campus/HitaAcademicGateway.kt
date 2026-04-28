package com.poxiao.app.campus

import com.poxiao.app.data.AcademicGateway
import com.poxiao.app.data.CourseItem
import com.poxiao.app.data.FeedCard
import com.poxiao.app.schedule.HitaAuthSession
import com.poxiao.app.schedule.HitaScheduleGateway
import com.poxiao.app.schedule.HitaScheduleGatewayImpl
import com.poxiao.app.schedule.HitaSchedulePreview
import com.poxiao.app.schedule.HitaTerm
import java.io.IOException
import java.time.LocalDate

class HitaAcademicGateway(
    private val studentId: String,
    private val password: String,
    private val gateway: HitaScheduleGateway = HitaScheduleGatewayImpl(),
) : AcademicGateway {
    private var cachedSession = CachedSession.EMPTY

    override suspend fun importFromHitas(userToken: String): List<CourseItem> {
        val (session, term) = ensureSession()
        val weeks = gateway.fetchWeeks(session, term)
        val currentWeek = weeks.firstOrNull() ?: return emptyList()
        val schedule = gateway.fetchWeekSchedule(session, term, currentWeek)
        return schedule.courses.mapIndexed { index, item ->
            CourseItem(
                id = "${item.courseName}-${item.dayOfWeek}-${item.majorIndex}-$index",
                title = item.courseName,
                teacher = item.teacher,
                classroom = item.classroom,
                weekday = item.dayOfWeek,
                startMinute = item.majorIndex * 100,
                endMinute = item.majorIndex * 100 + 90,
            )
        }
    }

    override suspend fun getEmptyClassrooms(day: String): List<FeedCard> {
        val (session, _) = ensureSession()
        val targetDate = normalizeDay(day)
        val buildingId = fetchTeachingBuildings().firstOrNull()?.id.orEmpty()
        if (buildingId.isBlank()) return emptyList()
        return gateway.fetchEmptyClassrooms(session, targetDate, buildingId)
    }

    override suspend fun getGrades(): List<FeedCard> {
        val (_, term) = ensureSession()
        return fetchGradesForTerm(term)
    }

    suspend fun fetchTerms(): List<HitaTerm> {
        val (session, _) = ensureSession()
        return gateway.fetchTerms(session)
    }

    suspend fun fetchGradesForTerm(term: HitaTerm): List<FeedCard> {
        val (session, _) = ensureSession()
        return gateway.fetchGrades(session, term)
    }

    suspend fun fetchTeachingBuildings(): List<FeedCard> {
        val (session, _) = ensureSession()
        return gateway.fetchTeachingBuildings(session)
    }

    suspend fun fetchEmptyClassrooms(date: String, buildingId: String): List<FeedCard> {
        val (session, _) = ensureSession()
        return gateway.fetchEmptyClassrooms(session, date, buildingId)
    }

    private fun normalizeDay(day: String): String {
        return if (day == "today") LocalDate.now().toString() else day
    }

    private suspend fun ensureSession(): Pair<HitaAuthSession, HitaTerm> {
        if (studentId.isBlank() || password.isBlank()) {
            throw IOException("\u672a\u627e\u5230\u6559\u52a1\u767b\u5f55\u4fe1\u606f\u3002")
        }
        val today = LocalDate.now().toString()
        if (cachedSession.studentId == studentId && cachedSession.date == today) {
            return cachedSession.session to cachedSession.term
        }
        val session = gateway.login(studentId, password)
        val terms = gateway.fetchTerms(session)
        val term = terms.firstOrNull { it.isCurrent } ?: terms.firstOrNull()
            ?: throw IOException("\u672a\u83b7\u53d6\u5230\u5f53\u524d\u5b66\u671f\u3002")
        cachedSession = CachedSession(studentId, today, session, term)
        return session to term
    }

    private data class CachedSession(
        val studentId: String,
        val date: String,
        val session: HitaAuthSession,
        val term: HitaTerm,
    ) {
        companion object {
            val EMPTY = CachedSession(
                studentId = "",
                date = "",
                session = HitaAuthSession(),
                term = HitaSchedulePreview.currentTerm,
            )
        }
    }
}
