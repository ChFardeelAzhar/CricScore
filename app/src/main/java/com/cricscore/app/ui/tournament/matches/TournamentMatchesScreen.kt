package com.cricscore.app.ui.tournament.matches

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import com.cricscore.app.ui.home.RecentMatchItem
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentMatchesScreen(
    viewModel: TournamentMatchesViewModel,
    tournamentId: Long,
    onBackClick: () -> Unit,
    onMatchClick: (Match) -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = tournamentId) {
        viewModel.initTournament(tournamentId)
    }

    val tournament by viewModel.tournament.collectAsStateWithLifecycle()
    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val activeMatch by viewModel.activeMatch.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament?.name ?: "Tournament Matches",
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // More Tournament Details Button
            Button(
                onClick = { onNavigateToDetails(tournamentId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavySurface,
                    contentColor = TextWhite
                ),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trophy),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = OrangeTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW TOURNAMENT DETAILS",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }

            // Active Match Card (if any)
            activeMatch?.let { match ->
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "LIVE MATCH",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
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
                                text = "${match.team1} vs ${match.team2}",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when(match.status) {
                                    MatchStatus.FIRST_INNINGS -> "1st Innings in progress"
                                    MatchStatus.SECOND_INNINGS -> "2nd Innings in progress"
                                    else -> "Starting soon"
                                },
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Matches Header
            Text(
                text = "RECENT MATCHES",
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextGray,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            if (matches.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No matches played in this tournament yet.",
                        fontFamily = DMSans,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val matchesToShow = remember(matches, activeMatch) {
                    matches.filter { it.id != activeMatch?.id }
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
