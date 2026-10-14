package com.example.notesapp.ui.manage.base

import androidx.lifecycle.ViewModel
import com.example.notesapp.data.repository.NoteRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseManageViewModel(
    protected val repo: NoteRepo
) : ViewModel() {

    protected val _finish = MutableSharedFlow<Unit>()
    val finish: SharedFlow<Unit> = _finish

    protected val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearError() {
        _error.value = null
    }
}