package com.poxiao.app.ui.components.charts

import org.json.JSONObject

data class ParsedAction(
    val type: String,
    val rawJson: String,
    val payload: JSONObject,
)

sealed interface MessageSegment {
    data class Text(val content: String) : MessageSegment
    data class Chart(val chart: ParsedChart) : MessageSegment
    data class Action(val action: ParsedAction) : MessageSegment
}

object AssistantMessageParser {

    private val BLOCK_REGEX = Regex(
        """```(chart:radar|action:\w+)\s*\n(.*?)\n\s*```""",
        setOf(RegexOption.DOT_MATCHES_ALL),
    )

    fun parse(content: String): List<MessageSegment> {
        val segments = mutableListOf<MessageSegment>()
        val matches = BLOCK_REGEX.findAll(content).toList()

        if (matches.isEmpty()) {
            if (content.isNotBlank()) {
                segments.add(MessageSegment.Text(content))
            }
            return segments
        }

        var lastEnd = 0
        for (match in matches) {
            if (match.range.first > lastEnd) {
                val textBefore = content.substring(lastEnd, match.range.first).trim()
                if (textBefore.isNotBlank()) {
                    segments.add(MessageSegment.Text(textBefore))
                }
            }

            val blockType = match.groupValues.getOrElse(1) { "" }.trim()
            val jsonStr = match.groupValues.getOrElse(2) { "" }.trim()

            if (blockType == "chart:radar") {
                val parsedChart = parseRadarChart(jsonStr)
                if (parsedChart != null) {
                    segments.add(MessageSegment.Chart(parsedChart))
                } else {
                    segments.add(MessageSegment.Text(match.value))
                }
            } else if (blockType.startsWith("action:")) {
                val actionType = blockType.removePrefix("action:")
                val parsedAction = parseAction(actionType, jsonStr)
                if (parsedAction != null) {
                    segments.add(MessageSegment.Action(parsedAction))
                } else {
                    segments.add(MessageSegment.Text(match.value))
                }
            } else {
                segments.add(MessageSegment.Text(match.value))
            }

            lastEnd = match.range.last + 1
        }

        if (lastEnd < content.length) {
            val textAfter = content.substring(lastEnd).trim()
            if (textAfter.isNotBlank()) {
                segments.add(MessageSegment.Text(textAfter))
            }
        }

        return segments
    }

    private fun parseRadarChart(jsonStr: String): ParsedChart? {
        return runCatching {
            val cleanJsonStr = jsonStr.replace(Regex("```json\\s*"), "").replace("```", "").trim()
            val root = JSONObject(cleanJsonStr)
            val data = mutableMapOf<String, Float>()
            root.keys().forEach { key ->
                val rawValue = root.opt(key)
                val value = if (rawValue is Number) {
                    rawValue.toFloat().coerceIn(0f, 1f)
                } else {
                    root.optDouble(key, 0.0).toFloat().coerceIn(0f, 1f)
                }
                if (value > 0f) {
                    data[key] = value
                }
            }
            if (data.isEmpty()) return null
            ParsedChart(type = "radar", rawJson = cleanJsonStr, data = data)
        }.getOrNull()
    }

    private fun parseAction(type: String, jsonStr: String): ParsedAction? {
        return runCatching {
            val cleanJsonStr = jsonStr.replace(Regex("```json\\s*"), "").replace("```", "").trim()
            val root = JSONObject(cleanJsonStr)
            ParsedAction(type = type, rawJson = cleanJsonStr, payload = root)
        }.getOrNull()
    }
}
