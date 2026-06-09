package com.cricscore.app.ui.tournament.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.domain.model.Tournament
import com.cricscore.app.domain.model.TournamentStatus
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentListScreen(
    viewModel: TournamentListViewModel,
    onBackClick: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToOverview: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val tournaments by viewModel.tournaments.collectAsState()
    var tournamentToDelete by remember { mutableStateOf<Tournament?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.tournaments),
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = NavyDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Create Tournament",
                        tint = NavyDark,
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Create Tournament")
                }

            }
        }
    ) { paddingValues ->
        if (tournaments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_winning_trophy),
                        contentDescription = null,
                        modifier = Modifier.size(130.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tournaments yet",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextWhite
                    )

                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(tournaments, key = { it.id }) { tournament ->
                    TournamentItem(
                        tournament = tournament,
                        onClick = { onNavigateToOverview(tournament.id) },
                        onLongClick = { tournamentToDelete = tournament }
                    )
                }
            }
        }

        // Deletion confirmation dialog
        tournamentToDelete?.let { tournament ->
            AlertDialog(
                onDismissRequest = { tournamentToDelete = null },
                title = {
                    Text(
                        text = "Delete Tournament?",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.delete_tournament_confirm),
                        fontFamily = DMSans,
                        color = TextGray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTournament(tournament)
                            tournamentToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                    ) {
                        Text(
                            text = "Delete",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tournamentToDelete = null }) {
                        Text(
                            text = "Cancel",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                },
                containerColor = NavySurface,
                titleContentColor = TextWhite,
                textContentColor = TextWhite
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TournamentItem(
    tournament: Tournament,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trophy),
                        contentDescription = null,
                        tint = if (tournament.status == TournamentStatus.COMPLETED) OrangeTertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tournament.name,
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextWhite,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusChip(status = tournament.status)
            }

            if (tournament.venue.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${tournament.venue}",
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoLabel(label = "Teams", value = tournament.totalTeams.toString())
                    InfoLabel(label = "Overs", value = tournament.oversPerMatch.toString())
                    InfoLabel(label = "Players", value = tournament.playersPerSide.toString())
                }

                if (tournament.status == TournamentStatus.COMPLETED) {
                    Text(
                        text = "🏆 Winner Declared",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = OrangeTertiary,
                        modifier = Modifier
                            .background(
                                OrangeTertiary.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: TournamentStatus) {
    val bgColor = when (status) {
        TournamentStatus.UPCOMING -> BorderGray
        TournamentStatus.ONGOING -> LimeAccent.copy(alpha = 0.15f)
        TournamentStatus.COMPLETED -> OrangeTertiary.copy(alpha = 0.15f)
    }
    val contentColor = when (status) {
        TournamentStatus.UPCOMING -> TextGray
        TournamentStatus.ONGOING -> LimeAccent
        TournamentStatus.COMPLETED -> OrangeTertiary
    }
    val text = when (status) {
        TournamentStatus.UPCOMING -> "Upcoming"
        TournamentStatus.ONGOING -> "Ongoing"
        TournamentStatus.COMPLETED -> "Completed"
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (status == TournamentStatus.ONGOING) {
                // Pulse indicator placeholder (could be an animation, simple dot is neat)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(LimeAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = contentColor
            )
        }
    }
}

@Composable
fun InfoLabel(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            fontFamily = DMSans,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
        Text(
            text = value,
            fontFamily = BarlowCondensed,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextWhite
        )
    }
}
