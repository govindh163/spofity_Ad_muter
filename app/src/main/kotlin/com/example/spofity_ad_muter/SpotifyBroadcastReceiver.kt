package com.example.spofity_ad_muter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.spofity_ad_muter.data.AppEvent
import com.example.spofity_ad_muter.data.EventBus
import com.example.spofity_ad_muter.data.PlaybackStatus

/**
 * Passive BroadcastReceiver for Spotify "Device Broadcast Status" events.
 * Operates purely on intent delivery, consuming 0% idle battery.
 */
class SpotifyBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val SPOTIFY_METADATA_CHANGED = "com.spotify.music.metadatachanged"
        const val SPOTIFY_PLAYBACK_STATE_CHANGED = "com.spotify.music.playbackstatechanged"
        const val SPOTIFY_QUEUE_CHANGED = "com.spotify.music.queuechanged"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        val prefs = AppPreferences.getInstance(context)

        // Handle Boot Completed: Auto-start foreground service if enabled
        if (action == Intent.ACTION_BOOT_COMPLETED) {
            if (prefs.autoStartOnBoot && prefs.isServiceActive) {
                val serviceIntent = Intent(context, AdMuterForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            return
        }

        if (!prefs.isServiceActive) return

        when (action) {
            SPOTIFY_METADATA_CHANGED, SPOTIFY_PLAYBACK_STATE_CHANGED -> {
                val id = intent.getStringExtra("id") ?: ""
                val artist = intent.getStringExtra("artist") ?: ""
                val track = intent.getStringExtra("track") ?: ""
                val playing = intent.getBooleanExtra("playing", true)

                val isAd = id.startsWith("spotify:ad:", ignoreCase = true) ||
                        track.equals("Advertisement", ignoreCase = true) ||
                        (artist.equals("Spotify", ignoreCase = true) && track.isEmpty())

                val volumeManager = VolumeManager(context)

                if (isAd && playing) {
                    volumeManager.muteAudio()
                    prefs.recordAdMuted(15L)
                    val status = PlaybackStatus(
                        title = track.ifEmpty { "Advertisement" },
                        artist = artist.ifEmpty { "Spotify Sponsor" },
                        isAd = true,
                        isMuted = true,
                        isPlaying = true
                    )
                    EventBus.emit(AppEvent.PlaybackUpdated(status))
                    EventBus.emit(AppEvent.StatsUpdated(prefs.getStatistics()))
                } else if (!isAd && playing) {
                    volumeManager.unmuteAudio {
                        val status = PlaybackStatus(
                            title = track,
                            artist = artist,
                            isAd = false,
                            isMuted = false,
                            isPlaying = true
                        )
                        EventBus.emit(AppEvent.PlaybackUpdated(status))
                    }
                }
            }
        }
    }
}
