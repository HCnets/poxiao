package com.poxiao.app.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.poxiao.app.data.AppSummaryProvider
import com.poxiao.app.data.AssistantGatewayFactory
import com.poxiao.app.data.AssistantPermissionStore
import com.poxiao.app.data.AssistantSessionStore
import com.poxiao.app.data.AssistantToolKit
import com.poxiao.app.notes.CourseNoteStore
import com.poxiao.app.review.ReviewPlannerStore

@Composable
internal fun rememberHomeDependencies(
    context: Context,
): HomeDependencies {
    val gateway = remember { AssistantGatewayFactory.create() }
    val prefs = remember(context) {
        HomePreferencesBundle(
            mapPrefs = context.getSharedPreferences("campus_map_prefs", 0),
            homePrefs = context.getSharedPreferences("home_workbench", Context.MODE_PRIVATE),
            todoPrefs = context.getSharedPreferences("todo_board", Context.MODE_PRIVATE),
            focusPrefs = context.getSharedPreferences("focus_bridge", Context.MODE_PRIVATE),
            focusRecordPrefs = context.getSharedPreferences("focus_records", Context.MODE_PRIVATE),
            schedulePrefs = context.getSharedPreferences("schedule_cache", Context.MODE_PRIVATE),
            scheduleAuthPrefs = context.getSharedPreferences("schedule_exam_week", Context.MODE_PRIVATE),
            authPrefs = context.getSharedPreferences("schedule_auth", 0),
            campusPrefs = context.getSharedPreferences("campus_services_prefs", 0),
            assistantBridgePrefs = context.getSharedPreferences("assistant_bridge", Context.MODE_PRIVATE),
        )
    }
    val stores = remember(context) {
        HomeStoresBundle(
            assistantStore = AssistantSessionStore(context),
            permissionStore = AssistantPermissionStore(context),
            toolKit = AssistantToolKit(),
            summaryProvider = AppSummaryProvider(context),
            noteStore = CourseNoteStore(context),
            reviewStore = ReviewPlannerStore(context),
        )
    }
    return remember(gateway, prefs, stores) {
        HomeDependencies(
            gateway = gateway,
            prefs = prefs,
            stores = stores,
        )
    }
}
