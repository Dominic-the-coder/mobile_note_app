package com.example.notesapp.ui.manage.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentAddBinding
import kotlinx.coroutines.launch

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddViewModel by viewModels {
        AddViewModel.factory
    }

    private val noteColors = mapOf(
        R.id.color0 to 0xFF2D3033L,
        R.id.color1 to 0xFF3B5B50L,
        R.id.color2 to 0xFFE0B94F.toLong(),
        R.id.color3 to 0xFFB96648L,
        R.id.color4 to 0xFF854F5A.toLong(),
        R.id.color5 to 0xFF505255.toLong()
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Select the default color
        binding.colorChipGroup.check(R.id.color0)

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.saveNoteButton.setOnClickListener {
            saveNote()
        }

        observeSaveResult()
    }

    private fun saveNote() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please enter a title or content",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val selectedColor = noteColors[
            binding.colorChipGroup.checkedChipId
        ] ?: 0xFF2D3033L

        binding.saveNoteButton.isEnabled = false

        viewModel.saveNote(
            title = title,
            content = content,
            color = selectedColor
        )
    }

    private fun observeSaveResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    when (result) {
                        true -> {
                            viewModel.resetResult()

                            Toast.makeText(
                                requireContext(),
                                "Note saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            findNavController().navigateUp()
                        }

                        false -> {
                            viewModel.resetResult()
                            binding.saveNoteButton.isEnabled = true

                            Toast.makeText(
                                requireContext(),
                                "Unable to save note. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
