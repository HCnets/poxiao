package com.poxiao.app.ui

import com.poxiao.app.data.FeedCard

internal fun formatTrendValue(value: Double?): String {
    return if (value == null || value == 0.0) "--" else String.format("%.2f", value)
}

internal fun formatPercent(count: Int?, total: Int?): String {
    if (count == null || total == null || total <= 0) return "--"
    return "${(count * 100 / total)}%"
}

internal fun buildGradeTrendPoints(cardsByTerm: List<Pair<String, List<FeedCard>>>): List<GradeTrendPoint> {
    return cardsByTerm.map { (termName, cards) ->
        val parsed = cards.map { parseGradeCard(it) }
        val scores = parsed.mapNotNull { it.score }
        val gradePoints = parsed.mapNotNull { it.gradePoint }
        val credits = parsed.mapNotNull { it.credits }
        GradeTrendPoint(
            termName = termName,
            averageScore = if (scores.isEmpty()) 0.0 else scores.average(),
            averageGradePoint = if (gradePoints.isEmpty()) 0.0 else gradePoints.average(),
            credits = credits.sum(),
            excellentCount = parsed.count { (it.score ?: 0.0) >= 90.0 },
            warningCount = parsed.count {
                val score = it.score
                score == null || score < 60.0
            },
            courseCount = cards.size,
            rawCards = cards,
        )
    }
}

private data class ParsedGradeCard(
    val score: Double?,
    val gradePoint: Double?,
    val credits: Double?,
)

private fun parseGradeCard(card: FeedCard): ParsedGradeCard {
    return ParsedGradeCard(
        score = extractNumericValue(card.source, "总评"),
        gradePoint = extractNumericValue(card.source, "绩点"),
        credits = extractNumericValue(card.description, "学分"),
    )
}

private fun extractNumericValue(text: String, key: String): Double? {
    val afterKey = text.substringAfter(key, "").trim()
    if (afterKey.isBlank()) return null
    val token = afterKey.takeWhile { it.isDigit() || it == '.' }
    return token.toDoubleOrNull()
}
