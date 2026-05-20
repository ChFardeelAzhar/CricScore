package com.cricscore.app.ui.setup

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cricscore.app.R
import com.cricscore.app.core.base.BaseFragment
import com.cricscore.app.core.extensions.setSafeOnClickListener
import com.cricscore.app.databinding.FragmentMatchSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MatchSetupFragment : BaseFragment<FragmentMatchSetupBinding>(FragmentMatchSetupBinding::inflate) {

    private val viewModel: MatchSetupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeEvents()
    }

    private fun setupClickListeners() {
        binding.btnBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnStartToss.setSafeOnClickListener {
            val team1 = binding.etTeam1.text?.toString().orEmpty()
            val team2 = binding.etTeam2.text?.toString().orEmpty()
            val overs = binding.etOvers.text?.toString().orEmpty()
            val players = binding.etPlayers.text?.toString().orEmpty()
            
            viewModel.createMatch(team1, team2, overs, players)
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setupEvent.collect { event ->
                    when (event) {
                        is MatchSetupViewModel.SetupEvent.ValidationError -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                        }
                        is MatchSetupViewModel.SetupEvent.Success -> {
                            val bundle = Bundle().apply {
                                putLong("matchId", event.matchId)
                            }
                            findNavController().navigate(R.id.action_match_setup_to_toss, bundle)
                        }
                        is MatchSetupViewModel.SetupEvent.Error -> {
                            Toast.makeText(requireContext(), event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
