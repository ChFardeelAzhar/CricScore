package com.cricscore.app.ui.inningssetup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.ui.theme.*

@Composable
fun PlayerSelectionDialog(
    title: String,
    players: List<TeamPlayer>,
    selectedPlayerName: String,
    excludePlayerName1: String,
    excludePlayerName2: String,
    onPlayerSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Cancel",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        title = {
            Text(
                text = title,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextWhite
            )
        },
        text = {
            if (players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No players available.",
                        fontFamily = DMSans,
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                val chunkedPlayers = players.chunked(3)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chunkedPlayers) { rowPlayers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPlayers.forEach { player ->
                                val isAlreadySelectedElsewhere = player.playerName.trim().equals(excludePlayerName1.trim(), ignoreCase = true) ||
                                        player.playerName.trim().equals(excludePlayerName2.trim(), ignoreCase = true)
                                val isCurrentSelection = player.playerName.trim().equals(selectedPlayerName.trim(), ignoreCase = true)

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    PlayerGridItem(
                                        player = player,
                                        isSelected = isCurrentSelection,
                                        isDisabled = isAlreadySelectedElsewhere,
                                        onClick = {
                                            onPlayerSelected(player.playerName)
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                            // Fill remainder of row with empty space
                            repeat(3 - rowPlayers.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    )
}

@Composable
fun PlayerGridItem(
    player: TeamPlayer,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val initial = player.playerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(enabled = !isDisabled) { onClick() }
            .border(
                1.dp,
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isDisabled -> BorderGray.copy(alpha = 0.2f)
                    else -> BorderGray
                },
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                isDisabled -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Badges overlay in top-right
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (player.isCaptain) {
                    Text(
                        text = "©",
                        fontFamily = DMSans,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeTertiary,
                        modifier = Modifier
                            .background(OrangeTertiary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
                if (player.isWicketKeeper) {
                    Text(
                        text = "🧤",
                        fontSize = 9.sp,
                        modifier = Modifier
                            .background(Color(0xFF9B59B6).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            // Main content column
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circle with Initial Letter
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (isDisabled) TextGray.copy(alpha = 0.1f) else OrangeTertiary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isDisabled) TextGray.copy(alpha = 0.2f) else OrangeTertiary.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDisabled) TextGray else OrangeTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Player name
                Text(
                    text = player.playerName,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (isDisabled) TextGray else TextWhite,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Player role label or custom subtitle
                Text(
                    text = subtitle ?: when (player.role) {
                        "BATSMAN" -> "Batsman"
                        "BOWLER" -> "Bowler"
                        "WICKET_KEEPER" -> "WK"
                        else -> "All-Rounder"
                    },
                    fontFamily = DMSans,
                    fontSize = 10.sp,
                    color = TextGray,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selected overlay indicator
            if (isDisabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NavyDark.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selected",
                        fontFamily = DMSans,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WicketRed,
                        modifier = Modifier
                            .background(WicketRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
