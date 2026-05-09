package com.poxiao.app.ui

import android.content.SharedPreferences

internal const val DefaultScheduleEventTime = "19:30"
internal const val DefaultScheduleEventType = "作业"

internal fun resetScheduleEventDraftState(
    onDraftTitleChange: (String) -> Unit,
    onDraftTimeChange: (String) -> Unit,
    onDraftTypeChange: (String) -> Unit,
    onDraftNoteChange: (String) -> Unit,
) {
    onDraftTitleChange("")
    onDraftTimeChange(DefaultScheduleEventTime)
    onDraftTypeChange(DefaultScheduleEventType)
    onDraftNoteChange("")
}

internal fun submitScheduleEventDraft(
    selectedDate: String,
    editingEvent: ScheduleExtraEvent?,
    draftTitle: String,
    draftTime: String,
    draftType: String,
    draftNote: String,
    draftPrefs: SharedPreferences,
    onAddEvent: (ScheduleExtraEvent) -> Unit,
    onUpdateEvent: (ScheduleExtraEvent) -> Unit,
): String {
    if (draftTitle.isBlank()) return "请先填写事件标题"
    val updatedEvent = ScheduleExtraEvent(
        id = editingEvent?.id ?: "event-${System.currentTimeMillis()}",
        date = selectedDate,
        title = draftTitle,
        time = draftTime,
        type = draftType.ifBlank { "事件" },
        note = draftNote,
    )
    clearScheduleEventDraft(draftPrefs)
    return if (editingEvent == null) {
        onAddEvent(updatedEvent)
        "已加入 $selectedDate $draftTime 的${draftType.ifBlank { "学习" }}事件"
    } else {
        onUpdateEvent(updatedEvent)
        "已更新 ${updatedEvent.title}"
    }
}

internal fun deleteScheduleEventDraft(
    editingEvent: ScheduleExtraEvent,
    draftPrefs: SharedPreferences,
    onDeleteEvent: (String) -> Unit,
): String {
    onDeleteEvent(editingEvent.id)
    clearScheduleEventDraft(draftPrefs)
    return "已删除 ${editingEvent.title}"
}
