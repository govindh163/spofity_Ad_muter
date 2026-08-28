package com.example.spofity_ad_muter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.theme.*
import com.example.spofity_ad_muter.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Fine-tune muting behavior and preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Section: Audio Behavior
        Text(
            text = "AUDIO TUNING",
            style = MaterialTheme.typography.labelSmall,
            color = SpotifyGreenBright
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unmute Delay",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Prevents audio popping when returning to song",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        color = DarkSurfaceHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${config.unmuteDelayMs} ms",
                            style = MaterialTheme.typography.labelLarge,
                            color = SpotifyGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Slider(
                    value = config.unmuteDelayMs.toFloat(),
                    onValueChange = { viewModel.setUnmuteDelay(it.toLong()) },
                    valueRange = 0f..1200f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = SpotifyGreen,
                        activeTrackColor = SpotifyGreen,
                        inactiveTrackColor = DarkSurfaceHighlight
                    )
                )
            }
        }

        // Section: Preferences
        Text(
            text = "SYSTEM & PREFERENCES",
            style = MaterialTheme.typography.labelSmall,
            color = SpotifyGreenBright
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                SettingsToggleItem(
                    title = "Low Volume Mode",
                    description = "Reduces volume to 5% instead of full silence",
                    icon = Icons.Default.VolumeDown,
                    checked = config.isLowVolumeMode,
                    onCheckedChange = { viewModel.setLowVolumeMode(it) }
                )

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(horizontal = 16.dp))

                SettingsToggleItem(
                    title = "Auto-Start on Boot",
                    description = "Automatically resume monitoring after phone restarts",
                    icon = Icons.Default.PowerSettingsNew,
                    checked = config.autoStartOnBoot,
                    onCheckedChange = { viewModel.setAutoStartBoot(it) }
                )

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(horizontal = 16.dp))

                SettingsToggleItem(
                    title = "Status Notification",
                    description = "Show persistent background status notification",
                    icon = Icons.Default.Notifications,
                    checked = config.showNotifications,
                    onCheckedChange = { viewModel.setShowNotifications(it) }
                )
            }
        }

        // Section: Data & Reset
        Text(
            text = "DATA MANAGEMENT",
            style = MaterialTheme.typography.labelSmall,
            color = SpotifyGreenBright
        )

        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MuteActiveRed),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MuteActiveRed.copy(alpha = 0.5f)))
        ) {
            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Statistics & Clear Log")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Statistics?", color = TextPrimary) },
            text = { Text("This will clear your muted ads count and activity log.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetStats()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = MuteActiveRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SpotifyGreen,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkBackground,
                checkedTrackColor = SpotifyGreen,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = DarkSurfaceHighlight
            )
        )
    }
}
