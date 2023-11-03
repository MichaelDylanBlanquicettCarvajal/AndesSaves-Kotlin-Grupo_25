package com.example.movilesapp.model.repositories

import com.google.firebase.auth.FirebaseUser

interface AuthRepository {
    suspend fun registerUser(email: String, password: String): FirebaseUser?
    suspend fun signInWithEmailAndPassword(email: String, password: String): Pair<Boolean, String?>
    suspend fun signOut()
}

