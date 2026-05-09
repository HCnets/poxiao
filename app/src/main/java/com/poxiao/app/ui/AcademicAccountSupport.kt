package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.poxiao.app.security.SecurePrefs
import java.io.File

internal fun loadAcademicAccountProfile(
    prefs: SharedPreferences,
    studentId: String? = null,
): AcademicAccountProfile {
    val normalizedStudentId = studentId.orEmpty().trim()
    if (normalizedStudentId.isBlank()) return AcademicAccountProfile("", "", "")
    fun key(name: String): String = "${name}_$normalizedStudentId"
    return AcademicAccountProfile(
        nickname = prefs.getString(key("academic_account_nickname"), "").orEmpty(),
        realName = prefs.getString(key("academic_account_real_name"), "").orEmpty(),
        avatarUri = prefs.getString(key("academic_account_avatar_uri"), "").orEmpty(),
    )
}

internal fun saveAcademicAccountProfile(
    prefs: SharedPreferences,
    profile: AcademicAccountProfile,
    studentId: String? = null,
) {
    val normalizedStudentId = studentId.orEmpty().trim()
    if (normalizedStudentId.isBlank()) return
    fun key(name: String): String = "${name}_$normalizedStudentId"
    prefs.edit()
        .putString(key("academic_account_nickname"), profile.nickname.trim())
        .putString(key("academic_account_real_name"), profile.realName.trim())
        .putString(key("academic_account_avatar_uri"), profile.avatarUri.trim())
        .remove("academic_account_name")
        .remove("academic_account_avatar")
        .apply()
}

private fun loadLegacyAcademicAccountProfile(
    prefs: SharedPreferences,
): AcademicAccountProfile {
    val legacyAvatar = prefs.getString("academic_account_avatar", "").orEmpty().trim()
    return AcademicAccountProfile(
        nickname = prefs.getString("academic_account_nickname", "").orEmpty().ifBlank {
            prefs.getString("academic_account_name", "").orEmpty()
        },
        realName = prefs.getString("academic_account_real_name", "").orEmpty(),
        avatarUri = if (legacyAvatar.startsWith("content://") || legacyAvatar.startsWith("file://")) legacyAvatar else "",
    )
}

private fun isAcademicAccountProfileEmpty(
    profile: AcademicAccountProfile,
): Boolean {
    return profile.nickname.isBlank() && profile.realName.isBlank() && profile.avatarUri.isBlank()
}

private fun clearLegacyAcademicAccountProfile(
    prefs: SharedPreferences,
) {
    prefs.edit()
        .remove("academic_account_nickname")
        .remove("academic_account_real_name")
        .remove("academic_account_avatar_uri")
        .remove("academic_account_avatar")
        .remove("academic_account_name")
        .apply()
}

internal fun resolveAcademicAccountProfile(
    prefs: SharedPreferences,
    studentId: String,
): AcademicAccountProfile {
    val normalized = studentId.trim()
    if (!isLikelyCompleteAcademicStudentId(normalized)) return AcademicAccountProfile("", "", "")
    val specific = loadAcademicAccountProfile(prefs, normalized)
    if (!isAcademicAccountProfileEmpty(specific)) return specific
    val legacy = loadLegacyAcademicAccountProfile(prefs)
    if (isAcademicAccountProfileEmpty(legacy)) return specific
    saveAcademicAccountProfile(prefs, legacy, normalized)
    addKnownAcademicAccountId(prefs, normalized)
    clearLegacyAcademicAccountProfile(prefs)
    return legacy
}

internal fun setAcademicAccountBound(
    prefs: SharedPreferences,
    bound: Boolean,
) {
    prefs.edit().putBoolean("academic_account_bound", bound).commit()
}

internal fun setBoundAcademicStudentId(
    prefs: SharedPreferences,
    studentId: String,
) {
    prefs.edit().putString("academic_account_bound_student_id", studentId.trim()).commit()
}

internal fun getPersistedAcademicStudentId(
    prefs: SharedPreferences,
): String {
    return SecurePrefs.getString(prefs, "student_id_secure", "student_id").trim()
}

private fun loadKnownAcademicAccountIds(
    prefs: SharedPreferences,
): Set<String> {
    return prefs.getStringSet("academic_account_known_ids", emptySet()).orEmpty().toSet()
}

internal fun addKnownAcademicAccountId(
    prefs: SharedPreferences,
    studentId: String,
) {
    val normalized = studentId.trim()
    if (!isLikelyCompleteAcademicStudentId(normalized)) return
    val updated = loadKnownAcademicAccountIds(prefs).toMutableSet().apply { add(normalized) }
    prefs.edit().putStringSet("academic_account_known_ids", updated).commit()
}

internal fun clearCurrentAcademicBinding(
    prefs: SharedPreferences,
) {
    prefs.edit()
        .remove("remember_password")
        .putBoolean("academic_account_bound", false)
        .remove("academic_account_bound_student_id")
        .remove("student_id_secure")
        .remove("student_id")
        .remove("password_secure")
        .remove("password")
        .commit()
}

internal fun isLikelyCompleteAcademicStudentId(
    studentId: String,
): Boolean {
    val normalized = studentId.trim()
    return normalized.length == 10 && normalized.all { it.isDigit() }
}

internal fun hasAcademicAccountProfile(
    prefs: SharedPreferences,
    studentId: String,
): Boolean {
    val normalized = studentId.trim()
    if (!isLikelyCompleteAcademicStudentId(normalized)) return false
    val profile = resolveAcademicAccountProfile(prefs, normalized)
    val hasStoredProfile = profile.nickname.isNotBlank() || profile.realName.isNotBlank() || profile.avatarUri.isNotBlank()
    return hasStoredProfile && (
        normalized in loadKnownAcademicAccountIds(prefs) ||
            prefs.contains("academic_account_nickname_$normalized") ||
            prefs.contains("academic_account_real_name_$normalized") ||
            prefs.contains("academic_account_avatar_uri_$normalized")
    )
}

internal fun persistAcademicCredentials(
    prefs: SharedPreferences,
    studentId: String,
    password: String,
    rememberPassword: Boolean,
) {
    prefs.edit().putBoolean("remember_password", rememberPassword).apply()
    SecurePrefs.putString(prefs, "student_id_secure", studentId.trim())
    if (rememberPassword) {
        SecurePrefs.putString(prefs, "password_secure", password)
    } else {
        SecurePrefs.remove(prefs, "password_secure", "password")
    }
}

internal fun persistAcademicAvatarImage(
    context: Context,
    sourceUri: Uri,
): String? {
    return runCatching {
        val avatarDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val avatarFile = File(avatarDir, "academic_account_avatar")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            avatarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        Uri.fromFile(avatarFile).toString()
    }.getOrNull()
}
