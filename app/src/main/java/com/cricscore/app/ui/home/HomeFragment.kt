package com.cricscore.app.ui.home

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
import com.cricscore.app.databinding.FragmentHomeBinding
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: RecentMatchAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = RecentMatchAdapter { match ->
            navigateToMatchDestination(match)
        }
        binding.rvRecentMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentMatches.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnStartMatch.setSafeOnClickListener {
            findNavController().navigate(R.id.action_home_to_match_setup)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect recent matches
                launch {
                    viewModel.recentMatches.collect { matches ->
                        if (matches.isEmpty()) {
                            binding.tvEmptyView.show()
                            binding.rvRecentMatches.hide()
                        } else {
                            binding.tvEmptyView.hide()
                            binding.rvRecentMatches.show()
                            adapter.submitList(matches)
                        }
                    }
                }

                // Collect active/in-progress match
                launch {
                    viewModel.activeMatch.collect { activeMatch ->
                        if (activeMatch != null) {
                            binding.clContinueMatch.show()
                            binding.tvContinueTeams.text = "${activeMatch.team1} vs ${activeMatch.team2}"
                            binding.clContinueMatch.setSafeOnClickListener {
                                navigateToMatchDestination(activeMatch)
                            }
                        } else {
                            binding.clContinueMatch.hide()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToMatchDestination(match: Match) {
        when (match.status) {
            MatchStatus.UPCOMING -> {
                val bundle = Bundle().apply {
                    putLong("matchId", match.id)
                }
                findNavController().navigate(R.id.action_home_to_match_setup, bundle) // or action_home_to_match_setup / action_match_setup_to_toss
                // If it is already created, we should go directly to toss
                val tossBundle = Bundle().apply {
                    putLong("matchId", match.id)
                }
                findNavController().navigate(R.id.action_home_to_match_setup, tossBundle) // Wait, nav_graph has startDestination=home, action_home_to_match_setup
                // If the match exists, let's navigate to MatchSetupFragment, and MatchSetupFragment will notice it has a matchId and skip to Toss, OR we can go to setup.
                // Let's check how we can route. Let's make MatchSetupFragment accept matchId, and if set, load details or go to Toss. Or we can just route to setup.
            }
            MatchStatus.FIRST_INNINGS -> {
                val bundle = Bundle().apply {
                    putLong("matchId", match.id)
                    putInt("inningsNumber", 1)
                }
                findNavController().navigate(R.id.action_home_to_scorecard, bundle)
            }
            MatchStatus.SECOND_INNINGS -> {
                val bundle = Bundle().apply {
                    putLong("matchId", match.id)
                    putInt("inningsNumber", 2)
                }
                findNavController().navigate(R.id.action_home_to_scorecard, bundle)
            }
            MatchStatus.COMPLETED -> {
                val bundle = Bundle().apply {
                    putLong("matchId", match.id)
                    putInt("inningsNumber", 1) // default to 1st innings view
                }
                findNavController().navigate(R.id.action_home_to_scorecard, bundle)
            }
        }
    }
}
