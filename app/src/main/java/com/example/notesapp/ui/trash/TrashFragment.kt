package com.example.notesapp.ui.trash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notesapp.databinding.FragmentTrashBinding
import kotlinx.coroutines.launch

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TrashViewModel by viewModels {
        TrashViewModel.factory
    }

    private lateinit var trashAdapter: TrashAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrashBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeTrashNotes()
        observeMessages()
    }

    private fun setupRecyclerView() {
        trashAdapter = TrashAdapter(
            onRestoreClick = { note ->
                viewModel.restoreNote(note)
            },
            onDeleteClick = { note ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete permanently?")
                    .setMessage(
                        "This note cannot be recovered after deletion."
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.permanentlyDeleteNote(note)
                    }
                    .show()
            }
        )

        binding.trashRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = trashAdapter
            setHasFixedSize(false)
        }
    }

    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.emptyTrashButton.setOnClickListener {
            if (viewModel.trashNotes.value.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Trash is already empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Empty trash?")
                .setMessage(
                    "All deleted notes will be permanently removed. This cannot be undone."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Empty trash") { _, _ ->
                    viewModel.emptyTrash()
                }
                .show()
        }
    }

    private fun observeTrashNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.trashNotes.collect { notes ->
                    trashAdapter.submitList(notes)

                    binding.trashCount.text =
                        if (notes.size == 1) {
                            "1 deleted note"
                        } else {
                            "${notes.size} deleted notes"
                        }

                    binding.emptyTrashMessage.visibility =
                        if (notes.isEmpty()) View.VISIBLE
                        else View.GONE

                    binding.trashRecyclerView.visibility =
                        if (notes.isEmpty()) View.GONE
                        else View.VISIBLE

                    binding.emptyTrashButton.isEnabled = notes.isNotEmpty()
                    binding.emptyTrashButton.alpha =
                        if (notes.isNotEmpty()) 1f else 0.5f
                }
            }
        }
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.message.collect { message ->
                    if (message != null) {
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.clearMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.trashRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
