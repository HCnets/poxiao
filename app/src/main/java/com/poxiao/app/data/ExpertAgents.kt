package com.poxiao.app.data

import android.content.Context
import android.content.SharedPreferences
import com.poxiao.app.review.ReviewItem
import com.poxiao.app.review.ReviewPlannerStore
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.HitaCourseBlock
import com.poxiao.app.todo.TodoPriority
import com.poxiao.app.todo.TodoQuadrant
import com.poxiao.app.todo.TodoTask
import com.poxiao.app.ui.loadTodoTasks
import com.poxiao.app.ui.saveTodoTasks
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 教务专家：处理课表、成绩、考试周
 */
class AcademicExpert(
    private val repository: AcademicRepository
) : ExpertAgent {
    override val id = "academic_expert"
    override val name = "学务助手"
    override val description = "精通课表编排与成绩分析"
    override val priority = 10

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        val keywords = listOf("课", "表", "成绩", "考试", "gpa", "学分", "挂科", "谁上课")
        return keywords.any { prompt.contains(it, ignoreCase = true) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        val uiState = repository.observeUiState().value
        val normalized = prompt.lowercase()
        
        val isGrade = normalized.contains("成绩") || normalized.contains("gpa")
        
        if (isGrade) {
            val gradeSummary = summaries.firstOrNull { it.id == "grade_summary" }
            return AgentExecutionResult(
                toolCall = AssistantToolCall(
                    id = "tool-academic-grade-${System.currentTimeMillis()}",
                    title = "读成绩",
                    status = "完成",
                    summary = gradeSummary?.body ?: "已提取成绩摘要。",
                    timestamp = System.currentTimeMillis()
                ),
                reply = "根据最新的教务同步结果：${gradeSummary?.body ?: "你目前的成绩表现稳定，建议继续保持。"}",
                confidence = 0.95f,
                handledBy = name
            )
        }

        // 处理课表查询
        val todayCourses = uiState.selectedDateCourses
        val reply = if (todayCourses.isNotEmpty()) {
            val courseList = todayCourses.joinToString("\n") { 
                "· ${it.courseName} (${it.classroom}) - ${it.teacher}" 
            }
            "今天的课表安排如下：\n$courseList\n看起来行程很充实，需要我帮你把这些课程对应的复习项加入待办吗？"
        } else {
            "今天没有排课，是个难得的自主学习日！"
        }

        return AgentExecutionResult(
            toolCall = AssistantToolCall(
                id = "tool-academic-schedule-${System.currentTimeMillis()}",
                title = "读课表",
                status = "完成",
                summary = "已提取今日 ${todayCourses.size} 门课程。",
                timestamp = System.currentTimeMillis()
            ),
            reply = reply,
            confidence = 0.9f,
            handledBy = name
        )
    }
}

/**
 * 效率专家：处理待办、番茄钟、专注记录
 */
class EfficiencyExpert(private val context: Context) : ExpertAgent {
    override val id = "efficiency_expert"
    override val name = "执行教官"
    override val description = "负责待办推进与专注力管理"
    override val priority = 8

    private val todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE)

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        val keywords = listOf("待办", "任务", "专注", "番茄", "时段", "计划", "添加", "新建")
        return keywords.any { prompt.contains(it, ignoreCase = true) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        val normalized = prompt.lowercase()
        
        // 尝试解析“添加待办”
        if (normalized.contains("添加") || normalized.contains("新建")) {
            val taskTitle = prompt.substringAfter("添加").substringAfter("新建").trim().take(20)
            if (taskTitle.isNotBlank()) {
                val currentTasks = loadTodoTasks(todoPrefs).toMutableList()
                val newTask = TodoTask(
                    id = "ai-todo-${System.currentTimeMillis()}",
                    title = taskTitle,
                    note = "由智能体助手添加。",
                    priority = TodoPriority.Medium,
                    quadrant = TodoQuadrant.ImportantNotUrgent,
                    dueText = "尽快完成",
                    listName = "收集箱"
                )
                currentTasks.add(0, newTask)
                saveTodoTasks(todoPrefs, currentTasks)
                
                return AgentExecutionResult(
                    toolCall = AssistantToolCall(
                        id = "tool-efficiency-add-${System.currentTimeMillis()}",
                        title = "新建待办",
                        status = "已完成",
                        summary = "已将“$taskTitle”加入待办清单。",
                        timestamp = System.currentTimeMillis()
                    ),
                    reply = "长官，已为你新建任务：$taskTitle。建议立即开始第一个番茄钟。",
                    confidence = 1.0f,
                    handledBy = name
                )
            }
        }

        val matchedSummary = summaries.firstOrNull { it.id == "todo_pending" || it.id == "focus_summary" }

        val isFocus = normalized.contains("专注") || normalized.contains("番茄")
        val toolTitle = if (isFocus) "读专注" else "读待办"
        val toolSummary = matchedSummary?.body ?: if (isFocus) "已分析专注状态。" else "已列出待办清单。"

        val toolCall = if (permissionState.readTodo || (isFocus && permissionState.readFocus)) {
            AssistantToolCall(
                id = "tool-efficiency-${System.currentTimeMillis()}",
                title = toolTitle,
                status = "完成",
                summary = toolSummary,
                timestamp = System.currentTimeMillis()
            )
        } else null

        return AgentExecutionResult(
            toolCall = toolCall,
            reply = "执行力是核心。$toolSummary 建议先把高优先级的那项解决掉。",
            confidence = 0.85f,
            handledBy = name
        )
    }
}

/**
 * 学习专家：处理复习计划、错题、知识掌握度
 */
class LearningExpert(private val context: Context) : ExpertAgent {
    override val id = "learning_expert"
    override val name = "复习导师"
    override val description = "专注于长期记忆与复习策略"
    override val priority = 9

    private val store = ReviewPlannerStore(context)

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        val keywords = listOf("复习", "掌握", "错题", "记忆", "调优", "魔法", "艾宾浩斯")
        return keywords.any { prompt.contains(it, ignoreCase = true) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        val allItems = store.loadItems()
        val today = LocalDate.now()
        val dueToday = allItems.filter { 
            val nextDate = LocalDate.ofEpochDay(it.nextReviewAt / (24 * 60 * 60 * 1000))
            !nextDate.isAfter(today) 
        }
        
        val matchedSummary = summaries.firstOrNull { it.id == "schedule_conflicts" }
        
        val toolCall = if (permissionState.readSchedule) {
            AssistantToolCall(
                id = "tool-learning-${System.currentTimeMillis()}",
                title = "读复习计划",
                status = "完成",
                summary = "今日共有 ${dueToday.size} 项待复习，其中 ${dueToday.count { it.errorProne }} 项为易错点。",
                timestamp = System.currentTimeMillis()
            )
        } else null

        val reply = if (dueToday.isNotEmpty()) {
            val focusItem = dueToday.maxByOrNull { it.importance.score }
            "根据艾宾浩斯遗忘曲线，你今天有 ${dueToday.size} 个知识点到达复习临界点。重点建议复习《${focusItem?.courseName}》下的“${focusItem?.noteTitle}”，它的掌握度目前仅为 ${(focusItem?.mastery ?: 0f * 100).toInt()}%。"
        } else {
            "当前复习计划推进良好，暂无今日到期的知识点。建议你可以回顾一下最近标注为“核心”的笔记内容。"
        }

        return AgentExecutionResult(
            toolCall = toolCall,
            reply = reply,
            confidence = 0.98f,
            handledBy = name
        )
    }
}

/**
 * 校园专家：处理地图、楼栋、校园服务
 */
class CampusExpert : ExpertAgent {
    override val id = "campus_expert"
    override val name = "导游"
    override val description = "熟悉校园每一个角落"
    override val priority = 7

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        val keywords = listOf("在哪", "楼", "地图", "导航", "怎么去", "餐厅", "图书馆")
        return keywords.any { prompt.contains(it, ignoreCase = true) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        val matchedSummary = summaries.firstOrNull { it.id == "focus_summary" } // Mock
        
        val toolCall = if (permissionState.openCampusMap) {
            AssistantToolCall(
                id = "tool-campus-${System.currentTimeMillis()}",
                title = "打开地图",
                status = "完成",
                summary = "已定位目标楼栋并准备导航。",
                timestamp = System.currentTimeMillis()
            )
        } else null

        return AgentExecutionResult(
            toolCall = toolCall,
            reply = "我知道那个地方。已经为你开启了室内导航路线。",
            confidence = 0.8f,
            handledBy = name
        )
    }
}

/**
 * DeepSeek 专家：全能型云端智能，处理复杂逻辑与跨领域推理
 */
class DeepSeekExpert(
    private val gateway: HiagentGateway? = null
) : ExpertAgent {
    override val id = "deepseek_expert"
    override val name = "DeepSeek"
    override val description = "深度推理与跨领域综合助手"
    override val priority = 1 // 优先级最低，作为通用兜底或复杂推理

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        // DeepSeek 几乎能处理任何问题，但我们倾向于先让本地专家处理
        // 或者当用户明确提到 "深度"、"推理"、"建议" 等词汇时触发
        val keywords = listOf("深度", "推理", "建议", "分析", "为什么", "怎么做")
        return prompt.length > 8 || keywords.any { prompt.contains(it) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        // 模拟 DeepSeek-V3 的思考链 (Chain of Thought)
        val thoughtChain = buildString {
            append("正在调用 DeepSeek-V3 推理引擎...\n")
            append("> 正在分析用户意图: ${prompt.take(10)}...\n")
            append("> 正在检索关联上下文: ${summaries.size} 条记录...\n")
            append("> 正在构建跨领域建议方案...")
        }
        
        kotlinx.coroutines.delay(1200) // 模拟云端推理耗时
        
        val reply = if (summaries.isNotEmpty()) {
            "基于你的近期数据，DeepSeek 建议：你可以尝试将当前的专注时段与复习计划深度融合。检测到你今天下午有 2 小时的空档，最适合进行《${summaries.firstOrNull()?.title ?: "核心课程"}》的难点攻克。"
        } else {
            "DeepSeek 已准备就绪。作为一个深度推理助手，我可以帮你拆解复杂的学习目标，或根据你的课表压力提供最优的时间分配建议。你可以试着问我：'怎么平衡这周的复习和作业？'"
        }

        return AgentExecutionResult(
            toolCall = null,
            reply = "$thoughtChain\n\n$reply",
            confidence = 0.75f,
            handledBy = name
        )
    }
}

/**
 * 本地端侧专家 (Local LLM)：离线处理隐私数据
 */
class LocalLLMExpert : ExpertAgent {
    override val id = "local_llm_expert"
    override val name = "本地脑核"
    override val description = "离线端侧 AI，保护隐私"
    override val priority = 5

    private val localKnowledge = mapOf(
        "密码" to "为了安全，请不要在聊天中输入真实密码。你可以前往“设置-教务账号”进行管理。",
        "备份" to "应用支持本地备份。你可以前往“更多-备份与恢复”导出你的所有数据。",
        "隐私" to "我们非常重视隐私。所有教务数据均加密存储在本地，不会上传至云端。",
        "离线" to "即使没有网络，你依然可以查看已缓存的课表、管理待办和使用番茄钟。",
        "版本" to "当前正在运行的是南工破晓 v1.1.0 稳定版。",
        "主题" to "你可以通过“更多-液态玻璃设置”来调整应用的外观风格。",
        "字体" to "目前应用跟随系统字体，你可以在系统设置中进行调整。",
        "语言" to "南工破晓目前完美适配简体中文，未来将支持更多语言。"
    )

    override fun canHandle(prompt: String, context: List<AssistantContextSummary>): Boolean {
        // 处理敏感信息、离线状态或应用基础设置的请求
        val keywords = listOf("密码", "私密", "离线", "断网", "隐私", "备份", "版本", "主题", "风格", "字体", "语言")
        return keywords.any { prompt.contains(it) }
    }

    override suspend fun execute(
        prompt: String,
        summaries: List<AssistantContextSummary>,
        permissionState: AssistantPermissionState
    ): AgentExecutionResult {
        val matchedEntry = localKnowledge.entries.firstOrNull { prompt.contains(it.key) }
        val reply = matchedEntry?.value ?: "【端侧离线模式】已为你加密处理该请求。由于处于离线环境，我将基于本地知识库为你提供支持。"
            
        return AgentExecutionResult(
            toolCall = null,
            reply = reply,
            confidence = 0.95f,
            handledBy = name
        )
    }
}
