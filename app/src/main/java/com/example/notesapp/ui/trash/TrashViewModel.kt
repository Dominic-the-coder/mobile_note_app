package com.example.notesapp.ui.trash

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
import kotlinx.coroutines.flow.catch

class TrashViewModel(
    private val repo: NoteRepo
) : ViewModel() {

    val trashNotes: StateFlow<List<Note>> =
        MutableStateFlow(emptyList())

    private val _notes = trashNotes as MutableStateFlow<List<Note>>

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repo.getTrashNotes()
                .catch { e ->
                    _message.value = e.message ?: "Failed to load trash"
                    _isLoading.value = false
                }
                .collect { notes ->
                    _notes.value = notes
                    _isLoading.value = false
                }
        }
    }

    fun restoreNote(note: Note) {
        val id = note.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.restoreNote(id)
                _message.value = "Note restored"
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to restore note"
            }
        }
    }

    fun permanentlyDeleteNote(note: Note) {
        val id = note.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.permanentlyDeleteNote(id)
                _message.value = "Note permanently deleted"
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to delete note"
            }
        }
    }

    fun emptyTrash() {
        val notesToDelete = _notes.value

        if (notesToDelete.isEmpty()) {
            _message.value = "Trash is already empty"
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                notesToDelete.forEach { note ->
                    note.id?.let { repo.permanentlyDeleteNote(it) }
                }
                _message.value = "Trash emptied"
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to empty trash"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MyApp
                TrashViewModel(app.repo)
            }
        }
    }
}
