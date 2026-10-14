package com.example.notesapp.ui.manage.edit

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.notesapp.MyApp
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepo
import com.example.notesapp.ui.manage.base.BaseManageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditViewModel(
    repo: NoteRepo
) : BaseManageViewModel(repo) {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult

    private val _deleteResult = MutableStateFlow<Boolean?>(null)
    val deleteResult: StateFlow<Boolean?> = _deleteResult

    private var loadedNoteId: String? = null

    fun setInitialNote(note: Note) {
        _note.value = note
    }

    fun loadNote(id: String) {
        if (id.isBlank() || loadedNoteId == id) return

        loadedNoteId = id

        viewModelScope.launch {
            try {
                val loadedNote = withContext(Dispatchers.IO) {
                    repo.getNoteById(id)
                }

                if (loadedNote != null) {
                    _note.value = loadedNote
                } else {
                    _error.value = "Note not found"
                    loadedNoteId = null
                }
            } catch (e: Exception) {
                loadedNoteId = null
                _error.value = e.message ?: "Failed to load note"
            }
        }
    }

    fun updateNote(
        title: String,
        content: String,
        color: Long
    ) {
        val currentNote = _note.value

        if (currentNote == null || currentNote.id.isNullOrBlank()) {
            _error.value = "Note not found"
            _saveResult.value = false
            return
        }

        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            _saveResult.value = false
            return
        }

        val updatedNote = currentNote.copy(
            title = title.trim(),
            content = content.trim(),
            color = color,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repo.updateNote(updatedNote)
                }

                _note.value = updatedNote
                _saveResult.value = true
                _finish.emit(Unit)
            } catch (e: Exception) {
                _saveResult.value = false
                _error.value = e.message ?: "Failed to update note"
            }
        }
    }

    fun moveToTrash() {
        val currentNote = _note.value
        val id = currentNote?.id

        if (id.isNullOrBlank()) {
            _error.value = "Note not found"
            _deleteResult.value = false
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repo.deleteNote(id)
                }

                _deleteResult.value = true
            } catch (e: Exception) {
                _deleteResult.value = false
                _error.value = e.message ?: "Failed to move note to Trash"
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = null
    }

    fun resetDeleteResult() {
        _deleteResult.value = null
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