package com.poxiao.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

internal fun copyLocalBackupPayload(
    context: Context,
    clipboard: ClipboardManager?,
): MoreBackupActionResult {
    val export = buildLocalBackupPayload(context)
    clipboard?.setPrimaryClip(ClipData.newPlainText("破晓本地备份", export))
    return MoreBackupActionResult(
        text = export,
        status = "已复制备份文本，可自行保存到任意位置。",
    )
}

internal fun pasteLocalBackupPayload(
    context: Context,
    clipboard: ClipboardManager?,
): MoreBackupActionResult {
    val text = clipboard?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
    return MoreBackupActionResult(
        text = text,
        status = if (text.isBlank()) "剪贴板里没有可恢复的备份文本。" else "已读取剪贴板内容，可直接执行恢复。",
    )
}

internal fun restoreLocalBackup(
    context: Context,
    backupText: String,
): String {
    return restoreLocalBackupPayload(context, backupText)
}

internal data class MoreBackupActionResult(
    val text: String,
    val status: String,
)
