package com.poxiao.app.ui

import android.content.SharedPreferences

internal fun loadStringList(
    prefs: SharedPreferences,
    key: String,
): List<String> {
    val raw = prefs.getString(key, "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

internal fun saveStringList(
    prefs: SharedPreferences,
    key: String,
    items: List<String>,
) {
    prefs.edit().putString(key, items.joinToString("|")).apply()
}
