package com.cricscore.app.ui.result

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cricscore.app.R
import com.cricscore.app.core.base.BaseFragment
import com.cricscore.app.core.extensions.setSafeOnClickListener
import com.cricscore.app.databinding.FragmentResultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResultFragment : BaseFragment<FragmentResultBinding>(FragmentResultBinding::inflate) {

    private val viewModel: ResultViewModel by viewModels()
    private var matchId: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchId = arguments?.getLong("matchId") ?: 0
        viewModel.loadMatch(matchId)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnHome.setSafeOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }

        binding.btnViewScorecard.setSafeOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Match Details
                launch {
                    viewModel.match.collect { match ->
                        match?.let {
                            binding.tvMatchTeams.text = "${it.team1} vs ${it.team2}"
                            
                            binding.tvWinnerMessage.text = if (it.isTied) {
                                "Match Tied!"
                            } else if (it.winnerTeam != null) {
                                val margin = if (it.winMarginRuns > 0) {
                                    "${it.winMarginRuns} runs"
                                } else {
                                    "${it.winMarginWickets} wickets"
                                }
                                "${it.winnerTeam} won by $margin"
                            } else {
                                "Match Completed"
                            }
                        }
                    }
                }

                // Collect Innings List Summary
                launch {
                    viewModel.inningsList.collect { list ->
                        val inn1 = list.firstOrNull { it.inningsNumber == 1 }
                        val inn2 = list.firstOrNull { it.inningsNumber == 2 }

                        if (inn1 != null) {
                            binding.tvInnings1Summary.text = "${inn1.battingTeam}: ${inn1.totalRuns}/${inn1.totalWickets} (${com.cricscore.app.core.util.CricketCalculator.ballsToOversString(inn1.ballsBowled)} Overs)"
                        } else {
                            binding.tvInnings1Summary.text = "Not Batted"
                        }

                        if (inn2 != null) {
                            binding.tvInnings2Summary.text = "${inn2.battingTeam}: ${inn2.totalRuns}/${inn2.totalWickets} (${com.cricscore.app.core.util.CricketCalculator.ballsToOversString(inn2.ballsBowled)} Overs)"
                        } else {
                            binding.tvInnings2Summary.text = "Not Batted"
                        }
                    }
                }
            }
        }
    }
}
