package com.poxiao.app.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TodoDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

internal fun buildTodoDueText(
    preset: TodoDuePreset,
    clock: String,
): String {
    val normalizedClock = clock.trim()
    return if (normalizedClock.isBlank()) preset.title else "${preset.title} $normalizedClock"
}

internal fun inferTodoDuePreset(dueText: String): TodoDuePreset {
    return when {
        dueText.contains("今晚") -> TodoDuePreset.Tonight
        dueText.contains("明天") -> TodoDuePreset.Tomorrow
        dueText.contains("本周") -> TodoDuePreset.ThisWeek
        dueText.contains("下周") -> TodoDuePreset.NextWeek
        else -> TodoDuePreset.Today
    }
}

internal fun extractTodoDueClock(dueText: String): String {
    val parts = dueText.split(" ")
    return parts.getOrNull(1) ?: "21:00"
}

internal fun inferTodoReminderPreset(reminderText: String): TodoReminderPreset {
    return TodoReminderPreset.entries.firstOrNull { it.title == reminderText } ?: TodoReminderPreset.Before30Min
}

internal fun formatTodoDateTime(dateTime: LocalDateTime): String = dateTime.format(TodoDateTimeFormatter)

internal fun parseTodoDateTime(
    text: String,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = text.trim()
    val explicitDate = Regex("""(\d{4}-\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
    val clock = parseClock(Regex("""(\d{1,2}:\d{2})""").find(trimmed)?.groupValues?.getOrNull(1) ?: extractTodoClockFallback(trimmed))
        ?: LocalTime.of(21, 0)
    val date = when {
        explicitDate != null -> runCatching { LocalDate.parse(explicitDate) }.getOrNull()
        trimmed.contains("今晚") || trimmed.contains("今天") -> now.toLocalDate()
        trimmed.contains("明晚") || trimmed.contains("明天") -> now.toLocalDate().plusDays(1)
        trimmed.contains("本周") -> now.toLocalDate().plusDays((7 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        trimmed.contains("下周") -> now.toLocalDate().plusDays(7)
        trimmed.contains("周末") -> now.toLocalDate().plusDays((6 - now.dayOfWeek.value).coerceAtLeast(0).toLong())
        else -> now.toLocalDate()
    } ?: return null
    return LocalDateTime.of(date, clock)
}

internal fun parseReminderDateTime(
    reminderText: String,
    dueAt: LocalDateTime,
    now: LocalDateTime,
): LocalDateTime? {
    val trimmed = reminderText.trim()
    if (trimmed.isBlank() || trimmed == TodoReminderPreset.None.title) return null
    val reminderTime = when (trimmed) {
        TodoReminderPreset.Before10Min.title -> dueAt.minusMinutes(10)
        TodoReminderPreset.Before30Min.title -> dueAt.minusMinutes(30)
        TodoReminderPreset.Before1Hour.title -> dueAt.minusHours(1)
        TodoReminderPreset.PreviousNight.title -> LocalDateTime.of(dueAt.toLocalDate().minusDays(1), LocalTime.of(20, 0))
        else -> parseTodoDateTime(trimmed, now)
    } ?: return null
    return if (reminderTime.isAfter(now)) reminderTime else null
}

internal fun extractTodoClockFallback(text: String): String {
    return when {
        text.contains("今晚") -> "20:00"
        text.contains("明晚") -> "20:00"
        else -> "21:00"
    }
}

internal fun parseClock(raw: String): LocalTime? {
    val normalized = raw.trim()
    val parts = normalized.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59)) }.getOrNull()
}
