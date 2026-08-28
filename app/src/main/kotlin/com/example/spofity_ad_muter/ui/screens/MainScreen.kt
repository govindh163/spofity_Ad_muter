package com.example.spofity_ad_muter.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.theme.*
import com.example.spofity_ad_muter.ui.viewmodel.MainViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    Dashboard("Home", Icons.Default.Home),
    Setup("Setup", Icons.Default.CheckCircleOutline),
    Activity("Activity", Icons.Default.FormatListBulleted),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.Dashboard) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = SpotifyGreenBright,
                            indicatorColor = SpotifyGreen,
                            unselectedIconColor = TextTertiary,
                            unselectedTextColor = TextTertiary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            NavigationTab.Dashboard -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToSetup = { selectedTab = NavigationTab.Setup },
                modifier = Modifier.padding(innerPadding)
            )
            NavigationTab.Setup -> SetupWizardScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            NavigationTab.Activity -> ActivityLogScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            NavigationTab.Settings -> SettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
