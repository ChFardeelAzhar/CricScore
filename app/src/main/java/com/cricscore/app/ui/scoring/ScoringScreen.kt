package com.cricscore.app.ui.scoring

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.core.util.OversHelper
import com.cricscore.app.domain.model.*
import com.cricscore.app.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

// Debouncer helper class
class DebouncedClick {
    private var lastClickTime = 0L
    fun execute(interval: Long = 150L, action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= interval) {
            lastClickTime = now
            action()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    viewModel: ScoringViewModel,
    matchId: Long,
    inningsNumber: Int,
    onBackClick: () -> Unit,
    onViewScorecardClick: () -> Unit,
    onMatchCompleted: (Long, Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val debouncer = remember { DebouncedClick() }

    // Init match
    LaunchedEffect(key1 = matchId, key2 = inningsNumber) {
        viewModel.initMatch(matchId, inningsNumber)
    }

    // Viewmodel States
    val match by viewModel.match.collectAsStateWithLifecycle()
    val innings by viewModel.innings.collectAsStateWithLifecycle()
    val currentStriker by viewModel.currentStriker.collectAsStateWithLifecycle()
    val currentNonStriker by viewModel.currentNonStriker.collectAsStateWithLifecycle()
    val currentBowler by viewModel.currentBowler.collectAsStateWithLifecycle()
    val bowlers by viewModel.bowlers.collectAsStateWithLifecycle()
    val balls by viewModel.balls.collectAsStateWithLifecycle()
    val firstInningsRuns by viewModel.firstInningsRuns.collectAsStateWithLifecycle()

    val thisOverBalls by viewModel.thisOverBalls.collectAsStateWithLifecycle()
    val buttonsEnabled by viewModel.scoringButtonsEnabled.collectAsStateWithLifecycle()

    // Overlay dialog triggers
    var showDismissalSheet by remember { mutableStateOf(false) }
    var showExtrasDialogType by remember { mutableStateOf<BallType?>(null) }
    var hasNavigatedCompletion by remember { mutableStateOf(false) }

    // Wicket Shake Animation
    val totalWickets = innings?.totalWickets ?: 0
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(key1 = totalWickets) {
        if (totalWickets > 0) {
            shakeOffset.animateTo(15f, animationSpec = tween(50))
            shakeOffset.animateTo(-15f, animationSpec = tween(50))
            shakeOffset.animateTo(10f, animationSpec = tween(50))
            shakeOffset.animateTo(-10f, animationSpec = tween(50))
            shakeOffset.animateTo(5f, animationSpec = tween(50))
            shakeOffset.animateTo(0f, animationSpec = tween(50))
        }
    }

    // Determine the active over's balls
    val lastBall = balls.lastOrNull()
    val currentOverNumber = lastBall?.overNumber ?: 0
    val legalInLastOver = lastBall?.let { last -> balls.filter { it.overNumber == last.overNumber && OversHelper.isLegalBall(it.ballType) }.size } ?: 0
    val isLastOverComplete = legalInLastOver == 6

    val showOverCompleteSheet = isLastOverComplete && currentBowler == null && innings?.isCompleted == false && match?.status != MatchStatus.COMPLETED

    // Reactively compute match/innings completion
    LaunchedEffect(key1 = innings, key2 = match, key3 = firstInningsRuns) {
        val inn = innings
        val m = match
        if (inn != null && m != null && !hasNavigatedCompletion) {
            val legalBalls = inn.ballsBowled
            val maxBalls = m.oversLimit * 6
            val maxWickets = m.playersPerSide - 1

            var isCompleted = false
            var message = ""
            var showStartNext = false

            if (inningsNumber == 1) {
                if (inn.totalWickets >= maxWickets || legalBalls >= maxBalls) {
                    isCompleted = true
                    message = "1st Innings Completed! Target: ${inn.totalRuns + 1}"
                    showStartNext = true
                }
            } else {
                val target = firstInningsRuns + 1
                if (inn.totalRuns >= target) {
                    isCompleted = true
                    message = "${inn.battingTeam} won by ${m.playersPerSide - 1 - inn.totalWickets} wickets!"
                } else if (inn.totalWickets >= maxWickets || legalBalls >= maxBalls) {
                    isCompleted = true
                    if (inn.totalRuns == target - 1) {
                        message = "Match Tied!"
                    } else {
                        message = "${inn.bowlingTeam} won by ${target - 1 - inn.totalRuns} runs!"
                    }
                }
            }

            if (isCompleted) {
                hasNavigatedCompletion = true
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                onMatchCompleted(matchId, inningsNumber, showStartNext)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_arrow),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    match?.let { m ->
                        Text(
                            text = "${m.team1} vs ${m.team2}",
                            fontFamily = BarlowCondensed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(8f)
                        )
                    }
                }
                
                Button(
                    onClick = onViewScorecardClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, BorderGray),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scorecard),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scorecard",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score Banner Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationX = shakeOffset.value),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Innings & Live Status Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Live breathing indicator dot
                        val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.2f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "live_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .alpha(alpha)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (inningsNumber == 1) stringResource(id = R.string.live_innings_1) else stringResource(id = R.string.live_innings_2),
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Runs & Wickets
                    innings?.let { inn ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AnimatedContent(
                                targetState = "${inn.totalRuns} - ${inn.totalWickets}",
                                transitionSpec = {
                                    slideInVertically { height -> height } + fadeIn() togetherWith
                                            slideOutVertically { height -> -height } + fadeOut()
                                },
                                label = "runs_animation"
                            ) { scoreStr ->
                                Text(
                                    text = scoreStr,
                                    fontFamily = BarlowCondensed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "(${CricketCalculator.ballsToOversString(inn.ballsBowled)} ${stringResource(id = R.string.overs_label)})",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = TextGray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // CRR & Target info
                        val crr = CricketCalculator.calculateRunRate(inn.totalRuns, inn.ballsBowled)
                        if (inningsNumber == 1) {
                            Text(
                                text = String.format(Locale.US, "CRR: %.2f", crr),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = TextGray
                            )
                        } else {
                            val target = firstInningsRuns + 1
                            val needed = target - inn.totalRuns
                            val maxBalls = (match?.oversLimit ?: 0) * 6
                            val ballsLeft = (maxBalls - inn.ballsBowled).coerceAtLeast(0)
                            val rrr = if (ballsLeft > 0) (needed.toFloat() / ballsLeft) * 6 else 0f
                            val statusText = if (needed > 0) "$needed runs needed off $ballsLeft balls" else "Target achieved!"

                            Text(
                                text = String.format(Locale.US, "Target: %d  ·  CRR: %.2f  ·  RRR: %.2f\n%s", target, crr, rrr, statusText),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = TextGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Batsmen Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Batsman", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.weight(1f))
                        Text(text = "R", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        Text(text = "B", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                        Text(text = "4s", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(text = "6s", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                        Text(text = "SR", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextGray, modifier = Modifier.width(48.dp), textAlign = TextAlign.End)
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderGray)

                    // Striker Row
                    currentStriker?.let { striker ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = striker.playerName,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "*", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(text = striker.runs.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                            Text(text = striker.balls.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                            Text(text = striker.fours.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text(text = striker.sixes.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text(
                                text = String.format(Locale.US, "%.1f", CricketCalculator.calculateStrikeRate(striker.runs, striker.balls)),
                                fontFamily = DMSans,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Non-Striker Row
                    currentNonStriker?.let { nonStriker ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nonStriker.playerName,
                                fontFamily = DMSans,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(text = nonStriker.runs.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                            Text(text = nonStriker.balls.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                            Text(text = nonStriker.fours.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text(text = nonStriker.sixes.toString(), fontFamily = DMSans, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            Text(
                                text = String.format(Locale.US, "%.1f", CricketCalculator.calculateStrikeRate(nonStriker.runs, nonStriker.balls)),
                                fontFamily = DMSans,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bowler info
            currentBowler?.let { bowler ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.bowler_label).uppercase(),
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bowler.playerName,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            BowlerStatItem("O", CricketCalculator.ballsToOversString(bowler.ballsBowled))
                            BowlerStatItem("M", bowler.maidens.toString())
                            BowlerStatItem("R", bowler.runsConceded.toString())
                            BowlerStatItem("W", bowler.wickets.toString())
                            BowlerStatItem("Econ", String.format(Locale.US, "%.2f", CricketCalculator.calculateEconomyRate(bowler.runsConceded, bowler.ballsBowled)))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // This Over Ticker (Recent Balls)
            Text(
                text = "THIS OVER",
                fontFamily = BarlowCondensed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (thisOverBalls.isEmpty()) {
                    Text(
                        text = "Over starting...",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    thisOverBalls.forEach { ball ->
                        BallIndicator(ball = ball)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Grid Layout for inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Runs 0, 1, 2, 3, 4, 6 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val runsButtons = listOf(0, 1, 2, 3, 4, 6)
                    runsButtons.take(3).forEach { runs ->
                        ScalingButton(
                            onClick = { debouncer.execute { viewModel.recordNormalBall(runs) } },
                            enabled = buttonsEnabled,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(text = runs.toString(), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val runsButtons = listOf(0, 1, 2, 3, 4, 6)
                    runsButtons.drop(3).forEach { runs ->
                        val isBoundary = runs == 4 || runs == 6
                        ScalingButton(
                            onClick = { debouncer.execute { viewModel.recordNormalBall(runs) } },
                            enabled = buttonsEnabled,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            containerColor = if (isBoundary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (isBoundary) NavyDark else MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(text = runs.toString(), fontFamily = BarlowCondensed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }

                // Extras: Wd, Nb, Bye, Lb
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScalingButton(
                        onClick = { showExtrasDialogType = BallType.WIDE },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        containerColor = OrangeTertiary.copy(alpha = 0.2f),
                        contentColor = OrangeTertiary
                    ) {
                        Text(text = "WD", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    ScalingButton(
                        onClick = { showExtrasDialogType = BallType.NO_BALL },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        containerColor = OrangeTertiary.copy(alpha = 0.2f),
                        contentColor = OrangeTertiary
                    ) {
                        Text(text = "NB", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    ScalingButton(
                        onClick = { showExtrasDialogType = BallType.BYE },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(text = "BYE", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    ScalingButton(
                        onClick = { showExtrasDialogType = BallType.LEG_BYE },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Text(text = "LBYE", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Bottom row: Undo, Switch, Wicket
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Undo
                    Button(
                        onClick = { viewModel.undoLastBall() },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_undo), contentDescription = "Undo", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(id = R.string.undo), fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Switch Strike
                    Button(
                        onClick = { viewModel.switchStrike() },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_switch), contentDescription = "Switch Strike", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Switch", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Wicket
                    Button(
                        onClick = { showDismissalSheet = true },
                        enabled = buttonsEnabled,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WicketRed,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_wicket), contentDescription = "Wicket", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "WICKET", fontFamily = DMSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Over Complete
    if (showOverCompleteSheet) {
        val nextOverNumber = currentOverNumber + 1
        val lastBall = balls.lastOrNull()
        val lastBowlerName = lastBall?.bowlerName

        ModalBottomSheet(
            onDismissRequest = { /* Force bowler selection, cannot dismiss */ },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { targetValue ->
                    targetValue != SheetValue.Hidden
                }
            ),
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            var bowlerName by remember { mutableStateOf("") }
            
            // Check if bowlerName matches any recent bowler name (excluding the one who just bowled)
            val selectedBowler = remember(bowlerName, bowlers, lastBowlerName) {
                val typed = bowlerName.trim()
                if (typed.isEmpty()) null
                else bowlers.firstOrNull { 
                    it.playerName.equals(typed, ignoreCase = true) && !it.playerName.equals(lastBowlerName, ignoreCase = true) 
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .imePadding()
            ) {
                // Header Area
                Text(
                    text = "End of Over $currentOverNumber",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = LimeAccent
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Scoreboard Banner inside the sheet
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL SCORE",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${innings?.totalRuns ?: 0} - ${innings?.totalWickets ?: 0}",
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = TextWhite
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "OVERS",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CricketCalculator.ballsToOversString(innings?.ballsBowled ?: 0),
                                fontFamily = BarlowCondensed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completed Over Balls Row
                Text(
                    text = "BALLS THIS OVER",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (thisOverBalls.isEmpty()) {
                        Text(
                            text = "No balls in this over",
                            fontFamily = DMSans,
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    } else {
                        thisOverBalls.forEach { ball ->
                            BallIndicator(ball = ball)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Recent Bowlers list
                if (bowlers.isNotEmpty()) {
                    Text(
                        text = "RECENT BOWLERS",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bowlers.forEach { bowler ->
                            val isJustBowled = bowler.playerName.equals(lastBowlerName, ignoreCase = true)
                            val isSelected = selectedBowler?.playerName == bowler.playerName
                            
                            RecentBowlerCard(
                                bowler = bowler,
                                isJustBowled = isJustBowled,
                                isSelected = isSelected,
                                onClick = {
                                    bowlerName = bowler.playerName
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Input Field for Bowler Name
                Text(
                    text = "ENTER NEXT BOWLER",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = bowlerName,
                    onValueChange = { bowlerName = it },
                    placeholder = { Text("Enter bowler name...", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = LimeAccent,
                        unfocusedBorderColor = BorderGray,
                        cursorColor = LimeAccent
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val name = bowlerName.trim()
                        if (name.isEmpty()) {
                            Toast.makeText(context, "Bowler name cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (name.equals(lastBowlerName, ignoreCase = true)) {
                            Toast.makeText(context, "The same bowler cannot bowl consecutive overs", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.selectNextBowler(name)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeAccent,
                        contentColor = NavyDark
                    )
                ) {
                    Text(
                        text = "Start Over $nextOverNumber",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheet: Dismissal (Wicket dialog)
    if (showDismissalSheet && currentStriker != null && currentNonStriker != null && innings != null && match != null) {
        val strikerName = currentStriker!!.playerName
        val nonStrikerName = currentNonStriker!!.playerName
        val inn = innings!!
        val m = match!!
        val maxWickets = m.playersPerSide - 1
        val isLastWicket = (inn.totalWickets + 1 >= maxWickets)

        ModalBottomSheet(
            onDismissRequest = { showDismissalSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val dismissalTypes = listOf("Bowled", "Caught", "Run Out", "LBW", "Stumped", "Hit Wicket", "Retired Hurt")
            
            var selectedTypeStr by remember { mutableStateOf("Bowled") }
            var dismissedPlayerName by remember { mutableStateOf(strikerName) }
            var fielderName by remember { mutableStateOf("") }
            var nextBatsmanName by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                Text(
                    text = "WICKET! 🔴",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = WicketRed
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dismissed Player Selector
                Text(
                    text = "WHO IS OUT?",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(strikerName, nonStrikerName).forEach { name ->
                        val isSelected = dismissedPlayerName == name
                        val suffix = if (name == strikerName) " (Striker)" else ""
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WicketRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
                                .border(1.dp, if (isSelected) WicketRed else BorderGray, RoundedCornerShape(8.dp))
                                .clickable { dismissedPlayerName = name },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name + suffix,
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) WicketRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dismissed Type (Exposed Dropdown replacement)
                Text(
                    text = "DISMISSAL TYPE",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                var dropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTypeStr,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        dismissalTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type, fontFamily = DMSans) },
                                onClick = {
                                    selectedTypeStr = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                val showFielderInput = selectedTypeStr == "Caught" || selectedTypeStr == "Stumped" || selectedTypeStr == "Run Out"
                if (showFielderInput) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "FIELDER NAME",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fielderName,
                        onValueChange = { fielderName = it },
                        placeholder = { Text("Fielder name...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                }

                if (!isLastWicket) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NEXT BATSMAN",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nextBatsmanName,
                        onValueChange = { nextBatsmanName = it },
                        placeholder = { Text("Next batsman name...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        val type = when (selectedTypeStr) {
                            "Bowled" -> DismissalType.BOWLED
                            "Caught" -> DismissalType.CAUGHT
                            "Run Out" -> DismissalType.RUN_OUT
                            "LBW" -> DismissalType.LBW
                            "Stumped" -> DismissalType.STUMPED
                            "Hit Wicket" -> DismissalType.HIT_WICKET
                            else -> DismissalType.RETIRED_HURT
                        }

                        val remainingPlayer = if (dismissedPlayerName == strikerName) nonStrikerName else strikerName
                        val fName = fielderName.trim().ifEmpty { null }
                        val nextBat = nextBatsmanName.trim().ifEmpty { null }

                        if (!isLastWicket && nextBat == null) {
                            Toast.makeText(context, "Next batsman name is required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (nextBat != null && nextBat.equals(remainingPlayer, ignoreCase = true)) {
                            Toast.makeText(context, "Next batsman cannot be the remaining active batsman", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            viewModel.recordBall(
                                runsBatsman = 0,
                                runsExtra = 0,
                                ballType = BallType.NORMAL,
                                isWicket = true,
                                dismissalType = type,
                                fielderName = fName,
                                dismissedPlayerName = dismissedPlayerName,
                                nextBatsmanName = nextBat
                            )
                            showDismissalSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WicketRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Confirm Dismissal",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Custom Extras Runs Dialog
    if (showExtrasDialogType != null) {
        val extraType = showExtrasDialogType!!
        AlertDialog(
            onDismissRequest = { showExtrasDialogType = null },
            title = {
                Text(
                    text = "Select ${extraType.name.replace("_", " ")} Runs",
                    fontFamily = BarlowCondensed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (extraType == BallType.WIDE) {
                        // Wide runs selection: standard wide (1 run), wide + runs completed
                        listOf(
                            Pair(1, "Wide only (1 Run)"),
                            Pair(2, "Wide + 1 (2 Runs)"),
                            Pair(3, "Wide + 2 (3 Runs)"),
                            Pair(4, "Wide + 3 (4 Runs)"),
                            Pair(5, "Wide + 4 (5 Runs)")
                        ).forEach { (runs, label) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.recordExtraBall(runs, BallType.WIDE)
                                        showExtrasDialogType = null
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else if (extraType == BallType.NO_BALL) {
                        // No Ball runs selection:
                        // 1. Off the bat (runsBatsman + 1 penalty extra)
                        // 2. Byes/Leg-byes (0 runsBatsman + penalty + runsExtra byes)
                        Text(
                            text = "OFF THE BAT",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                        listOf(
                            Triple(0, 1, "NB only (1 Run)"),
                            Triple(1, 1, "NB + 1 run (2 Runs)"),
                            Triple(2, 1, "NB + 2 runs (3 Runs)"),
                            Triple(3, 1, "NB + 3 runs (4 Runs)"),
                            Triple(4, 1, "NB + 4 runs (5 Runs)"),
                            Triple(6, 1, "NB + 6 runs (7 Runs)")
                        ).forEach { (runsBat, runsExtra, label) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.recordNoBall(runsBat, runsExtra)
                                        showExtrasDialogType = null
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            text = "BYES / EXTRAS",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                        listOf(
                            Triple(0, 2, "NB + 1 Bye (2 Runs)"),
                            Triple(0, 3, "NB + 2 Byes (3 Runs)"),
                            Triple(0, 4, "NB + 3 Byes (4 Runs)"),
                            Triple(0, 5, "NB + 4 Byes (5 Runs)")
                        ).forEach { (runsBat, runsExtra, label) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.recordNoBall(runsBat, runsExtra)
                                        showExtrasDialogType = null
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        // BYE / LEG_BYE runs selection: standard 1 to 4 runs
                        (1..4).forEach { runs ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, BorderGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.recordExtraBall(runs, extraType)
                                        showExtrasDialogType = null
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$runs Run${if (runs > 1) "s" else ""}",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExtrasDialogType = null }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Micro-interaction scaling button
@Composable
fun ScalingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = tween(80),
        finishedListener = { if (isPressed) isPressed = false },
        label = "button_scale"
    )
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        enabled = enabled,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        content = content
    )
}

@Composable
fun BowlerStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = DMSans,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = DMSans,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BallIndicator(ball: Ball) {
    val size = 36.dp
    val shape = CircleShape
    val text: String
    val bg: Color
    val tc: Color

    when {
        ball.isWicket -> {
            text = "W"
            bg = WicketRed
            tc = Color.White
        }
        ball.ballType == BallType.WIDE -> {
            text = "${ball.runsExtra}Wd"
            bg = OrangeTertiary
            tc = NavyDark
        }
        ball.ballType == BallType.NO_BALL -> {
            text = "${ball.runsBatsman + ball.runsExtra}Nb"
            bg = OrangeTertiary
            tc = NavyDark
        }
        ball.runsBatsman == 4 -> {
            text = "4"
            bg = LimeAccent
            tc = NavyDark
        }
        ball.runsBatsman == 6 -> {
            text = "6"
            bg = LimeAccent
            tc = NavyDark
        }
        ball.runsBatsman == 0 && ball.runsExtra == 0 -> {
            text = "0"
            bg = BorderGray
            tc = TextGray
        }
        else -> {
            text = "${ball.runsBatsman + ball.runsExtra}"
            bg = BlueSecondary
            tc = Color.White
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = DMSans,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = tc
        )
    }
}

@Composable
fun RecentBowlerCard(
    bowler: BowlerInnings,
    isJustBowled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isJustBowled, onClick = onClick)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) LimeAccent else BorderGray
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NavySurface else NavySurface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bowler.playerName,
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isJustBowled) TextGray else MaterialTheme.colorScheme.onSurface
                    )
                    if (isJustBowled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BorderGray)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "JUST BOWLED",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = TextGray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val statsStr = "${CricketCalculator.ballsToOversString(bowler.ballsBowled)}-${bowler.maidens}-${bowler.runsConceded}-${bowler.wickets}"
                Text(
                    text = "Stats: $statsStr",
                    fontFamily = DMSans,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LimeAccent.copy(alpha = 0.15f) else BorderGray.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${CricketCalculator.ballsToOversString(bowler.ballsBowled)} O  ·  ${bowler.wickets} W",
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (isSelected) LimeAccent else TextGray
                    )
                }
            }
        }
    }
}
