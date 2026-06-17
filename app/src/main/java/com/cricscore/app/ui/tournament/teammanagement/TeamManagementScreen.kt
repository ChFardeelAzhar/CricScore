package com.cricscore.app.ui.tournament.teammanagement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.ui.theme.*
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamManagementScreen(
    viewModel: TeamManagementViewModel,
    tournamentId: Long,
    onBackClick: () -> Unit,
    onNavigateToSquad: (Long, Long, String) -> Unit, // teamId, tournamentId, teamName
    modifier: Modifier = Modifier
) {
    LaunchedEffect(key1 = tournamentId) {
        viewModel.initTournament(tournamentId)
    }

    val tournament by viewModel.tournament.collectAsState()
    val teamDetails by viewModel.teamDetails.collectAsState()

    val requiredPlayers = tournament?.playersPerSide ?: 11
    val fullSquadsCount = teamDetails.count { it.playerCount >= requiredPlayers }
    val totalTeams = teamDetails.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.manage_squads),
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
                if (totalTeams > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surface,
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
                                    text = "$fullSquadsCount of $totalTeams teams have full squads",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = "Req: $requiredPlayers players",
                                    fontFamily = DMSans,
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = if (totalTeams > 0) fullSquadsCount.toFloat() / totalTeams else 0f
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LimeAccent,
                                trackColor = BorderGray
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (teamDetails.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No teams found in this tournament",
                    fontFamily = DMSans,
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                items(teamDetails, key = { it.team.id }) { detail ->
                    val team = detail.team
                    val parsedColor = try {
                        Color(team.colorHex.toColorInt())
                    } catch (e: Exception) {
                        BlueSecondary
                    }

                    val isSquadComplete = detail.playerCount >= requiredPlayers

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onNavigateToSquad(team.id, tournamentId, team.teamName)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, if (isSquadComplete) LimeAccent.copy(alpha = 0.3f) else BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left team icon circle
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(parsedColor, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_bat_bowl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                                        .padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))

                            // Middle info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = team.teamName,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (detail.captainName != null) "© ${detail.captainName}" else "No captain set",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (detail.captainName != null) OrangeTertiary else TextGray
                                )

                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (detail.playerCount == 0) {
                                    Text(
                                        text = "⚠ 0 Players",
                                        fontFamily = DMSans,
                                        fontSize = 12.sp,
                                        color = ErrorRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "${detail.playerCount} Players",
                                        fontFamily = DMSans,
                                        fontSize = 12.sp,
                                        color = if (isSquadComplete) LimeAccent else TextGray
                                    )
                                }
                            }
                            Spacer(Modifier.width(5.dp))
                            // Right completeness indicator & arrow
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                when {
                                    detail.playerCount == 0 -> {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(ErrorRed, CircleShape)
                                        )
                                    }
                                    detail.playerCount < requiredPlayers -> {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_warning),
                                            contentDescription = "Incomplete",
                                            tint = OrangeTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_check_circle),
                                            contentDescription = "Complete",
                                            tint = LimeAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
