package com.example.notesapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(
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
        observeNotes()

        // Add fake notes when the button is clicked
        binding.fabAdd.setOnClickListener {
            addFakeNotes()
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter()

        binding.notesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = noteAdapter
        }
    }

    private fun observeNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                noteAdapter.submitList(notes)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { message ->
                if (message != null) {
                    Toast.makeText(
                        requireContext(),
                        message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun addFakeNotes() {
        val fakeNotes = listOf(
            Note(
                title = "Groceries",
                content = "Oat milk\nNoodles\nLimes",
                color = 0xFF3B5B50L
            ),
            Note(
                title = "Ideas",
                content = "Build a portfolio\nTry new UI designs",
                color = 0xFF72558CL
            ),
            Note(
                title = "To Do",
                content = "Finish Android project\nReview Kotlin\nPush code to GitHub",
                color = 0xFF8C593FL
            ),
            Note(
                title = "Music",
                content = "Discover new albums\nPractice guitar\nUpdate playlist",
                color = 0xFF405C85L
            ),
            Note(
                title = "Reminder",
                content = "Submit assignment\nPrepare presentation",
                color = 0xFF806B35L
            ),
            Note(
                title = "Shopping List",
                content = "Keyboard\nMouse pad\nUSB cable",
                color = 0xFF854F5AL
            )
        )

        viewLifecycleOwner.lifecycleScope.launch {
            fakeNotes.forEach { note ->
                viewModel.addNote(note)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
