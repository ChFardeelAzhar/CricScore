package com.cricscore.app.ui.toss

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.domain.model.TossResult
import com.cricscore.app.ui.theme.BarlowCondensed
import com.cricscore.app.ui.theme.BorderGray
import com.cricscore.app.ui.theme.DMSans
import com.cricscore.app.ui.theme.TextGray
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossScreen(
    viewModel: TossViewModel,
    matchId: Long,
    onBackClick: () -> Unit,
    onTossSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val match by viewModel.match.collectAsStateWithLifecycle()
    
    var tossWinnerState by remember { mutableStateOf("") }
    var tossDecisionState by remember { mutableStateOf<TossResult?>(null) }
    
    var coinImageRes by remember { mutableStateOf(R.drawable.ic_coin_heads) }
    var coinFlipResultText by remember { mutableStateOf("") }
    
    // Animation properties
    val rotationY = remember { Animatable(0f) }
    val translationY = remember { Animatable(0f) }
    val scale = remember { Animatable(1.0f) }
    var isFlipping by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = matchId) {
        viewModel.loadMatch(matchId)
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.tossSaved.collectLatest { saved ->
            if (saved) {
                onTossSaved(matchId)
            }
        }
    }

    fun flipCoin() {
        if (isFlipping) return
        isFlipping = true
        
        coroutineScope.launch {
            // Concurrent spin rotation and scale lift
            launch {
                rotationY.animateTo(rotationY.value + 1800f, animationSpec = tween(800))
            }
            launch {
                scale.animateTo(1.4f, animationSpec = tween(400))
                scale.animateTo(1.0f, animationSpec = tween(400))
            }
            // Vertical movement
            translationY.animateTo(-250f, animationSpec = tween(400))
            translationY.animateTo(0f, animationSpec = tween(400))
            
            val random = Random()
            val isHeads = random.nextBoolean()
            coinImageRes = if (isHeads) R.drawable.ic_coin_heads else R.drawable.ic_coin_tails
            
            match?.let { matchVal ->
                val wonTeam = if (random.nextBoolean()) matchVal.team1 else matchVal.team2
                tossWinnerState = wonTeam
                coinFlipResultText = "Result: ${if (isHeads) "Heads" else "Tails"} — $wonTeam won the toss"
            }
            
            isFlipping = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.coin_toss),
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
                text = stringResource(id = R.string.step_n_of_m, 2),
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Coin Graphics & Flip Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                this.rotationY = rotationY.value
                                this.translationY = translationY.value
                                this.scaleX = scale.value
                                this.scaleY = scale.value
                            }
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = coinImageRes),
                            contentDescription = "Coin",
                            modifier = Modifier.size(92.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Toss Result Text
            if (coinFlipResultText.isNotEmpty()) {
                Text(
                    text = coinFlipResultText,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Flip Coin Button
            Button(
                onClick = { flipCoin() },
                enabled = !isFlipping,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(180.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Flip Coin 🪙",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Radio Groups replacement (Toss winner & decision selection cards)
            match?.let { matchVal ->
                // Who Won The Toss
                Text(
                    text = stringResource(id = R.string.who_calls),
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val teams = listOf(matchVal.team1, matchVal.team2)
                    teams.forEach { team ->
                        val isSelected = tossWinnerState == team
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { tossWinnerState = team },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = team,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // What they elected to do
                Text(
                    text = if (tossWinnerState.isNotEmpty()) {
                        stringResource(id = R.string.elects_to, tossWinnerState.uppercase())
                    } else {
                        "DECISION"
                    },
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val decisions = listOf(
                        Pair(TossResult.BAT, stringResource(id = R.string.bat)),
                        Pair(TossResult.BOWL, stringResource(id = R.string.bowl))
                    )
                    decisions.forEach { (decision, label) ->
                        val isSelected = tossDecisionState == decision
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { tossDecisionState = decision },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Confirm Button
            Button(
                onClick = {
                    if (tossWinnerState.isEmpty()) {
                        Toast.makeText(context, "Please select toss winner", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (tossDecisionState == null) {
                        Toast.makeText(context, "Please select toss decision", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.saveTossResult(tossWinnerState, tossDecisionState!!)
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
                    text = stringResource(id = R.string.confirm_setup_innings),
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
