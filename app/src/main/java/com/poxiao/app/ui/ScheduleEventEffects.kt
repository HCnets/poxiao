package com.poxiao.app.ui

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun DayScheduleDraftRestoreEffect(
    selectedDate: String,
    editingEventId: String?,
    draftPrefs: SharedPreferences,
    onDraftTitleChange: (String) -> Unit,
    onDraftTimeChange: (String) -> Unit,
    onDraftTypeChange: (String) -> Unit,
    onDraftNoteChange: (String) -> Unit,
    onEventHintChange: (String) -> Unit,
    onDraftReadyChange: (Boolean) -> Unit,
    findEditingEvent: (String?) -> ScheduleExtraEvent?,
) {
    LaunchedEffect(selectedDate, editingEventId) {
        val editingEvent = findEditingEvent(editingEventId)
        val restoredDraft = loadScheduleEventDraft(draftPrefs)
        if (editingEvent != null) {
            if (restoredDraft != null && restoredDraft.eventId == editingEvent.id) {
                onDraftTitleChange(restoredDraft.title)
                onDraftTimeChange(restoredDraft.time.ifBlank { editingEvent.time })
                onDraftTypeChange(restoredDraft.type.ifBlank { editingEvent.type })
                onDraftNoteChange(restoredDraft.note)
                onEventHintChange("已恢复 ${editingEvent.title} 的未完成编辑。")
            } else {
                onDraftTitleChange(editingEvent.title)
                onDraftTimeChange(editingEvent.time)
                onDraftTypeChange(editingEvent.type)
                onDraftNoteChange(editingEvent.note)
                onEventHintChange("正在编辑 ${editingEvent.title}")
            }
        } else {
            if (restoredDraft != null && restoredDraft.date == selectedDate && restoredDraft.eventId == null) {
                onDraftTitleChange(restoredDraft.title)
                onDraftTimeChange(restoredDraft.time.ifBlank { DefaultScheduleEventTime })
                onDraftTypeChange(restoredDraft.type.ifBlank { DefaultScheduleEventType })
                onDraftNoteChange(restoredDraft.note)
                onEventHintChange(
                    if (restoredDraft.title.isNotBlank() || restoredDraft.note.isNotBlank()) "已恢复 $selectedDate 的未完成事件草稿。" else "",
                )
            } else {
                resetScheduleEventDraftState(
                    onDraftTitleChange = onDraftTitleChange,
                    onDraftTimeChange = onDraftTimeChange,
                    onDraftTypeChange = onDraftTypeChange,
                    onDraftNoteChange = onDraftNoteChange,
                )
            }
        }
        onDraftReadyChange(true)
    }
}

@Composable
internal fun DayScheduleDraftPersistenceEffect(
    selectedDate: String,
    editingEventId: String?,
    draftTitle: String,
    draftTime: String,
    draftType: String,
    draftNote: String,
    draftReady: Boolean,
    draftPrefs: SharedPreferences,
) {
    LaunchedEffect(selectedDate, draftTitle, draftTime, draftType, draftNote, editingEventId, draftReady) {
        if (!draftReady) return@LaunchedEffect
        if (
            draftTitle.isBlank() &&
            draftNote.isBlank() &&
            draftTime == DefaultScheduleEventTime &&
            draftType == DefaultScheduleEventType
        ) {
            clearScheduleEventDraft(draftPrefs)
        } else {
            saveScheduleEventDraft(
                draftPrefs,
                ScheduleEventDraft(
                    eventId = editingEventId,
                    date = selectedDate,
                    title = draftTitle,
                    time = draftTime,
                    type = draftType,
                    note = draftNote,
                ),
            )
        }
    }
}
