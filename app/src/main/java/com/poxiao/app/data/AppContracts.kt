package com.poxiao.app.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
)

data class AssistantCapability(
    val voiceEnabled: Boolean = true,
    val textEnabled: Boolean = true,
    val imageEnabled: Boolean = true,
)

data class CourseItem(
    val id: String,
    val title: String,
    val teacher: String,
    val classroom: String,
    val weekday: Int,
    val startMinute: Int,
    val endMinute: Int,
)

data class TodoItem(
    val id: String,
    val title: String,
    val quadrant: String,
    val linkedCourseId: String? = null,
    val checkedInToday: Boolean = false,
)

data class PomodoroSession(
    val id: String,
    val title: String,
    val minutes: Int,
    val mode: String,
)

data class LedgerRecord(
    val id: String,
    val bookName: String,
    val amount: Double,
    val paymentMethod: String,
    val category: String,
)

data class FeedCard(
    val id: String,
    val title: String,
    val source: String,
    val description: String,
)

data class CampusLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

enum class AssistantProviderType {
    PREVIEW,
    SPARK,
    DEEPSEEK,
}

interface HiagentGateway {
    suspend fun sendText(message: String, history: List<ChatMessage> = emptyList()): ChatMessage
    suspend fun sendVoice(audioUri: String): ChatMessage
    suspend fun sendImage(imageUri: String, prompt: String): ChatMessage
    fun capability(): AssistantCapability
    fun providerType(): AssistantProviderType
}

interface AcademicGateway {
    suspend fun importFromHitas(userToken: String): List<CourseItem>
    suspend fun getEmptyClassrooms(day: String): List<FeedCard>
    suspend fun getGrades(): List<FeedCard>
}

interface CampusMapGateway {
    suspend fun locateMe(): CampusLocation?
    suspend fun searchNearbyUsers(): List<CampusLocation>
    suspend fun buildRoute(destination: CampusLocation): String
}

interface MarketplaceGateway {
    suspend fun listBooks(): List<FeedCard>
    suspend fun publishBook(card: FeedCard)
}

class PreviewAcademicGateway : AcademicGateway {
    override suspend fun importFromHitas(userToken: String): List<CourseItem> {
        delay(260)
        return listOf(
            CourseItem("course-1", "智能系统导论", "林老师", "B201", 2, 14 * 60, 15 * 60 + 45),
            CourseItem("course-2", "交互设计", "陈老师", "A305", 1, 8 * 60 + 30, 10 * 60 + 15),
        )
    }

    override suspend fun getEmptyClassrooms(day: String): List<FeedCard> {
        delay(220)
        return listOf(
            FeedCard("empty-a", "A305 当前空闲", "教学楼 A", "距离你最近，直到 10:20 前可用"),
            FeedCard("empty-b", "B201 14:00 后可用", "教学楼 B", "适合小组讨论，投影可用"),
        )
    }

    override suspend fun getGrades(): List<FeedCard> {
        delay(220)
        return listOf(
            FeedCard("grade-1", "智能系统导论", "成绩速览", "当前成绩 91，平时成绩已更新"),
            FeedCard("grade-2", "交互设计", "考试安排", "下周三 15:30，地点 A102"),
        )
    }
}

class PreviewCampusMapGateway : CampusMapGateway {
    override suspend fun locateMe(): CampusLocation {
        delay(180)
        return CampusLocation(
            id = "preview-location",
            name = "主教学楼",
            latitude = 45.741,
            longitude = 126.684,
        )
    }

    override suspend fun searchNearbyUsers(): List<CampusLocation> {
        delay(180)
        return listOf(
            CampusLocation("near-1", "理学楼", 45.742, 126.685),
            CampusLocation("near-2", "图书馆", 45.740, 126.682),
        )
    }

    override suspend fun buildRoute(destination: CampusLocation): String {
        delay(160)
        return "从主教学楼步行至 ${destination.name}，预计 6 分钟。"
    }
}

class PreviewMarketplaceGateway : MarketplaceGateway {
    override suspend fun listBooks(): List<FeedCard> {
        delay(220)
        return listOf(
            FeedCard("book-1", "高等数学教材", "二手书市", "同校自提，成色 9 成新"),
            FeedCard("book-2", "考研英语真题", "二手书市", "附笔记，可校内面交"),
        )
    }

    override suspend fun publishBook(card: FeedCard) {
        delay(180)
    }
}

interface LocalSyncRepository {
    fun observeCourses(): Flow<List<CourseItem>>
    fun observeTodos(): Flow<List<TodoItem>>
    fun observePomodoros(): Flow<List<PomodoroSession>>
    fun observeLedger(): Flow<List<LedgerRecord>>
}

class PreviewHiagentGateway : HiagentGateway {
    override suspend fun sendText(message: String, history: List<ChatMessage>): ChatMessage {
        delay(320)
        return ChatMessage(
            id = "assistant-${System.currentTimeMillis()}",
            role = "assistant",
            content = "已收到：$message。当前是本地预览智能体入口，后续可直接替换为真实接口。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendVoice(audioUri: String): ChatMessage {
        delay(320)
        return ChatMessage(
            id = "assistant-voice-${System.currentTimeMillis()}",
            role = "assistant",
            content = "语音入口已预留，当前路径为 $audioUri。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override suspend fun sendImage(imageUri: String, prompt: String): ChatMessage {
        delay(320)
        return ChatMessage(
            id = "assistant-image-${System.currentTimeMillis()}",
            role = "assistant",
            content = "图像入口已预留，提示词为：$prompt。",
            timestamp = System.currentTimeMillis(),
        )
    }

    override fun capability(): AssistantCapability = AssistantCapability()

    override fun providerType(): AssistantProviderType = AssistantProviderType.PREVIEW
}

class PreviewLocalSyncRepository : LocalSyncRepository {
    private val courses = MutableStateFlow(
        listOf(
            CourseItem("c1", "交互设计", "陈老师", "A305", 1, 8 * 60 + 30, 10 * 60 + 15),
            CourseItem("c2", "智能系统导论", "林老师", "B201", 2, 14 * 60, 15 * 60 + 45),
        ),
    )
    private val todos = MutableStateFlow(
        listOf(
            TodoItem("t1", "完成实验报告", "重要且紧急", "c2", false),
            TodoItem("t2", "整理本周课堂笔记", "重要不紧急", null, true),
        ),
    )
    private val pomodoros = MutableStateFlow(
        listOf(
            PomodoroSession("p1", "阅读论文", 25, "倒计时"),
            PomodoroSession("p2", "复盘课程", 15, "正计时"),
        ),
    )
    private val ledger = MutableStateFlow(
        listOf(
            LedgerRecord("l1", "日常账本", 18.0, "校园卡", "餐饮"),
            LedgerRecord("l2", "设备账本", 129.0, "微信", "学习用品"),
        ),
    )

    override fun observeCourses(): Flow<List<CourseItem>> = courses

    override fun observeTodos(): Flow<List<TodoItem>> = todos

    override fun observePomodoros(): Flow<List<PomodoroSession>> = pomodoros

    override fun observeLedger(): Flow<List<LedgerRecord>> = ledger
}
