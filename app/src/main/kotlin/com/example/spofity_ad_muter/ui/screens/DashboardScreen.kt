package com.example.spofity_ad_muter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.components.LiveStatusBanner
import com.example.spofity_ad_muter.ui.components.MasterPowerCard
import com.example.spofity_ad_muter.ui.components.StatsOverview
import com.example.spofity_ad_muter.ui.theme.*
import com.example.spofity_ad_muter.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackStatus by viewModel.playbackStatus.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val config by viewModel.config.collectAsState()
    val isNotificationAccessGranted by viewModel.isNotificationAccessGranted.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header & Efficiency Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Spotify Ad Muter",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Android 16 • Zero Polling Engine",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }

            // Battery efficiency chip
            Surface(
                color = SpotifyGreenGlow,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.border(1.dp, SpotifyGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Battery Efficient",
                        tint = SpotifyGreenBright,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "< 0.5% BATTERY",
                        style = MaterialTheme.typography.labelSmall,
                        color = SpotifyGreenBright
                    )
                }
            }
        }

        // Permission Banner (if not granted)
        if (!isNotificationAccessGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WarningAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Setup Needed",
                            tint = WarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Setup Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Grant notification access so the muter can detect ads.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToSetup,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningAmber,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Fix", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        // Master Switch Card
        MasterPowerCard(
            isActive = config.isServiceActive,
            onToggle = { viewModel.toggleServiceActive(it) }
        )

        // Live Playback / Mute Status Banner
        LiveStatusBanner(
            status = playbackStatus,
            isServiceActive = config.isServiceActive
        )

        // Real-Time Statistics
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        StatsOverview(stats = stats)

        // Battery Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery Friendly",
                        tint = SpotifyGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ultra Battery-Friendly Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• Event-driven execution: Zero background polling loops.\n" +
                            "• Instant package filtering: Ignores non-Spotify events in < 0.01ms.\n" +
                            "• Transient WakeLocks: Held only for volume transitions, then immediately freed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
