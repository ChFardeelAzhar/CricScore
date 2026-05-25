package com.cricscore.app.ui.tournament.playingeleven

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = NavyDark),
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
                // Large Selection Counter & Progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$selectedCount / $playersPerSide Selected",
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = if (isLimitReached) LimeAccent else TextWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = selectedCount.toFloat() / playersPerSide
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isLimitReached) LimeAccent else MaterialTheme.colorScheme.primary,
                        trackColor = BorderGray
                    )
                }

                // Selected players strip (horizontal)
                if (state.selectedPlayers.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NavyDark)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(state.selectedPlayers, key = { "selected_${it.id}" }) { player ->
                            InputChip(
                                selected = true,
                                onClick = { viewModel.togglePlayerSelection(player) },
                                label = {
                                    val name = player.playerName.split(" ").firstOrNull() ?: player.playerName
                                    Text(
                                        text = name,
                                        fontFamily = DMSans,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(10.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = InputChipDefaults.inputChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                                    selectedTrailingIconColor = MaterialTheme.colorScheme.primary
                                ),
                                border = InputChipDefaults.inputChipBorder(
                                    selected = true,
                                    enabled = true,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    selectedBorderWidth = 1.dp
                                )
                            )
                        }
                    }
                }

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
                        items(state.allSquadPlayers, key = { it.id }) { player ->
                            val isSelected = state.selectedPlayers.any { it.id == player.id }
                            val isRowEnabled = isSelected || !isLimitReached

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else BorderGray,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = isRowEnabled) {
                                        viewModel.togglePlayerSelection(player)
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.togglePlayerSelection(player) },
                                    enabled = isRowEnabled,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = TextGray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                // Initial letter tag instead of Jersey tag
                                val initial = player.playerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(OrangeTertiary.copy(alpha = 0.1f), CircleShape)
                                        .border(1.dp, OrangeTertiary.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        fontFamily = BarlowCondensed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = OrangeTertiary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                // Player Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = player.playerName,
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isRowEnabled) TextWhite else TextGray
                                        )
                                        if (player.isCaptain) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(id = R.string.captain_badge),
                                                fontFamily = DMSans,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = OrangeTertiary,
                                                modifier = Modifier
                                                    .background(OrangeTertiary.copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (player.isWicketKeeper) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(id = R.string.wk_badge),
                                                fontFamily = DMSans,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF9B59B6),
                                                modifier = Modifier
                                                    .background(Color(0xFF9B59B6).copy(alpha = 0.1f), RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (player.role) {
                                            "BATSMAN" -> "Batsman"
                                            "BOWLER" -> "Bowler"
                                            "WICKET_KEEPER" -> "Wicket Keeper"
                                            else -> "All-Rounder"
                                        },
                                        fontFamily = DMSans,
                                        fontSize = 11.sp,
                                        color = TextGray
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
                                    Toast.makeText(context, "Note: Captain is not in selection", Toast.LENGTH_SHORT).show()
                                }
                                if (!state.wicketKeeperIncluded) {
                                    Toast.makeText(context, "Warning: No Wicket Keeper in selection", Toast.LENGTH_SHORT).show()
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
