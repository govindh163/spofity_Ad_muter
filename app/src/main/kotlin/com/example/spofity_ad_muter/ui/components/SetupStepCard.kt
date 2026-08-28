package com.example.spofity_ad_muter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.spofity_ad_muter.ui.theme.*

@Composable
fun SetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    actionButtonText: String,
    icon: ImageVector,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isCompleted) SpotifyGreen.copy(alpha = 0.4f) else DarkBorder
    val containerBg = if (isCompleted) DarkSurfaceElevated else DarkSurface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) SpotifyGreen else DarkSurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = DarkBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "STEP $stepNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCompleted) SpotifyGreenBright else TextTertiary
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                }

                // Completion Tag
                Surface(
                    color = if (isCompleted) SpotifyGreenGlow else DarkSurfaceHighlight,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isCompleted) "ENABLED" else "ACTION NEEDED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) SpotifyGreenBright else WarningAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) DarkSurfaceHighlight else SpotifyGreen,
                    contentColor = if (isCompleted) TextPrimary else DarkBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = actionButtonText,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
