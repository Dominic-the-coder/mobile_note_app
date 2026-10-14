package com.example.notesapp.ui.manage.edit

import android.util.Log
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

class EditViewModel(
    private val repo: NoteRepo
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult

    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun setInitialNote(note: Note) {
        _note.value = note
    }


    fun loadNote(id: String) {
        if (id.isBlank()) return

        // Skip the request if this note is already loaded.
        if (_note.value?.id == id) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestNote = repo.getNoteById(id)

                if (latestNote != null) {
                    _note.value = latestNote
                }
            } catch (e: Exception) {
                Log.e(
                    "EditViewModel",
                    "Failed to refresh note",
                    e
                )
            }
        }
    }



    fun updateNote(
        title: String,
        content: String,
        color: Long
    ) {
        val currentNote = _note.value

        if (currentNote == null) {
            _error.value = "Note has not loaded yet"
            return
        }

        if (title.isBlank()) {
            _error.value = "Please enter a title"
            return
        }

        val updatedNote = currentNote.copy(
            title = title.trim(),
            content = content.trim(),
            color = color,
            updatedAt = System.currentTimeMillis()
        )

        // Update the UI state immediately.
        _note.value = updatedNote
        _saveResult.value = null
        _error.value = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Save the updated note to Firestore.
                repo.updateNote(updatedNote)

                _saveResult.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update note"
                _saveResult.value = false
            }
        }
    }


    fun moveToTrash() {
        val currentNote = _note.value

        if (currentNote?.id.isNullOrBlank()) {
            _error.value = "Note ID is missing"
            _deleteResult.value = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.deleteNote(currentNote!!.id!!)
                _deleteResult.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to move note to trash"
                _deleteResult.value = false
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
    }

    fun clearError() {
        _error.value = null
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MyApp
                EditViewModel(app.repo)
            }
        }
    }
}
