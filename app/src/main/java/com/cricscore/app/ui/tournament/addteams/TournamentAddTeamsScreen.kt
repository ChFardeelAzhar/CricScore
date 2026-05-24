package com.cricscore.app.ui.tournament.addteams

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentAddTeamsScreen(
    viewModel: TournamentAddTeamsViewModel,
    tournamentId: Long,
    totalTeamsLimit: Int,
    onBackClick: () -> Unit,
    onFixturesGenerated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.teams.collectAsState()
    var teamNameInput by remember { mutableStateOf("") }
    
    val presetColors = listOf(
        "#4A90D9", // blue
        "#E53935", // red
        "#7ED321", // green
        "#F5A623", // amber
        "#9B59B6", // purple
        "#1ABC9C"  // teal
    )
    var selectedColor by remember { mutableStateOf(presetColors[0]) }
    var duplicateError by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.add_teams),
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 2 OF 2 — ADD TEAMS",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = stringResource(id = R.string.teams_added_count, teams.size, totalTeamsLimit),
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (teams.size == totalTeamsLimit) LimeAccent else TextWhite
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { teams.size.toFloat() / totalTeamsLimit },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(BorderGray, RoundedCornerShape(3.dp)),
                color = if (teams.size == totalTeamsLimit) LimeAccent else MaterialTheme.colorScheme.secondary,
                trackColor = BorderGray
            )

            // Input fields section at the top of lists for quick typing
            if (teams.size < totalTeamsLimit) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ADD NEW TEAM",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextWhite
                        )

                        OutlinedTextField(
                            value = teamNameInput,
                            onValueChange = {
                                teamNameInput = it
                                duplicateError = false
                            },
                            label = { Text("Team Name") },
                            placeholder = { Text("e.g. Mumbai Strikers") },
                            supportingText = {
                                if (duplicateError) {
                                    Text(
                                        text = stringResource(id = R.string.duplicate_team_name),
                                        color = ErrorRed
                                    )
                                } else {
                                    Text(
                                        text = "${teamNameInput.length}/20",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            },
                            isError = duplicateError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = BorderGray,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = TextGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        // Color picker Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "TEAM COLOR INDICATOR",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                presetColors.forEach { hex ->
                                    val isColorSelected = selectedColor == hex
                                    val color = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(color, CircleShape)
                                            .border(
                                                width = if (isColorSelected) 3.dp else 0.dp,
                                                color = TextWhite,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedColor = hex },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isColorSelected) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_check),
                                                contentDescription = "Selected",
                                                tint = TextWhite,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val success = viewModel.addTeam(tournamentId, teamNameInput, selectedColor)
                                if (success) {
                                    teamNameInput = ""
                                    // pick a different color for the next team automatically
                                    val currentSize = teams.size
                                    if (currentSize < presetColors.size) {
                                        selectedColor = presetColors[currentSize]
                                    }
                                } else {
                                    duplicateError = true
                                }
                            },
                            enabled = teamNameInput.isNotBlank() && teamNameInput.length <= 20,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = NavyDark
                            )
                        ) {
                            Text(
                                text = "+ Add Team",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Teams list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teams) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(android.graphics.Color.parseColor(team.colorHex)), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = team.teamName,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextWhite
                                )
                            }
                            IconButton(
                                onClick = { viewModel.removeTeam(team.teamName) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Remove",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Button Generate Fixtures
            Button(
                onClick = { showConfirmationDialog = true },
                enabled = teams.size == totalTeamsLimit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = TextWhite
                )
            ) {
                Text(
                    text = "Generate Fixtures →",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Confirmation Dialog
        if (showConfirmationDialog) {
            val expectedMatches = totalTeamsLimit * (totalTeamsLimit - 1) / 2
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(
                        text = "Confirm Tournament Generation",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Generate a round-robin schedule containing $expectedMatches matches? Once generated, team names cannot be modified.",
                        fontFamily = DMSans,
                        color = TextGray
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.generateFixtures(tournamentId)
                                showConfirmationDialog = false
                                onFixturesGenerated(tournamentId)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Generate",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
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
