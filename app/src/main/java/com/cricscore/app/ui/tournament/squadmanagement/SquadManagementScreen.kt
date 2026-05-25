package com.cricscore.app.ui.tournament.squadmanagement

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import androidx.compose.ui.text.style.TextAlign
import com.cricscore.app.domain.model.TeamPlayer
import com.cricscore.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadManagementScreen(
    viewModel: SquadManagementViewModel,
    teamId: Long,
    tournamentId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(key1 = teamId, key2 = tournamentId) {
        viewModel.initTeam(teamId, tournamentId)
    }

    val state by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    LaunchedEffect(key1 = event) {
        event?.let { ev ->
            when (ev) {
                is SquadEvent.PlayerAdded -> {
                    Toast.makeText(context, ev.name, Toast.LENGTH_SHORT).show()
                }
                is SquadEvent.Error -> {
                    Toast.makeText(context, ev.message, Toast.LENGTH_LONG).show()
                }
                SquadEvent.CaptainSet -> {
                    Toast.makeText(context, "Captain set successfully", Toast.LENGTH_SHORT).show()
                }
                SquadEvent.ViceCaptainSet -> {
                    Toast.makeText(context, "Vice Captain set successfully", Toast.LENGTH_SHORT).show()
                }
                SquadEvent.WicketKeeperSet -> {
                    Toast.makeText(context, "Wicket Keeper set successfully", Toast.LENGTH_SHORT).show()
                }
                SquadEvent.PlayerRemoved -> {
                    Toast.makeText(context, "Player removed", Toast.LENGTH_SHORT).show()
                }
            }
            viewModel.clearEvent()
        }
    }

    var sortBy by remember { mutableStateOf(0) } // 0 = Name, 1 = Role, 2 = Jersey
    val sortedPlayers by remember(state.players, sortBy) {
        derivedStateOf {
            when (sortBy) {
                0 -> state.players.sortedBy { it.playerName.lowercase() }
                1 -> state.players.sortedBy { it.role }
                2 -> state.players.sortedBy { if (it.jerseyNumber == 0) Int.MAX_VALUE else it.jerseyNumber }
                else -> state.players
            }
        }
    }

    val parsedTeamColor = try {
        Color(android.graphics.Color.parseColor(state.teamColor))
    } catch (e: Exception) {
        BlueSecondary
    }

    var showBulkAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.teamName.ifBlank { "Squad" },
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "${state.playerCount} Players · ${state.requiredCount} required",
                            fontFamily = DMSans,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
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
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play), // placeholder for sort icon (rotated or chevron)
                            contentDescription = "Sort",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Name", fontFamily = DMSans) },
                            onClick = {
                                sortBy = 0
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Role", fontFamily = DMSans) },
                            onClick = {
                                sortBy = 1
                                sortMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Jersey Number", fontFamily = DMSans) },
                            onClick = {
                                sortBy = 2
                                sortMenuExpanded = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (state.playerCount < 20) {
                ExtendedFloatingActionButton(
                    onClick = { showBulkAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_groups),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bulk Add",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Team Info Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, parsedTeamColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(parsedTeamColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.teamName,
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgeChip(
                            label = "Captain: " + (state.captain?.playerName ?: "Not Set"),
                            color = OrangeTertiary,
                            isActive = state.captain != null
                        )
                        BadgeChip(
                            label = "VC: " + (state.viceCaptain?.playerName ?: "Not Set"),
                            color = BlueSecondary,
                            isActive = state.viceCaptain != null
                        )
                        BadgeChip(
                            label = "WK: " + (state.wicketKeeper?.playerName ?: "Not Set"),
                            color = Color(0xFF9B59B6),
                            isActive = state.wicketKeeper != null
                        )
                    }
                }
            }

            // Players List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (sortedPlayers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.no_players_yet),
                            fontFamily = DMSans,
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    val chunkedPlayers = sortedPlayers.chunked(3)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 280.dp) // Leave space for sticky add section
                    ) {
                        items(chunkedPlayers) { rowPlayers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPlayers.forEach { player ->
                                    Box(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        SquadPlayerGridItem(
                                            player = player,
                                            onAction = { action ->
                                                when (action) {
                                                    "captain" -> viewModel.setCaptain(player.id)
                                                    "vc" -> viewModel.setViceCaptain(player.id)
                                                    "wk" -> viewModel.setWicketKeeper(player.id)
                                                    "remove" -> viewModel.removePlayer(player.id)
                                                }
                                            }
                                        )
                                    }
                                }
                                repeat(3 - rowPlayers.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Sticky Add Player Panel
                if (state.playerCount < 20) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                    ) {
                        AddPlayerForm(
                            addState = state.addPlayerState,
                            onNameChange = { viewModel.updateAddPlayerName(it) },
                            onJerseyChange = { viewModel.updateAddPlayerJersey(it) },
                            onRoleChange = { viewModel.updateAddPlayerRole(it) },
                            onAddClick = { viewModel.addPlayer() }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.squad_full),
                            color = ErrorRed,
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showBulkAddSheet) {
        BulkAddBottomSheet(
            onDismiss = { showBulkAddSheet = false },
            onImport = { text, role ->
                viewModel.bulkAddPlayers(text, role)
                showBulkAddSheet = false
            }
        )
    }
}

@Composable
fun BadgeChip(label: String, color: Color, isActive: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isActive) color.copy(alpha = 0.15f) else BorderGray.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .border(
                1.dp,
                if (isActive) color else BorderGray,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = DMSans,
            fontSize = 11.sp,
            color = if (isActive) color else TextGray
        )
    }
}

@Composable
fun SquadPlayerGridItem(
    player: TeamPlayer,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val initial = player.playerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    val roleColor = when (player.role) {
        "BATSMAN" -> Color(0xFF4A90D9)
        "BOWLER" -> Color(0xFF7ED321)
        "WICKET_KEEPER" -> Color(0xFF9B59B6)
        else -> Color(0xFFF5A623) // ALL_ROUNDER
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(115.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { expandedMenu = true },
                    onTap = { expandedMenu = true }
                )
            }
            .border(
                1.dp,
                BorderGray,
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Badges overlay in top-right
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (player.isCaptain) {
                    Text(
                        text = "©",
                        fontFamily = DMSans,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeTertiary,
                        modifier = Modifier
                            .background(OrangeTertiary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
                if (player.isViceCaptain) {
                    Text(
                        text = "VC",
                        fontFamily = DMSans,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueSecondary,
                        modifier = Modifier
                            .background(BlueSecondary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
                if (player.isWicketKeeper) {
                    Text(
                        text = "🧤",
                        fontSize = 9.sp,
                        modifier = Modifier
                            .background(Color(0xFF9B59B6).copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            // Close / Action options indicator in top-left
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Options",
                tint = TextGray.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopStart)
                    .clickable { expandedMenu = true }
            )

            // Main content column
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circle with Initial Letter
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(OrangeTertiary.copy(alpha = 0.1f), CircleShape)
                        .border(
                            1.dp,
                            OrangeTertiary.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = OrangeTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Player name
                Text(
                    text = player.playerName,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextWhite,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Player role label
                Text(
                    text = when (player.role) {
                        "BATSMAN" -> "Batsman"
                        "BOWLER" -> "Bowler"
                        "WICKET_KEEPER" -> "WK"
                        else -> "All-Rounder"
                    },
                    fontFamily = DMSans,
                    fontSize = 10.sp,
                    color = roleColor,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            DropdownMenu(
                expanded = expandedMenu,
                onDismissRequest = { expandedMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Set as Captain", fontFamily = DMSans) },
                    onClick = {
                        onAction("captain")
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Set as Vice Captain", fontFamily = DMSans) },
                    onClick = {
                        onAction("vc")
                        expandedMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Set as Wicket Keeper", fontFamily = DMSans) },
                    onClick = {
                        onAction("wk")
                        expandedMenu = false
                    }
                )
                HorizontalDivider(color = BorderGray)
                DropdownMenuItem(
                    text = { Text("Remove Player", fontFamily = DMSans, color = ErrorRed) },
                    onClick = {
                        onAction("remove")
                        expandedMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddPlayerForm(
    addState: AddPlayerState,
    onNameChange: (String) -> Unit,
    onJerseyChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = addState.name,
                    onValueChange = { if (it.length <= 25) onNameChange(it) },
                    label = { Text("Player name") },
                    placeholder = { Text("e.g. Virat Kohli") },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                OutlinedTextField(
                    value = addState.jerseyNumber,
                    onValueChange = { onJerseyChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("Jersey #") },
                    placeholder = { Text("7") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role selection buttons
            Text(
                text = "ROLE SELECTOR",
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val roles = listOf("BATSMAN", "BOWLER", "ALL_ROUNDER", "WICKET_KEEPER")
                roles.forEach { role ->
                    val isSelected = addState.selectedRole == role
                    val roleLabel = when (role) {
                        "BATSMAN" -> "Batsman"
                        "BOWLER" -> "Bowler"
                        "WICKET_KEEPER" -> "WK Keeper"
                        else -> "All-Rounder"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onRoleChange(role) },
                        label = { Text(roleLabel, fontFamily = DMSans, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Transparent
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selected = isSelected,
                            enabled = true,
                            borderColor = BorderGray,
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }

            if (addState.error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = addState.error,
                    color = ErrorRed,
                    fontSize = 11.sp,
                    fontFamily = DMSans
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onAddClick,
                enabled = addState.name.trim().isNotEmpty() && !addState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = NavyDark
                )
            ) {
                Text(
                    text = "+ Add Player",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkAddBottomSheet(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        var textVal by remember { mutableStateOf("") }
        var defaultRole by remember { mutableStateOf("ALL_ROUNDER") }

        val names = remember(textVal) {
            textVal.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding()
        ) {
            Text(
                text = stringResource(id = R.string.quick_add_players),
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add multiple players at once (one player name per line)",
                fontFamily = DMSans,
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textVal,
                onValueChange = { textVal = it },
                placeholder = { Text(stringResource(id = R.string.bulk_add_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "DEFAULT ROLE FOR IMPORTED PLAYERS",
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextGray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("BATSMAN", "BOWLER", "ALL_ROUNDER").forEach { role ->
                    val isSelected = defaultRole == role
                    val label = when (role) {
                        "BATSMAN" -> "Batsman"
                        "BOWLER" -> "Bowler"
                        else -> "All-Rounder"
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { defaultRole = role },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.names_detected, names.size),
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    color = LimeAccent,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { onImport(textVal, defaultRole) },
                    enabled = names.isNotEmpty(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = NavyDark
                    )
                ) {
                    Text(
                        text = "Import Players",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
