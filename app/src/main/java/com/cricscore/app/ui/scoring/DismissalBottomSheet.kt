package com.cricscore.app.ui.scoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.cricscore.app.R
import com.cricscore.app.core.extensions.hide
import com.cricscore.app.core.extensions.show
import com.cricscore.app.databinding.DialogDismissalBottomSheetBinding
import com.cricscore.app.domain.model.DismissalType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DismissalBottomSheet(
    private val strikerName: String,
    private val nonStrikerName: String,
    private val isLastWicket: Boolean,
    private val onConfirmDismissal: (
        dismissalType: DismissalType,
        dismissedPlayerName: String,
        fielderName: String?,
        nextBatsmanName: String?
    ) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogDismissalBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val dismissalTypes = listOf(
        "Bowled", "Caught", "Run Out", "LBW", "Stumped", "Hit Wicket", "Retired Hurt"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDismissalBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = true

        setupRadioButtons()
        setupSpinner()
        setupNextBatsmanVisibility()

        binding.btnConfirmWicket.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupRadioButtons() {
        binding.rbStriker.text = strikerName
        binding.rbNonStriker.text = nonStrikerName
        binding.rbStriker.isChecked = true
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            dismissalTypes
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spDismissalType.adapter = adapter

        binding.spDismissalType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = dismissalTypes[position]
                if (selectedType == "Caught" || selectedType == "Stumped" || selectedType == "Run Out") {
                    binding.tilFielder.show()
                } else {
                    binding.tilFielder.hide()
                    binding.etFielder.text?.clear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupNextBatsmanVisibility() {
        if (isLastWicket) {
            binding.tilNextBatsman.hide()
            binding.etNextBatsman.text?.clear()
        } else {
            binding.tilNextBatsman.show()
        }
    }

    private fun validateAndSubmit() {
        val typeStr = binding.spDismissalType.selectedItem.toString()
        val type = when (typeStr) {
            "Bowled" -> DismissalType.BOWLED
            "Caught" -> DismissalType.CAUGHT
            "Run Out" -> DismissalType.RUN_OUT
            "LBW" -> DismissalType.LBW
            "Stumped" -> DismissalType.STUMPED
            "Hit Wicket" -> DismissalType.HIT_WICKET
            else -> DismissalType.RETIRED_HURT
        }

        val checkedId = binding.rgDismissedPlayer.checkedRadioButtonId
        val dismissedPlayer = if (checkedId == R.id.rb_striker) strikerName else nonStrikerName
        val remainingPlayer = if (checkedId == R.id.rb_striker) nonStrikerName else strikerName

        val fielder = binding.etFielder.text?.toString().orEmpty().trim().ifEmpty { null }
        val nextBatsman = binding.etNextBatsman.text?.toString().orEmpty().trim().ifEmpty { null }

        if (!isLastWicket && nextBatsman == null) {
            Toast.makeText(requireContext(), "Next batsman name is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (nextBatsman != null && nextBatsman.equals(remainingPlayer, ignoreCase = true)) {
            Toast.makeText(requireContext(), "Next batsman cannot be the remaining active batsman", Toast.LENGTH_SHORT).show()
            return
        }

        onConfirmDismissal(type, dismissedPlayer, fielder, nextBatsman)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
