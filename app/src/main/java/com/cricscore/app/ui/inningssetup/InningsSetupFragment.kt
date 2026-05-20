package com.cricscore.app.ui.inningssetup

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
import com.cricscore.app.databinding.FragmentInningsSetupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InningsSetupFragment : BaseFragment<FragmentInningsSetupBinding>(FragmentInningsSetupBinding::inflate) {

    private val viewModel: InningsSetupViewModel by viewModels()
    private var matchId: Long = 0
    private var inningsNumber: Int = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchId = arguments?.getLong("matchId") ?: 0
        inningsNumber = arguments?.getInt("inningsNumber") ?: 1

        binding.tvHeader.text = "INNINGS $inningsNumber SETUP"
        viewModel.loadInningsSetup(matchId, inningsNumber)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnStartScoring.setSafeOnClickListener {
            val striker = binding.etStriker.text?.toString().orEmpty()
            val nonStriker = binding.etNonStriker.text?.toString().orEmpty()
            val bowler = binding.etBowler.text?.toString().orEmpty()

            viewModel.startInnings(matchId, inningsNumber, striker, nonStriker, bowler)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Setup State
                launch {
                    viewModel.setupState.collect { state ->
                        state?.let {
                            binding.tvBattingTeam.text = it.battingTeam
                            binding.tvBowlingTeam.text = it.bowlingTeam
                        }
                    }
                }

                // Collect Validation Errors
                launch {
                    viewModel.validationError.collect { errorMsg ->
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }

                // Collect Innings Started Success
                launch {
                    viewModel.inningsStarted.collect { started ->
                        if (started) {
                            val bundle = Bundle().apply {
                                putLong("matchId", matchId)
                                putInt("inningsNumber", inningsNumber)
                            }
                            findNavController().navigate(R.id.action_innings_setup_to_scoring, bundle)
                        }
                    }
                }
            }
        }
    }
}
