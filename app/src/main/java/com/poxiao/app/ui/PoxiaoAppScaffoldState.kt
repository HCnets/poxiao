package com.poxiao.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.poxiao.app.notes.CourseNoteSeed
import com.poxiao.app.review.ReviewPlannerSeed
import com.poxiao.app.schedule.AcademicRepository
import com.poxiao.app.schedule.HitaScheduleRepository

internal class PoxiaoAppScaffoldState(
    pendingDrawerActionState: MutableState<(() -> Unit)?>,
    sectionState: MutableState<PrimarySection>,
    previousSectionState: MutableState<PrimarySection>,
    sectionTransitionDirectionState: MutableState<Int>,
    overlayPageState: MutableState<OverlayPage?>,
    sideNavExpandedState: MutableState<Boolean>,
    courseNoteSeedState: MutableState<CourseNoteSeed?>,
    reviewPlannerSeedState: MutableState<ReviewPlannerSeed?>,
    assistantHistoryFocusAtState: MutableState<Long?>,
    islandVisibleState: MutableState<Boolean>,
    scheduleEntryModeState: MutableState<ScheduleMode>,
    scheduleEntryWorkbenchState: MutableState<ScheduleWorkbench>,
    todoEntryFilterState: MutableState<TodoFilter>,
    val sectionSweepProgress: Animatable<Float, *>,
    val sectionOrder: List<PrimarySection>,
    val residentSections: SnapshotStateList<PrimarySection>,
    val repository: AcademicRepository,
) {
    var pendingDrawerAction by pendingDrawerActionState
    var section by sectionState
    var previousSection by previousSectionState
    var sectionTransitionDirection by sectionTransitionDirectionState
    var overlayPage by overlayPageState
    var sideNavExpanded by sideNavExpandedState
    var courseNoteSeed by courseNoteSeedState
    var reviewPlannerSeed by reviewPlannerSeedState
    var assistantHistoryFocusAt by assistantHistoryFocusAtState
    var islandVisible by islandVisibleState
    var scheduleEntryMode by scheduleEntryModeState
    var scheduleEntryWorkbench by scheduleEntryWorkbenchState
    var todoEntryFilter by todoEntryFilterState
}

@Composable
internal fun rememberPoxiaoAppScaffoldState(): PoxiaoAppScaffoldState {
    val sectionOrder = remember {
        listOf(
            PrimarySection.Home,
            PrimarySection.Schedule,
            PrimarySection.Todo,
            PrimarySection.Pomodoro,
            PrimarySection.More,
        )
    }
    return remember {
        PoxiaoAppScaffoldState(
            pendingDrawerActionState = mutableStateOf(null),
            sectionState = mutableStateOf(PrimarySection.Home),
            previousSectionState = mutableStateOf(PrimarySection.Home),
            sectionTransitionDirectionState = mutableIntStateOf(1),
            overlayPageState = mutableStateOf(null),
            sideNavExpandedState = mutableStateOf(false),
            courseNoteSeedState = mutableStateOf(null),
            reviewPlannerSeedState = mutableStateOf(null),
            assistantHistoryFocusAtState = mutableStateOf(null),
            islandVisibleState = mutableStateOf(true),
            scheduleEntryModeState = mutableStateOf(ScheduleMode.Week),
            scheduleEntryWorkbenchState = mutableStateOf(ScheduleWorkbench.Timetable),
            todoEntryFilterState = mutableStateOf(TodoFilter.All),
            sectionSweepProgress = Animatable(1f),
            sectionOrder = sectionOrder,
            residentSections = mutableStateListOf(PrimarySection.Home),
            repository = HitaScheduleRepository(),
        )
    }
}
