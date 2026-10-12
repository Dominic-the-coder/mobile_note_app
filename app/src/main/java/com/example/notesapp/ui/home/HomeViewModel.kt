
package com.example.notesapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.notesapp.MyApp
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: NoteRepo
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        getNotes()
    }

    fun getNotes() {
        viewModelScope.launch {
            repo.getNotes().collect { items ->
                _notes.update { items }
            }
        }
    }

    fun addNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.addNote(note)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to add note"
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.deleteNote(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete note"
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MyApp
                HomeViewModel(app.repo)
            }
        }
    }
}
