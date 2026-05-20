package com.cricscore.app.ui.scorecard

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cricscore.app.R
import com.cricscore.app.core.base.BaseFragment
import com.cricscore.app.core.extensions.hide
import com.cricscore.app.core.extensions.setSafeOnClickListener
import com.cricscore.app.core.extensions.show
import com.cricscore.app.databinding.FragmentScorecardBinding
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScorecardFragment : BaseFragment<FragmentScorecardBinding>(FragmentScorecardBinding::inflate) {

    private val viewModel: ScorecardViewModel by viewModels()
    private var matchId: Long = 0
    private var initialInnings: Int = 1
    private var showStartNext: Boolean = false

    private lateinit var batsmanAdapter: ScorecardBatsmanAdapter
    private lateinit var bowlerAdapter: ScorecardBowlerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchId = arguments?.getLong("matchId") ?: 0
        initialInnings = arguments?.getInt("inningsNumber") ?: 1
        showStartNext = arguments?.getBoolean("showStartNext") ?: false

        viewModel.initMatch(matchId, initialInnings)

        setupRecyclerViews()
        setupClickListeners()
        observeState()
    }

    private fun setupRecyclerViews() {
        batsmanAdapter = ScorecardBatsmanAdapter()
        binding.rvBatsmen.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBatsmen.adapter = batsmanAdapter

        bowlerAdapter = ScorecardBowlerAdapter()
        binding.rvBowlers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBowlers.adapter = bowlerAdapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnInnings1.setSafeOnClickListener {
            viewModel.setInningsNumber(1)
        }

        binding.btnInnings2.setSafeOnClickListener {
            viewModel.setInningsNumber(2)
        }

        binding.btnStartSecondInnings.setSafeOnClickListener {
            val bundle = Bundle().apply {
                putLong("matchId", matchId)
                putInt("inningsNumber", 2)
            }
            findNavController().navigate(R.id.action_scorecard_to_innings_setup, bundle)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Match Details
                launch {
                    viewModel.match.collect { match ->
                        match?.let {
                            binding.tvMatchName.text = "${it.team1} vs ${it.team2}"
                            
                            // Only show 2nd Innings tab if the match has progressed to 2nd innings or is completed
                            if (it.status == MatchStatus.SECOND_INNINGS || it.status == MatchStatus.COMPLETED) {
                                binding.btnInnings2.show()
                            } else {
                                binding.btnInnings2.hide()
                            }

                            val currentTab = viewModel.inningsNumber.value
                            updateStartNextButtonVisibility(it, currentTab)
                        }
                    }
                }

                // Collect Innings Tab Selection
                launch {
                    viewModel.inningsNumber.collect { num ->
                        updateTabSelection(num)
                        viewModel.match.value?.let { match ->
                            updateStartNextButtonVisibility(match, num)
                        }
                    }
                }

                // Collect Selected Innings Data
                launch {
                    viewModel.innings.collect { innings ->
                        if (innings != null) {
                            binding.clInningsSummary.show()
                            binding.tvInningsTeam.text = "${innings.battingTeam.uppercase()} INNINGS"
                            binding.tvInningsRunsWickets.text = "${innings.totalRuns} - ${innings.totalWickets}"
                            binding.tvInningsOvers.text = "(${com.cricscore.app.core.util.CricketCalculator.ballsToOversString(innings.ballsBowled)} overs)"

                            val totalExtras = innings.extrasWide + innings.extrasNoBall + innings.extrasBye + innings.extrasLegBye
                            binding.tvExtrasTotal.text = "Extras: $totalExtras"
                            binding.tvExtrasBreakdown.text = "(wd ${innings.extrasWide}, nb ${innings.extrasNoBall}, b ${innings.extrasBye}, lb ${innings.extrasLegBye})"
                        } else {
                            binding.clInningsSummary.hide()
                        }
                    }
                }

                // Collect Batsmen List
                launch {
                    viewModel.batsmen.collect { list ->
                        batsmanAdapter.submitList(list)
                    }
                }

                // Collect Bowlers List
                launch {
                    viewModel.bowlers.collect { list ->
                        bowlerAdapter.submitList(list)
                    }
                }
            }
        }
    }

    private fun updateTabSelection(selectedInnings: Int) {
        val context = requireContext()
        val primaryColor = context.getColor(R.color.colorPrimary)
        val transparentColor = context.getColor(android.R.color.transparent)
        val blackColor = context.getColor(R.color.black)
        val whiteColor = context.getColor(R.color.white)

        if (selectedInnings == 1) {
            binding.btnInnings1.backgroundTintList = ColorStateList.valueOf(primaryColor)
            binding.btnInnings1.setTextColor(blackColor)
            
            binding.btnInnings2.backgroundTintList = ColorStateList.valueOf(transparentColor)
            binding.btnInnings2.setTextColor(whiteColor)
        } else {
            binding.btnInnings2.backgroundTintList = ColorStateList.valueOf(primaryColor)
            binding.btnInnings2.setTextColor(blackColor)
            
            binding.btnInnings1.backgroundTintList = ColorStateList.valueOf(transparentColor)
            binding.btnInnings1.setTextColor(whiteColor)
        }
    }

    private fun updateStartNextButtonVisibility(match: Match, selectedInnings: Int) {
        if (match.status == MatchStatus.COMPLETED) {
            binding.btnStartSecondInnings.text = "View Match Result"
            binding.btnStartSecondInnings.show()
            binding.btnStartSecondInnings.setSafeOnClickListener {
                val bundle = Bundle().apply {
                    putLong("matchId", matchId)
                }
                findNavController().navigate(R.id.action_scorecard_to_result, bundle)
            }
        } else {
            if (showStartNext && selectedInnings == 1) {
                binding.btnStartSecondInnings.text = "Start 2nd Innings"
                binding.btnStartSecondInnings.show()
                binding.btnStartSecondInnings.setSafeOnClickListener {
                    val bundle = Bundle().apply {
                        putLong("matchId", matchId)
                        putInt("inningsNumber", 2)
                    }
                    findNavController().navigate(R.id.action_scorecard_to_innings_setup, bundle)
                }
            } else {
                binding.btnStartSecondInnings.hide()
            }
        }
    }
}
