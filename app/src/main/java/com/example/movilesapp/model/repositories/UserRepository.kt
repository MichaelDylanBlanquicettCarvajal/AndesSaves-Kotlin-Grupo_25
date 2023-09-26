package com.example.movilesapp.model.repositories

import android.util.Log
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.entities.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun createUser(user: User): Boolean {
        try {
            val userData = hashMapOf(
                "userId" to user.userId,
                "name" to user.name,
                "phone" to user.phone,
                "email" to user.email,
                "balance" to user.balance
            )

            db.collection("users").document(user.userId)
                .set(userData, SetOptions.merge())
                .await()

            UserSingleton.saveUserInfoSingleton(user)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun getUserInformation(userId: String): User? {
        try {
            val document = db.collection("users").document(userId).get().await()

            if (document.exists()) {
                val userData = document.data
                val user = User(
                    userId = userData!!["userId"] as String,
                    name = userData!!["name"] as String,
                    phone = userData!!["phone"] as Long,
                    email = userData!!["email"] as String,
                    balance = userData!!["balance"] as Double
                )
                UserSingleton.saveUserInfoSingleton(user)
                return user
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.d("User", "Exception user ${e.message.toString()}")
            return null
        }
    }

    suspend fun createTransaction(transaction: Transaction): Boolean {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val documentReference = db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .add(transaction)
                    .await()

                db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .document(documentReference.id)
                    .update("transactionId", documentReference.id)
                    .await()
                return true
            } else {
                return false
            }

        } catch (e: Exception) {
            Log.d("User", "Exception creating transaction: ${e.message.toString()}")
            return false
        }
    }

    suspend fun getTransactionsOfUser(): List<Transaction> {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("transactionId", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val transactions = mutableListOf<Transaction>()
                for (document in querySnapshot) {
                    val transaction = document.toObject(Transaction::class.java)
                    transactions.add(transaction)
                }
                return transactions
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("User", "Exception getting user transactions: ${e.message.toString()}")
            return emptyList()
        }
    }

}

