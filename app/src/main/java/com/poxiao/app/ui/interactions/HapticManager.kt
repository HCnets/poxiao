package com.poxiao.app.ui.interactions

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 提供不同强度的全局触觉反馈 (Haptic Feedback) 
 * 区分了轻微点击、成功、失败等不同语义的震感。
 */
class HapticManager(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * 极轻微的触碰反馈，用于导航栏切换、滑块滑动、微小状态改变。
     */
    fun playLightClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * 明确的按压反馈，用于按钮点击、卡片展开。
     */
    fun playHeavyClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * 连续的双重震动，用于 Todo 打勾完成、专注结束等正向激励。
     */
    fun playSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                vibrator?.vibrate(VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.5f)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 1.0f, 50)
                    .compose())
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 20, 60, 20), -1)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    /**
     * 沉重且缓慢的震动，用于警告、错误或删除操作。
     */
    fun playWarning() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40L)
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}

@Composable
fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    return remember(context) { HapticManager(context) }
}