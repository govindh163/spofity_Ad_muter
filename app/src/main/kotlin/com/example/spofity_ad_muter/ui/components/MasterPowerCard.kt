package com.example.spofity_ad_muter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.theme.*

@Composable
fun MasterPowerCard(
    isActive: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) SpotifyGreen else DarkBorder,
        animationSpec = tween(400),
        label = "border_color"
    )

    val glowGradient = if (isActive) {
        Brush.verticalGradient(
            colors = listOf(SpotifyGreenGlow, DarkSurfaceElevated)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(DarkSurfaceElevated, DarkSurface)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glowGradient)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isActive) "MUTER ACTIVE" else "MUTER PAUSED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) SpotifyGreenBright else TextTertiary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isActive) "Auto-Mute Protection ON" else "Protection is Offline",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isActive) "Monitoring Spotify in ultra-low battery mode" else "Tap power to resume auto-muting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Glowing Power Button
                val powerBgColor by animateColorAsState(
                    targetValue = if (isActive) SpotifyGreen else DarkSurfaceHighlight,
                    label = "power_bg"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isActive) DarkBackground else TextSecondary,
                    label = "icon_color"
                )

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(powerBgColor)
                        .clickable { onToggle(!isActive) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Toggle Muter",
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
