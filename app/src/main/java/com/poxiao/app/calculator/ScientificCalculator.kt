
package com.poxiao.app.calculator

import android.content.SharedPreferences
import android.content.ClipData
import android.content.ClipboardManager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.KeyboardHide
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ScatterPlot
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SquareFoot
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.poxiao.app.ui.LiquidGlassCard
import com.poxiao.app.ui.LiquidGlassSurface
import com.poxiao.app.ui.LocalLiquidGlassStylePreset
import com.poxiao.app.ui.LiquidGlassStylePreset
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.BambooGlass
import com.poxiao.app.ui.theme.CloudWhite
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PineInk
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class CalculatorApp(val title: String, val subtitle: String) {
    Compute("常规计算", "科学计算"),
    Statistics("统计推断", "均值方差"),
    Test("假设检验", "t检验等"),
    Distribution("概率分布", "正态二项"),
    Spreadsheet("数据表格", "表格汇总"),
    FunctionTable("函数表格", "生成数值"),
    Equation("方程求解", "一二次等"),
    Inequality("不等式求解", "一元二次"),
    Complex("复数域", "极坐标等"),
    Base("进制转换", "2至16进制"),
    Matrix("矩阵代数", "行列式等"),
    Vector("空间向量", "点积叉积"),
    Ratio("比例缩放", "正反比例"),
}

private enum class AngleMode(val title: String) {
    Deg("Deg"),
    Rad("Rad"),
}

private enum class UtilityPage(val title: String, val subtitle: String) {
    Settings("全局设置", "角度、格式、位数与舍入"),
    Format("结果格式", "格式规则与显示预览"),
    Unit("单位换算", "换算条目、最近使用与置顶"),
    Constants("常数表", "常数分组、最近查看与置顶"),
}

private enum class ResultFormat(val title: String) {
    Standard("标准"),
    Fixed2("定点2位"),
    Scientific("科学"),
    Engineering("工程"),
    DMS("度分秒"),
}

private enum class RoundingRule(val title: String, val mode: RoundingMode) {
    HalfUp("四舍五入", RoundingMode.HALF_UP),
    HalfEven("银行家舍入", RoundingMode.HALF_EVEN),
    Down("截断", RoundingMode.DOWN),
}

private data class CalculatorSettings(
    val angleMode: AngleMode = AngleMode.Rad,
    val resultFormat: ResultFormat = ResultFormat.Standard,
    val displayDigits: Int = 6,
    val roundingRule: RoundingRule = RoundingRule.HalfUp,
)

private sealed class FocusTarget {
    object None : FocusTarget()
    object ComputeExpression : FocusTarget()
    data class MatrixCell(val index: Int, val isMatrixB: Boolean = false) : FocusTarget()
    data class StatisticsCell(val index: Int, val isY: Boolean = false) : FocusTarget()
    object BaseInput : FocusTarget()
    data class GenericInput(val id: String) : FocusTarget() // 用于 Equation, Vector 等通用输入
    data class MatrixGridCell(val row: Int, val col: Int, val isMatrixB: Boolean = false) : FocusTarget()
    data class EquationGridCell(val row: Int, val col: Int, val equationId: String = "eq") : FocusTarget()
    data class ComplexGridCell(val index: Int, val isPolar: Boolean = false) : FocusTarget()
}

private sealed interface CalculatorRoute {
    data class App(val app: CalculatorApp) : CalculatorRoute
    data class Utility(val page: UtilityPage) : CalculatorRoute
}

private data class HistoryRecord(
    val id: Long = System.nanoTime(),
    val expression: String,
    val result: String
)

// 持久化工具函数
private fun saveHistory(prefs: SharedPreferences, history: List<HistoryRecord>) {
    val array = JSONArray()
    history.forEach { record ->
        val obj = JSONObject()
        obj.put("expr", record.expression)
        obj.put("res", record.result)
        obj.put("id", record.id)
        array.put(obj)
    }
    prefs.edit().putString("compute_history", array.toString()).apply()
}

private fun loadHistory(prefs: SharedPreferences): List<HistoryRecord> {
    val result = mutableListOf<HistoryRecord>()
    val jsonStr = prefs.getString("compute_history", null) ?: return result
    runCatching {
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(HistoryRecord(
                id = obj.optLong("id", System.nanoTime() + i),
                expression = obj.getString("expr"),
                result = obj.getString("res")
            ))
        }
    }
    return result
}

// 分数推导函数 (Farey 序列/连分数近似)
private fun doubleToFraction(value: Double, tolerance: Double = 1.0E-6): String? {
    if (value.isNaN() || value.isInfinite()) return null
    val absValue = abs(value)
    if (absValue > 1e7 || absValue < 1e-5) return null
    var h1 = 1L; var h2 = 0L
    var k1 = 0L; var k2 = 1L
    var b = absValue
    do {
        val a = kotlin.math.floor(b).toLong()
        var aux = h1; h1 = a * h1 + h2; h2 = aux
        aux = k1; k1 = a * k1 + k2; k2 = aux
        
        val diff = b - a
        if (abs(diff) < 1e-12) break // 防止除以极小值
        b = 1 / diff
    } while (abs(absValue - h1.toDouble() / k1) > absValue * tolerance && k1 <= 1000L)
    
    if (k1 in 2..1000L) {
        val sign = if (value < 0) "-" else ""
        return "$sign$h1/$k1"
    }
    return null
}

@Composable
fun ScientificCalculatorScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator_prefs", 0) }
    var settings by remember { mutableStateOf(loadCalculatorSettings(prefs)) }
    var currentRoute by remember { mutableStateOf<CalculatorRoute>(CalculatorRoute.App(CalculatorApp.Compute)) }
    var showDirectory by remember { mutableStateOf(false) }
    var focusTarget by remember { mutableStateOf<FocusTarget>(FocusTarget.ComputeExpression) }
    
    val updateSettings: (CalculatorSettings) -> Unit = { next ->
        settings = next
        saveCalculatorSettings(prefs, next)
    }
    val openApp: (CalculatorApp) -> Unit = { app ->
        currentRoute = CalculatorRoute.App(app)
        showDirectory = false
        // 重置焦点，确保键盘输入能导向当前模块的正确目标
        focusTarget = when (app) {
            CalculatorApp.Compute -> FocusTarget.ComputeExpression
            CalculatorApp.Matrix -> FocusTarget.MatrixGridCell(0, 0, false)
            CalculatorApp.Statistics -> FocusTarget.StatisticsCell(0, false)
            CalculatorApp.Base -> FocusTarget.BaseInput
            CalculatorApp.Equation -> FocusTarget.EquationGridCell(0, 0, "eq")
            CalculatorApp.Complex -> FocusTarget.ComplexGridCell(0, false)
            CalculatorApp.Vector -> FocusTarget.MatrixGridCell(0, 0, false)
            CalculatorApp.Inequality -> FocusTarget.GenericInput("ineq_a")
            CalculatorApp.Ratio -> FocusTarget.GenericInput("rat_a")
            else -> FocusTarget.ComputeExpression
        }
    }
    val openUtility: (UtilityPage) -> Unit = { page ->
        currentRoute = CalculatorRoute.Utility(page)
        showDirectory = false
    }
    val backToCalculator: () -> Unit = {
        currentRoute = CalculatorRoute.App(CalculatorApp.Compute)
        focusTarget = FocusTarget.ComputeExpression
        showDirectory = false
    }
    BackHandler(enabled = showDirectory || currentRoute != CalculatorRoute.App(CalculatorApp.Compute)) {
        when {
            showDirectory -> showDirectory = false
            currentRoute != CalculatorRoute.App(CalculatorApp.Compute) -> backToCalculator()
        }
    }
    
    val routeState = currentRoute
    val routeTitle = when {
        showDirectory -> "更多功能"
        routeState is CalculatorRoute.App -> routeState.app.title
        else -> (routeState as CalculatorRoute.Utility).page.title
    }
    val routeSubtitle = when {
        showDirectory -> "选择计算、分析与系统工具"
        routeState is CalculatorRoute.App -> routeState.app.subtitle
        else -> (routeState as CalculatorRoute.Utility).page.subtitle
    }
    // 主题切换状态
    var isDarkMode by remember { mutableStateOf(false) }

    // 当切换为深色模式时，改变系统状态栏颜色
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val context = view.context
            val activity = if (context is android.app.Activity) context 
                          else (context as? android.content.ContextWrapper)?.baseContext as? android.app.Activity
            
            activity?.window?.let { window ->
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
            }
        }
    }

    val routeIcon = when {
        showDirectory -> Icons.Outlined.Widgets
        routeState is CalculatorRoute.App -> tileIcon(routeState.app.title)
        else -> tileIcon((routeState as CalculatorRoute.Utility).page.title)
    }
    
    // 计算模块状态
    var computeExpression by remember { mutableStateOf(runCatching { prefs.getString("compute_expression", "") }.getOrNull() ?: "") }
    var computeResult by remember { mutableStateOf(runCatching { prefs.getString("compute_result", "0") }.getOrNull() ?: "0") }
    var computeCursorIndex by remember { mutableStateOf(computeExpression.length) }
    
    // 强制光标索引在合法范围内
    val safeCursorMove: (Int) -> Unit = { target ->
        computeCursorIndex = target.coerceIn(0, computeExpression.length)
    }
    val computeHistory = remember { 
        mutableStateListOf<HistoryRecord>().apply { 
            addAll(runCatching { loadHistory(prefs) }.getOrElse { emptyList() }) 
        } 
    }
    
    // 一键复制辅助
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
    val haptic = LocalHapticFeedback.current
    val copyToClipboard: (String) -> Unit = { text ->
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Calculator Data", text))
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        android.widget.Toast.makeText(context, "已复制: $text", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    // Undo/Redo 堆栈状态
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }
    
    val commitExpressionChange: (String) -> Unit = { newExpr ->
        if (newExpr != computeExpression) {
            undoStack.add(computeExpression)
            if (undoStack.size > 20) undoStack.removeAt(0) // 限制堆栈深度为 20
            redoStack.clear()
            computeExpression = newExpr
            runCatching { prefs.edit().putString("compute_expression", newExpr).apply() }
        }
    }
    
    LaunchedEffect(computeResult) {
        runCatching { prefs.edit().putString("compute_result", computeResult).apply() }
    }
    
    LaunchedEffect(computeHistory.size) {
        runCatching { saveHistory(prefs, computeHistory) }
    }

    // 专业模块状态 (矩阵、统计、进制等)
    val matrixFields = remember { mutableStateListOf<String>().apply { repeat(18) { add("0") } } }
    var statsRawX by remember { mutableStateOf("85,90,92,76,88,95") }
    var statsRawY by remember { mutableStateOf("78,81,88,70,86,91") }
    var baseValue by remember { mutableStateOf("255") }
    val genericFields = remember { mutableStateMapOf<String, String>() }
    
    // UI 配置变量
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    CompositionLocalProvider(
        LocalLiquidGlassStylePreset provides if (isDarkMode) LiquidGlassStylePreset.Hyper else LiquidGlassStylePreset.IOS
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (currentRoute is CalculatorRoute.App && (currentRoute as CalculatorRoute.App).app != CalculatorApp.Compute) {
                            focusTarget = FocusTarget.None
                        }
                    })
                },
            color = Color.Transparent,
        ) {
            Box(modifier = Modifier.fillMaxSize().background(if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color.Transparent)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 全局统一的 LiquidGlass Header，放置于 Column 顶层
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        CalculatorWorkspaceHeader(
                            title = routeTitle,
                            subtitle = routeSubtitle,
                            icon = routeIcon,
                            actionText = if (showDirectory || currentRoute != CalculatorRoute.App(CalculatorApp.Compute)) "计算器" else "返回",
                            actionColor = if (showDirectory) Color(0xFF5C8FB8) else ForestGreen,
                            onAction = {
                                if (showDirectory || currentRoute != CalculatorRoute.App(CalculatorApp.Compute)) {
                                    backToCalculator()
                                } else {
                                    onBack()
                                }
                            },
                            onOpenDirectory = { showDirectory = true },
                            directoryOpen = showDirectory,
                            isDarkMode = isDarkMode,
                            onToggleTheme = { isDarkMode = !isDarkMode },
                            modifier = Modifier.graphicsLayer { shadowElevation = 8.dp.toPx() } 
                        )
                    }

                    // 下方区域根据状态切换
                    Box(modifier = Modifier.weight(1f)) {
                        if (showDirectory) {
                            CalculatorDirectoryScreen(
                                currentRoute = currentRoute,
                                onOpenApp = openApp,
                                onOpenUtility = openUtility,
                                maxHeight = (configuration.screenHeightDp * 0.78f).dp.coerceIn(460.dp, 760.dp),
                                modifier = Modifier.padding(top = 0.dp)
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    when (routeState) {
                                        is CalculatorRoute.App -> when (routeState.app) {
                                            CalculatorApp.Compute -> ComputeModulePro(
                                                settings = settings,
                                                topPadding = 0.dp,
                                                expression = computeExpression,
                                                onExpressionChange = commitExpressionChange,
                                                result = computeResult,
                                                onResultChange = { computeResult = it },
                                                cursorIndex = computeCursorIndex,
                                                onCursorMove = safeCursorMove,
                                                history = computeHistory,
                                                onCopy = copyToClipboard
                                            )
                                            else -> CalculatorScrollableModule(
                                                routeState = routeState,
                                                settings = settings,
                                                updateSettings = updateSettings,
                                                topPadding = 0.dp,
                                                focusTarget = focusTarget,
                                                onFocusChange = { focusTarget = it },
                                                matrixFields = matrixFields,
                                                statsRawX = statsRawX,
                                                onRawXChange = { statsRawX = it },
                                                statsRawY = statsRawY,
                                                onRawYChange = { statsRawY = it },
                                                baseValue = baseValue,
                                                onBaseValueChange = { baseValue = it },
                                                genericFields = genericFields
                                            )
                                        }
                                        is CalculatorRoute.Utility -> CalculatorScrollableModule(
                                            routeState = routeState,
                                            settings = settings,
                                            updateSettings = updateSettings,
                                            topPadding = 0.dp,
                                            focusTarget = focusTarget,
                                            onFocusChange = { focusTarget = it },
                                            matrixFields = matrixFields,
                                            statsRawX = statsRawX,
                                            onRawXChange = { statsRawX = it },
                                            statsRawY = statsRawY,
                                            onRawYChange = { statsRawY = it },
                                            baseValue = baseValue,
                                            onBaseValueChange = { baseValue = it },
                                            genericFields = genericFields
                                        )
                                    }
                                }
                                
                                // 全局键盘逻辑 (仅在 App 路由下显示，且该模块需要固定键盘)
                                if (routeState is CalculatorRoute.App) {
                                    val showGlobalKeypad = when (routeState.app) {
                                        CalculatorApp.Test, CalculatorApp.Distribution, CalculatorApp.Spreadsheet, 
                                        CalculatorApp.FunctionTable -> false
                                        else -> true
                                    }
                                    if (showGlobalKeypad) {
                                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                            ProKeypad(
                                                onToken = { token ->
                                                    val mappedToken = appendFormulaToken("", token)
                                                    when (val target = focusTarget) {
                                                        is FocusTarget.ComputeExpression -> {
                                                            val next = insertAtCursor(TextFieldValue(computeExpression, TextRange(computeCursorIndex)), mappedToken)
                                                            commitExpressionChange(next.text)
                                                            safeCursorMove(next.selection.start)
                                                        }
                                                        is FocusTarget.MatrixGridCell -> {
                                                            val index = target.row * (if (routeState.app == CalculatorApp.Matrix) 3 else 1) + target.col + (if (target.isMatrixB) 9 else 0)
                                                            matrixFields[index] = if (matrixFields[index] == "0") mappedToken else matrixFields[index] + mappedToken
                                                        }
                                                        is FocusTarget.MatrixCell -> {
                                                            val index = target.index + (if (target.isMatrixB) 9 else 0)
                                                            matrixFields[index] = if (matrixFields[index] == "0") mappedToken else matrixFields[index] + mappedToken
                                                        }
                                                        is FocusTarget.StatisticsCell -> {
                                                            if (target.isY) {
                                                                val list = rawY.split(",").toMutableList()
                                                                while (list.size <= target.index) list.add("")
                                                                list[target.index] = if (list[target.index] == "0") mappedToken else list[target.index] + mappedToken
                                                                onRawYChange(list.joinToString(","))
                                                            } else {
                                                                val list = rawX.split(",").toMutableList()
                                                                while (list.size <= target.index) list.add("")
                                                                list[target.index] = if (list[target.index] == "0") mappedToken else list[target.index] + mappedToken
                                                                onRawXChange(list.joinToString(","))
                                                            }
                                                        }
                                                        is FocusTarget.StatisticsCell -> {
                                                            var nextIndex = target.index + delta
                                                            if (nextIndex >= 5) nextIndex = 0
                                                            else if (nextIndex < 0) nextIndex = 4
                                                            focusTarget = FocusTarget.StatisticsCell(nextIndex, target.isY)
                                                        }
                                                        is FocusTarget.BaseInput -> {
                                                            baseValue += mappedToken
                                                        }
                                                        is FocusTarget.GenericInput -> {
                                                            val current = genericFields[target.id] ?: ""
                                                            genericFields[target.id] = current + mappedToken
                                                        }
                                                        is FocusTarget.EquationGridCell -> {
                                                            val key = "${target.equationId}_r${target.row}c${target.col}"
                                                            val current = genericFields[key] ?: ""
                                                            genericFields[key] = if (current == "0") mappedToken else current + mappedToken
                                                        }
                                                        is FocusTarget.ComplexGridCell -> {
                                                            val key = if (target.isPolar) "c_p${target.index}" else "c_a${target.index}"
                                                            val current = genericFields[key] ?: ""
                                                            genericFields[key] = if (current == "0") mappedToken else current + mappedToken
                                                        }
                                                        FocusTarget.None -> {}
                                                    }
                                                },
                                                onDelete = {
                                                    when (val target = focusTarget) {
                                                        is FocusTarget.ComputeExpression -> {
                                                            if (computeCursorIndex > 0) {
                                                                val nextText = computeExpression.removeRange(computeCursorIndex - 1, computeCursorIndex)
                                                                commitExpressionChange(nextText)
                                                                safeCursorMove(computeCursorIndex - 1)
                                                            }
                                                        }
                                                        is FocusTarget.MatrixGridCell -> {
                                                            val index = target.row * (if (routeState.app == CalculatorApp.Matrix) 3 else 1) + target.col + (if (target.isMatrixB) 9 else 0)
                                                            if (matrixFields[index].isNotEmpty()) matrixFields[index] = matrixFields[index].dropLast(1).ifEmpty { "0" }
                                                        }
                                                        is FocusTarget.MatrixCell -> {
                                                            val index = target.index + (if (target.isMatrixB) 9 else 0)
                                                            if (matrixFields[index].isNotEmpty()) matrixFields[index] = matrixFields[index].dropLast(1).ifEmpty { "0" }
                                                        }
                                                        is FocusTarget.StatisticsCell -> {
                                                            if (target.isY) {
                                                                val list = rawY.split(",").toMutableList()
                                                                if (list.size > target.index) {
                                                                    list[target.index] = list[target.index].dropLast(1).ifEmpty { "0" }
                                                                    onRawYChange(list.joinToString(","))
                                                                }
                                                            } else {
                                                                val list = rawX.split(",").toMutableList()
                                                                if (list.size > target.index) {
                                                                    list[target.index] = list[target.index].dropLast(1).ifEmpty { "0" }
                                                                    onRawXChange(list.joinToString(","))
                                                                }
                                                            }
                                                        }
                                                        is FocusTarget.BaseInput -> {
                                                            if (baseValue.isNotEmpty()) baseValue = baseValue.dropLast(1)
                                                        }
                                                        is FocusTarget.GenericInput -> {
                                                            val current = genericFields[target.id] ?: ""
                                                            if (current.isNotEmpty()) genericFields[target.id] = current.dropLast(1)
                                                        }
                                                        is FocusTarget.EquationGridCell -> {
                                                            val key = "${target.equationId}_r${target.row}c${target.col}"
                                                            val current = genericFields[key] ?: ""
                                                            if (current.isNotEmpty()) genericFields[key] = current.dropLast(1).ifEmpty { "0" }
                                                        }
                                                        is FocusTarget.ComplexGridCell -> {
                                                            val key = if (target.isPolar) "c_p${target.index}" else "c_a${target.index}"
                                                            val current = genericFields[key] ?: ""
                                                            if (current.isNotEmpty()) genericFields[key] = current.dropLast(1).ifEmpty { "0" }
                                                        }
                                                        FocusTarget.None -> {}
                                                    }
                                                },
                                                onClear = {
                                                    when (val target = focusTarget) {
                                                        is FocusTarget.ComputeExpression -> {
                                                            commitExpressionChange("")
                                                            computeResult = "0"
                                                            safeCursorMove(0)
                                                        }
                                                        is FocusTarget.MatrixGridCell -> {
                                                            val index = target.row * (if (routeState.app == CalculatorApp.Matrix) 3 else 1) + target.col + (if (target.isMatrixB) 9 else 0)
                                                            matrixFields[index] = "0"
                                                        }
                                                        is FocusTarget.MatrixCell -> {
                                                            val index = target.index + (if (target.isMatrixB) 9 else 0)
                                                            matrixFields[index] = "0"
                                                        }
                                                        is FocusTarget.StatisticsCell -> {
                                                            if (target.isY) {
                                                                val list = rawY.split(",").toMutableList()
                                                                if (list.size > target.index) {
                                                                    list[target.index] = "0"
                                                                    onRawYChange(list.joinToString(","))
                                                                }
                                                            } else {
                                                                val list = rawX.split(",").toMutableList()
                                                                if (list.size > target.index) {
                                                                    list[target.index] = "0"
                                                                    onRawXChange(list.joinToString(","))
                                                                }
                                                            }
                                                        }
                                                        is FocusTarget.BaseInput -> {
                                                            baseValue = ""
                                                        }
                                                        is FocusTarget.GenericInput -> {
                                                            genericFields[target.id] = ""
                                                        }
                                                        is FocusTarget.EquationGridCell -> {
                                                            val key = "${target.equationId}_r${target.row}c${target.col}"
                                                            genericFields[key] = "0"
                                                        }
                                                        is FocusTarget.ComplexGridCell -> {
                                                            val key = if (target.isPolar) "c_p${target.index}" else "c_a${target.index}"
                                                            genericFields[key] = "0"
                                                        }
                                                        FocusTarget.None -> {}
                                                    }
                                                },
                                                onEqual = {
                                                    if (focusTarget == FocusTarget.ComputeExpression && computeExpression.isNotBlank()) {
                                                        // 智能括号补全
                                                        val openBrackets = computeExpression.count { it == '(' }
                                                        val closeBrackets = computeExpression.count { it == ')' }
                                                        val safeExpression = if (openBrackets > closeBrackets) {
                                                            computeExpression + ")".repeat(openBrackets - closeBrackets)
                                                        } else {
                                                            computeExpression
                                                        }
                                                        
                                                        val finalRes = runCatching {
                                                            val eval = ExpressionEngine.evaluateQuantity(safeExpression, angleMode = settings.angleMode)
                                                            eval.toString(settings)
                                                        }.getOrElse { e -> 
                                                            val msg = e.message ?: "未知错误"
                                                            if (msg.startsWith("语法错误") || msg.startsWith("计算错误") || msg.startsWith("单位不兼容")) msg else "计算错误: $msg"
                                                        }
                                                        computeHistory.add(HistoryRecord(expression = safeExpression, result = finalRes))
                                                        if (!finalRes.startsWith("语法错误") && !finalRes.startsWith("计算错误")) {
                                                            commitExpressionChange(safeExpression)
                                                            computeResult = finalRes
                                                            safeCursorMove(safeExpression.length)
                                                        } else {
                                                            computeResult = finalRes
                                                        }
                                                    }
                                                },
                                                onMoveCursor = { delta ->
                                                    when (val target = focusTarget) {
                                                        is FocusTarget.ComputeExpression -> safeCursorMove(computeCursorIndex + delta)
                                                        is FocusTarget.MatrixGridCell -> {
                                                            val totalCols = if (routeState.app == CalculatorApp.Matrix) 3 else 1
                                                            val totalRows = 3 
                                                            
                                                            var nextRow = target.row
                                                            var nextCol = target.col + delta
                                                            
                                                            if (nextCol >= totalCols) {
                                                                nextCol = 0
                                                                nextRow = (nextRow + 1) % totalRows
                                                            } else if (nextCol < 0) {
                                                                nextCol = totalCols - 1
                                                                nextRow = (nextRow - 1 + totalRows) % totalRows
                                                            }
                                                            focusTarget = FocusTarget.MatrixGridCell(nextRow, nextCol, target.isMatrixB)
                                                        }
                                                        is FocusTarget.EquationGridCell -> {
                                                            // 根据当前模式决定列数
                                                            // 模式存储在 genericFields 或局部状态，这里由于是 lambda，我们可以从 routeState 判断或简化逻辑
                                                            // 实际上 EquationGridCell 的导航逻辑取决于 UI 渲染时的 mode
                                                            // 这里先实现一个通用的循环导航
                                                            var nextCol = target.col + delta
                                                            var nextRow = target.row
                                                            
                                                            // 简单的 5 列兼容逻辑 (多项式最大 5 列，二元一次 3 列)
                                                            val maxCols = 5 
                                                            if (nextCol >= maxCols) { nextCol = 0; nextRow = (nextRow + 1) % 2 }
                                                            else if (nextCol < 0) { nextCol = maxCols - 1; nextRow = (nextRow - 1 + 2) % 2 }
                                                            
                                                            focusTarget = FocusTarget.EquationGridCell(nextRow, nextCol, target.equationId)
                                                        }
                                                        is FocusTarget.ComplexGridCell -> {
                                                            var nextIndex = target.index + delta
                                                            if (nextIndex >= 4) nextIndex = 0
                                                            else if (nextIndex < 0) nextIndex = 3
                                                            focusTarget = FocusTarget.ComplexGridCell(nextIndex, target.isPolar)
                                                        }
                                                        else -> {}
                                                    }
                                                },
                                                onUndo = {
                                                    if (undoStack.isNotEmpty()) {
                                                        redoStack.add(computeExpression)
                                                        computeExpression = undoStack.removeLast()
                                                        safeCursorMove(computeExpression.length)
                                                    }
                                                },
                                                onRedo = {
                                                    if (redoStack.isNotEmpty()) {
                                                        undoStack.add(computeExpression)
                                                        computeExpression = redoStack.removeLast()
                                                        safeCursorMove(computeExpression.length)
                                                    }
                                                },
                                                canUndo = undoStack.isNotEmpty(),
                                                canRedo = redoStack.isNotEmpty(),
                                                settings = settings,
                                                onToggleAngleMode = {
                                                    val nextMode = if (settings.angleMode == AngleMode.Deg) AngleMode.Rad else AngleMode.Deg
                                                    updateSettings(settings.copy(angleMode = nextMode))
                                                }
                                            )
                        }
                    }
                }
            }
        }
    }
}
            }
        }
    }
}

@Composable
private fun CalculatorScrollableModule(
    routeState: CalculatorRoute,
    settings: CalculatorSettings,
    updateSettings: (CalculatorSettings) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit,
    matrixFields: SnapshotStateList<String>,
    statsRawX: String,
    onRawXChange: (String) -> Unit,
    statsRawY: String,
    onRawYChange: (String) -> Unit,
    baseValue: String,
    onBaseValueChange: (String) -> Unit,
    genericFields: MutableMap<String, String>
) {
    // 核心改进：根据路由决定是否需要固定键盘布局
    val needsFixedKeypad = when (routeState) {
        is CalculatorRoute.App -> when (routeState.app) {
            CalculatorApp.Compute -> false // Handled separately in ScientificCalculatorScreen
            CalculatorApp.Test, CalculatorApp.Distribution, CalculatorApp.Spreadsheet, 
            CalculatorApp.FunctionTable -> false // These might still be better as scrollable with inline inputs or specialized UI
            else -> true // Matrix, Statistics, Base, Equation, Inequality, Complex, Vector, Ratio
        }
        else -> false
    }

    if (needsFixedKeypad) {
        FixedKeypadModuleContainer(
            routeState = routeState,
            settings = settings,
            updateSettings = updateSettings,
            topPadding = 0.dp,
            focusTarget = focusTarget,
            onFocusChange = onFocusChange,
            matrixFields = matrixFields,
            statsRawX = statsRawX,
            onRawXChange = onRawXChange,
            statsRawY = statsRawY,
            onRawYChange = onRawYChange,
            baseValue = baseValue,
            onBaseValueChange = onBaseValueChange,
            genericFields = genericFields
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 0.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                when (routeState) {
                    is CalculatorRoute.App -> when (routeState.app) {
                        CalculatorApp.Compute -> {} // Handled separately
                        CalculatorApp.Statistics -> {} // Handled in FixedKeypadModuleContainer
                        CalculatorApp.Test -> HypothesisTestModule(settings = settings)
                        CalculatorApp.Distribution -> DistributionModule(settings = settings)
                        CalculatorApp.Spreadsheet -> SpreadsheetModule()
                        CalculatorApp.FunctionTable -> FunctionTableModule(settings = settings)
                        CalculatorApp.Equation -> {} 
                        CalculatorApp.Inequality -> {}
                        CalculatorApp.Complex -> {}
                        CalculatorApp.Base -> {} // Handled in FixedKeypadModuleContainer
                        CalculatorApp.Matrix -> {} // Handled in FixedKeypadModuleContainer
                        CalculatorApp.Vector -> {}
                        CalculatorApp.Ratio -> {}
                    }
                    is CalculatorRoute.Utility -> when (routeState.page) {
                        UtilityPage.Settings -> SettingsModule(settings = settings, onChange = updateSettings)
                        UtilityPage.Format -> FormatModule(settings = settings, onChange = updateSettings)
                        UtilityPage.Unit -> UnitConversionModule()
                        UtilityPage.Constants -> ConstantsModule()
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedKeypadModuleContainer(
    routeState: CalculatorRoute,
    settings: CalculatorSettings,
    updateSettings: (CalculatorSettings) -> Unit,
    topPadding: androidx.compose.ui.unit.Dp,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit,
    matrixFields: SnapshotStateList<String>,
    statsRawX: String,
    onRawXChange: (String) -> Unit,
    statsRawY: String,
    onRawYChange: (String) -> Unit,
    baseValue: String,
    onBaseValueChange: (String) -> Unit,
    genericFields: MutableMap<String, String>
) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 0.dp, start = 14.dp, end = 14.dp, bottom = 14.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = routeState,
                transitionSpec = {
                    if (targetState is CalculatorRoute.App && initialState is CalculatorRoute.App) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        fadeIn().togetherWith(fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "ModuleTransition"
            ) { targetRoute ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        when (targetRoute) {
                            is CalculatorRoute.App -> when (targetRoute.app) {
                                CalculatorApp.Matrix -> MatrixModulePro(
                                    settings = settings,
                                    fields = matrixFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Statistics -> StatisticsModulePro(
                                    settings = settings,
                                    rawX = statsRawX,
                                    rawY = statsRawY,
                                    onRawXChange = onRawXChange,
                                    onRawYChange = onRawYChange,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Base -> BaseModulePro(
                                    value = baseValue,
                                    onValueChange = onBaseValueChange,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Equation -> EquationModulePro(
                                    fields = genericFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Vector -> VectorModulePro(
                                    settings = settings,
                                    fields = genericFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Complex -> ComplexModulePro(
                                    fields = genericFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Inequality -> InequalityModulePro(
                                    fields = genericFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                CalculatorApp.Ratio -> RatioModulePro(
                                    fields = genericFields,
                                    focusTarget = focusTarget,
                                    onFocusChange = onFocusChange
                                )
                                else -> {}
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

private fun orderedApps(): List<CalculatorApp> {
    return listOf(
        CalculatorApp.Compute,
        CalculatorApp.Statistics,
        CalculatorApp.Distribution,
        CalculatorApp.Matrix,
        CalculatorApp.Vector,
        CalculatorApp.Equation,
        CalculatorApp.Test,
        CalculatorApp.FunctionTable,
        CalculatorApp.Spreadsheet,
        CalculatorApp.Inequality,
        CalculatorApp.Complex,
        CalculatorApp.Base,
        CalculatorApp.Ratio,
    )
}

private data class CalculatorDirectoryItem(
    val title: String,
    val subtitle: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun CalculatorWorkspaceHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String,
    actionColor: Color,
    onAction: () -> Unit,
    onOpenDirectory: () -> Unit,
    directoryOpen: Boolean,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = tilePalette(title)
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tint = palette.primary.copy(alpha = 0.22f),
        borderColor = Color.White.copy(alpha = 0.4f),
        blurRadius = 36.dp // 增加模糊半径，确保在滚动时下层文本被强力模糊
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp), // 压缩内边距，减少垂直高度
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CalculatorInnerShape,
                    color = palette.primary.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.22f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.width(20.dp).height(20.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) CloudWhite else PineInk,
                    )
                    // 动态获取准确的副标题描述，防止文字显示不全或解释不全面
                    val detailedSubtitle = when (title) {
                        "常规计算" -> "科学公式、自然书写与表达式推演"
                        "矩阵计算" -> "最高 3x3 矩阵运算、求逆、特征值分析"
                        "统计分析" -> "一维/二维统计量、线性回归建模"
                        "基数转换" -> "HEX/DEC/OCT/BIN 多进制实时同步"
                        "方程求解" -> "一元二次/三次方程、线性方程组"
                        "复数计算" -> "直角坐标与极坐标复数四则运算"
                        "向量计算" -> "二维/三维向量点乘、叉乘、夹角"
                        "假设检验" -> "Z 检验、T 检验、卡方检验"
                        "概率分布" -> "正态分布、二项分布、泊松分布"
                        "函数表格" -> "基于解析式的动态数值表格"
                        "电子表格" -> "迷你单元格数据处理与汇总"
                        "不等式" -> "一元二次、高次不等式求解"
                        "比例计算" -> "正比例、反比例与系数缩放"
                        else -> subtitle
                    }
                    Text(
                        text = detailedSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.74f) else ForestDeep.copy(alpha = 0.74f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.clickable(onClick = onToggleTheme),
                    shape = CalculatorInnerShape,
                    color = Color.White.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "切换主题",
                            tint = if (isDarkMode) CloudWhite else PineInk,
                            modifier = Modifier.width(20.dp).height(20.dp),
                        )
                    }
                }
                Surface(
                    modifier = Modifier.clickable(onClick = onOpenDirectory),
                    shape = CalculatorInnerShape,
                    color = if (directoryOpen) palette.primary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "更多",
                            tint = if (directoryOpen) palette.primary else PineInk,
                            modifier = Modifier.width(20.dp).height(20.dp),
                        )
                    }
                }
                Button(
                    onClick = onAction,
                    shape = CalculatorInnerShape,
                    colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp) // 优化按钮排版
                ) {
                    Text(actionText, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun CalculatorDirectoryScreen(
    currentRoute: CalculatorRoute,
    onOpenApp: (CalculatorApp) -> Unit,
    onOpenUtility: (UtilityPage) -> Unit,
    maxHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val appItems = orderedApps().map { app ->
        CalculatorDirectoryItem(
            title = app.title,
            subtitle = app.subtitle,
            selected = currentRoute == CalculatorRoute.App(app),
            onClick = { onOpenApp(app) },
        )
    }
    val utilityItems = UtilityPage.entries.map { page ->
        CalculatorDirectoryItem(
            title = page.title,
            subtitle = page.subtitle,
            selected = currentRoute == CalculatorRoute.Utility(page),
            onClick = { onOpenUtility(page) },
        )
    }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            CalculatorDirectorySection(
                title = "计算工具",
                items = appItems,
            )
        }
        item {
            CalculatorDirectorySection(
                title = "系统工具",
                items = utilityItems,
            )
        }
    }
}

@Composable
private fun CalculatorDirectorySection(
    title: String,
    items: List<CalculatorDirectoryItem>,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        tint = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.28f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.4f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = if (isDarkMode) CloudWhite else PineInk)
                Surface(
                    shape = CircleShape, 
                    color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "${items.size} 项",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.72f) else ForestDeep.copy(alpha = 0.72f),
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.18f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, item ->
                        CalculatorDirectoryRow(item = item)
                        if (index != items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(if (isDarkMode) Color.White.copy(alpha = 0.05f) else BambooStroke.copy(alpha = 0.16f)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorDirectoryRow(
    item: CalculatorDirectoryItem,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val palette = tilePalette(item.title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiquidGlassSurface(
            modifier = Modifier
                .width(42.dp)
                .height(42.dp),
            cornerRadius = 18.dp,
            tint = palette.primary.copy(alpha = 0.14f),
            borderColor = Color.White.copy(alpha = 0.16f),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = tileIcon(item.title),
                    contentDescription = null,
                    tint = palette.primary,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = if (isDarkMode) CloudWhite.copy(alpha = 0.5f) else ForestDeep.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.selected) {
                LiquidGlassSurface(
                    modifier = Modifier,
                    cornerRadius = 18.dp,
                    tint = palette.primary.copy(alpha = 0.14f),
                    borderColor = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "当前",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.primary,
                    )

                }
            }
            Text("›", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.54f) else ForestDeep.copy(alpha = 0.54f))
        }
    }
}

private data class TilePalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

private sealed class MathNode {
    data class Text(val content: String, val startIndex: Int) : MathNode()
    data class Fraction(val numerator: MathNode, val denominator: MathNode, val startIndex: Int, val length: Int) : MathNode()
    data class Radical(val content: MathNode, val startIndex: Int, val length: Int) : MathNode()
    data class Power(val base: MathNode, val exponent: MathNode, val startIndex: Int, val length: Int) : MathNode()
    data class Sequence(val nodes: List<MathNode>) : MathNode()
}

@Composable
private fun NaturalMathRenderer(
    expression: String,
    cursorIndex: Int,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val node = remember(expression) { parseToMathNode(expression) }
    
    // 模拟闪烁光标的全局动画
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor-blink"
    )
    
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            RenderMathNode(node, cursorIndex, fontSize, color, isDarkMode, alpha)
        }
    }
}

@Composable
private fun RenderMathNode(
    node: MathNode,
    cursorIndex: Int,
    fontSize: androidx.compose.ui.unit.TextUnit,
    color: Color,
    isDarkMode: Boolean,
    cursorAlpha: Float
) {
    when (node) {
        is MathNode.Text -> {
            val text = node.content
            val start = node.startIndex
            
            for (i in text.indices) {
                val charIndex = start + i
                // 如果光标在这个字符之前
                if (cursorIndex == charIndex) {
                    CursorBar(fontSize, cursorAlpha, isDarkMode)
                }
                
                Text(
                    text = text[i].toString().replace("*", "×").replace("/", "÷"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = fontSize,
                        color = getMathCharColor(text[i], color, isDarkMode),
                        fontWeight = if (text[i].isDigit() || text[i] == '.') FontWeight.Medium else FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 0.5.dp)
                )
            }
            // 如果光标在文本末尾
            if (cursorIndex == start + text.length) {
                CursorBar(fontSize, cursorAlpha, isDarkMode)
            }
        }
        
        is MathNode.Fraction -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                RenderMathNode(node.numerator, cursorIndex, fontSize * 0.75f, color, isDarkMode, cursorAlpha)
                Box(
                    modifier = Modifier
                        .height(1.5.dp)
                        .width(IntrinsicSize.Max)
                        .background(color.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp)
                )
                RenderMathNode(node.denominator, cursorIndex, fontSize * 0.75f, color, isDarkMode, cursorAlpha)
            }
        }
        
        is MathNode.Radical -> {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 2.dp)) {
                Box(modifier = Modifier.height(IntrinsicSize.Max), contentAlignment = Alignment.Center) {
                    Text(
                        "√", 
                        fontSize = fontSize * 1.2f, 
                        color = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                        fontWeight = FontWeight.Light
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .drawBehind {
                            val strokeWidth = 1.5.dp.toPx()
                            drawLine(
                                color = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    RenderMathNode(node.content, cursorIndex, fontSize * 0.9f, color, isDarkMode, cursorAlpha)
                }
            }
        }
        
        is MathNode.Power -> {
            Row(verticalAlignment = Alignment.Bottom) {
                RenderMathNode(node.base, cursorIndex, fontSize, color, isDarkMode, cursorAlpha)
                Box(modifier = Modifier.offset(y = -(fontSize.value * 0.4f).dp)) {
                    RenderMathNode(node.exponent, cursorIndex, fontSize * 0.65f, color, isDarkMode, cursorAlpha)
                }
            }
        }
        
        is MathNode.Sequence -> {
            node.nodes.forEach { child ->
                RenderMathNode(child, cursorIndex, fontSize, color, isDarkMode, cursorAlpha)
            }
        }
    }
}

@Composable
private fun CursorBar(fontSize: androidx.compose.ui.unit.TextUnit, alpha: Float, isDarkMode: Boolean) {
    Box(
        modifier = Modifier
            .width(2.5.dp)
            .height(with(LocalDensity.current) { fontSize.toDp() * 1.2f })
            .background((if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = alpha), RoundedCornerShape(1.dp))
    )
}

private fun getMathCharColor(char: Char, defaultColor: Color, isDarkMode: Boolean): Color {
    return when {
        char.isDigit() || char == '.' -> defaultColor
        char in listOf('+', '-', '*', '/', '×', '÷', '=', '%', '!', '√') -> if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
        char.isLetter() -> if (isDarkMode) Color(0xFFFFB266) else Color(0xFFD35400)
        else -> defaultColor.copy(alpha = 0.5f)
    }
}

private fun parseToMathNode(expression: String): MathNode {
    return runCatching {
        MathNodeParser(expression).parse()
    }.getOrElse { 
        MathNode.Text(expression, 0) // 容错：解析失败回退到普通文本显示
    }
}

private class MathNodeParser(private val input: String, private val depth: Int = 0) {
    private var pos = 0
    private val maxDepth = 15 // 防止深度嵌套导致堆栈溢出

    fun parse(): MathNode {
        if (depth > maxDepth) return MathNode.Text(input, 0)
        val nodes = mutableListOf<MathNode>()
        runCatching {
            while (pos < input.length) {
                val node = parseNext()
                if (node != null) {
                    nodes.add(node)
                } else {
                    break
                }
            }
        }.onFailure {
            return MathNode.Text(input, 0)
        }
        return if (nodes.size == 1) nodes[0] else MathNode.Sequence(nodes)
    }

    private fun parseNext(): MathNode? {
        if (pos >= input.length) return null
        val start = pos

        // 尝试匹配 sqrt(...)
        if (input.startsWith("sqrt(", pos)) {
            val checkpoint = pos
            pos += 5
            val content = parseGroup('(', ')')
            if (content != null) {
                return MathNode.Radical(content, start, pos - start)
            }
            pos = checkpoint // 匹配失败，回退
        }

        // 尝试匹配 (...) / (...)
        if (input[pos] == '(') {
            val checkpoint = pos
            val numerator = parseGroup('(', ')')
            if (numerator != null && pos < input.length && input[pos] == '/') {
                val slashPos = pos
                pos++
                if (pos < input.length && input[pos] == '(') {
                    pos++
                    val denominator = parseGroup('(', ')')
                    if (denominator != null) {
                        return MathNode.Fraction(numerator, denominator, checkpoint, pos - checkpoint)
                    }
                }
                pos = slashPos + 1 // 回退到斜杠后
            }
            // 如果不是分数，回退并作为普通文本处理
            pos = checkpoint
        }

        // 处理普通文本
        val textStart = pos
        while (pos < input.length) {
            if (input.startsWith("sqrt(", pos)) break
            if (input[pos] == '(') {
                // 探测是否是分数的开头
                val lookahead = input.substring(pos)
                if (lookahead.contains(")/(")) break 
            }
            pos++
        }
        
        return if (pos > textStart) {
            MathNode.Text(input.substring(textStart, pos), textStart)
        } else {
            if (pos < input.length) {
                val char = input[pos].toString()
                val p = pos
                pos++
                MathNode.Text(char, p)
            } else null
        }
    }

    private fun parseGroup(open: Char, close: Char): MathNode? {
        val start = pos
        var bracketDepth = 1
        while (pos < input.length && bracketDepth > 0) {
            if (input[pos] == open) bracketDepth++
            else if (input[pos] == close) bracketDepth--
            pos++
        }
        if (bracketDepth > 0 || pos <= start) return null // 括号未闭合或非法偏移
        
        val content = input.substring(start, pos - 1)
        if (content.isEmpty()) return MathNode.Text("", start)
        
        return MathNodeParser(content, depth + 1).parse().let {
            offsetNodeIndices(it, start)
        }
    }

    private fun offsetNodeIndices(node: MathNode, offset: Int): MathNode {
        return when (node) {
            is MathNode.Text -> node.copy(startIndex = node.startIndex + offset)
            is MathNode.Fraction -> node.copy(
                numerator = offsetNodeIndices(node.numerator, offset),
                denominator = offsetNodeIndices(node.denominator, offset),
                startIndex = node.startIndex + offset
            )
            is MathNode.Radical -> node.copy(
                content = offsetNodeIndices(node.content, offset),
                startIndex = node.startIndex + offset
            )
            is MathNode.Power -> node.copy(
                base = offsetNodeIndices(node.base, offset),
                exponent = offsetNodeIndices(node.exponent, offset),
                startIndex = node.startIndex + offset
            )
            is MathNode.Sequence -> node.copy(nodes = node.nodes.map { offsetNodeIndices(it, offset) })
        }
    }
}

private fun tilePalette(title: String): TilePalette {
    return when (title) {
        "常规计算" -> TilePalette(Color(0xFF1E5C4C), Color(0xFF3A8A71), Color(0xFFBCE6D7))
        "统计推断" -> TilePalette(Color(0xFF315E84), Color(0xFF5C8FB8), Color(0xFFC7DCF2))
        "假设检验" -> TilePalette(Color(0xFF60527A), Color(0xFF8470A5), Color(0xFFE0D8F1))
        "概率分布" -> TilePalette(Color(0xFF7C5A37), Color(0xFFAF8356), Color(0xFFF0DFC3))
        "数据表格" -> TilePalette(Color(0xFF2D6B67), Color(0xFF4AA09A), Color(0xFFC5ECE7))
        "函数表格" -> TilePalette(Color(0xFF644D8D), Color(0xFF8A6DC1), Color(0xFFE1D7F7))
        "方程求解" -> TilePalette(Color(0xFF8A4E49), Color(0xFFBF746B), Color(0xFFF2D4CF))
        "不等式求解" -> TilePalette(Color(0xFF4A5A33), Color(0xFF738950), Color(0xFFDDE9C9))
        "复数域" -> TilePalette(Color(0xFF425D86), Color(0xFF6A8FBE), Color(0xFFD4E1F4))
        "进制转换" -> TilePalette(Color(0xFF5C4C46), Color(0xFF8B7268), Color(0xFFEBD7CF))
        "矩阵代数" -> TilePalette(Color(0xFF0F4F53), Color(0xFF2D7A80), Color(0xFFB8E4E5))
        "空间向量" -> TilePalette(Color(0xFF275A65), Color(0xFF458593), Color(0xFFC7E7ED))
        "比例缩放" -> TilePalette(Color(0xFF6C5A31), Color(0xFFA38A48), Color(0xFFF0E3BA))
        "全局设置", "设置" -> TilePalette(Color(0xFF47566E), Color(0xFF6B7D9E), Color(0xFFD6E0F0))
        "结果格式" -> TilePalette(Color(0xFF4B5D76), Color(0xFF728FB3), Color(0xFFD8E4F6))
        "单位换算" -> TilePalette(Color(0xFF3F675E), Color(0xFF5C998B), Color(0xFFCCECE4))
        "常数表" -> TilePalette(Color(0xFF65533C), Color(0xFF9A7D5B), Color(0xFFF2E0C8))
        else -> TilePalette(Color(0xFF4A6158), Color(0xFF78988E), Color(0xFFD9E8E2))
    }
}

private fun tileSection(title: String): String {
    return when (title) {
        "统计推断" -> "分析"
        "假设检验" -> "推断"
        "概率分布" -> "概率"
        "数据表格" -> "表格"
        "函数表格" -> "函数"
        "方程求解" -> "代数"
        "不等式求解" -> "求解"
        "复数域" -> "复平面"
        "进制转换" -> "编码"
        "矩阵代数" -> "线代"
        "空间向量" -> "空间"
        "比例缩放" -> "换算"
        "全局设置", "设置" -> "设置"
        "结果格式" -> "格式"
        "单位换算" -> "单位"
        "常数表" -> "常数"
        "常规计算" -> "表达式"
        else -> "模块"
    }
}

private val CalculatorPanelShape = RoundedCornerShape(26.dp)
private val CalculatorInnerShape = RoundedCornerShape(22.dp)
private val CalculatorChipShape = RoundedCornerShape(18.dp)

private fun tileIcon(title: String): ImageVector {
    return when (title) {
        "常规计算" -> Icons.Outlined.Calculate
        "统计推断" -> Icons.Outlined.ScatterPlot
        "假设检验" -> Icons.Outlined.Hub
        "概率分布" -> Icons.Outlined.BubbleChart
        "数据表格" -> Icons.Outlined.Dataset
        "函数表格" -> Icons.Outlined.Functions
        "方程求解" -> Icons.Outlined.Tune
        "不等式求解" -> Icons.Outlined.SquareFoot
        "复数域" -> Icons.Outlined.Widgets
        "进制转换" -> Icons.Outlined.DataObject
        "矩阵代数" -> Icons.Outlined.ViewColumn
        "空间向量" -> Icons.Outlined.SwapHoriz
        "比例缩放" -> Icons.Outlined.AccountTree
        "全局设置", "设置" -> Icons.Outlined.Settings
        "结果格式" -> Icons.Outlined.Tune
        "单位换算" -> Icons.Outlined.SwapHoriz
        "常数表" -> Icons.Outlined.Tag
        else -> Icons.Outlined.Widgets
    }
}


@Composable
private fun CalculatorDetailHeader(
    title: String,
    subtitle: String,
    actionText: String,
    actionColor: Color,
    onAction: () -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val palette = tilePalette(title)
    CalculatorGlassPanel(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CalculatorInnerShape,
                    color = palette.primary.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = tileIcon(title),
                            contentDescription = null,
                            tint = palette.primary,
                            modifier = Modifier.width(22.dp).height(22.dp),
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = CalculatorChipShape,
                        color = Color.White.copy(alpha = 0.18f),
                    ) {
                        Text(
                            text = tileSection(title),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDarkMode) CloudWhite.copy(alpha = 0.76f) else ForestDeep.copy(alpha = 0.76f),
                        )
                    }
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = if (isDarkMode) CloudWhite else PineInk)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.72f) else ForestDeep.copy(alpha = 0.72f))
                }
            }
            Button(
                onClick = onAction,
                shape = CalculatorInnerShape,
                colors = ButtonDefaults.buttonColors(containerColor = actionColor),
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun CalculatorCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val palette = tilePalette(title)
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.36f),
        borderColor = Color.White.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CalculatorInnerShape,
                color = palette.primary.copy(alpha = 0.14f),
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.18f)),
            ) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(46.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = tileIcon(title),
                        contentDescription = null,
                        tint = palette.primary,
                        modifier = Modifier.width(22.dp).height(22.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    shape = CalculatorChipShape,
                    color = Color.White.copy(alpha = 0.18f),
                ) {
                    Text(
                        text = tileSection(title),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.74f) else ForestDeep.copy(alpha = 0.74f),
                    )
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, color = if (isDarkMode) CloudWhite else PineInk)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column {
            content()
        }
    }
}

@Composable
private fun ResultBlock(text: String) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val lines = text.lines().filter { it.isNotBlank() }
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.36f),
        borderColor = Color.White.copy(alpha = 0.44f),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("结果", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.62f) else ForestDeep.copy(alpha = 0.62f))
                Surface(shape = CalculatorChipShape, color = Color.White.copy(alpha = 0.14f)) {
                    Text(
                        text = if (lines.size > 1) "${lines.size} 行" else "输出",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.72f) else ForestDeep.copy(alpha = 0.72f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDarkMode) CloudWhite else PineInk,
            )
        }
    }
}

@Composable
private fun StatusTag(text: String, subtle: Boolean = false) {
    val background = when {
        subtle -> Color.White.copy(alpha = 0.42f)
        text.contains("显著") -> Color(0xFF2F8F63).copy(alpha = 0.88f)
        text.contains("不显著") -> Color(0xFF6B7A6E).copy(alpha = 0.82f)
        else -> Color(0xFF9D8B41).copy(alpha = 0.82f)
    }
    LiquidGlassSurface(
        modifier = Modifier,
        cornerRadius = 18.dp,
        tint = background,
        borderColor = Color.White.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (subtle) PineInk else CloudWhite,
        )
    }
}

@Composable
private fun SectionLead(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.28f),
        borderColor = Color.White.copy(alpha = 0.32f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("提示", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.74f) else ForestDeep.copy(alpha = 0.74f))
        }
    }
}

@Composable
private fun ComputeModulePro(
    settings: CalculatorSettings,
    topPadding: androidx.compose.ui.unit.Dp,
    expression: String,
    onExpressionChange: (String) -> Unit,
    result: String,
    onResultChange: (String) -> Unit,
    cursorIndex: Int,
    onCursorMove: (Int) -> Unit,
    history: SnapshotStateList<HistoryRecord>,
    onCopy: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 历史记录与显示区 (占满剩余空间)
        Box(modifier = Modifier.weight(1f).padding(top = 0.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                reverseLayout = true // 让最新计算结果在底部
            ) {
                // 当前输入显示
                item {
                    Column {
                        ProCalculatorDisplay(
                            value = expression,
                            cursorIndex = cursorIndex,
                            onCursorMove = onCursorMove,
                            result = result,
                            settings = settings,
                            modifier = Modifier.fillMaxWidth(),
                            onCopy = onCopy,
                            onDelete = {
                                if (cursorIndex > 0) {
                                    val nextExpr = expression.removeRange(cursorIndex - 1, cursorIndex)
                                    onExpressionChange(nextExpr)
                                    onCursorMove(cursorIndex - 1)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                
                // 历史记录
                items(
                    items = history.asReversed(), 
                    key = { it.id } // 使用稳定的唯一 ID，彻底修复滑动崩溃
                ) { record ->
                    val expr = record.expression
                    val res = record.result
                    
                    // 历史记录进入动画
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { isVisible = true }
                    
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally { width -> width / 3 } + fadeIn(),
                        exit = slideOutHorizontally { width -> -width } + fadeOut()
                    ) {
                        Column {
                            HistoryItem(
                                expr = expr, 
                                res = res,
                                onClick = {
                                    onExpressionChange(expr)
                                    onResultChange(res)
                                    onCursorMove(expr.length) // 这里由父组件的 safeCursorMove 接管
                                },
                                onCopy = onCopy,
                                onDelete = {
                                    isVisible = false
                                    // 延迟移除以允许退出动画播放
                                    history.remove(record)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        // 实时计算预览
        LaunchedEffect(expression) {
            if (expression.isNotBlank()) {
                val evalRes = runCatching {
                    val eval = ExpressionEngine.evaluateQuantity(expression, angleMode = settings.angleMode)
                    eval.toString(settings)
                }.getOrElse { e -> 
                    val msg = e.message ?: "未知错误"
                    if (msg.startsWith("语法错误") || msg.startsWith("计算错误") || msg.startsWith("单位不兼容")) msg else "计算错误" 
                }
                onResultChange(evalRes)
            } else {
                onResultChange("0")
            }
        }
    }
}

@Composable
private fun HistoryItem(expr: String, res: String, onClick: () -> Unit, onCopy: (String) -> Unit, onDelete: () -> Unit) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val haptic = LocalHapticFeedback.current
    val displayExpr = expr.replace("*", "×").replace("/", "÷")
    
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        runCatching {
                            if (offsetX < -150f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDelete()
                            }
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-200f, 0f)
                    }
                )
            }
    ) {
        // 背景删除提示
        if (offsetX < -20f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    "删除", 
                    color = Color.Red.copy(alpha = 0.8f), 
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick() 
                },
            cornerRadius = 20.dp,
            tint = if (isDarkMode) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.46f),
            borderColor = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.2f),
        ) {
            Column(
                modifier = Modifier.padding(14.dp).fillMaxWidth().pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick() 
                        },
                        onLongPress = {
                            onCopy(res)
                        }
                    )
                }, 
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatMathExpression(expr, if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f)), 
                    style = MaterialTheme.typography.bodyMedium
                )
                
                val numRes = res.toDoubleOrNull()
                val fraction = if (numRes != null) doubleToFraction(numRes) else null
                
                val formattedRes = runCatching {
                    val num = res.toDouble()
                    if (num % 1.0 == 0.0 && num < 1e12 && num > -1e12) {
                        java.text.DecimalFormat("#,###").format(num)
                    } else res
                }.getOrElse { res }
                
                Text(formattedRes, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.8f) else PineInk, fontWeight = FontWeight.Bold)
                
                if (fraction != null && fraction != formattedRes) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "= $fraction",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.5f) else ForestDeep.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatrixSectionHeader(
    section: String,
    body: String,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        tint = Color.White.copy(alpha = 0.28f),
        borderColor = Color.White.copy(alpha = 0.32f),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text("矩阵结构", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(section, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.74f) else ForestDeep.copy(alpha = 0.74f))
        }
    }
}

@Composable
private fun CalculatorGlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CalculatorPanelShape,
        color = Color.White.copy(alpha = 0.36f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BambooStroke.copy(alpha = 0.44f)),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent,
                            Color(0x149FD2B0),
                        ),
                    ),
                )
                .padding(18.dp),
            content = content,
        )
    }
}

@Composable
private fun CalculatorSectionTitle(title: String) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    Text(title, style = MaterialTheme.typography.headlineSmall, color = if (isDarkMode) CloudWhite else PineInk)
}

@Composable
private fun CalculatorInlineSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
        LiquidGlassSurface(
            modifier = Modifier,
            cornerRadius = 18.dp,
            tint = Color.White.copy(alpha = 0.14f),
            borderColor = Color.White.copy(alpha = 0.1f)
        ) {
            Text(
                text = "分组",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelMedium,
                color = ForestDeep.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun CalculatorInsetPanel(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        tint = Color.White.copy(alpha = 0.46f),
        borderColor = BambooStroke.copy(alpha = 0.26f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) CloudWhite.copy(alpha = 0.7f) else ForestDeep.copy(alpha = 0.7f),
                        )
                    }
                }
                content()
            },
        )
    }
}

@Composable
private fun CalculatorMetricRow(
    vararg items: Pair<String, String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.toList().chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { (label, value) ->
                    CalculatorMetricTile(
                        label = label,
                        value = value,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalculatorMetricTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = 18.dp,
        tint = Color.White.copy(alpha = 0.22f),
        borderColor = BambooStroke.copy(alpha = 0.18f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.62f) else ForestDeep.copy(alpha = 0.62f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
        }
    }
}

private fun previewEntryCount(raw: String): Int {
    return raw
        .split(",", "，", "\n", " ")
        .count { it.isNotBlank() }
}

@Composable
private fun FormulaEditor(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tokens: List<String>,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var editorValue by remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var builder by remember { mutableStateOf<String?>(null) }
    var editMode by remember { mutableStateOf<String?>(null) }
    var activeStructure by remember { mutableStateOf<FormulaStructure?>(null) }
    var slotA by remember { mutableStateOf("") }
    var slotB by remember { mutableStateOf("") }
    val structures = extractFormulaStructures(editorValue.text)
    val activeIndex = structures.indexOfFirst { it.key == activeStructure?.key }.let { idx -> if (idx >= 0) idx else -1 }
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        tint = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.44f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                Surface(
                    shape = CalculatorChipShape, 
                    color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = "公式输入",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.74f) else ForestDeep.copy(alpha = 0.74f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = editorValue,
                onValueChange = {
                    editorValue = it
                    onValueChange(it.text)
                },
                label = { Text("输入公式") },
                modifier = Modifier.fillMaxWidth(),
                shape = CalculatorInnerShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
                    cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineActionChip("光标左移") {
                    val next = (editorValue.selection.start - 1).coerceAtLeast(0)
                    editorValue = editorValue.copy(selection = TextRange(next))
                }
                InlineActionChip("光标右移") {
                    val next = (editorValue.selection.start + 1).coerceAtMost(editorValue.text.length)
                    editorValue = editorValue.copy(selection = TextRange(next))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                tint = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.05f) else Color(0x162F7553),
                borderColor = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.16f),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = if (editorValue.text.isBlank()) "原式：等待输入" else "原式：${editorValue.text}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.68f) else ForestDeep.copy(alpha = 0.68f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StructuredFormulaPreview(
                        value = editorValue.text,
                        activeKey = activeStructure?.key,
                        onEditStructure = { structure ->
                            builder = structure.mode
                            editMode = structure.mode
                            activeStructure = structure
                            slotA = structure.slotA
                            slotB = structure.slotB
                        },
                        onReorderStructure = { structure, delta ->
                            val currentStructures = extractFormulaStructures(editorValue.text)
                            val index = currentStructures.indexOfFirst { it.key == structure.key }
                            val moved = reorderStructure(editorValue.text, currentStructures, index, delta)
                            if (moved != null) {
                                editorValue = TextFieldValue(moved.first, TextRange(moved.first.length))
                                onValueChange(moved.first)
                                val nextStructures = extractFormulaStructures(moved.first)
                                activeStructure = nextStructures.getOrNull(moved.second)
                                activeStructure?.let {
                                    builder = it.mode
                                    editMode = it.mode
                                    slotA = it.slotA
                                    slotB = it.slotB
                                }
                            }
                        },
                    )
                }
            }
            if (activeStructure != null && activeIndex >= 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    InlineActionChip("上一个结构") {
                        if (activeIndex > 0) {
                            val previous = structures[activeIndex - 1]
                            activeStructure = previous
                            builder = previous.mode
                            editMode = previous.mode
                            slotA = previous.slotA
                            slotB = previous.slotB
                        }
                    }
                    InlineActionChip("下一个结构") {
                        if (activeIndex < structures.lastIndex) {
                            val next = structures[activeIndex + 1]
                            activeStructure = next
                            builder = next.mode
                            editMode = next.mode
                            slotA = next.slotA
                            slotB = next.slotB
                        }
                    }
                    Text(
                        text = "结构 ${activeIndex + 1}/${structures.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) CloudWhite.copy(alpha = 0.72f) else ForestDeep.copy(alpha = 0.72f),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                InlineStructureEditor(
                    structure = activeStructure!!,
                    slotA = slotA,
                    slotB = slotB,
                    onSlotAChange = { slotA = it },
                    onSlotBChange = { slotB = it },
                    onApply = {
                        val replaced = replaceStructuredFormula(editorValue.text, activeStructure!!, slotA, slotB)
                        val nextStructures = extractFormulaStructures(replaced)
                        val nextIndex = activeIndex.coerceAtMost(nextStructures.lastIndex)
                        editorValue = TextFieldValue(replaced, TextRange(replaced.length))
                        onValueChange(replaced)
                        activeStructure = nextStructures.getOrNull(nextIndex)
                        activeStructure?.let {
                            builder = it.mode
                            editMode = it.mode
                            slotA = it.slotA
                            slotB = it.slotB
                        }
                    },
                    onDone = {
                        activeStructure = null
                        builder = null
                        editMode = null
                        slotA = ""
                        slotB = ""
                    },
                )
            }
            if (builder != null && activeStructure == null) {
                Spacer(modifier = Modifier.height(8.dp))
                StructuredInsertPanel(
                    mode = builder.orEmpty(),
                    slotA = slotA,
                    slotB = slotB,
                    onSlotAChange = { slotA = it },
                    onSlotBChange = { slotB = it },
                    onCancel = {
                        builder = null
                        slotA = ""
                        slotB = ""
                    },
                    onInsert = {
                        val formula = buildStructuredFormula(builder!!, slotA, slotB)
                        val next = insertAtCursor(editorValue, formula)
                        editorValue = next
                        onValueChange(next.text)
                        builder = null
                        slotA = ""
                        slotB = ""
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tokens.forEach { token ->
                    Surface(
                        shape = CalculatorChipShape,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.52f),
                        modifier = Modifier.clickable {
                            if (token in listOf("a/b", "1/x", "√")) {
                                builder = token
                                editMode = null
                                activeStructure = null
                                slotA = ""
                                slotB = ""
                            } else {
                                val next = insertAtCursor(editorValue, appendFormulaToken("", token))
                                editorValue = next
                                onValueChange(next.text)
                            }
                        },
                    ) {
                        Text(
                            text = token,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDarkMode) CloudWhite else PineInk,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineStructureEditor(
    structure: FormulaStructure,
    slotA: String,
    slotB: String,
    onSlotAChange: (String) -> Unit,
    onSlotBChange: (String) -> Unit,
    onApply: () -> Unit,
    onDone: () -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        tint = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.08f) else Color(0x2234976B),
        borderColor = (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = 0.38f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = when (structure.mode) {
                    "a/b" -> "当前结构：分数"
                    "1/x" -> "当前结构：倒数"
                    else -> "当前结构：根式"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkMode) Color(0xFF66FFB2) else (if (isDarkMode) CloudWhite else PineInk),
            )
            Spacer(modifier = Modifier.height(8.dp))
            when (structure.mode) {
                "a/b" -> {
                    SlotFormulaInput(title = "分子", value = slotA, onValueChange = onSlotAChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    SlotFormulaInput(title = "分母", value = slotB, onValueChange = onSlotBChange)
                }
                "1/x" -> {
                    SlotFormulaInput(title = "分母", value = slotA, onValueChange = onSlotAChange)
                }
                else -> {
                    SlotFormulaInput(title = "根号内", value = slotA, onValueChange = onSlotAChange)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "即时预览：${prettyFormatExpression(buildStructuredFormula(structure.mode, slotA, slotB))}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) CloudWhite.copy(alpha = 0.76f) else ForestDeep.copy(alpha = 0.76f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineActionChip("更新结构", onClick = onApply)
                InlineActionChip("结束聚焦", onClick = onDone)
            }
        }
    }
}

@Composable
private fun SlotFormulaInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var editor by remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    OutlinedTextField(
        value = editor,
        onValueChange = {
            editor = it
            onValueChange(it.text)
        },
        label = { Text(title) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
            cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
        )
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        InlineActionChip("左移") {
            val next = (editor.selection.start - 1).coerceAtLeast(0)
            editor = editor.copy(selection = TextRange(next))
        }
        InlineActionChip("右移") {
            val next = (editor.selection.start + 1).coerceAtMost(editor.text.length)
            editor = editor.copy(selection = TextRange(next))
        }
    }
    val tree = summarizeStructureTree(editor.text)
    if (tree.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.34f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
            Text(
                text = tree,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkMode) CloudWhite.copy(alpha = 0.78f) else ForestDeep.copy(alpha = 0.78f),
            )
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf("a/b", "√", "1/x", "x²", "x³", "+", "-", "×", "÷", "(", ")").forEach { token ->
            InlineActionChip(token) {
                val next = insertAtCursor(editor, appendFormulaToken("", token))
                editor = next
                onValueChange(next.text)
            }
        }
    }
}

@Composable
private fun StructuredInsertPanel(
    mode: String,
    slotA: String,
    slotB: String,
    onSlotAChange: (String) -> Unit,
    onSlotBChange: (String) -> Unit,
    onCancel: () -> Unit,
    onInsert: () -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        tint = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.22f),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = when (mode) {
                    "a/b" -> "结构输入：分数"
                    "1/x" -> "结构输入：倒数"
                    else -> "结构输入：根式"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (isDarkMode) Color(0xFF66FFB2) else (if (isDarkMode) CloudWhite else PineInk),
            )
            Spacer(modifier = Modifier.height(8.dp))
            when (mode) {
                "a/b" -> {
                    SlotFormulaInput(title = "分子", value = slotA, onValueChange = onSlotAChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    SlotFormulaInput(title = "分母", value = slotB, onValueChange = onSlotBChange)
                }
                "1/x" -> {
                    SlotFormulaInput(title = "分母", value = slotA, onValueChange = onSlotAChange)
                }
                else -> {
                    SlotFormulaInput(title = "根号内", value = slotA, onValueChange = onSlotAChange)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "预览：${prettyFormatExpression(buildStructuredFormula(mode, slotA, slotB))}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkMode) CloudWhite.copy(alpha = 0.76f) else ForestDeep.copy(alpha = 0.76f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InlineActionChip("取消", onClick = onCancel)
                InlineActionChip("插入", onClick = onInsert)
            }
        }
    }
}

@Composable
private fun NaturalMatrixGrid(
    gridCols: Int,
    gridRows: Int = -1,
    isMatrixB: Boolean = false,
    fields: SnapshotStateList<String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val bracketColor = if (isDarkMode) CloudWhite.copy(alpha = 0.3f) else PineInk.copy(alpha = 0.2f)
    val totalRows = if (gridRows == -1) gridCols else gridRows
    val totalCols = gridCols
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 左中括号
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(if (totalRows == 3) 160.dp else 110.dp)
                .drawBehind {
                    val thickness = 2.dp.toPx()
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(0f, 0f), thickness)
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(0f, size.height), thickness)
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(0f, size.height), androidx.compose.ui.geometry.Offset(size.width, size.height), thickness)
                }
        )
        
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(totalRows) { r ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(totalCols) { c ->
                        val index = r * totalCols + c
                        val currentFocus = FocusTarget.MatrixGridCell(r, c, isMatrixB)
                        val isFocused = focusTarget == currentFocus
                        
                        Box(
                            modifier = Modifier
                                .size(if (totalRows == 3 && totalCols == 3) 70.dp else 90.dp)
                                .background(
                                    if (isFocused) (if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.15f) else ForestGreen.copy(alpha = 0.1f))
                                    else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .drawBehind {
                                    if (!isFocused) {
                                        drawCircle(
                                            color = bracketColor.copy(alpha = 0.1f),
                                            radius = 2.dp.toPx(),
                                            center = center
                                        )
                                    }
                                }
                                .clickable { onFocusChange(currentFocus) },
                            contentAlignment = Alignment.Center
                        ) {
                            val value = fields[index + (if (isMatrixB) 9 else 0)]
                            if (value.isEmpty() || value == "0") {
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.2f)
                                )
                            } else {
                                NaturalMathRenderer(
                                    expression = value,
                                    cursorIndex = -1, // 矩阵网格内暂时不显示物理光标以保持简洁
                                    fontSize = 16.sp,
                                    color = if (isDarkMode) CloudWhite else PineInk,
                                    isDarkMode = isDarkMode
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 右中括号
        Box(
            modifier = Modifier
                .width(12.dp)
                .height(if (totalRows == 3) 160.dp else 110.dp)
                .drawBehind {
                    val thickness = 2.dp.toPx()
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), thickness)
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(size.width, 0f), androidx.compose.ui.geometry.Offset(size.width, size.height), thickness)
                    drawLine(bracketColor, androidx.compose.ui.geometry.Offset(size.width, size.height), androidx.compose.ui.geometry.Offset(0f, size.height), thickness)
                }
        )
    }
}

@Composable
private fun StructuredFormulaPreview(
    value: String,
    activeKey: String?,
    onEditStructure: (FormulaStructure) -> Unit,
    onReorderStructure: (FormulaStructure, Int) -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    if (value.isBlank()) {
        Text(
            text = "排版预览：等待输入",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else PineInk.copy(alpha = 0.6f),
        )
        return
    }
    val formatted = prettyFormatExpression(value)
    val lines = formatExpressionLines(formatted)
    Text(
        text = "排版预览",
        style = MaterialTheme.typography.bodyMedium,
        color = if (isDarkMode) CloudWhite else PineInk,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.28f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            lines.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) CloudWhite else PineInk,
                )
                if (index != lines.lastIndex) Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
    val structures = extractFormulaStructures(value)
    val hasSquare = value.contains("^2")
    val hasCube = value.contains("^3")
    if (structures.isNotEmpty() || hasSquare || hasCube) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            structures.forEach { structure ->
                when (structure.mode) {
                    "a/b" -> FractionTemplateCard(
                        title = "分数",
                        numerator = structure.slotA.ifBlank { "□" },
                        denominator = structure.slotB.ifBlank { "□" },
                        active = structure.key == activeKey,
                        onClick = { onEditStructure(structure) },
                        onReorder = { delta -> onReorderStructure(structure, delta) },
                    )
                    "1/x" -> FractionTemplateCard(
                        title = "倒数",
                        numerator = "1",
                        denominator = structure.slotA.ifBlank { "□" },
                        active = structure.key == activeKey,
                        onClick = { onEditStructure(structure) },
                        onReorder = { delta -> onReorderStructure(structure, delta) },
                    )
                    "√" -> RadicalTemplateCard(
                        content = structure.slotA.ifBlank { "□" },
                        active = structure.key == activeKey,
                        onClick = { onEditStructure(structure) },
                        onReorder = { delta -> onReorderStructure(structure, delta) },
                    )
                }
            }
            if (hasSquare) PowerTemplateCard(text = "x", power = "2")
            if (hasCube) PowerTemplateCard(text = "x", power = "3")
        }
    }
}

@Composable
private fun FractionTemplateCard(
    title: String,
    numerator: String,
    denominator: String,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
    onReorder: ((Int) -> Unit)? = null,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var dragX by remember { mutableStateOf(0f) }
    var numeratorPx by remember { mutableStateOf(0) }
    var denominatorPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val adaptiveWidth = with(density) { (max(numeratorPx, denominatorPx).coerceAtLeast(32) + 24).toDp() }
    val animatedOffset by androidx.compose.animation.core.animateFloatAsState(targetValue = dragX, label = "fraction-drag")
    val glowColor by animateColorAsState(
        targetValue = when {
            dragX > 24f -> ForestGreen.copy(alpha = 0.28f)
            dragX < -24f -> Ginkgo.copy(alpha = 0.24f)
            active -> Color(0x332F8F63)
            else -> Color.White.copy(alpha = 0.5f)
        },
        label = "fraction-glow",
    )
    LiquidGlassSurface(
        modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .pointerInput(onReorder) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> dragX += dragAmount },
                    onDragEnd = {
                        if (dragX > 48f) onReorder?.invoke(1)
                        if (dragX < -48f) onReorder?.invoke(-1)
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f },
                )
            }
            .offset { IntOffset(animatedOffset.roundToInt(), 0) },
        cornerRadius = 18.dp,
        tint = glowColor,
        borderColor = if (active) ForestGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = if (isDarkMode) CloudWhite.copy(alpha = 0.68f) else ForestDeep.copy(alpha = 0.68f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(numerator, style = MaterialTheme.typography.bodyLarge, color = if (isDarkMode) CloudWhite else PineInk, onTextLayout = { numeratorPx = it.size.width })
            Box(
                modifier = Modifier
                    .width(adaptiveWidth)
                    .height(1.dp)
                    .background(ForestGreen.copy(alpha = 0.7f)),
            )
            Text(denominator, style = MaterialTheme.typography.bodyLarge, color = if (isDarkMode) CloudWhite else PineInk, onTextLayout = { denominatorPx = it.size.width })
        }
    }
}

@Composable
private fun RadicalTemplateCard(
    content: String,
    active: Boolean = false,
    onClick: (() -> Unit)? = null,
    onReorder: ((Int) -> Unit)? = null,
) {
    var dragX by remember { mutableStateOf(0f) }
    var contentPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val adaptiveWidth = with(density) { (contentPx.coerceAtLeast(36) + 22).toDp() }
    val animatedOffset by androidx.compose.animation.core.animateFloatAsState(targetValue = dragX, label = "radical-drag")
    val glowColor by animateColorAsState(
        targetValue = when {
            dragX > 24f -> ForestGreen.copy(alpha = 0.28f)
            dragX < -24f -> Ginkgo.copy(alpha = 0.24f)
            active -> Color(0x332F8F63)
            else -> Color.White.copy(alpha = 0.5f)
        },
        label = "radical-glow",
    )
    LiquidGlassSurface(
        modifier = (if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .pointerInput(onReorder) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> dragX += dragAmount },
                    onDragEnd = {
                        if (dragX > 48f) onReorder?.invoke(1)
                        if (dragX < -48f) onReorder?.invoke(-1)
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f },
                )
            }
            .offset { IntOffset(animatedOffset.roundToInt(), 0) },
        cornerRadius = 18.dp,
        tint = glowColor,
        borderColor = if (active) ForestGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("√", style = MaterialTheme.typography.headlineSmall, color = ForestGreen)
            Column {
                Box(
                    modifier = Modifier
                        .width(adaptiveWidth)
                        .height(1.dp)
                        .background(ForestGreen.copy(alpha = 0.7f)),
                )
                Text(content, style = MaterialTheme.typography.bodyLarge, color = PineInk, onTextLayout = { contentPx = it.size.width })
            }
        }
    }
}

@Composable
private fun PowerTemplateCard(
    text: String,
    power: String,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier,
        cornerRadius = 18.dp,
        tint = Color.White.copy(alpha = 0.5f),
        borderColor = Color.White.copy(alpha = 0.2f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = if (isDarkMode) CloudWhite else PineInk)
            Text(power, style = MaterialTheme.typography.bodySmall, color = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen)
        }
    }
}

@Composable
private fun ProCell(
    value: String,
    label: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        cornerRadius = 16.dp,
        tint = if (isFocused) (if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.12f) else ForestGreen.copy(alpha = 0.08f)) else (if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.28f)),
        borderColor = if (isFocused) (if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.4f) else ForestGreen.copy(alpha = 0.4f)) else (if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = if (isDarkMode) CloudWhite.copy(alpha = 0.5f) else ForestDeep.copy(alpha = 0.5f))
            Text(
                text = formatMathExpression(value, if (isFocused) (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen) else (if (isDarkMode) CloudWhite else PineInk)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProInputField(
    value: String,
    label: String,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        cornerRadius = 20.dp,
        tint = if (isFocused) (if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.12f) else ForestGreen.copy(alpha = 0.08f)) else (if (isDarkMode) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.24f)),
        borderColor = if (isFocused) (if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.4f) else ForestGreen.copy(alpha = 0.4f)) else (if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(6.dp))
            if (value.isEmpty()) {
                Text(
                    text = "点击输入数据...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PineInk.copy(alpha = 0.3f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = formatMathExpression(value, if (isDarkMode) CloudWhite else PineInk),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StatisticsModulePro(
    settings: CalculatorSettings,
    rawX: String,
    rawY: String,
    onRawXChange: (String) -> Unit,
    onRawYChange: (String) -> Unit,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var mode by remember { mutableStateOf("单变量") }
    var result by remember { mutableStateOf("结果将在这里显示") }
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    
    // 解析数据为列表
    val listX = remember(rawX) { rawX.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList() }
    val listY = remember(rawY) { rawY.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList() }
    
    // 确保至少有一定行数供显示
    val displayRows = 5
    
    CalculatorCard("统计 Pro") {
        ModuleSelector(listOf("单变量", "回归分析"), mode) { mode = it }
        Spacer(modifier = Modifier.height(16.dp))
        
        // 数据表格标题
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No.", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelSmall, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.5f), textAlign = TextAlign.Center)
            Text("X 数据", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.5f), textAlign = TextAlign.Center)
            if (mode != "单变量") {
                Text("Y 数据", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.5f), textAlign = TextAlign.Center)
            }
        }

        // 数据编辑网格
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(displayRows) { r ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${r + 1}", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodySmall, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.4f), textAlign = TextAlign.Center)
                    
                    // X 单元格
                    EquationGridCell(
                        value = listX.getOrNull(r) ?: "",
                        label = "x${r+1}",
                        isFocused = focusTarget is FocusTarget.StatisticsCell && !focusTarget.isY && focusTarget.index == r,
                        onClick = { onFocusChange(FocusTarget.StatisticsCell(r, false)) }
                    )
                    
                    if (mode != "单变量") {
                        // Y 单元格
                        EquationGridCell(
                            value = listY.getOrNull(r) ?: "",
                            label = "y${r+1}",
                            isFocused = focusTarget is FocusTarget.StatisticsCell && focusTarget.isY && focusTarget.index == r,
                            onClick = { onFocusChange(FocusTarget.StatisticsCell(r, true)) }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CalcButton("执行分析", modifier = Modifier.weight(1f)) {
                result = runCatching {
                    val valuesX = listX.mapNotNull { it.toDoubleOrNull() }
                    if (valuesX.isEmpty()) throw Exception("X 数据为空")
                    
                    if (mode == "单变量") {
                        val mean = valuesX.average()
                        val variance = valuesX.map { (it - mean).pow(2) }.average()
                        "样本数: ${valuesX.size}\n均值: ${formatBySetting(mean, settings)}\n标准差: ${formatBySetting(sqrt(variance), settings)}\n方差: ${formatBySetting(variance, settings)}"
                    } else {
                        val valuesY = listY.mapNotNull { it.toDoubleOrNull() }
                        if (valuesY.size < valuesX.size) throw Exception("Y 数据长度不足")
                        "回归分析暂未实现..."
                    }
                }.getOrElse { it.message ?: "分析失败" }
            }
            
            CalcButton("清空", modifier = Modifier.weight(0.4f)) {
                onRawXChange("")
                onRawYChange("")
                result = "已清空数据"
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun MatrixModulePro(
    settings: CalculatorSettings,
    fields: SnapshotStateList<String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var dimension by remember { mutableStateOf("3x3") }
    var mode by remember { mutableStateOf("行列式") }
    var result by remember { mutableStateOf("支持行列式、逆矩阵等运算") }
    
    val size = if (dimension == "2x2") 2 else 3
    
    CalculatorCard("矩阵") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ModuleSelector(listOf("2x2", "3x3"), dimension) { dimension = it }
            }
            Box(modifier = Modifier.weight(1.5f)) {
                ModuleSelector(listOf("行列式", "逆矩阵", "乘法", "转置"), mode) { mode = it }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 矩阵 A 编辑区
        Text("矩阵 A", style = MaterialTheme.typography.titleSmall, color = if (isDarkMode) CloudWhite else PineInk)
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(size) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(size) { col ->
                        val index = row * size + col
                        ProCell(
                            value = fields[index],
                            label = "A${row+1}${col+1}",
                            isFocused = focusTarget == FocusTarget.MatrixCell(index, false),
                            onClick = { onFocusChange(FocusTarget.MatrixCell(index, false)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        if (mode == "乘法") {
            Spacer(modifier = Modifier.height(20.dp))
            Text("矩阵 B", style = MaterialTheme.typography.titleSmall, color = if (isDarkMode) CloudWhite else PineInk)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(size) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(size) { col ->
                            val index = row * size + col
                            ProCell(
                                value = fields[index + 9],
                                label = "B${row+1}${col+1}",
                                isFocused = focusTarget == FocusTarget.MatrixCell(index, true),
                                onClick = { onFocusChange(FocusTarget.MatrixCell(index, true)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        CalcButton("开始计算") {
            result = "计算中..."
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun BaseModulePro(
    value: String,
    onValueChange: (String) -> Unit,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var sourceBase by remember { mutableStateOf("10") }
    
    CalculatorCard("进制转换") {
        ModuleSelector(listOf("2", "8", "10", "16"), sourceBase) { sourceBase = it }
        Spacer(modifier = Modifier.height(16.dp))
        
        ProInputField(
            value = value,
            label = "输入值 (${sourceBase}进制)",
            isFocused = focusTarget == FocusTarget.BaseInput,
            onClick = { onFocusChange(FocusTarget.BaseInput) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        val result = runCatching {
            val longVal = value.toLong(sourceBase.toInt())
            "HEX: ${longVal.toString(16).uppercase()}\nDEC: ${longVal.toString(10)}\nOCT: ${longVal.toString(8)}\nBIN: ${longVal.toString(2)}"
        }.getOrElse { "输入格式无效" }
        
        ResultBlock(result)
    }
}

@Composable
private fun EquationModulePro(
    fields: MutableMap<String, String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var mode by remember { mutableStateOf("多项式") }
    var degree by remember { mutableStateOf("2次") }
    var result by remember { mutableStateOf("结果将在这里显示") }
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper

    CalculatorCard("方程 Pro") {
        ModuleSelector(listOf("线性方程", "二元一次", "多项式"), mode) { 
            mode = it 
            // 切换模式时重置焦点
            onFocusChange(FocusTarget.EquationGridCell(0, 0, "eq"))
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (mode) {
            "线性方程" -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("求解 ax + b = 0", style = MaterialTheme.typography.labelMedium, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        EquationGridCell(
                            value = fields.getOrDefault("eq_r0c0", "1"),
                            label = "a",
                            isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == 0 && focusTarget.col == 0,
                            onClick = { onFocusChange(FocusTarget.EquationGridCell(0, 0)) }
                        )
                        Text(" x + ", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                        EquationGridCell(
                            value = fields.getOrDefault("eq_r0c1", "0"),
                            label = "b",
                            isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == 0 && focusTarget.col == 1,
                            onClick = { onFocusChange(FocusTarget.EquationGridCell(0, 1)) }
                        )
                        Text(" = 0", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CalcButton("执行线性求解") {
                    result = runCatching {
                        val a = fields.getOrDefault("eq_r0c0", "1").toDouble()
                        val b = fields.getOrDefault("eq_r0c1", "0").toDouble()
                        if (abs(a) < 1e-9) {
                            if (abs(b) < 1e-9) "无数解" else "无解"
                        } else {
                            "x = ${formatNumber(-b / a)}"
                        }
                    }.getOrElse { "输入无效" }
                }
            }
            "二元一次" -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("a₁x + b₁y = c₁\na₂x + b₂y = c₂", style = MaterialTheme.typography.labelMedium, color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.6f))
                    
                    repeat(2) { r ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            EquationGridCell(
                                value = fields.getOrDefault("eq_r${r}c0", if(r==0) "1" else "0"),
                                label = if(r==0) "a1" else "a2",
                                isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == r && focusTarget.col == 0,
                                onClick = { onFocusChange(FocusTarget.EquationGridCell(r, 0)) }
                            )
                            Text(" x + ", style = MaterialTheme.typography.titleSmall, color = if (isDarkMode) CloudWhite else PineInk)
                            EquationGridCell(
                                value = fields.getOrDefault("eq_r${r}c1", if(r==0) "0" else "1"),
                                label = if(r==0) "b1" else "b2",
                                isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == r && focusTarget.col == 1,
                                onClick = { onFocusChange(FocusTarget.EquationGridCell(r, 1)) }
                            )
                            Text(" y = ", style = MaterialTheme.typography.titleSmall, color = if (isDarkMode) CloudWhite else PineInk)
                            EquationGridCell(
                                value = fields.getOrDefault("eq_r${r}c2", "0"),
                                label = if(r==0) "c1" else "c2",
                                isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == r && focusTarget.col == 2,
                                onClick = { onFocusChange(FocusTarget.EquationGridCell(r, 2)) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CalcButton("执行克莱姆法则求解") {
                    result = runCatching {
                        val a1 = fields.getOrDefault("eq_r0c0", "1").toDouble()
                        val b1 = fields.getOrDefault("eq_r0c1", "1").toDouble()
                        val c1 = fields.getOrDefault("eq_r0c2", "0").toDouble()
                        val a2 = fields.getOrDefault("eq_r1c0", "1").toDouble()
                        val b2 = fields.getOrDefault("eq_r1c1", "1").toDouble()
                        val c2 = fields.getOrDefault("eq_r1c2", "0").toDouble()
                        val det = a1 * b2 - a2 * b1
                        if (abs(det) < 1e-9) "行列式为 0，无唯一解"
                        else "x = ${formatNumber((c1 * b2 - c2 * b1) / det)}\ny = ${formatNumber((a1 * c2 - a2 * c1) / det)}"
                    }.getOrElse { "输入无效" }
                }
            }
            "多项式" -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModuleSelector(listOf("2次", "3次", "4次"), degree) { 
                        degree = it 
                        onFocusChange(FocusTarget.EquationGridCell(0, 0))
                    }
                    val count = when(degree) { "2次" -> 3; "3次" -> 4; else -> 5 }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(count) { i ->
                            val power = count - 1 - i
                            EquationGridCell(
                                value = fields.getOrDefault("eq_r0c$i", if(i==0) "1" else "0"),
                                label = ('a' + i).toString(),
                                isFocused = focusTarget is FocusTarget.EquationGridCell && focusTarget.row == 0 && focusTarget.col == i,
                                onClick = { onFocusChange(FocusTarget.EquationGridCell(0, i)) }
                            )
                            if (power > 0) {
                                Text(
                                    if (power > 1) "x$power + " else "x + ",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isDarkMode) CloudWhite else PineInk
                                )
                            } else {
                                Text(" = 0", style = MaterialTheme.typography.titleSmall, color = if (isDarkMode) CloudWhite else PineInk)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                CalcButton("Durand-Kerner 算法求解") {
                    result = runCatching {
                        val count = when(degree) { "2次" -> 3; "3次" -> 4; else -> 5 }
                        val coeffs = (0 until count).map { i -> fields.getOrDefault("eq_r0c$i", if(i==0) "1" else "0").toDouble() }
                        solvePolynomial(coeffs)
                    }.getOrElse { it.message ?: "输入无效" }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun EquationGridCell(
    value: String,
    label: String,
    isFocused: Boolean,
    onClick: () -> Unit
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val focusColor = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.15f) else ForestGreen.copy(alpha = 0.1f)
    val dotColor = if (isDarkMode) CloudWhite.copy(alpha = 0.1f) else PineInk.copy(alpha = 0.1f)
    
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 48.dp)
            .background(
                if (isFocused) focusColor else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .drawBehind {
                if (!isFocused) {
                    drawCircle(color = dotColor, radius = 1.5.dp.toPx(), center = center)
                }
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty() || value == "0") {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.3f)
            )
        } else {
            NaturalMathRenderer(
                expression = value,
                cursorIndex = -1,
                fontSize = 14.sp,
                color = if (isDarkMode) CloudWhite else PineInk,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun VectorModulePro(
    settings: CalculatorSettings,
    fields: MutableMap<String, String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var mode by remember { mutableStateOf("点积") }
    var result by remember { mutableStateOf("结果将在这里显示") }

    CalculatorCard("向量 Pro") {
        ModuleSelector(listOf("点积", "叉积", "夹角", "模长"), mode) { mode = it }
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("向量 A", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProCell(fields.getOrDefault("v_ax", "1"), "x", focusTarget == FocusTarget.GenericInput("v_ax"), { onFocusChange(FocusTarget.GenericInput("v_ax")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("v_ay", "0"), "y", focusTarget == FocusTarget.GenericInput("v_ay"), { onFocusChange(FocusTarget.GenericInput("v_ay")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("v_az", "0"), "z", focusTarget == FocusTarget.GenericInput("v_az"), { onFocusChange(FocusTarget.GenericInput("v_az")) }, Modifier.weight(1f))
            }
            
            Text("向量 B", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProCell(fields.getOrDefault("v_bx", "0"), "x", focusTarget == FocusTarget.GenericInput("v_bx"), { onFocusChange(FocusTarget.GenericInput("v_bx")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("v_by", "1"), "y", focusTarget == FocusTarget.GenericInput("v_by"), { onFocusChange(FocusTarget.GenericInput("v_by")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("v_bz", "0"), "z", focusTarget == FocusTarget.GenericInput("v_bz"), { onFocusChange(FocusTarget.GenericInput("v_bz")) }, Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("执行向量运算") {
            result = runCatching {
                val av = listOf(
                    fields.getOrDefault("v_ax", "1").toDouble(),
                    fields.getOrDefault("v_ay", "0").toDouble(),
                    fields.getOrDefault("v_az", "0").toDouble()
                )
                val bv = listOf(
                    fields.getOrDefault("v_bx", "0").toDouble(),
                    fields.getOrDefault("v_by", "1").toDouble(),
                    fields.getOrDefault("v_bz", "0").toDouble()
                )
                val dot = av.zip(bv).sumOf { it.first * it.second }
                val normA = sqrt(av.sumOf { it * it })
                val normB = sqrt(bv.sumOf { it * it })
                
                when (mode) {
                    "点积" -> "A·B = ${formatBySetting(dot, settings)}"
                    "模长" -> "|A| = ${formatBySetting(normA, settings)}\n|B| = ${formatBySetting(normB, settings)}"
                    "叉积" -> {
                        val cx = av[1] * bv[2] - av[2] * bv[1]
                        val cy = av[2] * bv[0] - av[0] * bv[2]
                        val cz = av[0] * bv[1] - av[1] * bv[0]
                        "A×B = (${formatNumber(cx)}, ${formatNumber(cy)}, ${formatNumber(cz)})"
                    }
                    "夹角" -> {
                        val cosTheta = (dot / (normA * normB)).coerceIn(-1.0, 1.0)
                        val angleRad = acos(cosTheta)
                        if (settings.angleMode == AngleMode.Deg) "${formatNumber(angleRad * 180 / PI)}°" else "${formatNumber(angleRad)} rad"
                    }
                    else -> ""
                }
            }.getOrElse { "输入无效" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun ComplexModulePro(
    fields: MutableMap<String, String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var mode by remember { mutableStateOf("代数式") }
    var result by remember { mutableStateOf("结果将在这里显示") }
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper

    CalculatorCard("复数 Pro") {
        ModuleSelector(listOf("代数式", "极坐标"), mode) { 
            mode = it 
            onFocusChange(FocusTarget.ComplexGridCell(0, it == "极坐标"))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(2) { i ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("z${i+1} = ", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                    
                    if (mode == "代数式") {
                        // a + bi
                        val reKey = "c_a${i*2}"
                        val imKey = "c_a${i*2+1}"
                        
                        EquationGridCell(
                            value = fields.getOrDefault(reKey, if(i==0) "2" else "1"),
                            label = "实部",
                            isFocused = focusTarget is FocusTarget.ComplexGridCell && !focusTarget.isPolar && focusTarget.index == i*2,
                            onClick = { onFocusChange(FocusTarget.ComplexGridCell(i*2, false)) }
                        )
                        Text(" + ", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                        EquationGridCell(
                            value = fields.getOrDefault(imKey, if(i==0) "3" else "-4"),
                            label = "虚部",
                            isFocused = focusTarget is FocusTarget.ComplexGridCell && !focusTarget.isPolar && focusTarget.index == i*2+1,
                            onClick = { onFocusChange(FocusTarget.ComplexGridCell(i*2+1, false)) }
                        )
                        Text(" i", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                    } else {
                        // r ∠ θ
                        val rKey = "c_p${i*2}"
                        val tKey = "c_p${i*2+1}"
                        
                        EquationGridCell(
                            value = fields.getOrDefault(rKey, if(i==0) "5" else "3"),
                            label = "模长",
                            isFocused = focusTarget is FocusTarget.ComplexGridCell && focusTarget.isPolar && focusTarget.index == i*2,
                            onClick = { onFocusChange(FocusTarget.ComplexGridCell(i*2, true)) }
                        )
                        Text(" ∠ ", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                        EquationGridCell(
                            value = fields.getOrDefault(tKey, if(i==0) "45" else "30"),
                            label = "辐角",
                            isFocused = focusTarget is FocusTarget.ComplexGridCell && focusTarget.isPolar && focusTarget.index == i*2+1,
                            onClick = { onFocusChange(FocusTarget.ComplexGridCell(i*2+1, true)) }
                        )
                        Text(" °", style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        CalcButton(if (mode == "代数式") "执行四则运算" else "转换与运算") {
            result = runCatching {
                if (mode == "代数式") {
                    val z1re = fields.getOrDefault("c_a0", "2").toDouble()
                    val z1im = fields.getOrDefault("c_a1", "3").toDouble()
                    val z2re = fields.getOrDefault("c_a2", "1").toDouble()
                    val z2im = fields.getOrDefault("c_a3", "-4").toDouble()
                    
                    val z1 = ComplexNumber(z1re, z1im)
                    val z2 = ComplexNumber(z2re, z2im)
                    val sum = z1 + z2
                    val diff = z1 - z2
                    val prod = z1 * z2
                    val quot = z1 / z2
                    "z1 + z2 = $sum\nz1 - z2 = $diff\nz1 × z2 = $prod\nz1 / z2 = $quot"
                } else {
                    val r1 = fields.getOrDefault("c_p0", "5").toDouble()
                    val t1 = fields.getOrDefault("c_p1", "45").toDouble() * PI / 180
                    val r2 = fields.getOrDefault("c_p2", "3").toDouble()
                    val t2 = fields.getOrDefault("c_p3", "30").toDouble() * PI / 180
                    
                    val z1 = ComplexNumber(r1 * cos(t1), r1 * sin(t1))
                    val z2 = ComplexNumber(r2 * cos(t2), r2 * sin(t2))
                    
                    val prodR = r1 * r2
                    val prodT = (t1 + t2) * 180 / PI
                    "z1 (代数): $z1\nz2 (代数): $z2\nz1×z2 (极): ${formatNumber(prodR)}∠${formatNumber(prodT)}°"
                }
            }.getOrElse { "输入无效" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun InequalityModulePro(
    fields: MutableMap<String, String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var sign by remember { mutableStateOf("≥ 0") }
    var result by remember { mutableStateOf("结果将在这里显示") }
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper

    CalculatorCard("不等式 Pro") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("求解 ax² + bx + c", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProCell(fields.getOrDefault("ineq_a", "1"), "a", focusTarget == FocusTarget.GenericInput("ineq_a"), { onFocusChange(FocusTarget.GenericInput("ineq_a")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("ineq_b", "-3"), "b", focusTarget == FocusTarget.GenericInput("ineq_b"), { onFocusChange(FocusTarget.GenericInput("ineq_b")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("ineq_c", "2"), "c", focusTarget == FocusTarget.GenericInput("ineq_c"), { onFocusChange(FocusTarget.GenericInput("ineq_c")) }, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            ModuleSelector(listOf("≥ 0", "> 0", "≤ 0", "< 0"), sign) { sign = it }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("执行不等式求解") {
            result = runCatching {
                val a = fields.getOrDefault("ineq_a", "1").toDouble()
                val b = fields.getOrDefault("ineq_b", "-3").toDouble()
                val c = fields.getOrDefault("ineq_c", "2").toDouble()
                solveQuadraticInequality(a, b, c, sign)
            }.getOrElse { "输入无效" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun RatioModulePro(
    fields: MutableMap<String, String>,
    focusTarget: FocusTarget,
    onFocusChange: (FocusTarget) -> Unit
) {
    var mode by remember { mutableStateOf("正比例") }
    var result by remember { mutableStateOf("结果将在这里显示") }
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper

    CalculatorCard("比例 Pro") {
        ModuleSelector(listOf("正比例", "反比例", "缩放"), mode) { mode = it }
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (mode == "缩放") "a : b × 倍率" else "a / b = c / x", style = MaterialTheme.typography.labelMedium, color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProCell(fields.getOrDefault("rat_a", "3"), "a", focusTarget == FocusTarget.GenericInput("rat_a"), { onFocusChange(FocusTarget.GenericInput("rat_a")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("rat_b", "5"), "b", focusTarget == FocusTarget.GenericInput("rat_b"), { onFocusChange(FocusTarget.GenericInput("rat_b")) }, Modifier.weight(1f))
                ProCell(fields.getOrDefault("rat_c", "9"), if(mode=="缩放") "倍率" else "c", focusTarget == FocusTarget.GenericInput("rat_c"), { onFocusChange(FocusTarget.GenericInput("rat_c")) }, Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("执行比例运算") {
            result = runCatching {
                val av = fields.getOrDefault("rat_a", "3").toDouble()
                val bv = fields.getOrDefault("rat_b", "5").toDouble()
                val cv = fields.getOrDefault("rat_c", "9").toDouble()
                when (mode) {
                    "正比例" -> "x = ${formatNumber(bv * cv / av)}"
                    "反比例" -> "x = ${formatNumber(av * bv / cv)}"
                    else -> "${formatNumber(av * cv)} : ${formatNumber(bv * cv)}"
                }
            }.getOrElse { "输入无效" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun MatrixGridEditor(
    title: String,
    size: Int,
    labels: List<String>,
    values: SnapshotStateList<String>,
    offset: Int = 0,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        contentPadding = PaddingValues(0.dp),
        tint = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.42f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.22f),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = if (isDarkMode) CloudWhite else PineInk)
            Spacer(modifier = Modifier.height(10.dp))
            repeat(size) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(size) { col ->
                        val index = offset + row * size + col
                        OutlinedTextField(
                            value = values[index],
                            onValueChange = { newValue -> values[index] = newValue },
                            label = { Text(labels[row * size + col]) },
                            modifier = Modifier.width(if (size == 2) 150.dp else 96.dp),
                            shape = RoundedCornerShape(18.dp),
                        )
                    }
                }
                if (row != size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CalcButton(text: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    Button(
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.2f) else ForestGreen),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, color = if (isDarkMode) Color(0xFF66FFB2) else CloudWhite, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun formatMathExpression(expression: String, baseColor: Color): androidx.compose.ui.text.AnnotatedString {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val operatorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
    val functionColor = if (isDarkMode) Color(0xFFFFB266) else Color(0xFFD35400)
    val bracketColor = baseColor.copy(alpha = 0.4f)

    return runCatching {
        androidx.compose.ui.text.buildAnnotatedString {
            val displayValue = expression.replace("*", "×").replace("/", "÷")
            var i = 0
            while (i < displayValue.length) {
                val char = displayValue[i]
                when {
                    char == '^' -> {
                        // Start superscript for the next number or variable
                        i++
                        var exponent = ""
                        while (i < displayValue.length && (displayValue[i].isDigit() || displayValue[i] == '.' || displayValue[i] == '-')) {
                            exponent += displayValue[i]
                            i++
                        }
                        if (exponent.isEmpty() && i < displayValue.length && displayValue[i].isLetter()) {
                            exponent += displayValue[i]
                            i++
                        }
                        if (exponent.isNotEmpty()) {
                            withStyle(androidx.compose.ui.text.SpanStyle(
                                baselineShift = androidx.compose.ui.text.style.BaselineShift.Superscript,
                                fontSize = androidx.compose.ui.unit.TextUnit(0.7f, androidx.compose.ui.unit.TextUnitType.Em),
                                color = functionColor
                            )) {
                                append(exponent)
                            }
                        }
                        continue // Skip the normal i++ at the end of the loop
                    }
                    char.isDigit() || char == '.' -> {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = baseColor)) {
                            append(char)
                        }
                    }
                    char in listOf('+', '-', '×', '÷', '=', '%', '!', '√') -> {
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            color = operatorColor, 
                            fontWeight = FontWeight.Bold
                        )) {
                            append(char)
                        }
                    }
                    char in listOf('(', ')') -> {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = bracketColor)) {
                            append(char)
                        }
                    }
                    char.isLetter() -> {
                        withStyle(androidx.compose.ui.text.SpanStyle(
                            color = functionColor,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )) {
                            append(char)
                        }
                    }
                    else -> {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = baseColor)) {
                            append(char)
                        }
                    }
                }
                i++
            }
        }
    }.getOrElse { 
        androidx.compose.ui.text.AnnotatedString(expression) 
    }
}

@Composable
private fun GraphSparkline(
    expression: String,
    angleMode: AngleMode,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (!expression.contains("x") || expression.length > 100) return

    val points = remember(expression, angleMode) {
        val list = mutableListOf<androidx.compose.ui.geometry.Offset>()
        val range = 10.0
        val steps = 40 // 降低步数以提升性能
        runCatching {
            for (i in 0..steps) {
                val x = -range + (2 * range * i / steps)
                val q = ExpressionEngine.evaluateQuantity(expression, xValue = x, angleMode = angleMode)
                val y = q.value
                if (!y.isNaN() && !y.isInfinite() && abs(y) < 1000) {
                    list.add(androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat()))
                }
            }
        }
        list
    }

    if (points.size < 2) return

    Canvas(modifier = modifier.height(24.dp).fillMaxWidth().padding(horizontal = 12.dp)) {
        val width = size.width
        val height = size.height
        
        runCatching {
            if (points.isEmpty()) return@runCatching
            
            val minX = points.minOf { it.x }
            val maxX = points.maxOf { it.x }
            val minY = points.minOf { it.y }
            val maxY = points.maxOf { it.y }
            
            val rangeX = if (maxX == minX) 1f else maxX - minX
            val rangeY = if (maxY == minY) 1f else maxY - minY
            
            val path = androidx.compose.ui.graphics.Path()
            points.forEachIndexed { index, pt ->
                val px = (pt.x - minX) / rangeX * width
                val py = height - (pt.y - minY) / rangeY * height
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            
            drawPath(
                path = path,
                color = (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = 0.4f),
                style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun ProCalculatorDisplay(
    value: String,
    cursorIndex: Int,
    onCursorMove: (Int) -> Unit,
    result: String,
    settings: CalculatorSettings,
    modifier: Modifier = Modifier,
    onCopy: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val haptic = LocalHapticFeedback.current

    // 触觉反馈增强：当光标位置改变时触发轻微震动 (Haptic Ticks) - 增加节流防止过载
    var lastHapticTime by remember { mutableStateOf(0L) }
    LaunchedEffect(cursorIndex) {
        val now = System.currentTimeMillis()
        if (value.isNotEmpty() && now - lastHapticTime > 50) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticTime = now
        }
    }
    
    // 格式化表达式用于显示
    val displayValue = value.replace("*", "×").replace("/", "÷")
    
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 32.dp,
        tint = if (isDarkMode) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.28f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.36f),
        blurRadius = 30.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 状态指示器 (Deg/Rad)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = settings.angleMode.title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (value.length > 15) {
                    Text(
                        text = "${value.length} 字符",
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = 0.4f)
                    )
                }
            }
            
            // 实时函数图像预览 (Sparkline)
            GraphSparkline(
                expression = value,
                angleMode = settings.angleMode,
                isDarkMode = isDarkMode,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // 表达式输入行 (带模拟光标与区域侧滑控制光标位置)
            var offsetX by remember { mutableStateOf(0f) }
            val animatedOffsetX by animateFloatAsState(targetValue = offsetX)
            val haptic = LocalHapticFeedback.current
            
            // 用于计算拖动灵敏度的累加器
            var cursorDragAccumulator by remember { mutableStateOf(0f) }
            // 表达式改变时重置累加器，防止残留位移导致闪退
            LaunchedEffect(value) { cursorDragAccumulator = 0f }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        // 使用 awaitEachGesture 手动分发，确保不与内部 Scroll 冲突
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            var dragAccumulator = 0f
                            
                            while (true) {
                                val event = awaitPointerEvent()
                                val dragEvent = event.changes.firstOrNull() ?: break
                                
                                 if (dragEvent.pressed) {
                                    val dragAmount = dragEvent.position.x - dragEvent.previousPosition.x
                                    
                                    // 分配给滑动删除或光标移动
                                    if (cursorIndex == value.length && offsetX < 0) {
                                        offsetX = (offsetX + dragAmount).coerceAtMost(0f).coerceAtLeast(-100f)
                                        dragEvent.consume()
                                    } else {
                                        dragAccumulator += dragAmount
                                        val threshold = 35f 
                                        if (abs(dragAccumulator) > threshold) {
                                            val direction = if (dragAccumulator > 0) 1 else -1
                                            if ((direction == 1 && cursorIndex < value.length) || (direction == -1 && cursorIndex > 0)) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                onCursorMove(cursorIndex + direction)
                                                dragEvent.consume()
                                            }
                                            dragAccumulator = 0f
                                        }
                                    }
                                } else {
                                    // 抬起手势
                                    if (offsetX < -50f && value.isNotEmpty()) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDelete?.invoke()
                                    }
                                    offsetX = 0f
                                    break
                                }
                            }
                        }
                    }
                    .offset { IntOffset(animatedOffsetX.roundToInt().coerceIn(-150, 0), 0) },
                contentAlignment = Alignment.CenterEnd
            ) {
                // 无缝动态缩放算法：根据字符长度平滑缩放字体
                val baseFontSize = MaterialTheme.typography.headlineMedium.fontSize.value
                val minFontSize = MaterialTheme.typography.titleMedium.fontSize.value
                val calculatedSize = if (value.length <= 12) baseFontSize else (baseFontSize - (value.length - 12) * 1.5f).coerceAtLeast(minFontSize)
                val expressionFontSize = androidx.compose.ui.unit.TextUnit(calculatedSize, androidx.compose.ui.unit.TextUnitType.Sp)
                
                NaturalMathRenderer(
                    expression = value,
                    cursorIndex = cursorIndex,
                    fontSize = expressionFontSize,
                    color = if (isDarkMode) CloudWhite else PineInk,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 结果展示行 (带错误抖动效果)
            val isError = result.startsWith("语法错误") || result.startsWith("计算错误")
            var shakeOffset by remember { mutableStateOf(0f) }
            
            LaunchedEffect(result) {
                if (isError) {
                    // 触发抖动与触觉反馈
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val anim = androidx.compose.animation.core.Animatable(0f)
                    anim.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 400
                            -15f at 50
                            15f at 150
                            -10f at 250
                            10f at 300
                            0f at 400
                        }
                    ) {
                        shakeOffset = this.value
                    }
                }
            }

            val formattedResult = runCatching {
                val num = result.toDouble()
                if (num % 1.0 == 0.0 && num < 1e12 && num > -1e12) {
                    val format = DecimalFormat("#,###")
                    format.format(num)
                } else {
                    result
                }
            }.getOrElse { result }
            
            val resultBaseFontSize = MaterialTheme.typography.displayMedium.fontSize.value
            val resultMinFontSize = MaterialTheme.typography.headlineMedium.fontSize.value
            val resultCalculatedSize = if (formattedResult.length <= 10) resultBaseFontSize else (resultBaseFontSize - (formattedResult.length - 10) * 2f).coerceAtLeast(resultMinFontSize)
            val resultFontSize = androidx.compose.ui.unit.TextUnit(resultCalculatedSize, androidx.compose.ui.unit.TextUnitType.Sp)
            
            Text(
                text = formattedResult,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = if (isError) MaterialTheme.typography.titleMedium.fontSize else resultFontSize),
                color = if (isError) Color(0xFFFF5252) else if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                fontWeight = FontWeight.Bold,
                maxLines = if (isError) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.roundToInt(), 0) }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { onCopy(result) }
                        )
                    }
            )
            
            val numRes = result.toDoubleOrNull()
            val fraction = if (!isError && numRes != null) doubleToFraction(numRes) else null
            
            // 进制预览 (Base-N Preview) - 仅针对非错误且为整数的结果
            val isInteger = numRes != null && !isError && abs(numRes % 1.0) < 1e-9 && numRes < Long.MAX_VALUE && numRes > Long.MIN_VALUE
            
            androidx.compose.animation.AnimatedVisibility(visible = isInteger || (fraction != null && fraction != formattedResult)) {
                Column(horizontalAlignment = Alignment.End) {
                    if (isInteger && numRes != null) {
                        val longVal = numRes.toLong()
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "HEX: ${longVal.toString(16).uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = 0.5f)
                            )
                            Text(
                                text = "BIN: ${longVal.toString(2)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = (if (isDarkMode) Color(0xFF66FFB2) else ForestGreen).copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 120.dp)
                            )
                        }
                    }
                    if (fraction != null && fraction != formattedResult) {
                        Text(
                            text = "= $fraction",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkMode) CloudWhite.copy(alpha = 0.6f) else ForestDeep.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProKeypad(
    onToken: (String) -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onEqual: () -> Unit,
    onMoveCursor: (Int) -> Unit,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    settings: CalculatorSettings? = null,
    onToggleAngleMode: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 模式切换条与光标控制
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (settings != null && onToggleAngleMode != null) {
                    Surface(
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onToggleAngleMode() },
                        shape = CircleShape,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = settings.angleMode.title,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isDarkMode) CloudWhite else PineInk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // 方向键与功能
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onUndo != null && onRedo != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clickable(enabled = canUndo) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onUndo() }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Outlined.Undo, null, tint = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = if (canUndo) 1f else 0.3f), modifier = Modifier.size(20.dp))
                            }
                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f)))
                            Box(
                                modifier = Modifier
                                    .clickable(enabled = canRedo) { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onRedo() }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Outlined.Redo, null, tint = (if (isDarkMode) CloudWhite else PineInk).copy(alpha = if (canRedo) 1f else 0.3f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                
                if (onDismiss != null) {
                    KeypadIconButton(Icons.Outlined.KeyboardHide, onClick = onDismiss)
                }
                
                // 模拟更专业的光标控制
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onMoveCursor(-1) }.padding(8.dp)
                        ) {
                            Icon(Icons.Outlined.ChevronLeft, null, tint = if (isDarkMode) CloudWhite else PineInk, modifier = Modifier.size(20.dp))
                        }
                        Box(modifier = Modifier.width(1.dp).height(16.dp).background(Color.White.copy(alpha = 0.1f)))
                        Box(
                            modifier = Modifier.clickable { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onMoveCursor(1) }.padding(8.dp)
                        ) {
                            Icon(Icons.Outlined.ChevronRight, null, tint = if (isDarkMode) CloudWhite else PineInk, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // 主键盘 (带侧滑高级功能)
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // 数字区
            Column(modifier = Modifier.weight(3f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeypadButton("7", { onToken("7") }, Modifier.weight(1f), swipeDownText = "sin", onSwipeDown = { onToken("sin(") }, swipeUpText = "asin", onSwipeUp = { onToken("asin(") })
                    KeypadButton("8", { onToken("8") }, Modifier.weight(1f), swipeDownText = "cos", onSwipeDown = { onToken("cos(") }, swipeUpText = "acos", onSwipeUp = { onToken("acos(") })
                    KeypadButton("9", { onToken("9") }, Modifier.weight(1f), swipeDownText = "tan", onSwipeDown = { onToken("tan(") }, swipeUpText = "atan", onSwipeUp = { onToken("atan(") })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeypadButton("4", { onToken("4") }, Modifier.weight(1f), swipeDownText = "ln", onSwipeDown = { onToken("ln(") }, swipeUpText = "e^x", onSwipeUp = { onToken("e^") })
                    KeypadButton("5", { onToken("5") }, Modifier.weight(1f), swipeDownText = "log", onSwipeDown = { onToken("log(") }, swipeUpText = "10^x", onSwipeUp = { onToken("10^") })
                    KeypadButton("6", { onToken("6") }, Modifier.weight(1f), swipeDownText = "√", onSwipeDown = { onToken("√") }, swipeUpText = "x²", onSwipeUp = { onToken("x²") })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeypadButton("1", { onToken("1") }, Modifier.weight(1f), swipeDownText = "π", onSwipeDown = { onToken("π") }, swipeUpText = "x!", onSwipeUp = { onToken("x!") })
                    KeypadButton("2", { onToken("2") }, Modifier.weight(1f), swipeDownText = "e", onSwipeDown = { onToken("e") }, swipeUpText = "x³", onSwipeUp = { onToken("x³") })
                    KeypadButton("3", { onToken("3") }, Modifier.weight(1f), swipeDownText = "^", onSwipeDown = { onToken("^") }, swipeUpText = "Ans", onSwipeUp = { onToken("Ans") })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeypadButton("0", { onToken("0") }, Modifier.weight(1.5f), swipeDownText = "(", onSwipeDown = { onToken("(") }, swipeUpText = ")", onSwipeUp = { onToken(")") })
                    KeypadButton("x", { onToken("x") }, Modifier.weight(0.8f), accent = true, contentColor = if (isDarkMode) Color(0xFFFFB266) else Color(0xFFD35400))
                    KeypadButton(".", { onToken(".") }, Modifier.weight(0.8f), swipeDownText = ",", onSwipeDown = { onToken(",") })
                }
            }
            
            // 操作符区
            Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val dangerColor = if (isDarkMode) Color(0xFFFF4D4D) else Color(0xFFE57373)
                KeypadButton("⌫", onDelete, Modifier.fillMaxWidth(), accent = false, contentColor = dangerColor)
                KeypadButton("C", onClear, Modifier.fillMaxWidth(), accent = false, contentColor = dangerColor)
                KeypadButton("÷", { onToken("÷") }, Modifier.fillMaxWidth(), accent = true)
                KeypadButton("×", { onToken("×") }, Modifier.fillMaxWidth(), accent = true)
            }
            
            // 主执行区
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KeypadButton("-", { onToken("-") }, Modifier.fillMaxWidth(), accent = true)
                KeypadButton("+", { onToken("+") }, Modifier.fillMaxWidth(), accent = true)
                KeypadButton(
                    text = "=", 
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onEqual() }, 
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    accent = true, 
                    fillHeight = true,
                    containerColor = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.2f) else ForestGreen,
                    contentColor = if (isDarkMode) Color(0xFF66FFB2) else CloudWhite
                )
            }
        }
    }
}

@Composable
private fun KeypadIconButton(icon: ImageVector, onClick: () -> Unit) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onClick() },
        shape = CircleShape,
        color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isDarkMode) CloudWhite else PineInk, modifier = Modifier.width(22.dp).height(22.dp))
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    isSmall: Boolean = false,
    fillHeight: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    swipeUpText: String? = null,
    onSwipeUp: (() -> Unit)? = null,
    swipeDownText: String? = null,
    onSwipeDown: (() -> Unit)? = null
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val haptic = LocalHapticFeedback.current
    
    // 微缩放动画与物理反馈
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "KeypadScale"
    )

    val baseColor = when {
        containerColor != null -> containerColor
        accent -> if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.16f) else ForestGreen.copy(alpha = 0.12f)
        else -> if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.44f)
    }
    
    // 按下时的背景色渐变反馈 (世界一流 App 级按压反馈)
    val animatedBgColor by animateColorAsState(
        targetValue = if (isPressed) {
            if (accent || containerColor != null) baseColor.copy(alpha = baseColor.alpha * 1.5f)
            else if (isDarkMode) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f)
        } else baseColor,
        animationSpec = tween(durationMillis = if (isPressed) 50 else 300),
        label = "KeypadColor"
    )
    
    val textColor = when {
        contentColor != null -> contentColor
        accent -> if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
        else -> if (isDarkMode) CloudWhite else PineInk
    }

    val finalModifier = if (fillHeight) modifier.fillMaxHeight() else modifier.height(if (isSmall) 42.dp else 62.dp)
    
    var isSwiping by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    Surface(
        modifier = finalModifier
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale 
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(offset)
                        coroutineScope.launch {
                            interactionSource.emit(press)
                        }
                        try {
                            awaitRelease()
                            coroutineScope.launch {
                                interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                            }
                        } catch (c: androidx.compose.foundation.gestures.GestureCancellationException) {
                            coroutineScope.launch {
                                interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Cancel(press))
                            }
                        }
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isSwiping = true },
                    onDragEnd = { isSwiping = false },
                    onDragCancel = { isSwiping = false },
                    onDrag = { change, dragAmount ->
                        if (!isSwiping) return@detectDragGestures
                        if (dragAmount.y > 20f && swipeDownText != null && onSwipeDown != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipeDown()
                            isSwiping = false
                        } else if (dragAmount.y < -20f && swipeUpText != null && onSwipeUp != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipeUp()
                            isSwiping = false
                        }
                    }
                )
            },
        shape = RoundedCornerShape(if (isSmall) 18.dp else 22.dp),
        color = animatedBgColor,
        shadowElevation = 0.dp, // 彻底关闭所有按键的阴影，避免 Compose 渲染层在透明/彩色背景下产生奇怪的矩形轮廓
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            if (swipeUpText != null) {
                Text(
                    text = swipeUpText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp)
                )
            }
            if (swipeDownText != null) {
                Text(
                    text = swipeDownText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp)
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = text,
                    style = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium,
                    color = textColor,
                    fontWeight = if (accent) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun InlineActionChip(
    text: String,
    onClick: () -> Unit,
) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    LiquidGlassSurface(
        modifier = Modifier.clickable(onClick = onClick),
        cornerRadius = 18.dp,
        tint = Color.White.copy(alpha = 0.46f),
        borderColor = Color.White.copy(alpha = 0.18f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isDarkMode) CloudWhite else PineInk,
        )
    }
}

@Composable
private fun ModuleSelector(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    
    // 胶囊滑动指示器 (Capsule Segmented Control)
    LiquidGlassSurface(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        cornerRadius = 24.dp,
        tint = if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.32f),
        borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f),
        contentPadding = PaddingValues(4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) (if (isDarkMode) Color(0xFF2E8B57) else ForestGreen) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "TabColor"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) CloudWhite else (if (isDarkMode) CloudWhite.copy(alpha = 0.7f) else PineInk.copy(alpha = 0.7f)),
                    animationSpec = tween(200),
                    label = "TabTextColor"
                )
                
                Surface(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isSelected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelect(option)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor,
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsModule(
    settings: CalculatorSettings,
    onChange: (CalculatorSettings) -> Unit,
) {
    CalculatorCard("设置") {
        CalculatorMetricRow(
            "角度" to settings.angleMode.title,
            "格式" to settings.resultFormat.title,
            "位数" to "${settings.displayDigits}",
            "舍入" to settings.roundingRule.title,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SectionLead(title = "全局设置", body = "会同步到计算、函数表、分布、向量、统计等页面。")
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "默认配置",
            subtitle = "修改后会立即同步到各个计算模块。",
        ) {
            ModuleSelector(AngleMode.entries.map { it.title }, settings.angleMode.title) {
                onChange(settings.copy(angleMode = AngleMode.entries.first { mode -> mode.title == it }))
            }
            Spacer(modifier = Modifier.height(10.dp))
            ModuleSelector(ResultFormat.entries.map { it.title }, settings.resultFormat.title) {
                onChange(settings.copy(resultFormat = ResultFormat.entries.first { mode -> mode.title == it }))
            }
            Spacer(modifier = Modifier.height(10.dp))
            ModuleSelector((2..10).map { "${it}位" }, "${settings.displayDigits}位") {
                onChange(settings.copy(displayDigits = it.removeSuffix("位").toInt()))
            }
            Spacer(modifier = Modifier.height(10.dp))
            ModuleSelector(RoundingRule.entries.map { it.title }, settings.roundingRule.title) {
                onChange(settings.copy(roundingRule = RoundingRule.entries.first { mode -> mode.title == it }))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock("默认角度单位：${settings.angleMode.title}\n默认结果格式：${settings.resultFormat.title}\n显示位数：${settings.displayDigits}\n舍入规则：${settings.roundingRule.title}\n以上设置会联动到计算、函数表、分布、向量、统计等页面。")
    }
}

@Composable
private fun FormatModule(
    settings: CalculatorSettings,
    onChange: (CalculatorSettings) -> Unit,
) {
    var value by remember { mutableStateOf("12345.6789") }
    var result by remember { mutableStateOf("可切换标准、定点、科学、工程格式") }
    CalculatorCard("结果格式") {
        CalculatorMetricRow(
            "当前格式" to settings.resultFormat.title,
            "显示位数" to "${settings.displayDigits}位",
            "舍入规则" to settings.roundingRule.title,
            "示例" to value,
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "格式策略",
            subtitle = "在统一格式、位数和舍入规则之间快速切换。",
        ) {
            ModuleSelector(ResultFormat.entries.map { it.title }, settings.resultFormat.title) {
                onChange(settings.copy(resultFormat = ResultFormat.entries.first { mode -> mode.title == it }))
            }
            Spacer(modifier = Modifier.height(10.dp))
            ModuleSelector((2..10).map { "${it}位" }, "${settings.displayDigits}位") {
                onChange(settings.copy(displayDigits = it.removeSuffix("位").toInt()))
            }
            Spacer(modifier = Modifier.height(10.dp))
            ModuleSelector(RoundingRule.entries.map { it.title }, settings.roundingRule.title) {
                onChange(settings.copy(roundingRule = RoundingRule.entries.first { mode -> mode.title == it }))
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("输入数值") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("格式化") {
            result = runCatching {
                val v = value.toDouble()
                formatBySetting(v, settings)
            }.getOrElse { it.message ?: "格式化失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun UnitConversionModule() {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator_prefs", 0) }
    var keyword by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("长度") }
    var value by remember { mutableStateOf("1") }
    var result by remember { mutableStateOf("在常用单位间快速换算") }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "unit_pinned")) } }
    val recent = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "unit_recent")) } }
    val catalog = mapOf(
        "长度" to listOf("m", "cm", "mm", "km", "in", "ft", "yd", "mi", "nm"),
        "质量" to listOf("kg", "g", "mg", "t", "lb", "oz"),
        "温度" to listOf("℃", "℉", "K"),
        "面积" to listOf("m²", "cm²", "ha", "acre", "km²"),
        "体积" to listOf("L", "mL", "m³", "gal", "ft³"),
        "速度" to listOf("m/s", "km/h", "mph", "kn"),
        "能量" to listOf("J", "kJ", "cal", "kWh"),
    )
    CalculatorCard("单位换算") {
        CalculatorMetricRow(
            "分类" to mode,
            "置顶" to "${favorites.size}项",
            "最近" to "${recent.size}项",
            "当前值" to value,
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "换算工作区",
            subtitle = "先搜索条目，再选择分类并输入当前值。",
        ) {
            OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("搜索条目") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ModuleSelector(catalog.keys.toList(), mode) { mode = it }
        }
        if (favorites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CalculatorInlineSectionHeader("置顶")
            Spacer(modifier = Modifier.height(6.dp))
            favorites.forEachIndexed { index, item ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    tint = Color.White.copy(alpha = 0.46f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite else PineInk)
                }
                if (index != favorites.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
        if (recent.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CalculatorInlineSectionHeader("最近使用")
            Spacer(modifier = Modifier.height(6.dp))
            recent.forEachIndexed { index, item ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                    tint = Color.White.copy(alpha = 0.42f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite else PineInk)
                }
                if (index != recent.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("输入值") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("换算") {
            result = runCatching {
                val v = value.toDouble()
                when (mode) {
                    "长度" -> "m = ${formatNumber(v)}\ncm = ${formatNumber(v * 100)}\nmm = ${formatNumber(v * 1000)}\nkm = ${formatNumber(v / 1000)}\nin = ${formatNumber(v * 39.3700787)}\nft = ${formatNumber(v * 3.2808399)}\nyd = ${formatNumber(v * 1.0936133)}\nmi = ${formatNumber(v / 1609.344)}\nnm = ${formatNumber(v / 1852.0)}"
                    "质量" -> "kg = ${formatNumber(v)}\ng = ${formatNumber(v * 1000)}\nmg = ${formatNumber(v * 1_000_000)}\nt = ${formatNumber(v / 1000)}\nlb = ${formatNumber(v * 2.20462262)}\noz = ${formatNumber(v * 35.2739619)}"
                    "面积" -> "m² = ${formatNumber(v)}\ncm² = ${formatNumber(v * 10000)}\nha = ${formatNumber(v / 10000)}\nacre = ${formatNumber(v / 4046.85642)}\nkm² = ${formatNumber(v / 1_000_000)}"
                    "体积" -> "L = ${formatNumber(v)}\nmL = ${formatNumber(v * 1000)}\nm³ = ${formatNumber(v / 1000)}\ngal = ${formatNumber(v * 0.264172052)}\nft³ = ${formatNumber(v * 0.0353146667)}"
                    "速度" -> "m/s = ${formatNumber(v)}\nkm/h = ${formatNumber(v * 3.6)}\nmph = ${formatNumber(v * 2.23693629)}\nkn = ${formatNumber(v * 1.94384449)}"
                    "能量" -> "J = ${formatNumber(v)}\nkJ = ${formatNumber(v / 1000)}\ncal = ${formatNumber(v / 4.184)}\nkWh = ${formatNumber(v / 3_600_000)}"
                    else -> "℃ = ${formatNumber(v)}\n℉ = ${formatNumber(v * 9 / 5 + 32)}\nK = ${formatNumber(v + 273.15)}"
                }
            }.also {
                val stamp = "$mode · $value"
                recent.remove(stamp)
                recent.add(0, stamp)
                if (recent.size > 5) recent.removeAt(recent.lastIndex)
                savePreferenceList(prefs, "unit_recent", recent)
            }.getOrElse { it.message ?: "换算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
        Spacer(modifier = Modifier.height(12.dp))
        val visible = catalog[mode].orEmpty().filter { keyword.isBlank() || it.contains(keyword, ignoreCase = true) }
        visible.forEachIndexed { index, unit ->
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (favorites.contains(unit)) {
                        favorites.remove(unit)
                    } else {
                        favorites.add(unit)
                    }
                    savePreferenceList(prefs, "unit_pinned", favorites)
                },
                cornerRadius = 16.dp,
                tint = Color.White.copy(alpha = 0.48f),
                borderColor = Color.White.copy(alpha = 0.16f),
            ) {
                Text(
                    text = if (favorites.contains(unit)) "$unit  · 已收藏" else unit,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) CloudWhite else PineInk,
                )
            }
            if (index != visible.lastIndex) Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ConstantsModule() {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator_prefs", 0) }
    var keyword by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("数学") }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "constant_pinned")) } }
    val recent = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "constant_recent")) } }
    CalculatorCard("常数表") {
        val constants = mapOf(
            "数学" to listOf(
                "π = 3.141592653589793",
                "e = 2.718281828459045",
            ),
            "物理" to listOf(
                "c = 299792458 m/s",
                "g = 9.80665 m/s²",
                "G = 6.67430e-11 N·m²/kg²",
                "h = 6.62607015e-34 J·s",
                "k = 1.380649e-23 J/K",
                "ε0 = 8.8541878128e-12 F/m",
                "μ0 = 1.25663706212e-6 N/A²",
                "σ = 5.670374419e-8 W/(m²·K⁴)",
            ),
            "化学" to listOf(
                "NA = 6.02214076e23",
                "R = 8.314462618 J/(mol·K)",
                "F = 96485.33212 C/mol",
                "u = 1.66053906660e-27 kg",
            ),
            "微观粒子" to listOf(
                "me = 9.1093837015e-31 kg",
                "mp = 1.67262192369e-27 kg",
                "mn = 1.67492749804e-27 kg",
                "a0 = 5.29177210903e-11 m",
            ),
        )
        CalculatorMetricRow(
            "分组" to group,
            "置顶" to "${favorites.size}项",
            "最近" to "${recent.size}项",
            "结果" to "${constants[group].orEmpty().size}项",
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "检索常数",
            subtitle = "先切换分组，再按关键词过滤当前常数列表。",
        ) {
            ModuleSelector(constants.keys.toList(), group) { group = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("搜索常数") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (favorites.isNotEmpty()) {
            CalculatorInlineSectionHeader("置顶")
            Spacer(modifier = Modifier.height(6.dp))
            favorites.forEachIndexed { index, item ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    tint = Color.White.copy(alpha = 0.44f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite else PineInk)
                }
                if (index != favorites.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (recent.isNotEmpty()) {
            CalculatorInlineSectionHeader("最近查看")
            Spacer(modifier = Modifier.height(6.dp))
            recent.forEachIndexed { index, item ->
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    tint = Color.White.copy(alpha = 0.40f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                ) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium, color = if (isDarkMode) CloudWhite else PineInk)
                }
                if (index != recent.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        val visible = constants[group].orEmpty().filter { keyword.isBlank() || it.contains(keyword, ignoreCase = true) }
        visible.forEachIndexed { index, item ->
            LiquidGlassSurface(
                modifier = Modifier.fillMaxWidth().clickable {
                    recent.remove(item)
                    recent.add(0, item)
                    if (recent.size > 5) recent.removeAt(recent.lastIndex)
                    savePreferenceList(prefs, "constant_recent", recent)
                    if (favorites.contains(item)) {
                        favorites.remove(item)
                    } else {
                        favorites.add(item)
                    }
                    savePreferenceList(prefs, "constant_pinned", favorites)
                },
                cornerRadius = 18.dp,
                tint = Color.White.copy(alpha = 0.5f),
                borderColor = Color.White.copy(alpha = 0.16f),
            ) {
                Text(
                    text = if (favorites.contains(item)) "$item  · 已收藏" else item,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) CloudWhite else PineInk,
                )
            }
            if (index != visible.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HypothesisTestModule(settings: CalculatorSettings) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var category by remember { mutableStateOf("拟合优度") }
    var mode by remember { mutableStateOf("χ²检验") }
    var raw by remember { mutableStateOf("12,18,20,15") }
    var rawY by remember { mutableStateOf("16,16,16,17") }
    var parameter by remember { mutableStateOf("15") }
    var result by remember { mutableStateOf("检验结果将在这里显示") }
    var verdict by remember { mutableStateOf("待检验") }
    var advice by remember { mutableStateOf("输入数据后开始检验。") }
    CalculatorCard("检验") {
        ModuleSelector(listOf("拟合优度", "分布检验", "参数检验"), category) {
            category = it
            mode = when (it) {
                "拟合优度" -> "χ²检验"
                "分布检验" -> "KS检验"
                else -> "单样本t检验"
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusTag(verdict)
            StatusTag(advice, subtle = true)
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(
            when (category) {
                "拟合优度" -> "结果说明：用于比较观测频数与期望频数是否一致。\n公式：χ² = Σ((O - E)² / E)"
                "分布检验" -> "结果说明：用于比较样本经验分布与目标分布之间的最大偏差。\n公式：D = sup|F_n(x) - F(x)|"
                else -> "结果说明：用于检验均值差异或样本均值与给定总体均值是否显著。\n公式：t = (样本均值差) / 标准误"
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        val modes = when (category) {
            "拟合优度" -> listOf("χ²检验")
            "分布检验" -> listOf("KS检验")
            else -> listOf("单样本t检验", "双样本t检验")
        }
        ModuleSelector(modes, mode) { mode = it }
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = raw, 
            onValueChange = { raw = it }, 
            label = { Text("样本 / 观测频数") }, 
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
                cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
            )
        )
        if (mode == "χ²检验" || mode == "双样本t检验") {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = rawY, 
                onValueChange = { rawY = it }, 
                label = { Text(if (mode == "χ²检验") "期望频数" else "第二样本") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
                    cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
                )
            )
        }
        if (mode == "单样本t检验") {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = parameter, 
                onValueChange = { parameter = it }, 
                label = { Text("检验均值 μ0") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                    unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
                    cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("开始检验") {
            result = runCatching {
                val values = parseNumberList(raw)
                when (mode) {
                    "χ²检验" -> {
                        val expected = parseNumberList(rawY)
                        require(values.size == expected.size) { "观测与期望长度必须一致" }
                        val statistic = values.indices.sumOf { (values[it] - expected[it]).pow(2) / expected[it] }
                        val df = max(1.0, (values.size - 1).toDouble())
                        val p = 1.0 - chiSquareCdf(statistic, df)
                        verdict = if (p < 0.05) "显著" else "不显著"
                        advice = if (p < 0.05) "观测频数与期望频数差异较大。" else "当前样本与期望频数基本一致。"
                        "χ² = ${formatBySetting(statistic, settings)}\ndf = ${formatNumber(df)}\np≈${formatBySetting(p, settings)}"
                    }
                    "KS检验" -> {
                        val test = kolmogorovSmirnovNormalTest(values)
                        verdict = if (test.second < 0.05) "显著" else "不显著"
                        advice = if (test.second < 0.05) "样本分布与目标分布存在偏离。" else "样本分布未见显著偏离。"
                        "D = ${formatBySetting(test.first, settings)}\np≈${formatBySetting(test.second, settings)}"
                    }
                    "单样本t检验" -> {
                        val mu0 = parameter.toDouble()
                        val test = oneSampleTTest(values, mu0)
                        verdict = if (test.third < 0.05) "显著" else "不显著"
                        advice = if (test.third < 0.05) "样本均值与给定 μ0 有显著差异。" else "样本均值与给定 μ0 差异不显著。"
                        "t = ${formatBySetting(test.first, settings)}\ndf = ${formatNumber(test.second)}\np≈${formatBySetting(test.third, settings)}"
                    }
                    else -> {
                        val other = parseNumberList(rawY)
                        val test = twoSampleTTest(values, other)
                        verdict = if (test.third < 0.05) "显著" else "不显著"
                        advice = if (test.third < 0.05) "两组样本均值存在显著差异。" else "两组样本均值差异不显著。"
                        "t = ${formatBySetting(test.first, settings)}\ndf = ${formatBySetting(test.second, settings)}\np≈${formatBySetting(test.third, settings)}"
                    }
                }
            }.getOrElse { it.message ?: "检验失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun DistributionModule(settings: CalculatorSettings) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var mode by remember { mutableStateOf("正态分布") }
    var a by remember { mutableStateOf("0") }
    var b by remember { mutableStateOf("1") }
    var c by remember { mutableStateOf("1") }
    var d by remember { mutableStateOf("2") }
    var result by remember { mutableStateOf("选择模型并计算") }
    val distributionSummary = when (mode) {
        "正态密度" -> "查看指定点的正态分布概率密度。"
        "正态分布" -> "查看正态分布在指定位置的累计概率。"
        "区间正态" -> "直接计算正态分布在区间内的概率。"
        "二项分布" -> "适合有限次独立试验的精确概率。"
        "二项累积" -> "适合查看 X≤k 的累计概率。"
        "泊松分布" -> "适合低频随机事件的次数概率。"
        "几何分布" -> "适合直到首次成功前的失败次数。"
        else -> "适合总体有限、无放回抽样的概率。"
    }
    CalculatorCard("分布") {
        CalculatorMetricRow(
            "模型" to mode,
            "输入1" to a,
            "输入2" to b,
            "输入3" to c,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SectionLead(title = "分布工作区", body = distributionSummary)
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "参数设置",
            subtitle = "按当前模型填写参数，支持正态、离散和抽样场景。",
        ) {
            ModuleSelector(listOf("正态密度", "正态分布", "区间正态", "二项分布", "二项累积", "泊松分布", "几何分布", "超几何分布"), mode) { mode = it }
            Spacer(modifier = Modifier.height(14.dp))
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
                cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
            )
            
            if (mode == "正态密度" || mode == "正态分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("x") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 μ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("标准差 σ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            } else if (mode == "区间正态") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("下界 a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("上界 b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 μ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("标准差 σ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            } else if (mode == "二项分布" || mode == "二项累积") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("试验次数 n") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("成功概率 p") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            } else if (mode == "泊松分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("事件次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 λ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            } else if (mode == "几何分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("成功前失败次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功概率 p") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            } else {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("总体大小 N") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功总体数 K") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("抽样数 n") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("成功样本数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("计算") {
            result = runCatching {
                if (mode == "正态密度" || mode == "正态分布") {
                    val x = a.toDouble()
                    val mean = b.toDouble()
                    val sd = c.toDouble()
                    val pdf = normalPdf(x, mean, sd)
                    val cdf = normalCdf(x, mean, sd)
                    if (mode == "正态密度") "概率密度 ${formatBySetting(pdf, settings)}" else "累计概率 ${formatBySetting(cdf, settings)}"
                } else if (mode == "区间正态") {
                    val lower = a.toDouble()
                    val upper = d.toDouble()
                    val mean = b.toDouble()
                    val sd = c.toDouble()
                    require(upper >= lower) { "上界需大于等于下界" }
                    val value = normalCdf(upper, mean, sd) - normalCdf(lower, mean, sd)
                    "P(${formatBySetting(lower, settings)}≤X≤${formatBySetting(upper, settings)}) = ${formatBySetting(value, settings)}"
                } else if (mode == "二项分布" || mode == "二项累积") {
                    val n = a.toInt()
                    val k = b.toInt()
                    val p = c.toDouble()
                    if (mode == "二项分布") {
                        "P(X=k) = ${formatBySetting(binomialProbability(n, k, p), settings)}"
                    } else {
                        val total = (0..k).sumOf { binomialProbability(n, it, p) }
                        "P(X≤k) = ${formatBySetting(total, settings)}"
                    }
                } else if (mode == "泊松分布") {
                    val k = a.toInt()
                    val lambda = b.toDouble()
                    val value = poissonProbability(k, lambda)
                    "P(X=k) = ${formatBySetting(value, settings)}"
                } else if (mode == "几何分布") {
                    val k = a.toInt()
                    val p = b.toDouble()
                    "P(X=k) = ${formatBySetting((1 - p).pow(k) * p, settings)}"
                } else {
                    val n = a.toInt()
                    val successPopulation = b.toInt()
                    val sampleSize = c.toInt()
                    val successSample = d.toInt()
                    val value = combination(successPopulation, successSample) *
                        combination(n - successPopulation, sampleSize - successSample) /
                        combination(n, sampleSize)
                    "P(X=k) = ${formatBySetting(value, settings)}"
                }
            }.getOrElse { it.message ?: "计算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun SpreadsheetModule() {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    val rows = remember { mutableStateListOf("12", "18", "21", "16", "25") }
    var result by remember { mutableStateOf("可录入 5 行数据并快速汇总") }
    CalculatorCard("数据表格") {
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
            cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
        )
        rows.forEachIndexed { index, value ->
            OutlinedTextField(value = value, onValueChange = { rows[index] = it }, label = { Text("第 ${index + 1} 行") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
            Spacer(modifier = Modifier.height(10.dp))
        }
        CalcButton("汇总") {
            result = runCatching {
                val values = rows.map { it.toDouble() }
                "合计 ${formatNumber(values.sum())}\n平均 ${formatNumber(values.average())}\n最大 ${formatNumber(values.maxOrNull() ?: 0.0)}\n最小 ${formatNumber(values.minOrNull() ?: 0.0)}"
            }.getOrElse { it.message ?: "表格计算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun FunctionTableModule(settings: CalculatorSettings) {
    val isDarkMode = LocalLiquidGlassStylePreset.current == LiquidGlassStylePreset.Hyper
    var expression by remember { mutableStateOf("x^2+2*x+1") }
    var expressionG by remember { mutableStateOf("sin(x)") }
    var dual by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf("-2") }
    var end by remember { mutableStateOf("2") }
    var step by remember { mutableStateOf("0.5") }
    var result by remember { mutableStateOf("将生成 x 与 y 的对应表") }
    CalculatorCard("函数表格") {
        ModuleSelector(listOf("单函数", "双函数"), if (dual) "双函数" else "单函数") { dual = it == "双函数" }
        Spacer(modifier = Modifier.height(12.dp))
        FormulaEditor(
            label = "f(x)",
            value = expression,
            onValueChange = { expression = it },
            tokens = listOf("x", "(", ")", "^", "sin(x)", "cos(x)", "tan(x)", "sqrt(", "ln(x)"),
        )
        if (dual) {
            Spacer(modifier = Modifier.height(10.dp))
            FormulaEditor(
                label = "g(x)",
                value = expressionG,
                onValueChange = { expressionG = it },
                tokens = listOf("x", "(", ")", "^", "sin(x)", "cos(x)", "tan(x)", "sqrt(", "ln(x)"),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            focusedLabelColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen,
            unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.2f) else PineInk.copy(alpha = 0.3f),
            cursorColor = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen
        )
        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("起点") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("终点") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = step, onValueChange = { step = it }, label = { Text("步长") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = textFieldColors)
        Spacer(modifier = Modifier.height(16.dp))
        CalcButton("生成") {
            result = runCatching {
                var x = start.toDouble()
                val target = end.toDouble()
                val delta = step.toDouble()
                require(delta > 0) { "步长需大于 0" }
                buildString {
                    while (x <= target + 1e-9) {
                        val fx = ExpressionEngine.evaluateQuantity(expression = expression, xValue = x, angleMode = settings.angleMode)
                        append("x=${formatBySetting(x, settings)}  f=${fx.toString(settings)}")
                        if (dual) {
                            val gx = ExpressionEngine.evaluateQuantity(expression = expressionG, xValue = x, angleMode = settings.angleMode)
                            append("  g=${gx.toString(settings)}")
                        }
                        append("\n")
                        x += delta
                    }
                }.trim()
            }.getOrElse { it.message ?: "生成失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}
private fun parseNumberList(raw: String): List<Double> {
    val values = raw.split(",", "，", " ", "\n").mapNotNull { it.trim().takeIf(String::isNotBlank)?.toDoubleOrNull() }
    require(values.isNotEmpty()) { "请输入有效数据" }
    return values
}

private fun normalPdf(x: Double, mean: Double, sd: Double): Double {
    require(sd > 0) { "标准差需大于 0" }
    return 1.0 / (sd * sqrt(2 * PI)) * exp(-((x - mean).pow(2)) / (2 * sd * sd))
}

private fun normalCdf(x: Double, mean: Double, sd: Double): Double {
    val z = (x - mean) / (sd * sqrt(2.0))
    return 0.5 * (1 + erf(z))
}

private fun erf(x: Double): Double {
    val sign = if (x >= 0) 1 else -1
    val ax = abs(x)
    val t = 1.0 / (1.0 + 0.3275911 * ax)
    val y = 1.0 - (((((1.061405429 * t - 1.453152027) * t + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * exp(-ax * ax))
    return sign * y
}

private fun binomialProbability(n: Int, k: Int, p: Double): Double {
    require(n >= 0 && k in 0..n) { "参数范围无效" }
    require(p in 0.0..1.0) { "概率需在 0 到 1 之间" }
    return combination(n, k) * p.pow(k) * (1 - p).pow(n - k)
}

private fun combination(n: Int, k: Int): Double {
    val m = min(k, n - k)
    var result = 1.0
    for (i in 1..m) {
        result = result * (n - m + i) / i
    }
    return result
}

private fun factorialInt(n: Int): Double {
    require(n >= 0) { "参数不能为负数" }
    var result = 1.0
    for (i in 2..n) result *= i
    return result
}

private fun poissonProbability(k: Int, lambda: Double): Double {
    require(k >= 0) { "事件次数不能为负数" }
    require(lambda > 0) { "均值 λ 需大于 0" }
    return exp(-lambda) * lambda.pow(k) / factorialInt(k)
}

private fun quadraticRegression(x: List<Double>, y: List<Double>): List<Double> {
    require(x.size == y.size && x.isNotEmpty()) { "请输入成对数据" }
    val n = x.size.toDouble()
    val sx = x.sum()
    val sx2 = x.sumOf { it * it }
    val sx3 = x.sumOf { it * it * it }
    val sx4 = x.sumOf { it * it * it * it }
    val sy = y.sum()
    val sxy = x.indices.sumOf { x[it] * y[it] }
    val sx2y = x.indices.sumOf { x[it] * x[it] * y[it] }
    return solveLinear3(
        matrix = listOf(
            listOf(sx4, sx3, sx2),
            listOf(sx3, sx2, sx),
            listOf(sx2, sx, n),
        ),
        rhs = listOf(sx2y, sxy, sy),
    )
}

private fun solveQuadraticInequality(a: Double, b: Double, c: Double, sign: String): String {
    if (abs(a) < 1e-9) {
        val x = -c / b
        return when (sign) {
            "≥ 0", "> 0" -> if (b > 0) "x ${if (sign == "≥ 0") "≥" else ">"} ${formatNumber(x)}" else "x ${if (sign == "≥ 0") "≤" else "<"} ${formatNumber(x)}"
            else -> if (b > 0) "x ${if (sign == "≤ 0") "≤" else "<"} ${formatNumber(x)}" else "x ${if (sign == "≤ 0") "≥" else ">"} ${formatNumber(x)}"
        }
    }
    val delta = b * b - 4 * a * c
    if (delta < 0) {
        return if ((a > 0 && (sign == "≥ 0" || sign == "> 0")) || (a < 0 && (sign == "≤ 0" || sign == "< 0"))) "任意实数" else "无解"
    }
    val root = sqrt(max(delta, 0.0))
    val x1 = min((-b - root) / (2 * a), (-b + root) / (2 * a))
    val x2 = max((-b - root) / (2 * a), (-b + root) / (2 * a))
    val outer = if (sign == "≥ 0" || sign == "> 0") a > 0 else a < 0
    val equal = sign == "≥ 0" || sign == "≤ 0"
    return if (outer) {
        "x ${if (equal) "≤" else "<"} ${formatNumber(x1)} 或 x ${if (equal) "≥" else ">"} ${formatNumber(x2)}"
    } else {
        "${formatNumber(x1)} ${if (equal) "≤" else "<"} x ${if (equal) "≤" else "<"} ${formatNumber(x2)}"
    }
}

private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "无定义"
    val rounded = round(value * 1_000_000_000.0) / 1_000_000_000.0
    return if (abs(rounded - rounded.toLong()) < 1e-9) rounded.toLong().toString() else rounded.toString()
}

private fun engineeringFormat(value: Double): String {
    if (value == 0.0) return "0"
    val exponent = kotlin.math.floor(log10(abs(value)) / 3).toInt() * 3
    val mantissa = value / 10.0.pow(exponent)
    return "${formatNumber(mantissa)}E$exponent"
}

private fun formatBySetting(value: Double, settings: CalculatorSettings): String {
    return when (settings.resultFormat) {
        ResultFormat.Standard -> roundDecimal(value, settings.displayDigits, settings.roundingRule.mode).stripTrailingZeros().toPlainString()
        ResultFormat.Fixed2 -> roundDecimal(value, 2, settings.roundingRule.mode).toPlainString()
        ResultFormat.Scientific -> scientificString(value, settings.displayDigits, settings.roundingRule.mode)
        ResultFormat.Engineering -> engineeringString(value, settings.displayDigits, settings.roundingRule.mode)
        ResultFormat.DMS -> dmsString(value)
    }
}

private fun formatBySetting(value: Double, format: ResultFormat): String {
    return when (format) {
        ResultFormat.Standard -> formatNumber(value)
        ResultFormat.Fixed2 -> String.format("%.2f", value)
        ResultFormat.Scientific -> String.format("%.6e", value)
        ResultFormat.Engineering -> engineeringFormat(value)
        ResultFormat.DMS -> dmsString(value)
    }
}

private fun dmsString(value: Double): String {
    val absValue = abs(value)
    val d = kotlin.math.floor(absValue)
    val rem = (absValue - d) * 60
    val m = kotlin.math.floor(rem)
    val s = (rem - m) * 60
    val sign = if (value < 0) "-" else ""
    return String.format("%s%.0f°%.0f'%.2f\"", sign, d, m, s)
}

private fun roundDecimal(value: Double, scale: Int, mode: RoundingMode): BigDecimal {
    return BigDecimal.valueOf(value).setScale(scale, mode)
}

private fun scientificString(value: Double, digits: Int, mode: RoundingMode): String {
    if (value == 0.0) return "0"
    val pattern = buildString {
        append("0.")
        repeat(max(0, digits - 1)) { append("0") }
        append("E0")
    }
    return DecimalFormat(pattern).apply { roundingMode = mode }.format(value)
}

private fun engineeringString(value: Double, digits: Int, mode: RoundingMode): String {
    if (value == 0.0) return "0"
    val exponent = kotlin.math.floor(log10(abs(value)) / 3).toInt() * 3
    val mantissa = value / 10.0.pow(exponent)
    return "${roundDecimal(mantissa, max(0, digits - 1), mode).stripTrailingZeros().toPlainString()}E$exponent"
}

private fun meanConfidenceInterval(values: List<Double>): Pair<Double, Double> {
    require(values.size >= 2) { "至少需要两项数据" }
    val mean = values.average()
    val sampleVariance = values.sumOf { (it - mean).pow(2) } / (values.size - 1)
    val se = sqrt(sampleVariance / values.size)
    val t = inverseStudentT(0.975, (values.size - 1).toDouble())
    return mean - t * se to mean + t * se
}

private fun jarqueBeraTest(values: List<Double>): Pair<Double, Double> {
    require(values.size >= 3) { "至少需要三项数据" }
    val mean = values.average()
    val centered = values.map { it - mean }
    val m2 = centered.averageOf { it.pow(2) }
    val m3 = centered.averageOf { it.pow(3) }
    val m4 = centered.averageOf { it.pow(4) }
    if (abs(m2) < 1e-12) return 0.0 to 1.0
    val skewness = m3 / m2.pow(1.5)
    val kurtosis = m4 / m2.pow(2)
    val jb = values.size / 6.0 * (skewness.pow(2) + (kurtosis - 3.0).pow(2) / 4.0)
    val p = exp(-jb / 2.0)
    return jb to p
}

private fun List<Double>.averageOf(selector: (Double) -> Double): Double = this.map(selector).average()

private fun studentTCdf(t: Double, df: Double): Double {
    if (df <= 0) return Double.NaN
    val x = df / (df + t * t)
    val ib = regularizedIncompleteBeta(x, df / 2.0, 0.5)
    return if (t >= 0) 1.0 - 0.5 * ib else 0.5 * ib
}

private fun fCdf(value: Double, d1: Double, d2: Double): Double {
    if (value <= 0) return 0.0
    val x = d1 * value / (d1 * value + d2)
    return regularizedIncompleteBeta(x, d1 / 2.0, d2 / 2.0)
}

private fun inverseStudentT(probability: Double, df: Double): Double {
    var low = 0.0
    var high = 20.0
    repeat(80) {
        val mid = (low + high) / 2.0
        if (studentTCdf(mid, df) < probability) low = mid else high = mid
    }
    return (low + high) / 2.0
}

private fun regularizedIncompleteBeta(x: Double, a: Double, b: Double): Double {
    require(x in 0.0..1.0) { "x 需在 0 到 1 之间" }
    if (x == 0.0 || x == 1.0) return x
    val bt = exp(logGamma(a + b) - logGamma(a) - logGamma(b) + a * ln(x) + b * ln(1 - x))
    return if (x < (a + 1) / (a + b + 2)) {
        bt * betaContinuedFraction(x, a, b) / a
    } else {
        1 - bt * betaContinuedFraction(1 - x, b, a) / b
    }
}

private fun betaContinuedFraction(x: Double, a: Double, b: Double): Double {
    val maxIterations = 200
    val epsilon = 3e-7
    val fpMin = 1e-30
    var qab = a + b
    var qap = a + 1
    var qam = a - 1
    var c = 1.0
    var d = 1.0 - qab * x / qap
    if (abs(d) < fpMin) d = fpMin
    d = 1.0 / d
    var h = d
    for (m in 1..maxIterations) {
        val m2 = 2 * m
        var aa = m * (b - m) * x / ((qam + m2) * (a + m2))
        d = 1.0 + aa * d
        if (abs(d) < fpMin) d = fpMin
        c = 1.0 + aa / c
        if (abs(c) < fpMin) c = fpMin
        d = 1.0 / d
        h *= d * c
        aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2))
        d = 1.0 + aa * d
        if (abs(d) < fpMin) d = fpMin
        c = 1.0 + aa / c
        if (abs(c) < fpMin) c = fpMin
        d = 1.0 / d
        val delta = d * c
        h *= delta
        if (abs(delta - 1.0) < epsilon) break
    }
    return h
}

private fun logGamma(value: Double): Double {
    val coefficients = doubleArrayOf(
        76.18009172947146,
        -86.50532032941677,
        24.01409824083091,
        -1.231739572450155,
        0.001208650973866179,
        -0.000005395239384953,
    )
    var x = value
    var y = value
    var tmp = x + 5.5
    tmp -= (x + 0.5) * ln(tmp)
    var series = 1.000000000190015
    coefficients.forEach {
        y += 1.0
        series += it / y
    }
    return -tmp + ln(2.5066282746310005 * series / x)
}

private fun regressionSummary(
    equation: String,
    xValues: List<Double>,
    actual: List<Double>,
    predicted: List<Double>,
    forecast: Double,
    forecastX: Double,
    settings: CalculatorSettings,
    extra: String? = null,
    coefficients: List<Pair<String, Double>> = emptyList(),
    coefficientStats: List<String> = emptyList(),
    predictorCount: Int = 1,
): String {
    val parameterCount = predictorCount + 1
    val residuals = actual.indices.map { actual[it] - predicted[it] }
    val residualPreview = residuals.take(3).joinToString(", ") { formatBySetting(it, settings) }
    val mean = actual.average()
    val ssRes = residuals.sumOf { it * it }
    val ssTot = actual.sumOf { (it - mean) * (it - mean) }
    val r2 = if (abs(ssTot) < 1e-9) 1.0 else 1.0 - ssRes / ssTot
    val adjustedR2 = if (actual.size - parameterCount - 1 <= 0 || abs(1 - r2) < 1e-9) r2 else 1 - (1 - r2) * (actual.size - 1) / (actual.size - parameterCount - 1)
    val rmse = sqrt(ssRes / actual.size)
    val residualDf = max(1.0, (actual.size - parameterCount).toDouble())
    val residualStd = sqrt(ssRes / residualDf)
    val ciHalfWidth = inverseStudentT(0.975, residualDf) * residualStd
    val fStatistic = if (actual.size - predictorCount - 1 <= 0 || abs(1 - r2) < 1e-9) Double.POSITIVE_INFINITY else (r2 / predictorCount) / ((1 - r2) / (actual.size - predictorCount - 1))
    val pValue = if (fStatistic.isInfinite()) 0.0 else 1.0 - fCdf(fStatistic, predictorCount.toDouble(), residualDf)
    val significance = when {
        fStatistic.isInfinite() -> "显著"
        fStatistic > 10 -> "显著"
        fStatistic > 4 -> "较显著"
        else -> "样本不足或不显著"
    }
    val residualTable = xValues.indices.take(4).joinToString("\n") { index ->
        "x=${formatBySetting(xValues[index], settings)}  y=${formatBySetting(actual[index], settings)}  ŷ=${formatBySetting(predicted[index], settings)}  e=${formatBySetting(residuals[index], settings)}"
    }
    val coefficientTable = coefficients.joinToString("\n") { (name, value) ->
        "$name = ${formatBySetting(value, settings)}"
    }
    val ssReg = max(0.0, ssTot - ssRes)
    val dfReg = predictorCount.toDouble()
    val dfRes = residualDf
    val msReg = ssReg / dfReg
    val msRes = ssRes / dfRes
    val anovaTable = "回归项: SS=${formatBySetting(ssReg, settings)}  df=${formatNumber(dfReg)}  MS=${formatBySetting(msReg, settings)}\n残差项: SS=${formatBySetting(ssRes, settings)}  df=${formatNumber(dfRes)}  MS=${formatBySetting(msRes, settings)}\n总计: SS=${formatBySetting(ssTot, settings)}  df=${formatNumber((actual.size - 1).toDouble())}"
    return buildString {
        appendLine(equation)
        if (!extra.isNullOrBlank()) appendLine(extra)
        appendLine("r² = ${formatBySetting(r2, settings)}")
        appendLine("调整后 r² = ${formatBySetting(adjustedR2, settings)}")
        appendLine("RMSE = ${formatBySetting(rmse, settings)}")
        appendLine("残差标准误 = ${formatBySetting(residualStd, settings)}")
        appendLine("显著性检验 F = ${formatBySetting(fStatistic, settings)} · p≈${formatBySetting(pValue, settings)} · $significance")
        appendLine("残差平方和 = ${formatBySetting(ssRes, settings)}")
        appendLine("95% 置信带 ≈ [${formatBySetting(forecast - ciHalfWidth, settings)}, ${formatBySetting(forecast + ciHalfWidth, settings)}]")
        appendLine("ANOVA 表：")
        appendLine(anovaTable)
        if (coefficientTable.isNotBlank()) {
            appendLine("回归系数表：")
            appendLine(coefficientTable)
        }
        if (coefficientStats.isNotEmpty()) {
            appendLine("系数标准误与 t 值：")
            coefficientStats.forEach { appendLine(it) }
        }
        appendLine("残差预览 = $residualPreview")
        appendLine("残差表：")
        appendLine(residualTable)
        append("当 x = ${formatBySetting(forecastX, settings)} 时，预测 y = ${formatBySetting(forecast, settings)}")
    }
}

private fun linearCoefficientStats(
    xValues: List<Double>,
    yValues: List<Double>,
    slope: Double,
    intercept: Double,
    settings: CalculatorSettings,
): List<String> {
    if (xValues.size < 3) return emptyList()
    val meanX = xValues.average()
    val fitted = xValues.map { slope * it + intercept }
    val ssRes = yValues.indices.sumOf { (yValues[it] - fitted[it]).pow(2) }
    val sxx = xValues.sumOf { (it - meanX).pow(2) }
    if (abs(sxx) < 1e-9) return emptyList()
    val sigma2 = ssRes / max(1.0, (xValues.size - 2).toDouble())
    val seSlope = sqrt(sigma2 / sxx)
    val seIntercept = sqrt(sigma2 * (1.0 / xValues.size + meanX.pow(2) / sxx))
    val tSlope = if (abs(seSlope) < 1e-9) Double.POSITIVE_INFINITY else slope / seSlope
    val tIntercept = if (abs(seIntercept) < 1e-9) Double.POSITIVE_INFINITY else intercept / seIntercept
    val df = max(1.0, (xValues.size - 2).toDouble())
    return listOf(
        "β1: SE=${formatBySetting(seSlope, settings)}  t=${formatBySetting(tSlope, settings)}  p≈${formatBySetting(2 * (1 - studentTCdf(abs(tSlope), df)), settings)}",
        "β0: SE=${formatBySetting(seIntercept, settings)}  t=${formatBySetting(tIntercept, settings)}  p≈${formatBySetting(2 * (1 - studentTCdf(abs(tIntercept), df)), settings)}",
    )
}

private fun chiSquareCdf(value: Double, df: Double): Double {
    if (value <= 0) return 0.0
    return regularizedGammaP(df / 2.0, value / 2.0)
}

private fun oneSampleTTest(values: List<Double>, mu0: Double): Triple<Double, Double, Double> {
    require(values.size >= 2) { "至少需要两项数据" }
    val mean = values.average()
    val sampleVariance = values.sumOf { (it - mean).pow(2) } / (values.size - 1)
    val se = sqrt(sampleVariance / values.size)
    val t = (mean - mu0) / se
    val df = (values.size - 1).toDouble()
    val p = 2 * (1 - studentTCdf(abs(t), df))
    return Triple(t, df, p)
}

private fun twoSampleTTest(sampleA: List<Double>, sampleB: List<Double>): Triple<Double, Double, Double> {
    require(sampleA.size >= 2 && sampleB.size >= 2) { "每组至少需要两项数据" }
    val meanA = sampleA.average()
    val meanB = sampleB.average()
    val varA = sampleA.sumOf { (it - meanA).pow(2) } / (sampleA.size - 1)
    val varB = sampleB.sumOf { (it - meanB).pow(2) } / (sampleB.size - 1)
    val numerator = meanA - meanB
    val se2 = varA / sampleA.size + varB / sampleB.size
    val t = numerator / sqrt(se2)
    val df = se2.pow(2) / (((varA / sampleA.size).pow(2) / (sampleA.size - 1)) + ((varB / sampleB.size).pow(2) / (sampleB.size - 1)))
    val p = 2 * (1 - studentTCdf(abs(t), df))
    return Triple(t, df, p)
}

private fun kolmogorovSmirnovNormalTest(values: List<Double>): Pair<Double, Double> {
    require(values.size >= 3) { "至少需要三项数据" }
    val sorted = values.sorted()
    val mean = values.average()
    val variance = values.sumOf { (it - mean).pow(2) } / (values.size - 1)
    val sd = sqrt(variance)
    val d = sorted.indices.maxOf { index ->
        val empirical = (index + 1).toDouble() / values.size
        val theoretical = normalCdf(sorted[index], mean, sd)
        max(abs(empirical - theoretical), abs(theoretical - index.toDouble() / values.size))
    }
    val lambda = (sqrt(values.size.toDouble()) + 0.12 + 0.11 / sqrt(values.size.toDouble())) * d
    val p = (1..6).sumOf { k -> 2 * (-1.0).pow(k - 1) * exp(-2 * k * k * lambda * lambda) }.coerceIn(0.0, 1.0)
    return d to p
}

private fun det3(matrix: List<List<Double>>): Double {
    return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) -
        matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) +
        matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0])
}

private fun adjugate2(matrix: List<List<Double>>): List<List<Double>> {
    return listOf(
        listOf(matrix[1][1], -matrix[0][1]),
        listOf(-matrix[1][0], matrix[0][0]),
    )
}

private fun adjugate3(matrix: List<List<Double>>): List<List<Double>> {
    val cofactors = List(3) { row ->
        List(3) { col ->
            val minor = matrix
                .filterIndexed { r, _ -> r != row }
                .map { current -> current.filterIndexed { c, _ -> c != col } }
            val minorDet = minor[0][0] * minor[1][1] - minor[0][1] * minor[1][0]
            if ((row + col) % 2 == 0) minorDet else -minorDet
        }
    }
    return transpose(cofactors)
}

private fun transpose(matrix: List<List<Double>>): List<List<Double>> {
    return List(matrix.first().size) { col ->
        List(matrix.size) { row ->
            matrix[row][col]
        }
    }
}

private fun multiplyMatrixVector(matrix: List<List<Double>>, vector: List<Double>): List<Double> {
    require(matrix.first().size == vector.size) { "矩阵列数与向量维度不一致" }
    return matrix.map { row -> row.indices.sumOf { index -> row[index] * vector[index] } }
}

private fun matrixVectorToString(vector: List<Double>): String {
    return vector.joinToString(prefix = "[", postfix = "]") { formatNumber(it) }
}

private fun augmentedMatrixToString(matrix: List<List<Double>>, rhs: List<Double>): String {
    return matrix.indices.joinToString("\n") { row ->
        val left = matrix[row].joinToString(prefix = "[", postfix = "]") { formatNumber(it) }
        "$left | ${formatNumber(rhs[row])}"
    }
}

private fun inverse2(matrix: List<List<Double>>): List<List<Double>> {
    val det = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
    require(abs(det) > 1e-9) { "矩阵不可逆" }
    val adj = adjugate2(matrix)
    return adj.map { row -> row.map { it / det } }
}

private fun matrixRank(matrix: List<List<Double>>): Int {
    val working = matrix.map { it.toMutableList() }.toMutableList()
    var rank = 0
    var row = 0
    for (col in working.first().indices) {
        var pivot = row
        while (pivot < working.size && abs(working[pivot][col]) < 1e-9) pivot++
        if (pivot == working.size) continue
        if (pivot != row) {
            val temp = working[row]
            working[row] = working[pivot]
            working[pivot] = temp
        }
        val divisor = working[row][col]
        for (index in col until working[row].size) {
            working[row][index] /= divisor
        }
        for (current in working.indices) {
            if (current == row) continue
            val factor = working[current][col]
            for (index in col until working[current].size) {
                working[current][index] -= factor * working[row][index]
            }
        }
        rank++
        row++
        if (row == working.size) break
    }
    return rank
}

private fun luDecomposition(matrix: List<List<Double>>): Pair<List<List<Double>>, List<List<Double>>> {
    val n = matrix.size
    val l = MutableList(n) { row -> MutableList(n) { col -> if (row == col) 1.0 else 0.0 } }
    val u = MutableList(n) { MutableList(n) { 0.0 } }
    for (i in 0 until n) {
        for (k in i until n) {
            u[i][k] = matrix[i][k] - (0 until i).sumOf { j -> l[i][j] * u[j][k] }
        }
        for (k in i + 1 until n) {
            require(abs(u[i][i]) > 1e-9) { "LU 分解失败，主元为 0" }
            l[k][i] = (matrix[k][i] - (0 until i).sumOf { j -> l[k][j] * u[j][i] }) / u[i][i]
        }
    }
    return l to u
}

private fun luToString(result: Pair<List<List<Double>>, List<List<Double>>>): String {
    return buildString {
        appendLine("L =")
        appendLine(matrixToString(result.first))
        appendLine("U =")
        append(matrixToString(result.second))
    }
}

private fun qrDecomposition(matrix: List<List<Double>>): Pair<List<List<Double>>, List<List<Double>>> {
    val rows = matrix.size
    val cols = matrix.first().size
    val qColumns = mutableListOf<List<Double>>()
    val r = MutableList(cols) { MutableList(cols) { 0.0 } }
    for (j in 0 until cols) {
        var v = List(rows) { matrix[it][j] }
        for (i in 0 until j) {
            val qi = qColumns[i]
            val rij = qi.indices.sumOf { index -> qi[index] * matrix[index][j] }
            r[i][j] = rij
            v = v.indices.map { index -> v[index] - rij * qi[index] }
        }
        val norm = sqrt(v.sumOf { it * it })
        require(norm > 1e-9) { "QR 分解失败，列向量线性相关" }
        r[j][j] = norm
        qColumns.add(v.map { it / norm })
    }
    val q = List(rows) { row -> List(cols) { col -> qColumns[col][row] } }
    return q to r
}

private fun qrToString(result: Pair<List<List<Double>>, List<List<Double>>>): String {
    return buildString {
        appendLine("Q =")
        appendLine(matrixToString(result.first))
        appendLine("R =")
        append(matrixToString(result.second))
    }
}

private fun eigenValues2(matrix: List<List<Double>>): String {
    val trace = matrix[0][0] + matrix[1][1]
    val determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
    val delta = trace * trace - 4 * determinant
    return if (delta >= 0) {
        val root = sqrt(delta)
        "λ1 = ${formatNumber((trace + root) / 2)}\nλ2 = ${formatNumber((trace - root) / 2)}"
    } else {
        val real = trace / 2
        val imaginary = sqrt(-delta) / 2
        "λ1 = ${formatNumber(real)} + ${formatNumber(imaginary)}i\nλ2 = ${formatNumber(real)} - ${formatNumber(imaginary)}i"
    }
}

private fun eigenValues2Real(matrix: List<List<Double>>): List<Double>? {
    val trace = matrix[0][0] + matrix[1][1]
    val determinant = matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0]
    val delta = trace * trace - 4 * determinant
    if (delta < 0) return null
    val root = sqrt(delta)
    return listOf((trace + root) / 2, (trace - root) / 2)
}

private fun diagonalization2(matrix: List<List<Double>>): String {
    val eigen = eigenValues2Real(matrix) ?: return "存在复特征值，当前不做实数域相似对角化"
    if (abs(eigen[0] - eigen[1]) < 1e-9) return "重根情况下当前未提供稳定的特征向量基"
    val vectors = eigen.map { lambda ->
        if (abs(matrix[0][1]) > 1e-9) listOf(matrix[0][1], lambda - matrix[0][0]) else listOf(lambda - matrix[1][1], matrix[1][0])
    }
    val p = listOf(
        listOf(vectors[0][0], vectors[1][0]),
        listOf(vectors[0][1], vectors[1][1]),
    )
    val d = listOf(
        listOf(eigen[0], 0.0),
        listOf(0.0, eigen[1]),
    )
    return buildString {
        appendLine("P =")
        appendLine(matrixToString(p))
        appendLine("D =")
        appendLine(matrixToString(d))
        appendLine("P^-1 =")
        append(matrixToString(inverse2(p)))
    }
}

private fun eigenVectors2(matrix: List<List<Double>>): String {
    val eigen = eigenValues2Real(matrix) ?: return "存在复特征值，当前不输出实特征向量"
    return eigen.mapIndexed { index, lambda ->
        val shifted = listOf(
            listOf(matrix[0][0] - lambda, matrix[0][1]),
            listOf(matrix[1][0], matrix[1][1] - lambda),
        )
        val basis = nullSpaceBasis(shifted)
        "λ${index + 1} = ${formatNumber(lambda)}\n" + basis.mapIndexed { basisIndex, vector ->
            "v${index + 1}.${basisIndex + 1} = ${matrixVectorToString(vector)}"
        }.joinToString("\n")
    }.joinToString("\n")
}

private fun jordanForm2(matrix: List<List<Double>>): String {
    val eigen = eigenValues2Real(matrix) ?: return "存在复特征值，当前不输出实若当标准形"
    return if (abs(eigen[0] - eigen[1]) < 1e-9) {
        val lambda = eigen[0]
        val shifted = listOf(listOf(matrix[0][0] - lambda, matrix[0][1]), listOf(matrix[1][0], matrix[1][1] - lambda))
        val basis = nullSpaceBasis(shifted)
        if (basis.size >= 2 || matrixRank(shifted) == 0) {
            "J = [[${formatNumber(lambda)}, 0], [0, ${formatNumber(lambda)}]]"
        } else {
            val chain = solveLinearSystemGeneral(shifted, basis.first())
            "J = [[${formatNumber(lambda)}, 1], [0, ${formatNumber(lambda)}]]\n链向量 v1=${matrixVectorToString(basis.first())}\nv2=${chain?.let(::matrixVectorToString) ?: "未求得"}"
        }
    } else {
        "J = [[${formatNumber(eigen[0])}, 0], [0, ${formatNumber(eigen[1])}]]"
    }
}

private fun eigenValues3(matrix: List<List<Double>>): String {
    val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]
    val principalSum =
        matrix[0][0] * matrix[1][1] + matrix[0][0] * matrix[2][2] + matrix[1][1] * matrix[2][2] -
            matrix[0][1] * matrix[1][0] - matrix[0][2] * matrix[2][0] - matrix[1][2] * matrix[2][1]
    val determinant = det3(matrix)
    return solvePolynomial(listOf(1.0, -trace, principalSum, -determinant))
}

private fun diagonalization3(matrix: List<List<Double>>): String {
    val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]
    val principalSum =
        matrix[0][0] * matrix[1][1] + matrix[0][0] * matrix[2][2] + matrix[1][1] * matrix[2][2] -
            matrix[0][1] * matrix[1][0] - matrix[0][2] * matrix[2][0] - matrix[1][2] * matrix[2][1]
    val determinant = det3(matrix)
    val roots = durandKernerRoots(listOf(1.0, -trace, principalSum, -determinant))
    val realRoots = roots.filter { abs(it.im) < 1e-6 }.map { it.re }
    if (realRoots.size != 3) return "当前仅在三实特征值情况下提供对角阵候选"
    val d = listOf(
        listOf(realRoots[0], 0.0, 0.0),
        listOf(0.0, realRoots[1], 0.0),
        listOf(0.0, 0.0, realRoots[2]),
    )
    return buildString {
        appendLine("D =")
        appendLine(matrixToString(d))
        append("存在三实特征值时可相似对角化，当前先输出对角阵候选。")
    }
}

private fun eigenVectors3(matrix: List<List<Double>>): String {
    val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]
    val principalSum =
        matrix[0][0] * matrix[1][1] + matrix[0][0] * matrix[2][2] + matrix[1][1] * matrix[2][2] -
            matrix[0][1] * matrix[1][0] - matrix[0][2] * matrix[2][0] - matrix[1][2] * matrix[2][1]
    val determinant = det3(matrix)
    val roots = durandKernerRoots(listOf(1.0, -trace, principalSum, -determinant)).filter { abs(it.im) < 1e-6 }.map { it.re }
    if (roots.isEmpty()) return "当前仅在实特征值情况下输出候选特征向量"
    return roots.distinctBy { round(it * 1_000_000) / 1_000_000 }.mapIndexed { index, lambda ->
        val shifted = List(3) { row ->
            List(3) { col ->
                if (row == col) matrix[row][col] - lambda else matrix[row][col]
            }
        }
        val basis = nullSpaceBasis(shifted)
        "λ${index + 1} = ${formatNumber(lambda)}\n" + basis.mapIndexed { basisIndex, vector ->
            "v${index + 1}.${basisIndex + 1} = ${matrixVectorToString(vector)}"
        }.joinToString("\n")
    }.joinToString("\n")
}

private fun jordanForm3(matrix: List<List<Double>>): String {
    val trace = matrix[0][0] + matrix[1][1] + matrix[2][2]
    val principalSum =
        matrix[0][0] * matrix[1][1] + matrix[0][0] * matrix[2][2] + matrix[1][1] * matrix[2][2] -
            matrix[0][1] * matrix[1][0] - matrix[0][2] * matrix[2][0] - matrix[1][2] * matrix[2][1]
    val determinant = det3(matrix)
    val roots = durandKernerRoots(listOf(1.0, -trace, principalSum, -determinant)).filter { abs(it.im) < 1e-6 }.map { it.re }
    if (roots.size != 3) return "当前仅在三实特征值场景下输出若当标准形候选"
    val sorted = roots.sorted()
    val grouped = sorted.groupBy { round(it * 1_000_000) / 1_000_000 }
    val diagonal = mutableListOf<List<Double>>()
    val chains = mutableListOf<String>()
    grouped.entries.forEach { entry ->
        val lambda = entry.key
        val multiplicity = entry.value.size
        val shifted = List(3) { row ->
            List(3) { col ->
                if (row == col) matrix[row][col] - lambda else matrix[row][col]
            }
        }
        val basis = nullSpaceBasis(shifted)
        if (multiplicity == 1 || basis.size >= multiplicity) {
            repeat(multiplicity) { diagonal += listOf(lambda) }
        } else {
            repeat(multiplicity) { diagonal += listOf(lambda) }
            val chain = solveLinearSystemGeneral(shifted, basis.first())
            chains += "λ=${formatNumber(lambda)} 链向量: v1=${matrixVectorToString(basis.first())}, v2=${chain?.let(::matrixVectorToString) ?: "未求得"}"
        }
    }
    val j = MutableList(3) { MutableList(3) { 0.0 } }
    diagonal.forEachIndexed { index, item -> j[index][index] = item.first() }
    for (index in 0 until 2) {
        if (abs(j[index][index] - j[index + 1][index + 1]) < 1e-6) j[index][index + 1] = 1.0
    }
    return buildString {
        appendLine(matrixToString(j))
        if (chains.isNotEmpty()) append(chains.joinToString("\n"))
    }
}

private fun jordanChainView(matrix: List<List<Double>>, lambda: Double, chainLength: Int, activeNode: Int): String {
    require(chainLength >= 1) { "链长度至少为 1" }
    val shifted = List(matrix.size) { row ->
        List(matrix.first().size) { col ->
            if (row == col) matrix[row][col] - lambda else matrix[row][col]
        }
    }
    val basis = nullSpaceBasis(shifted)
    require(basis.isNotEmpty()) { "该 λ 下没有普通特征向量" }
    val chain = mutableListOf<List<Double>>()
    chain += basis.first()
    val steps = mutableListOf<String>()
    steps += "步骤 1：构造 A - λI"
    steps += matrixToString(shifted)
    steps += "步骤 2：求 null(A - λI)，得到起始特征向量 v1 = ${matrixVectorToString(basis.first())}"
    while (chain.size < chainLength) {
        val target = chain.last()
        steps += "步骤 ${chain.size + 1}：求解 (A - λI)v${chain.size + 1} = v${chain.size}"
        steps += "右侧目标 = ${matrixVectorToString(target)}"
        val next = solveLinearSystemGeneral(shifted, target) ?: break
        chain += next
        steps += "得到 v${chain.size} = ${matrixVectorToString(next)}"
    }
    return buildString {
        appendLine("Jordan 链编辑视图")
        appendLine("λ = ${formatNumber(lambda)}")
        appendLine("目标链长度 = $chainLength")
        appendLine("当前聚焦 = v$activeNode")
        appendLine()
        chain.forEachIndexed { index, vector ->
            appendLine("${if (index + 1 == activeNode) ">> " else ""}v${index + 1} = ${matrixVectorToString(vector)}")
        }
        appendLine()
        appendLine("步骤展示：")
        steps.forEachIndexed { index, step ->
            appendLine("${if (index + 1 == activeNode) ">> " else ""}$step")
        }
        if (chain.size < chainLength) append("当前矩阵下只能构造到长度 ${chain.size} 的 Jordan 链。")
    }
}

private fun nullSpaceBasis(matrix: List<List<Double>>): List<List<Double>> {
    val rref = matrix.map { it.toMutableList() }.toMutableList()
    val rows = rref.size
    val cols = rref.first().size
    val pivotColumns = mutableListOf<Int>()
    var row = 0
    for (col in 0 until cols) {
        var pivot = row
        while (pivot < rows && abs(rref[pivot][col]) < 1e-9) pivot++
        if (pivot == rows) continue
        val temp = rref[row]
        rref[row] = rref[pivot]
        rref[pivot] = temp
        val divisor = rref[row][col]
        for (j in col until cols) rref[row][j] /= divisor
        for (current in 0 until rows) {
            if (current == row) continue
            val factor = rref[current][col]
            for (j in col until cols) rref[current][j] -= factor * rref[row][j]
        }
        pivotColumns += col
        row++
        if (row == rows) break
    }
    val freeColumns = (0 until cols).filterNot { pivotColumns.contains(it) }
    if (freeColumns.isEmpty()) return emptyList()
    return freeColumns.map { free ->
        MutableList(cols) { col -> if (col == free) 1.0 else 0.0 }.also { vector ->
            pivotColumns.forEachIndexed { pivotRow, pivotCol ->
                vector[pivotCol] = -rref[pivotRow][free]
            }
        }
    }
}

private fun solveLinearSystemGeneral(matrix: List<List<Double>>, rhs: List<Double>): List<Double>? {
    val rows = matrix.size
    val cols = matrix.first().size
    val augmented = Array(rows) { row ->
        DoubleArray(cols + 1) { col -> if (col < cols) matrix[row][col] else rhs[row] }
    }
    val pivotColumns = mutableListOf<Int>()
    var row = 0
    for (col in 0 until cols) {
        var pivot = row
        while (pivot < rows && abs(augmented[pivot][col]) < 1e-9) pivot++
        if (pivot == rows) continue
        val temp = augmented[row]
        augmented[row] = augmented[pivot]
        augmented[pivot] = temp
        val divisor = augmented[row][col]
        for (j in col until cols + 1) augmented[row][j] /= divisor
        for (current in 0 until rows) {
            if (current == row) continue
            val factor = augmented[current][col]
            for (j in col until cols + 1) augmented[current][j] -= factor * augmented[row][j]
        }
        pivotColumns += col
        row++
        if (row == rows) break
    }
    for (current in 0 until rows) {
        if ((0 until cols).all { abs(augmented[current][it]) < 1e-9 } && abs(augmented[current][cols]) > 1e-9) return null
    }
    val solution = MutableList(cols) { 0.0 }
    pivotColumns.forEachIndexed { pivotRow, pivotCol ->
        solution[pivotCol] = augmented[pivotRow][cols]
    }
    return solution
}

private fun regularizedGammaP(a: Double, x: Double): Double {
    if (x <= 0) return 0.0
    return if (x < a + 1.0) {
        gammaSeries(a, x)
    } else {
        1.0 - gammaContinuedFraction(a, x)
    }
}

private fun gammaSeries(a: Double, x: Double): Double {
    var sum = 1.0 / a
    var term = sum
    var n = 1
    while (n < 200) {
        term *= x / (a + n)
        sum += term
        if (abs(term) < abs(sum) * 1e-12) break
        n++
    }
    return sum * exp(-x + a * ln(x) - logGamma(a))
}

private fun gammaContinuedFraction(a: Double, x: Double): Double {
    val fpMin = 1e-30
    var b = x + 1.0 - a
    var c = 1.0 / fpMin
    var d = 1.0 / b
    var h = d
    for (i in 1..200) {
        val an = -i.toDouble() * (i.toDouble() - a)
        b += 2.0
        d = an * d + b
        if (abs(d) < fpMin) d = fpMin
        c = b + an / c
        if (abs(c) < fpMin) c = fpMin
        d = 1.0 / d
        val delta = d * c
        h *= delta
        if (abs(delta - 1.0) < 1e-12) break
    }
    return exp(-x + a * ln(x) - logGamma(a)) * h
}

private fun loadCalculatorSettings(prefs: SharedPreferences): CalculatorSettings {
    return runCatching {
        val angle = prefs.getString("settings_angle", AngleMode.Rad.name).orEmpty()
        val format = prefs.getString("settings_format", ResultFormat.Standard.name).orEmpty()
        val digits = prefs.getInt("settings_digits", 6)
        val rounding = prefs.getString("settings_rounding", RoundingRule.HalfUp.name).orEmpty()
        CalculatorSettings(
            angleMode = AngleMode.entries.firstOrNull { it.name == angle } ?: AngleMode.Rad,
            resultFormat = ResultFormat.entries.firstOrNull { it.name == format } ?: ResultFormat.Standard,
            displayDigits = digits.coerceIn(2, 10),
            roundingRule = RoundingRule.entries.firstOrNull { it.name == rounding } ?: RoundingRule.HalfUp,
        )
    }.getOrElse {
        CalculatorSettings() // 加载失败时回退到默认设置
    }
}

private fun saveCalculatorSettings(prefs: SharedPreferences, settings: CalculatorSettings) {
    prefs.edit()
        .putString("settings_angle", settings.angleMode.name)
        .putString("settings_format", settings.resultFormat.name)
        .putInt("settings_digits", settings.displayDigits)
        .putString("settings_rounding", settings.roundingRule.name)
        .apply()
}

private fun loadPreferenceList(prefs: SharedPreferences, key: String): List<String> {
    return prefs.getString(key, "").orEmpty().split('\u0001').map { it.trim() }.filter { it.isNotEmpty() }
}

private fun savePreferenceList(prefs: SharedPreferences, key: String, values: SnapshotStateList<String>) {
    prefs.edit().putString(key, values.joinToString("\u0001")).apply()
}

private fun inverse3(matrix: List<List<Double>>): List<List<Double>> {
    val det = det3(matrix)
    require(abs(det) > 1e-9) { "矩阵不可逆" }
    val adjugate = adjugate3(matrix)
    return List(3) { row ->
        List(3) { col ->
            adjugate[row][col] / det
        }
    }
}

private fun solveLinear3(matrix: List<List<Double>>, rhs: List<Double>): List<Double> {
    require(matrix.size == 3 && rhs.size == 3) { "仅支持 3 元一次方程组" }
    val augmented = Array(3) { row ->
        DoubleArray(4) { col ->
            if (col < 3) matrix[row][col] else rhs[row]
        }
    }
    for (pivot in 0 until 3) {
        var best = pivot
        for (row in pivot + 1 until 3) {
            if (abs(augmented[row][pivot]) > abs(augmented[best][pivot])) best = row
        }
        require(abs(augmented[best][pivot]) > 1e-9) { "方程组无唯一解" }
        if (best != pivot) {
            val temp = augmented[pivot]
            augmented[pivot] = augmented[best]
            augmented[best] = temp
        }
        val divisor = augmented[pivot][pivot]
        for (col in pivot until 4) augmented[pivot][col] /= divisor
        for (row in 0 until 3) {
            if (row == pivot) continue
            val factor = augmented[row][pivot]
            for (col in pivot until 4) {
                augmented[row][col] -= factor * augmented[pivot][col]
            }
        }
    }
    return List(3) { augmented[it][3] }
}

private fun multiply3(a: List<List<Double>>, b: List<List<Double>>): List<List<Double>> {
    return List(3) { row ->
        List(3) { col ->
            (0..2).sumOf { idx -> a[row][idx] * b[idx][col] }
        }
    }
}

private fun matrixToString(matrix: List<List<Double>>): String {
    return matrix.joinToString("\n") { row ->
        row.joinToString(prefix = "[", postfix = "]") { formatNumber(it) }
    }
}

private fun Double?.orZero(): Double = this ?: 0.0

private data class ComplexNumber(val re: Double, val im: Double = 0.0) {
    operator fun plus(other: ComplexNumber) = ComplexNumber(re + other.re, im + other.im)
    operator fun minus(other: ComplexNumber) = ComplexNumber(re - other.re, im - other.im)
    operator fun times(other: ComplexNumber) = ComplexNumber(re * other.re - im * other.im, re * other.im + im * other.re)
    operator fun div(other: ComplexNumber): ComplexNumber {
        val denominator = other.re * other.re + other.im * other.im
        return ComplexNumber((re * other.re + im * other.im) / denominator, (im * other.re - re * other.im) / denominator)
    }
    fun absValue(): Double = sqrt(re * re + im * im)
    override fun toString(): String {
        return if (abs(im) < 1e-8) {
            formatNumber(re)
        } else {
            "${formatNumber(re)} ${if (im >= 0) "+" else "-"} ${formatNumber(abs(im))}i"
        }
    }
}

private fun solvePolynomial(coefficients: List<Double>): String {
    require(coefficients.firstOrNull()?.let { abs(it) > 1e-9 } == true) { "最高次项系数不能为 0" }
    val degree = coefficients.size - 1
    val roots = if (degree == 1) {
        listOf(ComplexNumber(-coefficients[1] / coefficients[0]))
    } else {
        durandKernerRoots(coefficients)
    }
    return roots.mapIndexed { index, root -> "x${index + 1} = $root" }.joinToString("\n")
}

private data class FormulaStructure(
    val key: String,
    val mode: String,
    val slotA: String,
    val slotB: String,
    val start: Int,
    val end: Int,
)

private fun appendFormulaToken(current: String, token: String): String {
    return when (token) {
        "÷" -> current + "/"
        "×" -> current + "*"
        "√" -> current + "sqrt("
        "()" -> current + "()"
        "a/b" -> current + "()/()"
        "1/x" -> current + "1/()"
        "x²" -> current + "^2"
        "x³" -> current + "^3"
        "x!" -> current + "!"
        else -> current + token
    }
}

private fun buildStructuredFormula(mode: String, slotA: String, slotB: String): String {
    val left = slotA.ifBlank { "0" }
    val right = slotB.ifBlank { "1" }
    return when (mode) {
        "a/b" -> "($left)/($right)"
        "1/x" -> "1/(${slotA.ifBlank { "1" }})"
        "√" -> "sqrt(${slotA.ifBlank { "0" }})"
        else -> ""
    }
}

private fun insertAtCursor(value: TextFieldValue, inserted: String): TextFieldValue {
    val start = value.selection.start.coerceIn(0, value.text.length)
    val end = value.selection.end.coerceIn(0, value.text.length)
    val nextText = value.text.replaceRange(start, end, inserted)
    val nextCursor = (start + inserted.length).coerceAtMost(nextText.length)
    return TextFieldValue(nextText, TextRange(nextCursor))
}

private fun extractFormulaStructures(expression: String): List<FormulaStructure> {
    val result = mutableListOf<FormulaStructure>()
    Regex("""\(([^()]*)\)/\(([^()]*)\)""").findAll(expression).forEachIndexed { index, match ->
        result += FormulaStructure(
            key = "frac-$index-${match.range.first}",
            mode = "a/b",
            slotA = match.groupValues[1],
            slotB = match.groupValues[2],
            start = match.range.first,
            end = match.range.last + 1,
        )
    }
    Regex("""1/\(([^()]*)\)""").findAll(expression).forEachIndexed { index, match ->
        result += FormulaStructure(
            key = "rec-$index-${match.range.first}",
            mode = "1/x",
            slotA = match.groupValues[1],
            slotB = "",
            start = match.range.first,
            end = match.range.last + 1,
        )
    }
    Regex("""sqrt\(([^()]*)\)""").findAll(expression).forEachIndexed { index, match ->
        result += FormulaStructure(
            key = "rad-$index-${match.range.first}",
            mode = "√",
            slotA = match.groupValues[1],
            slotB = "",
            start = match.range.first,
            end = match.range.last + 1,
        )
    }
    return result.sortedBy { it.start }
}

private fun replaceStructuredFormula(expression: String, structure: FormulaStructure, slotA: String, slotB: String): String {
    val replacement = buildStructuredFormula(structure.mode, slotA, slotB)
    return expression.replaceRange(structure.start, structure.end, replacement)
}

private fun summarizeStructureTree(expression: String): String {
    val structures = extractFormulaStructures(expression)
    if (structures.isEmpty()) return ""
    return buildString {
        append("结构树：")
        structures.forEachIndexed { index, structure ->
            if (index > 0) append("  |  ")
            append(
                when (structure.mode) {
                    "a/b" -> "分数(${structure.slotA.ifBlank { "□" }}/${structure.slotB.ifBlank { "□" }})"
                    "1/x" -> "倒数(1/${structure.slotA.ifBlank { "□" }})"
                    else -> "根式(√${structure.slotA.ifBlank { "□" }})"
                },
            )
        }
    }
}

private fun formatExpressionLines(expression: String, maxLineLength: Int = 26): List<String> {
    if (expression.length <= maxLineLength) return listOf(expression)
    val result = mutableListOf<String>()
    var current = StringBuilder()
    val breakChars = setOf('+', '-', '×', '∕')
    var depth = 0
    expression.forEach { ch ->
        if (ch == ')') depth = (depth - 1).coerceAtLeast(0)
        current.append(ch)
        if (current.length >= maxLineLength && breakChars.contains(ch)) {
            result += current.toString().trim()
            current = StringBuilder("  ".repeat(depth.coerceAtMost(4)))
        }
        if (ch == '(') depth++
    }
    if (current.isNotEmpty()) result += current.toString().trim()
    return result.filter { it.isNotBlank() }
}

private fun reorderStructure(
    expression: String,
    structures: List<FormulaStructure>,
    index: Int,
    delta: Int,
): Pair<String, Int>? {
    val targetIndex = index + delta
    if (index !in structures.indices || targetIndex !in structures.indices) return null
    val first = structures[min(index, targetIndex)]
    val second = structures[max(index, targetIndex)]
    val before = expression.substring(0, first.start)
    val firstText = expression.substring(first.start, first.end)
    val middle = expression.substring(first.end, second.start)
    val secondText = expression.substring(second.start, second.end)
    val after = expression.substring(second.end)
    val swapped = if (delta < 0) {
        before + secondText + middle + firstText + after
    } else {
        before + secondText + middle + firstText + after
    }
    return swapped to targetIndex
}

private fun prettyFormatExpression(expression: String): String {
    return expression
        .replace("sqrt(", "√(")
        .replace("sin(", "sin(")
        .replace("cos(", "cos(")
        .replace("tan(", "tan(")
        .replace("ln(", "ln(")
        .replace("log(", "log(")
        .replace("*", " × ")
        .replace("/", " ∕ ")
        .replace("^3", "³")
        .replace("^2", "²")
}

private fun durandKernerRoots(coefficients: List<Double>): List<ComplexNumber> {
    val degree = coefficients.size - 1
    val normalized = coefficients.map { it / coefficients[0] }
    var roots = List(degree) { index ->
        val angle = 2 * PI * index / degree
        ComplexNumber(cos(angle), sin(angle))
    }
    repeat(80) {
        roots = roots.mapIndexed { index, root ->
            var denominator = ComplexNumber(1.0, 0.0)
            roots.forEachIndexed { otherIndex, other ->
                if (index != otherIndex) denominator *= (root - other)
            }
            root - evaluatePolynomial(normalized, root) / denominator
        }
    }
    return roots.sortedBy { it.re }
}

private fun evaluatePolynomial(coefficients: List<Double>, value: ComplexNumber): ComplexNumber {
    var result = ComplexNumber(coefficients.first())
    for (index in 1 until coefficients.size) {
        result = result * value + ComplexNumber(coefficients[index])
    }
    return result
}

private enum class BaseUnit(val symbol: String) {
    None(""), Length("m"), Mass("kg"), Time("s"), Current("A"), Temperature("K"), Amount("mol"), Luminous("cd")
}

private data class UnitDimension(val units: Map<BaseUnit, Int> = emptyMap()) {
    operator fun plus(other: UnitDimension): UnitDimension {
        val next = units.toMutableMap()
        other.units.forEach { (k, v) -> next[k] = next.getOrDefault(k, 0) + v }
        return UnitDimension(next.filterValues { it != 0 })
    }
    operator fun minus(other: UnitDimension): UnitDimension {
        val next = units.toMutableMap()
        other.units.forEach { (k, v) -> next[k] = next.getOrDefault(k, 0) - v }
        return UnitDimension(next.filterValues { it != 0 })
    }
    fun isCompatible(other: UnitDimension) = units == other.units
    fun isNone() = units.isEmpty()
    
    override fun toString(): String {
        if (isNone()) return ""
        return units.entries.joinToString("*") { (u, p) -> if (p == 1) u.symbol else "${u.symbol}^$p" }
    }
}

private data class Quantity(val value: Double, val dimension: UnitDimension = UnitDimension()) {
    operator fun plus(other: Quantity): Quantity {
        require(dimension.isCompatible(other.dimension)) { "单位不兼容: $dimension vs ${other.dimension}" }
        return Quantity(value + other.value, dimension)
    }
    operator fun minus(other: Quantity): Quantity {
        require(dimension.isCompatible(other.dimension)) { "单位不兼容: $dimension vs ${other.dimension}" }
        return Quantity(value - other.value, dimension)
    }
    operator fun times(other: Quantity) = Quantity(value * other.value, dimension + other.dimension)
    operator fun div(other: Quantity) = Quantity(value / other.value, dimension - other.dimension)
    
    fun toString(settings: CalculatorSettings): String {
        val formattedValue = formatBySetting(value, settings)
        val dimStr = dimension.toString()
        return if (dimStr.isEmpty()) formattedValue else "$formattedValue $dimStr"
    }
}

private object UnitRegistry {
    private val registry = mapOf(
        "m" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "km" to Quantity(1000.0, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "cm" to Quantity(0.01, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "mm" to Quantity(0.001, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "in" to Quantity(0.0254, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "ft" to Quantity(0.3048, UnitDimension(mapOf(BaseUnit.Length to 1))),
        "s" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Time to 1))),
        "min" to Quantity(60.0, UnitDimension(mapOf(BaseUnit.Time to 1))),
        "h" to Quantity(3600.0, UnitDimension(mapOf(BaseUnit.Time to 1))),
        "kg" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Mass to 1))),
        "g" to Quantity(0.001, UnitDimension(mapOf(BaseUnit.Mass to 1))),
        "t" to Quantity(1000.0, UnitDimension(mapOf(BaseUnit.Mass to 1))),
        "N" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Mass to 1, BaseUnit.Length to 1, BaseUnit.Time to -2))),
        "J" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Mass to 1, BaseUnit.Length to 2, BaseUnit.Time to -2))),
        "W" to Quantity(1.0, UnitDimension(mapOf(BaseUnit.Mass to 1, BaseUnit.Length to 2, BaseUnit.Time to -3))),
    )
    
    fun get(symbol: String): Quantity? = registry[symbol]
}

private object ExpressionEngine {
    fun evaluate(
        expression: String,
        xValue: Double = 0.0,
        angleMode: AngleMode = AngleMode.Rad,
        variables: Map<String, Double> = emptyMap(),
    ): Double {
        val parser = Parser(expression.replace("x", "($xValue)"), angleMode, variables.mapValues { Quantity(it.value) })
        return parser.parse().value
    }

    fun evaluateQuantity(
        expression: String,
        xValue: Double = 0.0,
        angleMode: AngleMode = AngleMode.Rad,
        variables: Map<String, Quantity> = emptyMap(),
    ): Quantity {
        if (expression.length > 500) throw IllegalArgumentException("语法错误: 表达式过长")
        val parser = Parser(expression.replace("x", "($xValue)"), angleMode, variables)
        return parser.parse()
    }

    private class Parser(
        private val source: String,
        private val angleMode: AngleMode,
        private val variables: Map<String, Quantity>,
    ) {
        private var index = 0
        private var recursionDepth = 0
        private val maxRecursionDepth = 100 // 严格限制递归深度

        private fun checkRecursion() {
            recursionDepth++
            if (recursionDepth > maxRecursionDepth) throw IllegalStateException("计算错误: 嵌套过深")
        }

        private fun exitRecursion() {
            recursionDepth--
        }

        fun parse(): Quantity {
            return runCatching {
                val value = parseExpression()
                skipSpace()
                if (index < source.length) throw IllegalArgumentException("语法错误: 存在无法识别的字符")
                value
            }.getOrElse { e ->
                throw if (e is IllegalArgumentException || e is IllegalStateException) e 
                else IllegalArgumentException("语法错误: ${e.message}")
            }
        }

        private fun parseExpression(): Quantity {
            checkRecursion()
            try {
                var value = parseTerm()
                while (true) {
                    skipSpace()
                    value = when {
                        match('+') -> value + parseTerm()
                        match('-') -> value - parseTerm()
                        else -> return value
                    }
                }
            } finally {
                exitRecursion()
            }
        }

        private fun parseTerm(): Quantity {
            var value = parsePower()
            while (true) {
                skipSpace()
                value = when {
                    match('*') -> value * parsePower()
                    match('/') -> value / parsePower()
                    // 隐式乘法
                    peek() == '(' || peek().isLetter() || peek().isDigit() -> {
                        value * parsePower()
                    }
                    else -> return value
                }
            }
        }

        private fun parsePower(): Quantity {
            var value = parseUnary()
            skipSpace()
            if (match('^')) {
                val exponent = parsePower()
                require(exponent.dimension.isNone()) { "指数不能带单位" }
                value = Quantity(value.value.pow(exponent.value), UnitDimension(value.dimension.units.mapValues { (it.value * exponent.value).toInt() }))
            }
            return value
        }

        private fun parseUnary(): Quantity {
            skipSpace()
            return when {
                match('+') -> parseUnary()
                match('-') -> {
                    val q = parseUnary()
                    Quantity(-q.value, q.dimension)
                }
                else -> parsePostfix()
            }
        }

        private fun parsePostfix(): Quantity {
            var value = parsePrimary()
            while (true) {
                skipSpace()
                value = when {
                    match('%') -> Quantity(value.value / 100.0, value.dimension)
                    match('!') -> {
                        require(value.dimension.isNone()) { "阶乘不能带单位" }
                        Quantity(factorial(value.value))
                    }
                    else -> return value
                }
            }
        }

        private fun parsePrimary(): Quantity {
            skipSpace()
            if (match('(')) {
                val value = parseExpression()
                require(match(')')) { "语法错误: 缺少右括号" }
                return value
            }
            if (peek().isLetter()) {
                val name = parseIdentifier()
                skipSpace()
                
                // 优先检查是否是单位 (例如 5km)
                val unit = UnitRegistry.get(name)
                if (unit != null) {
                    // 如果单位前面紧跟数字或表达式，作为乘法处理
                    // 但由于我们的递归逻辑，这里的 unit 会被当作一个独立的 Quantity
                    return unit
                }

                if (match('(')) {
                    val value = parseExpression()
                    require(match(')')) { "语法错误: 函数缺少右括号" }
                    require(value.dimension.isNone()) { "函数参数不能带单位" }
                    return Quantity(applyFunction(name, value.value))
                }
                return when (name.lowercase()) {
                    "pi" -> Quantity(PI)
                    "e" -> Quantity(kotlin.math.E)
                    "ans" -> variables["ans"] ?: Quantity(0.0)
                    else -> variables[name] ?: variables[name.uppercase()] ?: error("语法错误: 未知标识符 $name")
                }
            }
            return Quantity(parseNumber())
        }

        private fun applyFunction(name: String, value: Double): Double {
            val angleValue = if (angleMode == AngleMode.Deg) value * PI / 180.0 else value
            return when (name.lowercase()) {
                "sin" -> sin(angleValue)
                "cos" -> cos(angleValue)
                "tan" -> tan(angleValue)
                "asin" -> if (angleMode == AngleMode.Deg) asin(value) * 180 / PI else asin(value)
                "acos" -> if (angleMode == AngleMode.Deg) acos(value) * 180 / PI else acos(value)
                "atan" -> if (angleMode == AngleMode.Deg) atan(value) * 180 / PI else atan(value)
                "sqrt" -> sqrt(value)
                "ln" -> ln(value)
                "log" -> log10(value)
                "abs" -> abs(value)
                "exp" -> exp(value)
                else -> error("语法错误: 未知函数 $name")
            }
        }

        private fun factorial(value: Double): Double {
            require(abs(value - round(value)) < 1e-9) { "计算错误: 阶乘要求整数" }
            require(value >= 0) { "计算错误: 阶乘要求非负整数" }
            val target = round(value).toInt()
            var result = 1.0
            for (i in 2..target) result *= i
            return result
        }

        private fun parseNumber(): Double {
            skipSpace()
            val start = index
            while (index < source.length && (source[index].isDigit() || source[index] == '.')) index++
            require(start != index) { "语法错误: 缺少数值" }
            val numStr = source.substring(start, index)
            return numStr.toDoubleOrNull() ?: throw IllegalArgumentException("语法错误: 非法数值 $numStr")
        }

        private fun parseIdentifier(): String {
            val start = index
            while (index < source.length && source[index].isLetter()) index++
            return source.substring(start, index)
        }

        private fun skipSpace() {
            while (index < source.length && source[index].isWhitespace()) index++
        }

        private fun match(char: Char): Boolean {
            if (index < source.length && source[index] == char) {
                index++
                return true
            }
            return false
        }

        private fun peek(): Char = if (index < source.length) source[index] else '\u0000'
    }
}
