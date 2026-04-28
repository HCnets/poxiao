package com.poxiao.app.todo

enum class TodoQuadrant(
    val title: String,
    val subtitle: String,
) {
    ImportantUrgent("\u91cd\u8981\u4e14\u7d27\u6025", "\u7acb\u5373\u5904\u7406\uff0c\u4f18\u5148\u6e05\u7a7a"),
    ImportantNotUrgent("\u91cd\u8981\u4e0d\u7d27\u6025", "\u6301\u7eed\u63a8\u8fdb\uff0c\u6c89\u6dc0\u957f\u671f\u4ef7\u503c"),
    UrgentNotImportant("\u7d27\u6025\u4e0d\u91cd\u8981", "\u5feb\u901f\u5b8c\u6210\uff0c\u51cf\u5c11\u4e34\u65f6\u6253\u65ad"),
    Neither("\u4e0d\u7d27\u6025\u4e0d\u91cd\u8981", "\u5ef6\u540e\u6574\u7406\uff0c\u907f\u514d\u5360\u7528\u5fc3\u6d41"),
}

enum class TodoPriority(val title: String) {
    High("\u9ad8\u4f18\u5148"),
    Medium("\u4e2d\u4f18\u5148"),
    Low("\u4f4e\u4f18\u5148"),
}

data class TodoSubtask(
    val title: String,
    val done: Boolean = false,
)

data class TodoTask(
    val id: String,
    val title: String,
    val note: String,
    val quadrant: TodoQuadrant,
    val priority: TodoPriority = TodoPriority.Medium,
    val dueText: String,
    val tags: List<String> = emptyList(),
    val listName: String = "\u6536\u96c6\u7bb1",
    val reminderText: String = "",
    val repeatText: String = "\u4e0d\u91cd\u590d",
    val subtasks: List<TodoSubtask> = emptyList(),
    val focusCount: Int = 0,
    val focusGoal: Int = 0,
    val done: Boolean = false,
)

object TodoPreviewData {
    val tasks = listOf(
        TodoTask(
            id = "t1",
            title = "\u5b8c\u6210\u673a\u5668\u5b66\u4e60\u5b9e\u9a8c\u62a5\u544a",
            note = "\u8865\u9f50\u7ed3\u679c\u5206\u6790\u4e0e\u53ef\u89c6\u5316\u622a\u56fe\uff0c\u4eca\u665a\u524d\u63d0\u4ea4\u3002",
            quadrant = TodoQuadrant.ImportantUrgent,
            priority = TodoPriority.High,
            dueText = "\u4eca\u5929 20:00",
            tags = listOf("\u8bfe\u7a0b", "DDL"),
            listName = "\u5b66\u4e60",
            reminderText = "\u63d0\u524d 30 \u5206\u949f",
            repeatText = "\u4e0d\u91cd\u590d",
            focusGoal = 3,
            subtasks = listOf(
                TodoSubtask("\u8865\u7ed3\u679c\u5206\u6790"),
                TodoSubtask("\u68c0\u67e5\u53ef\u89c6\u5316\u56fe\u8868"),
            ),
        ),
        TodoTask(
            id = "t2",
            title = "\u6574\u7406\u4ea4\u4e92\u8bbe\u8ba1\u5468\u8bb0",
            note = "\u628a\u8349\u56fe\u3001\u6d4b\u8bd5\u8bb0\u5f55\u548c\u53cd\u601d\u7edf\u4e00\u8fdb\u4f5c\u54c1\u96c6\u3002",
            quadrant = TodoQuadrant.ImportantNotUrgent,
            priority = TodoPriority.Medium,
            dueText = "\u672c\u5468\u5185",
            tags = listOf("\u4f5c\u54c1\u96c6", "\u590d\u76d8"),
            listName = "\u521b\u4f5c",
            reminderText = "\u4eca\u665a 21:00",
            repeatText = "\u6bcf\u5468",
            focusGoal = 2,
        ),
        TodoTask(
            id = "t3",
            title = "\u56de\u590d\u793e\u56e2\u7269\u6599\u786e\u8ba4",
            note = "\u786e\u8ba4\u6d77\u62a5\u5c3a\u5bf8\u4e0e\u6253\u5370\u6570\u91cf\u3002",
            quadrant = TodoQuadrant.UrgentNotImportant,
            priority = TodoPriority.Medium,
            dueText = "\u4eca\u5929 16:30",
            tags = listOf("\u6c9f\u901a"),
            listName = "\u7ec4\u7ec7",
            reminderText = "\u63d0\u524d 10 \u5206\u949f",
            repeatText = "\u4e0d\u91cd\u590d",
        ),
        TodoTask(
            id = "t4",
            title = "\u6e05\u7406\u4e0b\u8f7d\u76ee\u5f55",
            note = "\u628a\u65e0\u7528\u8349\u7a3f\u548c\u91cd\u590d\u7d20\u6750\u5f52\u6863\u3002",
            quadrant = TodoQuadrant.Neither,
            priority = TodoPriority.Low,
            dueText = "\u5468\u672b",
            tags = listOf("\u6574\u7406"),
            listName = "\u751f\u6d3b",
            reminderText = "",
            repeatText = "\u6bcf\u6708",
        ),
        TodoTask(
            id = "t5",
            title = "\u51c6\u5907\u4e0b\u5468\u7b54\u8fa9\u63d0\u7eb2",
            note = "\u5148\u5199 3 \u9875\u6838\u5fc3\u903b\u8f91\uff0c\u518d\u8865\u89c6\u89c9\u9875\u3002",
            quadrant = TodoQuadrant.ImportantNotUrgent,
            priority = TodoPriority.High,
            dueText = "\u4e0b\u5468\u4e8c",
            tags = listOf("\u7b54\u8fa9", "\u91cd\u70b9"),
            listName = "\u5b66\u4e60",
            reminderText = "\u660e\u665a 20:00",
            repeatText = "\u4e0d\u91cd\u590d",
            focusGoal = 4,
        ),
        TodoTask(
            id = "t6",
            title = "\u63d0\u4ea4\u56fe\u4e66\u9986\u9884\u7ea6",
            note = "\u9884\u7ea6\u660e\u5929\u4e0a\u5348\u81ea\u4e60\u5ea7\u4f4d\u3002",
            quadrant = TodoQuadrant.ImportantUrgent,
            priority = TodoPriority.Medium,
            dueText = "\u4eca\u5929 22:00",
            tags = listOf("\u5b66\u4e60"),
            listName = "\u6821\u56ed",
            reminderText = "\u4eca\u665a 21:30",
            repeatText = "\u5de5\u4f5c\u65e5",
        ),
    )
}
