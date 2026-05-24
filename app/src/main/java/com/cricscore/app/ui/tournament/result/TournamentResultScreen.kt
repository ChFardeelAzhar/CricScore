package com.cricscore.app.ui.tournament.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.domain.model.TournamentPlayerStat
import com.cricscore.app.domain.model.TournamentTeam
import com.cricscore.app.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentResultScreen(
    viewModel: TournamentResultViewModel,
    tournamentId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = tournamentId) {
        viewModel.loadTournamentResult(tournamentId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tournament Standings",
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = LimeAccent)
            } else if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "An error occurred",
                    color = ErrorRed,
                    fontFamily = DMSans,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val tournament = uiState.tournament
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Title Banner
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (tournament?.name ?: "TOURNAMENT").uppercase(),
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = LimeAccent,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "OFFICIAL RESULTS & AWARDS",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextGray,
                            letterSpacing = 2.sp
                        )
                    }

                    // Champion Gold Card
                    uiState.champion?.let { champ ->
                        ChampionCard(team = champ)
                    }

                    // Podium View (2nd & 3rd placements)
                    if (uiState.runnerUp != null || uiState.thirdPlace != null) {
                        PodiumView(
                            runnerUp = uiState.runnerUp,
                            thirdPlace = uiState.thirdPlace
                        )
                    }

                    // Individual Awards Section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "INDIVIDUAL AWARDS",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BlueSecondary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AwardCard(
                                    title = "ORANGE CAP",
                                    subtitle = "Top Run Scorer",
                                    player = uiState.orangeCap,
                                    statText = uiState.orangeCap?.let { "${it.totalRuns} Runs" } ?: "N/A",
                                    capColor = OrangeTertiary,
                                    emoji = "🧢"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AwardCard(
                                    title = "PURPLE CAP",
                                    subtitle = "Top Wicket Taker",
                                    player = uiState.purpleCap,
                                    statText = uiState.purpleCap?.let { "${it.wicketsTaken} Wkts" } ?: "N/A",
                                    capColor = Color(0xFF9D4EDD), // Purple
                                    emoji = "🧢"
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                AwardCard(
                                    title = "SIXES KING",
                                    subtitle = "Most Sixes Hit",
                                    player = uiState.topSixes,
                                    statText = uiState.topSixes?.let { "${it.totalSixes} Sixes" } ?: "N/A",
                                    capColor = LimeAccent,
                                    emoji = "💥"
                                )
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                AwardCard(
                                    title = "FOURS LEADER",
                                    subtitle = "Most Fours Hit",
                                    player = uiState.topFours,
                                    statText = uiState.topFours?.let { "${it.totalFours} Fours" } ?: "N/A",
                                    capColor = BlueSecondary,
                                    emoji = "4️⃣"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NavySurface,
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, BorderGray),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Back to Tournament Hub",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ChampionCard(team: TournamentTeam, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, OrangeTertiary),
        colors = CardDefaults.cardColors(containerColor = NavySurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OrangeTertiary.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(OrangeTertiary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trophy),
                        contentDescription = "Trophy",
                        tint = OrangeTertiary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "TOURNAMENT CHAMPION",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = OrangeTertiary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = team.teamName.uppercase(),
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .background(NavyDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val colorHex = team.colorHex
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(android.graphics.Color.parseColor(colorHex)), CircleShape)
                    )
                    Text(
                        text = "PTS: ${team.points}",
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextWhite
                    )
                    Text(
                        text = "NRR: ${String.format(Locale.US, "%+.3f", team.netRunRate)}",
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = LimeAccent
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumView(
    runnerUp: TournamentTeam?,
    thirdPlace: TournamentTeam?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "RUNNERS UP",
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextGray,
                letterSpacing = 1.sp
            )

            runnerUp?.let { runUp ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BorderGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextWhite
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(android.graphics.Color.parseColor(runUp.colorHex)), CircleShape)
                        )
                        Text(
                            text = runUp.teamName,
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    }
                    Text(
                        text = "${runUp.points} PTS · NRR ${String.format(Locale.US, "%+.3f", runUp.netRunRate)}",
                        fontFamily = BarlowCondensed,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }

            if (thirdPlace != null) {
                Divider(color = BorderGray, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BorderGray.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(android.graphics.Color.parseColor(thirdPlace.colorHex)), CircleShape)
                        )
                        Text(
                            text = thirdPlace.teamName,
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextWhite
                        )
                    }
                    Text(
                        text = "${thirdPlace.points} PTS · NRR ${String.format(Locale.US, "%+.3f", thirdPlace.netRunRate)}",
                        fontFamily = BarlowCondensed,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
fun AwardCard(
    title: String,
    subtitle: String,
    player: TournamentPlayerStat?,
    statText: String,
    capColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(capColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = statText,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = capColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = capColor,
                letterSpacing = 1.sp
            )

            Text(
                text = player?.playerName ?: "No Stats",
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextWhite,
                maxLines = 1
            )

            Text(
                text = player?.teamName ?: subtitle,
                fontFamily = DMSans,
                fontSize = 10.sp,
                color = TextGray,
                maxLines = 1
            )
        }
    }
}
