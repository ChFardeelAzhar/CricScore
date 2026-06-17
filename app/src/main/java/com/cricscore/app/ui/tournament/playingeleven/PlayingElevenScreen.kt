package com.cricscore.app.ui.tournament.playingeleven

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeIndicator(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = DMSans,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), CircleShape)
            .padding(horizontal = 10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayingElevenScreen(
    viewModel: PlayingElevenViewModel,
    fixtureId: Long,
    teamId: Long,
    playersPerSide: Int,
    isLastTeam: Boolean,
    onBackClick: () -> Unit,
    onConfirmSuccess: () -> Unit,
    onNavigateToSquadSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = fixtureId, key2 = teamId) {
        viewModel.initSetup(fixtureId, teamId, playersPerSide)
    }

    val state by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    val sortedPlayers by remember(state.allSquadPlayers) {
        derivedStateOf {
            state.allSquadPlayers.sortedBy { player ->
                when (player.role) {
                    "BATSMAN" -> 0
                    "WICKET_KEEPER" -> 1
                    "ALL_ROUNDER" -> 2
                    "BOWLER" -> 3
                    else -> 4
                }
            }
        }
    }

    LaunchedEffect(key1 = event) {
        event?.let { ev ->
            when (ev) {
                PlayingElevenEvent.SavedSuccessfully -> {
                    Toast.makeText(context, "Playing XI confirmed!", Toast.LENGTH_SHORT).show()
                    onConfirmSuccess()
                }

                is PlayingElevenEvent.Error -> {
                    Toast.makeText(context, ev.message, Toast.LENGTH_LONG).show()
                }
            }
            viewModel.clearEvent()
        }
    }

    val selectedCount = state.selectedPlayers.size
    val isLimitReached = selectedCount == playersPerSide

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id = R.string.select_playing_xi),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                text = state.teamName,
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        Text(
                            text = "$selectedCount / $playersPerSide Selected",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isLimitReached) LimeAccent else TextWhite
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (state.allSquadPlayers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No players registered in this squad yet.",
                    fontFamily = DMSans,
                    color = TextWhite,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "You need to add players to the squad before setting the Playing XI.",
                    fontFamily = DMSans,
                    color = TextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onNavigateToSquadSetup,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = NavyDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Go to Squad Management",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                // Main Squad Checklist
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp)
                    ) {
                        itemsIndexed(sortedPlayers, key = { _, it -> it.id }) { index, player ->
                            val isSelected = state.selectedPlayers.any { it.id == player.id }
                            val isRowEnabled = isSelected || !isLimitReached

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
                                    .clickable(enabled = isRowEnabled) {
                                        viewModel.togglePlayerSelection(player)
                                    },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
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
                                        color = TextGray.copy(alpha = 0.5f),
                                        modifier = Modifier.width(28.dp)
                                    )


                                    // Category Icon
                                    Image(
                                        painter = painterResource(id = roleIcon),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                MaterialTheme.colorScheme.background.copy(
                                                    alpha = 0.5f
                                                )
                                            )
                                            .padding(12.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Name and Role Tag
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = player.playerName,
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isRowEnabled) TextWhite else TextGray,
                                            maxLines = 1
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Role Tag
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        roleColor.copy(alpha = 0.1f),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(horizontal = 9.dp)
                                            ) {
                                                Text(
                                                    text = roleLabel.uppercase(),
                                                    fontFamily = DMSans,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = roleColor
                                                )
                                            }

                                            if (player.isCaptain || player.isWicketKeeper) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    if (player.isCaptain) BadgeIndicator(
                                                        "C",
                                                        OrangeTertiary
                                                    )
                                                    if (player.isWicketKeeper) BadgeIndicator(
                                                        "WK",
                                                        Color(0xFF9B59B6)
                                                    )
                                                }
                                            }
                                        }


                                    }
                                    // Selection Checkbox
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { viewModel.togglePlayerSelection(player) },
                                        enabled = isRowEnabled,
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = MaterialTheme.colorScheme.primary,
                                            uncheckedColor = TextGray
                                        )
                                    )

                                }
                            }
                        }
                    }

                    // Sticky Bottom Buttons Panel
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { viewModel.selectAll() }) {
                                Text(
                                    text = "Select First $playersPerSide",
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = { viewModel.clearAll() }) {
                                Text(
                                    text = "Clear All",
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (state.allSquadPlayers.any { it.isCaptain } && !state.captainIncluded) {
                                    Toast.makeText(
                                        context,
                                        "Note: Captain is not in selection",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                if (!state.wicketKeeperIncluded) {
                                    Toast.makeText(
                                        context,
                                        "Warning: No Wicket Keeper in selection",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                viewModel.confirmSelection()
                            },
                            enabled = isLimitReached && !state.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = NavyDark
                            )
                        ) {
                            val btnText = if (isLimitReached) {
                                stringResource(id = R.string.confirm_xi)
                            } else {
                                "Select ${playersPerSide - selectedCount} more"
                            }
                            Text(
                                text = btnText,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
