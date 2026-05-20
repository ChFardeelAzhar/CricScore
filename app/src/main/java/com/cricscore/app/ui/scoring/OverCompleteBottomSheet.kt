package com.cricscore.app.ui.scoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cricscore.app.databinding.DialogOverCompleteBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OverCompleteBottomSheet(
    private val onConfirmBowler: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: DialogOverCompleteBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOverCompleteBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.btnConfirmBowler.setOnClickListener {
            val name = binding.etNextBowler.text?.toString().orEmpty().trim()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Bowler name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onConfirmBowler(name)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
