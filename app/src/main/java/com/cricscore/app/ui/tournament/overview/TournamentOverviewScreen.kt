package com.cricscore.app.ui.tournament.overview

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.domain.model.Fixture
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.model.TournamentPlayerStat
import com.cricscore.app.domain.model.TournamentStatus
import com.cricscore.app.domain.model.TournamentTeam
import com.cricscore.app.ui.theme.*
import com.cricscore.app.ui.tournament.list.StatusChip
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentOverviewScreen(
    viewModel: TournamentOverviewViewModel,
    tournamentId: Long,
    onBackClick: () -> Unit,
    onStartMatch: (Long, Long) -> Unit, // (matchId, fixtureId)
    onResumeMatch: (Long) -> Unit, // matchId
    onViewScorecard: (Long) -> Unit, // matchId
    onNavigateToResult: (Long) -> Unit, // tournamentId
    onNavigateToTeamManagement: (Long) -> Unit, // tournamentId
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = tournamentId) {
        viewModel.initTournament(tournamentId)
    }

    val tournament by viewModel.tournament.collectAsState()
    val fixtures by viewModel.fixtures.collectAsState()
    val pointsTable by viewModel.pointsTable.collectAsState()
    val playerStats by viewModel.playerStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Fixtures", "Points Table", "Stats")

    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tournament?.name ?: "Tournament Overview",
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1
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
                actions = {
                    tournament?.let { t ->
                        IconButton(onClick = { onNavigateToTeamManagement(t.id) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_groups),
                                contentDescription = stringResource(id = R.string.manage_squads),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        if (t.status == TournamentStatus.COMPLETED) {
                            IconButton(onClick = { onNavigateToResult(t.id) }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trophy),
                                    contentDescription = "View Winner",
                                    tint = OrangeTertiary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            // "Score Next Match" FAB if ongoing
            tournament?.let { t ->
                if (t.status == TournamentStatus.ONGOING || t.status == TournamentStatus.UPCOMING) {
                    val nextScheduled = fixtures.firstOrNull { it.status == "SCHEDULED" }
                    if (nextScheduled != null && selectedTab == 0) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.startFixtureMatch(
                                    nextScheduled,
                                    onMatchCreated = { mId -> onStartMatch(mId, nextScheduled.id) },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = NavyDark,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Score Next Match",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        tournament?.let { t ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header overview details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (t.venue.isNotBlank()) {
                                Text(
                                    text = "📍 ${t.venue}",
                                    fontFamily = DMSans,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                            Text(
                                text = "${t.totalTeams} Teams · ${t.oversPerMatch} Overs · ${t.playersPerSide} Per Side",
                                fontFamily = DMSans,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                        StatusChip(status = t.status)
                    }
                }

                // Tab selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { Divider(color = BorderGray) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (selectedTab) {
                        0 -> FixturesTab(
                            fixtures = fixtures,
                            onStartMatch = { fixture ->
                                viewModel.startFixtureMatch(
                                    fixture,
                                    onMatchCreated = { mId -> onStartMatch(mId, fixture.id) },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                                )
                            },
                            onResumeMatch = onResumeMatch,
                            onViewScorecard = onViewScorecard
                        )
                        1 -> PointsTableTab(
                            teams = pointsTable
                        )
                        2 -> StatsTab(
                            playerStats = playerStats
                        )
                    }
                }
            }
        }
    }
}

// 1. FIXTURES TAB COMPOSABLE
@Composable
fun FixturesTab(
    fixtures: List<Fixture>,
    onStartMatch: (Fixture) -> Unit,
    onResumeMatch: (Long) -> Unit,
    onViewScorecard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (fixtures.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(id = R.string.no_fixtures_yet),
                fontFamily = DMSans,
                color = TextGray,
                fontSize = 14.sp
            )
        }
    } else {
        val groupedFixtures = remember(fixtures) {
            fixtures.groupBy { it.roundNumber }
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            groupedFixtures.forEach { (round, roundFixtures) ->
                item {
                    Text(
                        text = stringResource(id = R.string.round_n, round).uppercase(),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(roundFixtures, key = { it.id }) { fixture ->
                    FixtureCard(
                        fixture = fixture,
                        onStartMatch = { onStartMatch(fixture) },
                        onResumeMatch = { onResumeMatch(fixture.linkedMatchId) },
                        onViewScorecard = { onViewScorecard(fixture.linkedMatchId) }
                    )
                }
            }
        }
    }
}

@Composable
fun FixtureCard(
    fixture: Fixture,
    onStartMatch: () -> Unit,
    onResumeMatch: () -> Unit,
    onViewScorecard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = fixture.status != "SCHEDULED") {
                if (fixture.status == "LIVE") onResumeMatch() else onViewScorecard()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.match_n, fixture.matchNumber),
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextGray
                )
                
                // Status indicator
                val statusText = when (fixture.status) {
                    "SCHEDULED" -> "Scheduled"
                    "LIVE" -> "Live Scoring"
                    "COMPLETED" -> "Completed"
                    else -> "No Result"
                }
                val statusColor = when (fixture.status) {
                    "SCHEDULED" -> TextGray
                    "LIVE" -> LimeAccent
                    "COMPLETED" -> MaterialTheme.colorScheme.secondary
                    else -> ErrorRed
                }
                Text(
                    text = statusText,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = statusColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1 Name
                Text(
                    text = fixture.team1Name,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (fixture.winnerId == fixture.team1Id) TextWhite else if (fixture.winnerId != -1L) TextGray else TextWhite,
                    modifier = Modifier.weight(1f)
                )

                if (fixture.status == "COMPLETED") {
                    Text(
                        text = fixture.team1Score,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (fixture.winnerId == fixture.team1Id) LimeAccent else TextGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else {
                    Text(
                        text = "vs",
                        fontFamily = DMSans,
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                // Team 2 Name
                Text(
                    text = fixture.team2Name,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (fixture.winnerId == fixture.team2Id) TextWhite else if (fixture.winnerId != -1L) TextGray else TextWhite,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )

                if (fixture.status == "COMPLETED") {
                    Text(
                        text = fixture.team2Score,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (fixture.winnerId == fixture.team2Id) LimeAccent else TextGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Results Details / Action Button
            if (fixture.status == "COMPLETED" && fixture.resultSummary.isNotBlank()) {
                Text(
                    text = fixture.resultSummary,
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = LimeAccent
                )
            } else if (fixture.status == "SCHEDULED") {
                Button(
                    onClick = onStartMatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(32.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.start_match),
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// 2. POINTS TABLE COMPOSABLE
@Composable
fun PointsTableTab(
    teams: List<TournamentTeam>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sticky Columns Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
            Text(text = "TEAM", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
            Text(text = "P", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.Center)
            Text(text = "W", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.Center)
            Text(text = "L", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.Center)
            Text(text = "PTS", modifier = Modifier.width(36.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.Center)
            Text(text = "NRR", modifier = Modifier.width(54.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (teams.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No teams available", fontFamily = DMSans, color = TextGray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(teams) { index, team ->
                    val rank = index + 1
                    val isTopTwo = rank <= 2
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isTopTwo) LimeAccent.copy(alpha = 0.5f) else BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rank.toString(),
                                modifier = Modifier.width(24.dp),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isTopTwo) LimeAccent else TextWhite
                            )

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(android.graphics.Color.parseColor(team.colorHex)), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = team.teamName,
                                    fontFamily = DMSans,
                                    fontWeight = if (isTopTwo) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextWhite,
                                    maxLines = 1
                                )
                            }

                            Text(text = team.matchesPlayed.toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.Center)
                            Text(text = team.won.toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.Center)
                            Text(text = team.lost.toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.Center)
                            
                            Text(
                                text = team.points.toString(),
                                modifier = Modifier.width(36.dp),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isTopTwo) LimeAccent else TextWhite,
                                textAlign = TextAlign.Center
                            )

                            val nrrString = String.format(Locale.US, "%+.3f", team.netRunRate)
                            Text(
                                text = nrrString,
                                modifier = Modifier.width(54.dp),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (team.netRunRate > 0) LimeAccent else if (team.netRunRate < 0) ErrorRed else TextWhite,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Legend: P=Played · W=Won · L=Lost · PTS=Points · NRR=Net Run Rate\nTop 2 teams qualify for highlights.",
            fontFamily = DMSans,
            fontSize = 11.sp,
            color = TextGray,
            lineHeight = 16.sp
        )
    }
}

// 3. STATS TAB COMPOSABLE
@Composable
fun StatsTab(
    playerStats: List<TournamentPlayerStat>,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = listOf("Batting", "Bowling", "Sixes", "Fours", "Best Score")

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { index, title ->
                val isSelected = selectedCategory == index
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else BorderGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategory = index }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextWhite
                    )
                }
            }
        }

        if (playerStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_stats_yet),
                    fontFamily = DMSans,
                    color = TextGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedCategory) {
                    0 -> BattingStatsView(playerStats = playerStats)
                    1 -> BowlingStatsView(playerStats = playerStats)
                    2 -> SixesStatsView(playerStats = playerStats)
                    3 -> FoursStatsView(playerStats = playerStats)
                    4 -> BestScoresStatsView(playerStats = playerStats)
                }
            }
        }
    }
}

// BATTING VIEW
@Composable
fun BattingStatsView(playerStats: List<TournamentPlayerStat>) {
    val topRuns = remember(playerStats) {
        playerStats.filter { it.totalRuns > 0 }.sortedByDescending { it.totalRuns }.take(10)
    }

    if (topRuns.isEmpty()) {
        EmptyStatsPlaceholder()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Orange Cap Card
        item {
            val leader = topRuns.first()
            CapLeaderCard(
                title = "ORANGE CAP",
                name = leader.playerName,
                team = leader.teamName,
                stat = "${leader.totalRuns} Runs",
                color = OrangeTertiary,
                emoji = "🧢"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "PLAYER", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "RUNS", modifier = Modifier.width(44.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "BALLS", modifier = Modifier.width(44.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "SR", modifier = Modifier.width(48.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "HS", modifier = Modifier.width(36.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
            }
        }

        itemsIndexed(topRuns) { index, player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = (index + 1).toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.playerName, fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text(text = player.teamName, fontFamily = DMSans, fontSize = 11.sp, color = TextGray)
                    }
                    Text(text = player.totalRuns.toString(), modifier = Modifier.width(44.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OrangeTertiary, textAlign = TextAlign.End)
                    Text(text = player.totalBalls.toString(), modifier = Modifier.width(44.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                    
                    val sr = if (player.totalBalls > 0) (player.totalRuns.toFloat() / player.totalBalls) * 100f else 0f
                    Text(text = String.format(Locale.US, "%.1f", sr), modifier = Modifier.width(48.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                    
                    val hsText = "${player.highestScore}${if (player.highestScoreNotOut) "*" else ""}"
                    Text(text = hsText, modifier = Modifier.width(36.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// BOWLING VIEW
@Composable
fun BowlingStatsView(playerStats: List<TournamentPlayerStat>) {
    val topWickets = remember(playerStats) {
        playerStats.filter { it.oversBowled > 0 || it.ballsBowled > 0 }
            .sortedWith(compareByDescending<TournamentPlayerStat> { it.wicketsTaken }.thenBy { it.runsConceded })
            .take(10)
    }

    if (topWickets.isEmpty()) {
        EmptyStatsPlaceholder()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Purple Cap Card
        item {
            val leader = topWickets.first()
            CapLeaderCard(
                title = "PURPLE CAP",
                name = leader.playerName,
                team = leader.teamName,
                stat = "${leader.wicketsTaken} Wkts",
                color = Color(0xFF9D4EDD), // Purple
                emoji = "🧢"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "PLAYER", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "WKT", modifier = Modifier.width(32.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "OVERS", modifier = Modifier.width(44.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "RUNS", modifier = Modifier.width(36.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "ECON", modifier = Modifier.width(40.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "BEST", modifier = Modifier.width(44.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
            }
        }

        itemsIndexed(topWickets) { index, player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = (index + 1).toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.playerName, fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text(text = player.teamName, fontFamily = DMSans, fontSize = 11.sp, color = TextGray)
                    }
                    Text(text = player.wicketsTaken.toString(), modifier = Modifier.width(32.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB388FF), textAlign = TextAlign.End)
                    Text(text = "${player.oversBowled}.${player.ballsBowled}", modifier = Modifier.width(44.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                    Text(text = player.runsConceded.toString(), modifier = Modifier.width(36.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                    
                    val totalBalls = player.oversBowled * 6 + player.ballsBowled
                    val econ = if (totalBalls > 0) (player.runsConceded.toFloat() / totalBalls) * 6f else 0f
                    Text(text = String.format(Locale.US, "%.2f", econ), modifier = Modifier.width(40.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                    
                    Text(text = "${player.bestBowlingWickets}/${player.bestBowlingRuns}", modifier = Modifier.width(44.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// SIXES VIEW
@Composable
fun SixesStatsView(playerStats: List<TournamentPlayerStat>) {
    val topSixes = remember(playerStats) {
        playerStats.filter { it.totalSixes > 0 }.sortedByDescending { it.totalSixes }.take(10)
    }

    if (topSixes.isEmpty()) {
        EmptyStatsPlaceholder()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "PLAYER", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "💥 SIXES", modifier = Modifier.width(60.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "MATCHES", modifier = Modifier.width(60.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
            }
        }

        itemsIndexed(topSixes) { index, player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = (index + 1).toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.playerName, fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text(text = player.teamName, fontFamily = DMSans, fontSize = 11.sp, color = TextGray)
                    }
                    Text(text = player.totalSixes.toString(), modifier = Modifier.width(60.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangeTertiary, textAlign = TextAlign.End)
                    Text(text = player.matchesPlayed.toString(), modifier = Modifier.width(60.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// FOURS VIEW
@Composable
fun FoursStatsView(playerStats: List<TournamentPlayerStat>) {
    val topFours = remember(playerStats) {
        playerStats.filter { it.totalFours > 0 }.sortedByDescending { it.totalFours }.take(10)
    }

    if (topFours.isEmpty()) {
        EmptyStatsPlaceholder()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "PLAYER", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "4️⃣ FOURS", modifier = Modifier.width(60.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "MATCHES", modifier = Modifier.width(60.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
            }
        }

        itemsIndexed(topFours) { index, player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = (index + 1).toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.playerName, fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text(text = player.teamName, fontFamily = DMSans, fontSize = 11.sp, color = TextGray)
                    }
                    Text(text = player.totalFours.toString(), modifier = Modifier.width(60.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.End)
                    Text(text = player.matchesPlayed.toString(), modifier = Modifier.width(60.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// BEST SCORE VIEW
@Composable
fun BestScoresStatsView(playerStats: List<TournamentPlayerStat>) {
    val topScores = remember(playerStats) {
        playerStats.filter { it.highestScore > 0 }.sortedByDescending { it.highestScore }.take(10)
    }

    if (topScores.isEmpty()) {
        EmptyStatsPlaceholder()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", modifier = Modifier.width(24.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "PLAYER", modifier = Modifier.weight(1f), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray)
                Text(text = "🎯 SCORE", modifier = Modifier.width(60.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
                Text(text = "BALLS", modifier = Modifier.width(50.dp), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextGray, textAlign = TextAlign.End)
            }
        }

        itemsIndexed(topScores) { index, player ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = (index + 1).toString(), modifier = Modifier.width(24.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = player.playerName, fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Text(text = player.teamName, fontFamily = DMSans, fontSize = 11.sp, color = TextGray)
                    }
                    
                    val hsText = "${player.highestScore}${if (player.highestScoreNotOut) "*" else ""}"
                    Text(text = hsText, modifier = Modifier.width(60.dp), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LimeAccent, textAlign = TextAlign.End)
                    Text(text = player.totalBalls.toString(), modifier = Modifier.width(50.dp), fontFamily = BarlowCondensed, fontSize = 14.sp, color = TextWhite, textAlign = TextAlign.End)
                }
            }
        }
    }
}

// Shared widgets for stats
@Composable
fun CapLeaderCard(
    title: String,
    name: String,
    team: String,
    stat: String,
    color: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = color,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextWhite
                )
                Text(
                    text = team,
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Text(
                text = stat,
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = color
            )
        }
    }
}

@Composable
fun EmptyStatsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No statistical records found for this category.",
            fontFamily = DMSans,
            color = TextGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
