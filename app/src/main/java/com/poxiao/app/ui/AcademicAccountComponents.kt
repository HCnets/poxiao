package com.poxiao.app.ui

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
