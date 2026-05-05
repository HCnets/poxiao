package com.poxiao.app.data

import android.content.Context
import com.poxiao.app.ui.EditionCapabilities
import com.poxiao.app.ui.canShowCampus
import com.poxiao.app.ui.canShowGradeSearch
import com.poxiao.app.ui.canShowSchedule
import com.poxiao.app.schedule.AcademicRepository

data class AssistantPermissionState(
    val readSchedule: Boolean = true,
    val readTodo: Boolean = true,
    val readFocus: Boolean = true,
    val readGrades: Boolean = true,
    val readMap: Boolean = true,
    val createTodo: Boolean = true,
    val bindPomodoro: Boolean = true,
    val openCampusMap: Boolean = true,
)

data class AssistantToolDefinition(
    val id: String,
    val title: String,
    val description: String,
)

class AssistantPermissionStore(context: Context) {
    private val prefs = context.getSharedPreferences("assistant_permissions", Context.MODE_PRIVATE)

    fun load(): AssistantPermissionState {
        return AssistantPermissionState(
            readSchedule = prefs.getBoolean("read_schedule", true),
            readTodo = prefs.getBoolean("read_todo", true),
            readFocus = prefs.getBoolean("read_focus", true),
            readGrades = prefs.getBoolean("read_grades", true),
            readMap = prefs.getBoolean("read_map", true),
            createTodo = prefs.getBoolean("create_todo", true),
            bindPomodoro = prefs.getBoolean("bind_pomodoro", true),
            openCampusMap = prefs.getBoolean("open_campus_map", true),
        )
    }

    fun save(state: AssistantPermissionState) {
        prefs.edit()
            .putBoolean("read_schedule", state.readSchedule)
            .putBoolean("read_todo", state.readTodo)
            .putBoolean("read_focus", state.readFocus)
            .putBoolean("read_grades", state.readGrades)
            .putBoolean("read_map", state.readMap)
            .putBoolean("create_todo", state.createTodo)
            .putBoolean("bind_pomodoro", state.bindPomodoro)
            .putBoolean("open_campus_map", state.openCampusMap)
            .apply()
    }
}

data class AssistantMockExecution(
    val toolCall: AssistantToolCall?,
    val reply: String,
)

class AssistantToolKit(
    private val context: Context,
    private val repository: AcademicRepository
) {
    private val orchestrator = AgentOrchestrator(
        listOf(
            AcademicExpert(repository),
            EfficiencyExpert(context),
            LearningExpert(context),
            CampusExpert(),
            DeepSeekExpert(),
            LocalLLMExpert()
        )
    )

    fun availableTools(
        permissionState: AssistantPermissionState,
        capabilities: EditionCapabilities,
    ): List<AssistantToolDefinition> {
        return buildList {
            if (permissionState.readSchedule && capabilities.canShowSchedule) {
                add(
                    AssistantToolDefinition(
                        "read_schedule",
                        "\u8BFB\u8BFE\u8868",
                        "\u8BFB\u53D6\u4ECA\u65E5\u8BFE\u7A0B\u3001\u5F53\u524D\u5468\u8BFE\u8868\u4E0E\u8003\u8BD5\u5468\u6458\u8981",
                    ),
                )
            }
            if (permissionState.readTodo) {
                add(
                    AssistantToolDefinition(
                        "read_todo",
                        "\u8BFB\u5F85\u529E",
                        "\u8BFB\u53D6\u5F85\u529E\u4F18\u5148\u9879\u3001\u76EE\u6807\u8FDB\u5EA6\u4E0E\u6E05\u5355\u6458\u8981",
                    ),
                )
            }
            if (permissionState.readFocus) {
                add(
                    AssistantToolDefinition(
                        "read_focus",
                        "\u8BFB\u4E13\u6CE8",
                        "\u8BFB\u53D6\u4E13\u6CE8\u8BB0\u5F55\u3001\u5F53\u524D\u7ED1\u5B9A\u4EFB\u52A1\u4E0E\u8FBE\u6210\u60C5\u51B5",
                    ),
                )
            }
            if (permissionState.readGrades && capabilities.canShowGradeSearch) {
                add(
                    AssistantToolDefinition(
                        "read_grades",
                        "\u8BFB\u6210\u7EE9",
                        "\u8BFB\u53D6\u6210\u7EE9\u6458\u8981\u4E0E\u6700\u8FD1\u540C\u6B65\u7F13\u5B58",
                    ),
                )
            }
            if (permissionState.readMap && capabilities.canShowCampus) {
                add(
                    AssistantToolDefinition(
                        "read_map",
                        "\u8BFB\u5730\u56FE",
                        "\u8BFB\u53D6\u5730\u56FE\u5FEB\u6377\u70B9\u4F4D\u4E0E\u6700\u8FD1\u8BBF\u95EE\u5730\u70B9",
                    ),
                )
            }
            if (permissionState.createTodo) {
                add(
                    AssistantToolDefinition(
                        "create_todo",
                        "\u5EFA\u5F85\u529E",
                        "\u6309\u8BF7\u6C42\u751F\u6210\u4E00\u6761 mock \u5F85\u529E\u5EFA\u8BAE",
                    ),
                )
            }
            if (permissionState.bindPomodoro) {
                add(
                    AssistantToolDefinition(
                        "bind_pomodoro",
                        "\u7ED1\u4E13\u6CE8",
                        "\u751F\u6210\u4E00\u6761 mock \u4E13\u6CE8\u7ED1\u5B9A\u5EFA\u8BAE",
                    ),
                )
            }
            if (permissionState.openCampusMap && capabilities.canShowCampus) {
                add(
                    AssistantToolDefinition(
                        "open_map",
                        "\u6253\u5F00\u5730\u56FE",
                        "\u751F\u6210\u4E00\u6761 mock \u5730\u56FE\u8DF3\u8F6C\u5EFA\u8BAE",
                    ),
                )
            }
        }
    }

    suspend fun runMock(
        prompt: String,
        permissionState: AssistantPermissionState,
        summaries: List<AssistantContextSummary>,
        capabilities: EditionCapabilities,
    ): AssistantMockExecution {
        return orchestrator.coordinate(prompt, summaries, permissionState, capabilities)
    }
}
