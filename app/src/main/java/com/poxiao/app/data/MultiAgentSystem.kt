package com.poxiao.app.data

import com.poxiao.app.ui.EditionCapabilities
import com.poxiao.app.ui.canShowCampus
import com.poxiao.app.ui.canShowSchedule

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 专家智能体接口：定义特定领域的处理逻辑
 */
interface ExpertAgent {
    val id: String
    val name: String
    val description: String
    val priority: Int // 处理优先级

    /**
     * 判断该智能体是否能处理当前请求
     */
    fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean

    /**
     * 执行具体逻辑（支持异步）
     */
    suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult
}

/**
 * 智能体执行结果
 */
data class AgentExecutionResult(
    val toolCall: AssistantToolCall?,
    val reply: String,
    val confidence: Float, // 置信度 0.0 - 1.0
    val handledBy: String // 哪个智能体处理的
)

/**
 * 智能体编排器：负责分发任务给最合适的专家
 */
class AgentOrchestrator(
    private val experts: List<ExpertAgent>
) {
    suspend fun coordinate(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState,
        capabilities: EditionCapabilities
    ): AssistantMockExecution = coroutineScope {
        // 1. 过滤符合当前版本能力的专家
        val availableExperts = experts.filter { expert ->
            when (expert.id) {
                "academic_expert" -> capabilities.canShowSchedule
                "campus_expert" -> capabilities.canShowCampus
                "deepseek_expert" -> true // DeepSeek 作为全能型专家全版本开启
                else -> true
            }
        }

        // 2. 并行执行专家逻辑
        val results = availableExperts
            .filter { it.canHandle(prompt, summaries) }
            .map { expert ->
                async { expert.execute(prompt, summaries, permissionState) }
            }
            .awaitAll()
            .sortedByDescending { it.confidence }

        val bestResult = results.firstOrNull()

        // 3. 专家间反思与润色协议 (MAS Reflection & Polish)
        // 使用 DeepSeek 专家作为“总设计师”进行二次加工
        val finalExecution = if (bestResult != null && bestResult.confidence < 0.9f) {
            val deepseek = availableExperts.find { it.id == "deepseek_expert" }
            if (deepseek != null) {
                val reflection = deepseek.execute(
                    prompt = "请基于以下回复进行反思和润色，使其更符合用户上下文：${bestResult.reply}",
                    summaries = summaries,
                    permissionState = permissionState
                )
                AssistantMockExecution(
                    toolCall = bestResult.toolCall,
                    reply = reflection.reply // 使用 DeepSeek 润色后的回复
                )
            } else {
                AssistantMockExecution(
                    toolCall = bestResult.toolCall,
                    reply = "【${bestResult.handledBy}】${bestResult.reply}"
                )
            }
        } else if (bestResult != null) {
            AssistantMockExecution(
                toolCall = bestResult.toolCall,
                reply = "【${bestResult.handledBy}】${bestResult.reply}"
            )
        } else {
            // ... 兜底逻辑
            AssistantMockExecution(
                toolCall = null,
                reply = "我已收到你的请求，但目前没有找到专门的领域专家来处理。我会作为通用助手为你记录。"
            )
        }

        // 4. 专家间协作协议 (Expert Collaboration)
        if (bestResult?.handledBy == "学务助手" && (prompt.contains("复习") || prompt.contains("待办"))) {
            val learningExpert = availableExperts.find { it.id == "learning_expert" }
            if (learningExpert != null) {
                val collaborationResult = learningExpert.execute(prompt, summaries, permissionState)
                return@coroutineScope AssistantMockExecution(
                    toolCall = bestResult.toolCall,
                    reply = "${finalExecution.reply}\n\n此外，${collaborationResult.reply}"
                )
            }
        }

        return@coroutineScope finalExecution
    }
}
