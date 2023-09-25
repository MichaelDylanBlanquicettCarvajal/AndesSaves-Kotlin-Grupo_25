package com.example.movilesapp.model.repositories

import com.example.movilesapp.model.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createUser(user: User): Boolean {
        try {
            val userId = user.userId
            val userData = hashMapOf(
                "userId" to user.userId,
                "name" to user.name,
                "phone" to user.phone,
                "email" to user.email,
                "balance" to user.balance
            )

            db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .await()

            return true
        } catch (e: Exception) {
            return false
        }
    }
}

