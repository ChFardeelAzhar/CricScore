package com.cricscore.app.ui.toss

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
import com.cricscore.app.databinding.FragmentTossBinding
import com.cricscore.app.domain.model.TossResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Random

@AndroidEntryPoint
class TossFragment : BaseFragment<FragmentTossBinding>(FragmentTossBinding::inflate) {

    private val viewModel: TossViewModel by viewModels()
    private var matchId: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchId = arguments?.getLong("matchId") ?: 0
        viewModel.loadMatch(matchId)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnFlipCoin.setSafeOnClickListener {
            flipCoin()
        }

        binding.btnNextOpeners.setSafeOnClickListener {
            val selectedWinnerId = binding.rgTossWinner.checkedRadioButtonId
            val selectedDecisionId = binding.rgTossDecision.checkedRadioButtonId

            if (selectedWinnerId == -1) {
                Toast.makeText(requireContext(), "Please select toss winner", Toast.LENGTH_SHORT).show()
                return@setSafeOnClickListener
            }

            if (selectedDecisionId == -1) {
                Toast.makeText(requireContext(), "Please select toss decision", Toast.LENGTH_SHORT).show()
                return@setSafeOnClickListener
            }

            val match = viewModel.match.value ?: return@setSafeOnClickListener
            val winnerTeam = if (selectedWinnerId == R.id.rb_team1) match.team1 else match.team2
            val decision = if (selectedDecisionId == R.id.rb_bat) TossResult.BAT else TossResult.BOWL

            viewModel.saveTossResult(winnerTeam, decision)
        }
    }

    private fun flipCoin() {
        // Disable button during spin
        binding.btnFlipCoin.isEnabled = false
        
        // Spin, lift, and scale up
        binding.ivCoin.animate()
            .rotationYBy(1800f) // Spin 5 times
            .translationY(-300f) // Lift up
            .scaleX(1.4f)
            .scaleY(1.4f)
            .setDuration(400)
            .withEndAction {
                // Fall back down, spin more, and return to original scale
                binding.ivCoin.animate()
                    .translationY(0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(400)
                    .withEndAction {
                        binding.btnFlipCoin.isEnabled = true
                        
                        val random = Random()
                        val isHeads = random.nextBoolean()
                        val coinDrawable = if (isHeads) R.drawable.ic_coin_heads else R.drawable.ic_coin_tails
                        binding.ivCoin.setImageResource(coinDrawable)

                        val match = viewModel.match.value
                        if (match != null) {
                            val winnerTeam = if (random.nextBoolean()) match.team1 else match.team2
                            if (winnerTeam == match.team1) {
                                binding.rbTeam1.isChecked = true
                            } else {
                                binding.rbTeam2.isChecked = true
                            }
                            binding.tvCoinResult.text = "Result: ${if (isHeads) "Heads" else "Tails"} — $winnerTeam won the toss"
                        }
                    }
                    .start()
            }
            .start()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect match details to label radio buttons
                launch {
                    viewModel.match.collect { match ->
                        match?.let {
                            binding.rbTeam1.text = it.team1
                            binding.rbTeam2.text = it.team2
                        }
                    }
                }

                // Collect toss save success
                launch {
                    viewModel.tossSaved.collect { saved ->
                        if (saved) {
                            val bundle = Bundle().apply {
                                putLong("matchId", matchId)
                                putInt("inningsNumber", 1)
                            }
                            findNavController().navigate(R.id.action_toss_to_innings_setup, bundle)
                        }
                    }
                }
            }
        }
    }
}
