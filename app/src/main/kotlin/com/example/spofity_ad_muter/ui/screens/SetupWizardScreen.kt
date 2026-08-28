package com.example.spofity_ad_muter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.components.SetupStepCard
import com.example.spofity_ad_muter.ui.theme.*
import com.example.spofity_ad_muter.ui.viewmodel.MainViewModel

@Composable
fun SetupWizardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isNotificationAccessGranted by viewModel.isNotificationAccessGranted.collectAsState()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Setup & Permissions",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Configure your phone for automatic muting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            IconButton(onClick = { viewModel.checkPermissions() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Status",
                    tint = SpotifyGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Step 1: Notification Access
        SetupStepCard(
            stepNumber = 1,
            title = "Notification Access (Required)",
            description = "Allows the app to instantly detect when Spotify switches from music to an advertisement.",
            isCompleted = isNotificationAccessGranted,
            actionButtonText = if (isNotificationAccessGranted) "Permission Granted" else "Grant Notification Access",
            icon = Icons.Default.NotificationsActive,
            onActionClick = {
                viewModel.openNotificationAccessSettings(context)
            }
        )

        // Step 2: Spotify Broadcast Status
        SetupStepCard(
            stepNumber = 2,
            title = "Spotify Broadcast Status",
            description = "Open Spotify Settings > scroll down to 'Device Broadcast Status' and turn it ON. This enables secondary real-time broadcast signals.",
            isCompleted = true, // User can manually verify / open
            actionButtonText = "Open Spotify Settings",
            icon = Icons.Default.Radio,
            onActionClick = {
                viewModel.openSpotifySettings(context)
            }
        )

        // Step 3: Battery Optimization Exemption
        SetupStepCard(
            stepNumber = 3,
            title = "Unrestricted Battery Mode",
            description = "Prevent Android OS from killing the muter when your screen is locked or Spotify plays in background.",
            isCompleted = isBatteryOptimizationIgnored,
            actionButtonText = if (isBatteryOptimizationIgnored) "Battery Unrestricted" else "Set Unrestricted",
            icon = Icons.Default.BatteryAlert,
            onActionClick = {
                viewModel.requestIgnoreBatteryOptimization(context)
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}
