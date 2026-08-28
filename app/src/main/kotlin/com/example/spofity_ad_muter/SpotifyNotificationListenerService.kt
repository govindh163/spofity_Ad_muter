package com.example.spofity_ad_muter

import android.app.Notification
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.spofity_ad_muter.data.AppEvent
import com.example.spofity_ad_muter.data.EventBus
import com.example.spofity_ad_muter.data.PlaybackStatus

/**
 * High-performance, zero-polling NotificationListenerService.
 * Only wakes on Spotify notification updates and executes in < 1ms.
 */
class SpotifyNotificationListenerService : NotificationListenerService() {

    companion object {
        const val SPOTIFY_PACKAGE = "com.spotify.music"

        var isConnected: Boolean = false
            private set
    }

    private lateinit var volumeManager: VolumeManager
    private lateinit var prefs: AppPreferences

    private var lastProcessedTitle: String = ""
    private var lastProcessedArtist: String = ""
    private var lastWasAd: Boolean = false

    override fun onCreate() {
        super.onCreate()
        volumeManager = VolumeManager(this)
        prefs = AppPreferences.getInstance(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isConnected = true
        EventBus.emit(AppEvent.ServiceConnectionChanged(true))
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isConnected = false
        EventBus.emit(AppEvent.ServiceConnectionChanged(false))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // 1. Ultra-fast short-circuit: Drop non-Spotify notifications in 0.01ms
        if (sbn.packageName != SPOTIFY_PACKAGE) return

        // 2. Check if user enabled the muter service
        if (!prefs.isServiceActive) return

        val extras = sbn.notification?.extras ?: return

        // 3. Extract notification text components
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

        // 4. Extract MediaSession information if available
        var mediaId: String? = null
        var duration: Long = -1L
        var isPlaying = true

        try {
            val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
            } else {
                @Suppress("DEPRECATION")
                extras.getParcelable(Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
            }

            if (token != null) {
                val controller = MediaController(this, token)
                val metadata = controller.metadata
                val playbackState = controller.playbackState

                if (playbackState != null) {
                    isPlaying = playbackState.state == PlaybackState.STATE_PLAYING
                }

                if (metadata != null) {
                    mediaId = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
                    duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
                }
            }
        } catch (_: Exception) {}

        if (title.isEmpty() && artist.isEmpty()) return

        // 5. Evaluate Multi-Heuristic Ad Rules
        val isAd = isAdvertisement(title, artist, subText, mediaId, duration)

        // 6. Avoid duplicate processing if state has not changed
        if (isAd == lastWasAd && title == lastProcessedTitle && artist == lastProcessedArtist) {
            return
        }

        lastProcessedTitle = title
        lastProcessedArtist = artist
        lastWasAd = isAd

        if (isAd) {
            handleAdDetected(title, artist)
        } else {
            handleRegularTrack(title, artist, isPlaying)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn?.packageName == SPOTIFY_PACKAGE) {
            // Spotify closed/dismissed: Unmute audio safely
            if (volumeManager.isCurrentlyMuted) {
                volumeManager.unmuteAudio()
                val status = PlaybackStatus(
                    title = "",
                    artist = "",
                    isAd = false,
                    isMuted = false,
                    isPlaying = false
                )
                EventBus.emit(AppEvent.PlaybackUpdated(status))
            }
        }
    }

    private fun isAdvertisement(
        title: String,
        artist: String,
        subText: String,
        mediaId: String?,
        duration: Long
    ): Boolean {
        // Direct media ID check (Spotify standard URI for ads)
        if (mediaId != null && (mediaId.startsWith("spotify:ad:", ignoreCase = true) || mediaId.contains(":ad:"))) {
            return true
        }

        // Title matching
        if (title.equals("Advertisement", ignoreCase = true) ||
            (title.equals("Spotify", ignoreCase = true) && artist.isEmpty()) ||
            title.equals("Spotify Free", ignoreCase = true)) {
            return true
        }

        // Artist & Title matching
        if (artist.equals("Spotify", ignoreCase = true) &&
            (title.isEmpty() || title.equals("Advertisement", ignoreCase = true))) {
            return true
        }

        // SubText indicators
        if (subText.contains("Advertisement", ignoreCase = true) ||
            subText.contains("Sponsored", ignoreCase = true)) {
            return true
        }

        return false
    }

    private fun handleAdDetected(title: String, artist: String) {
        val muted = volumeManager.muteAudio()
        if (muted) {
            prefs.recordAdMuted(15L)
        }

        val status = PlaybackStatus(
            title = if (title.isEmpty()) "Advertisement" else title,
            artist = if (artist.isEmpty()) "Spotify Sponsor" else artist,
            isAd = true,
            isMuted = true,
            isPlaying = true
        )
        EventBus.emit(AppEvent.PlaybackUpdated(status))
        EventBus.emit(AppEvent.StatsUpdated(prefs.getStatistics()))
    }

    private fun handleRegularTrack(title: String, artist: String, isPlaying: Boolean) {
        if (volumeManager.isCurrentlyMuted) {
            volumeManager.unmuteAudio {
                val status = PlaybackStatus(
                    title = title,
                    artist = artist,
                    isAd = false,
                    isMuted = false,
                    isPlaying = isPlaying
                )
                EventBus.emit(AppEvent.PlaybackUpdated(status))
            }
        } else {
            val status = PlaybackStatus(
                title = title,
                artist = artist,
                isAd = false,
                isMuted = false,
                isPlaying = isPlaying
            )
            EventBus.emit(AppEvent.PlaybackUpdated(status))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        volumeManager.cleanUp()
    }
}
