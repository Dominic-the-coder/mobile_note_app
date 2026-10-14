package com.example.notesapp.ui.manage.add

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesapp.R
import com.example.notesapp.ui.manage.base.BaseManageFragment
import kotlinx.coroutines.launch

class AddFragment : BaseManageFragment() {

    override val viewModel: AddViewModel by viewModels {
        AddViewModel.factory
    }

    private val noteColors = mapOf(
        R.id.color0 to 0xFF2D3033L,
        R.id.color1 to 0xFF3B5B50L,
        R.id.color2 to 0xFFE0B94FL,
        R.id.color3 to 0xFFB96648L,
        R.id.color4 to 0xFF854F5AL,
        R.id.color5 to 0xFF505255L
    )

    private var selectedColor = 0xFF2D3033L

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.editorHeading.text = "New note"
        binding.saveNoteButton.text = "Save"

        binding.colorChipGroup.check(R.id.color0)

        setupListeners()
        observeSaveResult()
        observeErrors()
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.colorChipGroup.setOnCheckedStateChangeListener { group, _ ->
            val checkedId = group.checkedChipId
            selectedColor = noteColors[checkedId] ?: 0xFF2D3033L
        }

        binding.saveNoteButton.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isBlank() && content.isBlank()) {
            showMessage("Please enter a title or note content")
            return
        }

        binding.saveNoteButton.isEnabled = false

        viewModel.saveNote(
            title = title,
            content = content,
            color = selectedColor
        )
    }

    private fun observeSaveResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.saveResult.collect { result ->
                    when (result) {
                        true -> {
                            showMessage("Note saved successfully")
                            viewModel.resetResult()
                        }

                        false -> {
                            showMessage("Failed to save note")
                            binding.saveNoteButton.isEnabled = true
                            viewModel.resetResult()
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.error.collect { message ->
                    if (message != null) {
                        showMessage(message)
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}