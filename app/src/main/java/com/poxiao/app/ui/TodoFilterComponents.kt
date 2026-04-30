package com.poxiao.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun TodoFilterPanel(
    filter: TodoFilter,
    onFilterChange: (TodoFilter) -> Unit,
    viewMode: TodoViewMode,
    onViewModeChange: (TodoViewMode) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    allTags: List<String>,
    selectedTags: SnapshotStateList<String>,
    listOptions: List<String>,
    listFilter: String,
    onListFilterChange: (String) -> Unit,
    focusGoalFilter: TodoFocusGoalFilter,
    onFocusGoalFilterChange: (TodoFocusGoalFilter) -> Unit,
) {
    GlassCard {
        SelectionRow(options = TodoFilter.entries.toList(), selected = filter, label = { it.title }, onSelect = onFilterChange)
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = TodoViewMode.entries.toList(), selected = viewMode, label = { it.title }, onSelect = onViewModeChange)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("搜索任务") },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        if (allTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allTags.forEach { tag ->
                    SelectionChip(
                        text = tag,
                        chosen = tag in selectedTags,
                        onClick = {
                            if (tag in selectedTags) selectedTags.remove(tag) else selectedTags.add(tag)
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(options = listOptions, selected = listFilter, label = { it }, onSelect = onListFilterChange)
        Spacer(modifier = Modifier.height(12.dp))
        SelectionRow(
            options = TodoFocusGoalFilter.entries.toList(),
            selected = focusGoalFilter,
            label = { it.title },
            onSelect = onFocusGoalFilterChange,
        )
    }
}
