package com.poxiao.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.poxiao.app.ui.PoxiaoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装 Android 12+ 闪屏
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 解锁高刷新率限制 (支持 90Hz/120Hz/144Hz)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.let { win ->
                val modes = win.windowManager.defaultDisplay.supportedModes
                val maxMode = modes.maxByOrNull { it.refreshRate }
                if (maxMode != null) {
                    val lp = win.attributes
                    lp.preferredDisplayModeId = maxMode.modeId
                    win.attributes = lp
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            PoxiaoApp()
        }
    }
}
