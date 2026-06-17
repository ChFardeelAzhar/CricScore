package com.cricscore.app.ui.tournament.squadmanagement

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
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

    var showBulkAddSheet by remember { mutableStateOf(false) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = event) {
        event?.let { ev ->
            when (ev) {
                is SquadEvent.PlayerAdded -> {
                    Toast.makeText(context, ev.name, Toast.LENGTH_SHORT).show()
                    showAddPlayerDialog = false
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

    val sortedPlayers by remember(state.players) {
        derivedStateOf {
            state.players.sortedBy { player ->
                when (player.role) {
                    "BATSMAN" -> 0
                    "WICKET_KEEPER" -> 1
                    "ALL_ROUNDER" -> 2
                    "BOWLER" -> 3
                    else -> 4
                }
            }
        }
    }

    val parsedTeamColor = try {
        Color(android.graphics.Color.parseColor(state.teamColor))
    } catch (e: Exception) {
        BlueSecondary
    }

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
                actions = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            if (state.playerCount < 20) {
                Column(horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { showBulkAddSheet = true },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_groups),
                            contentDescription = "Bulk Add",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    ExtendedFloatingActionButton(
                        onClick = { showAddPlayerDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = NavyDark,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person_add),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Player",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
                    ) {
                        itemsIndexed(sortedPlayers) { index, player ->
                            SquadPlayerListItem(
                                player = player,
                                index = index,
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
                }

                // Message if full
                if (state.playerCount >= 20) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
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

    if (showAddPlayerDialog) {
        AddPlayerDialog(
            addState = state.addPlayerState,
            onDismiss = { showAddPlayerDialog = false },
            onNameChange = { viewModel.updateAddPlayerName(it) },
            onRoleChange = { viewModel.updateAddPlayerRole(it) },
            onAddClick = {
                viewModel.addPlayer()
                // We'll close it on success if needed, but let's see how the ViewModel behaves.
                // Usually we clear state. If name is empty, it means added.
            }
        )
    }

    if (showBulkAddSheet) {
        BulkAddBottomSheet(
            onDismiss = { showBulkAddSheet = false },
            onImport = { players ->
                viewModel.bulkAddPlayers(players)
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
fun SquadPlayerListItem(
    player: TeamPlayer,
    index: Int,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val roleIcon = when (player.role) {
        "BOWLER" -> R.drawable.ic_bowl
        else -> R.drawable.ic_bat_bowl
    }

    val roleLabel = when (player.role) {
        "BATSMAN" -> "Batsman"
        "BOWLER" -> "Bowler"
        "WICKET_KEEPER" -> "WK Keeper"
        else -> "All-Rounder"
    }

    val roleColor = when (player.role) {
        "BATSMAN" -> Color(0xFF4A90D9)
        "BOWLER" -> Color(0xFFE74C3C)
        "WICKET_KEEPER" -> Color(0xFF9B59B6)
        else -> Color(0xFFF5A623)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expandedMenu = true },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Index Counter
            Text(
                text = (index + 1).toString().padStart(2, '0'),
                fontFamily = BarlowCondensed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextGray.copy(alpha = 0.5f),
                modifier = Modifier.width(20.dp)
            )

            // Category Icon
            Image(
                painter = painterResource(id = roleIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name and Role Tag
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = player.playerName,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextWhite,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Tag
                    Box(
                        modifier = Modifier
                            .background(roleColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = roleLabel.uppercase(),
                            fontFamily = DMSans,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = roleColor
                        )
                    }
                    
                    if (player.isCaptain || player.isViceCaptain || player.isWicketKeeper) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (player.isCaptain) BadgeIndicator("C", OrangeTertiary)
                            if (player.isViceCaptain) BadgeIndicator("VC", BlueSecondary)
                            if (player.isWicketKeeper) BadgeIndicator("WK", Color(0xFF9B59B6))
                        }
                    }
                }
            }

            // Options Icon
            IconButton(onClick = { expandedMenu = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = "Options",
                    modifier = Modifier.size(14.dp).rotate(90f),
                    tint = TextGray.copy(alpha = 0.6f)
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
fun BadgeIndicator(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = DMSans,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 1.dp)
    )
}

@Composable
fun AddPlayerDialog(
    addState: AddPlayerState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onAddClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(24.dp),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Add New Player",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextWhite
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = addState.name,
                    onValueChange = { if (it.length <= 25) onNameChange(it) },
                    label = { Text("Player name") },
                    placeholder = { Text("e.g. Virat Kohli") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role selection buttons
                Text(
                    text = "ROLE SELECTOR",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
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

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text("Cancel", fontFamily = DMSans, color = TextWhite)
                    }
                    
                    Button(
                        onClick = onAddClick,
                        enabled = addState.name.trim().isNotEmpty() && !addState.isLoading,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = NavyDark
                        )
                    ) {
                        if (addState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NavyDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Add Player",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkAddBottomSheet(
    onDismiss: () -> Unit,
    onImport: (List<Pair<String, String>>) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        var textVal by remember { mutableStateOf("") }
        var isReviewMode by remember { mutableStateOf(false) }
        var playersList by remember { mutableStateOf(listOf<Pair<String, String>>()) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Text(
                text = if (!isReviewMode) stringResource(id = R.string.quick_add_players) else "Assign Roles",
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextWhite
            )
            Text(
                text = if (!isReviewMode) 
                    "Add multiple players at once (one player name per line)" 
                    else "Assign a specific role to each player before importing",
                fontFamily = DMSans,
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isReviewMode) {
                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    placeholder = { Text(stringResource(id = R.string.bulk_add_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = BorderGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val names = textVal.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        playersList = names.map { it to "ALL_ROUNDER" }
                        isReviewMode = true
                    },
                    enabled = textVal.trim().isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = NavyDark)
                ) {
                    Text("Continue to Roles", fontFamily = DMSans, fontWeight = FontWeight.Bold)
                }
            } else {
                // Review Mode: List of players with role selectors
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(playersList) { index, playerPair ->
                        val (name, currentRole) = playerPair
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavyDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextWhite,
                                maxLines = 1
                            )
                            
                            // Segmented-style Role Selector
                            val roles = listOf("BATSMAN", "BOWLER", "ALL_ROUNDER")
                            Row(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                roles.forEach { role ->
                                    val isSelected = currentRole == role
                                    val label = when (role) {
                                        "BATSMAN" -> "BAT"
                                        "BOWLER" -> "BOWL"
                                        else -> "ALL"
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                val newList = playersList.toMutableList()
                                                newList[index] = name to role
                                                playersList = newList
                                            }
                                            .padding(horizontal = 10.dp, vertical = 0.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = DMSans,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) NavyDark else TextGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { isReviewMode = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Text("Back", color = TextWhite, fontFamily = DMSans)
                    }
                    Button(
                        onClick = { onImport(playersList) },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeAccent, contentColor = NavyDark)
                    ) {
                        Text("Import ${playersList.size} Players", fontFamily = DMSans, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
