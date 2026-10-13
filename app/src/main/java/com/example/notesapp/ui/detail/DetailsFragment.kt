package com.example.notesapp.ui.detail

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
import com.example.notesapp.databinding.FragmentDetailsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModels {
        DetailsViewModel.factory
    }

    private var noteId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteId = arguments?.getString("noteId").orEmpty()

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.editNoteButton.setOnClickListener {
            val note = viewModel.note.value

            if (note != null && !noteId.isBlank()) {
                val bundle = Bundle().apply {
                    putString("noteId", noteId)
                    putString("noteTitle", note.title)
                    putString("noteContent", note.content)
                    putLong("noteColor", note.color)
                    putLong("noteUpdatedAt", note.updatedAt)
                }

                findNavController().navigate(
                    R.id.editFragment,
                    bundle
                )
            }
        }


        binding.deleteNoteButton.setOnClickListener {
            if (noteId.isNotBlank()) {
                binding.deleteNoteButton.isEnabled = false
                viewModel.deleteNote(noteId)
            } else {
                showMessage("Note ID is missing")
            }
        }


        if (noteId.isBlank()) {
            showMessage("Note ID is missing")
            return
        }

        observeNote()
        observeDeleteResult()

        val args = requireArguments()

        val cachedNote = com.example.notesapp.data.model.Note(
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

    private fun observeNote() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.note.collect { note ->
                    if (note == null) return@collect

                    binding.detailTitle.text =
                        note.title.ifBlank { "Untitled note" }

                    binding.detailContent.text =
                        note.content.ifBlank { "No content" }

                    val formattedDate = SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.getDefault()
                    ).format(Date(note.updatedAt))

                    binding.detailDate.text = "Last edited $formattedDate"

                    // Change the entire detail page background.
                    binding.detailsRoot.setBackgroundColor(
                        note.color.toInt()
                    )
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
                            showMessage("Note moved to trash")
                            findNavController().navigateUp()
                        }

                        false -> {
                            viewModel.resetDeleteResult()
                            binding.deleteNoteButton.isEnabled = true
                            showMessage("Failed to delete note")
                        }

                        null -> Unit
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
