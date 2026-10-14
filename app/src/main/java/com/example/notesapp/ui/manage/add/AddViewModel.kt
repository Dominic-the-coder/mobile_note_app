package com.example.notesapp.ui.manage.add

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.notesapp.MyApp
import com.example.notesapp.data.model.Note
import com.example.notesapp.data.repository.NoteRepo
import com.example.notesapp.ui.manage.base.BaseManageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddViewModel(
    repo: NoteRepo
) : BaseManageViewModel(repo) {

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult

    fun saveNote(
        title: String,
        content: String,
        color: Long
    ) {
        if (title.isBlank() && content.isBlank()) {
            _saveResult.value = false
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val note = Note(
                        title = title,
                        content = content,
                        color = color,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        isDeleted = false
                    )

                    repo.addNote(note)
                }

                _saveResult.value = true
                _finish.emit(Unit)

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