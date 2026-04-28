
package com.poxiao.app.calculator

import android.content.SharedPreferences
import android.content.ClipData
import android.content.ClipboardManager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.Calculate
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import kotlin.math.roundToInt
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
    Compute("计算", "表达式与科学函数"),
    Statistics("统计", "均值、方差、回归"),
    Test("检验", "χ² / KS / t 检验"),
    Distribution("分布", "正态与二项分布"),
    Spreadsheet("数据表格", "小型表格与汇总"),
    FunctionTable("函数表格", "按区间生成函数值"),
    Equation("方程", "一次、二次、线性方程组"),
    Inequality("不等式", "一元二次不等式"),
    Complex("复数", "代数与极坐标"),
    Base("进制", "2/8/10/16 转换"),
    Matrix("矩阵", "2x2 行列式与逆"),
    Vector("向量", "点积、叉积、模长"),
    Ratio("比例", "正反比例与缩放"),
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

private sealed interface CalculatorRoute {
    data class App(val app: CalculatorApp) : CalculatorRoute
    data class Utility(val page: UtilityPage) : CalculatorRoute
}

@Composable
fun ScientificCalculatorScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator_prefs", 0) }
    val configuration = LocalConfiguration.current
    var settings by remember { mutableStateOf(loadCalculatorSettings(prefs)) }
    var currentRoute by remember { mutableStateOf<CalculatorRoute>(CalculatorRoute.App(CalculatorApp.Compute)) }
    var showDirectory by remember { mutableStateOf(false) }
    val contentMaxHeight = (configuration.screenHeightDp * 0.78f).dp.coerceIn(460.dp, 760.dp)
    val updateSettings: (CalculatorSettings) -> Unit = { next ->
        settings = next
        saveCalculatorSettings(prefs, next)
    }
    val openApp: (CalculatorApp) -> Unit = { app ->
        currentRoute = CalculatorRoute.App(app)
        showDirectory = false
    }
    val openUtility: (UtilityPage) -> Unit = { page ->
        currentRoute = CalculatorRoute.Utility(page)
        showDirectory = false
    }
    val backToCalculator: () -> Unit = {
        currentRoute = CalculatorRoute.App(CalculatorApp.Compute)
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
    val routeIcon = when {
        showDirectory -> Icons.Outlined.Widgets
        routeState is CalculatorRoute.App -> tileIcon(routeState.app.title)
        else -> tileIcon((routeState as CalculatorRoute.Utility).page.title)
    }
    val primaryActionText = if (showDirectory || currentRoute != CalculatorRoute.App(CalculatorApp.Compute)) "计算器" else "返回"
    val primaryActionColor = if (showDirectory) Color(0xFF5C8FB8) else ForestGreen
    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(0.dp),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color(0x22A1D8B7),
                        ),
                    ),
                )
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            CalculatorWorkspaceHeader(
                title = routeTitle,
                subtitle = routeSubtitle,
                icon = routeIcon,
                actionText = primaryActionText,
                actionColor = primaryActionColor,
                onAction = {
                    if (showDirectory || currentRoute != CalculatorRoute.App(CalculatorApp.Compute)) {
                        backToCalculator()
                    } else {
                        onBack()
                    }
                },
                onOpenDirectory = { showDirectory = true },
                directoryOpen = showDirectory,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (showDirectory) {
                CalculatorDirectoryScreen(
                    currentRoute = currentRoute,
                    onOpenApp = openApp,
                    onOpenUtility = openUtility,
                    maxHeight = contentMaxHeight,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = contentMaxHeight),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        when (routeState) {
                            is CalculatorRoute.App -> when (routeState.app) {
                                CalculatorApp.Compute -> ComputeModule(settings = settings)
                                CalculatorApp.Statistics -> StatisticsModule(settings = settings)
                                CalculatorApp.Test -> HypothesisTestModule(settings = settings)
                                CalculatorApp.Distribution -> DistributionModule(settings = settings)
                                CalculatorApp.Spreadsheet -> SpreadsheetModule()
                                CalculatorApp.FunctionTable -> FunctionTableModule(settings = settings)
                                CalculatorApp.Equation -> EquationModule()
                                CalculatorApp.Inequality -> InequalityModule()
                                CalculatorApp.Complex -> ComplexModule()
                                CalculatorApp.Base -> BaseModule()
                                CalculatorApp.Matrix -> MatrixModule(settings = settings)
                                CalculatorApp.Vector -> VectorModule(settings = settings)
                                CalculatorApp.Ratio -> RatioModule()
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
) {
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.primary.copy(alpha = 0.22f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
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
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = PineInk)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.clickable(onClick = onOpenDirectory),
                    shape = CalculatorInnerShape,
                    color = if (directoryOpen) palette.primary.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreHoriz,
                            contentDescription = "更多",
                            tint = if (directoryOpen) palette.primary else PineInk,
                        )
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
}

@Composable
private fun CalculatorDirectoryScreen(
    currentRoute: CalculatorRoute,
    onOpenApp: (CalculatorApp) -> Unit,
    onOpenUtility: (UtilityPage) -> Unit,
    maxHeight: androidx.compose.ui.unit.Dp,
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
        modifier = Modifier
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
    CalculatorGlassPanel {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = PineInk)
                Surface(shape = CalculatorChipShape, color = Color.White.copy(alpha = 0.18f)) {
                    Text(
                        text = "${items.size} 项",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
            }
            Surface(
                shape = CalculatorPanelShape,
                color = Color.White.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, item ->
                        CalculatorDirectoryRow(item = item)
                        if (index != items.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(BambooStroke.copy(alpha = 0.26f)),
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
    val palette = tilePalette(item.title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height(42.dp)
                .background(palette.primary.copy(alpha = 0.14f), CalculatorChipShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tileIcon(item.title),
                contentDescription = null,
                tint = palette.primary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.72f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.selected) {
                Surface(shape = CalculatorChipShape, color = palette.primary.copy(alpha = 0.14f)) {
                    Text(
                        text = "当前",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.primary,
                    )
                }
            }
            Text("›", style = MaterialTheme.typography.titleMedium, color = ForestDeep.copy(alpha = 0.54f))
        }
    }
}

private data class TilePalette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
)

private fun tilePalette(title: String): TilePalette {
    return when (title) {
        "计算", "通用计算" -> TilePalette(Color(0xFF1E5C4C), Color(0xFF3A8A71), Color(0xFFBCE6D7))
        "统计" -> TilePalette(Color(0xFF315E84), Color(0xFF5C8FB8), Color(0xFFC7DCF2))
        "检验" -> TilePalette(Color(0xFF60527A), Color(0xFF8470A5), Color(0xFFE0D8F1))
        "分布" -> TilePalette(Color(0xFF7C5A37), Color(0xFFAF8356), Color(0xFFF0DFC3))
        "数据表格" -> TilePalette(Color(0xFF2D6B67), Color(0xFF4AA09A), Color(0xFFC5ECE7))
        "函数表格" -> TilePalette(Color(0xFF644D8D), Color(0xFF8A6DC1), Color(0xFFE1D7F7))
        "方程" -> TilePalette(Color(0xFF8A4E49), Color(0xFFBF746B), Color(0xFFF2D4CF))
        "不等式" -> TilePalette(Color(0xFF4A5A33), Color(0xFF738950), Color(0xFFDDE9C9))
        "复数" -> TilePalette(Color(0xFF425D86), Color(0xFF6A8FBE), Color(0xFFD4E1F4))
        "进制" -> TilePalette(Color(0xFF5C4C46), Color(0xFF8B7268), Color(0xFFEBD7CF))
        "矩阵" -> TilePalette(Color(0xFF0F4F53), Color(0xFF2D7A80), Color(0xFFB8E4E5))
        "向量" -> TilePalette(Color(0xFF275A65), Color(0xFF458593), Color(0xFFC7E7ED))
        "比例" -> TilePalette(Color(0xFF6C5A31), Color(0xFFA38A48), Color(0xFFF0E3BA))
        "全局设置", "设置" -> TilePalette(Color(0xFF47566E), Color(0xFF6B7D9E), Color(0xFFD6E0F0))
        "结果格式" -> TilePalette(Color(0xFF4B5D76), Color(0xFF728FB3), Color(0xFFD8E4F6))
        "单位换算" -> TilePalette(Color(0xFF3F675E), Color(0xFF5C998B), Color(0xFFCCECE4))
        "常数表" -> TilePalette(Color(0xFF65533C), Color(0xFF9A7D5B), Color(0xFFF2E0C8))
        else -> TilePalette(Color(0xFF4A6158), Color(0xFF78988E), Color(0xFFD9E8E2))
    }
}

private fun tileSection(title: String): String {
    return when (title) {
        "统计" -> "分析"
        "检验" -> "推断"
        "分布" -> "概率"
        "数据表格" -> "表格"
        "函数表格" -> "函数"
        "方程" -> "代数"
        "不等式" -> "求解"
        "复数" -> "复平面"
        "进制" -> "编码"
        "矩阵" -> "线代"
        "向量" -> "空间"
        "比例" -> "换算"
        "全局设置", "设置" -> "设置"
        "结果格式" -> "格式"
        "单位换算" -> "单位"
        "常数表" -> "常数"
        "计算", "通用计算" -> "表达式"
        else -> "模块"
    }
}

private val CalculatorPanelShape = RoundedCornerShape(26.dp)
private val CalculatorInnerShape = RoundedCornerShape(22.dp)
private val CalculatorChipShape = RoundedCornerShape(18.dp)

private fun tileIcon(title: String): ImageVector {
    return when (title) {
        "计算", "通用计算" -> Icons.Outlined.Calculate
        "统计" -> Icons.Outlined.ScatterPlot
        "检验" -> Icons.Outlined.Hub
        "分布" -> Icons.Outlined.BubbleChart
        "数据表格" -> Icons.Outlined.Dataset
        "函数表格" -> Icons.Outlined.Functions
        "方程" -> Icons.Outlined.Tune
        "不等式" -> Icons.Outlined.SquareFoot
        "复数" -> Icons.Outlined.Widgets
        "进制" -> Icons.Outlined.DataObject
        "矩阵" -> Icons.Outlined.ViewColumn
        "向量" -> Icons.Outlined.SwapHoriz
        "比例" -> Icons.Outlined.AccountTree
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
                            color = ForestDeep.copy(alpha = 0.76f),
                        )
                    }
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = PineInk)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
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
private fun CalculatorCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val palette = tilePalette(title)
    CalculatorGlassPanel {
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
                        color = ForestDeep.copy(alpha = 0.74f),
                    )
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, color = PineInk)
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
    val lines = text.lines().filter { it.isNotBlank() }
    Surface(
        shape = CalculatorPanelShape,
        color = Color.White.copy(alpha = 0.54f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BambooStroke.copy(alpha = 0.38f)),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.Transparent,
                            BambooGlass.copy(alpha = 0.18f),
                        ),
                    ),
                )
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("结果", style = MaterialTheme.typography.labelMedium, color = ForestDeep.copy(alpha = 0.62f))
                Surface(shape = CalculatorChipShape, color = Color.White.copy(alpha = 0.14f)) {
                    Text(
                        text = if (lines.size > 1) "${lines.size} 行" else "输出",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestDeep.copy(alpha = 0.72f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = PineInk,
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
    Surface(shape = RoundedCornerShape(18.dp), color = background) {
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
    CalculatorGlassPanel(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text("提示", style = MaterialTheme.typography.labelMedium, color = ForestDeep.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
        }
    }
}

@Composable
private fun MatrixSectionHeader(
    section: String,
    body: String,
) {
    CalculatorGlassPanel {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text("矩阵结构", style = MaterialTheme.typography.labelMedium, color = ForestDeep.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(section, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.74f))
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
    Text(title, style = MaterialTheme.typography.headlineSmall, color = PineInk)
}

@Composable
private fun CalculatorInlineSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
        Surface(shape = CalculatorChipShape, color = Color.White.copy(alpha = 0.14f)) {
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CalculatorInnerShape,
        color = Color.White.copy(alpha = 0.46f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BambooStroke.copy(alpha = 0.26f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = ForestDeep.copy(alpha = 0.7f),
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
    Surface(
        modifier = modifier,
        shape = CalculatorChipShape,
        color = Color.White.copy(alpha = 0.22f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ForestDeep.copy(alpha = 0.62f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = PineInk)
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
    var editorValue by remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    var builder by remember { mutableStateOf<String?>(null) }
    var editMode by remember { mutableStateOf<String?>(null) }
    var activeStructure by remember { mutableStateOf<FormulaStructure?>(null) }
    var slotA by remember { mutableStateOf("") }
    var slotB by remember { mutableStateOf("") }
    val structures = extractFormulaStructures(editorValue.text)
    val activeIndex = structures.indexOfFirst { it.key == activeStructure?.key }.let { if (it >= 0) it else -1 }
    Surface(
        shape = CalculatorPanelShape,
        color = Color.White.copy(alpha = 0.44f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = PineInk)
                Surface(shape = CalculatorChipShape, color = Color.White.copy(alpha = 0.18f)) {
                    Text(
                        text = "公式输入",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestDeep.copy(alpha = 0.74f),
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
            Surface(
                shape = CalculatorInnerShape,
                color = Color(0x162F7553),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = if (editorValue.text.isBlank()) "原式：等待输入" else "原式：${editorValue.text}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.68f),
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
                        text = "结构 ${activeIndex + 1}/${structures.size}，左右拖动结构块可排序",
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.72f),
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
                        editMode = null
                        activeStructure = null
                    },
                    onInsert = {
                        val inserted = buildStructuredFormula(builder.orEmpty(), slotA, slotB)
                        if (editMode != null && activeStructure != null) {
                            val replaced = replaceStructuredFormula(editorValue.text, activeStructure!!, slotA, slotB)
                            editorValue = TextFieldValue(replaced, TextRange(replaced.length))
                            onValueChange(replaced)
                        } else {
                            val next = insertAtCursor(editorValue, inserted)
                            editorValue = next
                            onValueChange(next.text)
                        }
                        builder = null
                        editMode = null
                        activeStructure = null
                        slotA = ""
                        slotB = ""
                    },
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
                        color = Color.White.copy(alpha = 0.52f),
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
                            color = PineInk,
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0x2234976B),
        border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.38f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = when (structure.mode) {
                    "a/b" -> "当前结构：分数"
                    "1/x" -> "当前结构：倒数"
                    else -> "当前结构：根式"
                },
                style = MaterialTheme.typography.titleMedium,
                color = PineInk,
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
                color = ForestDeep.copy(alpha = 0.76f),
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
            color = Color.White.copy(alpha = 0.34f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        ) {
            Text(
                text = tree,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.78f),
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
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = when (mode) {
                    "a/b" -> "结构输入：分数"
                    "1/x" -> "结构输入：倒数"
                    else -> "结构输入：根式"
                },
                style = MaterialTheme.typography.titleMedium,
                color = PineInk,
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
                color = ForestDeep.copy(alpha = 0.76f),
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
private fun StructuredFormulaPreview(
    value: String,
    activeKey: String?,
    onEditStructure: (FormulaStructure) -> Unit,
    onReorderStructure: (FormulaStructure, Int) -> Unit,
) {
    if (value.isBlank()) {
        Text(
            text = "排版预览：等待输入",
            style = MaterialTheme.typography.bodyMedium,
            color = PineInk,
        )
        return
    }
    val formatted = prettyFormatExpression(value)
    val lines = formatExpressionLines(formatted)
    Text(
        text = "排版预览",
        style = MaterialTheme.typography.bodyMedium,
        color = PineInk,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.28f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            lines.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PineInk,
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
                        onReorder = { onReorderStructure(structure, it) },
                    )
                    "1/x" -> FractionTemplateCard(
                        title = "倒数",
                        numerator = "1",
                        denominator = structure.slotA.ifBlank { "□" },
                        active = structure.key == activeKey,
                        onClick = { onEditStructure(structure) },
                        onReorder = { onReorderStructure(structure, it) },
                    )
                    "√" -> RadicalTemplateCard(
                        content = structure.slotA.ifBlank { "□" },
                        active = structure.key == activeKey,
                        onClick = { onEditStructure(structure) },
                        onReorder = { onReorderStructure(structure, it) },
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = glowColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) ForestGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f)),
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
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.68f))
            Spacer(modifier = Modifier.height(6.dp))
            Text(numerator, style = MaterialTheme.typography.bodyLarge, color = PineInk, onTextLayout = { numeratorPx = it.size.width })
            Box(
                modifier = Modifier
                    .width(adaptiveWidth)
                    .height(1.dp)
                    .background(ForestGreen.copy(alpha = 0.7f)),
            )
            Text(denominator, style = MaterialTheme.typography.bodyLarge, color = PineInk, onTextLayout = { denominatorPx = it.size.width })
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = glowColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (active) ForestGreen.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.2f)),
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text(text, style = MaterialTheme.typography.bodyLarge, color = PineInk)
            Text(power, style = MaterialTheme.typography.bodySmall, color = ForestGreen)
        }
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
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.42f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Spacer(modifier = Modifier.height(10.dp))
            repeat(size) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(size) { col ->
                        val index = offset + row * size + col
                        OutlinedTextField(
                            value = values[index],
                            onValueChange = { values[index] = it },
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
    Button(
        onClick = onClick,
        shape = CalculatorInnerShape,
        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) {
        Text(text, color = CloudWhite, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    accent: Boolean = false,
) {
    Surface(
        shape = CalculatorInnerShape,
        color = if (accent) ForestGreen.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.56f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
        modifier = Modifier
            .width(72.dp)
            .height(54.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (accent) CloudWhite else PineInk,
            )
        }
    }
}

@Composable
private fun InlineActionChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CalculatorChipShape,
        color = Color.White.copy(alpha = 0.46f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelLarge,
            color = PineInk,
        )
    }
}

@Composable
private fun ModuleSelector(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            Surface(
                shape = CalculatorChipShape,
                color = if (option == selected) ForestGreen else Color.White.copy(alpha = 0.52f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (option == selected) ForestGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.18f),
                ),
                modifier = Modifier.clickable { onSelect(option) },
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (option == selected) CloudWhite else PineInk,
                )
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
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.46f)) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = PineInk)
                }
                if (index != favorites.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
        if (recent.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CalculatorInlineSectionHeader("最近使用")
            Spacer(modifier = Modifier.height(6.dp))
            recent.forEachIndexed { index, item ->
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.42f)) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.bodyMedium, color = PineInk)
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.48f),
                modifier = Modifier.clickable {
                    if (favorites.contains(unit)) {
                        favorites.remove(unit)
                    } else {
                        favorites.add(unit)
                    }
                    savePreferenceList(prefs, "unit_pinned", favorites)
                },
            ) {
                Text(
                    text = if (favorites.contains(unit)) "$unit  · 已收藏" else unit,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PineInk,
                )
            }
            if (index != visible.lastIndex) Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ConstantsModule() {
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
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.44f)) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium, color = PineInk)
                }
                if (index != favorites.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (recent.isNotEmpty()) {
            CalculatorInlineSectionHeader("最近查看")
            Spacer(modifier = Modifier.height(6.dp))
            recent.forEachIndexed { index, item ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.40f)) {
                    Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), style = MaterialTheme.typography.bodyMedium, color = PineInk)
                }
                if (index != recent.lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        val visible = constants[group].orEmpty().filter { keyword.isBlank() || it.contains(keyword, ignoreCase = true) }
        visible.forEachIndexed { index, item ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.clickable {
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
            ) {
                Text(
                    text = if (favorites.contains(item)) "$item  · 已收藏" else item,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PineInk,
                )
            }
            if (index != visible.lastIndex) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@Composable
private fun ComputeModule(settings: CalculatorSettings) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("calculator_prefs", 0) }
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    var expression by remember { mutableStateOf(prefs.getString("compute_expression", "sin(0.5)+2^3/4").orEmpty()) }
    var result by remember { mutableStateOf(prefs.getString("compute_result", "点击计算").orEmpty()) }
    var ans by remember { mutableStateOf(0.0) }
    var memoryA by remember { mutableStateOf("0") }
    var memoryB by remember { mutableStateOf("0") }
    val history = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "compute_history")) } }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(loadPreferenceList(prefs, "compute_favorites")) } }
    val keypadRows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "()", "+"),
        listOf("Ans", "A", "B", "^"),
        listOf("sin(", "cos(", "tan(", "√"),
        listOf("%", "1/x", "x²", "x³"),
        listOf("x!", "a/b", "ln(", "log("),
    )
    val appendToken: (String) -> Unit = { token ->
        expression = appendFormulaToken(expression, token)
        prefs.edit().putString("compute_expression", expression).apply()
    }
    val evaluateExpression: () -> Unit = {
        result = runCatching {
            val value = ExpressionEngine.evaluate(
                expression = expression,
                angleMode = settings.angleMode,
                variables = mapOf(
                    "Ans" to ans,
                    "A" to memoryA.toDoubleOrNull().orZero(),
                    "B" to memoryB.toDoubleOrNull().orZero(),
                ),
            )
            ans = value
            val formatted = formatBySetting(value, settings)
            val line = "$expression = $formatted"
            history.remove(line)
            history.add(0, line)
            if (history.size > 6) history.removeAt(history.lastIndex)
            savePreferenceList(prefs, "compute_history", history)
            prefs.edit().putString("compute_result", formatted).putString("compute_expression", expression).apply()
            formatted
        }.getOrElse { it.message ?: "表达式无效" }
    }
    CalculatorCard("计算") {
        CalculatorMetricRow(
            "角度" to settings.angleMode.title,
            "格式" to settings.resultFormat.title,
            "位数" to "${settings.displayDigits}位",
            "历史" to "${history.size}条",
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "表达式工作区",
            subtitle = "直接编辑公式，支持 Ans / A / B 和常用科学函数。",
        ) {
            FormulaEditor(
                label = "数学输入行",
                value = expression,
                onValueChange = { expression = it },
                tokens = listOf("(", ")", "a/b", "√", "^", "x²", "x³", "x!", "sin(", "cos(", "tan(", "ln(", "log(", "Ans", "A", "B"),
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InlineActionChip("填入 Ans") { appendToken("Ans") }
                InlineActionChip("填入 π") { appendToken("pi") }
                InlineActionChip("清空") {
                    expression = ""
                    result = "点击计算"
                    prefs.edit().putString("compute_expression", expression).putString("compute_result", result).apply()
                }
                InlineActionChip("收藏当前") {
                    if (expression.isNotBlank()) {
                        val line = "$expression = $result"
                        favorites.remove(line)
                        favorites.add(0, line)
                        if (favorites.size > 8) favorites.removeAt(favorites.lastIndex)
                        savePreferenceList(prefs, "compute_favorites", favorites)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "变量区",
            subtitle = "A / B 会跟当前表达式一起参与计算。",
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = memoryA,
                    onValueChange = { memoryA = it },
                    label = { Text("变量 A") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                )
                OutlinedTextField(
                    value = memoryB,
                    onValueChange = { memoryB = it },
                    label = { Text("变量 B") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "计算键盘",
            subtitle = "数字、结构化符号和高频函数集中在这里。",
        ) {
            keypadRows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        KeypadButton(text = key, onClick = { appendToken(key) }, accent = key in listOf("÷", "×", "-", "+", "^"))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KeypadButton(text = "⌫", onClick = {
                    if (expression.isNotEmpty()) {
                        expression = expression.dropLast(1)
                        prefs.edit().putString("compute_expression", expression).apply()
                    }
                })
                KeypadButton(text = "C", onClick = {
                    expression = ""
                    result = "点击计算"
                    prefs.edit().putString("compute_expression", expression).putString("compute_result", result).apply()
                })
                KeypadButton(text = "π", onClick = { appendToken("pi") })
                KeypadButton(text = "=", onClick = evaluateExpression, accent = true)
            }
            Spacer(modifier = Modifier.height(10.dp))
            CalcButton("计算并记录", onClick = evaluateExpression)
        }
        if (favorites.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            CalculatorInsetPanel(
                title = "收藏表达式",
                subtitle = "保留常用计算，之后可直接回填。",
            ) {
                favorites.forEachIndexed { index, item ->
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.32f)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InlineActionChip("再次编辑") {
                                    expression = item.substringBefore("=").trim()
                                    prefs.edit().putString("compute_expression", expression).apply()
                                }
                                InlineActionChip("复制") {
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("calculator_favorite", item))
                                }
                            }
                        }
                    }
                    if (index != favorites.lastIndex) Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            CalculatorInsetPanel(
                title = "最近历史",
                subtitle = "保留最近计算结果，可再次编辑或加入收藏。",
            ) {
                history.forEachIndexed { index, item ->
                    Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.3f)) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(item, style = MaterialTheme.typography.bodyMedium, color = PineInk)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                InlineActionChip("再次编辑") {
                                    expression = item.substringBefore("=").trim()
                                    prefs.edit().putString("compute_expression", expression).apply()
                                }
                                InlineActionChip("复制") {
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("calculator_history", item))
                                }
                                InlineActionChip(if (favorites.contains(item)) "取消收藏" else "收藏") {
                                    if (favorites.contains(item)) {
                                        favorites.remove(item)
                                    } else {
                                        favorites.add(0, item)
                                        if (favorites.size > 8) favorites.removeAt(favorites.lastIndex)
                                    }
                                    savePreferenceList(prefs, "compute_favorites", favorites)
                                }
                            }
                        }
                    }
                    if (index != history.lastIndex) Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun StatisticsModule(settings: CalculatorSettings) {
    var mode by remember { mutableStateOf("单变量") }
    var raw by remember { mutableStateOf("85,90,92,76,88,95") }
    var rawY by remember { mutableStateOf("78,81,88,70,86,91") }
    var predictX by remember { mutableStateOf("100") }
    var result by remember { mutableStateOf("均值、方差、中位数将在这里显示") }
    val modeSummary = when (mode) {
        "单变量" -> "适合快速查看均值、离散程度和分布特征。"
        "线性回归" -> "基于 X/Y 数据建立线性趋势，并给出预测值。"
        "指数回归" -> "适合增长或衰减趋势，Y 需全部大于 0。"
        "对数回归" -> "适合前期变化快、后期趋缓的关系。"
        "幂回归" -> "适合比例缩放关系，X/Y 需全部大于 0。"
        else -> "使用二次曲线拟合拐点趋势，并支持预测。"
    }
    CalculatorCard("统计") {
        CalculatorMetricRow(
            "模式" to mode,
            "X 样本" to "${previewEntryCount(raw)}项",
            "Y 样本" to if (mode == "单变量") "未使用" else "${previewEntryCount(rawY)}项",
            "预测" to if (mode == "单变量") "关闭" else "x=$predictX",
        )
        Spacer(modifier = Modifier.height(12.dp))
        SectionLead(title = "统计工作区", body = modeSummary)
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "样本输入",
            subtitle = if (mode == "单变量") "输入一列样本即可开始分析。" else "回归模式需要成对的 X / Y 数据列。",
        ) {
            ModuleSelector(listOf("单变量", "线性回归", "指数回归", "对数回归", "幂回归", "二次回归"), mode) { mode = it }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = raw, onValueChange = { raw = it }, label = { Text("X / 单列样本") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            if (mode != "单变量") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = rawY, onValueChange = { rawY = it }, label = { Text("Y 数据列") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = predictX, onValueChange = { predictX = it }, label = { Text("预测 X") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("统计") {
            result = runCatching {
                val values = parseNumberList(raw)
                if (mode == "单变量") {
                    val mean = values.average()
                    val sorted = values.sorted()
                    val median = if (sorted.size % 2 == 0) (sorted[sorted.size / 2] + sorted[sorted.size / 2 - 1]) / 2.0 else sorted[sorted.size / 2]
                    val variance = values.map { (it - mean).pow(2) }.average()
                    val std = sqrt(variance)
                    val ci = meanConfidenceInterval(values)
                    val jb = jarqueBeraTest(values)
                    "数量 ${values.size}\n均值 ${formatBySetting(mean, settings)}\n中位数 ${formatBySetting(median, settings)}\n方差 ${formatBySetting(variance, settings)}\n标准差 ${formatBySetting(std, settings)}\n均值95%区间 [${formatBySetting(ci.first, settings)}, ${formatBySetting(ci.second, settings)}]\nJarque-Bera = ${formatBySetting(jb.first, settings)}  p≈${formatBySetting(jb.second, settings)}\n最小值 ${formatBySetting(values.minOrNull() ?: 0.0, settings)}\n最大值 ${formatBySetting(values.maxOrNull() ?: 0.0, settings)}"
                } else {
                    val yValues = parseNumberList(rawY)
                    require(values.size == yValues.size) { "X 与 Y 长度必须一致" }
                    val meanX = values.average()
                    val meanY = yValues.average()
                    val predictTarget = predictX.toDouble()
                    if (mode == "线性回归") {
                        val covariance = values.indices.sumOf { (values[it] - meanX) * (yValues[it] - meanY) } / values.size
                        val varianceX = values.map { (it - meanX).pow(2) }.average()
                        val varianceY = yValues.map { (it - meanY).pow(2) }.average()
                        val slope = covariance / varianceX
                        val intercept = meanY - slope * meanX
                        val correlation = covariance / (sqrt(varianceX) * sqrt(varianceY))
                        val predicted = values.map { slope * it + intercept }
                        regressionSummary(
                            equation = "回归方程 y = ${formatBySetting(slope, settings)}x + ${formatBySetting(intercept, settings)}",
                            xValues = values,
                            actual = yValues,
                            predicted = predicted,
                            forecast = slope * predictTarget + intercept,
                            forecastX = predictTarget,
                            settings = settings,
                            extra = "相关系数 r = ${formatBySetting(correlation, settings)}",
                            coefficients = listOf("β1" to slope, "β0" to intercept),
                            coefficientStats = linearCoefficientStats(values, yValues, slope, intercept, settings),
                            predictorCount = 1,
                        )
                    } else if (mode == "指数回归") {
                        require(yValues.all { it > 0 }) { "指数回归要求 Y 全部大于 0" }
                        val lnY = yValues.map { ln(it) }
                        val meanLnY = lnY.average()
                        val covariance = values.indices.sumOf { (values[it] - meanX) * (lnY[it] - meanLnY) } / values.size
                        val varianceX = values.map { (it - meanX).pow(2) }.average()
                        val b = covariance / varianceX
                        val a = exp(meanLnY - b * meanX)
                        val predicted = values.map { a * exp(b * it) }
                        regressionSummary(
                            equation = "回归方程 y = ${formatBySetting(a, settings)}e^(${formatBySetting(b, settings)}x)",
                            xValues = values,
                            actual = yValues,
                            predicted = predicted,
                            forecast = a * exp(b * predictTarget),
                            forecastX = predictTarget,
                            settings = settings,
                            coefficients = listOf("a" to a, "b" to b),
                            predictorCount = 1,
                        )
                    } else if (mode == "对数回归") {
                        require(values.all { it > 0 }) { "对数回归要求 X 全部大于 0" }
                        val lnX = values.map { ln(it) }
                        val meanLnX = lnX.average()
                        val covariance = values.indices.sumOf { (lnX[it] - meanLnX) * (yValues[it] - meanY) } / values.size
                        val varianceLnX = lnX.map { (it - meanLnX).pow(2) }.average()
                        val b = covariance / varianceLnX
                        val a = meanY - b * meanLnX
                        val predicted = values.map { a + b * ln(it) }
                        regressionSummary(
                            equation = "回归方程 y = ${formatBySetting(a, settings)} + ${formatBySetting(b, settings)}ln(x)",
                            xValues = values,
                            actual = yValues,
                            predicted = predicted,
                            forecast = a + b * ln(predictTarget),
                            forecastX = predictTarget,
                            settings = settings,
                            coefficients = listOf("a" to a, "b" to b),
                            predictorCount = 1,
                        )
                    } else if (mode == "幂回归") {
                        require(values.all { it > 0 } && yValues.all { it > 0 }) { "幂回归要求 X、Y 全部大于 0" }
                        val lnX = values.map { ln(it) }
                        val lnY = yValues.map { ln(it) }
                        val meanLnX = lnX.average()
                        val meanLnY = lnY.average()
                        val covariance = values.indices.sumOf { (lnX[it] - meanLnX) * (lnY[it] - meanLnY) } / values.size
                        val varianceLnX = lnX.map { (it - meanLnX).pow(2) }.average()
                        val b = covariance / varianceLnX
                        val a = exp(meanLnY - b * meanLnX)
                        val predicted = values.map { a * it.pow(b) }
                        regressionSummary(
                            equation = "回归方程 y = ${formatBySetting(a, settings)}x^${formatBySetting(b, settings)}",
                            xValues = values,
                            actual = yValues,
                            predicted = predicted,
                            forecast = a * predictTarget.pow(b),
                            forecastX = predictTarget,
                            settings = settings,
                            coefficients = listOf("a" to a, "b" to b),
                            predictorCount = 1,
                        )
                    } else {
                        val coeffs = quadraticRegression(values, yValues)
                        val predicted = values.map { coeffs[0] * it * it + coeffs[1] * it + coeffs[2] }
                        regressionSummary(
                            equation = "回归方程 y = ${formatBySetting(coeffs[0], settings)}x² + ${formatBySetting(coeffs[1], settings)}x + ${formatBySetting(coeffs[2], settings)}",
                            xValues = values,
                            actual = yValues,
                            predicted = predicted,
                            forecast = coeffs[0] * predictTarget * predictTarget + coeffs[1] * predictTarget + coeffs[2],
                            forecastX = predictTarget,
                            settings = settings,
                            coefficients = listOf("β2" to coeffs[0], "β1" to coeffs[1], "β0" to coeffs[2]),
                            predictorCount = 2,
                        )
                    }
                }
            }.getOrElse { it.message ?: "统计失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun HypothesisTestModule(settings: CalculatorSettings) {
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
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusTag(verdict)
            StatusTag(advice, subtle = true)
        }
        Spacer(modifier = Modifier.height(8.dp))
        ResultBlock(
            when (category) {
                "拟合优度" -> "结果说明：用于比较观测频数与期望频数是否一致。\n公式：χ² = Σ((O - E)² / E)"
                "分布检验" -> "结果说明：用于比较样本经验分布与目标分布之间的最大偏差。\n公式：D = sup|F_n(x) - F(x)|"
                else -> "结果说明：用于检验均值差异或样本均值与给定总体均值是否显著。\n公式：t = (样本均值差) / 标准误"
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        val modes = when (category) {
            "拟合优度" -> listOf("χ²检验")
            "分布检验" -> listOf("KS检验")
            else -> listOf("单样本t检验", "双样本t检验")
        }
        ModuleSelector(modes, mode) { mode = it }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(value = raw, onValueChange = { raw = it }, label = { Text("样本 / 观测频数") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        if (mode == "χ²检验" || mode == "双样本t检验") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = rawY, onValueChange = { rawY = it }, label = { Text(if (mode == "χ²检验") "期望频数" else "第二样本") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        }
        if (mode == "单样本t检验") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = parameter, onValueChange = { parameter = it }, label = { Text("检验均值 μ0") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            if (mode == "正态密度" || mode == "正态分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("x") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 μ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("标准差 σ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            } else if (mode == "区间正态") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("下界 a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("上界 b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 μ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("标准差 σ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            } else if (mode == "二项分布" || mode == "二项累积") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("试验次数 n") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("成功概率 p") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            } else if (mode == "泊松分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("事件次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("均值 λ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            } else if (mode == "几何分布") {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("成功前失败次数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功概率 p") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            } else {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("总体大小 N") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("成功总体数 K") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("抽样数 n") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("成功样本数 k") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
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
    val rows = remember { mutableStateListOf("12", "18", "21", "16", "25") }
    var result by remember { mutableStateOf("可录入 5 行数据并快速汇总") }
    CalculatorCard("数据表格") {
        rows.forEachIndexed { index, value ->
            OutlinedTextField(value = value, onValueChange = { rows[index] = it }, label = { Text("第 ${index + 1} 行") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
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
    var expression by remember { mutableStateOf("x^2+2*x+1") }
    var expressionG by remember { mutableStateOf("sin(x)") }
    var dual by remember { mutableStateOf(false) }
    var start by remember { mutableStateOf("-2") }
    var end by remember { mutableStateOf("2") }
    var step by remember { mutableStateOf("0.5") }
    var result by remember { mutableStateOf("将生成 x 与 y 的对应表") }
    CalculatorCard("函数表格") {
        ModuleSelector(listOf("单函数", "双函数"), if (dual) "双函数" else "单函数") { dual = it == "双函数" }
        Spacer(modifier = Modifier.height(8.dp))
        FormulaEditor(
            label = "f(x)",
            value = expression,
            onValueChange = { expression = it },
            tokens = listOf("x", "(", ")", "^", "sin(x)", "cos(x)", "tan(x)", "sqrt(", "ln(x)"),
        )
        if (dual) {
            Spacer(modifier = Modifier.height(8.dp))
            FormulaEditor(
                label = "g(x)",
                value = expressionG,
                onValueChange = { expressionG = it },
                tokens = listOf("x", "(", ")", "^", "sin(x)", "cos(x)", "tan(x)", "sqrt(", "ln(x)"),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("起点") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("终点") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = step, onValueChange = { step = it }, label = { Text("步长") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("生成") {
            result = runCatching {
                var x = start.toDouble()
                val target = end.toDouble()
                val delta = step.toDouble()
                require(delta > 0) { "步长需大于 0" }
                buildString {
                    while (x <= target + 1e-9) {
                        val fx = ExpressionEngine.evaluate(expression = expression, xValue = x, angleMode = settings.angleMode)
                        append("x=${formatBySetting(x, settings)}  f=${formatBySetting(fx, settings)}")
                        if (dual) {
                            val gx = ExpressionEngine.evaluate(expression = expressionG, xValue = x, angleMode = settings.angleMode)
                            append("  g=${formatBySetting(gx, settings)}")
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
@Composable
private fun EquationModule() {
    var mode by remember { mutableStateOf("多项式") }
    var degree by remember { mutableStateOf("2次") }
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("0") }
    var c by remember { mutableStateOf("-4") }
    var d by remember { mutableStateOf("1") }
    var e by remember { mutableStateOf("1") }
    var f by remember { mutableStateOf("2") }
    var result by remember { mutableStateOf("选择方程模式") }
    CalculatorCard("方程") {
        ModuleSelector(listOf("一元一次", "二元一次", "多项式"), mode) { mode = it }
        Spacer(modifier = Modifier.height(10.dp))
        when (mode) {
            "一元一次" -> {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(12.dp))
                CalcButton("求解") {
                    result = runCatching { "x = ${formatNumber(-b.toDouble() / a.toDouble())}" }.getOrElse { it.message ?: "求解失败" }
                }
            }
            "多项式" -> {
                ModuleSelector(listOf("2次", "3次", "4次"), degree) { degree = it }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                if (degree != "2次") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("d") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                }
                if (degree == "4次") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("e") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                CalcButton("求解") {
                    result = runCatching {
                        val coeffs = when (degree) {
                            "2次" -> listOf(a.toDouble(), b.toDouble(), c.toDouble())
                            "3次" -> listOf(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble())
                            else -> listOf(a.toDouble(), b.toDouble(), c.toDouble(), d.toDouble(), e.toDouble())
                        }
                        solvePolynomial(coeffs)
                    }.getOrElse { it.message ?: "求解失败" }
                }
            }
            else -> {
                OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a1") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b1") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c1") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("a2") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = e, onValueChange = { e = it }, label = { Text("b2") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = f, onValueChange = { f = it }, label = { Text("c2") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
                Spacer(modifier = Modifier.height(12.dp))
                CalcButton("求解") {
                    result = runCatching {
                        val a1 = a.toDouble(); val b1 = b.toDouble(); val c1 = c.toDouble(); val a2 = d.toDouble(); val b2 = e.toDouble(); val c2 = f.toDouble()
                        val det = a1 * b2 - a2 * b1
                        require(abs(det) > 1e-9) { "方程组无唯一解" }
                        val x = (c1 * b2 - c2 * b1) / det
                        val y = (a1 * c2 - a2 * c1) / det
                        "x = ${formatNumber(x)}\ny = ${formatNumber(y)}"
                    }.getOrElse { it.message ?: "求解失败" }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun InequalityModule() {
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("-3") }
    var c by remember { mutableStateOf("2") }
    var sign by remember { mutableStateOf("≥ 0") }
    var result by remember { mutableStateOf("求解 ax²+bx+c 与 0 的区间关系") }
    CalculatorCard("不等式") {
        OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ModuleSelector(listOf("≥ 0", "> 0", "≤ 0", "< 0"), sign) { sign = it }
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("求解") {
            result = runCatching { solveQuadraticInequality(a.toDouble(), b.toDouble(), c.toDouble(), sign) }.getOrElse { it.message ?: "求解失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun ComplexModule() {
    var a by remember { mutableStateOf("2") }
    var b by remember { mutableStateOf("3") }
    var c by remember { mutableStateOf("1") }
    var d by remember { mutableStateOf("-4") }
    var result by remember { mutableStateOf("复数运算结果将在这里显示") }
    CalculatorCard("复数") {
        OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("z1 实部") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("z1 虚部") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("z2 实部") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("z2 虚部") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("运算") {
            result = runCatching {
                val x1 = a.toDouble(); val y1 = b.toDouble(); val x2 = c.toDouble(); val y2 = d.toDouble()
                val add = "${formatNumber(x1 + x2)} + ${formatNumber(y1 + y2)}i"
                val mul = "${formatNumber(x1 * x2 - y1 * y2)} + ${formatNumber(x1 * y2 + y1 * x2)}i"
                val mod = sqrt(x1 * x1 + y1 * y1)
                val arg = atan(y1 / x1) * 180 / PI
                "z1+z2 = $add\nz1×z2 = $mul\n|z1| = ${formatNumber(mod)}\narg(z1) = ${formatNumber(arg)}°"
            }.getOrElse { it.message ?: "运算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun BaseModule() {
    var raw by remember { mutableStateOf("255") }
    var source by remember { mutableStateOf("10") }
    var result by remember { mutableStateOf("可在 2/8/10/16 进制间转换") }
    CalculatorCard("进制") {
        OutlinedTextField(value = raw, onValueChange = { raw = it }, label = { Text("输入值") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        ModuleSelector(listOf("2", "8", "10", "16"), source) { source = it }
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("转换") {
            result = runCatching {
                val value = raw.toLong(source.toInt())
                "BIN ${value.toString(2)}\nOCT ${value.toString(8)}\nDEC ${value.toString(10)}\nHEX ${value.toString(16).uppercase()}"
            }.getOrElse { it.message ?: "转换失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}
@Composable
private fun MatrixModule(settings: CalculatorSettings) {
    var section by remember { mutableStateOf("基础") }
    var mode by remember { mutableStateOf("行列式") }
    var dimension by remember { mutableStateOf("2x2") }
    val fields = remember {
        mutableStateListOf(
            "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "10", "11", "12", "13", "14", "15", "16", "17", "18",
        )
    }
    var rhs1 by remember { mutableStateOf("5") }
    var rhs2 by remember { mutableStateOf("6") }
    var rhs3 by remember { mutableStateOf("7") }
    var vx by remember { mutableStateOf("1") }
    var vy by remember { mutableStateOf("1") }
    var vz by remember { mutableStateOf("1") }
    var jordanLambda by remember { mutableStateOf("1") }
    var jordanLength by remember { mutableStateOf("2") }
    var activeJordanNode by remember { mutableStateOf(1) }
    var result by remember { mutableStateOf("支持 2x2 / 3x3 行列式、逆矩阵、乘法与线性方程组") }
    val firstLabels = if (dimension == "2x2") {
        listOf("A11", "A12", "A21", "A22")
    } else {
        listOf("A11", "A12", "A13", "A21", "A22", "A23", "A31", "A32", "A33")
    }
    val secondLabels = if (dimension == "2x2") {
        listOf("B11", "B12", "B21", "B22")
    } else {
        listOf("B11", "B12", "B13", "B21", "B22", "B23", "B31", "B32", "B33")
    }
    CalculatorCard("矩阵") {
        CalculatorMetricRow(
            "分栏" to section,
            "模式" to mode,
            "矩阵" to dimension,
            "工作区" to if (mode == "Jordan链视图") "结构分析" else "数值运算",
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "矩阵工作台",
            subtitle = "先选分栏、模式和维度，再录入矩阵或扩展参数。",
        ) {
            ModuleSelector(listOf("基础", "分解", "谱", "Jordan"), section) {
                section = it
                mode = when (it) {
                    "基础" -> "行列式"
                    "分解" -> "LU分解"
                    "谱" -> "特征值"
                    else -> "Jordan链视图"
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val sectionModes = when (section) {
                "基础" -> listOf("行列式", "迹", "逆矩阵", "伴随矩阵", "转置", "秩", "线性变换", "增广矩阵", "乘法", "线性方程组")
                "分解" -> listOf("LU分解", "QR分解")
                "谱" -> listOf("特征值", "特征向量", "相似对角化")
                else -> listOf("若当标准形", "Jordan链视图")
            }
            ModuleSelector(sectionModes, mode) { mode = it }
            Spacer(modifier = Modifier.height(8.dp))
            ModuleSelector(listOf("2x2", "3x3"), dimension) { dimension = it }
        }
        Spacer(modifier = Modifier.height(8.dp))
        MatrixSectionHeader(
            section = "${section}分栏",
            body = when (section) {
                "基础" -> "处理行列式、迹、逆、秩、线性变换与增广矩阵。"
                "分解" -> "聚焦 LU / QR 这类结构化分解，适合后续求解与分析。"
                "谱" -> "聚焦特征值、特征向量和相似对角化。"
                else -> "聚焦若当标准形与 Jordan 链编辑。"
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        MatrixGridEditor(
            title = "矩阵 A",
            size = if (dimension == "2x2") 2 else 3,
            labels = firstLabels,
            values = fields,
        )
        if (mode == "乘法") {
            Spacer(modifier = Modifier.height(10.dp))
            MatrixGridEditor(
                title = "矩阵 B",
                size = if (dimension == "2x2") 2 else 3,
                labels = secondLabels,
                values = fields,
                offset = 9,
            )
        }
        if (mode == "线性方程组") {
            Spacer(modifier = Modifier.height(10.dp))
            Text("右侧常数列", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
            Spacer(modifier = Modifier.height(6.dp))
            val rhsLabels = if (dimension == "2x2") listOf("b1", "b2") else listOf("b1", "b2", "b3")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rhsLabels.forEachIndexed { index, label ->
                    val value = when (index) {
                        0 -> rhs1
                        1 -> rhs2
                        else -> rhs3
                    }
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            when (index) {
                                0 -> rhs1 = it
                                1 -> rhs2 = it
                                else -> rhs3 = it
                            }
                        },
                        label = { Text(label) },
                        modifier = Modifier.width(if (dimension == "2x2") 150.dp else 96.dp),
                        shape = RoundedCornerShape(18.dp),
                    )
                }
            }
        }
        if (mode == "线性变换") {
            Spacer(modifier = Modifier.height(10.dp))
            Text("输入向量", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = vx, onValueChange = { vx = it }, label = { Text("x") }, modifier = Modifier.width(if (dimension == "2x2") 150.dp else 96.dp), shape = RoundedCornerShape(18.dp))
                OutlinedTextField(value = vy, onValueChange = { vy = it }, label = { Text("y") }, modifier = Modifier.width(if (dimension == "2x2") 150.dp else 96.dp), shape = RoundedCornerShape(18.dp))
                if (dimension == "3x3") {
                    OutlinedTextField(value = vz, onValueChange = { vz = it }, label = { Text("z") }, modifier = Modifier.width(96.dp), shape = RoundedCornerShape(18.dp))
                }
            }
        }
        if (mode == "Jordan链视图") {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Jordan 链编辑区", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.72f))
            Spacer(modifier = Modifier.height(6.dp))
            ResultBlock("说明：先固定特征值 λ，再构造 (A-λI)v1=0，随后逐步解 (A-λI)v(k+1)=v(k)。")
            Spacer(modifier = Modifier.height(8.dp))
            Text("当前矩阵 A", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.42f)) {
                Text(
                    text = if (dimension == "2x2") {
                        matrixToString(
                            listOf(
                                listOf(fields[0].toDoubleOrNull().orZero(), fields[1].toDoubleOrNull().orZero()),
                                listOf(fields[2].toDoubleOrNull().orZero(), fields[3].toDoubleOrNull().orZero()),
                            ),
                        )
                    } else {
                        matrixToString(
                            listOf(
                                listOf(fields[0].toDoubleOrNull().orZero(), fields[1].toDoubleOrNull().orZero(), fields[2].toDoubleOrNull().orZero()),
                                listOf(fields[3].toDoubleOrNull().orZero(), fields[4].toDoubleOrNull().orZero(), fields[5].toDoubleOrNull().orZero()),
                                listOf(fields[6].toDoubleOrNull().orZero(), fields[7].toDoubleOrNull().orZero(), fields[8].toDoubleOrNull().orZero()),
                            ),
                        )
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PineInk,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = jordanLambda, onValueChange = { jordanLambda = it }, label = { Text("特征值 λ") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ModuleSelector(if (dimension == "2x2") listOf("1", "2") else listOf("1", "2", "3"), jordanLength) { jordanLength = it }
            Spacer(modifier = Modifier.height(8.dp))
            Text("链长度可视化", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(jordanLength.toInt()) { index ->
                    val node = index + 1
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (node == activeJordanNode) ForestGreen else Color.White.copy(alpha = 0.52f),
                        modifier = Modifier.clickable { activeJordanNode = node },
                    ) {
                        Text(
                            text = "v$node",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (node == activeJordanNode) CloudWhite else PineInk,
                        )
                    }
                    if (index != jordanLength.toInt() - 1) {
                        Text("→", style = MaterialTheme.typography.titleMedium, color = ForestGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("步骤高亮", style = MaterialTheme.typography.bodyMedium, color = ForestDeep.copy(alpha = 0.66f))
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(jordanLength.toInt()) { index ->
                    val node = index + 1
                    val stepLabel = if (node == 1) {
                        "(A-λI)v1 = 0"
                    } else {
                        "(A-λI)v$node = v${node - 1}"
                    }
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (node == activeJordanNode) Color(0x332F8F63) else Color.White.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (node == activeJordanNode) ForestGreen.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.2f),
                        ),
                    ) {
                        Text(
                            text = if (node == activeJordanNode) "当前步骤：$stepLabel" else stepLabel,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = PineInk,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ResultBlock("当前聚焦节点：v$activeJordanNode。点击链节点可切换聚焦态，便于逐步查看链构造。")
        }
        CalcButton("计算") {
            result = runCatching {
                if (dimension == "2x2") {
                    val a11 = fields[0].toDouble(); val a12 = fields[1].toDouble(); val a21 = fields[2].toDouble(); val a22 = fields[3].toDouble()
                    val det = a11 * a22 - a12 * a21
                    when (mode) {
                        "行列式" -> "det = ${formatNumber(det)}"
                        "迹" -> "tr = ${formatNumber(a11 + a22)}"
                        "逆矩阵" -> if (abs(det) < 1e-9) "det = ${formatNumber(det)}\n矩阵不可逆" else "逆矩阵 = [[${formatNumber(a22 / det)}, ${formatNumber(-a12 / det)}], [${formatNumber(-a21 / det)}, ${formatNumber(a11 / det)}]]"
                        "伴随矩阵" -> matrixToString(adjugate2(listOf(listOf(a11, a12), listOf(a21, a22))))
                        "转置" -> matrixToString(transpose(listOf(listOf(a11, a12), listOf(a21, a22))))
                        "秩" -> "rank = ${matrixRank(listOf(listOf(a11, a12), listOf(a21, a22)))}"
                        "LU分解" -> luToString(luDecomposition(listOf(listOf(a11, a12), listOf(a21, a22))))
                        "QR分解" -> qrToString(qrDecomposition(listOf(listOf(a11, a12), listOf(a21, a22))))
                        "特征值" -> eigenValues2(listOf(listOf(a11, a12), listOf(a21, a22)))
                        "特征向量" -> eigenVectors2(listOf(listOf(a11, a12), listOf(a21, a22)))
                        "相似对角化" -> diagonalization2(listOf(listOf(a11, a12), listOf(a21, a22)))
                        "若当标准形" -> jordanForm2(listOf(listOf(a11, a12), listOf(a21, a22)))
                        "Jordan链视图" -> jordanChainView(listOf(listOf(a11, a12), listOf(a21, a22)), jordanLambda.toDouble(), jordanLength.toInt(), activeJordanNode)
                        "线性变换" -> matrixVectorToString(multiplyMatrixVector(listOf(listOf(a11, a12), listOf(a21, a22)), listOf(vx.toDouble(), vy.toDouble())))
                        "增广矩阵" -> augmentedMatrixToString(listOf(listOf(a11, a12), listOf(a21, a22)), listOf(rhs1.toDouble(), rhs2.toDouble()))
                        "线性方程组" -> {
                            require(abs(det) > 1e-9) { "方程组无唯一解" }
                            val b1 = rhs1.toDouble(); val b2 = rhs2.toDouble()
                            val x = (b1 * a22 - a12 * b2) / det
                            val y = (a11 * b2 - b1 * a21) / det
                            "x = ${formatNumber(x)}\ny = ${formatNumber(y)}"
                        }
                        else -> {
                            val b11 = fields[9].toDouble(); val b12 = fields[10].toDouble(); val b21 = fields[11].toDouble(); val b22 = fields[12].toDouble()
                            "[[${formatNumber(a11 * b11 + a12 * b21)}, ${formatNumber(a11 * b12 + a12 * b22)}], [${formatNumber(a21 * b11 + a22 * b21)}, ${formatNumber(a21 * b12 + a22 * b22)}]]"
                        }
                    }
                } else {
                    val matrix = listOf(
                        listOf(fields[0].toDouble(), fields[1].toDouble(), fields[2].toDouble()),
                        listOf(fields[3].toDouble(), fields[4].toDouble(), fields[5].toDouble()),
                        listOf(fields[6].toDouble(), fields[7].toDouble(), fields[8].toDouble()),
                    )
                    when (mode) {
                        "行列式" -> "det = ${formatNumber(det3(matrix))}"
                        "迹" -> "tr = ${formatNumber(matrix[0][0] + matrix[1][1] + matrix[2][2])}"
                        "逆矩阵" -> matrixToString(inverse3(matrix))
                        "伴随矩阵" -> matrixToString(adjugate3(matrix))
                        "转置" -> matrixToString(transpose(matrix))
                        "秩" -> "rank = ${matrixRank(matrix)}"
                        "LU分解" -> luToString(luDecomposition(matrix))
                        "QR分解" -> qrToString(qrDecomposition(matrix))
                        "特征值" -> eigenValues3(matrix)
                        "特征向量" -> eigenVectors3(matrix)
                        "相似对角化" -> diagonalization3(matrix)
                        "若当标准形" -> jordanForm3(matrix)
                        "Jordan链视图" -> jordanChainView(matrix, jordanLambda.toDouble(), jordanLength.toInt(), activeJordanNode)
                        "线性变换" -> matrixVectorToString(multiplyMatrixVector(matrix, listOf(vx.toDouble(), vy.toDouble(), vz.toDouble())))
                        "增广矩阵" -> augmentedMatrixToString(matrix, listOf(rhs1.toDouble(), rhs2.toDouble(), rhs3.toDouble()))
                        "线性方程组" -> {
                            val solution = solveLinear3(matrix, listOf(rhs1.toDouble(), rhs2.toDouble(), rhs3.toDouble()))
                            "x = ${formatNumber(solution[0])}\ny = ${formatNumber(solution[1])}\nz = ${formatNumber(solution[2])}"
                        }
                        else -> {
                            val matrixB = listOf(
                                listOf(fields[9].toDouble(), fields[10].toDouble(), fields[11].toDouble()),
                                listOf(fields[12].toDouble(), fields[13].toDouble(), fields[14].toDouble()),
                                listOf(fields[15].toDouble(), fields[16].toDouble(), fields[17].toDouble()),
                            )
                            matrixToString(multiply3(matrix, matrixB))
                        }
                    }
                }
            }.getOrElse { it.message ?: "矩阵计算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun VectorModule(settings: CalculatorSettings) {
    var mode by remember { mutableStateOf("点积") }
    var dimension by remember { mutableStateOf("3维") }
    var ax by remember { mutableStateOf("1") }
    var ay by remember { mutableStateOf("2") }
    var az by remember { mutableStateOf("3") }
    var bx by remember { mutableStateOf("2") }
    var by by remember { mutableStateOf("0") }
    var bz by remember { mutableStateOf("1") }
    var result by remember { mutableStateOf("支持 3D 点积、叉积、模长") }
    CalculatorCard("向量") {
        CalculatorMetricRow(
            "模式" to mode,
            "维度" to dimension,
            "向量 A" to if (dimension == "2维") "($ax, $ay)" else "($ax, $ay, $az)",
            "向量 B" to if (dimension == "2维") "($bx, $by)" else "($bx, $by, $bz)",
        )
        Spacer(modifier = Modifier.height(12.dp))
        SectionLead(
            title = "向量工作区",
            body = when (mode) {
                "点积" -> "查看两个向量的投影关系与同向程度。"
                "叉积" -> "查看法向量或二维有向面积。"
                "夹角" -> "根据当前角度制返回夹角结果。"
                else -> "同时输出 A、B 的模长。"
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        CalculatorInsetPanel(
            title = "向量输入",
            subtitle = "先选模式和维度，再录入 A / B 两组坐标。",
        ) {
            ModuleSelector(listOf("点积", "叉积", "夹角", "模长"), mode) { mode = it }
            Spacer(modifier = Modifier.height(8.dp))
            ModuleSelector(listOf("2维", "3维"), dimension) { dimension = it }
            Spacer(modifier = Modifier.height(8.dp))
            val editorItems = if (dimension == "2维") {
                listOf("A.x" to ax, "A.y" to ay, "B.x" to bx, "B.y" to by)
            } else {
                listOf("A.x" to ax, "A.y" to ay, "A.z" to az, "B.x" to bx, "B.y" to by, "B.z" to bz)
            }
            editorItems.chunked(2).forEachIndexed { rowIndex, rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEachIndexed { columnIndex, pair ->
                        val index = rowIndex * 2 + columnIndex
                        OutlinedTextField(
                            value = pair.second,
                            onValueChange = {
                                if (dimension == "2维") {
                                    when (index) {
                                        0 -> ax = it
                                        1 -> ay = it
                                        2 -> bx = it
                                        else -> by = it
                                    }
                                } else {
                                    when (index) {
                                        0 -> ax = it
                                        1 -> ay = it
                                        2 -> az = it
                                        3 -> bx = it
                                        4 -> by = it
                                        else -> bz = it
                                    }
                                }
                            },
                            label = { Text(pair.first) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(22.dp),
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIndex != editorItems.chunked(2).lastIndex) Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("计算") {
            result = runCatching {
                val av = if (dimension == "2维") listOf(ax.toDouble(), ay.toDouble(), 0.0) else listOf(ax.toDouble(), ay.toDouble(), az.toDouble())
                val bv = if (dimension == "2维") listOf(bx.toDouble(), by.toDouble(), 0.0) else listOf(bx.toDouble(), by.toDouble(), bz.toDouble())
                val dot = av.zip(bv).sumOf { it.first * it.second }
                val crossX = av[1] * bv[2] - av[2] * bv[1]
                val crossY = av[2] * bv[0] - av[0] * bv[2]
                val crossZ = av[0] * bv[1] - av[1] * bv[0]
                val normA = sqrt(av.sumOf { it * it })
                val normB = sqrt(bv.sumOf { it * it })
                when (mode) {
                    "点积" -> "A·B = ${formatBySetting(dot, settings)}"
                    "叉积" -> if (dimension == "2维") "A×B = ${formatBySetting(crossZ, settings)}" else "A×B = (${formatBySetting(crossX, settings)}, ${formatBySetting(crossY, settings)}, ${formatBySetting(crossZ, settings)})"
                    "夹角" -> {
                        val angle = acos((dot / (normA * normB)).coerceIn(-1.0, 1.0)) * 180 / PI
                        if (settings.angleMode == AngleMode.Deg) "夹角 = ${formatBySetting(angle, settings)}°" else "夹角 = ${formatBySetting(angle * PI / 180.0, settings)} rad"
                    }
                    else -> "|A| = ${formatBySetting(normA, settings)}\n|B| = ${formatBySetting(normB, settings)}"
                }
            }.getOrElse { it.message ?: "向量计算失败" }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ResultBlock(result)
    }
}

@Composable
private fun RatioModule() {
    var a by remember { mutableStateOf("3") }
    var b by remember { mutableStateOf("5") }
    var c by remember { mutableStateOf("9") }
    var mode by remember { mutableStateOf("正比例") }
    var result by remember { mutableStateOf("求第四比例项或缩放结果") }
    CalculatorCard("比例") {
        ModuleSelector(listOf("正比例", "反比例", "缩放"), mode) { mode = it }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text(if (mode == "缩放") "倍率" else "c") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp))
        Spacer(modifier = Modifier.height(12.dp))
        CalcButton("计算") {
            result = runCatching {
                val av = a.toDouble(); val bv = b.toDouble(); val cv = c.toDouble()
                when (mode) {
                    "正比例" -> "x = ${formatNumber(bv * cv / av)}"
                    "反比例" -> "x = ${formatNumber(av * bv / cv)}"
                    else -> "缩放后 = ${formatNumber(av * cv)} : ${formatNumber(bv * cv)}"
                }
            }.getOrElse { it.message ?: "比例计算失败" }
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
    }
}

private fun formatBySetting(value: Double, format: ResultFormat): String {
    return when (format) {
        ResultFormat.Standard -> formatNumber(value)
        ResultFormat.Fixed2 -> String.format("%.2f", value)
        ResultFormat.Scientific -> String.format("%.6e", value)
        ResultFormat.Engineering -> engineeringFormat(value)
    }
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
    val angle = prefs.getString("settings_angle", AngleMode.Rad.name).orEmpty()
    val format = prefs.getString("settings_format", ResultFormat.Standard.name).orEmpty()
    val digits = prefs.getInt("settings_digits", 6)
    val rounding = prefs.getString("settings_rounding", RoundingRule.HalfUp.name).orEmpty()
    return CalculatorSettings(
        angleMode = AngleMode.entries.firstOrNull { it.name == angle } ?: AngleMode.Rad,
        resultFormat = ResultFormat.entries.firstOrNull { it.name == format } ?: ResultFormat.Standard,
        displayDigits = digits.coerceIn(2, 10),
        roundingRule = RoundingRule.entries.firstOrNull { it.name == rounding } ?: RoundingRule.HalfUp,
    )
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

private object ExpressionEngine {
    fun evaluate(
        expression: String,
        xValue: Double = 0.0,
        angleMode: AngleMode = AngleMode.Rad,
        variables: Map<String, Double> = emptyMap(),
    ): Double {
        val parser = Parser(expression.replace("x", "($xValue)"), angleMode, variables)
        return parser.parse()
    }

    private class Parser(
        private val source: String,
        private val angleMode: AngleMode,
        private val variables: Map<String, Double>,
    ) {
        private var index = 0

        fun parse(): Double {
            val value = parseExpression()
            skipSpace()
            require(index == source.length) { "存在无法识别的字符" }
            return value
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipSpace()
                value = when {
                    match('+') -> value + parseTerm()
                    match('-') -> value - parseTerm()
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (true) {
                skipSpace()
                value = when {
                    match('*') -> value * parsePower()
                    match('/') -> value / parsePower()
                    else -> return value
                }
            }
        }

        private fun parsePower(): Double {
            var value = parseUnary()
            skipSpace()
            if (match('^')) value = value.pow(parsePower())
            return value
        }

        private fun parseUnary(): Double {
            skipSpace()
            return when {
                match('+') -> parseUnary()
                match('-') -> -parseUnary()
                else -> parsePostfix()
            }
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                skipSpace()
                value = when {
                    match('%') -> value / 100.0
                    match('!') -> factorial(value)
                    else -> return value
                }
            }
        }

        private fun parsePrimary(): Double {
            skipSpace()
            if (match('(')) {
                val value = parseExpression()
                require(match(')')) { "缺少右括号" }
                return value
            }
            if (peek().isLetter()) {
                val name = parseIdentifier()
                skipSpace()
                if (match('(')) {
                    val value = parseExpression()
                    require(match(')')) { "函数缺少右括号" }
                    return applyFunction(name, value)
                }
                return when (name.lowercase()) {
                    "pi" -> PI
                    "e" -> kotlin.math.E
                    else -> variables[name]?.orZero() ?: variables[name.uppercase()]?.orZero() ?: error("未知标识符 $name")
                }
            }
            return parseNumber()
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
                else -> error("未知函数 $name")
            }
        }

        private fun factorial(value: Double): Double {
            require(abs(value - round(value)) < 1e-9) { "阶乘要求整数" }
            require(value >= 0) { "阶乘要求非负整数" }
            val target = round(value).toInt()
            var result = 1.0
            for (i in 2..target) result *= i
            return result
        }

        private fun parseNumber(): Double {
            skipSpace()
            val start = index
            while (index < source.length && (source[index].isDigit() || source[index] == '.')) index++
            require(start != index) { "缺少数值" }
            return source.substring(start, index).toDouble()
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
