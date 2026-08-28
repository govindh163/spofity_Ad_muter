package com.example.spofity_ad_muter

import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import kotlinx.coroutines.*

/**
 * Ultra battery-conscious Audio Manager.
 * Uses transient WakeLocks (max 500ms) only during volume transitions to guarantee execution
 * when screen is off, without keeping the CPU awake continuously.
 */
class VolumeManager(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val prefs = AppPreferences.getInstance(context)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var unmuteJob: Job? = null

    @Volatile
    var isCurrentlyMuted: Boolean = false
        private set

    private var previousVolume: Int = -1

    /**
     * Mutes media stream immediately upon ad detection.
     */
    @Synchronized
    fun muteAudio(): Boolean {
        unmuteJob?.cancel()

        if (isCurrentlyMuted) {
            return false // Already muted, skip redundant work
        }

        withTransientWakeLock(500L) {
            val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            // Save original volume
            if (currentVol > 0) {
                previousVolume = currentVol
                prefs.savedVolume = currentVol
            } else if (previousVolume <= 0) {
                previousVolume = prefs.savedVolume.takeIf { it > 0 } ?: (maxVol / 2)
            }

            val targetVolume = if (prefs.isLowVolumeMode) 1 else 0
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            isCurrentlyMuted = true
        }

        return true
    }

    /**
     * Restores media volume after a configurable delay to prevent audio popping.
     */
    @Synchronized
    fun unmuteAudio(onUnmuted: (() -> Unit)? = null) {
        if (!isCurrentlyMuted) return

        unmuteJob?.cancel()
        val delayMs = prefs.unmuteDelayMs

        unmuteJob = scope.launch {
            if (delayMs > 0) {
                delay(delayMs)
            }

            withTransientWakeLock(500L) {
                val restoreVol = when {
                    previousVolume > 0 -> previousVolume
                    prefs.savedVolume > 0 -> prefs.savedVolume
                    else -> audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2
                }

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restoreVol, 0)
                isCurrentlyMuted = false
            }

            onUnmuted?.invoke()
        }
    }

    /**
     * Executes block within a brief, auto-releasing WakeLock.
     * Prevents CPU sleeping during the critical millisecond of volume adjustment.
     */
    private inline fun withTransientWakeLock(timeoutMs: Long, crossinline block: () -> Unit) {
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SpotifyAdMuter:VolumeTransitionWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(timeoutMs)
        }

        try {
            block()
        } finally {
            try {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (_: Exception) {}
        }
    }

    fun cleanUp() {
        unmuteJob?.cancel()
        scope.cancel()
    }
}
