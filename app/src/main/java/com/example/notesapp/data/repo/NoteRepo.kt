
package com.example.notesapp.data.repository

import com.example.notesapp.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepo {
    fun getNotes(): Flow<List<Note>>

    suspend fun getNoteById(id: String): Note?

    suspend fun addNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(id: String)

    fun getTrashNotes(): Flow<List<Note>>

    suspend fun restoreNote(id: String)

    suspend fun permanentlyDeleteNote(id: String)
}
