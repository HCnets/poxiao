package com.poxiao.app.pomodoro

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class NoisePlayer(context: Context) {
    private val appContext = context.applicationContext
    private val cacheDir = File(appContext.cacheDir, "ambient_audio_cache").apply { mkdirs() }
    private val ioExecutor = Executors.newFixedThreadPool(3)
    private val activeDownloads = ConcurrentHashMap.newKeySet<String>()
    private var player: MediaPlayer? = null
    private var currentUrl: String? = null
    private var preparingUrl: String? = null
    private var generation: Long = 0L

    var onFailure: (() -> Unit)? = null
    var onReady: (() -> Unit)? = null

    fun warmUp() {
        NoiseProfile.entries.forEach(::cacheAsync)
    }

    fun isCached(profile: String): Boolean = cacheFileFor(NoiseProfile.from(profile)).exists()

    fun isCaching(profile: String): Boolean = activeDownloads.contains(NoiseProfile.from(profile).playableUrl)

    fun start(profile: String): Boolean {
        val targetProfile = NoiseProfile.from(profile)
        val targetUrl = targetProfile.playableUrl
        val cachedFile = cacheFileFor(targetProfile)

        if (currentUrl == targetUrl && player?.isPlaying == true) {
            return true
        }
        if (preparingUrl == targetUrl) {
            return true
        }

        stop()
        cacheAsync(targetProfile)

        val source = if (cachedFile.exists()) cachedFile.absolutePath else targetUrl
        return startPlayer(targetProfile, source)
    }

    fun stop() {
        generation += 1
        preparingUrl = null
        currentUrl = null
        player?.let { mediaPlayer ->
            mediaPlayer.setOnPreparedListener(null)
            mediaPlayer.setOnErrorListener(null)
            runCatching {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                } else {
                    mediaPlayer.reset()
                }
            }
            runCatching { mediaPlayer.release() }
        }
        player = null
    }

    private fun startPlayer(profile: NoiseProfile, source: String): Boolean {
        val mediaPlayer = MediaPlayer()
        val playGeneration = ++generation
        preparingUrl = profile.playableUrl

        val started = runCatching {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            mediaPlayer.setOnPreparedListener { readyPlayer ->
                if (playGeneration != generation) {
                    runCatching { readyPlayer.release() }
                    return@setOnPreparedListener
                }
                preparingUrl = null
                currentUrl = profile.playableUrl
                readyPlayer.isLooping = true
                readyPlayer.setVolume(profile.volume, profile.volume)
                readyPlayer.start()
                onReady?.invoke()
            }
            mediaPlayer.setOnErrorListener { brokenPlayer, _, _ ->
                if (playGeneration == generation) {
                    preparingUrl = null
                    currentUrl = null
                    if (player === brokenPlayer) {
                        player = null
                    }
                    onFailure?.invoke()
                }
                runCatching { brokenPlayer.reset() }
                runCatching { brokenPlayer.release() }
                true
            }
            mediaPlayer.setDataSource(source)
            player = mediaPlayer
            mediaPlayer.prepareAsync()
        }.isSuccess

        if (!started) {
            runCatching { mediaPlayer.release() }
            player = null
            preparingUrl = null
            currentUrl = null
        }
        return started
    }

    private fun cacheAsync(profile: NoiseProfile) {
        val targetUrl = profile.playableUrl
        val targetFile = cacheFileFor(profile)
        if (targetFile.exists() || !activeDownloads.add(targetUrl)) {
            return
        }
        ioExecutor.execute {
            downloadToCache(targetUrl, targetFile)
            activeDownloads.remove(targetUrl)
        }
    }

    private fun cacheFileFor(profile: NoiseProfile): File =
        File(cacheDir, "${profile.cacheKey}.mp3")

    private fun downloadToCache(sourceUrl: String, targetFile: File): Boolean {
        if (targetFile.exists()) return true
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.part")
        return runCatching {
            val connection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 20000
                instanceFollowRedirects = true
                requestMethod = "GET"
            }
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.length() <= 0L) {
                tempFile.delete()
                false
            } else {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            }
        }.getOrElse {
            tempFile.delete()
            false
        }
    }

    private enum class NoiseProfile(
        val sourceUrl: String,
        val cacheKey: String,
        val volume: Float,
    ) {
        MountainWind("https://down.ear0.com:3321/index/preview?soundid=37753&type=mp3&audio=sound.mp3", "mountain_wind", 0.92f),
        Stream("https://preview.tosound.com:3321/preview?file=freesound%2F0%2F283%2F385870.mp3", "stream", 0.90f),
        Rain("https://preview.tosound.com:3321/preview?file=freesound%2F0%2F281%2F383355.mp3", "rain", 0.90f),
        Wave("https://down.ear0.com:3321/index/preview?soundid=13264&type=mp3&audio=sound.mp3", "wave", 0.90f),
        Campfire("https://down.ear0.com:3321/index/preview?soundid=37750&type=mp3&audio=sound.mp3", "campfire", 0.82f),
        White("https://preview.tosound.com:3321/preview?file=freesound%2F0%2F176%2F278948.mp3", "white_noise", 0.72f),
        Pink("https://preview.tosound.com:3321/preview?file=freesound%2F0%2F118%2F220817.mp3", "pink_noise", 0.48f),
        ;

        val playableUrl: String
            get() = buildPlayableUrl(sourceUrl)

        companion object {
            fun from(raw: String): NoiseProfile {
                val normalized = raw.trim().lowercase().replace(" ", "")
                return when {
                    normalized.isBlank() -> MountainWind
                    normalized.contains("白噪") ||
                        normalized.contains("white") ||
                        normalized.contains("whitenoise") -> White
                    normalized.contains("粉噪") ||
                        normalized.contains("粉红噪") ||
                        normalized.contains("pink") ||
                        normalized.contains("pinknoise") -> Pink
                    normalized.contains("溪流") ||
                        normalized.contains("流水") ||
                        normalized.contains("stream") ||
                        normalized.contains("brook") ||
                        normalized.contains("river") -> Stream
                    normalized.contains("雨") ||
                        normalized.contains("rain") ||
                        normalized.contains("storm") -> Rain
                    normalized.contains("海浪") ||
                        normalized.contains("wave") ||
                        normalized.contains("ocean") -> Wave
                    normalized.contains("篝火") ||
                        normalized.contains("营火") ||
                        normalized.contains("campfire") ||
                        normalized.contains("fireplace") ||
                        normalized.contains("fire") -> Campfire
                    normalized.contains("山风") ||
                        normalized.contains("风") ||
                        normalized.contains("wind") -> MountainWind
                    else -> MountainWind
                }
            }

            private fun buildPlayableUrl(rawUrl: String): String {
                if (!rawUrl.contains("preview.tosound.com") || rawUrl.contains("token=")) {
                    return rawUrl
                }
                val encodedFile = rawUrl.substringAfter("file=", "").substringBefore("&")
                if (encodedFile.isBlank()) {
                    return rawUrl
                }
                val token = Base64.encodeToString(encodedFile.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val withSound = if (rawUrl.contains("sound=")) rawUrl else "$rawUrl&sound=audio.mp3"
                return if (withSound.contains("token=")) withSound else "$withSound&token=$token"
            }
        }
    }
}
