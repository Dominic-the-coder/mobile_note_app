package com.example.notesapp.core

import com.google.firebase.auth.FirebaseAuth

object AuthService {
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { note ->
                if(note.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, note.exception?.message)
                }
            }
    }

    fun register(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { note ->
                if(note.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, note.exception?.message)
                }
            }
    }

    fun isUserLoggedIn() = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }
}