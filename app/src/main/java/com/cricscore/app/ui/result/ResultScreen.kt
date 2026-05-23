package com.cricscore.app.ui.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val playerPerformances by viewModel.playerPerformances.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = playerPerformances) {
        viewModel.selectDefaultPlayerOfMatchIfNeeded(playerPerformances)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.match_result).uppercase(),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        match?.let { currentMatch ->
            val inn1 = inningsList.firstOrNull { it.inningsNumber == 1 }
            val inn2 = inningsList.firstOrNull { it.inningsNumber == 2 }

            val inn1Text = inn1?.let { "${it.totalRuns}/${it.totalWickets} (${CricketCalculator.ballsToOversString(it.ballsBowled)} ov)" } ?: "-"
            val inn2Text = inn2?.let { "${it.totalRuns}/${it.totalWickets} (${CricketCalculator.ballsToOversString(it.ballsBowled)} ov)" } ?: "-"
            val matchScoresSummary = "$inn1Text vs $inn2Text"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 1. Celebration Trophy Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(NavySurface, NavyDark)
                                )
                            )
                            .padding(vertical = 28.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {

                            // Large Trophy Icon
                            Text(
                                text = "\uD83C\uDFC6",
                                fontSize = 90.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Winner Team Name
                            val winnerText = if (currentMatch.isTied) {
                                stringResource(id = R.string.match_tied)
                            } else {
                                currentMatch.winnerTeam ?: "Match Completed"
                            }
                            Text(
                                text = winnerText,
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Win Margin
                            if (!currentMatch.isTied && currentMatch.winnerTeam != null) {
                                val marginText = if (currentMatch.winMarginRuns > 0) {
                                    stringResource(id = R.string.won_by_runs, currentMatch.winMarginRuns)
                                } else {
                                    stringResource(id = R.string.won_by_wickets, currentMatch.winMarginWickets)
                                }
                                Text(
                                    text = marginText,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = LimeAccent,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Subtext: "127/8 (10.0 ov) vs 93/10 (9.2 ov)"
                            Text(
                                text = matchScoresSummary,
                                fontFamily = DMSans,
                                fontSize = 13.sp,
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 2. Innings Comparison Card Row (Image 1 & 2 Layout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    val team1Name = inn1?.battingTeam ?: currentMatch.team1
                    val isTeam1Winner = !currentMatch.isTied && currentMatch.winnerTeam == team1Name
                    val team1Color = if (currentMatch.isTied) TextGray else if (isTeam1Winner) LimeAccent else ErrorRed

                    // 🔥 TEAM 1 CARD
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)), // dark premium bg
                        border = BorderStroke(
                            width = if (isTeam1Winner) 1.5.dp else 1.dp,
                            color = if (isTeam1Winner) LimeAccent else BorderGray
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = team1Name.uppercase(),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = team1Color,
                                maxLines = 1
                            )

                            Text(
                                text = inn1?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "0/0",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp, // 🔥 Bigger score
                                color = Color.White
                            )

                            Text(
                                text = inn1?.let { "${CricketCalculator.ballsToOversString(it.ballsBowled)} Overs" }
                                    ?: "0.0 Overs",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }

                    // ⚡ VS BADGE (IMPROVED)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = Color(0xFF1F2937),
                                shape = CircleShape
                            )
                            .border(1.dp, BorderGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    val team2Name = inn2?.battingTeam ?: currentMatch.team2
                    val isTeam2Winner = !currentMatch.isTied && currentMatch.winnerTeam == team2Name
                    val team2Color = if (currentMatch.isTied) TextGray else if (isTeam2Winner) LimeAccent else ErrorRed

                    // 🔥 TEAM 2 CARD
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        border = BorderStroke(
                            width = if (isTeam2Winner) 1.5.dp else 1.dp,
                            color = if (isTeam2Winner) LimeAccent else BorderGray
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = team2Name.uppercase(),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = team2Color,
                                maxLines = 1
                            )

                            Text(
                                text = inn2?.let { "${it.totalRuns}/${it.totalWickets}" } ?: "0/0",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = Color.White
                            )

                            Text(
                                text = inn2?.let { "${CricketCalculator.ballsToOversString(it.ballsBowled)} Overs" }
                                    ?: "0.0 Overs",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Player of the Match Section (Image 3 Layout)
                val potmName = currentMatch.playerOfMatch ?: ""
                val potmPerformance = playerPerformances.firstOrNull { it.name == potmName }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.player_of_match).uppercase(),
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextGray,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 4.dp, bottom = 8.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Initials Avatar with Purple Gradient Background
                            val initials = if (potmName.isNotBlank()) {
                                val parts = potmName.trim().split("\\s+".toRegex())
                                if (parts.size >= 2) {
                                    "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
                                } else {
                                    potmName.trim().take(2).uppercase()
                                }
                            } else {
                                "PM"
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color(0xFF8A2BE2), Color(0xFF5A0DAD))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextWhite
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (potmName.isNotBlank()) potmName else "Match Winner",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                
                                val performanceText = buildString {
                                    append("⭐ ")
                                    if (potmPerformance != null && potmPerformance.performanceSummary.isNotBlank()) {
                                        append("${potmPerformance.performanceSummary} · ")
                                    }
                                    append(potmPerformance?.description ?: "Outstanding performance")
                                }
                                
                                Text(
                                    text = performanceText,
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB388FF) // Lavender/Light purple text
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Star Button on the right
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(NavyDark.copy(alpha = 0.5f), CircleShape)
                                    .border(1.dp, BorderGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_medal),
                                    contentDescription = null,
                                    tint = OrangeTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Action Row Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onHomeClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LimeAccent,
                            contentColor = NavyDark
                        )
                    ) {

                        Text(
                            text = "\uD83C\uDFCF NEW MATCH",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onViewScorecardClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = TextWhite
                        ),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text(
                            text = "📋 FULL SCORECARD",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                
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
}
