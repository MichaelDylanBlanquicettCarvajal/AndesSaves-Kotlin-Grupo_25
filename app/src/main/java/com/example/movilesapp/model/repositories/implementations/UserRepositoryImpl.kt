package com.example.movilesapp.model.repositories.implementations

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.model.entities.*
import com.example.movilesapp.model.repositories.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository {

    private val db = FirebaseFirestore.getInstance()

    override suspend fun createUser(user: User): Boolean {
        try {
            val userData = hashMapOf(
                "userId" to user.userId,
                "name" to user.name,
                "phone" to user.phone,
                "email" to user.email,
                "balance" to user.balance
            )

            val tags = listOf(
                hashMapOf("name" to "Food"),
                hashMapOf("name" to "Transport"),
                hashMapOf("name" to "House"),
                hashMapOf("name" to "Other")
            )

            val batch = db.batch()

            val userRef = db.collection("users").document(user.userId)
            batch.set(userRef, userData, SetOptions.merge())

            val tagsRef = userRef.collection("tags")
            for (tag in tags) {
                val newTagRef = tagsRef.document()
                batch.set(newTagRef, tag)
            }

            batch.commit().await()

            UserSingleton.saveUserInfoSingleton(user)
            return true
        } catch (e: Exception) {
            return false
        }
    }


    override suspend fun getUserInformation(userId: String): User? {
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

    private suspend fun saveImageToStorage(base64Image: String, transactionId: String): String? {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val imagesRef = storageRef.child("Transactions/$transactionId.jpg")

        try {
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)

            val uploadTask = imagesRef.putBytes(imageBytes).await()

            try {
                val downloadUrl = imagesRef.downloadUrl.await()
                val imageUrl = downloadUrl.toString()

                imagesRef.updateMetadata(
                    StorageMetadata.Builder()
                        .setContentType("application/octet-stream")
                        .build()
                )
                return imageUrl

            } catch (e: Exception) {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun createTransaction(transaction: Transaction): Boolean {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val documentReference = db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .add(transaction)
                    .await()

                val transactionId = documentReference.id

                val imageUrl = saveImageToStorage(transaction.imageUri, transactionId)

                if (imageUrl != null) {
                    db.collection("users")
                        .document(userId)
                        .collection("transactions")
                        .document(transactionId)
                        .update("transactionId", transactionId, "imageUri", imageUrl)
                        .await()
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.d("User", "Exception creating transaction: ${e.message.toString()}")
            return false
        }
    }


    override suspend fun getTransactionsOfUser(): List<Transaction> {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("transactions")
                    .orderBy("date", Query.Direction.DESCENDING)
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

    override suspend fun getUserTags(): List<Tag> {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("tags")
                    .get()
                    .await()

                val tags = mutableListOf<Tag>()
                for (document in querySnapshot) {
                    val tag = document.toObject(Tag::class.java)
                    tags.add(tag)
                }
                return tags
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("User", "Exception getting user tags: ${e.message.toString()}")
            return emptyList()
        }
    }

    override suspend fun createBudget(budget: Budget): Boolean {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            return if (userId != null) {
                val documentReference = db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .add(budget)
                    .await()

                val budgetId = documentReference.id

                db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(budgetId)
                    .update("budgetId", budgetId)
                    .await()

                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            Log.d("User", "Exception creating budget: ${e.message.toString()}")
            return false
        }
    }


    override suspend fun getBudgets(): List<Budget> {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .orderBy("type")
                    .get()
                    .await()

                val budgets = mutableListOf<Budget>()

                val batch = db.batch()

                for (document in querySnapshot) {
                    val budget = document.toObject(Budget::class.java)
                    budgets.add(budget)

                    if (budget.budgetId == null || budget.budgetId.isEmpty()) {
                        val budgetId = document.id

                        val budgetRef = db.collection("users")
                            .document(userId)
                            .collection("budgets")
                            .document(budgetId)

                        batch.update(budgetRef, "budgetId", budgetId)
                    }
                }

                batch.commit().await()

                return budgets
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("User", "Exception getting budgets: ${e.message.toString()}")
            return emptyList()
        }
    }

    override suspend fun updateBudgetContributions(budgetId: String, newContributions: Double): Boolean {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val budgetRef = db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(budgetId)

                budgetRef.update("contributions", newContributions).await()
                return true
            }
        } catch (e: Exception) {
            Log.d("User", "Exception updating budget contributions: ${e.message.toString()}")
        }
        return false
    }

    override suspend fun deleteBudgetById(budgetId: String): Boolean {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val budgetRef = db.collection("users")
                    .document(userId)
                    .collection("budgets")
                    .document(budgetId)

                budgetRef.delete().await()
                return true
            }
        } catch (e: Exception) {
            Log.d("User", "Exception deleting budget: ${e.message.toString()}")
        }
        return false
    }

    override suspend fun getUserPredictions(): List<Prediction> {
        try {
            val userId = UserSingleton.getUserInfoSingleton()?.userId
            if (userId != null) {
                val querySnapshot = db.collection("users")
                    .document(userId)
                    .collection("predictions")
                    .get()
                    .await()

                val predictions = mutableListOf<Prediction>()
                for (document in querySnapshot) {
                    val predictionData = document.data
                    val month = (predictionData["month"] as Long).toInt()
                    val predictedExpense = predictionData["predicted_expense"] as Double
                    val year = (predictionData["year"] as Long).toInt()

                    val prediction = Prediction(month, predictedExpense, year)
                    predictions.add(prediction)
                }
                return predictions
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("User", "Exception getting user predictions: ${e.message.toString()}")
            return emptyList()
        }
    }



}