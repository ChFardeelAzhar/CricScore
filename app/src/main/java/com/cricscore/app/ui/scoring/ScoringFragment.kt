package com.cricscore.app.ui.scoring

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cricscore.app.R
import com.cricscore.app.core.base.BaseFragment
import com.cricscore.app.core.extensions.hide
import com.cricscore.app.core.extensions.setSafeOnClickListener
import com.cricscore.app.core.extensions.show
import com.cricscore.app.core.util.CricketCalculator
import com.cricscore.app.core.util.OversHelper
import com.cricscore.app.databinding.FragmentScoringBinding
import com.cricscore.app.domain.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScoringFragment : BaseFragment<FragmentScoringBinding>(FragmentScoringBinding::inflate) {

    private val viewModel: ScoringViewModel by viewModels()
    private var matchId: Long = 0
    private var inningsNumber: Int = 1

    private var lastPromptedOverNumber: Int = -1
    private var hasNavigatedCompletion: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchId = arguments?.getLong("matchId") ?: 0
        inningsNumber = arguments?.getInt("inningsNumber") ?: 1

        viewModel.initMatch(matchId, inningsNumber)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnViewScorecard.setSafeOnClickListener {
            navigateToScorecard(showStartNext = false)
        }

        // Runs buttons
        binding.btnRuns0.setSafeOnClickListener { viewModel.recordNormalBall(0) }
        binding.btnRuns1.setSafeOnClickListener { viewModel.recordNormalBall(1) }
        binding.btnRuns2.setSafeOnClickListener { viewModel.recordNormalBall(2) }
        binding.btnRuns3.setSafeOnClickListener { viewModel.recordNormalBall(3) }
        binding.btnRuns4.setSafeOnClickListener { viewModel.recordNormalBall(4) }
        binding.btnRuns6.setSafeOnClickListener { viewModel.recordNormalBall(6) }

        // Extras buttons
        binding.btnExtraWd.setSafeOnClickListener { viewModel.recordExtraBall(1, BallType.WIDE) }
        binding.btnExtraNb.setSafeOnClickListener { viewModel.recordExtraBall(1, BallType.NO_BALL) }
        binding.btnExtraBye.setSafeOnClickListener { showExtrasRunsDialog(BallType.BYE) }
        binding.btnExtraLegbye.setSafeOnClickListener { showExtrasRunsDialog(BallType.LEG_BYE) }

        // Undo
        binding.btnUndo.setSafeOnClickListener { viewModel.undoLastBall() }

        // Wicket
        binding.btnWicket.setSafeOnClickListener {
            showDismissalBottomSheet()
        }
    }

    private fun showExtrasRunsDialog(ballType: BallType) {
        val options = arrayOf("1 Run", "2 Runs", "3 Runs", "4 Runs")
        AlertDialog.Builder(requireContext())
            .setTitle("Select ${ballType.name} Runs")
            .setItems(options) { _, which ->
                val runs = which + 1
                viewModel.recordExtraBall(runs, ballType)
            }
            .show()
    }

    private fun showDismissalBottomSheet() {
        val striker = viewModel.currentStriker.value ?: return
        val nonStriker = viewModel.currentNonStriker.value ?: return
        val inningsVal = viewModel.innings.value ?: return
        val matchVal = viewModel.match.value ?: return

        val maxWickets = matchVal.playersPerSide - 1
        val isLast = (inningsVal.totalWickets + 1 >= maxWickets)

        val bottomSheet = DismissalBottomSheet(
            strikerName = striker.playerName,
            nonStrikerName = nonStriker.playerName,
            isLastWicket = isLast
        ) { type, dismissedPlayer, fielder, nextBatsman ->
            lifecycleScope.launch {
                viewModel.recordBall(
                    runsBatsman = 0,
                    runsExtra = 0,
                    ballType = BallType.NORMAL,
                    isWicket = true,
                    dismissalType = type,
                    fielderName = fielder,
                    dismissedPlayerName = dismissedPlayer
                )
                if (nextBatsman != null) {
                    viewModel.introduceNewBatsman(nextBatsman)
                }
            }
        }
        bottomSheet.show(childFragmentManager, "DismissalBottomSheet")
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Match Details
                launch {
                    viewModel.match.collect { match ->
                        match?.let {
                            binding.tvMatchName.text = "${it.team1} vs ${it.team2}"
                            checkMatchAndInningsCompletion()
                        }
                    }
                }

                // Collect Innings Details
                launch {
                    viewModel.innings.collect { innings ->
                        innings?.let {
                            binding.tvScore.text = "${it.totalRuns} - ${it.totalWickets}"
                            binding.tvOvers.text = "${com.cricscore.app.core.util.CricketCalculator.ballsToOversString(it.ballsBowled)} Overs"
                            updateRunRateInfo(it)
                            checkMatchAndInningsCompletion()
                        }
                    }
                }

                // Collect Striker
                launch {
                    viewModel.currentStriker.collect { striker ->
                        if (striker != null) {
                            binding.trStriker.show()
                            binding.tvStrikerName.text = "${striker.playerName} *"
                            binding.tvStrikerRuns.text = striker.runs.toString()
                            binding.tvStrikerBalls.text = striker.balls.toString()
                            binding.tvStrikerFours.text = striker.fours.toString()
                            binding.tvStrikerSixes.text = striker.sixes.toString()
                            binding.tvStrikerSr.text = String.format(java.util.Locale.US, "%.1f", com.cricscore.app.core.util.CricketCalculator.calculateStrikeRate(striker.runs, striker.balls))
                        } else {
                            binding.trStriker.hide()
                        }
                    }
                }

                // Collect Non-Striker
                launch {
                    viewModel.currentNonStriker.collect { nonStriker ->
                        if (nonStriker != null) {
                            binding.trNonStriker.show()
                            binding.tvNonStrikerName.text = nonStriker.playerName
                            binding.tvNonStrikerRuns.text = nonStriker.runs.toString()
                            binding.tvNonStrikerBalls.text = nonStriker.balls.toString()
                            binding.tvNonStrikerFours.text = nonStriker.fours.toString()
                            binding.tvNonStrikerSixes.text = nonStriker.sixes.toString()
                            binding.tvNonStrikerSr.text = String.format(java.util.Locale.US, "%.1f", com.cricscore.app.core.util.CricketCalculator.calculateStrikeRate(nonStriker.runs, nonStriker.balls))
                        } else {
                            binding.trNonStriker.hide()
                        }
                    }
                }

                // Collect Bowler
                launch {
                    viewModel.currentBowler.collect { bowler ->
                        if (bowler != null) {
                            binding.cvBowler.show()
                            binding.tvBowlerName.text = bowler.playerName
                            binding.tvBowlerOvers.text = com.cricscore.app.core.util.CricketCalculator.ballsToOversString(bowler.ballsBowled)
                            binding.tvBowlerMaidens.text = bowler.maidens.toString()
                            binding.tvBowlerRuns.text = bowler.runsConceded.toString()
                            binding.tvBowlerWickets.text = bowler.wickets.toString()
                            binding.tvBowlerEcon.text = String.format(java.util.Locale.US, "%.2f", com.cricscore.app.core.util.CricketCalculator.calculateEconomyRate(bowler.runsConceded, bowler.ballsBowled))
                        } else {
                            binding.cvBowler.hide()
                        }
                    }
                }

                // Collect This Over's Balls
                launch {
                    viewModel.thisOverBalls.collect { ballsList ->
                        updateRecentBallsTicker(ballsList)
                        checkOverCompletion(ballsList)
                    }
                }
            }
        }
    }

    private fun updateRunRateInfo(innings: Innings) {
        val matchVal = viewModel.match.value ?: return
        val crr = com.cricscore.app.core.util.CricketCalculator.calculateRunRate(innings.totalRuns, innings.ballsBowled)
        if (inningsNumber == 1) {
            binding.tvRrInfo.text = String.format(java.util.Locale.US, "CRR: %.2f", crr)
        } else {
            val target = viewModel.firstInningsRuns.value + 1
            val needed = target - innings.totalRuns
            val legalBallsBowled = innings.ballsBowled
            val maxBalls = matchVal.oversLimit * 6
            val ballsLeft = (maxBalls - legalBallsBowled).coerceAtLeast(0)
            
            val rrr = if (ballsLeft > 0) {
                (needed.toFloat() / ballsLeft) * 6
            } else {
                0f
            }

            val statusText = if (needed > 0) {
                "$needed runs needed off $ballsLeft balls"
            } else {
                "Target achieved!"
            }

            binding.tvRrInfo.text = String.format(
                "Target: %d · CRR: %.2f · RRR: %.2f\n%s",
                target, crr, rrr, statusText
            )
        }
    }

    private fun updateRecentBallsTicker(ballsList: List<Ball>) {
        binding.llRecentBalls.removeAllViews()
        for (ball in ballsList) {
            val view = createBallIndicatorView(ball)
            binding.llRecentBalls.addView(view)
        }
        binding.hsvRecentBalls.post {
            binding.hsvRecentBalls.fullScroll(View.FOCUS_RIGHT)
        }
    }

    private fun createBallIndicatorView(ball: Ball): View {
        val sizePx = (36 * resources.displayMetrics.density).toInt()
        val marginPx = (4 * resources.displayMetrics.density).toInt()
        
        val textView = TextView(requireContext()).apply {
            layoutParams = ViewGroup.MarginLayoutParams(sizePx, sizePx).apply {
                setMargins(marginPx, 0, marginPx, 0)
            }
            gravity = android.view.Gravity.CENTER
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val (bgRes, textColorRes, text) = when {
            ball.isWicket -> Triple(R.drawable.bg_ball_indicator_wicket, R.color.white, "W")
            ball.ballType == BallType.WIDE -> Triple(R.drawable.bg_ball_indicator_wide, R.color.black, "${ball.runsExtra}Wd")
            ball.ballType == BallType.NO_BALL -> Triple(R.drawable.bg_ball_indicator_noball, R.color.black, "${ball.runsBatsman + ball.runsExtra}Nb")
            ball.runsBatsman == 4 -> Triple(R.drawable.bg_ball_indicator_four, R.color.white, "4")
            ball.runsBatsman == 6 -> Triple(R.drawable.bg_ball_indicator_six, R.color.white, "6")
            ball.runsBatsman == 0 && ball.runsExtra == 0 -> Triple(R.drawable.bg_ball_indicator_dot, R.color.colorTextSecondary, "0")
            else -> Triple(R.drawable.bg_ball_indicator_runs, R.color.white, "${ball.runsBatsman + ball.runsExtra}")
        }

        textView.setBackgroundResource(bgRes)
        textView.setTextColor(requireContext().getColor(textColorRes))
        textView.text = text
        return textView
    }

    private fun checkOverCompletion(ballsList: List<Ball>) {
        if (ballsList.isEmpty()) return
        val lastBall = ballsList.last()
        val legalCount = ballsList.filter { OversHelper.isLegalBall(it.ballType) }.size

        if (legalCount == 6) {
            // Over completed! Freeze scoring buttons
            toggleScoringButtons(false)

            if (lastPromptedOverNumber != lastBall.overNumber) {
                showOverCompleteBottomSheet(lastBall.overNumber)
            }
        } else {
            // Over is in progress, make sure buttons are enabled
            toggleScoringButtons(true)
        }
    }

    private fun showOverCompleteBottomSheet(overNumber: Int) {
        val bottomSheet = OverCompleteBottomSheet { nextBowlerName ->
            viewModel.selectNextBowler(nextBowlerName)
            lastPromptedOverNumber = overNumber
            toggleScoringButtons(true)
        }
        bottomSheet.show(childFragmentManager, "OverCompleteBottomSheet")
    }

    private fun toggleScoringButtons(enabled: Boolean) {
        binding.btnRuns0.isEnabled = enabled
        binding.btnRuns1.isEnabled = enabled
        binding.btnRuns2.isEnabled = enabled
        binding.btnRuns3.isEnabled = enabled
        binding.btnRuns4.isEnabled = enabled
        binding.btnRuns6.isEnabled = enabled
        binding.btnExtraWd.isEnabled = enabled
        binding.btnExtraNb.isEnabled = enabled
        binding.btnExtraBye.isEnabled = enabled
        binding.btnExtraLegbye.isEnabled = enabled
        binding.btnWicket.isEnabled = enabled
    }

    private fun checkMatchAndInningsCompletion() {
        if (hasNavigatedCompletion) return

        val inningsVal = viewModel.innings.value ?: return
        val matchVal = viewModel.match.value ?: return

        val legalBalls = inningsVal.ballsBowled
        val maxBalls = matchVal.oversLimit * 6
        val maxWickets = matchVal.playersPerSide - 1

        var isCompleted = false
        var message = ""
        var showStartNext = false

        if (inningsNumber == 1) {
            if (inningsVal.totalWickets >= maxWickets || legalBalls >= maxBalls) {
                isCompleted = true
                message = "1st Innings Completed! Target: ${inningsVal.totalRuns + 1}"
                showStartNext = true
            }
        } else {
            val target = viewModel.firstInningsRuns.value + 1
            if (inningsVal.totalRuns >= target) {
                isCompleted = true
                message = "${inningsVal.battingTeam} won by ${matchVal.playersPerSide - 1 - inningsVal.totalWickets} wickets!"
            } else if (inningsVal.totalWickets >= maxWickets || legalBalls >= maxBalls) {
                isCompleted = true
                if (inningsVal.totalRuns == target - 1) {
                    message = "Match Tied!"
                } else {
                    message = "${inningsVal.bowlingTeam} won by ${target - 1 - inningsVal.totalRuns} runs!"
                }
            }
        }

        if (isCompleted) {
            hasNavigatedCompletion = true
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            navigateToScorecard(showStartNext)
        }
    }

    private fun navigateToScorecard(showStartNext: Boolean) {
        val bundle = Bundle().apply {
            putLong("matchId", matchId)
            putInt("inningsNumber", inningsNumber)
            putBoolean("showStartNext", showStartNext)
        }
        findNavController().navigate(R.id.action_scoring_to_scorecard, bundle)
    }
}
