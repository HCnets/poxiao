package com.poxiao.app.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poxiao.app.ui.theme.PoxiaoThemeState
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 学情诊断雷达图组件。
 *
 * 基于 Canvas 自绘的多维能力分布雷达图（蜘蛛网图），
 * 用于展示学生在各学科/维度上的掌握程度。
 *
 * ## 使用示例
 * ```kotlin
 * RadarChart(
 *     data = mapOf("数学" to 0.8f, "英语" to 0.5f, "物理" to 0.9f),
 *     modifier = Modifier.fillMaxWidth(),
 * )
 * ```
 *
 * @param data     维度名称到数值的映射，值域 [0.0, 1.0]
 * @param modifier  修饰符
 */
@Composable
fun RadarChart(
    data: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return

    val palette = PoxiaoThemeState.palette

    // 将数据转为有序列表，保证渲染稳定
    val entries = data.entries.toList()
    val sides = entries.size

    // 颜色定义
    val gridColor = palette.cardBorder          // 网格线颜色
    val fillColor = palette.primary.copy(alpha = 0.2f)  // 填充底色
    val strokeColor = palette.primary                  // 数据边框
    val dotColor = palette.primary                     // 顶点圆点
    val labelColor = palette.ink.copy(alpha = 0.72f)   // 标签颜色

    val density = LocalDensity.current
    val labelSizePx = with(density) { 11.dp.toPx() }
    val dotRadiusDp = 3.dp
    val gridStrokeWidthDp = 0.5.dp
    val dataStrokeWidthDp = 2.dp
    val labelPaddingDp = 18.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        val canvasSizeDp = androidx.compose.ui.unit.min(maxWidth, maxHeight)
        val labelPadPx = with(density) { labelPaddingDp.toPx() }
        val gridStrokePx = with(density) { gridStrokeWidthDp.toPx() }
        val dataStrokePx = with(density) { dataStrokeWidthDp.toPx() }
        val dotRadiusPx = with(density) { dotRadiusDp.toPx() }

        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width.coerceAtMost(size.height) / 2f) - labelPadPx - labelSizePx
            val angleStep = (2f * PI.toFloat()) / sides

            // ── 1. 绘制网格（蜘蛛网） ──
            val gridLevels = 4
            for (level in 1..gridLevels) {
                val levelRadius = radius * level / gridLevels
                val gridPath = Path()
                for (i in 0 until sides) {
                    val angle = -PI.toFloat() / 2f + i * angleStep
                    val x = center.x + levelRadius * cos(angle)
                    val y = center.y + levelRadius * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(
                    path = gridPath,
                    color = gridColor,
                    style = Stroke(width = gridStrokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            // ── 2. 绘制轴线 ──
            for (i in 0 until sides) {
                val angle = -PI.toFloat() / 2f + i * angleStep
                val endX = center.x + radius * cos(angle)
                val endY = center.y + radius * sin(angle)
                drawLine(
                    color = gridColor,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = gridStrokePx,
                )
            }

            // ── 3. 绘制数据填充区域 ──
            val dataPath = Path()
            val dataPoints = mutableListOf<Offset>()
            for (i in 0 until sides) {
                val angle = -PI.toFloat() / 2f + i * angleStep
                val value = entries[i].value.coerceIn(0f, 1f)
                val pointRadius = radius * value
                val x = center.x + pointRadius * cos(angle)
                val y = center.y + pointRadius * sin(angle)
                dataPoints.add(Offset(x, y))
                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // 填充
            drawPath(path = dataPath, color = fillColor)
            // 描边
            drawPath(
                path = dataPath,
                color = strokeColor,
                style = Stroke(width = dataStrokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // ── 4. 绘制顶点圆点 ──
            for (point in dataPoints) {
                drawCircle(
                    color = dotColor,
                    radius = dotRadiusPx,
                    center = point,
                )
            }

            // ── 5. 绘制维度标签 ──
            for (i in 0 until sides) {
                val angle = -PI.toFloat() / 2f + i * angleStep
                val labelRadius = radius + labelPadPx * 0.6f
                val labelX = center.x + labelRadius * cos(angle)
                val labelY = center.y + labelRadius * sin(angle)
                val label = entries[i].key

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    labelX,
                    labelY + labelSizePx / 3f,  // 垂直居中微调
                    android.graphics.Paint().apply {
                        color = labelColor.toArgb()
                        textSize = labelSizePx
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    },
                )
            }
        }

        // ── 中心数值标注 ──
        Text(
            text = buildString {
                entries.take(3).forEach { (key, value) ->
                    append("${key} ${(value * 100).toInt()}%  ")
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = palette.ink.copy(alpha = 0.45f),
            modifier = Modifier.align(Alignment.Center).padding(top = 6.dp),
        )
    }
}
