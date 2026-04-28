package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.poxiao.app.security.SecurePrefs
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class AcademicAccountProfile(
    val nickname: String,
    val realName: String,
    val avatarUri: String,
)

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

private fun defaultAcademicAvatar(
    displayName: String,
    studentId: String = "",
): String {
    val normalized = displayName.trim()
    if (normalized.isBlank()) {
        val idFallback = studentId.trim().takeLast(2)
        return if (idFallback.isNotBlank()) idFallback else "教务"
    }
    return normalized.take(2)
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

@Composable
private fun rememberAcademicAvatarBitmap(
    avatarUri: String,
): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(initialValue = null, context, avatarUri) {
        value = if (avatarUri.isBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    val parsedUri = Uri.parse(avatarUri)
                    when (parsedUri.scheme) {
                        "file", null -> parsedUri.path?.let { path ->
                            BitmapFactory.decodeFile(path)?.asImageBitmap()
                        }
                        else -> context.contentResolver.openInputStream(parsedUri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.asImageBitmap()
                        }
                    }
                }.getOrNull()
            }
        }
    }
    return bitmap
}

@Composable
internal fun AcademicAvatarBadge(
    displayName: String,
    studentId: String,
    avatarUri: String,
    accent: Color,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val bitmap = rememberAcademicAvatarBitmap(avatarUri)
    Surface(
        shape = CircleShape,
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.18f)),
        modifier = modifier.size(size),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = defaultAcademicAvatar(displayName, studentId),
                    style = if (size >= 60.dp) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
                    color = accent,
                )
            }
        }
    }
}
