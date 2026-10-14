package com.example.notesapp.ui.manage.edit

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.notesapp.R
import com.example.notesapp.data.model.Note
import com.example.notesapp.ui.manage.base.BaseManageFragment
import kotlinx.coroutines.launch

class EditFragment : BaseManageFragment() {

    override val viewModel: EditViewModel by viewModels {
        EditViewModel.factory
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
    private var noteId = ""

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getString("noteId").orEmpty()

        if (noteId.isBlank()) {
            showMessage("Note not found")
            findNavController().navigateUp()
            return
        }

        binding.editorHeading.text = "Edit note"
        binding.saveNoteButton.text = "Update"
        binding.moveToTrashButton.visibility = View.VISIBLE

        setupListeners()
        observeNote()
        observeSaveResult()
        observeDeleteResult()
        observeErrors()
        loadNote()
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.colorChipGroup.setOnCheckedStateChangeListener { group, _ ->
            selectedColor =
                noteColors[group.checkedChipId] ?: selectedColor
        }

        binding.saveNoteButton.setOnClickListener {
            updateNote()
        }

        binding.moveToTrashButton.setOnClickListener {
            binding.moveToTrashButton.isEnabled = false
            viewModel.moveToTrash()
        }
    }

    private fun loadNote() {
        val args = requireArguments()

        val cachedNote = Note(
            id = noteId,
            title = args.getString("noteTitle").orEmpty(),
            content = args.getString("noteContent").orEmpty(),
            color = args.getLong("noteColor", 0xFF2D3033L),
            updatedAt = args.getLong(
                "noteUpdatedAt",
                System.currentTimeMillis()
            )
        )

        viewModel.setInitialNote(cachedNote)
        viewModel.loadNote(noteId)
    }

    private fun updateNote() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isBlank()) {
            showMessage("Please enter a title")
            binding.editTitle.requestFocus()
            return
        }

        binding.saveNoteButton.isEnabled = false

        viewModel.updateNote(
            title = title,
            content = content,
            color = selectedColor
        )
    }

    private fun observeNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.note.collect { note ->
                    note ?: return@collect

                    binding.editTitle.setText(note.title)
                    binding.editContent.setText(note.content)

                    binding.manageNoteRoot.setBackgroundColor(
                        note.color.toInt()
                    )

                    selectedColor = note.color

                    val matchingChip = noteColors.entries
                        .firstOrNull {
                            it.value.toInt() == note.color.toInt()
                        }?.key ?: R.id.color0

                    binding.colorChipGroup.check(matchingChip)
                }
            }
        }
    }

    private fun observeSaveResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.saveResult.collect { result ->
                    when (result) {
                        true -> {
                            showMessage("Note updated successfully")
                            viewModel.resetSaveResult()
                        }

                        false -> {
                            binding.saveNoteButton.isEnabled = true
                            viewModel.resetSaveResult()
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    private fun observeDeleteResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.deleteResult.collect { result ->
                    when (result) {
                        true -> {
                            showMessage("Note moved to Trash")
                            viewModel.resetDeleteResult()

                            findNavController().navigate(
                                R.id.homeFragment
                            )
                        } false -> {
                            binding.moveToTrashButton.isEnabled = true
                            viewModel.resetDeleteResult()
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
                viewModel.error.collect { error ->
                    if (!error.isNullOrBlank()) {
                        showMessage(error)
                        viewModel.clearError()
                    }
                }
            }
        }
    }
}