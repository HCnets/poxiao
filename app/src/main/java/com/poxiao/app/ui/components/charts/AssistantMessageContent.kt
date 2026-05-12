package com.poxiao.app.ui.components.charts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

/**
 * 智能体消息内容渲染组件（图表拦截器）。
 *
 * 自动检测消息文本中的 ```chart:radar``` 代码块，
 * 将其替换为 Canvas 雷达图组件渲染，而非作为纯代码文本展示。
 *
 * ## 使用方式
 * 替换原本的 `Text(text = message.content, ...)`：
 * ```kotlin
 * AssistantMessageContent(
 *     content = message.content,
 *     textColor = PineInk,
 *     textStyle = MaterialTheme.typography.bodyLarge,
 *     modifier = Modifier.padding(14.dp),
 * )
 * ```
 *
 * @param content   原始消息文本（可能包含 chart 代码块）
 * @param textColor 纯文本的颜色
 * @param textStyle 纯文本的样式
 * @param modifier  修饰符
 */
@Composable
fun AssistantMessageContent(
    content: String,
    textColor: androidx.compose.ui.graphics.Color,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    onActionClick: ((ParsedAction) -> Unit)? = null
) {
    val segments = AssistantMessageParser.parse(content)

    Column(modifier = modifier) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is MessageSegment.Text -> {
                    Text(
                        text = segment.content,
                        color = textColor,
                        style = textStyle,
                    )
                }

                is MessageSegment.Chart -> {
                    when (segment.chart.type) {
                        "radar" -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            RadarChart(
                                data = segment.chart.data,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        else -> {
                            Text(
                                text = "[图表:${segment.chart.type}] ${segment.chart.rawJson}",
                                color = textColor.copy(alpha = 0.5f),
                                style = textStyle,
                            )
                        }
                    }
                }
                
                is MessageSegment.Action -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    AssistantActionPill(
                        action = segment.action,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onActionClick?.invoke(segment.action) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (index < segments.lastIndex && segment is MessageSegment.Text) {
                // 文本段之间自然换行由 Text 自身处理
            }
        }
    }
}
