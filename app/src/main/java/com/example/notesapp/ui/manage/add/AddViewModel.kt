package com.example.notesapp.ui.manage.add

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

class AddViewModel(
    private val repository: NoteRepo
) : ViewModel() {

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult

    fun saveNote(title: String, content: String, color: Long) {
        if (title.isBlank() && content.isBlank()) {
            _saveResult.value = false
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val note = Note(
                    title = title.trim(),
                    content = content.trim(),
                    color = color,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isDeleted = false
                )

                repository.addNote(note)
                _saveResult.value = true
            } catch (e: Exception) {
                _saveResult.value = false
            }
        }
    }

    fun resetResult() {
        _saveResult.value = null
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MyApp
                AddViewModel(app.repo)
            }
        }
    }
}
