package com.cricscore.app.ui.inningssetup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.ui.theme.*

@Composable
fun PlayerSelectionDialog(
    title: String,
    players: List<TeamPlayer>,
    selectedPlayerName: String,
    excludePlayerName1: String = "",
    excludePlayerName2: String = "",
    excludePlayerNames: Set<String> = emptySet(),
    onPlayerSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sortedPlayers = remember(players) {
        players.sortedBy { player ->
            when (player.role) {
                "BATSMAN" -> 0
                "WICKET_KEEPER" -> 1
                "ALL_ROUNDER" -> 2
                "BOWLER" -> 3
                else -> 4
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "Cancel",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    color = LimeAccent
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
            if (sortedPlayers.isEmpty()) {
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(sortedPlayers) { index, player ->
                        val isAlreadySelectedElsewhere = player.playerName.trim().equals(excludePlayerName1.trim(), ignoreCase = true) ||
                                player.playerName.trim().equals(excludePlayerName2.trim(), ignoreCase = true) ||
                                excludePlayerNames.any { it.trim().equals(player.playerName.trim(), ignoreCase = true) }
                        val isCurrentSelection = player.playerName.trim().equals(selectedPlayerName.trim(), ignoreCase = true)

                        PlayerSelectionListItem(
                            player = player,
                            index = index,
                            isSelected = isCurrentSelection,
                            isDisabled = isAlreadySelectedElsewhere,
                            onClick = {
                                if (!isAlreadySelectedElsewhere) {
                                    onPlayerSelected(player.playerName)
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = NavyDark,
        shape = RoundedCornerShape(16.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    )
}

@Composable
fun PlayerSelectionListItem(
    player: TeamPlayer,
    index: Int,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val roleIcon = when (player.role) {
        "BOWLER" -> R.drawable.ic_red_bowl
        else -> R.drawable.ic_bat_bowl
    }

    val roleLabel = when (player.role) {
        "BATSMAN" -> "Batsman"
        "BOWLER" -> "Bowler"
        "WICKET_KEEPER" -> "WK Keeper"
        else -> "All-Rounder"
    }

    val roleColor = when (player.role) {
        "BATSMAN" -> Color(0xFF4A90D9)
        "BOWLER" -> Color(0xFFE74C3C)
        "WICKET_KEEPER" -> Color(0xFF9B59B6)
        else -> Color(0xFFF5A623)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled) { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Index Counter
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                fontFamily = BarlowCondensed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisabled) TextGray.copy(alpha = 0.3f) else TextGray.copy(alpha = 0.5f),
                modifier = Modifier.width(28.dp)
            )

            // Category Icon
            Image(
                painter = painterResource(id = roleIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(8.dp),
                alpha = if (isDisabled) 0.5f else 1f
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name and Role Tag
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.playerName,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDisabled) TextGray else TextWhite,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Tag
                    Box(
                        modifier = Modifier
                            .background(
                                if (isDisabled) roleColor.copy(alpha = 0.05f) else roleColor.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = roleLabel.uppercase(),
                            fontFamily = DMSans,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDisabled) roleColor.copy(alpha = 0.5f) else roleColor
                        )
                    }

                    if (player.isCaptain || player.isWicketKeeper) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (player.isCaptain) BadgeIndicatorSmall("C", OrangeTertiary, isDisabled)
                            if (player.isWicketKeeper) BadgeIndicatorSmall("WK", Color(0xFF9B59B6), isDisabled)
                        }
                    }
                }
            }

            if (isDisabled) {
                Text(
                    text = "SELECTED",
                    fontFamily = DMSans,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WicketRed.copy(alpha = 0.7f),
                    modifier = Modifier
                        .background(WicketRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            } else if (isSelected) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BadgeIndicatorSmall(text: String, color: Color, isDisabled: Boolean) {
    Text(
        text = text,
        fontFamily = DMSans,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = if (isDisabled) color.copy(alpha = 0.5f) else color,
        modifier = Modifier
            .background(
                if (isDisabled) color.copy(alpha = 0.05f) else color.copy(alpha = 0.15f),
                RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}
