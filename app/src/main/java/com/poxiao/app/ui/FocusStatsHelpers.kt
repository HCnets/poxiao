package com.poxiao.app.ui

import java.time.LocalDate

internal fun buildFocusDayStats(records: List<FocusRecord>): List<FocusDayStat> {
    val today = LocalDate.now()
    return (6 downTo 0).map { offset ->
        val target = today.minusDays(offset.toLong())
        val label = "${target.monthValue}.${target.dayOfMonth}"
        val minutes = records.filter { parseFocusRecordDate(it.finishedAt) == target }.map { it.seconds }.sum() / 60
        FocusDayStat(label = label, minutes = minutes)
    }
}

internal fun buildFocusTaskStats(records: List<FocusRecord>): List<FocusTaskStat> {
    return records
        .groupBy { it.taskTitle.ifBlank { "未命名专注" } }
        .map { (title, items) ->
            FocusTaskStat(
                title = title,
                minutes = items.map { it.seconds }.sum() / 60,
                count = items.size,
            )
        }
        .sortedWith(compareByDescending<FocusTaskStat> { it.minutes }.thenByDescending { it.count })
}

private fun parseFocusRecordDate(raw: String): LocalDate? {
    if (raw.isBlank()) return null
    return runCatching {
        java.time.LocalDateTime.parse(raw, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.recoverCatching {
        val currentYear = LocalDate.now().year
        java.time.LocalDateTime.parse(
            "$currentYear-$raw",
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        ).toLocalDate()
    }.getOrNull()
}
