package com.cricscore.app.ui.setup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cricscore.app.R
import com.cricscore.app.ui.theme.BarlowCondensed
import com.cricscore.app.ui.theme.BorderGray
import com.cricscore.app.ui.theme.DMSans
import com.cricscore.app.ui.theme.TextGray
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupScreen(
    viewModel: MatchSetupViewModel,
    onBackClick: () -> Unit,
    onSetupSuccess: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var team1 by remember { mutableStateOf("") }
    var team2 by remember { mutableStateOf("") }
    var overs by remember { mutableStateOf("5") }
    var players by remember { mutableStateOf("11") }

    val standardOvers = listOf("2", "5", "10", "20")

    LaunchedEffect(key1 = viewModel) {
        viewModel.setupEvent.collectLatest { event ->
            when (event) {
                is MatchSetupViewModel.SetupEvent.ValidationError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MatchSetupViewModel.SetupEvent.Success -> {
                    onSetupSuccess(event.matchId)
                }
                is MatchSetupViewModel.SetupEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.match_setup),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Step counter
            Text(
                text = stringResource(id = R.string.step_n_of_m, 1),
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TEAM NAMES SECTION
            Text(
                text = stringResource(id = R.string.team_names),
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = team1,
                onValueChange = { team1 = it },
                label = { Text(text = stringResource(id = R.string.team_1_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = team2,
                onValueChange = { team2 = it },
                label = { Text(text = stringResource(id = R.string.team_2_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // OVERS LIMIT SECTION
            Text(
                text = stringResource(id = R.string.number_of_overs),
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                standardOvers.forEach { overOpt ->
                    val isSelected = overs == overOpt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else BorderGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { overs = overOpt },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$overOpt Overs",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = overs,
                onValueChange = { overs = it },
                label = { Text(text = stringResource(id = R.string.custom_overs)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PLAYERS COUNT SECTION
            Text(
                text = stringResource(id = R.string.players_per_side),
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = players,
                onValueChange = { players = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Submit Button
            Button(
                onClick = { viewModel.createMatch(team1, team2, overs, players) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.proceed_to_toss),
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
