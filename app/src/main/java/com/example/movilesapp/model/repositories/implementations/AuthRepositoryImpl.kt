package com.example.movilesapp.model.repositories.implementations

import android.content.Context
import com.example.movilesapp.model.local.LocalDatabase
import com.example.movilesapp.model.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(private val contextRepository: Context) : AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val localDatabase: LocalDatabase = LocalDatabase.getInstance(contextRepository)


    override suspend fun registerUser(email: String, password: String): FirebaseUser? {
        return withContext(Dispatchers.IO) {
            try {
                localDatabase.clearAllTables()

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                return@withContext user
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Pair<Boolean, String?> {
        return withContext(Dispatchers.IO) {
            try {
                localDatabase.clearAllTables()

                val result = auth.signInWithEmailAndPassword(email, password).await()
                return@withContext Pair(result.user != null, result.user?.uid)
            } catch (ex: Exception) {
                return@withContext Pair(false, ex.message)
            }
        }
    }

    override suspend fun signOut() {
        return withContext(Dispatchers.IO) {
            localDatabase.clearAllTables()

            auth.signOut()
        }
    }

}
