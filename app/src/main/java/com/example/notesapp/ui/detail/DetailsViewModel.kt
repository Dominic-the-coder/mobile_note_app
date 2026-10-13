package com.example.notesapp.ui.detail

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
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val repo: NoteRepo
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult

    fun setInitialNote(note: Note) {
        _note.value = note
    }

    fun loadNote(id: String) {
        if (id.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestNote = repo.getNoteById(id)
                if (latestNote != null) {
                    _note.value = latestNote
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "DetailsViewModel",
                    "Failed to refresh note",
                    e
                )
            }
        }
    }

    fun deleteNote(id: String) {
        if (id.isBlank()) {
            _deleteResult.value = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.deleteNote(id)
                _deleteResult.value = true
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MyApp
                DetailsViewModel(app.repo)
            }
        }
    }
}
