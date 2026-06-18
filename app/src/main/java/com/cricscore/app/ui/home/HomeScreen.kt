package com.cricscore.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartNewMatchClick: () -> Unit,
    onNavigateToTournaments: () -> Unit,
    onNavigateToTournamentMatches: (Long) -> Unit,
    onMatchClick: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentMatches by viewModel.recentMatches.collectAsStateWithLifecycle()
    val activeMatch by viewModel.activeMatch.collectAsStateWithLifecycle()
    val ongoingTournament by viewModel.ongoingTournament.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header CricScore Branding

            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "app_logo",
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Yellow,
                        spotColor = Color.Yellow
                    )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.tagline),
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Start New Match Button
            Button(
                onClick = onStartNewMatchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = NavyDark
                )
            ) {
                Text(
                    text = stringResource(id = R.string.start_new_match),
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tournaments Button
            Button(
                onClick = onNavigateToTournaments,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavySurface,
                    contentColor = TextWhite
                ),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Text(
                    text = "🏆 TOURNAMENTS & LEAGUES",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Ongoing Tournament Card
            ongoingTournament?.let { tournament ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = OrangeTertiary.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OrangeTertiary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTournamentMatches(tournament.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ONGOING LEAGUE",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = OrangeTertiary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tournament.name,
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📍 ${tournament.venue} · ${tournament.totalTeams} Teams",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trophy),
                            contentDescription = null,
                            tint = OrangeTertiary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Continue Match Section
            activeMatch?.let { match ->
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(
                            alpha = 0.15f
                        )
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMatchClick(match) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.continue_match).uppercase(),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${match.team1} vs ${match.team2}",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check), // or arrow
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Matches Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_recent_matches),
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.recent_matches),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextGray
                    )
                }

            }


            Spacer(modifier = Modifier.height(12.dp))

            if (recentMatches.isEmpty()) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bat_bowl),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.no_recent_matches),
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }


            } else {
                val matchesToShow = remember(recentMatches, activeMatch) {
                    recentMatches.filter { it.id != activeMatch?.id }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(matchesToShow, key = { it.id }) { match ->
                        RecentMatchItem(match = match, onClick = { onMatchClick(match) })
                    }
                }
            }
        }
    }
}

@Composable
fun RecentMatchItem(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(match.createdAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
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
                    text = "${match.team1} vs ${match.team2}",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val (statusText, statusColor) = when (match.status) {
                MatchStatus.UPCOMING -> Pair("Toss Pending", MaterialTheme.colorScheme.primary)
                MatchStatus.FIRST_INNINGS -> Pair(
                    "Live · 1st Innings in progress",
                    MaterialTheme.colorScheme.primary
                )

                MatchStatus.SECOND_INNINGS -> Pair(
                    "Live · 2nd Innings in progress",
                    MaterialTheme.colorScheme.primary
                )

                MatchStatus.COMPLETED -> {
                    val result = if (match.isTied) {
                        "Match Tied"
                    } else if (match.winnerTeam != null) {
                        val margin = if (match.winMarginRuns > 0) {
                            "${match.winMarginRuns} runs"
                        } else {
                            "${match.winMarginWickets} wickets"
                        }
                        "${match.winnerTeam} won by $margin"
                    } else {
                        "Match Completed"
                    }
                    Pair(result, TextGray)
                }
            }

            Text(
                text = statusText,
                fontFamily = DMSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = statusColor
            )
        }
    }
}
