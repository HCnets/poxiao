package com.poxiao.app.ui

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun formatSeconds(seconds: Int): String {
    val minute = seconds / 60
    val second = seconds % 60
    return "%02d:%02d".format(minute, second)
}

internal fun formatSyncTime(time: LocalDateTime): String {
    return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
}
