package com.example.notesapp.ui.manage.edit

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
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.FragmentEditBinding
import kotlinx.coroutines.launch

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditViewModel by viewModels {
        EditViewModel.factory
    }

    private var noteId: String = ""
    private var selectedColor: Long = 0xFF2D3033L
    private var loadedNoteId: String? = null

    private val noteColors = mapOf(
        R.id.color0 to 0xFF2D3033L,
        R.id.color1 to 0xFF3B5B50L,
        R.id.color2 to 0xFFE0B94FL,
        R.id.color3 to 0xFFB96648L,
        R.id.color4 to 0xFF854F5AL,
        R.id.color5 to 0xFF505255L
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getString("noteId").orEmpty()

        setupListeners()
        observeNote()
        observeSaveResult()
        observeDeleteResult()
        observeErrors()

        if (noteId.isBlank()) {
            showMessage("Note ID is missing")
            return
        }


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

        // Display the existing data immediately.
        viewModel.setInitialNote(cachedNote)

        // Refresh from Firestore in the background if necessary.
        viewModel.loadNote(noteId)

    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.colorChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()

            if (checkedId != null) {
                selectedColor = noteColors[checkedId] ?: selectedColor
            }
        }

        binding.saveNoteButton.setOnClickListener {
            val title = binding.editTitle.text.toString()
            val content = binding.editContent.text.toString()

            if (title.isBlank()) {
                binding.editTitle.error = "Title is required"
                return@setOnClickListener
            }

            binding.saveNoteButton.isEnabled = false

            viewModel.updateNote(
                title = title,
                content = content,
                color = selectedColor
            )
        }

        binding.moveToTrashButton.setOnClickListener {
            binding.moveToTrashButton.isEnabled = false
            viewModel.moveToTrash()
        }
    }

    private fun observeNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.note.collect { note ->
                    if (note == null || note.id == loadedNoteId) {
                        return@collect
                    }

                    loadedNoteId = note.id

                    binding.editTitle.setText(note.title)
                    binding.editContent.setText(note.content)

                        // Change the Edit page background to match the note color.
                    binding.editRoot.setBackgroundColor(note.color.toInt())

                    selectedColor = note.color

                    val selectedChipId = noteColors.entries
                        .firstOrNull { it.value == note.color }
                        ?.key ?: R.id.color0

                    binding.colorChipGroup.check(selectedChipId)
                }
            }
        }
    }


    private fun observeSaveResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    when (result) {
                        true -> {
                            binding.saveNoteButton.isEnabled = true
                            viewModel.resetSaveResult()
                            showMessage("Note updated successfully")
                            findNavController().navigateUp()
                        }

                        false -> {
                            binding.saveNoteButton.isEnabled = true
                            viewModel.resetSaveResult()
                            showMessage("Failed to save note")
                        }

                        null -> Unit
                    }
                }
            }
        }
    }


    private fun observeDeleteResult() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteResult.collect { result ->
                    when (result) {
                        true -> {
                            viewModel.resetDeleteResult()

                            findNavController().popBackStack(
                                R.id.homeFragment,
                                false
                            )
                        }

                        false -> {
                            viewModel.resetDeleteResult()
                            binding.moveToTrashButton.isEnabled = true
                        }

                        null -> Unit
                    }
                }
            }
        }
    }

    private fun observeErrors() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { message ->
                    if (message != null) {
                        showMessage(message)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
