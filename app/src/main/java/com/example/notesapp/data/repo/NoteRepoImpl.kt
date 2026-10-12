package com.example.notesapp.data.repository

import com.example.notesapp.data.model.Note
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NoteRepoImpl(
    private val collectionReference: CollectionReference
) : NoteRepo {

    override fun getNotes(): Flow<List<Note>> = callbackFlow {
        val listener = collectionReference
            .whereEqualTo("isDeleted", false)
            .addSnapshotListener { snapshots, exception ->

                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val notes = snapshots?.documents?.mapNotNull { document ->
                    document.data?.let { data ->
                        Note.fromHashMap(document.id, data)
                    }
                } ?: emptyList()

                trySend(notes)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        val snapshot = collectionReference.document(id)
            .get()
            .await()

        return snapshot.data?.let { data ->
            Note.fromHashMap(snapshot.id, data)
        }
    }

    override suspend fun addNote(note: Note) {
        val now = System.currentTimeMillis()

        val newNote = note.copy(
            createdAt = now,
            updatedAt = now,
            isDeleted = false
        )

        collectionReference.add(newNote.toHashMap()).await()
    }

    override suspend fun updateNote(note: Note) {
        val id = note.id ?: return

        val updatedNote = note.copy(
            updatedAt = System.currentTimeMillis()
        )

        collectionReference.document(id)
            .set(updatedNote.toHashMap())
            .await()
    }

    override suspend fun deleteNote(id: String) {
        collectionReference.document(id)
            .update(
                mapOf(
                    "isDeleted" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    override fun getTrashNotes(): Flow<List<Note>> = callbackFlow {
        val listener = collectionReference
            .whereEqualTo("isDeleted", true)
            .addSnapshotListener { snapshots, exception ->

                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }

                val notes = snapshots?.documents?.mapNotNull { document ->
                    document.data?.let { data ->
                        Note.fromHashMap(document.id, data)
                    }
                } ?: emptyList()

                trySend(notes)
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun restoreNote(id: String) {
        collectionReference.document(id)
            .update(
                mapOf(
                    "isDeleted" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    suspend fun permanentlyDeleteNote(id: String) {
        collectionReference.document(id).delete().await()
    }
}
