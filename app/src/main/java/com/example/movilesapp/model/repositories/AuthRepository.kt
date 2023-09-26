package com.example.movilesapp.model.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun registerUser(email: String, password: String): FirebaseUser? {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            return user
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Pair<Boolean, String?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Pair(result.user != null, result.user?.uid)
        } catch (ex: Exception) {
            Pair(false, ex.message)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

}

