package com.example.spofity_ad_muter.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.theme.MuteActiveRed
import com.example.spofity_ad_muter.ui.theme.SpotifyGreen
import com.example.spofity_ad_muter.ui.theme.TextTertiary

@Composable
fun WaveVisualizer(
    isPlaying: Boolean,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    maxHeight: Dp = 28.dp,
    minHeight: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")

    val barColor = when {
        isMuted -> MuteActiveRed
        isPlaying -> SpotifyGreen
        else -> TextTertiary
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val animDuration = if (isPlaying && !isMuted) (450 + (i * 120)) else 1000

            val animatedFraction by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isPlaying && !isMuted) 1f else 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val currentHeight = if (isPlaying && !isMuted) {
                minHeight + (maxHeight - minHeight) * animatedFraction
            } else if (isMuted) {
                minHeight + 2.dp
            } else {
                minHeight
            }

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(currentHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
