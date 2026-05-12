package com.poxiao.app.campus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.SharedPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dataset
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.poxiao.app.data.FeedCard
import com.poxiao.app.data.PreviewCampusMapGateway
import com.poxiao.app.data.PreviewMarketplaceGateway
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.schedule.HitaTerm
import com.poxiao.app.ui.LiquidGlassSurface
import com.poxiao.app.ui.interactions.bouncyClick
import com.poxiao.app.ui.interactions.rememberHapticManager
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.interactions.SkeletonPlaceholder
import java.time.LocalDate
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private enum class CampusFilter(val title: String) {
    All("\u5168\u90e8"),
    Study("\u5b66\u4e60"),
    Travel("\u51fa\u884c"),
    Life("\u751f\u6d3b"),
    Trade("\u4ea4\u6613"),
    Account("\u8d26\u6237"),
    Info("\u8d44\u8baf"),
}

private enum class GradeStatusFilter(val title: String) {
    All("\u5168\u90e8"),
    Excellent("\u4f18\u79c0"),
    Passed("\u901a\u8fc7"),
    Warning("\u9884\u8b66"),
    Pending("\u5f85\u51fa\u5206"),
}

private enum class GradeSortMode(val title: String) {
    Default("\u9ed8\u8ba4"),
    ScoreDesc("\u6309\u5206\u6570"),
    CreditDesc("\u6309\u5b66\u5206"),
    NameAsc("\u6309\u8bfe\u540d"),
}

private data class CampusService(
    val id: String,
    val title: String,
    val body: String,
    val tag: String,
    val status: String,
    val icon: ImageVector,
    val featured: Boolean = false,
)

@Composable
fun CampusServicesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("campus_services_prefs", 0) }
    val authPrefs = remember { context.getSharedPreferences("schedule_auth", 0) }
    val savedStudentId = remember { SecurePrefs.getString(authPrefs, "student_id_secure", "student_id") }
    val savedPassword = remember { SecurePrefs.getString(authPrefs, "password_secure", "password") }
    val academicGateway = remember(savedStudentId, savedPassword) {
        if (savedStudentId.isNotBlank() && savedPassword.isNotBlank()) HitaAcademicGateway(savedStudentId, savedPassword) else null
    }
    val repository = remember(academicGateway) {
        if (academicGateway != null) {
            CampusServicesRepository(
                academicGateway = academicGateway,
                campusMapGateway = PreviewCampusMapGateway(),
                marketplaceGateway = PreviewMarketplaceGateway(),
            )
        } else {
            CampusServicesRepository()
        }
    }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var keyword by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CampusFilter.All) }
    var snapshot by remember { mutableStateOf<CampusSnapshot?>(null) }
    var terms by remember { mutableStateOf<List<HitaTerm>>(emptyList()) }
    var selectedTerm by remember { mutableStateOf<HitaTerm?>(null) }
    var gradeDetails by remember { mutableStateOf<List<FeedCard>>(emptyList()) }
    var gradeKeyword by remember { mutableStateOf("") }
    var gradeStatusFilter by remember { mutableStateOf(GradeStatusFilter.All) }
    var gradeSortMode by remember { mutableStateOf(GradeSortMode.Default) }
    var buildings by remember { mutableStateOf<List<FeedCard>>(emptyList()) }
    var selectedBuildingId by remember { mutableStateOf(prefs.getString("recent_building_id", "").orEmpty()) }
    var buildingKeyword by remember { mutableStateOf("") }
    var classroomDate by remember { mutableStateOf(prefs.getString("recent_classroom_date", LocalDate.now().toString()).orEmpty()) }
    var emptyClassrooms by remember { mutableStateOf<List<FeedCard>>(emptyList()) }
    val selectedSlotFilters = remember { mutableStateListOf<String>() }
    var academicLoading by remember { mutableStateOf(false) }
    var academicError by remember { mutableStateOf("") }
    var gradeExportHint by remember { mutableStateOf("") }
    var reloadTick by remember { mutableStateOf(0) }
    val favoriteBuildingIds = remember { mutableStateListOf<String>().apply { addAll(loadFavoriteBuildings(prefs)) } }
    val savedQueryKeys = remember { mutableStateListOf<String>().apply { addAll(loadSavedClassroomQueries(prefs)) } }

    LaunchedEffect(Unit) { snapshot = repository.loadSnapshot() }
    LaunchedEffect(academicGateway, reloadTick) {
        if (academicGateway == null) return@LaunchedEffect
        academicLoading = true
        academicError = ""
        runCatching {
            val termList = academicGateway.fetchTerms()
            val buildingList = academicGateway.fetchTeachingBuildings()
            terms = termList
            selectedTerm = termList.firstOrNull { it.isCurrent } ?: termList.firstOrNull()
            buildings = buildingList
            selectedBuildingId = buildingList.firstOrNull { it.id == selectedBuildingId }?.id ?: buildingList.firstOrNull()?.id.orEmpty()
        }.onFailure {
            academicError = it.message ?: "\u6559\u52a1\u6570\u636e\u540c\u6b65\u5931\u8d25\u3002"
        }
        academicLoading = false
    }
    LaunchedEffect(academicGateway, selectedTerm, reloadTick) {
        val gateway = academicGateway ?: return@LaunchedEffect
        val term = selectedTerm ?: return@LaunchedEffect
        runCatching { gateway.fetchGradesForTerm(term) }
            .onSuccess {
                gradeDetails = it
                saveCampusGradeCache(prefs, it)
            }
            .onFailure {
                gradeDetails = emptyList()
                academicError = it.message ?: "\u6210\u7ee9\u52a0\u8f7d\u5931\u8d25\u3002"
            }
    }
    LaunchedEffect(academicGateway, classroomDate, selectedBuildingId, reloadTick) {
        val gateway = academicGateway ?: return@LaunchedEffect
        if (selectedBuildingId.isBlank()) return@LaunchedEffect
        prefs.edit()
            .putString("recent_classroom_date", classroomDate)
            .putString("recent_building_id", selectedBuildingId)
            .apply()
        runCatching { emptyClassrooms = gateway.fetchEmptyClassrooms(classroomDate, selectedBuildingId) }
            .onFailure {
                emptyClassrooms = emptyList()
                academicError = it.message ?: "\u7a7a\u6559\u5ba4\u52a0\u8f7d\u5931\u8d25\u3002"
            }
    }

    val services = remember {
        listOf(
            CampusService("empty-classroom", "\u7a7a\u6559\u5ba4", "\u6309\u697c\u680b\u548c\u65e5\u671f\u7b5b\u9009\u53ef\u7528\u6559\u5ba4\u3002", "\u5b66\u4e60", "\u5b9e\u65f6", Icons.Outlined.MeetingRoom, true),
            CampusService("campus-map", "\u6821\u56ed\u5730\u56fe", "\u6559\u5b66\u697c\u3001\u98df\u5802\u3001\u64cd\u573a\u4e0e\u670d\u52a1\u70b9\u5bfc\u822a\u3002", "\u51fa\u884c", "\u5e38\u7528", Icons.Outlined.Explore, true),
            CampusService("grades", "\u6210\u7ee9\u8be6\u60c5", "\u6309\u5b66\u671f\u67e5\u770b\u6210\u7ee9\u3001\u5b66\u5206\u548c\u8003\u6838\u65b9\u5f0f\u3002", "\u5b66\u4e60", "\u6559\u52a1", Icons.Outlined.Dataset),
            CampusService("library", "\u56fe\u4e66\u9986", "\u9986\u85cf\u3001\u501f\u9605\u3001\u5ea7\u4f4d\u4e0e\u65b0\u4e66\u63a8\u8350\u7edf\u4e00\u5f52\u7eb3\u3002", "\u5b66\u4e60", "\u5e38\u7528", Icons.Outlined.Map),
        )
    }
    val filteredServices = remember(keyword) {
        services.filter { service ->
            keyword.isBlank() || listOf(service.title, service.body, service.tag, service.status).any { it.contains(keyword, true) }
        }
    }
    val mapQuickCards = remember(buildings, snapshot?.location) {
        buildList {
            add(
                FeedCard(
                    id = "map-location",
                    title = "\u6821\u56ed\u5730\u56fe",
                    source = snapshot?.location?.name ?: "\u6682\u65e0\u5b9a\u4f4d",
                    description = if (buildings.isNotEmpty()) {
                        "\u5df2\u540c\u6b65 ${buildings.size} \u4e2a\u6559\u5b66\u697c\u70b9\u4f4d\u5165\u53e3"
                    } else {
                        "\u7b49\u5f85\u6559\u5b66\u697c\u70b9\u4f4d\u540c\u6b65"
                    },
                ),
            )
            addAll(buildings.take(2).map {
                FeedCard(
                    id = "map-${it.id}",
                    title = it.title,
                    source = "\u6559\u5b66\u697c\u70b9\u4f4d",
                    description = "\u53ef\u4f5c\u4e3a\u6821\u56ed\u5730\u56fe\u9996\u6279\u70b9\u4f4d\u5165\u53e3",
                )
            })
        }
    }
    
    // 注入 DeepSeek 教务直连的上下文状态
    var aiQuery by remember { mutableStateOf("") }
    var isAiAnalyzing by remember { mutableStateOf(false) }
    val filteredBuildings = remember(buildings, buildingKeyword) {
        if (buildingKeyword.isBlank()) {
            buildings
        } else {
            buildings.filter {
                it.title.contains(buildingKeyword, ignoreCase = true) ||
                    it.id.contains(buildingKeyword, ignoreCase = true)
            }
        }
    }
    val academicQuickSummary = remember(selectedTerm, gradeDetails, emptyClassrooms, buildings, classroomDate) {
        listOf(
            FeedCard(
                id = "sum-term",
                title = "\u5f53\u524d\u5b66\u671f",
                source = selectedTerm?.name ?: "\u672a\u9009\u62e9",
                description = if (gradeDetails.isEmpty()) "\u6682\u65e0\u6210\u7ee9\u8be6\u60c5" else "\u5df2\u52a0\u8f7d ${gradeDetails.size} \u95e8\u8bfe\u7a0b\u6210\u7ee9",
            ),
            FeedCard(
                id = "sum-empty",
                title = "\u7a7a\u6559\u5ba4",
                source = classroomDate,
                description = if (emptyClassrooms.isEmpty()) "\u5f53\u524d\u7b5b\u9009\u4e0b\u6682\u672a\u627e\u5230\u53ef\u7528\u6559\u5ba4" else "\u5df2\u627e\u5230 ${emptyClassrooms.size} \u95f4\u53ef\u7528\u6559\u5ba4",
            ),
            FeedCard(
                id = "sum-building",
                title = "\u6559\u5b66\u697c",
                source = "${buildings.size} \u4e2a\u697c\u680b",
                description = if (selectedBuildingId.isBlank()) "\u7b49\u5f85\u9009\u62e9\u697c\u680b" else "\u5f53\u524d\u697c\u680b ${buildings.firstOrNull { it.id == selectedBuildingId }?.title.orEmpty()}",
            ),
        )
    }
    val filteredGradeDetails = remember(gradeDetails, gradeKeyword, gradeStatusFilter, gradeSortMode) {
        val filtered = gradeDetails.filter { card ->
            val matchesKeyword = gradeKeyword.isBlank() || listOf(card.title, card.source, card.description).any { it.contains(gradeKeyword, ignoreCase = true) }
            val statusText = card.source + card.description
            val matchesStatus = when (gradeStatusFilter) {
                GradeStatusFilter.All -> true
                GradeStatusFilter.Excellent -> statusText.contains("\u4f18\u79c0") || statusText.contains("90")
                GradeStatusFilter.Passed -> statusText.contains("\u901a\u8fc7")
                GradeStatusFilter.Warning -> statusText.contains("\u9884\u8b66") || statusText.contains("\u4e0d\u53ca\u683c") || statusText.contains("59") || statusText.contains("58")
                GradeStatusFilter.Pending -> statusText.contains("\u5f85")
            }
            matchesKeyword && matchesStatus
        }
        when (gradeSortMode) {
            GradeSortMode.Default -> filtered
            GradeSortMode.ScoreDesc -> filtered.sortedByDescending { extractCampusNumericValue(it.source, "\u603b\u8bc4") ?: -1.0 }
            GradeSortMode.CreditDesc -> filtered.sortedByDescending { extractCampusNumericValue(it.description, "\u5b66\u5206") ?: -1.0 }
            GradeSortMode.NameAsc -> filtered.sortedBy { it.title }
        }
    }
    val slotOptions = remember { listOf("1", "2", "3", "4", "5", "6") }
    val filteredEmptyClassrooms = remember(emptyClassrooms, selectedSlotFilters.toList()) {
        if (selectedSlotFilters.isEmpty()) {
            emptyClassrooms
        } else {
            emptyClassrooms.filter { card ->
                val fullText = card.title + card.source + card.description
                selectedSlotFilters.any { slot ->
                    fullText.contains("\u7b2c${slot}\u5927\u8282") || fullText.contains("DJ$slot")
                }
            }
        }
    }

    fun openService(service: CampusService) {
        when (service.id) {
            "campus-map" -> onOpenMap()
            "empty-classroom" -> {
                scope.launch { listState.animateScrollToItem(if (snapshot != null) 3 else 2) }
            }
            "grades" -> {
                scope.launch { listState.animateScrollToItem(if (snapshot != null) 3 else 2) }
            }
            else -> {
                keyword = service.title
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().background(PoxiaoThemeState.palette.card.copy(alpha = 0.05f)),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 22.dp,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 26.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SectionCard("\u6821\u56ed\u670d\u52a1", "\u4e00\u7ad9\u5f0f\u6821\u52a1\u5de5\u4f5c\u53f0", onBack) }
        
        // DeepSeek AI 直连入口
        item {
            LiquidGlassSurface(
                cornerRadius = 24.dp,
                tint = PoxiaoThemeState.palette.primary.copy(alpha = 0.1f),
                borderColor = PoxiaoThemeState.palette.primary.copy(alpha = 0.3f),
                glowColor = PoxiaoThemeState.palette.primary.copy(alpha = 0.15f),
                blurRadius = 16.dp,
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Dataset, contentDescription = "AI", tint = PoxiaoThemeState.palette.primary, modifier = Modifier.size(20.dp))
                        Text("DeepSeek 教务管家", style = MaterialTheme.typography.titleMedium, color = PoxiaoThemeState.palette.primary)
                    }
                    Text("不知道空教室怎么筛？或者想做期末成绩学情分析？直接对我说，我帮你调数据画图表。", style = MaterialTheme.typography.bodySmall, color = PoxiaoThemeState.palette.ink.copy(alpha = 0.7f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = aiQuery,
                            onValueChange = { aiQuery = it },
                            placeholder = { Text("例如：帮我分析一下大二上的成绩") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PoxiaoThemeState.palette.primary.copy(alpha = 0.5f),
                                unfocusedBorderColor = PoxiaoThemeState.palette.primary.copy(alpha = 0.2f),
                                focusedContainerColor = Color.White.copy(alpha = 0.4f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                            )
                        )
                        LiquidGlassSurface(
                            cornerRadius = 20.dp,
                            tint = PoxiaoThemeState.palette.primary.copy(alpha = 0.8f),
                            borderColor = PoxiaoThemeState.palette.primary.copy(alpha = 0.4f),
                            modifier = Modifier.bouncyClick(hapticManager = rememberHapticManager()) {
                                if (aiQuery.isNotBlank()) {
                                    isAiAnalyzing = true
                                    // 模拟网络请求或后续真实接入
                                    scope.launch {
                                        kotlinx.coroutines.delay(1500)
                                        isAiAnalyzing = false
                                        aiQuery = ""
                                    }
                                }
                            }
                        ) {
                            Text(
                                if (isAiAnalyzing) "分析中..." else "发送",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        snapshot?.let { current ->
            item { Header("\u5b9e\u65f6\u901f\u89c8", "\u7a7a\u6559\u5ba4\u3001\u6210\u7ee9\u548c\u5730\u56fe\u4f1a\u5148\u5728\u8fd9\u91cc\u7ed9\u51fa\u5feb\u7167\u3002") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailCard(FeedCard("snap-empty", "\u7a7a\u6559\u5ba4", current.emptyClassrooms.firstOrNull()?.title ?: "\u6682\u65e0\u6570\u636e", current.emptyClassrooms.firstOrNull()?.description ?: "\u7b49\u5f85\u540c\u6b65\u7a7a\u6559\u5ba4\u72b6\u6001\u3002"))
                    DetailCard(FeedCard("snap-grade", "\u6210\u7ee9\u8003\u8bd5", current.grades.firstOrNull()?.title ?: "\u6682\u65e0\u6570\u636e", current.grades.firstOrNull()?.description ?: "\u7b49\u5f85\u540c\u6b65\u6210\u7ee9\u8be6\u60c5\u3002"))
                    DetailCard(FeedCard("snap-map", "\u6821\u56ed\u5730\u56fe", current.location?.name ?: "\u6682\u65e0\u5b9a\u4f4d", current.location?.let { "\u5df2\u5b9a\u4f4d\u5230 ${it.name}" } ?: "\u7b49\u5f85\u5730\u56fe\u5b9a\u4f4d\u3002"))
                }
            }
        }
        item { Header("\u6559\u52a1\u8be6\u60c5", if (academicGateway != null) "\u6210\u7ee9\u6309\u5b66\u671f\u67e5\u770b\uff0c\u7a7a\u6559\u5ba4\u6309\u697c\u680b\u548c\u65e5\u671f\u7b5b\u9009\u3002" else "\u5148\u53bb\u8bfe\u8868\u9875\u767b\u5f55\u6559\u52a1\u7cfb\u7edf\uff0c\u8fd9\u91cc\u624d\u4f1a\u5207\u6362\u6210\u771f\u5b9e\u6570\u636e\u3002") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (academicGateway == null) {
                    HintCard("\u672a\u8fde\u63a5\u6559\u52a1", "\u5f53\u524d\u4ecd\u5728\u663e\u793a\u9884\u89c8\u6570\u636e\u3002\u5148\u5728\u8bfe\u8868\u9875\u767b\u5f55\u6559\u52a1\u7cfb\u7edf\u3002")
                } else {
                    if (academicLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SkeletonPlaceholder(modifier = Modifier.size(18.dp).clip(CircleShape))
                            Text("\u6b63\u5728\u540c\u6b65\u6559\u52a1\u6570\u636e", color = PoxiaoThemeState.palette.ink.copy(alpha = 0.76f))
                        }
                        Spacer(Modifier.height(10.dp))
                        SkeletonPlaceholder(modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(20.dp)))
                        Spacer(Modifier.height(10.dp))
                        SkeletonPlaceholder(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(20.dp)))
                    }
                    if (academicError.isNotBlank()) {
                        HintCard("\u540c\u6b65\u63d0\u793a", academicError)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionChip("\u91cd\u65b0\u540c\u6b65") { reloadTick += 1 }
                        ActionChip("\u5b9a\u4f4d\u6210\u7ee9") {
                            scope.launch { listState.animateScrollToItem(if (snapshot != null) 5 else 4) }
                        }
                        ActionChip("\u5b9a\u4f4d\u7a7a\u6559\u5ba4") {
                            scope.launch { listState.animateScrollToItem(if (snapshot != null) 5 else 4) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        academicQuickSummary.forEach { card ->
                            QuickMetricCard(card)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Panel("\u6210\u7ee9\u8be6\u60c5", "\u6309\u5b66\u671f\u5207\u6362\u8bfe\u7a0b\u6210\u7ee9\u4e0e\u8be6\u60c5\u3002") {
                        if (terms.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(terms) { term -> FilterChip(term.name, term == selectedTerm) { selectedTerm = term } }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                        
                        var showAdvancedFilters by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = gradeKeyword,
                                onValueChange = { gradeKeyword = it },
                                label = { Text("\u641c\u7d22\u8bfe\u7a0b\u6216\u8003\u6838\u4fe1\u606f") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PoxiaoThemeState.palette.primary,
                                    unfocusedBorderColor = PoxiaoThemeState.palette.cardBorder
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ActionChip(if (showAdvancedFilters) "收起" else "筛选") {
                                showAdvancedFilters = !showAdvancedFilters
                            }
                        }
                        
                        if (showAdvancedFilters) {
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GradeStatusFilter.entries) { item ->
                                    FilterChip(item.title, item == gradeStatusFilter) { gradeStatusFilter = item }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(GradeSortMode.entries) { item ->
                                    FilterChip(item.title, item == gradeSortMode) { gradeSortMode = item }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        ActionChip("\u590d\u5236\u8bfe\u7a0b\u6210\u7ee9\u6458\u8981") {
                            val text = buildGradeSummaryText(selectedTerm?.name.orEmpty(), filteredGradeDetails)
                            val clipboard = context.getSystemService(ClipboardManager::class.java)
                            clipboard?.setPrimaryClip(ClipData.newPlainText("\u6210\u7ee9\u6458\u8981", text))
                            gradeExportHint = "\u5df2\u590d\u5236\u5f53\u524d\u5b66\u671f\u6210\u7ee9\u6458\u8981\u3002"
                        }
                        if (gradeExportHint.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(gradeExportHint, style = MaterialTheme.typography.bodySmall, color = PoxiaoThemeState.palette.ink.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.height(10.dp))
                        if (gradeDetails.isEmpty()) {
                            HintCard("\u6682\u65e0\u6210\u7ee9", "\u5f53\u524d\u5b66\u671f\u6682\u672a\u8fd4\u56de\u6210\u7ee9\u8be6\u60c5\u3002")
                        } else if (filteredGradeDetails.isEmpty()) {
                            HintCard("\u672a\u5339\u914d\u6210\u7ee9", "\u5f53\u524d\u7b5b\u9009\u6761\u4ef6\u4e0b\u6ca1\u6709\u627e\u5230\u5bf9\u5e94\u8bfe\u7a0b\u3002")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredGradeDetails.forEach { DetailCard(it) }
                            }
                        }
                    }
                    Panel("\u7a7a\u6559\u5ba4\u7b5b\u9009", "\u652f\u6301\u6307\u5b9a\u65e5\u671f\u548c\u697c\u680b\u67e5\u770b\u771f\u5b9e\u7a7a\u95f2\u6559\u5ba4\u3002") {
                        var showAdvancedRoomFilters by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = classroomDate,
                                onValueChange = { classroomDate = it },
                                label = { Text("\u65e5\u671f\uff0c\u683c\u5f0f YYYY-MM-DD") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PoxiaoThemeState.palette.primary,
                                    unfocusedBorderColor = PoxiaoThemeState.palette.cardBorder
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ActionChip(if (showAdvancedRoomFilters) "收起" else "高级筛选") {
                                showAdvancedRoomFilters = !showAdvancedRoomFilters
                            }
                        }
                        
                        if (showAdvancedRoomFilters) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FilterChip("\u4eca\u5929", false) { classroomDate = LocalDate.now().toString() }
                                FilterChip("\u660e\u5929", false) { classroomDate = LocalDate.now().plusDays(1).toString() }
                                FilterChip("\u540e\u5929", false) { classroomDate = LocalDate.now().plusDays(2).toString() }
                            }
                            val currentQueryKey = buildClassroomQueryKey(classroomDate, selectedBuildingId, selectedSlotFilters)
                            if (savedQueryKeys.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(savedQueryKeys) { key ->
                                        FilterChip(formatClassroomQueryKey(key), false) {
                                            restoreClassroomQuery(key) { date, buildingId, slots ->
                                                classroomDate = date
                                                selectedBuildingId = buildingId
                                                selectedSlotFilters.clear()
                                                selectedSlotFilters.addAll(slots)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            ActionChip(if (currentQueryKey in savedQueryKeys) "\u5df2\u6536\u85cf\u67e5\u8be2\u6761\u4ef6" else "\u6536\u85cf\u5f53\u524d\u67e5\u8be2\u6761\u4ef6") {
                                if (currentQueryKey !in savedQueryKeys) {
                                    savedQueryKeys.add(0, currentQueryKey)
                                    while (savedQueryKeys.size > 6) savedQueryKeys.removeAt(savedQueryKeys.lastIndex)
                                    saveSavedClassroomQueries(prefs, savedQueryKeys)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(slotOptions) { slot ->
                                    FilterChip(
                                        "\u7b2c${slot}\u5927\u8282",
                                        slot in selectedSlotFilters,
                                    ) {
                                        if (slot in selectedSlotFilters) {
                                            selectedSlotFilters.remove(slot)
                                        } else {
                                            selectedSlotFilters.add(slot)
                                        }
                                    }
                                }
                            }
                            if (buildings.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                if (favoriteBuildingIds.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(buildings.filter { it.id in favoriteBuildingIds }) { building ->
                                            FilterChip(
                                                "\u2605 ${building.title}",
                                                building.id == selectedBuildingId,
                                            ) {
                                                selectedBuildingId = building.id
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                }
                                OutlinedTextField(
                                    value = buildingKeyword,
                                    onValueChange = { buildingKeyword = it },
                                    label = { Text("\u641c\u7d22\u697c\u680b") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                )
                                Spacer(Modifier.height(10.dp))
                                if (filteredBuildings.isEmpty()) {
                                    HintCard("\u672a\u627e\u5230\u697c\u680b", "\u8bf7\u6539\u4e00\u4e2a\u5173\u952e\u5b57\uff0c\u6216\u6e05\u7a7a\u641c\u7d22\u540e\u91cd\u8bd5\u3002")
                                } else {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(filteredBuildings) { building ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                FilterChip(building.title, building.id == selectedBuildingId) {
                                                    selectedBuildingId = building.id
                                                }
                                                Text(
                                                    if (building.id in favoriteBuildingIds) "\u53d6\u6d88" else "\u6536\u85cf",
                                                    modifier = Modifier.clickable {
                                                        if (building.id in favoriteBuildingIds) {
                                                            favoriteBuildingIds.remove(building.id)
                                                        } else {
                                                            favoriteBuildingIds.add(building.id)
                                                        }
                                                        saveFavoriteBuildings(prefs, favoriteBuildingIds)
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = PoxiaoThemeState.palette.ink.copy(alpha = 0.74f),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        if (emptyClassrooms.isEmpty()) {
                            HintCard("\u6682\u65e0\u7a7a\u6559\u5ba4", "\u5f53\u524d\u65e5\u671f\u548c\u697c\u680b\u4e0b\u6ca1\u6709\u67e5\u5230\u53ef\u7528\u6559\u5ba4\u3002")
                        } else if (filteredEmptyClassrooms.isEmpty()) {
                            HintCard("\u672a\u5339\u914d\u7a7a\u6559\u5ba4", "\u5f53\u524d\u9009\u4e2d\u7684\u5927\u8282\u4e0b\u6ca1\u6709\u5339\u914d\u5230\u7a7a\u95f2\u6559\u5ba4\u3002")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                filteredEmptyClassrooms.forEach { DetailCard(it) }
                            }
                        }
                    }
                }
            }
        }
        item { Header("\u5feb\u6377\u5165\u53e3", "\u5148\u5904\u7406\u6700\u5e38\u7528\u7684\u6821\u52a1\u80fd\u529b\u3002") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(
                    listOf(
                        FeedCard("q1", "\u7a7a\u6559\u5ba4", "\u5b66\u4e60", "\u6309\u697c\u680b\u548c\u65e5\u671f\u7b5b\u9009"),
                        FeedCard("q2", "\u6210\u7ee9", "\u6559\u52a1", "\u6309\u5b66\u671f\u67e5\u770b\u8be6\u60c5"),
                    ) + mapQuickCards,
                ) { card ->
                    QuickCard(card) {
                        when {
                            card.id == "q1" -> services.firstOrNull { it.id == "empty-classroom" }?.let(::openService)
                            card.id == "q2" -> services.firstOrNull { it.id == "grades" }?.let(::openService)
                            card.id == "q3" || card.id.startsWith("map-") -> services.firstOrNull { it.id == "campus-map" }?.let(::openService)
                        }
                    }
                }
            }
        }
        item { Header("\u6821\u56ed\u670d\u52a1\u5217\u8868", "基础功能入口") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredServices.forEach { service ->
                    ServiceCard(
                        service = service,
                        onOpen = { openService(service) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(label: String, title: String, onBack: () -> Unit) {
    val palette = PoxiaoThemeState.palette
    val haptic = rememberHapticManager()
    LiquidGlassSurface(
        cornerRadius = 24.dp,
        tint = palette.primary.copy(alpha = 0.85f),
        borderColor = palette.primary.copy(alpha = 0.3f),
        glowColor = palette.primary.copy(alpha = 0.2f),
        blurRadius = 12.dp,
        refractionHeight = 8.dp,
        refractionAmount = 10.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.82f))
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
                LiquidGlassSurface(
                    cornerRadius = 16.dp,
                    tint = Color.White.copy(alpha = 0.2f),
                    borderColor = Color.White.copy(alpha = 0.16f),
                    modifier = Modifier.bouncyClick(hapticManager = haptic) { onBack() },
                ) {
                    Text(
                        "\u8fd4\u56de",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.95f),
                    )
                }
            }
            Text(
                "\u628a\u6559\u52a1\u3001\u5730\u56fe\u4e0e\u65e5\u5e38\u5de5\u5177\u96c6\u4e2d\u5230\u4e00\u5904\u3002",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.82f),
            )
        }
    }
}

@Composable private fun ServiceSearchField(value: String, onValueChange: (String) -> Unit) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassSurface(
        cornerRadius = 22.dp,
        tint = palette.card.copy(alpha = 0.4f),
        borderColor = palette.cardBorder.copy(alpha = 0.2f),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("\u641c\u7d22\u6821\u56ed\u670d\u52a1") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = palette.ink.copy(alpha = 0.72f),
                unfocusedLabelColor = palette.ink.copy(alpha = 0.64f),
                focusedTextColor = palette.ink,
                unfocusedTextColor = palette.ink,
                cursorColor = palette.primary,
            ),
        )
    }
}

@Composable private fun Header(title: String, body: String) {
    val palette = PoxiaoThemeState.palette
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.78f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        MiniPill("\u5206\u533a")
    }
}

@Composable private fun FilterChip(title: String, selected: Boolean, onClick: () -> Unit) {
    val palette = PoxiaoThemeState.palette
    val haptic = rememberHapticManager()
    LiquidGlassSurface(
        cornerRadius = 16.dp,
        tint = if (selected) palette.primary.copy(alpha = 0.15f) else palette.card.copy(alpha = 0.4f),
        borderColor = if (selected) palette.primary.copy(alpha = 0.25f) else palette.cardBorder.copy(alpha = 0.2f),
        glowColor = if (selected) palette.primary.copy(alpha = 0.1f) else Color.Transparent,
        modifier = Modifier.bouncyClick(hapticManager = haptic, onClick = onClick),
    ) {
        Text(title, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = MaterialTheme.typography.labelMedium, color = if (selected) palette.primary else palette.ink.copy(alpha = 0.72f))
    }
}

@Composable private fun Panel(title: String, body: String, content: @Composable () -> Unit) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassSurface(
        cornerRadius = 24.dp, 
        tint = palette.card.copy(alpha = 0.3f), 
        borderColor = palette.cardBorder.copy(alpha = 0.2f),
        blurRadius = 8.dp,
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.6f))
            content()
        }
    }
}

@Composable private fun DetailCard(card: FeedCard) {
    val palette = PoxiaoThemeState.palette
    val badgeText = if (card.source.contains(" \u00b7 ")) card.source.substringBefore(" \u00b7 ") else card.title
    val headline = if (card.source.contains(" \u00b7 ")) card.title else card.source
    val summary = if (card.source.contains(" \u00b7 ")) {
        card.source.substringAfter(" \u00b7 ", "") + if (card.description.isNotBlank()) "\n${card.description}" else ""
    } else {
        card.description
    }
    val detailLines = remember(card.id, card.source, card.description) { buildCardDetailLines(card) }
    var expanded by remember(card.id) { mutableStateOf(false) }
    
    LiquidGlassSurface(
        cornerRadius = 22.dp, 
        tint = palette.card.copy(alpha = 0.4f), 
        borderColor = palette.cardBorder.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(badgeText)
            Text(headline, style = MaterialTheme.typography.titleMedium, color = palette.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(summary, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.7f))
            if (detailLines.size > 1) {
                Text(
                    if (expanded) "\u6536\u8d77\u6210\u7ee9\u7ec4\u6210" else "\u5c55\u5f00\u6210\u7ee9\u7ec4\u6210",
                    modifier = Modifier.clickable { expanded = !expanded },
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.primary,
                )
                if (expanded) {
                    detailLines.forEach { line ->
                        Text(
                            "\u00b7 $line",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.ink.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}

@Composable private fun MiniPill(text: String) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassSurface(
        cornerRadius = 12.dp,
        tint = palette.card.copy(alpha = 0.5f),
        borderColor = palette.cardBorder.copy(alpha = 0.2f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = palette.ink.copy(alpha = 0.65f),
        )
    }
}

@Composable private fun StatusBadge(text: String) {
    val palette = PoxiaoThemeState.palette
    val badgeColor = when {
        text.contains("\u4f18\u79c0") -> Color(0xFF3C8A63)
        text.contains("\u901a\u8fc7") -> Color(0xFF4C7C5A)
        text.contains("\u9884\u8b66") -> Color(0xFFAD6A3A)
        text.contains("\u5f85") -> palette.ink.copy(alpha = 0.6f)
        else -> palette.primary
    }
    LiquidGlassSurface(
        cornerRadius = 14.dp,
        tint = badgeColor.copy(alpha = 0.15f),
        borderColor = badgeColor.copy(alpha = 0.2f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = badgeColor,
        )
    }
}

@Composable private fun HintCard(title: String, body: String) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassSurface(
        cornerRadius = 20.dp, 
        tint = palette.card.copy(alpha = 0.3f), 
        borderColor = palette.cardBorder.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = palette.ink)
            Text(body, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.6f))
        }
    }
}

@Composable private fun QuickCard(card: FeedCard, onClick: () -> Unit) {
    val palette = PoxiaoThemeState.palette
    val haptic = rememberHapticManager()
    LiquidGlassSurface(
        cornerRadius = 22.dp, 
        tint = palette.card.copy(alpha = 0.4f), 
        borderColor = palette.cardBorder.copy(alpha = 0.15f), 
        modifier = Modifier.width(210.dp).bouncyClick(hapticManager = haptic, onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Map, contentDescription = card.title, tint = palette.primary, modifier = Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(card.title, style = MaterialTheme.typography.titleSmall, color = palette.ink)
                    MiniPill(card.source)
                }
            }
            Text(card.description, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.65f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable private fun ActionChip(text: String, onClick: () -> Unit) {
    val palette = PoxiaoThemeState.palette
    val haptic = rememberHapticManager()
    LiquidGlassSurface(
        cornerRadius = 16.dp,
        tint = palette.card.copy(alpha = 0.5f),
        borderColor = palette.cardBorder.copy(alpha = 0.2f),
        modifier = Modifier.bouncyClick(hapticManager = haptic, onClick = onClick),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = palette.ink,
        )
    }
}

@Composable private fun QuickMetricCard(card: FeedCard) {
    val palette = PoxiaoThemeState.palette
    LiquidGlassSurface(
        cornerRadius = 20.dp,
        tint = palette.card.copy(alpha = 0.35f),
        borderColor = palette.cardBorder.copy(alpha = 0.15f),
        modifier = Modifier.width(170.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniPill(card.title)
            Text(card.source, style = MaterialTheme.typography.titleMedium, color = palette.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(card.description, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable private fun ServiceCard(service: CampusService, onOpen: () -> Unit) {
    val palette = PoxiaoThemeState.palette
    val haptic = rememberHapticManager()
    LiquidGlassSurface(
        cornerRadius = 22.dp,
        tint = palette.card.copy(alpha = 0.4f),
        borderColor = palette.cardBorder.copy(alpha = 0.15f),
        modifier = Modifier.bouncyClick(hapticManager = haptic, onClick = onOpen),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(if (service.featured) 56.dp else 48.dp).background(palette.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(service.icon, contentDescription = service.title, tint = palette.primary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(service.title, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                    MiniPill(service.tag)
                }
                Text(service.body, style = MaterialTheme.typography.bodySmall, color = palette.ink.copy(alpha = 0.65f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniPill(service.status)
                Text("\u6253\u5f00", color = palette.ink.copy(alpha = 0.5f), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private fun loadPinnedCampusServices(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("pinned_ids", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun savePinnedCampusServices(prefs: SharedPreferences, ids: List<String>) {
    prefs.edit().putString("pinned_ids", ids.joinToString("|")).apply()
}

private fun loadRecentCampusServices(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("recent_ids", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun saveRecentCampusServices(prefs: SharedPreferences, ids: List<String>) {
    prefs.edit().putString("recent_ids", ids.joinToString("|")).apply()
}

private fun loadFavoriteBuildings(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("favorite_buildings", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("|").filter { it.isNotBlank() }
}

private fun saveFavoriteBuildings(prefs: SharedPreferences, ids: List<String>) {
    prefs.edit().putString("favorite_buildings", ids.joinToString("|")).apply()
}

private fun loadSavedClassroomQueries(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("saved_classroom_queries", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return raw.split("||").filter { it.isNotBlank() }
}

private fun saveSavedClassroomQueries(prefs: SharedPreferences, queries: List<String>) {
    prefs.edit().putString("saved_classroom_queries", queries.joinToString("||")).apply()
}

private fun saveCampusGradeCache(prefs: SharedPreferences, cards: List<FeedCard>) {
    val array = JSONArray().apply {
        cards.forEach { card ->
            put(
                JSONObject().apply {
                    put("id", card.id)
                    put("title", card.title)
                    put("source", card.source)
                    put("description", card.description)
                },
            )
        }
    }
    prefs.edit().putString("grade_cache_v1", array.toString()).apply()
}

private fun loadCampusGradeCache(prefs: SharedPreferences): List<FeedCard> {
    val raw = prefs.getString("grade_cache_v1", "").orEmpty()
    if (raw.isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    FeedCard(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        source = item.optString("source"),
                        description = item.optString("description"),
                    ),
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun extractCampusNumericValue(text: String, key: String): Double? {
    val afterKey = text.substringAfter(key, "").trim()
    if (afterKey.isBlank()) return null
    val token = afterKey.takeWhile { it.isDigit() || it == '.' }
    return token.toDoubleOrNull()
}

private fun buildCardDetailLines(card: FeedCard): List<String> {
    return (card.source + "\n" + card.description)
        .split("\n", "·")
        .map { it.trim() }
        .filter { it.isNotBlank() && it != card.title }
        .distinct()
}

private fun buildGradeSummaryText(termName: String, cards: List<FeedCard>): String {
    if (cards.isEmpty()) return "当前没有可复制的成绩摘要。"
    val header = if (termName.isBlank()) "课程成绩摘要" else "$termName 成绩摘要"
    val lines = cards.map { "${it.title}：${it.source}；${it.description}" }
    return (listOf(header) + lines).joinToString("\n")
}

private fun buildClassroomQueryKey(date: String, buildingId: String, slots: List<String>): String {
    return listOf(date, buildingId, slots.sorted().joinToString(",")).joinToString("#")
}

private fun restoreClassroomQuery(
    key: String,
    onRestore: (String, String, List<String>) -> Unit,
) {
    val parts = key.split("#")
    if (parts.size < 3) return
    val slots = parts[2].split(",").filter { it.isNotBlank() }
    onRestore(parts[0], parts[1], slots)
}

private fun formatClassroomQueryKey(key: String): String {
    val parts = key.split("#")
    if (parts.size < 3) return key
    val slots = if (parts[2].isBlank()) "全时段" else parts[2].split(",").joinToString(" ") { "第${it}大节" }
    return "${parts[0]} · ${parts[1]} · $slots"
}
