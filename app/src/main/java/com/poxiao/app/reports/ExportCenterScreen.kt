package com.poxiao.app.reports

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.ui.LiquidGlassCard
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PoxiaoThemePreset
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.TeaGreen
import com.poxiao.app.ui.theme.WarmMist
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

private enum class ExportBundle(
    val title: String,
    val subtitle: String,
    val accent: Color,
) {
    Schedule("课表摘要", "导出当前课程安排与日程重点。", ForestGreen),
    Review("复习计划摘要", "导出今日复习、错题来源与冲刺重点。", TeaGreen),
    Grade("成绩趋势摘要", "导出成绩表现、状态与学期概览。", MossGreen),
    Learning("学习数据摘要", "导出课表、待办、专注、成绩与笔记总览。", ForestGreen),
    Todo("待办周报", "导出待办推进、目标任务和重点事项。", Ginkgo),
    Focus("专注周报", "导出专注时长、任务投入与近期走势。", TeaGreen),
}

private enum class ExportTemplate(val title: String) {
    Brief("简报版"),
    Poster("海报版"),
    Minimal("极简版"),
}

private enum class ExportRange(val title: String) {
    Today("仅今天"),
    ThisWeek("本周"),
    Recent7Days("最近 7 天"),
    All("全部摘要"),
}

private data class ExportItem(
    val bundle: ExportBundle,
    val previewCount: String,
    val preview: String,
    val fullText: String,
    val targets: List<String> = listOf("全部"),
)

private data class ExportThemeSpec(
    val badge: String,
    val start: Int,
    val mid: Int,
    val end: Int,
    val panel: Int,
    val stroke: Int,
    val title: Int,
    val body: Int,
    val footer: Int,
)

private data class ExportHistoryRecord(
    val bundleName: String,
    val action: String,
    val templateName: String,
    val rangeName: String,
    val target: String,
    val createdAt: Long,
)

private data class ExportSaveRequest(
    val bundle: ExportBundle,
    val fileName: String,
    val content: String,
)

@Composable
fun ExportCenterScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val palette = PoxiaoThemeState.palette
    val clipboard = remember { context.getSystemService(ClipboardManager::class.java) }
    val noteStore = remember { CourseNoteStore(context) }
    val theme = remember { loadThemeSpec(context) }
    val exportPrefs = remember {
        context.getSharedPreferences("export_center_prefs", Context.MODE_PRIVATE)
    }

    val initialTemplate = remember(exportPrefs) {
        runCatching {
            ExportTemplate.valueOf(
                exportPrefs.getString("template", ExportTemplate.Brief.name).orEmpty()
            )
        }.getOrDefault(ExportTemplate.Brief)
    }
    val initialRange = remember(exportPrefs) {
        runCatching {
            ExportRange.valueOf(
                exportPrefs.getString("range", ExportRange.ThisWeek.name).orEmpty()
            )
        }.getOrDefault(ExportRange.ThisWeek)
    }

    var selectedTemplate by remember { mutableStateOf(initialTemplate) }
    var selectedRange by remember { mutableStateOf(initialRange) }
    var pendingSave by remember { mutableStateOf<ExportSaveRequest?>(null) }
    val targetSelections = remember {
        mutableStateMapOf<String, String>().apply {
            ExportBundle.entries.forEach { bundle ->
                val stored = exportPrefs.getString("target_${bundle.name}", null)
                if (!stored.isNullOrBlank()) {
                    put(bundle.name, stored)
                }
            }
        }
    }
    val history = remember { mutableStateOf(loadExportHistory(exportPrefs)) }
    val items = buildItems(context, noteStore, selectedRange, targetSelections)
    var statusText by remember { mutableStateOf("可复制文本、系统分享，或生成图片卡。") }

    fun recordHistory(bundle: ExportBundle, action: String) {
        val record = ExportHistoryRecord(
            bundleName = bundle.title,
            action = action,
            templateName = selectedTemplate.title,
            rangeName = selectedRange.title,
            target = targetSelections[bundle.name] ?: "全部",
            createdAt = System.currentTimeMillis(),
        )
        val updated = listOf(record) + history.value
        history.value = updated.take(8)
        persistExportHistory(exportPrefs, history.value)
    }

    val createTextDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        val request = pendingSave
        pendingSave = null
        if (request == null) return@rememberLauncherForActivityResult
        if (uri == null) {
            statusText = "已取消保存“${request.bundle.title}”导出文件。"
            return@rememberLauncherForActivityResult
        }
        if (writeExportText(context, uri, request.content)) {
            recordHistory(request.bundle, "保存文件")
            statusText = "已保存“${request.bundle.title}”文本文件。"
        } else {
            statusText = "保存“${request.bundle.title}”文件失败，请稍后重试。"
        }
    }

    fun restoreHistory(record: ExportHistoryRecord) {
        selectedTemplate = ExportTemplate.entries.firstOrNull { it.title == record.templateName } ?: selectedTemplate
        selectedRange = ExportRange.entries.firstOrNull { it.title == record.rangeName } ?: selectedRange
        val bundle = ExportBundle.entries.firstOrNull { it.title == record.bundleName }
        if (bundle != null && record.target.isNotBlank()) {
            targetSelections[bundle.name] = record.target
            exportPrefs.edit().putString("target_${bundle.name}", record.target).apply()
        }
        exportPrefs.edit()
            .putString("template", selectedTemplate.name)
            .putString("range", selectedRange.name)
            .apply()
        statusText = "已恢复“${record.bundleName}”上次导出预设。"
    }

    fun copyText(label: String, value: String) {
        clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
        ExportBundle.entries.firstOrNull { it.title == label }?.let { recordHistory(it, "复制文本") }
        statusText = "已复制“$label”文本摘要。"
    }

    fun shareText(title: String, value: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, value)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "分享$title"))
        }.onSuccess {
            ExportBundle.entries.firstOrNull { it.title == title }?.let { recordHistory(it, "系统分享") }
            statusText = "已打开“$title”的系统分享面板。"
        }.onFailure {
            statusText = "系统分享暂时不可用，请稍后重试。"
        }
    }

    fun shareCard(item: ExportItem) {
        val uri = createImageCard(context, item, theme, selectedTemplate)
        if (uri == null) {
            statusText = "图片卡生成失败，请稍后重试。"
            return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(intent, "分享${item.bundle.title}"))
        }.onSuccess {
            recordHistory(item.bundle, "分享卡片")
            statusText = "已生成“${item.bundle.title} ${selectedTemplate.title}”图片卡。"
        }.onFailure {
            statusText = "系统分享暂时不可用，但图片卡已生成到缓存目录。"
        }
    }

    fun saveText(item: ExportItem) {
        val target = targetSelections[item.bundle.name] ?: item.targets.firstOrNull().orEmpty().ifBlank { "全部" }
        val fileName = buildExportFileName(item.bundle.title, selectedRange.title, target)
        pendingSave = ExportSaveRequest(
            bundle = item.bundle,
            fileName = fileName,
            content = buildExportFileContent(item, selectedTemplate, selectedRange, target),
        )
        createTextDocument.launch(fileName)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        palette.backgroundTop.copy(alpha = 0.95f),
                        palette.backgroundBottom.copy(alpha = 0.98f),
                    )
                )
            ),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ExportGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.76f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("导出中心", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        Text(
                            "支持主题联动、版式切换、范围筛选和对象定向导出。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.softText,
                        )
                    }
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmMist,
                            contentColor = palette.ink,
                        ),
                    ) { Text("返回") }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(statusText, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                if (history.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("最近导出", style = MaterialTheme.typography.titleMedium, color = palette.ink)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExportChipRow(
                        options = history.value.map {
                    "${it.bundleName} · ${it.action} · ${it.templateName}"
                        },
                        selected = "",
                        active = palette.primary,
                        idle = palette.secondary,
                    ) { clicked ->
                        val record = history.value.firstOrNull {
                    "${it.bundleName} · ${it.action} · ${it.templateName}" == clicked
                        }
                        if (record != null) {
                            restoreHistory(record)
                        } else {
                            statusText = "最近导出：$clicked"
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ExportChipRow(
                    options = ExportTemplate.entries.map { it.title },
                    selected = selectedTemplate.title,
                    active = palette.primary,
                    idle = palette.secondary,
                ) { clicked ->
                    selectedTemplate = ExportTemplate.entries.first { it.title == clicked }
                    exportPrefs.edit().putString("template", selectedTemplate.name).apply()
                    statusText = "当前导出模板已切换为“$clicked”。"
                }
                Spacer(modifier = Modifier.height(10.dp))
                ExportChipRow(
                    options = ExportRange.entries.map { it.title },
                    selected = selectedRange.title,
                    active = palette.primary,
                    idle = palette.secondary,
                ) { clicked ->
                    selectedRange = ExportRange.entries.first { it.title == clicked }
                    exportPrefs.edit().putString("range", selectedRange.name).apply()
                    statusText = "当前导出范围已切换为“$clicked”。"
                }
            }
        }

        items(items, key = { it.bundle.name }) { item ->
            ExportGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.72f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(item.bundle.title, style = MaterialTheme.typography.titleLarge, color = palette.ink)
                        Text(item.bundle.subtitle, style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                    }
                    Text(item.previewCount, style = MaterialTheme.typography.titleMedium, color = item.bundle.accent)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.softText,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.targets.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ExportChipRow(
                        options = item.targets,
                        selected = targetSelections[item.bundle.name] ?: item.targets.first(),
                        active = item.bundle.accent,
                        idle = palette.secondary,
                    ) { clicked ->
                        targetSelections[item.bundle.name] = clicked
                        exportPrefs.edit().putString("target_${item.bundle.name}", clicked).apply()
                        statusText = "“${item.bundle.title}”已切换到对象“$clicked”。"
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExportActionPill("复制文本", item.bundle.accent) { copyText(item.bundle.title, item.fullText) }
                    ExportActionPill("保存文件", Ginkgo) { saveText(item) }
                    ExportActionPill("系统分享", palette.secondary) { shareText(item.bundle.title, item.fullText) }
                    ExportActionPill("分享卡片", palette.primary) { shareCard(item) }
                }
            }
        }
    }
}

@Composable
private fun ExportGlassCard(content: @Composable ColumnScope.() -> Unit) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        contentPadding = PaddingValues(18.dp),
        tint = palette.card.copy(alpha = 0.34f),
        borderColor = palette.cardBorder.copy(alpha = 0.8f),
        glowColor = palette.cardGlow.copy(alpha = 0.22f),
        blurRadius = 12.dp,
        refractionHeight = 12.dp,
        refractionAmount = 18.dp,
        content = content,
    )
}

@Composable
private fun ExportActionPill(title: String, tint: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = 0.92f),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun ExportChipRow(
    options: List<String>,
    selected: String,
    active: Color,
    idle: Color,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            ExportActionPill(
                title = option,
                tint = if (option == selected) active else idle,
                onClick = { onClick(option) },
            )
        }
    }
}

private fun buildItems(
    context: Context,
    noteStore: CourseNoteStore,
    range: ExportRange,
    targets: Map<String, String>,
): List<ExportItem> {
    return listOf(
        buildScheduleItem(context, range, targets[ExportBundle.Schedule.name] ?: "全部"),
        buildGradeItem(context, range, targets[ExportBundle.Grade.name] ?: "全部"),
        buildLearningItem(context, noteStore, range, targets[ExportBundle.Learning.name] ?: "总览"),
        buildReviewItem(context, range, targets[ExportBundle.Review.name] ?: "全部"),
        buildTodoItem(context, noteStore, range, targets[ExportBundle.Todo.name] ?: "全部"),
        buildFocusItem(context, range, targets[ExportBundle.Focus.name] ?: "全部"),
    )
}

private fun buildScheduleItem(
    context: Context,
    range: ExportRange,
    target: String,
): ExportItem {
    val raw = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
        .getString("schedule_cache_v1", "")
        .orEmpty()
        .ifBlank {
            context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
                .getString("schedule_cache_v1", "")
                .orEmpty()
        }
    if (raw.isBlank()) {
        return ExportItem(ExportBundle.Schedule, "0 条", "当前还没有可导出的课表缓存。", "当前还没有可导出的课表缓存。")
    }
    return runCatching {
        val root = JSONObject(raw)
        val week = root.optJSONObject("weekSchedule") ?: JSONObject()
        val source = when (range) {
            ExportRange.Today -> resolveTodayScheduleCourses(root, week)
            ExportRange.ThisWeek, ExportRange.Recent7Days, ExportRange.All -> jsonObjectList(week.optJSONArray("courses"))
        }

        val targets = mutableListOf("全部")
        val items = mutableListOf<JSONObject>()
        source.forEach { item ->
            val courseName = item.optString("courseName").ifBlank { "课程待补全" }
            if (!targets.contains(courseName)) targets += courseName
            if (target == "全部" || target == courseName) items += item
        }

        val termName = week.optJSONObject("term")?.optString("name").orEmpty()
        val weekTitle = week.optJSONObject("week")?.optString("title").orEmpty()
        val lines = mutableListOf<String>()
        lines += "课表摘要"
        if (termName.isNotBlank() || weekTitle.isNotBlank()) {
        lines += listOf(termName, weekTitle).filter { it.isNotBlank() }.joinToString(" · ")
        }
        lines += when (range) {
            ExportRange.Today -> "导出范围：今天，命中 ${items.size} 条课程"
            ExportRange.ThisWeek -> "导出范围：本周，命中 ${items.size} 条课程"
            ExportRange.Recent7Days -> "导出范围：最近 7 天（当前课表缓存），命中 ${items.size} 条课程"
            ExportRange.All -> "导出范围：全部摘要（当前课表缓存），命中 ${items.size} 条课程"
        }
        if (target != "全部") lines += "导出对象：$target"

        items.take(if (range == ExportRange.Today) 8 else 6).forEach { item ->
            val courseName = item.optString("courseName").ifBlank { "课程待补全" }
            val majorIndex = item.optInt("majorIndex", 0)
            val classroom = item.optString("classroom").ifBlank { "教室待补全" }
            lines += "• $courseName · 第${majorIndex}大节 · $classroom"
        }

        ExportItem(
            bundle = ExportBundle.Schedule,
            previewCount = "${items.size} 条",
            preview = lines.drop(1).take(3).joinToString("\n"),
            fullText = lines.joinToString("\n"),
            targets = targets,
        )
    }.getOrDefault(ExportItem(ExportBundle.Schedule, "0 条", "课表摘要生成失败。", "课表摘要生成失败。"))
}

private fun buildExportFileName(title: String, range: String, target: String): String {
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val safeTitle = sanitizeExportName(title)
    val safeRange = sanitizeExportName(range)
    val safeTarget = sanitizeExportName(target).ifBlank { "全部" }
    return "颇晓-$safeTitle-$safeRange-$safeTarget-$date.txt"
}

private fun buildExportFileContent(
    item: ExportItem,
    template: ExportTemplate,
    range: ExportRange,
    target: String,
): String {
    val header = buildList {
        add(item.bundle.title)
        add("模板：${template.title}")
        add("范围：${range.title}")
        add("对象：${target.ifBlank { "全部" }}")
        add("导出时间：${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
        add("")
    }
    return (header + item.fullText).joinToString("\n")
}

private fun sanitizeExportName(value: String): String {
    return value.trim().replace(Regex("""[\\/:*?"<>|]"""), "-")
}

private fun writeExportText(
    context: Context,
    uri: Uri,
    content: String,
): Boolean {
    return runCatching {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8).use { writer ->
            requireNotNull(writer)
            writer.write(content)
            writer.flush()
        }
        true
    }.getOrDefault(false)
}

private fun buildGradeItem(
    context: Context,
    range: ExportRange,
    target: String,
): ExportItem {
    val raw = context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE)
        .getString("grade_cache_v1", "")
        .orEmpty()
    if (raw.isBlank()) {
        return ExportItem(ExportBundle.Grade, "0 门", "当前还没有同步到可导出的成绩缓存。", "当前还没有同步到可导出的成绩缓存。")
    }
    return runCatching {
        val source = JSONArray(raw)
        val all = mutableListOf<JSONObject>()
        val targets = mutableListOf("全部")
        for (index in 0 until source.length()) {
            val item = source.optJSONObject(index) ?: continue
            all += item
            val courseName = item.optString("title").ifBlank { "课程待补全" }
            if (!targets.contains(courseName)) targets += courseName
        }

        val items = all.filter { target == "全部" || it.optString("title") == target }
        var excellent = 0
        var warning = 0
        val lines = mutableListOf<String>()
        lines += "成绩趋势摘要"
        lines += if (range == ExportRange.All) {
            "导出范围：全部摘要，命中 ${items.size} 门课程"
        } else {
            "导出范围：精选摘要，命中 ${items.size} 门课程"
        }
        if (target != "全部") lines += "导出对象：$target"

        val display = if (range == ExportRange.All) items else items.take(8)
        display.forEach { item ->
            val title = item.optString("title").ifBlank { "课程待补全" }
            val sourceText = item.optString("source").ifBlank { "状态待补全" }
            val description = item.optString("description").ifBlank { "暂无成绩说明" }
            if (description.contains("优秀") || sourceText.contains("优秀") || description.contains("90")) excellent += 1
            if (description.contains("预警") || description.contains("不及格") || description.contains("59")) warning += 1
            lines += "• $title · $sourceText · $description"
        }
        lines += "优秀 $excellent 门，预警 $warning 门"

        ExportItem(
            bundle = ExportBundle.Grade,
            previewCount = "${items.size} 门",
            preview = lines.drop(1).take(3).joinToString("\n"),
            fullText = lines.joinToString("\n"),
            targets = targets,
        )
    }.getOrDefault(ExportItem(ExportBundle.Grade, "0 门", "成绩趋势摘要生成失败。", "成绩趋势摘要生成失败。"))
}

private fun buildLearningItem(
    context: Context,
    noteStore: CourseNoteStore,
    range: ExportRange,
    target: String,
): ExportItem {
    val scheduleRaw = context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE)
        .getString("schedule_cache_v1", "")
        .orEmpty()
        .ifBlank {
            context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE)
                .getString("schedule_cache_v1", "")
                .orEmpty()
        }
    val scheduleJson = runCatching { JSONObject(scheduleRaw) }.getOrNull()
    val weekSchedule = scheduleJson?.optJSONObject("weekSchedule")
    val todayCount = scheduleJson?.let { resolveTodayScheduleCourses(it, weekSchedule ?: JSONObject()).size } ?: 0
    val weekCount = weekSchedule?.optJSONArray("courses")?.length() ?: 0
    val gradeCount = runCatching {
        val raw = context.getSharedPreferences("campus_services_prefs", Context.MODE_PRIVATE)
            .getString("grade_cache_v1", "")
            .orEmpty()
        if (raw.isBlank()) 0 else JSONArray(raw).length()
    }.getOrDefault(0)
    val todoCount = runCatching {
        val raw = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
            .getString("todo_tasks", "")
            .orEmpty()
        if (raw.isBlank()) 0 else {
            val source = JSONArray(raw)
            (0 until source.length()).count { index ->
                source.optJSONObject(index)?.let { item -> todoItemMatchesRange(item, range) } == true
            }
        }
    }.getOrDefault(0)
    val focusMinutes = runCatching {
        val raw = context.getSharedPreferences("focus_records", Context.MODE_PRIVATE)
            .getString("focus_records", "")
            .orEmpty()
        if (raw.isBlank()) return@runCatching 0
        val source = JSONArray(raw)
        var seconds = 0
        for (index in 0 until source.length()) {
            val item = source.optJSONObject(index) ?: continue
            val date = parseFocusRecordDate(item.optString("finishedAt"))
            if (!matchesExportRange(date, range)) continue
            seconds += item.optInt("seconds", 0)
        }
        seconds / 60
    }.getOrDefault(0)
    val notes = noteStore.loadNotes()
    val scopedNotes = notes.filter { note -> matchesExportRange(epochMillisToDate(note.updatedAt), range) }
    val targets = listOf("总览", "课表", "待办", "专注", "成绩", "笔记")

    val lines = mutableListOf<String>()
    lines += "学习数据摘要"
    lines += "导出范围：${range.title}"
    if (target != "总览") lines += "导出对象：$target"
    if (target == "总览" || target == "课表") lines += when (range) {
        ExportRange.Today -> "课表：今日日程 $todayCount 条"
        ExportRange.ThisWeek -> "课表：本周课程 $weekCount 条，今日日程 $todayCount 条"
        ExportRange.Recent7Days -> "课表：当前课表缓存 $weekCount 条，今日日程 $todayCount 条"
        ExportRange.All -> "课表：当前课表缓存 $weekCount 条，今日日程 $todayCount 条"
    }
    if (target == "总览" || target == "待办") lines += "待办：命中 $todoCount 条任务"
    if (target == "总览" || target == "专注") lines += "专注：命中 $focusMinutes 分钟"
    if (target == "总览" || target == "成绩") lines += "成绩：已缓存 $gradeCount 门"
    if (target == "总览" || target == "笔记") lines += "笔记：命中 ${scopedNotes.size} 条，覆盖 ${scopedNotes.map { it.courseName }.distinct().size} 门课程"

    return ExportItem(
        bundle = ExportBundle.Learning,
        previewCount = "${max(lines.size - 1, 0)} 条摘要",
        preview = lines.drop(1).take(4).joinToString("\n"),
        fullText = lines.joinToString("\n"),
        targets = targets,
    )
}

private fun buildReviewItem(
    context: Context,
    range: ExportRange,
    target: String,
): ExportItem {
    val raw = context.getSharedPreferences("review_planner", Context.MODE_PRIVATE)
        .getString("review_items_v1", "")
        .orEmpty()
    if (raw.isBlank()) {
        return ExportItem(ExportBundle.Review, "0 项", "当前还没有可导出的复习计划。", "当前还没有可导出的复习计划。")
    }
    return runCatching {
        val source = JSONArray(raw)
        val all = mutableListOf<JSONObject>()
        val targets = mutableListOf("全部")
        for (index in 0 until source.length()) {
            val item = source.optJSONObject(index) ?: continue
            val course = item.optString("courseName").ifBlank { "课程待补全" }
            if (!targets.contains(course)) targets += course
            all += item
        }
        val ranged = all.filter { item -> reviewItemMatchesRange(item, range) }
        val items = ranged.filter { target == "全部" || it.optString("courseName") == target }
        val errorCount = items.count { it.optBoolean("errorProne") }
        val coreCount = items.count { it.optString("importance") == "Core" }
        val dueCount = items.count { it.optLong("nextReviewAt") <= System.currentTimeMillis() }
        val topSources = items.groupBy { "${it.optString("courseName")} · ${it.optString("sourceTitle")}" }
            .entries
            .sortedByDescending { it.value.size }
            .take(3)
        val lines = mutableListOf<String>()
        lines += "复习计划摘要"
        lines += "导出范围：${range.title} · 命中 ${items.size} 项"
        if (target != "全部") lines += "导出对象：$target"
        lines += "逾期 $dueCount 项 · 错题 $errorCount 项 · 核心 $coreCount 项"
        topSources.forEach { entry ->
            lines += "• ${entry.key} · ${entry.value.size} 项"
        }
        items.take(if (range == ExportRange.Today) 6 else 8).forEach { item ->
            val title = item.optString("noteTitle").ifBlank { "知识点待补全" }
            val course = item.optString("courseName").ifBlank { "课程待补全" }
            val minutes = item.optInt("recommendedMinutes", 25)
            val sourceTitle = item.optString("sourceTitle").ifBlank { "来源待补全" }
            lines += "• $course · $title · ${minutes} 分钟 · $sourceTitle"
        }
        ExportItem(
            bundle = ExportBundle.Review,
            previewCount = "${items.size} 项",
            preview = lines.drop(1).take(4).joinToString("\n"),
            fullText = lines.joinToString("\n"),
            targets = targets,
        )
    }.getOrDefault(ExportItem(ExportBundle.Review, "0 项", "复习计划摘要生成失败。", "复习计划摘要生成失败。"))
}

private fun buildTodoItem(
    context: Context,
    noteStore: CourseNoteStore,
    range: ExportRange,
    target: String,
): ExportItem {
    val raw = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)
        .getString("todo_tasks", "")
        .orEmpty()
    if (raw.isBlank()) {
        return ExportItem(ExportBundle.Todo, "0 项", "当前还没有待办数据。", "当前还没有待办数据。")
    }
    return runCatching {
        val source = JSONArray(raw)
        val all = mutableListOf<JSONObject>()
        val targets = mutableListOf("全部")
        for (index in 0 until source.length()) {
            val item = source.optJSONObject(index) ?: continue
            all += item
            val bucket = item.optString("listName").ifBlank { "收集箱" }
            if (!targets.contains(bucket)) targets += bucket
        }
        val ranged = all.filter { item -> todoItemMatchesRange(item, range) }
        val items = ranged.filter {
            target == "全部" ||
                it.optString("listName").ifBlank { "收集箱" } == target ||
                it.optString("title") == target
        }
        val pending = items.count { !it.optBoolean("done") }
        val done = items.count { it.optBoolean("done") }
        val focusGoals = items.count { it.optInt("focusGoal", 0) > 0 }
        val priorities = items
            .filter { !it.optBoolean("done") && it.optString("priority") == "High" }
            .map { it.optString("title").ifBlank { "待命名任务" } }

        val lines = mutableListOf<String>()
        lines += "待办周报"
        lines += when (range) {
            ExportRange.Today -> "导出范围：今日摘要"
            ExportRange.ThisWeek -> "导出范围：本周"
            ExportRange.Recent7Days -> "导出范围：最近 7 天"
            else -> "导出范围：全部摘要"
        }
        if (target != "全部") lines += "导出对象：$target"
        lines += "总任务 ${items.size} 项，待完成 $pending 项，已完成 $done 项"
        lines += "带专注目标任务 $focusGoals 项，课程笔记 ${noteStore.loadNotes().size} 条"
        if (priorities.isNotEmpty()) {
            lines += "当前优先推进"
            priorities.take(if (range == ExportRange.Today) 3 else 5).forEach { title ->
                lines += "• $title"
            }
        }

        ExportItem(
            bundle = ExportBundle.Todo,
            previewCount = "${items.size} 项",
            preview = lines.take(4).joinToString("\n"),
            fullText = lines.joinToString("\n"),
            targets = targets,
        )
    }.getOrDefault(ExportItem(ExportBundle.Todo, "0 项", "待办周报生成失败。", "待办周报生成失败。"))
}

private fun buildFocusItem(
    context: Context,
    range: ExportRange,
    target: String,
): ExportItem {
    val raw = context.getSharedPreferences("focus_records", Context.MODE_PRIVATE)
        .getString("focus_records", "")
        .orEmpty()
    if (raw.isBlank()) {
        return ExportItem(ExportBundle.Focus, "0 分钟", "当前还没有专注记录。", "当前还没有专注记录。")
    }
    return runCatching {
        val source = JSONArray(raw)
        val targets = mutableListOf("全部")
        val today = java.time.LocalDate.now()
        val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        val records = buildList<Triple<String, Int, java.time.LocalDate?>> {
            for (index in 0 until source.length()) {
                val item = source.optJSONObject(index) ?: continue
                val title = item.optString("taskTitle").ifBlank { "未命名专注" }
                if (!targets.contains(title)) targets += title
                add(
                    Triple(
                        title,
                        item.optInt("seconds", 0),
                        parseFocusRecordDate(item.optString("finishedAt")),
                    ),
                )
            }
        }
        val filtered = records.filter { (_, _, date) ->
            when (range) {
                ExportRange.Today -> date == today
                ExportRange.ThisWeek -> date != null && !date.isBefore(weekStart)
                ExportRange.Recent7Days -> date != null && !date.isBefore(today.minusDays(6))
                ExportRange.All -> true
            }
        }
        val perTask = linkedMapOf<String, Int>()
        filtered.forEach { (title, seconds, _) ->
            perTask[title] = (perTask[title] ?: 0) + seconds
        }
        val selectedEntries = perTask.entries
            .filter { target == "全部" || it.key == target }
            .sortedByDescending { it.value }
        val selectedSeconds = if (target == "全部") {
            filtered.sumOf { it.second }
        } else {
            selectedEntries.sumOf { it.value }
        }
        val selectedSessions = if (target == "全部") {
            filtered.size
        } else {
            filtered.count { it.first == target }
        }
        if (selectedSessions == 0) {
            return@runCatching ExportItem(
                ExportBundle.Focus,
                "0 分钟",
                "当前范围内还没有专注记录。",
                "当前范围内还没有专注记录。",
                targets,
            )
        }

        val lines = mutableListOf<String>()
        lines += "专注周报"
        if (target != "全部") lines += "导出对象：$target"
        lines += when (range) {
            ExportRange.Today -> "导出范围：今日摘要 · 共 ${selectedSeconds / 60} 分钟"
            ExportRange.ThisWeek -> "导出范围：本周 · 共 ${selectedSeconds / 60} 分钟"
            ExportRange.Recent7Days -> "导出范围：最近 7 天 · 共 ${selectedSeconds / 60} 分钟"
            ExportRange.All -> "导出范围：全部摘要 · 累计 ${selectedSeconds / 60} 分钟"
        }
        lines += "累计完成 $selectedSessions 个专注轮次"

        selectedEntries
            .take(if (range == ExportRange.Today) 3 else 5)
            .forEach { entry ->
                lines += "• ${entry.key} · ${entry.value / 60} 分钟"
            }

        ExportItem(
            bundle = ExportBundle.Focus,
            previewCount = "${selectedSeconds / 60} 分钟",
            preview = lines.take(4).joinToString("\n"),
            fullText = lines.joinToString("\n"),
            targets = targets,
        )
    }.getOrDefault(ExportItem(ExportBundle.Focus, "0 分钟", "专注周报生成失败。", "专注周报生成失败。"))
}

private fun parseFocusRecordDate(value: String): java.time.LocalDate? {
    if (value.isBlank()) return null
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.recoverCatching {
        val currentYear = LocalDate.now().year
        LocalDateTime.parse("$currentYear-$value", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).toLocalDate()
    }.getOrNull()
}

private fun resolveTodayScheduleCourses(
    root: JSONObject,
    week: JSONObject,
): List<JSONObject> {
    val today = LocalDate.now().toString()
    val days = week.optJSONArray("days") ?: JSONArray()
    val todayWeekDay = (0 until days.length())
        .mapNotNull { index -> days.optJSONObject(index) }
        .firstOrNull { day ->
            day.optString("fullDate").ifBlank { day.optString("date") } == today
        }
        ?.optInt("weekDay", -1)
        ?: -1
    if (todayWeekDay <= 0) return jsonObjectList(root.optJSONArray("selectedDateCourses"))
    return jsonObjectList(week.optJSONArray("courses")).filter { it.optInt("dayOfWeek") == todayWeekDay }
}

private fun jsonObjectList(array: JSONArray?): List<JSONObject> {
    if (array == null) return emptyList()
    return buildList {
        for (index in 0 until array.length()) {
            array.optJSONObject(index)?.let(::add)
        }
    }
}

private fun epochMillisToDate(value: Long): LocalDate? {
    if (value <= 0L) return null
    return runCatching {
        java.time.Instant.ofEpochMilli(value).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }.getOrNull()
}

private fun matchesExportRange(
    date: LocalDate?,
    range: ExportRange,
): Boolean {
    if (range == ExportRange.All) return true
    if (date == null) return false
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val weekEnd = weekStart.plusDays(6)
    return when (range) {
        ExportRange.Today -> date == today
        ExportRange.ThisWeek -> !date.isBefore(weekStart) && !date.isAfter(weekEnd)
        ExportRange.Recent7Days -> !date.isBefore(today.minusDays(6)) && !date.isAfter(today)
        ExportRange.All -> true
    }
}

private fun reviewItemMatchesRange(item: JSONObject, range: ExportRange): Boolean {
    if (range == ExportRange.All) return true
    val zone = java.time.ZoneId.systemDefault()
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val weekEnd = weekStart.plusDays(6)
    val nextReviewDate = item.optLong("nextReviewAt")
        .takeIf { it > 0L }
        ?.let { java.time.Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
    val lastReviewedDate = item.optLong("lastReviewedAt")
        .takeIf { it > 0L }
        ?.let { java.time.Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
    return when (range) {
        ExportRange.Today -> nextReviewDate == today
        ExportRange.ThisWeek -> nextReviewDate != null && !nextReviewDate.isBefore(weekStart) && !nextReviewDate.isAfter(weekEnd)
        ExportRange.Recent7Days -> {
            val recentStart = today.minusDays(6)
            (lastReviewedDate != null && !lastReviewedDate.isBefore(recentStart) && !lastReviewedDate.isAfter(today)) ||
                (nextReviewDate != null && !nextReviewDate.isBefore(recentStart) && !nextReviewDate.isAfter(today))
        }
        ExportRange.All -> true
    }
}

private fun todoItemMatchesRange(item: JSONObject, range: ExportRange): Boolean {
    if (range == ExportRange.All) return true
    val today = LocalDate.now()
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val weekEnd = weekStart.plusDays(6)
    val dueText = item.optString("dueText")
    val dueDate = parseTodoDueDate(dueText)
    return when (range) {
        ExportRange.Today -> dueDate == today || dueText.contains("今天") || dueText.contains("今晚")
        ExportRange.ThisWeek -> {
            (dueDate != null && !dueDate.isBefore(weekStart) && !dueDate.isAfter(weekEnd)) ||
                dueText.contains("今天") ||
                dueText.contains("今晚") ||
                dueText.contains("明天") ||
                dueText.contains("明晚") ||
                dueText.contains("本周") ||
                dueText.contains("周末")
        }
        ExportRange.Recent7Days -> {
            val recentStart = today.minusDays(6)
            (dueDate != null && !dueDate.isBefore(recentStart) && !dueDate.isAfter(today)) ||
                dueText.contains("今天") ||
                dueText.contains("今晚")
        }
        ExportRange.All -> true
    }
}

private fun parseTodoDueDate(value: String): LocalDate? {
    val trimmed = value.trim()
    if (trimmed.isBlank()) return null
    val today = LocalDate.now()
    return when {
        trimmed.contains("今天") || trimmed.contains("今晚") -> today
        trimmed.contains("明天") || trimmed.contains("明晚") -> today.plusDays(1)
        else -> {
            val fullMatch = Regex("""(\d{4}-\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
            val shortMatch = Regex("""(\d{2}-\d{2})""").find(trimmed)?.groupValues?.getOrNull(1)
            when {
                fullMatch != null -> runCatching { LocalDate.parse(fullMatch, DateTimeFormatter.ofPattern("yyyy-MM-dd")) }.getOrNull()
                shortMatch != null -> runCatching {
                    LocalDate.parse("${today.year}-$shortMatch", DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }.getOrNull()
                else -> null
            }
        }
    }
}

private fun createImageCard(
    context: Context,
    item: ExportItem,
    theme: ExportThemeSpec,
    template: ExportTemplate,
): android.net.Uri? = runCatching {
    val width = when (template) {
        ExportTemplate.Brief -> 1240
        ExportTemplate.Poster -> 1440
        ExportTemplate.Minimal -> 1080
    }
    val height = when (template) {
        ExportTemplate.Brief -> 1754
        ExportTemplate.Poster -> 1920
        ExportTemplate.Minimal -> 1440
    }

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            intArrayOf(theme.start, theme.mid, theme.end),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

    val panelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.panel }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.stroke
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = item.bundle.accent.copy(alpha = 0.92f).toArgb() }
    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.title
        isFakeBoldText = true
    }
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = theme.body }
    val metricPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = item.bundle.accent.toArgb()
        isFakeBoldText = true
    }
    val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = theme.footer
        textSize = 28f
    }

    when (template) {
        ExportTemplate.Brief -> {
            canvas.drawRoundRect(RectF(80f, 92f, width - 80f, height - 92f), 58f, 58f, panelPaint)
            canvas.drawRoundRect(RectF(80f, 92f, width - 80f, height - 92f), 58f, 58f, strokePaint)
            canvas.drawRoundRect(RectF(120f, 132f, 380f, 200f), 34f, 34f, badgePaint)
            titlePaint.textSize = 40f
            bodyPaint.textSize = 44f
            metricPaint.textSize = 72f
            canvas.drawText(theme.badge, 156f, 178f, titlePaint)
            canvas.drawText(item.previewCount, width - 350f, 188f, metricPaint)
            drawBlock(canvas, item.bundle.title, titlePaint, 120, 252, width - 240)
            drawBlock(canvas, item.fullText, bodyPaint, 120, 360, width - 240)
        }

        ExportTemplate.Poster -> {
            canvas.drawRoundRect(RectF(90f, 120f, width - 90f, height - 120f), 72f, 72f, panelPaint)
            canvas.drawRoundRect(RectF(90f, 120f, width - 90f, height - 120f), 72f, 72f, strokePaint)
            canvas.drawRoundRect(RectF(120f, 160f, width - 120f, 740f), 60f, 60f, badgePaint)
            titlePaint.textSize = 44f
            bodyPaint.textSize = 56f
            metricPaint.textSize = 110f
            canvas.drawText(theme.badge, 164f, 228f, titlePaint)
            canvas.drawText(item.previewCount, 164f, 360f, metricPaint)
            drawBlock(canvas, item.bundle.title, bodyPaint, 160, 450, width - 320)
            drawBlock(canvas, item.fullText, TextPaint(bodyPaint).apply { textSize = 42f }, 150, 840, width - 300)
        }

        ExportTemplate.Minimal -> {
            val card = RectF(72f, 72f, width - 72f, height - 72f)
            canvas.drawRoundRect(card, 42f, 42f, panelPaint)
            canvas.drawRoundRect(card, 42f, 42f, strokePaint)
            titlePaint.textSize = 30f
            bodyPaint.textSize = 38f
            metricPaint.textSize = 60f
            footerPaint.textSize = 24f
            canvas.drawText(theme.badge, 112f, 132f, titlePaint)
            canvas.drawText(item.previewCount, width - 320f, 132f, metricPaint)
            drawBlock(canvas, item.bundle.title, TextPaint(bodyPaint).apply { textSize = 68f }, 112, 210, width - 224)
            drawBlock(canvas, item.fullText, bodyPaint, 112, 360, width - 224)
        }
    }

    val footerText = "生成时间 · ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}\n来自 破晓 · 校园学习工作台"
    drawBlock(canvas, footerText, footerPaint, 112, height - 160, width - 224)

    val directory = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(directory, "export_${item.bundle.name.lowercase()}_${template.name.lowercase()}.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}.getOrNull()

private fun drawBlock(
    canvas: Canvas,
    text: String,
    paint: TextPaint,
    left: Int,
    top: Int,
    width: Int,
) {
    val layout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(false)
        .setLineSpacing(0f, 1.18f)
        .build()
    canvas.save()
    canvas.translate(left.toFloat(), top.toFloat())
    layout.draw(canvas)
    canvas.restore()
}

private fun loadExportHistory(prefs: android.content.SharedPreferences): List<ExportHistoryRecord> {
    val raw = prefs.getString("history_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val source = JSONArray(raw)
        val records = mutableListOf<ExportHistoryRecord>()
        for (index in 0 until source.length()) {
            val item = source.optJSONObject(index) ?: continue
            records += ExportHistoryRecord(
                bundleName = item.optString("bundleName"),
                action = item.optString("action"),
                templateName = item.optString("templateName"),
                rangeName = item.optString("rangeName"),
                target = item.optString("target"),
                createdAt = item.optLong("createdAt"),
            )
        }
        records
    }.getOrDefault(emptyList())
}

private fun persistExportHistory(
    prefs: android.content.SharedPreferences,
    records: List<ExportHistoryRecord>,
) {
    val source = JSONArray()
    records.forEach { record ->
        source.put(
            JSONObject().apply {
                put("bundleName", record.bundleName)
                put("action", record.action)
                put("templateName", record.templateName)
                put("rangeName", record.rangeName)
                put("target", record.target)
                put("createdAt", record.createdAt)
            }
        )
    }
    prefs.edit().putString("history_v1", source.toString()).apply()
}

private fun loadThemeSpec(context: Context): ExportThemeSpec {
    val stored = context.getSharedPreferences("ui_prefs", Context.MODE_PRIVATE)
        .getString("theme_preset", PoxiaoThemePreset.Forest.name)
        .orEmpty()
    val preset = runCatching { PoxiaoThemePreset.valueOf(stored) }.getOrDefault(PoxiaoThemePreset.Forest)
    return when (preset) {
        PoxiaoThemePreset.Forest -> ExportThemeSpec("森林青屿", Color(0xFFF7F3EA).toArgb(), Color(0xFFE6F3E6).toArgb(), Color(0xFFD5E9DA).toArgb(), Color.White.copy(alpha = 0.70f).toArgb(), Color.White.copy(alpha = 0.55f).toArgb(), Color(0xFF12352A).toArgb(), Color(0xCC1F4837).toArgb(), Color(0xA63B5F4E).toArgb())
        PoxiaoThemePreset.Aero -> ExportThemeSpec("Frutiger Aero", Color(0xFFF2FCFF).toArgb(), Color(0xFFD9F4FF).toArgb(), Color(0xFFD8F5EA).toArgb(), Color.White.copy(alpha = 0.74f).toArgb(), Color(0x99D8F7FF).toArgb(), Color(0xFF104A5D).toArgb(), Color(0xCC2D6375).toArgb(), Color(0xA6406E83).toArgb())
        PoxiaoThemePreset.Ink -> ExportThemeSpec("墨白书卷", Color(0xFFF7F2E8).toArgb(), Color(0xFFF0E8D8).toArgb(), Color(0xFFE4DAC7).toArgb(), Color(0xE6FFFDF8).toArgb(), Color(0x99D5CCBE).toArgb(), Color(0xFF1F2925).toArgb(), Color(0xCC49554F).toArgb(), Color(0xA66A756F).toArgb())
        PoxiaoThemePreset.Sunset -> ExportThemeSpec("落日果汽", Color(0xFFFFF4EC).toArgb(), Color(0xFFFFE6D9).toArgb(), Color(0xFFFFD0BF).toArgb(), Color(0xEFFFF7F3).toArgb(), Color(0x88FFE2D1).toArgb(), Color(0xFF572E24).toArgb(), Color(0xCC7A4A3A).toArgb(), Color(0xA68F614E).toArgb())
        PoxiaoThemePreset.Night -> ExportThemeSpec("夜航雾岛", Color(0xFF0D1524).toArgb(), Color(0xFF13263A).toArgb(), Color(0xFF1D3957).toArgb(), Color(0xCC16243A).toArgb(), Color(0x668ADFFF).toArgb(), Color(0xFFF2FBFF).toArgb(), Color(0xD1CAE9F2).toArgb(), Color(0x99B8D9E6).toArgb())
    }
}
