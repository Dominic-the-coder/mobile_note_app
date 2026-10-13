package com.example.notesapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notesapp.R
import com.example.notesapp.data.model.Note
import com.example.notesapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory
    }

    private lateinit var noteAdapter: NoteAdapter

    // Keep the original list so filters can be combined.
    private var allNotes: List<Note> = emptyList()

    // null means all colors are selected.
    private var selectedColor: Long? = null

    // false = newest first, true = oldest first.
    private var oldestFirst = false

    private val colorMap = mapOf(
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
        _binding = FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupFilters()
        setupTrashNavigation()
        observeNotes()
        observeErrors()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            val bundle = Bundle().apply {
                putString("noteId", note.id)
                putString("noteTitle", note.title)
                putString("noteContent", note.content)
                putLong("noteColor", note.color)
                putLong("noteUpdatedAt", note.updatedAt)
            }

            findNavController().navigate(
                R.id.action_homeFragment_to_detailsFragment,
                bundle
            )
        }

        binding.notesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = noteAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_addFragment
            )
        }
    }

    private fun setupTrashNavigation() {
        binding.openTrashButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_trashFragment
            )
        }
    }

    private fun setupFilters() {

        // All button clears the selected color.
        binding.filterAll.setOnClickListener {
            selectedColor = null
            binding.colorChipGroup.clearCheck()
            applyFilters()
        }

        // Selecting a chip filters notes by its color.
        binding.colorChipGroup.setOnCheckedStateChangeListener {
                _, checkedIds ->

            selectedColor = checkedIds.firstOrNull()?.let { id ->
                colorMap[id]
            }

            applyFilters()
        }

        // Search by title or content.
        binding.searchNotes.doAfterTextChanged {
            applyFilters()
        }

        // Toggle newest-first / oldest-first when the sort icon is tapped.
        binding.filterBy.setOnClickListener {
            oldestFirst = !oldestFirst
            applyFilters()

            Toast.makeText(
                requireContext(),
                if (oldestFirst) {
                    "Showing oldest notes first"
                } else {
                    "Showing newest notes first"
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun applyFilters() {
        if (_binding == null) return

        val query = binding.searchNotes.text
            .toString()
            .trim()

        val filteredNotes = allNotes
            .filter { note ->

                val matchesColor =
                    selectedColor == null ||
                            note.color.toInt() == selectedColor!!.toInt()

                val matchesSearch =
                    note.title.contains(query, ignoreCase = true) ||
                            note.content.contains(query, ignoreCase = true)

                matchesColor && matchesSearch
            }
            .let { notes ->
                if (oldestFirst) {
                    notes.sortedBy { it.updatedAt }
                } else {
                    notes.sortedByDescending { it.updatedAt }
                }
            }

        noteAdapter.submitList(filteredNotes)

        binding.noteCount.text = if (filteredNotes.size == 1) {
            "1 note"
        } else {
            "${filteredNotes.size} notes"
        }
    }

    private fun observeNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.notes.collect { notes ->
                    allNotes = notes
                    applyFilters()
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
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.clearError()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.notesRecyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}