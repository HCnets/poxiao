import re

def refactor_calculator(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add imports
    if 'LocalHapticFeedback' not in content:
        content = content.replace('import androidx.compose.ui.platform.LocalContext\n', 
                                  'import androidx.compose.ui.platform.LocalContext\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType\nimport androidx.compose.ui.platform.LocalHapticFeedback\n')

    # 2. Add isDarkMode to ScientificCalculatorScreen
    if 'var isDarkMode by remember' not in content:
        content = content.replace(
            'val routeIcon = when {',
            '''// 主题切换状态
    var isDarkMode by remember { mutableStateOf(false) }

    // 当切换为深色模式时，改变系统状态栏颜色
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    val routeIcon = when {'''
        )

    # 3. Add CompositionLocalProvider
    if 'LocalLiquidGlassStylePreset provides' not in content:
        content = content.replace(
            'Surface(\n            modifier = modifier.fillMaxSize()',
            '''com.poxiao.app.ui.CompositionLocalProvider(
        com.poxiao.app.ui.LocalLiquidGlassStylePreset provides if (isDarkMode) com.poxiao.app.ui.LiquidGlassStylePreset.Hyper else com.poxiao.app.ui.LiquidGlassStylePreset.IOS
    ) {
        Surface(
            modifier = modifier.fillMaxSize()'''
        )
        # We need to close the provider at the end of ScientificCalculatorScreen.
        # Find the end of ScientificCalculatorScreen
        # It's right before "@Composable\nprivate fun CalculatorScrollableModule"
        content = content.replace(
            '        }\n    }\n}\n\n@Composable\nprivate fun CalculatorScrollableModule',
            '        }\n    }\n    }\n}\n\n@Composable\nprivate fun CalculatorScrollableModule'
        )

    # 4. detectTapGestures
    content = content.replace(
        'modifier = modifier.fillMaxSize()',
        'modifier = modifier.fillMaxSize().pointerInput(Unit) {\n                detectTapGestures(onTap = {\n                    if (currentRoute is CalculatorRoute.App && (currentRoute as CalculatorRoute.App).app != CalculatorApp.Compute) {\n                        focusTarget = FocusTarget.None\n                    }\n                })\n            }'
    )
    if 'import androidx.compose.foundation.gestures.detectTapGestures' not in content:
        content = content.replace('import androidx.compose.foundation.gestures.detectHorizontalDragGestures\n',
                                  'import androidx.compose.foundation.gestures.detectHorizontalDragGestures\nimport androidx.compose.foundation.gestures.detectTapGestures\n')

    # 5. Background color
    content = content.replace(
        'Box(modifier = Modifier.fillMaxSize()) {',
        'Box(modifier = Modifier.fillMaxSize().background(if (isDarkMode) Color.Black.copy(alpha = 0.5f) else Color.Transparent)) {'
    )

    # 6. Update CalculatorWorkspaceHeader call
    content = content.replace(
        'directoryOpen = showDirectory\n                )',
        'directoryOpen = showDirectory,\n                    isDarkMode = isDarkMode,\n                    onToggleTheme = { isDarkMode = !isDarkMode }\n                )'
    )

    # 7. Update CalculatorWorkspaceHeader definition
    content = content.replace(
        'directoryOpen: Boolean,\n) {',
        'directoryOpen: Boolean,\n    isDarkMode: Boolean,\n    onToggleTheme: () -> Unit,\n) {'
    )
    
    toggle_btn = '''Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.clickable(onClick = onToggleTheme),
                    shape = CalculatorInnerShape,
                    color = Color.White.copy(alpha = 0.16f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = "切换主题",
                            tint = PineInk,
                        )
                    }
                }
                Surface('''
    content = content.replace(
        '''Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(''', toggle_btn)

    if 'import androidx.compose.material.icons.outlined.LightMode' not in content:
        content = content.replace('import androidx.compose.material.icons.outlined.KeyboardHide\n',
                                  'import androidx.compose.material.icons.outlined.KeyboardHide\nimport androidx.compose.material.icons.outlined.LightMode\nimport androidx.compose.material.icons.outlined.DarkMode\n')

    # Save intermediate
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

if __name__ == '__main__':
    refactor_calculator('app/src/main/java/com/poxiao/app/calculator/ScientificCalculator.kt')
