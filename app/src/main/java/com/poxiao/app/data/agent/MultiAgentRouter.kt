package com.poxiao.app.data.agent

import com.poxiao.app.data.AssistantProviderType

/**
 * 多智能体路由中枢 (Multi-Agent Router)。
 *
 * 根据用户输入的语义关键词进行轻量级意图分类，
 * 将请求分发给最合适的大模型客户端处理。
 *
 * ## 路由规则
 * - **DeepSeek**：复杂推理场景（高数、推导、代码、分析、建议、为什么、怎么做 等）
 * - **星火 Spark**：轻量场景（课表、闲聊、日常、待办、番茄 等）
 * - **兜底**：默认路由至星火
 *
 * @param sparkClient     星火大模型客户端
 * @param deepSeekClient  DeepSeek 大模型客户端
 */
class MultiAgentRouter(
    private val sparkClient: AgentClient,
    private val deepSeekClient: AgentClient,
) {
    /**
     * 根据意图路由并执行流式对话。
     *
     * 当用户意图涉及学情诊断 / 能力分布时，自动注入图表 System Prompt，
     * 引导大模型输出 ```chart:radar``` 格式的可视化数据。
     *
     * @param messages  对话消息列表
     * @param onChunk   流式文本回调
     * @return Result.success(完整回复文本) 或 Result.failure(异常)
     */
    suspend fun route(
        messages: List<Message>,
        onChunk: (String) -> Unit,
    ): RouteResult {
        val userMessage = messages.lastOrNull { it.role == "user" }?.content.orEmpty()
        val target = classify(userMessage)

        // ── v1.10.0: 场景感知自动注入 System Prompt (图表/动作) ──
        var augmentedMessages = messages
        if (shouldInjectChartPrompt(userMessage)) {
            augmentedMessages = injectSystemPrompt(augmentedMessages, CHART_SYSTEM_PROMPT)
        } else if (shouldInjectActionPrompt(userMessage)) {
            augmentedMessages = injectSystemPrompt(augmentedMessages, ACTION_SYSTEM_PROMPT)
        }

        val result = when (target) {
            AssistantProviderType.DEEPSEEK -> deepSeekClient.chat(augmentedMessages, onChunk)
            else -> sparkClient.chat(augmentedMessages, onChunk)
        }

        return RouteResult(
            provider = target,
            content = result.getOrNull().orEmpty(),
            error = result.exceptionOrNull()?.message,
        )
    }

    /**
     * 基于规则的轻量级意图分类器。
     *
     * @param prompt 用户输入的原始文本
     * @return 应该处理该请求的模型提供商类型
     */
    fun classify(prompt: String): AssistantProviderType {
        val normalized = prompt.lowercase().trim()
        if (normalized.isBlank()) return AssistantProviderType.SPARK

        // ── DeepSeek 路由关键词：复杂推理 / 代码 / 数学推导 ──
        val deepSeekKeywords = listOf(
            // 数学与推导
            "高数", "高等数学", "微积分", "线性代数", "概率论",
            "推导", "证明", "求解", "方程", "定理", "极限",
            "导数", "积分", "矩阵", "特征值", "微分",
            // 编程与代码
            "代码", "编程", "算法", "debug", "调试",
            "数据结构", "leetcode", "编译", "报错",
            "python", "java", "kotlin", "c++", "函数",
            // 深度分析
            "分析", "推理", "为什么", "怎么做", "原理",
            "深度", "逻辑", "归纳", "总结建议", "帮我看看",
            "优化建议", "对比", "区别", "优缺点",
        )

        // ── 星火路由关键词：轻量 / 闲聊 / 校园生活 ──
        val sparkKeywords = listOf(
            "课表", "课程", "上课", "教室",
            "闲聊", "聊天", "你好", "嗨", "哈哈",
            "天气", "吃饭", "食堂",
            "待办", "任务", "番茄", "专注",
            "复习", "考试", "成绩",
            "图书馆", "地图", "导航",
        )

        // 优先匹配 DeepSeek 关键词（复杂推理优先级更高）
        for (keyword in deepSeekKeywords) {
            if (normalized.contains(keyword)) {
                return AssistantProviderType.DEEPSEEK
            }
        }

        // 其次匹配星火关键词
        for (keyword in sparkKeywords) {
            if (normalized.contains(keyword)) {
                return AssistantProviderType.SPARK
            }
        }

        // 兜底：短问题（≤6 字）通常为闲聊，路由至星火
        if (prompt.trim().length <= 6) {
            return AssistantProviderType.SPARK
        }

        // 中等长度问题倾向于星火（更稳定、更快）
        // 长问题（>30 字）倾向于 DeepSeek（需要深度理解）
        return if (prompt.trim().length > 30) {
            AssistantProviderType.DEEPSEEK
        } else {
            AssistantProviderType.SPARK
        }
    }

    /**
     * 获取当前路由器中注册的所有客户端。
     */
    fun clients(): Map<AssistantProviderType, AgentClient> = mapOf(
        AssistantProviderType.SPARK to sparkClient,
        AssistantProviderType.DEEPSEEK to deepSeekClient,
    )

    // ──── v1.10.0: 图表与动作 System Prompt 注入逻辑 ────

    /**
     * 图表输出引导 System Prompt。
     *
     * 引导大模型在学情诊断、能力分布等场景下输出可视化雷达图数据。
     * 格式：```chart:radar\n{"维度1": 值, "维度2": 值}\n```
     */
    companion object {
        val CHART_SYSTEM_PROMPT = """
你是一个学习诊断助手。当用户询问学习状态、能力分布、成绩分析、掌握程度等问题时，
你可以输出一个雷达图（Radar Chart）来可视化数据。
请结合【当前本地上下文】中提供的成绩或专注意据进行分析。如果找不到数据，请根据当前日程或待办给出合理的预测。

雷达图输出格式（必须严格遵循以下 Markdown 结构，不要嵌套其他代码块，不要输出 null）：
```chart:radar
{"维度1": 0.8, "维度2": 0.5, "维度3": 0.9}
```

格式要求：
- 第一行必须是 ```chart:radar
- 第二行是一个合法的 JSON 对象，键为维度名称（如"数学"、"英语"等），值为 0.0 到 1.0 之间的浮点数。不允许使用 null！
- 最后一行必须是 ```
- JSON 必须在一行或多行内闭合，且不能包含 markdown 的嵌套如 ```json
- 数值越大表示掌握程度越高（例如成绩 91 分可以映射为 0.91）

示例：
根据你的最近数据，以下是各学科掌握度分析：
```chart:radar
{"高等数学": 0.85, "程序设计": 0.91, "大学物理": 0.58}
```
从雷达图可以看出，你的程序设计和高数掌握较好，但物理还有提升空间。
        """.trimIndent()

        val ACTION_SYSTEM_PROMPT = """
你是一个高效的个人管家。当用户要求你创建任务、安排番茄钟、添加待办时，你可以直接输出操作指令 (Action)，前端会自动渲染为可交互的按钮供用户点击。

请严格使用以下格式之一（必须是合法的 JSON，不要嵌套 ```json 标签）：

1. 新建待办：
```action:create_todo
{"title": "任务名称", "priority": "High"}
```
(priority 可选：High, Medium, Low)

2. 开始专注 (番茄钟)：
```action:start_focus
{"title": "专注任务名称", "minutes": 25}
```

示例：
用户："帮我建一个复习高数的待办"
你："好的，已经为你准备了待办卡片，点击即可添加：
```action:create_todo
{"title": "复习高数", "priority": "High"}
```"
        """.trimIndent()

        /** 触发图表注入的关键词集合 */
        private val CHART_TRIGGER_KEYWORDS = listOf(
            "学情", "能力", "分布", "掌握", "雷达",
            "成绩分布", "各科", "学科分析", "学习报告",
            "强项", "弱项", "短板", "擅长", "不擅长",
            "评估", "诊断", "综合", "多维度",
        )

        /** 触发动作指令的关键词集合 */
        private val ACTION_TRIGGER_KEYWORDS = listOf(
            "新建", "添加", "创建", "安排", "计划",
            "待办", "任务", "番茄", "专注", "自习",
            "提醒", "记一下"
        )
    }

    /**
     * 判断用户意图是否需要注入图表 System Prompt。
     */
    private fun shouldInjectChartPrompt(userMessage: String): Boolean {
        val normalized = userMessage.lowercase().trim()
        return CHART_TRIGGER_KEYWORDS.any { normalized.contains(it) }
    }

    /**
     * 判断用户意图是否需要注入动作 System Prompt。
     */
    private fun shouldInjectActionPrompt(userMessage: String): Boolean {
        val normalized = userMessage.lowercase().trim()
        return ACTION_TRIGGER_KEYWORDS.any { normalized.contains(it) }
    }

    /**
     * 在消息列表头部注入 System Prompt。
     * 如果已存在 system 消息，则替换；否则插入到最前面。
     */
    private fun injectSystemPrompt(messages: List<Message>, promptText: String): List<Message> {
        val systemMsg = Message(role = "system", content = promptText)
        val existingSystemIndex = messages.indexOfFirst { it.role == "system" }
        return if (existingSystemIndex >= 0) {
            messages.toMutableList().apply {
                this[existingSystemIndex] = systemMsg
            }
        } else {
            listOf(systemMsg) + messages
        }
    }
}

/**
 * 路由结果封装。
 *
 * @param provider  实际处理请求的模型提供商
 * @param content   完整的回复文本（成功时）
 * @param error     错误信息（失败时，成功时为 null）
 */
data class RouteResult(
    val provider: AssistantProviderType,
    val content: String,
    val error: String? = null,
) {
    val isSuccess: Boolean get() = error == null && content.isNotBlank()
}
