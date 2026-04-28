package com.poxiao.app.campus

import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.CampusLocation
import com.poxiao.app.data.PreviewCampusMapGateway
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.BambooStroke
import com.poxiao.app.ui.theme.ForestDeep
import com.poxiao.app.ui.theme.PineInk
import kotlinx.coroutines.launch

@Composable
fun CampusMapScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("campus_map_prefs", 0) }
    val authPrefs = remember { context.getSharedPreferences("schedule_auth", 0) }
    val savedStudentId = remember { SecurePrefs.getString(authPrefs, "student_id_secure", "student_id") }
    val savedPassword = remember { SecurePrefs.getString(authPrefs, "password_secure", "password") }
    val academicGateway = remember(savedStudentId, savedPassword) {
        if (savedStudentId.isNotBlank() && savedPassword.isNotBlank()) {
            HitaAcademicGateway(savedStudentId, savedPassword)
        } else {
            null
        }
    }
    val mapGateway = remember { PreviewCampusMapGateway() }
    val scope = rememberCoroutineScope()

    var keyword by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("all") }
    var currentLocation by remember { mutableStateOf<CampusLocation?>(null) }
    var routePreview by remember { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf<CampusLocation?>(null) }
    var points by remember { mutableStateOf<List<CampusLocation>>(emptyList()) }
    var availableMapApps by remember { mutableStateOf<List<String>>(emptyList()) }
    val favorites = remember { mutableStateListOf<String>().apply { addAll(loadMapFavorites(prefs)) } }
    val homeShortcuts = remember { mutableStateListOf<String>().apply { addAll(loadMapHomeShortcuts(prefs)) } }
    val recentIds = remember { mutableStateListOf<String>().apply { addAll(loadRecentPoints(prefs)) } }

    LaunchedEffect(Unit) {
        availableMapApps = MapNavigator.availableMapApps(context)
        currentLocation = mapGateway.locateMe()
        val buildingPoints = if (academicGateway != null) {
            runCatching { academicGateway.fetchTeachingBuildings() }.getOrDefault(emptyList()).map { item ->
                CampusLocation(
                    id = item.id,
                    name = item.title,
                    latitude = 0.0,
                    longitude = 0.0,
                )
            }
        } else {
            mapGateway.searchNearbyUsers()
        }
        points = buildingPoints
    }

    val filteredPoints = remember(points, keyword, selectedCategory, favorites.toList()) {
        val searched = if (keyword.isBlank()) {
            points
        } else {
            points.filter { it.name.contains(keyword, ignoreCase = true) || it.id.contains(keyword, ignoreCase = true) }
        }
        val categorized = if (selectedCategory == "all") {
            searched
        } else {
            searched.filter { pointGroupCategory(it) == selectedCategory }
        }
        categorized.sortedWith(compareByDescending<CampusLocation> { it.id in favorites }.thenBy { it.name })
    }
    val groupedPoints = remember(filteredPoints) {
        filteredPoints.groupBy(::pointGroupTitle).toList()
    }
    val favoritePoints = remember(filteredPoints, favorites.toList()) {
        filteredPoints.filter { it.id in favorites }
    }
    val recentPoints = remember(points, recentIds.toList()) {
        recentIds.mapNotNull { id -> points.firstOrNull { it.id == id } }
    }

    val openPoint: (CampusLocation) -> Unit = { point ->
        selectedPoint = point
        updateRecentPoint(prefs, recentIds, point.id)
        scope.launch {
            val result = MapNavigator.openCampusPoint(context, point)
            routePreview = if (result.opened) {
                "\u5df2\u8df3\u8f6c${result.appName}\uff0c\u53ef\u7ee7\u7eed\u5bfc\u822a\u5230 ${point.name}\u3002"
            } else {
                mapGateway.buildRoute(point)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 20.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 26.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            MapSectionCard(
                title = "\u6821\u56ed\u5730\u56fe",
                body = "\u5e94\u7528\u5185\u8d1f\u8d23\u70b9\u4f4d\u641c\u7d22\u3001\u6536\u85cf\u4e0e\u5feb\u6377\u5165\u53e3\uff0c\u5bfc\u822a\u65f6\u4f18\u5148\u5916\u8df3\u5230\u53ef\u7528\u7684\u5730\u56fe\u5e94\u7528\u3002",
                trailing = "\u8fd4\u56de",
                onTrailingClick = onBack,
            )
        }
        item {
            MapSearchField(
                value = keyword,
                onValueChange = { keyword = it },
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(mapCategoryOptions()) { option ->
                    MapFilterChip(
                        text = option.second,
                        selected = option.first == selectedCategory,
                        onClick = { selectedCategory = option.first },
                    )
                }
            }
        }
        item {
            if (availableMapApps.isEmpty()) {
                MapHintCard(
                    title = "\u672a\u53d1\u73b0\u5730\u56fe\u5e94\u7528",
                    body = "\u5f53\u524d\u8bbe\u5907\u672a\u68c0\u6d4b\u5230\u53ef\u7528\u5730\u56fe App\uff0c\u5efa\u8bae\u5b89\u88c5\u9ad8\u5fb7\u3001\u767e\u5ea6\u6216\u817e\u8baf\u5730\u56fe\u3002",
                )
            } else {
                MapHintCard(
                    title = "\u53ef\u7528\u5730\u56fe",
                    body = availableMapApps.joinToString("\u3001"),
                )
            }
        }
        item {
            currentLocation?.let { location ->
                MapPointCard(
                    title = "\u5f53\u524d\u4f4d\u7f6e",
                    subtitle = location.name,
                    body = "\u53ef\u4ece\u8fd9\u91cc\u51fa\u53d1\u5230\u76ee\u6807\u697c\u5b87\u8fdb\u884c\u5bfc\u822a\u3002",
                    favorite = false,
                    selected = false,
                    onToggleFavorite = null,
                    onOpen = { routePreview = "\u5f53\u524d\u5df2\u5b9a\u4f4d\u5230 ${location.name}" },
                )
            } ?: MapHintCard(
                title = "\u6682\u65e0\u5b9a\u4f4d",
                body = "\u5f53\u524d\u4f7f\u7528\u9884\u89c8\u5b9a\u4f4d\u80fd\u529b\uff0c\u540e\u7eed\u53ef\u7ee7\u7eed\u66ff\u6362\u4e3a\u771f\u5b9e\u5b9a\u4f4d\u3002",
            )
        }
        if (routePreview.isNotBlank()) {
            item {
                MapHintCard("\u8def\u7ebf\u9884\u89c8", routePreview)
            }
        }
        selectedPoint?.let { point ->
            item {
                MapDetailCard(
                    point = point,
                    favorite = point.id in favorites,
                    inHome = point.id in homeShortcuts,
                    onToggleFavorite = {
                        if (point.id in favorites) favorites.remove(point.id) else favorites.add(point.id)
                        saveMapFavorites(prefs, favorites)
                    },
                    onToggleHome = {
                        if (point.id in homeShortcuts) homeShortcuts.remove(point.id) else homeShortcuts.add(point.id)
                        saveMapHomeShortcuts(prefs, homeShortcuts)
                    },
                )
            }
        }
        if (favoritePoints.isNotEmpty()) {
            item {
                MapSectionTitle("\u5e38\u7528\u70b9\u4f4d", "\u5df2\u6536\u85cf\u70b9\u4f4d\u4f1a\u6392\u5728\u66f4\u9760\u524d\u7684\u4f4d\u7f6e\u3002")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favoritePoints) { point ->
                        MapFavoriteChip(point.name) { openPoint(point) }
                    }
                }
            }
        }
        if (recentPoints.isNotEmpty()) {
            item {
                MapSectionTitle("\u6700\u8fd1\u8bbf\u95ee", "\u53ef\u4ece\u8fd9\u91cc\u5feb\u901f\u56de\u5230\u521a\u6253\u5f00\u8fc7\u7684\u70b9\u4f4d\u3002")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentPoints) { point ->
                        MapFavoriteChip(point.name) { openPoint(point) }
                    }
                }
            }
        }
        item {
            MapSectionTitle(
                title = "\u6559\u5b66\u697c\u70b9\u4f4d",
                body = "\u5f53\u524d\u5171 ${filteredPoints.size} \u4e2a\u70b9\u4f4d\uff0c\u53ef\u6309\u7c7b\u7b5b\u9009\u5e76\u4e00\u952e\u8df3\u8f6c\u5730\u56fe\u5bfc\u822a\u3002",
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                groupedPoints.forEach { (groupTitle, groupItems) ->
                    MapSectionTitle(groupTitle, "\u5206\u7ec4\u5185\u5171 ${groupItems.size} \u4e2a\u70b9\u4f4d")
                    groupItems.forEach { point ->
                        MapPointCard(
                            title = pointGroupBadge(point),
                            subtitle = point.name,
                            body = nearbyServiceHint(point),
                            favorite = point.id in favorites,
                            selected = selectedPoint?.id == point.id,
                            onToggleFavorite = {
                                if (point.id in favorites) favorites.remove(point.id) else favorites.add(point.id)
                                saveMapFavorites(prefs, favorites)
                            },
                            onOpen = { openPoint(point) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapSectionCard(
    title: String,
    body: String,
    trailing: String,
    onTrailingClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "\u5de5\u5177\u4e2d\u5fc3",
                        style = MaterialTheme.typography.labelMedium,
                        color = ForestDeep.copy(alpha = 0.62f),
                    )
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = PineInk)
                }
                MapFavoriteChip(trailing, onTrailingClick)
            }
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.74f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MapSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("\u641c\u7d22\u6559\u5b66\u697c\u4e0e\u70b9\u4f4d") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = ForestDeep.copy(alpha = 0.7f),
                unfocusedLabelColor = ForestDeep.copy(alpha = 0.62f),
                focusedTextColor = PineInk,
                unfocusedTextColor = PineInk,
                cursorColor = PineInk,
            ),
            singleLine = true,
        )
    }
}

@Composable
private fun MapSectionTitle(title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.78f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = PineInk)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        MapMetaPill("\u5206\u7ec4")
    }
}

@Composable
private fun MapPointCard(
    title: String,
    subtitle: String,
    body: String,
    favorite: Boolean,
    selected: Boolean,
    onToggleFavorite: (() -> Unit)?,
    onOpen: () -> Unit,
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.22f),
        animationSpec = tween(220),
        label = "mapCardColor",
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (selected) Color.White.copy(alpha = 0.28f) else BambooStroke.copy(alpha = 0.16f),
        animationSpec = tween(220),
        label = "mapCardBorder",
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        animationSpec = tween(220),
        label = "mapCardScale",
    )
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = animatedColor,
        border = BorderStroke(1.dp, animatedBorder),
        modifier = Modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clickable(onClick = onOpen),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.76f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MapMetaPill(title)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = PineInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = body,
                        style = MaterialTheme.typography.bodySmall,
                        color = ForestDeep.copy(alpha = 0.76f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End,
                )
                {
                    if (onToggleFavorite != null) {
                        MapMetaPill(
                            when {
                                selected -> "\u5f53\u524d"
                                favorite -> "\u5df2\u6536\u85cf"
                                else -> "\u6536\u85cf"
                            },
                        )
                    }
                    Text(
                        "\u5bfc\u822a",
                        style = MaterialTheme.typography.labelMedium,
                        color = PineInk,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onToggleFavorite != null) {
                        MapFavoriteChip(
                            if (favorite) "\u53d6\u6d88\u6536\u85cf" else "\u6536\u85cf",
                            onToggleFavorite,
                        )
                    }
                    MapFavoriteChip("\u8df3\u8f6c\u5730\u56fe", onOpen)
                }
            }
        }
    }
}

@Composable
private fun MapMetaPill(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = ForestDeep.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MapFavoriteChip(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.24f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = PineInk,
        )
    }
}

@Composable
private fun MapFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.14f),
        border = BorderStroke(
            1.dp,
            if (selected) Color.White.copy(alpha = 0.24f) else BambooStroke.copy(alpha = 0.12f),
        ),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = PineInk,
        )
    }
}

@Composable
private fun MapHintCard(title: String, body: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.16f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = PineInk)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = ForestDeep.copy(alpha = 0.72f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MapDetailCard(
    point: CampusLocation,
    favorite: Boolean,
    inHome: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHome: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, BambooStroke.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.72f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("\u70b9\u4f4d\u8be6\u60c5", style = MaterialTheme.typography.labelMedium, color = ForestDeep.copy(alpha = 0.7f))
                    Text(point.name, style = MaterialTheme.typography.headlineSmall, color = PineInk)
                    Text(pointGroupTitle(point), style = MaterialTheme.typography.bodySmall, color = ForestDeep.copy(alpha = 0.74f))
                }
                MapMetaPill(pointGroupBadge(point))
            }
            Text(
                text = "\u9644\u8fd1\u670d\u52a1",
                style = MaterialTheme.typography.titleSmall,
                color = PineInk,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(nearbyServiceItems(point)) { item ->
                    MapMetaPill(item)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MapFavoriteChip(if (favorite) "\u53d6\u6d88\u6536\u85cf" else "\u6536\u85cf\u70b9\u4f4d", onToggleFavorite)
                MapFavoriteChip(if (inHome) "\u79fb\u51fa\u9996\u9875" else "\u52a0\u5165\u9996\u9875", onToggleHome)
            }
        }
    }
}

private fun loadMapFavorites(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("favorite_points", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun saveMapFavorites(prefs: SharedPreferences, ids: List<String>) {
    prefs.edit().putString("favorite_points", ids.joinToString("|")).apply()
}

private fun loadMapHomeShortcuts(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("home_quick_points", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun saveMapHomeShortcuts(prefs: SharedPreferences, ids: List<String>) {
    prefs.edit().putString("home_quick_points", ids.joinToString("|")).apply()
}

private fun loadRecentPoints(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("recent_points", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun updateRecentPoint(prefs: SharedPreferences, recentIds: MutableList<String>, id: String) {
    val latest = recentIds.filter { it != id }.toMutableList()
    latest.add(0, id)
    val saved = latest.take(6)
    recentIds.clear()
    recentIds.addAll(saved)
    prefs.edit().putString("recent_points", saved.joinToString("|")).apply()
}

private fun pointGroupTitle(point: CampusLocation): String {
    val token = point.id.trim().take(1).uppercase()
    val name = point.name
    return when {
        name.contains("\u56fe\u4e66\u9986") || name.contains("\u81ea\u4e60") || name.contains("\u516c\u5171") -> "\u56fe\u4e66\u4e0e\u516c\u5171\u8d44\u6e90"
        name.contains("\u5b9e\u9a8c") || name.contains("\u79d1\u7814") || token == "L" -> "\u5b9e\u9a8c\u4e0e\u79d1\u7814\u533a"
        name.contains("\u4f53\u80b2") || name.contains("\u64cd\u573a") || name.contains("\u6d3b\u52a8") -> "\u8fd0\u52a8\u4e0e\u6d3b\u52a8\u533a"
        name.contains("\u98df\u5802") || name.contains("\u9910") || name.contains("\u751f\u6d3b") || name.contains("\u5bbf\u820d") -> "\u751f\u6d3b\u670d\u52a1\u533a"
        token == "A" -> "\u6559\u5b66\u697c A \u533a"
        token == "B" -> "\u6559\u5b66\u697c B \u533a"
        token == "C" -> "\u6559\u5b66\u697c C \u533a"
        token == "T" -> "T \u697c"
        else -> "\u5176\u4ed6\u70b9\u4f4d"
    }
}

private fun pointGroupCategory(point: CampusLocation): String {
    return when (pointGroupTitle(point)) {
        "\u56fe\u4e66\u4e0e\u516c\u5171\u8d44\u6e90" -> "public"
        "\u5b9e\u9a8c\u4e0e\u79d1\u7814\u533a" -> "research"
        "\u751f\u6d3b\u670d\u52a1\u533a" -> "living"
        "\u8fd0\u52a8\u4e0e\u6d3b\u52a8\u533a" -> "activity"
        else -> "teaching"
    }
}

private fun pointGroupBadge(point: CampusLocation): String {
    return when (pointGroupCategory(point)) {
        "public" -> "\u516c\u5171\u8d44\u6e90"
        "research" -> "\u5b9e\u9a8c\u79d1\u7814"
        "living" -> "\u751f\u6d3b\u670d\u52a1"
        "activity" -> "\u8fd0\u52a8\u6d3b\u52a8"
        else -> "\u6559\u5b66\u70b9\u4f4d"
    }
}

private fun nearbyServiceHint(point: CampusLocation): String {
    return when (pointGroupCategory(point)) {
        "public" -> "\u56fe\u4e66\u9986\u670d\u52a1\u53f0\u3001\u81ea\u4e60\u533a\u3001\u6253\u5370\u590d\u5370"
        "research" -> "\u5b9e\u9a8c\u4e2d\u5fc3\u3001\u8bbe\u5907\u670d\u52a1\u3001\u5bfc\u5e08\u529e\u516c\u533a"
        "living" -> "\u98df\u5802\u3001\u8d85\u5e02\u3001\u5feb\u9012\u4e0e\u751f\u6d3b\u670d\u52a1"
        "activity" -> "\u8fd0\u52a8\u573a\u9986\u3001\u793e\u56e2\u6d3b\u52a8\u70b9\u3001\u4f11\u95f2\u533a"
        else -> "\u6559\u5ba4\u3001\u81ea\u4e60\u70b9\u3001\u7a7a\u6559\u5ba4\u4e0e\u6253\u5370\u70b9"
    }
}

private fun nearbyServiceItems(point: CampusLocation): List<String> {
    return nearbyServiceHint(point).split("\u3001").filter { it.isNotBlank() }
}

private fun mapCategoryOptions(): List<Pair<String, String>> {
    return listOf(
        "all" to "\u5168\u90e8",
        "teaching" to "\u6559\u5b66",
        "research" to "\u5b9e\u9a8c",
        "living" to "\u751f\u6d3b",
        "public" to "\u516c\u5171",
        "activity" to "\u6d3b\u52a8",
    )
}
