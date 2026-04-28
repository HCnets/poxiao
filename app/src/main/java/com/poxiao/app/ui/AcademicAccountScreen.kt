package com.poxiao.app.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.poxiao.app.schedule.HitaScheduleRepository
import com.poxiao.app.security.SecurePrefs
import com.poxiao.app.ui.theme.ForestGreen
import com.poxiao.app.ui.theme.Ginkgo
import com.poxiao.app.ui.theme.MossGreen
import com.poxiao.app.ui.theme.PoxiaoThemeState
import com.poxiao.app.ui.theme.WarmMist
import kotlinx.coroutines.launch

@Composable
internal fun AcademicAccountScreen(
    repository: HitaScheduleRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val palette = PoxiaoThemeState.palette
    val scope = rememberCoroutineScope()
    val schedulePrefs = remember { context.getSharedPreferences("schedule_auth", Context.MODE_PRIVATE) }
    val uiState by repository.observeUiState().collectAsState()
    val sessionStudentId = uiState.studentId.trim()
    val boundPassword = SecurePrefs.getString(schedulePrefs, "password_secure", "password")
    val persistedStudentId = getPersistedAcademicStudentId(schedulePrefs)
    val liveBoundStudentId = sessionStudentId.takeIf {
        uiState.loggedIn && isLikelyCompleteAcademicStudentId(it)
    }.orEmpty()
    val storedBoundStudentId = persistedStudentId.takeIf {
        isLikelyCompleteAcademicStudentId(it) && boundPassword.isNotBlank()
    }.orEmpty()
    val boundStudentId = liveBoundStudentId.ifBlank { storedBoundStudentId }
    var accountProfile by remember { mutableStateOf(resolveAcademicAccountProfile(schedulePrefs, boundStudentId)) }
    var loginStudentId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberPassword by remember { mutableStateOf(schedulePrefs.getBoolean("remember_password", true)) }
    var logoutInProgress by remember { mutableStateOf(false) }
    val realName = accountProfile.realName.ifBlank {
        if (uiState.loggedIn && sessionStudentId == boundStudentId) uiState.studentName else ""
    }
    val nickname = accountProfile.nickname.ifBlank { realName }
    var nicknameInput by remember { mutableStateOf(accountProfile.nickname) }
    var nicknameFocused by remember { mutableStateOf(false) }
    val hasBoundAccount = boundStudentId.isNotBlank() && (realName.isNotBlank() || storedBoundStudentId.isNotBlank())
    val displayName = if (hasBoundAccount) {
        nickname.ifBlank {
            realName.ifBlank { boundStudentId }
        }
    } else {
        "教务账号"
    }
    val typedPreviewProfile = if (!hasBoundAccount && hasAcademicAccountProfile(schedulePrefs, loginStudentId)) {
        resolveAcademicAccountProfile(schedulePrefs, loginStudentId.trim())
    } else {
        AcademicAccountProfile("", "", "")
    }
    val previewDisplayName = typedPreviewProfile.nickname.ifBlank {
        typedPreviewProfile.realName.ifBlank {
            "教务账号"
        }
    }
    val accountStatusText = when {
        uiState.loading -> "同步中"
        uiState.authExpired && hasBoundAccount -> "会话失效"
        uiState.loggedIn && hasBoundAccount -> "已连接"
        hasBoundAccount -> "未登录"
        else -> "未登录"
    }
    val accountStatusAccent = when {
        uiState.loading -> Ginkgo
        uiState.authExpired && hasBoundAccount -> WarmMist
        uiState.loggedIn && hasBoundAccount -> ForestGreen
        else -> palette.softText
    }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            persistAcademicAvatarImage(context, uri)?.let { storedUri ->
                val updated = accountProfile.copy(avatarUri = storedUri)
                accountProfile = updated
                if (isLikelyCompleteAcademicStudentId(boundStudentId)) {
                    saveAcademicAccountProfile(schedulePrefs, updated, boundStudentId)
                }
            }
        }
    }

    LaunchedEffect(uiState.loggedIn, uiState.studentId, uiState.studentName, loginStudentId, loginPassword, rememberPassword, boundStudentId, boundPassword) {
        if (logoutInProgress) return@LaunchedEffect
        val effectiveStudentId = when {
            isLikelyCompleteAcademicStudentId(uiState.studentId) -> uiState.studentId.trim()
            isLikelyCompleteAcademicStudentId(loginStudentId) -> loginStudentId.trim()
            else -> ""
        }
        val effectivePassword = when {
            isLikelyCompleteAcademicStudentId(uiState.studentId) && boundPassword.isNotBlank() -> boundPassword
            isLikelyCompleteAcademicStudentId(loginStudentId) -> loginPassword
            else -> loginPassword
        }
        if (uiState.loggedIn && uiState.studentName.isNotBlank() && isLikelyCompleteAcademicStudentId(effectiveStudentId)) {
            persistAcademicCredentials(schedulePrefs, effectiveStudentId, effectivePassword, rememberPassword)
            setAcademicAccountBound(schedulePrefs, true)
            addKnownAcademicAccountId(schedulePrefs, effectiveStudentId)
            setBoundAcademicStudentId(schedulePrefs, effectiveStudentId)
            val restored = resolveAcademicAccountProfile(schedulePrefs, effectiveStudentId)
            val updated = restored.copy(
                realName = uiState.studentName,
                nickname = restored.nickname.ifBlank { uiState.studentName },
            )
            accountProfile = updated
            saveAcademicAccountProfile(schedulePrefs, updated, effectiveStudentId)
        }
    }

    LaunchedEffect(uiState.loggedIn) {
        if (!uiState.loggedIn) {
            logoutInProgress = false
        }
    }

    LaunchedEffect(accountProfile.nickname, nicknameFocused) {
        if (!nicknameFocused) {
            nicknameInput = accountProfile.nickname
        }
    }

    LaunchedEffect(boundStudentId) {
        accountProfile = when {
            isLikelyCompleteAcademicStudentId(boundStudentId) -> resolveAcademicAccountProfile(schedulePrefs, boundStudentId)
            else -> AcademicAccountProfile("", "", "")
        }
    }

    LaunchedEffect(boundStudentId, uiState.loggedIn) {
        if (boundStudentId.isBlank() && !uiState.loggedIn) {
            accountProfile = AcademicAccountProfile("", "", "")
            nicknameInput = ""
        }
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
    ) {
        ScreenColumn {
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("教务账号", style = MaterialTheme.typography.headlineMedium, color = palette.ink)
                        ActionPill("返回", WarmMist, onClick = onBack)
                    }
                }
            }
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AcademicAvatarBadge(
                            displayName = if (hasBoundAccount) displayName else previewDisplayName,
                            studentId = if (hasBoundAccount) boundStudentId else loginStudentId.trim(),
                            avatarUri = if (hasBoundAccount) accountProfile.avatarUri else typedPreviewProfile.avatarUri,
                            accent = palette.primary,
                            size = 76.dp,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                if (hasBoundAccount) displayName else previewDisplayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.ink,
                            )
                            Text(
                                text = if (hasBoundAccount) {
                                    "学号 $boundStudentId"
                                } else {
                                    loginStudentId.trim().ifBlank { "未填写学号" }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.softText,
                            )
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                                color = accountStatusAccent.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, accountStatusAccent.copy(alpha = 0.18f)),
                            ) {
                                Text(
                                    text = accountStatusText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = accountStatusAccent,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ActionPill("选择头像", palette.primary) {
                            avatarPicker.launch("image/*")
                        }
                        if (hasBoundAccount && accountProfile.avatarUri.isNotBlank()) {
                            ActionPill("恢复默认", WarmMist) {
                                val updated = accountProfile.copy(avatarUri = "")
                                accountProfile = updated
                                if (isLikelyCompleteAcademicStudentId(boundStudentId)) {
                                    saveAcademicAccountProfile(schedulePrefs, updated, boundStudentId)
                                }
                            }
                        }
                    }
                }
            }
            item {
                GlassCard {
                    if (hasBoundAccount) {
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.18f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("姓名", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                                Text(realName, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.18f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("账号", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                                Text(boundStudentId, style = MaterialTheme.typography.titleMedium, color = palette.ink)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp), color = Color.White.copy(alpha = 0.18f)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text("密码", style = MaterialTheme.typography.labelLarge, color = palette.softText)
                                Text(
                                    if (boundPassword.isNotBlank()) "已保存" else "未保存",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.ink,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = nicknameInput,
                            onValueChange = {
                                nicknameInput = it
                                val updated = accountProfile.copy(nickname = it.trim())
                                accountProfile = updated
                                if (isLikelyCompleteAcademicStudentId(boundStudentId)) {
                                    saveAcademicAccountProfile(schedulePrefs, updated, boundStudentId)
                                }
                            },
                            label = { Text("昵称") },
                            singleLine = true,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    val wasFocused = nicknameFocused
                                    nicknameFocused = focusState.isFocused
                                    if (wasFocused && !focusState.isFocused && nicknameInput.isBlank()) {
                                        val fallback = realName.trim()
                                        val updated = accountProfile.copy(nickname = fallback)
                                        accountProfile = updated
                                        nicknameInput = fallback
                                        if (isLikelyCompleteAcademicStudentId(boundStudentId)) {
                                            saveAcademicAccountProfile(schedulePrefs, updated, boundStudentId)
                                        }
                                    }
                                },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ActionPill("刷新课表", MossGreen) {
                                scope.launch { repository.refreshCurrent() }
                            }
                            ActionPill("退出登录", WarmMist) {
                                logoutInProgress = true
                                repository.logout()
                                clearCurrentAcademicBinding(schedulePrefs)
                                accountProfile = AcademicAccountProfile("", "", "")
                                nicknameInput = ""
                                loginStudentId = ""
                                loginPassword = ""
                                rememberPassword = true
                                focusManager.clearFocus()
                            }
                        }
                    } else {
                        Text("请输入教务账号和密码完成首次登录。", style = MaterialTheme.typography.bodyLarge, color = palette.softText)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = loginStudentId,
                            onValueChange = { loginStudentId = it },
                            label = { Text("学号") },
                            singleLine = true,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("密码") },
                            singleLine = true,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ToggleLine("记住密码并自动登录", rememberPassword) { rememberPassword = it }
                        Spacer(modifier = Modifier.height(12.dp))
                        ActionPill(
                            text = if (uiState.loading) "连接中" else "连接教务",
                            background = ForestGreen,
                        ) {
                            scope.launch {
                                repository.connectAndLoad(loginStudentId.trim(), loginPassword)
                            }
                        }
                    }
                    if (uiState.status.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(uiState.status, style = MaterialTheme.typography.bodyMedium, color = palette.softText)
                    }
                }
            }
        }
    }
}
