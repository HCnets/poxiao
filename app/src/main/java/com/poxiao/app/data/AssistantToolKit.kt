package com.poxiao.app.data

import android.content.Context
import org.json.JSONObject

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

class AssistantToolKit {
    fun availableTools(permissionState: AssistantPermissionState): List<AssistantToolDefinition> {
        return buildList {
            if (permissionState.readSchedule) add(AssistantToolDefinition("read_schedule", "\u8BFB\u8BFE\u8868", "\u8BFB\u53D6\u4ECA\u65E5\u8BFE\u7A0B\u3001\u5F53\u524D\u5468\u8BFE\u8868\u4E0E\u8003\u8BD5\u5468\u6458\u8981"))
            if (permissionState.readTodo) add(AssistantToolDefinition("read_todo", "\u8BFB\u5F85\u529E", "\u8BFB\u53D6\u5F85\u529E\u4F18\u5148\u9879\u3001\u76EE\u6807\u8FDB\u5EA6\u4E0E\u6E05\u5355\u6458\u8981"))
            if (permissionState.readFocus) add(AssistantToolDefinition("read_focus", "\u8BFB\u4E13\u6CE8", "\u8BFB\u53D6\u4E13\u6CE8\u8BB0\u5F55\u3001\u5F53\u524D\u7ED1\u5B9A\u4EFB\u52A1\u4E0E\u8FBE\u6210\u60C5\u51B5"))
            if (permissionState.readGrades) add(AssistantToolDefinition("read_grades", "\u8BFB\u6210\u7EE9", "\u8BFB\u53D6\u6210\u7EE9\u6458\u8981\u4E0E\u6700\u8FD1\u540C\u6B65\u7F13\u5B58"))
            if (permissionState.readMap) add(AssistantToolDefinition("read_map", "\u8BFB\u5730\u56FE", "\u8BFB\u53D6\u5FEB\u6377\u70B9\u4F4D\u4E0E\u6700\u8FD1\u8BBF\u95EE\u5730\u70B9"))
            if (permissionState.createTodo) add(AssistantToolDefinition("create_todo", "\u5EFA\u5F85\u529E", "\u6309\u8BF7\u6C42\u751F\u6210\u4E00\u6761 mock \u5F85\u529E\u5EFA\u8BAE"))
            if (permissionState.bindPomodoro) add(AssistantToolDefinition("bind_pomodoro", "\u7ED1\u4E13\u6CE8", "\u751F\u6210\u4E00\u6761 mock \u4E13\u6CE8\u7ED1\u5B9A\u5EFA\u8BAE"))
            if (permissionState.openCampusMap) add(AssistantToolDefinition("open_map", "\u6253\u5F00\u5730\u56FE", "\u751F\u6210\u4E00\u6761 mock \u5730\u56FE\u8DF3\u8F6C\u5EFA\u8BAE"))
        }
    }

    fun runMock(
        prompt: String,
        permissionState: AssistantPermissionState,
        summaries: List<AssistantContextSummary>,
    ): AssistantMockExecution {
        val normalized = prompt.lowercase()
        val matchedSummary = summaries.firstOrNull {
            prompt.contains(it.title) || prompt.contains(it.source) || prompt.contains(it.body.take(6))
        }
        val tool = when {
            normalized.contains("\u590D\u4E60") && permissionState.readSchedule ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u8BFB\u590D\u4E60\u8BA1\u5212", "\u5B8C\u6210", matchedSummary?.body ?: "\u5DF2\u8BFB\u53D6\u5F53\u524D\u5F85\u63A5\u7BA1\u7684\u590D\u4E60\u8BA1\u5212\u6458\u8981\u3002", System.currentTimeMillis())
            (normalized.contains("\u8BFE") || normalized.contains("\u8003\u8BD5")) && permissionState.readSchedule ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u8BFB\u8BFE\u8868", "\u5B8C\u6210", matchedSummary?.body ?: "\u5DF2\u8BFB\u53D6\u5F53\u524D\u65E5\u7A0B\u6458\u8981\u3002", System.currentTimeMillis())
            (normalized.contains("\u5F85\u529E") || normalized.contains("\u4EFB\u52A1")) && permissionState.readTodo ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u8BFB\u5F85\u529E", "\u5B8C\u6210", matchedSummary?.body ?: "\u5DF2\u8BFB\u53D6\u5F85\u529E\u4E0E\u4F18\u5148\u9879\u6458\u8981\u3002", System.currentTimeMillis())
            (normalized.contains("\u4E13\u6CE8") || normalized.contains("\u756A\u8304")) && permissionState.bindPomodoro ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u7ED1\u4E13\u6CE8", "\u5B8C\u6210", "\u5DF2\u751F\u6210 mock \u4E13\u6CE8\u7ED1\u5B9A\u5EFA\u8BAE\uFF0C\u53EF\u7EE7\u7EED\u63A5\u756A\u8304\u949F\u771F\u5B9E\u52A8\u4F5C\u3002", System.currentTimeMillis())
            (normalized.contains("\u6210\u7EE9") || normalized.contains("gpa")) && permissionState.readGrades ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u8BFB\u6210\u7EE9", "\u5B8C\u6210", matchedSummary?.body ?: "\u5DF2\u8BFB\u53D6\u6210\u7EE9\u4E0E\u8D8B\u52BF\u6458\u8981\u3002", System.currentTimeMillis())
            (normalized.contains("\u5730\u56FE") || normalized.contains("\u697C") || normalized.contains("\u5BFC\u822A")) && permissionState.openCampusMap ->
                AssistantToolCall("tool-${System.currentTimeMillis()}", "\u6253\u5F00\u5730\u56FE", "\u5B8C\u6210", matchedSummary?.body ?: "\u5DF2\u751F\u6210 mock \u5730\u56FE\u5BFC\u822A\u5EFA\u8BAE\u3002", System.currentTimeMillis())
            else -> null
        }
        val reply = when (tool?.title) {
            "\u8BFB\u590D\u4E60\u8BA1\u5212" -> "\u6211\u5DF2\u7ECF\u8BFB\u53D6\u4E86\u5F53\u524D\u5F85\u63A5\u7BA1\u7684\u590D\u4E60\u8BA1\u5212\u3002${tool.summary}\u7B49\u771F\u5B9E\u63A5\u53E3\u6253\u901A\u540E\uFF0C\u8FD9\u7EC4\u8BA1\u5212\u5C31\u53EF\u4EE5\u76F4\u63A5\u4EA4\u7ED9\u667A\u80FD\u4F53\u91CD\u6392\u3001\u62C6\u89E3\u548C\u6267\u884C\u3002"
            "\u8BFB\u8BFE\u8868" -> "\u6211\u5DF2\u7ECF\u6309\u8BFE\u8868\u4E0A\u4E0B\u6587\u6574\u7406\u51FA\u5F53\u524D\u65E5\u7A0B\u3002${tool.summary}\u5982\u679C\u63A5\u4E0A\u771F\u5B9E\u63A5\u53E3\uFF0C\u4E0B\u4E00\u6B65\u53EF\u4EE5\u76F4\u63A5\u7ED9\u4F60\u6392\u5E8F\u6216\u62C6\u89E3\u4ECA\u5929\u7684\u5B89\u6392\u3002"
            "\u8BFB\u5F85\u529E" -> "\u6211\u5DF2\u7ECF\u6309\u5F85\u529E\u4E0A\u4E0B\u6587\u7ED9\u4F60\u505A\u4E86 mock \u68B3\u7406\u3002${tool.summary}\u540E\u7EED\u63A5\u5165\u771F\u5B9E\u5DE5\u5177\u540E\uFF0C\u53EF\u4EE5\u76F4\u63A5\u751F\u6210\u5F85\u529E\u6216\u6267\u884C\u62C6\u89E3\u3002"
            "\u7ED1\u4E13\u6CE8" -> "\u6211\u5DF2\u6839\u636E\u4F60\u7684\u8BF7\u6C42\u751F\u6210\u4E86 mock \u4E13\u6CE8\u5EFA\u8BAE\u3002${tool.summary}"
            "\u8BFB\u6210\u7EE9" -> "\u6211\u5DF2\u7ECF\u4ECE\u6210\u7EE9\u6458\u8981\u91CC\u63D0\u53D6\u4E86\u53EF\u7528\u4FE1\u606F\u3002${tool.summary}\u63A5\u4E0A\u63A5\u53E3\u540E\u53EF\u4EE5\u76F4\u63A5\u505A\u6210\u7EE9\u5206\u6790\u6216\u590D\u4E60\u5EFA\u8BAE\u3002"
            "\u6253\u5F00\u5730\u56FE" -> "\u6211\u5DF2\u7ECF\u6839\u636E\u5730\u56FE\u4E0A\u4E0B\u6587\u751F\u6210\u4E86 mock \u5BFC\u822A\u5EFA\u8BAE\u3002${tool.summary}"
            else -> {
                val contextHint = summaries.firstOrNull()?.body ?: "\u5F53\u524D\u5DF2\u6709\u8BFE\u8868\u3001\u5F85\u529E\u3001\u4E13\u6CE8\u548C\u6210\u7EE9\u6458\u8981\u53EF\u4F9B\u6CE8\u5165\u3002"
                "\u6211\u5DF2\u6536\u5230\u4F60\u7684\u95EE\u9898\u3002\u76EE\u524D\u8FD8\u662F\u672C\u5730 mock \u9636\u6BB5\uFF0C\u4F46\u5DF2\u7ECF\u53EF\u4EE5\u57FA\u4E8E\u73B0\u6709\u4E0A\u4E0B\u6587\u505A\u7ED3\u6784\u5316\u56DE\u590D\u3002$contextHint"
            }
        }
        return AssistantMockExecution(toolCall = tool, reply = reply)
    }
}
