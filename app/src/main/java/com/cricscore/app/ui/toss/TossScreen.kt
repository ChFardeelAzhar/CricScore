package com.cricscore.app.ui.toss

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cricscore.app.R
import com.cricscore.app.domain.model.TossResult
import com.cricscore.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Random

@Composable
fun GoldCoin(
    side: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(90.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFF5A623))
                )
            )
            .border(4.dp, Color(0xFFD88B0E), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Inner circle border
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .border(1.dp, Color(0xFFD88B0E).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (side == "HEADS") "🦁" else "🐯",
                fontSize = 44.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TossSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = TextGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

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
    
    var callerTeam by remember { mutableStateOf("") }
    var calledSide by remember { mutableStateOf("") } // "HEADS" or "TAILS"
    var coinResult by remember { mutableStateOf("HEADS") } // "HEADS" or "TAILS"
    
    var tossWinnerState by remember { mutableStateOf("") }
    var tossDecisionState by remember { mutableStateOf<TossResult?>(null) }
    
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
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
                showBottomSheet = false
                onTossSaved(matchId)
            }
        }
    }

    // Default selection initialization once match loads
    LaunchedEffect(match) {
        match?.let { matchVal ->
            if (callerTeam.isEmpty()) {
                callerTeam = matchVal.team1
            }
            if (calledSide.isEmpty()) {
                calledSide = "HEADS"
            }
        }
    }

    fun flipCoin() {
        if (isFlipping) return
        if (callerTeam.isEmpty()) {
            Toast.makeText(context, "Please select who calls first", Toast.LENGTH_SHORT).show()
            return
        }
        if (calledSide.isEmpty()) {
            Toast.makeText(context, "Please select the call (HEADS/TAILS) first", Toast.LENGTH_SHORT).show()
            return
        }
        
        isFlipping = true
        tossWinnerState = ""
        tossDecisionState = null
        
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
            coinResult = if (isHeads) "HEADS" else "TAILS"
            
            match?.let { matchVal ->
                tossWinnerState = if (coinResult == calledSide) {
                    callerTeam
                } else {
                    if (callerTeam == matchVal.team1) matchVal.team2 else matchVal.team1
                }
                showBottomSheet = true
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Coin Graphics & Flip Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer glow shadow circle
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            this.rotationY = rotationY.value
                            this.translationY = translationY.value
                            this.scaleX = scale.value
                            this.scaleY = scale.value
                        }
                        .size(115.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(LimeAccent.copy(alpha = 0.3f), Color.Transparent),
                                radius = 180f
                            )
                        )
                )

                // The Golden Coin
                GoldCoin(
                    side = coinResult,
                    onClick = { flipCoin() },
                    modifier = Modifier
                        .graphicsLayer {
                            this.rotationY = rotationY.value
                            this.translationY = translationY.value
                            this.scaleX = scale.value
                            this.scaleY = scale.value
                        }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Result capsule showing HEADS or TAILS below coin
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavySurface)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = coinResult,
                    color = LimeAccent,
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // Flip Coin Button
            Button(
                onClick = { flipCoin() },
                enabled = !isFlipping,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavySurface,
                    contentColor = LimeAccent
                ),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Text(
                    text = "Flip Coin 🪙",
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Radio Groups (Toss winner & decision selection cards)
            match?.let { matchVal ->
                // Who Won The Toss (WHO CALLS?)
                TossSectionCard(title = stringResource(id = R.string.who_calls)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val teams = listOf(matchVal.team1, matchVal.team2)
                        teams.forEach { team ->
                            val isSelected = callerTeam == team
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeAccent else NavyDark)
                                    .border(1.dp, if (isSelected) LimeAccent else BorderGray, RoundedCornerShape(8.dp))
                                    .clickable { 
                                        callerTeam = team
                                        // Reset coin outcome until they flip again
                                        tossWinnerState = ""
                                        tossDecisionState = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {

                                    Text(
                                        text = team,
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) NavyDark else TextGray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // [CALLER] CALLS...
                TossSectionCard(title = stringResource(id = R.string.calls_label, callerTeam.uppercase())) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val sides = listOf("HEADS", "TAILS")
                        sides.forEach { side ->
                            val isSelected = calledSide == side
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeAccent else NavyDark)
                                    .border(1.dp, if (isSelected) LimeAccent else BorderGray, RoundedCornerShape(8.dp))
                                    .clickable { 
                                        calledSide = side
                                        // Reset coin outcome until they flip again
                                        tossWinnerState = ""
                                        tossDecisionState = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = side,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) NavyDark else TextGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Re-open Bottom Sheet button if the coin has been flipped but not confirmed
                if (tossWinnerState.isNotEmpty()) {
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LimeAccent,
                            contentColor = NavyDark
                        )
                    ) {
                        Text(
                            text = "Choose Decision",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Modal Bottom Sheet for Toss Result & Elect Decision
    if (showBottomSheet && tossWinnerState.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = NavySurface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Toss Result green banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF111810),
                    border = BorderStroke(1.dp, LimeAccent.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "🎉", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "TOSS RESULT",
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = LimeAccent,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = R.string.toss_result_won, tossWinnerState),
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        if (tossDecisionState != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "and elected to ",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = TextGray
                                )
                                Text(
                                    text = if (tossDecisionState == TossResult.BAT) "BAT" else "BOWL",
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = LimeAccent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // [TOSS WINNER] ELECTS TO... card
                TossSectionCard(
                    title = stringResource(
                        id = R.string.elects_to,
                        tossWinnerState.uppercase()
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val decisions = listOf(
                            Pair(TossResult.BAT, "🏏 BAT"),
                            Pair(TossResult.BOWL, "\uD83E\uDD4E BOWL")
                        )
                        decisions.forEach { (decision, label) ->
                            val isSelected = tossDecisionState == decision
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeAccent else NavyDark)
                                    .border(1.dp, if (isSelected) LimeAccent else BorderGray, RoundedCornerShape(8.dp))
                                    .clickable { tossDecisionState = decision },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelected) NavyDark else TextGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Confirm & Setup Innings Button
                val isConfirmEnabled = tossDecisionState != null
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
                    enabled = isConfirmEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeAccent,
                        contentColor = NavyDark,
                        disabledContainerColor = LimeAccent.copy(alpha = 0.5f),
                        disabledContentColor = NavyDark.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Confirm & Setup Innings",
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
