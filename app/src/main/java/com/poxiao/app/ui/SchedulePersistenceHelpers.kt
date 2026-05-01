package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.schedule.AcademicUiState
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.schedule.HitaTerm
import com.poxiao.app.schedule.HitaTimeSlot
import com.poxiao.app.schedule.HitaWeek
import com.poxiao.app.schedule.HitaWeekDay
import com.poxiao.app.schedule.HitaWeekSchedule
import org.json.JSONArray
import org.json.JSONObject

private const val ScheduleExtraEventsKey = "schedule_extra_events_v1"
private const val ScheduleEventDraftKey = "schedule_event_draft_v1"
private const val ScheduleCacheKey = "schedule_cache_v1"

internal fun loadScheduleExtraEvents(
    prefs: SharedPreferences,
): List<ScheduleExtraEvent> {
    val raw = prefs.getString(ScheduleExtraEventsKey, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val json = array.getJSONObject(index)
                add(
                    ScheduleExtraEvent(
                        id = json.optString("id"),
                        date = json.optString("date"),
                        title = json.optString("title"),
                        time = json.optString("time"),
                        type = json.optString("type"),
                        note = json.optString("note"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

internal fun saveScheduleExtraEvents(
    prefs: SharedPreferences,
    events: List<ScheduleExtraEvent>,
) {
    val array = JSONArray().apply {
        events.forEach { event ->
            put(
                JSONObject().apply {
                    put("id", event.id)
                    put("date", event.date)
                    put("title", event.title)
                    put("time", event.time)
                    put("type", event.type)
                    put("note", event.note)
                },
            )
        }
    }
    prefs.edit().putString(ScheduleExtraEventsKey, array.toString()).apply()
}

internal fun loadScheduleEventDraft(
    prefs: SharedPreferences,
): ScheduleEventDraft? {
    val raw = prefs.getString(ScheduleEventDraftKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val json = JSONObject(raw)
        ScheduleEventDraft(
            eventId = json.optString("eventId").ifBlank { null },
            date = json.optString("date"),
            title = json.optString("title"),
            time = json.optString("time", "19:30"),
            type = json.optString("type", "作业"),
            note = json.optString("note"),
        )
    }.getOrNull()
}

internal fun saveScheduleEventDraft(
    prefs: SharedPreferences,
    draft: ScheduleEventDraft,
) {
    prefs.edit()
        .putString(
            ScheduleEventDraftKey,
            JSONObject().apply {
                put("eventId", draft.eventId)
                put("date", draft.date)
                put("title", draft.title)
                put("time", draft.time)
                put("type", draft.type)
                put("note", draft.note)
            }.toString(),
        )
        .apply()
}

internal fun clearScheduleEventDraft(prefs: SharedPreferences) {
    prefs.edit().remove(ScheduleEventDraftKey).apply()
}

internal fun saveCachedScheduleUiState(
    prefs: SharedPreferences,
    state: AcademicUiState,
) {
    val root = JSONObject().apply {
        put("loggedIn", state.loggedIn)
        put("studentId", state.studentId)
        put("studentName", state.studentName)
        put("selectedDate", state.selectedDate)
        put("terms", JSONArray().apply { state.terms.forEach { put(termToJson(it)) } })
        put("currentTerm", termToJson(state.currentTerm))
        put("weeks", JSONArray().apply { state.weeks.forEach { put(weekToJson(it)) } })
        put("currentWeek", weekToJson(state.currentWeek))
        put("weekSchedule", weekScheduleToJson(state.weekSchedule))
        put("selectedDateCourses", JSONArray().apply { state.selectedDateCourses.forEach { put(courseToJson(it)) } })
    }
    prefs.edit().putString(ScheduleCacheKey, root.toString()).apply()
}

internal fun loadCachedScheduleUiState(
    prefs: SharedPreferences,
): AcademicUiState? {
    val raw = prefs.getString(ScheduleCacheKey, "").orEmpty()
    if (raw.isBlank()) return null
    return runCatching {
        val root = JSONObject(raw)
        val terms = root.optJSONArray("terms")?.let(::termsFromJson).orEmpty()
        val weeks = root.optJSONArray("weeks")?.let(::weeksFromJson).orEmpty()
        val weekSchedule = root.optJSONObject("weekSchedule")?.let(::weekScheduleFromJson) ?: return null
        val currentTerm = root.optJSONObject("currentTerm")?.let(::termFromJson) ?: weekSchedule.term
        val currentWeek = root.optJSONObject("currentWeek")?.let(::weekFromJson) ?: weekSchedule.week
        val selectedDate = root.optString("selectedDate", weekSchedule.days.firstOrNull()?.fullDate.orEmpty())
        val selectedDateCourses = root.optJSONArray("selectedDateCourses")?.let(::coursesFromJson).orEmpty()
        AcademicUiState(
            loading = false,
            loggedIn = root.optBoolean("loggedIn", false),
            studentId = root.optString("studentId", ""),
            studentName = root.optString("studentName", ""),
            terms = if (terms.isEmpty()) listOf(currentTerm) else terms,
            currentTerm = currentTerm,
            weeks = if (weeks.isEmpty()) listOf(currentWeek) else weeks,
            currentWeek = currentWeek,
            weekSchedule = weekSchedule,
            selectedDate = selectedDate,
            selectedDateCourses = if (selectedDateCourses.isEmpty()) {
                val day = weekSchedule.days.firstOrNull { it.fullDate == selectedDate }?.weekDay ?: 1
                weekSchedule.courses.filter { it.dayOfWeek == day }
            } else {
                selectedDateCourses
            },
            status = "已恢复上次缓存课表。",
        )
    }.getOrNull()
}

private fun termToJson(term: HitaTerm): JSONObject {
    return JSONObject().apply {
        put("year", term.year)
        put("term", term.term)
        put("name", term.name)
        put("isCurrent", term.isCurrent)
    }
}

private fun termFromJson(json: JSONObject): HitaTerm {
    return HitaTerm(
        year = json.optString("year"),
        term = json.optString("term"),
        name = json.optString("name"),
        isCurrent = json.optBoolean("isCurrent"),
    )
}

private fun weekToJson(week: HitaWeek): JSONObject {
    return JSONObject().apply {
        put("index", week.index)
        put("title", week.title)
    }
}

private fun weekFromJson(json: JSONObject): HitaWeek {
    return HitaWeek(
        index = json.optInt("index"),
        title = json.optString("title"),
    )
}

private fun weekDayToJson(day: HitaWeekDay): JSONObject {
    return JSONObject().apply {
        put("weekDay", day.weekDay)
        put("label", day.label)
        put("date", day.date)
        put("fullDate", day.fullDate)
    }
}

private fun weekDayFromJson(json: JSONObject): HitaWeekDay {
    return HitaWeekDay(
        weekDay = json.optInt("weekDay"),
        label = json.optString("label"),
        date = json.optString("date"),
        fullDate = json.optString("fullDate"),
    )
}

private fun timeSlotToJson(slot: HitaTimeSlot): JSONObject {
    return JSONObject().apply {
        put("majorIndex", slot.majorIndex)
        put("label", slot.label)
        put("timeRange", slot.timeRange)
        put("startSection", slot.startSection)
        put("endSection", slot.endSection)
    }
}

private fun timeSlotFromJson(json: JSONObject): HitaTimeSlot {
    return HitaTimeSlot(
        majorIndex = json.optInt("majorIndex"),
        label = json.optString("label"),
        timeRange = json.optString("timeRange"),
        startSection = json.optInt("startSection"),
        endSection = json.optInt("endSection"),
    )
}

private fun courseToJson(course: HitaCourseBlock): JSONObject {
    return JSONObject().apply {
        put("courseName", course.courseName)
        put("classroom", course.classroom)
        put("teacher", course.teacher)
        put("dayOfWeek", course.dayOfWeek)
        put("majorIndex", course.majorIndex)
        put("accent", course.accent)
    }
}

private fun courseFromJson(json: JSONObject): HitaCourseBlock {
    return HitaCourseBlock(
        courseName = json.optString("courseName"),
        classroom = json.optString("classroom"),
        teacher = json.optString("teacher"),
        dayOfWeek = json.optInt("dayOfWeek"),
        majorIndex = json.optInt("majorIndex"),
        accent = json.optLong("accent"),
    )
}

private fun weekScheduleToJson(schedule: HitaWeekSchedule): JSONObject {
    return JSONObject().apply {
        put("term", termToJson(schedule.term))
        put("week", weekToJson(schedule.week))
        put("days", JSONArray().apply { schedule.days.forEach { put(weekDayToJson(it)) } })
        put("timeSlots", JSONArray().apply { schedule.timeSlots.forEach { put(timeSlotToJson(it)) } })
        put("courses", JSONArray().apply { schedule.courses.forEach { put(courseToJson(it)) } })
    }
}

private fun weekScheduleFromJson(json: JSONObject): HitaWeekSchedule {
    return HitaWeekSchedule(
        term = termFromJson(json.getJSONObject("term")),
        week = weekFromJson(json.getJSONObject("week")),
        days = weekDaysFromJson(json.getJSONArray("days")),
        timeSlots = timeSlotsFromJson(json.getJSONArray("timeSlots")),
        courses = coursesFromJson(json.getJSONArray("courses")),
    )
}

private fun termsFromJson(array: JSONArray): List<HitaTerm> {
    return buildList {
        for (index in 0 until array.length()) add(termFromJson(array.getJSONObject(index)))
    }
}

private fun weeksFromJson(array: JSONArray): List<HitaWeek> {
    return buildList {
        for (index in 0 until array.length()) add(weekFromJson(array.getJSONObject(index)))
    }
}

private fun weekDaysFromJson(array: JSONArray): List<HitaWeekDay> {
    return buildList {
        for (index in 0 until array.length()) add(weekDayFromJson(array.getJSONObject(index)))
    }
}

private fun timeSlotsFromJson(array: JSONArray): List<HitaTimeSlot> {
    return buildList {
        for (index in 0 until array.length()) add(timeSlotFromJson(array.getJSONObject(index)))
    }
}

private fun coursesFromJson(array: JSONArray): List<HitaCourseBlock> {
    return buildList {
        for (index in 0 until array.length()) add(courseFromJson(array.getJSONObject(index)))
    }
}
