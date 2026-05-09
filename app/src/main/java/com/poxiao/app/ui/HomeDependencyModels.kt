package com.poxiao.app.ui

import android.content.SharedPreferences
import com.poxiao.app.data.AppSummaryProvider
import com.poxiao.app.data.AssistantPermissionStore
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.data.HiagentGateway
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.review.ReviewPlannerStore

internal data class HomePreferencesBundle(
    val mapPrefs: SharedPreferences,
    val homePrefs: SharedPreferences,
    val todoPrefs: SharedPreferences,
    val focusPrefs: SharedPreferences,
    val focusRecordPrefs: SharedPreferences,
    val schedulePrefs: SharedPreferences,
    val scheduleAuthPrefs: SharedPreferences,
    val authPrefs: SharedPreferences,
    val campusPrefs: SharedPreferences,
    val assistantBridgePrefs: SharedPreferences,
)

internal data class HomeStoresBundle(
    val assistantStore: AssistantSessionStore,
    val permissionStore: AssistantPermissionStore,
    val toolKit: AssistantToolKit,
    val summaryProvider: AppSummaryProvider,
    val noteStore: CourseNoteStore,
    val reviewStore: ReviewPlannerStore,
)

internal data class HomeDependencies(
    val gateway: HiagentGateway,
    val prefs: HomePreferencesBundle,
    val stores: HomeStoresBundle,
)
