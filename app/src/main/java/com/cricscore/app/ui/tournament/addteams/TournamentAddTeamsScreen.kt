package com.cricscore.app.ui.tournament.addteams

import android.graphics.Color.parseColor
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.alpha
import com.cricscore.app.R
import com.cricscore.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt

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
    
    // Auto-select first color
    var selectedColor by remember { mutableStateOf(presetColors[0]) }
    
    // Update selected color when teams change to pick the next unused color
    LaunchedEffect(teams.size) {
        selectedColor = presetColors[teams.size % presetColors.size]
    }
    
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "TEAMS ADDED",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextGray,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${teams.size} / $totalTeamsLimit",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (teams.size == totalTeamsLimit) LimeAccent else TextWhite
                )
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { teams.size.toFloat() / totalTeamsLimit },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (teams.size == totalTeamsLimit) LimeAccent else MaterialTheme.colorScheme.primary,
                trackColor = NavySurface
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
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ADD NEW TEAM",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = LimeAccent,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = teamNameInput,
                            onValueChange = {
                                if (it.length <= 20) {
                                    teamNameInput = it
                                    duplicateError = false
                                }
                            },
                            placeholder = { 
                                Text(
                                    text = "Team name",
                                    color = TextGray.copy(alpha = 0.5f),
                                    fontSize = 15.sp
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_groups),
                                    contentDescription = null,
                                    tint = TextGray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            supportingText = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "${teamNameInput.length}/20",
                                        color = if (duplicateError) ErrorRed else TextGray.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            isError = duplicateError,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = NavyDark.copy(alpha = 0.5f),
                                unfocusedContainerColor = NavyDark.copy(alpha = 0.5f),
                                focusedBorderColor = BorderGray,
                                unfocusedBorderColor = BorderGray,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                cursorColor = LimeAccent
                            )
                        )

                        if (duplicateError) {
                            Text(
                                text = stringResource(id = R.string.duplicate_team_name),
                                color = ErrorRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                            )
                        }

                        // Color picker Row
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "TEAM COLOR",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextGray,
                                letterSpacing = 1.sp
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
                                            .size(38.dp)
                                            .background(color, CircleShape)
                                            .border(
                                                width = if (isColorSelected) 2.dp else 0.dp,
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
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val success = viewModel.addTeam(tournamentId, teamNameInput, selectedColor)
                                if (success) {
                                    teamNameInput = ""
                                    // selectedColor is updated via LaunchedEffect
                                } else {
                                    duplicateError = true
                                }
                            },
                            enabled = teamNameInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LimeAccent,
                                contentColor = NavyDark,
                                disabledContainerColor = BorderGray,
                                disabledContentColor = TextGray
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add Team",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "YOUR TEAMS",
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextGray,
                letterSpacing = 1.sp
            )

            // Teams list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(teams) { team ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
//                                // Color Indicator Dot
//                                Box(
//                                    modifier = Modifier
//                                        .padding(horizontal = 8.dp)
//                                        .size(10.dp)
//                                        .background(Color(android.graphics.Color.parseColor(team.colorHex)), CircleShape)
//                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Icon Box
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(team.colorHex.toColorInt()), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bat_bowl),
                                        contentDescription = null,
                                        tint = Color.Unspecified, // Keep original vector colors if any
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Team Info
                                Column {
                                    Text(
                                        text = team.teamName,
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextWhite
                                    )
                                    Surface(
                                        color = Color(parseColor(team.colorHex)).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Cricket Team",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontFamily = DMSans,
                                            fontSize = 10.sp,
                                            color = Color(parseColor(team.colorHex)),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Remove Button
                            IconButton(
                                onClick = { viewModel.removeTeam(team.teamName) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(NavyDark, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_close),
                                    contentDescription = "Remove",
                                    tint = TextGray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
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
                    .padding(vertical = 12.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeAccent,
                    contentColor = NavyDark,
                    disabledContainerColor = NavySurface,
                    disabledContentColor = TextGray.copy(alpha = 0.5f)
                ),
                border = if (teams.size != totalTeamsLimit) BorderStroke(1.dp, BorderGray.copy(alpha = 0.5f)) else null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate Fixtures",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
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
