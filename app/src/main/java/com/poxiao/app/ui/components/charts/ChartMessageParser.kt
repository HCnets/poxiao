package com.poxiao.app.ui.components.charts

import org.json.JSONObject

/**
 * 聊天消息中的图表代码块解析结果。
 *
 * @param type      图表类型，目前支持 "radar"
 * @param rawJson   原始 JSON 字符串
 * @param data      解析后的维度→数值映射，值域 [0.0, 1.0]
 */
data class ParsedChart(
    val type: String,
    val rawJson: String,
    val data: Map<String, Float>,
)

/**
 * 消息内容分段。
 *
 * 将一条消息的内容拆分为纯文本段和图表段的有序序列，
 * 渲染时按顺序依次绘制即可。
 */
sealed interface MessageSegment {
    data class Text(val content: String) : MessageSegment
    data class Chart(val chart: ParsedChart) : MessageSegment
}

/**
 * 图表消息解析器。
 *
 * 从聊天消息的纯文本内容中识别并提取 ```chart:radar``` 代码块，
 * 将其解析为 [ParsedChart] 结构，其余文本保留为纯文本段。
 *
 * ## 支持的格式
 * ````
 * ```chart:radar
 * {"数学": 0.8, "英语": 0.5, "物理": 0.9}
 * ```
 * ````
 */
object ChartMessageParser {

    /**
     * chart:radar 代码块正则：
     * 匹配 ```chart:radar\n{...json...}\n``` 格式
     */
    private val CHART_RADAR_REGEX = Regex(
        """```chart:radar\s*\n(.*?)\n\s*```""",
        setOf(RegexOption.DOT_MATCHES_ALL),
    )

    /**
     * 将消息内容解析为有序的 [MessageSegment] 列表。
     *
     * @param content 原始消息文本
     * @return 文本段与图表段交替的有序列表
     */
    fun parse(content: String): List<MessageSegment> {
        val segments = mutableListOf<MessageSegment>()
        val matches = CHART_RADAR_REGEX.findAll(content).toList()

        if (matches.isEmpty()) {
            // 无图表，整段为纯文本
            if (content.isNotBlank()) {
                segments.add(MessageSegment.Text(content))
            }
            return segments
        }

        var lastEnd = 0
        for (match in matches) {
            // 匹配前的内容作为纯文本段
            if (match.range.first > lastEnd) {
                val textBefore = content.substring(lastEnd, match.range.first).trim()
                if (textBefore.isNotBlank()) {
                    segments.add(MessageSegment.Text(textBefore))
                }
            }

            // 解析 JSON 图表数据
            val jsonStr = match.groupValues.getOrElse(1) { "" }.trim()
            val parsedChart = parseRadarChart(jsonStr)
            if (parsedChart != null) {
                segments.add(MessageSegment.Chart(parsedChart))
            } else {
                // 解析失败时，将原始代码块作为纯文本保留
                segments.add(MessageSegment.Text(match.value))
            }

            lastEnd = match.range.last + 1
        }

        // 匹配后的剩余内容
        if (lastEnd < content.length) {
            val textAfter = content.substring(lastEnd).trim()
            if (textAfter.isNotBlank()) {
                segments.add(MessageSegment.Text(textAfter))
            }
        }

        return segments
    }

    /**
     * 尝试解析 JSON 为雷达图数据。
     *
     * @param jsonStr 形如 {"维度1": 0.8, "维度2": 0.5} 的 JSON
     * @return 解析成功返回 [ParsedChart]，失败返回 null
     */
    private fun parseRadarChart(jsonStr: String): ParsedChart? {
        return runCatching {
            // 清理可能被大模型错误嵌套的 Markdown json 标签
            val cleanJsonStr = jsonStr.replace(Regex("```json\\s*"), "")
                .replace("```", "")
                .trim()
                
            val root = JSONObject(cleanJsonStr)
            val data = mutableMapOf<String, Float>()
            root.keys().forEach { key ->
                // 防止 null 或者 NaN 导致异常
                val rawValue = root.opt(key)
                val value = if (rawValue is Number) {
                    rawValue.toFloat().coerceIn(0f, 1f)
                } else {
                    root.optDouble(key, 0.0).toFloat().coerceIn(0f, 1f)
                }
                if (value > 0f) { // 过滤掉 0 或者是解析失败导致全是 0 的情况
                    data[key] = value
                }
            }
            if (data.isEmpty()) return null
            ParsedChart(
                type = "radar",
                rawJson = cleanJsonStr,
                data = data,
            )
        }.getOrNull()
    }
}
