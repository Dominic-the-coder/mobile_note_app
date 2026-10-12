package com.example.notesapp

import android.app.Application
import com.example.notesapp.data.repository.NoteRepo
import com.example.notesapp.data.repository.NoteRepoImpl
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApp : Application() {

    lateinit var repo: NoteRepo

    override fun onCreate() {
        super.onCreate()

        repo = NoteRepoImpl(
            FirebaseFirestore.getInstance().collection("notes")
        )
    }
}
