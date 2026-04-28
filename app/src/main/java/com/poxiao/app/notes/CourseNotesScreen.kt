package com.poxiao.app.notes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.poxiao.app.ui.LiquidGlassCard
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PoxiaoThemeState
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

data class CourseNoteSeed(
    val courseName: String,
    val teacher: String = "",
    val classroom: String = "",
    val courseLabel: String = "",
    val focusTitle: String = "",
)

data class CourseNoteAttachment(
    val id: String,
    val displayName: String,
    val mimeType: String,
    val relativePath: String,
    val sizeBytes: Long,
    val addedAt: Long,
)

data class CourseNote(
    val id: String,
    val courseName: String,
    val title: String,
    val content: String,
    val teacher: String,
    val classroom: String,
    val courseLabel: String,
    val tags: List<String>,
    val knowledgePoints: List<String>,
    val attachments: List<CourseNoteAttachment>,
    val createdAt: Long,
    val updatedAt: Long,
)

class CourseNoteStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("course_notes", Context.MODE_PRIVATE)
    private val attachmentRoot = File(appContext.filesDir, ATTACHMENT_DIR).apply { mkdirs() }

    companion object {
        private const val NOTES_KEY = "notes_v1"
        private const val ATTACHMENT_DIR = "course_note_assets"
    }

    fun loadNotes(): List<CourseNote> {
        val raw = prefs.getString(NOTES_KEY, "").orEmpty()
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val tagsArray = item.optJSONArray("tags") ?: JSONArray()
                    val tags = mutableListOf<String>()
                    for (tagIndex in 0 until tagsArray.length()) {
                        val tag = tagsArray.optString(tagIndex)
                        if (tag.isNotBlank()) tags += tag
                    }
                    val pointsArray = item.optJSONArray("knowledgePoints") ?: JSONArray()
                    val knowledgePoints = mutableListOf<String>()
                    for (pointIndex in 0 until pointsArray.length()) {
                        val point = pointsArray.optString(pointIndex)
                        if (point.isNotBlank()) knowledgePoints += point
                    }
                    val attachmentsArray = item.optJSONArray("attachments") ?: JSONArray()
                    val attachments = mutableListOf<CourseNoteAttachment>()
                    for (attachmentIndex in 0 until attachmentsArray.length()) {
                        val attachmentItem = attachmentsArray.optJSONObject(attachmentIndex) ?: continue
                        val relativePath = attachmentItem.optString("relativePath")
                        if (relativePath.isBlank()) continue
                        attachments += CourseNoteAttachment(
                            id = attachmentItem.optString("id"),
                            displayName = attachmentItem.optString("displayName").ifBlank { "附件" },
                            mimeType = attachmentItem.optString("mimeType"),
                            relativePath = relativePath,
                            sizeBytes = attachmentItem.optLong("sizeBytes"),
                            addedAt = attachmentItem.optLong("addedAt"),
                        )
                    }
                    add(
                        CourseNote(
                            id = item.optString("id"),
                            courseName = item.optString("courseName"),
                            title = item.optString("title"),
                            content = item.optString("content"),
                            teacher = item.optString("teacher"),
                            classroom = item.optString("classroom"),
                            courseLabel = item.optString("courseLabel"),
                            tags = tags,
                            knowledgePoints = knowledgePoints,
                            attachments = attachments,
                            createdAt = item.optLong("createdAt"),
                            updatedAt = item.optLong("updatedAt"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveNotes(notes: List<CourseNote>) {
        val array = JSONArray()
        notes.forEach { note ->
            array.put(
                JSONObject().apply {
                    put("id", note.id)
                    put("courseName", note.courseName)
                    put("title", note.title)
                    put("content", note.content)
                    put("teacher", note.teacher)
                    put("classroom", note.classroom)
                    put("courseLabel", note.courseLabel)
                    put("tags", JSONArray(note.tags))
                    put("knowledgePoints", JSONArray(note.knowledgePoints))
                    put(
                        "attachments",
                        JSONArray().apply {
                            note.attachments.forEach { attachment ->
                                put(
                                    JSONObject().apply {
                                        put("id", attachment.id)
                                        put("displayName", attachment.displayName)
                                        put("mimeType", attachment.mimeType)
                                        put("relativePath", attachment.relativePath)
                                        put("sizeBytes", attachment.sizeBytes)
                                        put("addedAt", attachment.addedAt)
                                    },
                                )
                            }
                        },
                    )
                    put("createdAt", note.createdAt)
                    put("updatedAt", note.updatedAt)
                },
            )
        }
        prefs.edit().putString(NOTES_KEY, array.toString()).apply()
    }

    fun importAttachments(noteId: String, uris: List<Uri>): List<CourseNoteAttachment> {
        return buildList {
            uris.forEach { uri ->
                val meta = queryAttachmentMeta(uri)
                val targetFile = createAttachmentFile(noteId, meta.displayName)
                val imported = runCatching {
                    appContext.contentResolver.openInputStream(uri)?.use { input ->
                        targetFile.outputStream().use { output -> input.copyTo(output) }
                    } ?: return@runCatching null
                    CourseNoteAttachment(
                        id = UUID.randomUUID().toString(),
                        displayName = meta.displayName,
                        mimeType = meta.mimeType,
                        relativePath = targetFile.relativeTo(attachmentRoot).path.replace('\\', '/'),
                        sizeBytes = meta.sizeBytes.takeIf { it > 0 } ?: targetFile.length(),
                        addedAt = System.currentTimeMillis(),
                    )
                }.getOrNull()
                if (imported != null) add(imported)
            }
        }
    }

    fun resolveAttachmentFile(relativePath: String): File {
        return File(attachmentRoot, relativePath.replace('/', File.separatorChar))
    }

    fun attachmentExists(relativePath: String): Boolean = resolveAttachmentFile(relativePath).exists()

    fun deleteAttachment(relativePath: String) {
        runCatching { resolveAttachmentFile(relativePath).delete() }
    }

    fun deleteAttachments(relativePaths: Collection<String>) {
        relativePaths.forEach(::deleteAttachment)
    }

    fun deleteNoteAttachments(noteId: String) {
        runCatching { File(attachmentRoot, noteId).deleteRecursively() }
    }

    private fun queryAttachmentMeta(uri: Uri): AttachmentMeta {
        var displayName = "附件"
        var sizeBytes = 0L
        runCatching {
            appContext.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) displayName = cursor.getString(nameIndex).orEmpty().ifBlank { displayName }
                    if (sizeIndex >= 0) sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        }
        return AttachmentMeta(
            displayName = sanitizeAttachmentName(displayName),
            mimeType = appContext.contentResolver.getType(uri).orEmpty().ifBlank { guessAttachmentMimeType(displayName) },
            sizeBytes = sizeBytes,
        )
    }

    private fun createAttachmentFile(noteId: String, displayName: String): File {
        val noteDir = File(attachmentRoot, noteId).apply { mkdirs() }
        val sanitized = sanitizeAttachmentName(displayName)
        val hasExtension = sanitized.contains('.')
        val extension = sanitized.substringAfterLast('.', "")
        val baseName = if (hasExtension) sanitized.removeSuffix(".$extension") else sanitized
        var candidate = File(noteDir, sanitized)
        var suffix = 2
        while (candidate.exists()) {
            candidate = File(
                noteDir,
                if (hasExtension) "$baseName-$suffix.$extension" else "$baseName-$suffix",
            )
            suffix += 1
        }
        return candidate
    }

    private fun sanitizeAttachmentName(name: String): String {
        val trimmed = name.trim().ifBlank { "附件" }
        return trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private data class AttachmentMeta(
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long,
    )
}

private fun guessAttachmentMimeType(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    if (extension.isBlank()) return "application/octet-stream"
    if (extension == "pdf") return "application/pdf"
    if (extension == "ppt") return "application/vnd.ms-powerpoint"
    if (extension == "pptx") return "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}

private fun Context.openCourseNoteAttachment(file: File, mimeType: String, displayName: String): Boolean {
    if (!file.exists()) return false
    val contentUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    val resolvedMimeType = mimeType.ifBlank { guessAttachmentMimeType(file.name) }
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, resolvedMimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching {
        startActivity(
            Intent.createChooser(intent, "打开${displayName.ifBlank { "附件" }}").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }.isSuccess
}

private fun Context.shareCourseNoteAttachment(file: File, mimeType: String, displayName: String): Boolean {
    if (!file.exists()) return false
    val contentUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    val resolvedMimeType = mimeType.ifBlank { guessAttachmentMimeType(file.name) }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = resolvedMimeType
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_TITLE, displayName.ifBlank { file.name })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching {
        startActivity(
            Intent.createChooser(intent, "分享${displayName.ifBlank { "附件" }}").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    }.isSuccess
}

private fun CourseNoteAttachment.readableTypeLabel(): String {
    return when {
        mimeType.startsWith("image/") -> "图片"
        mimeType.contains("pdf", ignoreCase = true) || displayName.endsWith(".pdf", true) -> "PDF"
        mimeType.contains("powerpoint", ignoreCase = true) ||
            displayName.endsWith(".ppt", true) ||
            displayName.endsWith(".pptx", true) -> "PPT"
        else -> "文件"
    }
}

@Composable
fun CourseNotesScreen(
    modifier: Modifier = Modifier,
    initialSeed: CourseNoteSeed? = null,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { CourseNoteStore(context) }
    val palette = PoxiaoThemeState.palette
    val notes = remember { mutableStateListOf<CourseNote>().apply { addAll(store.loadNotes()) } }
    var selectedCourse by remember { mutableStateOf(initialSeed?.courseName.orEmpty()) }
    var searchText by remember { mutableStateOf(initialSeed?.focusTitle.orEmpty()) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var draftNoteId by remember { mutableStateOf<String?>(null) }
    var draftCourseName by remember { mutableStateOf(initialSeed?.courseName.orEmpty()) }
    var draftTitle by remember { mutableStateOf("") }
    var draftContent by remember { mutableStateOf("") }
    var draftTeacher by remember { mutableStateOf(initialSeed?.teacher.orEmpty()) }
    var draftClassroom by remember { mutableStateOf(initialSeed?.classroom.orEmpty()) }
    var draftCourseLabel by remember { mutableStateOf(initialSeed?.courseLabel.orEmpty()) }
    var draftTags by remember { mutableStateOf("") }
    var draftKnowledgePoints by remember { mutableStateOf("") }
    var draftAttachments by remember { mutableStateOf<List<CourseNoteAttachment>>(emptyList()) }
    var existingAttachmentPaths by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removedAttachmentPaths by remember { mutableStateOf<Set<String>>(emptySet()) }
    var statusText by remember { mutableStateOf("课程笔记会保存在本地，后续可直接接入 hoa.moe。") }

    LaunchedEffect(initialSeed?.courseName, initialSeed?.focusTitle) {
        if (initialSeed != null) {
            selectedCourse = initialSeed.courseName
            if (draftCourseName.isBlank()) draftCourseName = initialSeed.courseName
            if (draftTeacher.isBlank()) draftTeacher = initialSeed.teacher
            if (draftClassroom.isBlank()) draftClassroom = initialSeed.classroom
            if (draftCourseLabel.isBlank()) draftCourseLabel = initialSeed.courseLabel
            if (searchText.isBlank() && initialSeed.focusTitle.isNotBlank()) {
                searchText = initialSeed.focusTitle
            }
        }
    }

    val allCourseNames = remember(notes.toList(), initialSeed?.courseName) {
        val courseNames = mutableSetOf<String>()
        if (initialSeed?.courseName?.isNotBlank() == true) courseNames += initialSeed.courseName
        notes.forEach { if (it.courseName.isNotBlank()) courseNames += it.courseName }
        courseNames.toList().sorted()
    }
    val filteredNotes = remember(notes.toList(), selectedCourse, searchText) {
        notes
            .filter { selectedCourse.isBlank() || it.courseName == selectedCourse }
            .filter { note ->
                searchText.isBlank() ||
                    note.courseName.contains(searchText, true) ||
                    note.title.contains(searchText, true) ||
                    note.content.contains(searchText, true) ||
                    note.tags.any { tag -> tag.contains(searchText, true) } ||
                    note.knowledgePoints.any { point -> point.contains(searchText, true) }
            }
            .sortedByDescending { it.updatedAt }
    }
    val groupedNotes = remember(filteredNotes) { filteredNotes.groupBy { it.courseName } }
    val currentDraftTags = remember(draftTags) {
        draftTags
            .split("，", ",", " ", "\n", "\t")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
    val suggestedDraftTags = remember(draftTitle, draftContent, draftTags) {
        extractNoteTagSuggestions(draftTitle, draftContent, currentDraftTags)
    }
    val parsedDraftKnowledgePoints = remember(draftKnowledgePoints) {
        draftKnowledgePoints
            .split("\n", "；", ";")
            .map { it.trim().trimStart('-', '*', '•', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '、') }
            .map { it.trim() }
            .filter { it.length >= 2 }
            .distinct()
    }
    val suggestedKnowledgePoints = remember(draftTitle, draftContent, draftKnowledgePoints) {
        extractStructuredKnowledgePoints(draftTitle, draftContent, parsedDraftKnowledgePoints)
    }

    val attachmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val noteId = draftNoteId ?: editingNoteId ?: UUID.randomUUID().toString().also { draftNoteId = it }
        val imported = store.importAttachments(noteId, uris)
        if (imported.isEmpty()) {
            statusText = "附件导入失败，请重试。"
        } else {
            draftAttachments = (draftAttachments + imported).distinctBy { it.relativePath }
            statusText = "已插入 ${imported.size} 个附件。"
        }
    }

    fun openAttachment(attachment: CourseNoteAttachment) {
        val opened = context.openCourseNoteAttachment(
            file = store.resolveAttachmentFile(attachment.relativePath),
            mimeType = attachment.mimeType,
            displayName = attachment.displayName,
        )
        statusText = if (opened) {
            "已打开 ${attachment.displayName}。"
        } else {
            "当前设备没有可读取该附件的应用，或附件已丢失。"
        }
    }

    fun shareAttachment(attachment: CourseNoteAttachment) {
        val shared = context.shareCourseNoteAttachment(
            file = store.resolveAttachmentFile(attachment.relativePath),
            mimeType = attachment.mimeType,
            displayName = attachment.displayName,
        )
        statusText = if (shared) {
            "已发起分享：${attachment.displayName}。"
        } else {
            "附件已丢失，或当前设备无法分享该文件。"
        }
    }

    fun clearDraft(keepCourse: Boolean = true, discardTransientAttachments: Boolean = true) {
        if (discardTransientAttachments) {
            draftAttachments
                .filterNot { existingAttachmentPaths.contains(it.relativePath) }
                .forEach { store.deleteAttachment(it.relativePath) }
        }
        editingNoteId = null
        draftNoteId = null
        draftTitle = ""
        draftContent = ""
        draftTags = ""
        draftKnowledgePoints = ""
        draftAttachments = emptyList()
        existingAttachmentPaths = emptySet()
        removedAttachmentPaths = emptySet()
        if (!keepCourse) draftCourseName = initialSeed?.courseName.orEmpty()
        draftTeacher = if (keepCourse && draftCourseName == initialSeed?.courseName) initialSeed?.teacher.orEmpty() else ""
        draftClassroom = if (keepCourse && draftCourseName == initialSeed?.courseName) initialSeed?.classroom.orEmpty() else ""
        draftCourseLabel = if (keepCourse && draftCourseName == initialSeed?.courseName) initialSeed?.courseLabel.orEmpty() else ""
    }

    fun persistNotes(updated: List<CourseNote>) {
        notes.clear()
        notes.addAll(updated.sortedByDescending { it.updatedAt })
        store.saveNotes(notes)
    }

    fun startEdit(note: CourseNote) {
        editingNoteId = note.id
        draftNoteId = note.id
        draftCourseName = note.courseName
        draftTitle = note.title
        draftContent = note.content
        draftTeacher = note.teacher
        draftClassroom = note.classroom
        draftCourseLabel = note.courseLabel
        draftTags = note.tags.joinToString("，")
        draftKnowledgePoints = note.knowledgePoints.joinToString("\n")
        draftAttachments = note.attachments
        existingAttachmentPaths = note.attachments.map { it.relativePath }.toSet()
        removedAttachmentPaths = emptySet()
        selectedCourse = note.courseName
        statusText = "正在编辑《${note.title}》。"
    }

    fun saveDraft() {
        if (draftCourseName.isBlank() || draftTitle.isBlank() || draftContent.isBlank()) {
            statusText = "请先填写课程、标题和正文。"
            return
        }
        val now = System.currentTimeMillis()
        val noteId = editingNoteId ?: draftNoteId ?: UUID.randomUUID().toString()
        val parsedTags = draftTags
            .split("，", ",", " ", "\n", "\t")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val updated = notes.toMutableList()
        val existingIndex = updated.indexOfFirst { it.id == editingNoteId }
        val createdAt = if (existingIndex >= 0) updated[existingIndex].createdAt else now
        val note = CourseNote(
            id = noteId,
            courseName = draftCourseName.trim(),
            title = draftTitle.trim(),
            content = draftContent.trim(),
            teacher = draftTeacher.trim(),
            classroom = draftClassroom.trim(),
            courseLabel = draftCourseLabel.trim(),
            tags = parsedTags,
            knowledgePoints = parsedDraftKnowledgePoints,
            attachments = draftAttachments,
            createdAt = createdAt,
            updatedAt = now,
        )
        if (existingIndex >= 0) {
            updated[existingIndex] = note
            statusText = "已更新《${note.title}》。"
        } else {
            updated.add(note)
            statusText = "已新增《${note.title}》。"
        }
        persistNotes(updated)
        store.deleteAttachments(removedAttachmentPaths)
        selectedCourse = note.courseName
        clearDraft(discardTransientAttachments = false)
    }

    fun deleteEditing() {
        val targetId = editingNoteId ?: return
        val removed = notes.firstOrNull { it.id == targetId } ?: return
        persistNotes(notes.filterNot { it.id == targetId })
        store.deleteNoteAttachments(targetId)
        clearDraft(discardTransientAttachments = false)
        statusText = "已删除《${removed.title}》。"
    }

    fun toggleDraftTag(tag: String) {
        draftTags = if (currentDraftTags.contains(tag)) {
            currentDraftTags.filterNot { it == tag }.joinToString("，")
        } else {
            (currentDraftTags + tag).distinct().joinToString("，")
        }
    }

    fun removeDraftAttachment(attachment: CourseNoteAttachment) {
        draftAttachments = draftAttachments.filterNot { it.id == attachment.id }
        if (existingAttachmentPaths.contains(attachment.relativePath)) {
            removedAttachmentPaths = removedAttachmentPaths + attachment.relativePath
        } else {
            store.deleteAttachment(attachment.relativePath)
        }
        statusText = "已移除 ${attachment.displayName}。"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.backgroundTop.copy(alpha = 0.94f),
                        palette.backgroundBottom.copy(alpha = 0.98f),
                    ),
                ),
            ),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            NotesGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(0.76f),
                    ) {
                        Text("课程笔记", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        Text(
                            if (initialSeed == null) {
                                "按课程沉淀课堂要点、复习摘要与资料线索。"
                            } else {
                                "已从 ${initialSeed.courseName} 进入，可直接记录本门课的笔记。"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.softText,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.secondary,
                            contentColor = palette.pillOn,
                        ),
                    ) {
                        Text("返回")
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryPill(title = "课程", value = allCourseNames.size.toString())
                    SummaryPill(title = "笔记", value = notes.size.toString())
                    SummaryPill(title = "筛选", value = if (selectedCourse.isBlank()) "全部" else "单课")
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(statusText, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
            }
        }
        item {
            NotesGlassCard {
                Text("笔记编辑", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = draftCourseName,
                    onValueChange = { draftCourseName = it },
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftTitle,
                    onValueChange = { draftTitle = it },
                    label = { Text("笔记标题") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftTeacher,
                    onValueChange = { draftTeacher = it },
                    label = { Text("教师") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftClassroom,
                    onValueChange = { draftClassroom = it },
                    label = { Text("教室") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draftCourseLabel,
                    onValueChange = { draftCourseLabel = it },
                    label = { Text("课内标记") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftTags,
                    onValueChange = { draftTags = it },
                    label = { Text("标签，支持多个") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                if (currentDraftTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        currentDraftTags.forEach { tag ->
                            CourseFilterChip(
                                title = "$tag 移除",
                                selected = true,
                                onClick = { toggleDraftTag(tag) },
                            )
                        }
                    }
                }
                if (suggestedDraftTags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("标签建议", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        suggestedDraftTags.forEach { tag ->
                            CourseFilterChip(
                                title = tag,
                                selected = currentDraftTags.contains(tag),
                                onClick = { toggleDraftTag(tag) },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftContent,
                    onValueChange = { draftContent = it },
                    label = { Text("正文") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    minLines = 5,
                    maxLines = 10,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("资料附件", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        attachmentPicker.launch(
                            arrayOf(
                                "image/*",
                                "application/pdf",
                                "application/vnd.ms-powerpoint",
                                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            ),
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.secondary,
                        contentColor = palette.pillOn,
                    ),
                ) {
                    Text("插入图片 / PPT / PDF")
                }
                if (draftAttachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        draftAttachments.forEach { attachment ->
                            CourseNoteAttachmentRow(
                                attachment = attachment,
                                available = store.attachmentExists(attachment.relativePath),
                                onOpen = { openAttachment(attachment) },
                                onShare = { shareAttachment(attachment) },
                                onRemove = { removeDraftAttachment(attachment) },
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "支持插入图片、PPT、PDF，保存后可直接打开。",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.softText,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draftKnowledgePoints,
                    onValueChange = { draftKnowledgePoints = it },
                    label = { Text("知识点结构化编辑") },
                    placeholder = { Text("一行一个知识点，或用分号分隔") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    minLines = 4,
                    maxLines = 8,
                )
                if (parsedDraftKnowledgePoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("当前知识点", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        parsedDraftKnowledgePoints.forEach { point ->
                            CourseFilterChip(
                                title = "$point 移除",
                                selected = true,
                                onClick = {
                                    draftKnowledgePoints = parsedDraftKnowledgePoints
                                        .filterNot { it == point }
                                        .joinToString("\n")
                                },
                            )
                        }
                    }
                }
                if (suggestedKnowledgePoints.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("知识点建议", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        suggestedKnowledgePoints.forEach { point ->
                            CourseFilterChip(
                                title = point,
                                selected = parsedDraftKnowledgePoints.contains(point),
                                onClick = {
                                    draftKnowledgePoints = (parsedDraftKnowledgePoints + point)
                                        .distinct()
                                        .joinToString("\n")
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = ::saveDraft,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.primary,
                            contentColor = palette.pillOn,
                        ),
                    ) {
                        Text(if (editingNoteId == null) "保存笔记" else "保存修改")
                    }
                    OutlinedButton(onClick = { clearDraft(keepCourse = true) }, shape = RoundedCornerShape(18.dp)) {
                        Text("清空表单")
                    }
                    if (editingNoteId != null) {
                        OutlinedButton(onClick = ::deleteEditing, shape = RoundedCornerShape(18.dp)) {
                            Text("删除笔记")
                        }
                    }
                }
            }
        }
        item {
            NotesGlassCard {
                Text("检索与分组", style = MaterialTheme.typography.titleLarge, color = palette.ink)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("搜索课程、标题、正文或标签") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CourseFilterChip(
                        title = "全部课程",
                        selected = selectedCourse.isBlank(),
                        onClick = { selectedCourse = "" },
                    )
                    allCourseNames.forEach { course ->
                        CourseFilterChip(
                            title = course,
                            selected = selectedCourse == course,
                            onClick = { selectedCourse = course },
                        )
                    }
                }
            }
        }
        if (groupedNotes.isEmpty()) {
            item {
                NotesGlassCard {
                    Text("暂时还没有匹配到笔记", style = MaterialTheme.typography.titleMedium, color = palette.ink)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (notes.isEmpty()) {
                            "可以先从课程详情进入，也可以直接在这里新建。"
                        } else {
                            "试试切换课程筛选或修改搜索词。"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = palette.softText,
                    )
                }
            }
        } else {
            groupedNotes.forEach { (courseName, courseNotes) ->
                item {
                    Text(courseName, style = MaterialTheme.typography.titleLarge, color = palette.ink)
                }
                items(courseNotes, key = { it.id }) { note ->
                    CourseNoteCard(
                        note = note,
                        onEdit = { startEdit(note) },
                        onOpenAttachment = ::openAttachment,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotesGlassCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 30.dp,
        contentPadding = PaddingValues(18.dp),
        tint = palette.card.copy(alpha = 0.34f),
        borderColor = palette.cardBorder.copy(alpha = 0.82f),
        glowColor = palette.cardGlow.copy(alpha = 0.22f),
        blurRadius = 12.dp,
        refractionHeight = 12.dp,
        refractionAmount = 18.dp,
        content = content,
    )
}

@Composable
private fun SummaryPill(title: String, value: String) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.8f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = palette.softText)
            Text(value, style = MaterialTheme.typography.titleMedium, color = palette.ink)
        }
    }
}

@Composable
private fun CourseFilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) palette.primary.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, if (selected) palette.primary.copy(alpha = 0.2f) else palette.cardBorder),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = if (selected) palette.pillOn else palette.ink,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CourseNoteCard(
    note: CourseNote,
    onEdit: () -> Unit,
    onOpenAttachment: (CourseNoteAttachment) -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.86f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.76f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(note.title, style = MaterialTheme.typography.titleMedium, color = palette.ink, fontWeight = FontWeight.SemiBold)
                    Text(
                        listOf(note.teacher, note.classroom, note.courseLabel).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "本地课程笔记" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.softText,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(onClick = onEdit, shape = RoundedCornerShape(18.dp)) {
                    Text("编辑")
                }
            }
            Text(
                note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = ForestDeep.copy(alpha = 0.86f),
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            val knowledgePreview = remember(note.knowledgePoints, note.content) {
                if (note.knowledgePoints.isNotEmpty()) note.knowledgePoints.take(3) else extractKnowledgePointPreview(note.content)
            }
            if (knowledgePreview.isNotEmpty()) {
                Text(
                            "知识点：${knowledgePreview.joinToString(" · ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.softText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (note.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    note.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(tag, style = MaterialTheme.typography.labelMedium, color = palette.softText)
                        }
                    }
                }
            }
            if (note.attachments.isNotEmpty()) {
                Text(
                    "附件 ${note.attachments.size} 个",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.softText,
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    note.attachments.forEach { attachment ->
                        AttachmentChip(
                            title = "${attachment.readableTypeLabel()} · ${attachment.displayName}",
                            onClick = { onOpenAttachment(attachment) },
                        )
                    }
                }
            }
            Text(
                "更新于 ${note.updatedAt.toDisplayTime()}",
                style = MaterialTheme.typography.bodySmall,
                color = palette.softText,
            )
        }
    }
}

@Composable
private fun CourseNoteAttachmentRow(
    attachment: CourseNoteAttachment,
    available: Boolean,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.8f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.62f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    attachment.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.ink,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    buildString {
                        append(attachment.readableTypeLabel())
                        append(" · ")
                        append(attachment.sizeBytes.toReadableSize())
                        if (!available) append(" · 文件缺失")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.softText,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpen, enabled = available, shape = RoundedCornerShape(16.dp)) {
                    Text("打开")
                }
                OutlinedButton(onClick = onShare, enabled = available, shape = RoundedCornerShape(16.dp)) {
                    Text("分享")
                }
                OutlinedButton(onClick = onRemove, shape = RoundedCornerShape(16.dp)) {
                    Text("移除")
                }
            }
        }
    }
}

@Composable
private fun AttachmentChip(
    title: String,
    onClick: () -> Unit,
) {
    val palette = PoxiaoThemeState.palette
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, palette.cardBorder.copy(alpha = 0.82f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = palette.softText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun Long.toDisplayTime(): String {
    if (this <= 0L) return "刚刚"
    return runCatching {
        DateTimeFormatter.ofPattern("MM月dd日 HH:mm")
            .format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime())
    }.getOrDefault("刚刚")
}

private fun Long.toReadableSize(): String {
    if (this <= 0L) return "未知大小"
    val size = this.toDouble()
    return when {
        size >= 1024 * 1024 -> String.format("%.1f MB", size / (1024 * 1024))
        size >= 1024 -> String.format("%.0f KB", size / 1024)
        else -> "${this} B"
    }
}

private fun extractNoteTagSuggestions(
    title: String,
    content: String,
    existingTags: List<String>,
): List<String> {
    val source = "$title\n$content"
    val suggestions = buildList {
        if (source.contains("定义")) add("定义")
        if (source.contains("公式") || source.contains("=") || source.contains("定理")) add("公式")
        if (source.contains("例题") || source.contains("习题")) add("例题")
        if (source.contains("实验") || source.contains("操作")) add("实验")
        if (source.contains("作业")) add("作业")
        if (source.contains("考试") || source.contains("必考", true) || source.contains("重点")) add("重点")
        if (source.contains("难点") || source.contains("证明")) add("难点")
        if (source.contains("易错") || source.contains("错题")) add("错题")
        if (source.contains("总结") || source.contains("结论")) add("总结")
        if (source.contains("复习")) add("复习")
    }
    return suggestions.distinct().filterNot { existingTags.contains(it) }.take(8)
}

private fun extractStructuredKnowledgePoints(
    title: String,
    content: String,
    existingPoints: List<String>,
): List<String> {
    val candidates = mutableListOf<String>()
    if (title.isNotBlank()) candidates += title
    candidates += content
        .replace("\r", "\n")
        .replace("；", "\n")
        .replace(";", "\n")
        .lines()
        .map { it.trim().trimStart('-', '*', '•', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '、') }
        .map { it.trim() }
        .filter { it.length >= 4 }
    return candidates
        .map { it.replace(Regex("\\s+"), " ").trim() }
        .filter { it.isNotBlank() && it !in existingPoints }
        .distinct()
        .take(6)
}

private fun extractKnowledgePointPreview(content: String): List<String> {
    return content
        .lines()
        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
        .filter { it.isNotBlank() }
        .take(3)
}
