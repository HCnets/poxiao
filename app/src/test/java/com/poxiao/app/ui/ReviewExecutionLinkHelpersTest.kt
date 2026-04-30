package com.poxiao.app.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReviewExecutionLinkHelpersTest {
    @Test
    fun `buildReviewReplayDiffSummary describes reused added missing tasks and original binding`() {
        val summary = buildReviewReplayDiffSummary(
            sourceTitles = listOf("复习：高数", "复习：线代", "复习：英语"),
            replayTitles = listOf("复习：高数", "复习：英语", "复习：概率论"),
            sourceBoundTitle = "复习：高数",
            replayBoundTitle = "复习：高数",
        )

        assertEquals("与原方案相比：复用了 2 项，新增 1 项，缺少 1 项，已恢复原番茄绑定。", summary)
    }

    @Test
    fun `buildReviewReplayDiffSummary reports adjusted binding when replay binding changed`() {
        val summary = buildReviewReplayDiffSummary(
            sourceTitles = listOf("复习：高数"),
            replayTitles = listOf("复习：高数"),
            sourceBoundTitle = "复习：高数",
            replayBoundTitle = "复习：线代",
        )

        assertEquals("与原方案相比：复用了 1 项，番茄绑定已调整。", summary)
    }
}
