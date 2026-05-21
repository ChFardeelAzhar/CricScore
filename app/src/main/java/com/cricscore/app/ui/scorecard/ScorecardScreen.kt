package com.cricscore.app.ui.scorecard

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.domain.model.BatsmanInnings
import com.cricscore.app.domain.model.BowlerInnings
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(
    viewModel: ScorecardViewModel,
    matchId: Long,
    initialInnings: Int,
    showStartNext: Boolean,
    onBackClick: () -> Unit,
    onStartSecondInningsClick: (Long) -> Unit,
    onViewResultClick: (Long) -> Unit,
    onResumeScoringClick: (Long, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = matchId, key2 = initialInnings) {
        viewModel.initMatch(matchId, initialInnings)
    }

    val match by viewModel.match.collectAsStateWithLifecycle()
    val innings by viewModel.innings.collectAsStateWithLifecycle()
    val inningsNumber by viewModel.inningsNumber.collectAsStateWithLifecycle()
    val batsmen by viewModel.batsmen.collectAsStateWithLifecycle()
    val bowlers by viewModel.bowlers.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = match?.let { "${it.team1} vs ${it.team2}" } ?: stringResource(id = R.string.full_scorecard),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            match?.let { currentMatch ->
                // Innings Selector Tabs
                if (currentMatch.status == MatchStatus.SECOND_INNINGS || currentMatch.status == MatchStatus.COMPLETED) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        val tabModifier = Modifier
                            .weight(1f)
                            .height(36.dp)

                        Button(
                            onClick = { viewModel.setInningsNumber(1) },
                            modifier = tabModifier,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inningsNumber == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (inningsNumber == 1) NavyDark else TextWhite
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "1st Innings",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = { viewModel.setInningsNumber(2) },
                            modifier = tabModifier,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inningsNumber == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (inningsNumber == 2) NavyDark else TextWhite
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "2nd Innings",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    innings?.let { currentInnings ->
                        Spacer(modifier = Modifier.height(8.dp))

                        // Innings summary card
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentInnings.battingTeam.uppercase()} INNINGS",
                                        fontFamily = BarlowCondensed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (currentInnings.isCompleted) {
                                        Text(
                                            text = stringResource(id = R.string.innings_complete).uppercase(),
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = OrangeTertiary,
                                            modifier = Modifier
                                                .background(OrangeTertiary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = "${currentInnings.totalRuns} - ${currentInnings.totalWickets}",
                                        fontFamily = BarlowCondensed,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 36.sp,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${CricketCalculator.ballsToOversString(currentInnings.ballsBowled)} overs)",
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp,
                                        color = TextGray,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = BorderGray, thickness = 1.dp)
                                Spacer(modifier = Modifier.height(12.dp))

                                val totalExtras = currentInnings.extrasWide + currentInnings.extrasNoBall + currentInnings.extrasBye + currentInnings.extrasLegBye
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Extras: $totalExtras",
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "(wd ${currentInnings.extrasWide}, nb ${currentInnings.extrasNoBall}, b ${currentInnings.extrasBye}, lb ${currentInnings.extrasLegBye})",
                                        fontFamily = DMSans,
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Batting Table Section
                    Text(
                        text = stringResource(id = R.string.batting_table_header),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Batting Table Header Row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Batsman",
                                modifier = Modifier.weight(2.2f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = "R",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "B",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "4s",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "6s",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "SR",
                                modifier = Modifier.weight(1f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Batting Table Rows
                    if (batsmen.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No batting details available",
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                    } else {
                        batsmen.forEachIndexed { index, batsman ->
                            val isLast = index == batsmen.lastIndex
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = if (isLast) RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp) else RoundedCornerShape(0.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = batsman.playerName,
                                            modifier = Modifier.weight(2.2f),
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = batsman.runs.toString(),
                                            modifier = Modifier.weight(0.6f),
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextWhite,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = batsman.balls.toString(),
                                            modifier = Modifier.weight(0.6f),
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = TextGray,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = batsman.fours.toString(),
                                            modifier = Modifier.weight(0.6f),
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = TextGray,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = batsman.sixes.toString(),
                                            modifier = Modifier.weight(0.6f),
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = TextGray,
                                            textAlign = TextAlign.End
                                        )
                                        val sr = CricketCalculator.calculateStrikeRate(batsman.runs, batsman.balls)
                                        Text(
                                            text = String.format(java.util.Locale.US, "%.1f", sr),
                                            modifier = Modifier.weight(1f),
                                            fontFamily = DMSans,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                    
                                    val status = if (batsman.isOut) {
                                        batsman.dismissalDescription ?: "out"
                                    } else {
                                        if (batsman.balls == 0 && batsman.runs == 0) {
                                            "yet to bat"
                                        } else {
                                            "not out"
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = status,
                                        fontFamily = DMSans,
                                        fontSize = 11.sp,
                                        color = if (status == "not out") MaterialTheme.colorScheme.primary else TextGray,
                                        modifier = Modifier.padding(start = 0.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bowling Table Section
                    Text(
                        text = stringResource(id = R.string.bowling_table_header),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bowling Table Header Row
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bowler",
                                modifier = Modifier.weight(2f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = "O",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "M",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "R",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "W",
                                modifier = Modifier.weight(0.6f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "Econ",
                                modifier = Modifier.weight(1f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    // Bowling Table Rows
                    if (bowlers.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No bowling details available",
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        }
                    } else {
                        bowlers.forEachIndexed { index, bowler ->
                            val isLast = index == bowlers.lastIndex
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = if (isLast) RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp) else RoundedCornerShape(0.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = bowler.playerName,
                                        modifier = Modifier.weight(2f),
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = CricketCalculator.ballsToOversString(bowler.ballsBowled),
                                        modifier = Modifier.weight(0.6f),
                                        fontFamily = DMSans,
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = bowler.maidens.toString(),
                                        modifier = Modifier.weight(0.6f),
                                        fontFamily = DMSans,
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = bowler.runsConceded.toString(),
                                        modifier = Modifier.weight(0.6f),
                                        fontFamily = DMSans,
                                        fontSize = 14.sp,
                                        color = TextGray,
                                        textAlign = TextAlign.End
                                    )
                                    Text(
                                        text = bowler.wickets.toString(),
                                        modifier = Modifier.weight(0.6f),
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End
                                    )
                                    val econ = CricketCalculator.calculateEconomyRate(bowler.runsConceded, bowler.ballsBowled)
                                    Text(
                                        text = String.format(java.util.Locale.US, "%.2f", econ),
                                        modifier = Modifier.weight(1f),
                                        fontFamily = DMSans,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // BOTTOM ACTION BAR
                val isCompleted = currentMatch.status == MatchStatus.COMPLETED
                val isFirstInnings = currentMatch.status == MatchStatus.FIRST_INNINGS
                val isSecondInnings = currentMatch.status == MatchStatus.SECOND_INNINGS

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NavyDark,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (isCompleted) {
                            Button(
                                onClick = { onViewResultClick(matchId) },
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
                                    text = "View Match Result",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else if (showStartNext && inningsNumber == 1 && isFirstInnings) {
                            Button(
                                onClick = { onStartSecondInningsClick(matchId) },
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
                                    text = stringResource(id = R.string.start_2nd_innings),
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else if (isFirstInnings || isSecondInnings) {
                            Button(
                                onClick = { onResumeScoringClick(matchId, if (isFirstInnings) 1 else 2) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = TextWhite
                                )
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_play),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Resume Scoring",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
