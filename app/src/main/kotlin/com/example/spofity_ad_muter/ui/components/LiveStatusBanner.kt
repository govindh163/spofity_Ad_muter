package com.example.spofity_ad_muter.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.data.PlaybackStatus
import com.example.spofity_ad_muter.ui.theme.*

@Composable
fun LiveStatusBanner(
    status: PlaybackStatus,
    isServiceActive: Boolean,
    modifier: Modifier = Modifier
) {
    val isAdActive = status.isAd && status.isMuted
    val isMusicPlaying = status.isPlaying && !status.isAd && status.title.isNotEmpty()

    val cardBg = when {
        !isServiceActive -> DarkSurface
        isAdActive -> Color(0xFF241416)
        isMusicPlaying -> Color(0xFF14241A)
        else -> DarkSurface
    }

    val cardBorder = when {
        !isServiceActive -> DarkBorder
        isAdActive -> MuteActiveRed.copy(alpha = 0.6f)
        isMusicPlaying -> SpotifyGreen.copy(alpha = 0.5f)
        else -> DarkBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Container
            val iconBg = when {
                !isServiceActive -> DarkSurfaceHighlight
                isAdActive -> MuteActiveRedGlow
                isMusicPlaying -> SpotifyGreenGlow
                else -> DarkSurfaceHighlight
            }

            val iconTint = when {
                !isServiceActive -> TextTertiary
                isAdActive -> MuteActiveRed
                isMusicPlaying -> SpotifyGreenBright
                else -> TextSecondary
            }

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isAdActive -> Icons.Default.VolumeOff
                        isMusicPlaying -> Icons.Default.VolumeUp
                        else -> Icons.Default.MusicNote
                    },
                    contentDescription = "Status Icon",
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Playback Details
            Column(modifier = Modifier.weight(1f)) {
                val headerText = when {
                    !isServiceActive -> "Service Offline"
                    isAdActive -> "ADVERTISEMENT DETECTED"
                    isMusicPlaying -> "PLAYING SPOTIFY"
                    else -> "WAITING FOR PLAYBACK"
                }

                val headerColor = when {
                    !isServiceActive -> TextTertiary
                    isAdActive -> MuteActiveRed
                    isMusicPlaying -> SpotifyGreenBright
                    else -> TextTertiary
                }

                Text(
                    text = headerText,
                    style = MaterialTheme.typography.labelSmall,
                    color = headerColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                val mainText = when {
                    !isServiceActive -> "Muter is paused"
                    isAdActive -> "Media Volume Silenced 🔇"
                    isMusicPlaying -> status.title
                    else -> "No active Spotify track"
                }

                Text(
                    text = mainText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isMusicPlaying && status.artist.isNotEmpty()) {
                    Text(
                        text = status.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (isAdActive) {
                    Text(
                        text = "Unmuting automatically when song resumes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Waveform visualizer
            WaveVisualizer(
                isPlaying = isMusicPlaying,
                isMuted = isAdActive
            )
        }
    }
}
