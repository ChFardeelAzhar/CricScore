package com.cricscore.app.ui.inningssetup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.ui.theme.BarlowCondensed
import com.cricscore.app.ui.theme.BorderGray
import com.cricscore.app.ui.theme.DMSans
import com.cricscore.app.ui.theme.TextGray
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InningsSetupScreen(
    viewModel: InningsSetupViewModel,
    matchId: Long,
    inningsNumber: Int,
    onBackClick: () -> Unit,
    onInningsStarted: (Long, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val setupState by viewModel.setupState.collectAsStateWithLifecycle()
    
    var striker by remember { mutableStateOf("") }
    var nonStriker by remember { mutableStateOf("") }
    var bowler by remember { mutableStateOf("") }

    LaunchedEffect(key1 = matchId, key2 = inningsNumber) {
        viewModel.loadInningsSetup(matchId, inningsNumber)
    }

    LaunchedEffect(key1 = viewModel) {
        // Collect errors
        launch {
            viewModel.validationError.collectLatest { errorMsg ->
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
        // Collect success
        launch {
            viewModel.inningsStarted.collectLatest { started ->
                if (started) {
                    onInningsStarted(matchId, inningsNumber)
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
                        text = stringResource(id = R.string.innings_setup),
                        fontFamily = BarlowCondensed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
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


            setupState?.let { state ->
                // Batting and Bowling Teams banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.batting_label),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.battingTeam,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // VS separator
                        Text(
                            text = "vs",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextGray
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(id = R.string.bowling_label),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.bowlingTeam,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // OPENING BATSMEN
            Text(
                text = stringResource(id = R.string.opening_batsmen),
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = striker,
                onValueChange = { striker = it },
                label = { Text(text = stringResource(id = R.string.striker_facing_first)) },
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
                value = nonStriker,
                onValueChange = { nonStriker = it },
                label = { Text(text = stringResource(id = R.string.non_striker)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // OPENING BOWLER
            Text(
                text = stringResource(id = R.string.opening_bowler),
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bowler,
                onValueChange = { bowler = it },
                label = { Text(text = "Bowler Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Start Match Button
            Button(
                onClick = {
                    viewModel.startInnings(matchId, inningsNumber, striker, nonStriker, bowler)
                },
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
                    text = stringResource(id = R.string.start_match),
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
