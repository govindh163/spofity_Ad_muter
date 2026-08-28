package com.example.spofity_ad_muter.data

/**
 * Real-time playback status of Spotify on the device.
 */
data class PlaybackStatus(
    val title: String = "",
    val artist: String = "",
    val isAd: Boolean = false,
    val isMuted: Boolean = false,
    val isPlaying: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Historical log item for muted ad activity.
 */
data class AdLogEntry(
    val id: String,
    val title: String,
    val artist: String,
    val timestamp: Long = System.currentTimeMillis(),
    val estimatedSilenceSeconds: Long = 15L
)

/**
 * Aggregate metrics and performance statistics.
 */
data class MuterStatistics(
    val totalAdsMuted: Int = 0,
    val todayAdsMuted: Int = 0,
    val totalSilenceSeconds: Long = 0L
)

/**
 * User configuration and fine-tuning options.
 */
data class AppConfig(
    val isServiceActive: Boolean = true,
    val unmuteDelayMs: Long = 400L,
    val isLowVolumeMode: Boolean = false,
    val showNotifications: Boolean = true,
    val autoStartOnBoot: Boolean = true
)

/**
 * Internal event stream messages.
 */
sealed class AppEvent {
    data class PlaybackUpdated(val status: PlaybackStatus) : AppEvent()
    data class StatsUpdated(val stats: MuterStatistics) : AppEvent()
    data class ServiceConnectionChanged(val isConnected: Boolean) : AppEvent()
}
