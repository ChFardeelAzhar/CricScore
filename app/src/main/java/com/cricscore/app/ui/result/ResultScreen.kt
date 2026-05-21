package com.cricscore.app.ui.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    matchId: Long,
    onHomeClick: () -> Unit,
    onViewScorecardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = matchId) {
        viewModel.loadMatch(matchId)
    }

    val match by viewModel.match.collectAsStateWithLifecycle()
    val inningsList by viewModel.inningsList.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.match_result),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Trophy / Medal Icon Section with custom background glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_cricscore_logo), // Using the logo or custom trophy if available
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            match?.let { currentMatch ->
                Text(
                    text = "${currentMatch.team1} vs ${currentMatch.team2}",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                val winnerText = if (currentMatch.isTied) {
                    stringResource(id = R.string.match_tied)
                } else if (currentMatch.winnerTeam != null) {
                    val margin = if (currentMatch.winMarginRuns > 0) {
                        stringResource(id = R.string.won_by_runs, currentMatch.winMarginRuns)
                    } else {
                        stringResource(id = R.string.won_by_wickets, currentMatch.winMarginWickets)
                    }
                    "${currentMatch.winnerTeam} $margin"
                } else {
                    "Match Completed"
                }

                Text(
                    text = winnerText,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                // POTM Section if exists
                currentMatch.playerOfMatch?.let { potm ->
                    if (potm.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.player_of_match),
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = potm,
                                    fontFamily = BarlowCondensed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Innings Summary Cards
                Text(
                    text = "MATCH SUMMARY",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextGray,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val inn1 = inningsList.firstOrNull { it.inningsNumber == 1 }
                val inn2 = inningsList.firstOrNull { it.inningsNumber == 2 }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Innings 1 Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = inn1?.battingTeam ?: currentMatch.team1,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = inn1?.let { "(${CricketCalculator.ballsToOversString(it.ballsBowled)} Overs)" } ?: "Not Batted",
                                    fontFamily = DMSans,
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = inn1?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "-",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = BorderGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Innings 2 Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = inn2?.battingTeam ?: currentMatch.team2,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = inn2?.let { "(${CricketCalculator.ballsToOversString(it.ballsBowled)} Overs)" } ?: "Not Batted",
                                    fontFamily = DMSans,
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = inn2?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "-",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                Button(
                    onClick = onViewScorecardClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = TextWhite
                    ),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text(
                        text = stringResource(id = R.string.full_scorecard),
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onHomeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = NavyDark
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.new_match),
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(id = R.string.footer_tagline),
                fontFamily = DMSans,
                fontSize = 11.sp,
                color = TextGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
