package com.example.movilesapp.model.repositories.implementations

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.example.movilesapp.model.UserSingleton
import com.example.movilesapp.model.entities.*
import com.example.movilesapp.model.local.LocalDatabase
import com.example.movilesapp.model.local.daos.BudgetDao
import com.example.movilesapp.model.local.daos.PredictionDao
import com.example.movilesapp.model.local.daos.TransactionDao
import com.example.movilesapp.model.local.daos.UserDao
import com.example.movilesapp.model.local.entities.BudgetEntity
import com.example.movilesapp.model.local.entities.PredictionEntity
import com.example.movilesapp.model.local.entities.TransactionEntity
import com.example.movilesapp.model.local.entities.UserEntity
import com.example.movilesapp.model.repositories.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class UserRepositoryImpl(private val contextRepository: Context) : UserRepository {

    private val db = FirebaseFirestore.getInstance()

    private val localDatabase: LocalDatabase = LocalDatabase.getInstance(contextRepository)

    private val userDao: UserDao = localDatabase.userDao()
    private val transactionDao: TransactionDao = localDatabase.transactionDao()
    private val predictionDao: PredictionDao = localDatabase.predictionDao()
    private val budgetDao: BudgetDao = localDatabase.budgetDao()


    // ----------------------------------------
    // --------------- USER -------------------
    // ----------------------------------------

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
            saveUserInformationLocal(user)

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
                saveUserInformationLocal(user)
                return user
            } else {
                return null
            }
        } catch (e: Exception) {
            Log.d("User", "Exception user ${e.message.toString()}")
            return null
        }
    }

    private suspend fun saveUserInformationLocal(user: User) {
        val userEntity = UserEntity(
            userId = user.userId,
            name = user.name,
            phone = user.phone,
            email = user.email,
            balance = user.balance
        )

        try {
            withContext(Dispatchers.IO) {
                userDao.insertUser(userEntity)
            }
            Log.d("HOLA", "User information inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting user information: ${e.message}")
        }
    }


    // ----------------------------------------
    // ------------- TRANSACTIONS -------------
    // ----------------------------------------

    override suspend fun createTransaction(transaction: Transaction): Boolean {
        if (isNetworkAvailable()) {
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
        } else {
            saveUnsyncedTransactionLocal(transaction)
            return true
        }
        return false
    }

    override suspend fun getTransactionsOfUser(): List<Transaction> {
        if (isNetworkAvailable()) {
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
                        saveTransactionLocal(transaction)
                    }
                    return transactions
                } else {
                    return emptyList()
                }
            } catch (e: Exception) {
                Log.d("User", "Exception getting user transactions: ${e.message.toString()}")
                return emptyList()
            }
        } else {
            return getLocalTransactions()
        }
        return emptyList()
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

    private suspend fun saveTransactionLocal(transaction: Transaction) {
        val transactionEntity = TransactionEntity(
            transactionId = transaction.transactionId,
            name = transaction.name,
            amount = transaction.amount,
            source = transaction.source,
            type = transaction.type,
            category = transaction.category,
            date = transaction.date.seconds,
            imageUri = transaction.imageUri
        )

        // Agregar registros de Log para verificar el proceso
        Log.d("HOLA", "Transaction amount: ${transaction.amount}")
        Log.d("HOLA", "Transaction date: ${transaction.date.seconds}")

        try {
            withContext(Dispatchers.IO) {
                transactionDao.insertTransaction(transactionEntity)
            }
            Log.d("HOLA", "Transaction inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting transaction: ${e.message}")
        }
    }

    private suspend fun saveUnsyncedTransactionLocal(transaction: Transaction) {
        val transactionEntity = TransactionEntity(
            transactionId = generateProvisionalId(),
            name = transaction.name,
            amount = transaction.amount,
            source = transaction.source,
            type = transaction.type,
            category = transaction.category,
            date = transaction.date.seconds,
            imageUri = transaction.imageUri,
            isSynced = false
        )

        Log.d("HOLA", "Transaction name: ${transaction.name}")
        Log.d("HOLA", "Transaction date: ${transaction.date.seconds}")

        try {
            withContext(Dispatchers.IO) {
                transactionDao.insertTransaction(transactionEntity)
            }
            Log.d("HOLA", "Unsynced Transaction inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting unsynced transaction: ${e.message}")
        }
    }

    private suspend fun getLocalTransactions(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                val localTransactions = transactionDao.getTransactions()
                Log.d("HOLA", "Local transactions count: ${localTransactions.size}")

                val mappedTransactions = localTransactions.map {
                    val date = Date(it.date * 1000L)
                    Transaction(
                        transactionId = it.transactionId,
                        name = it.name,
                        amount = it.amount,
                        source = it.source,
                        type = it.type,
                        category = it.category,
                        date = Timestamp(date),
                        imageUri = it.imageUri
                    )
                }

                Log.d("HOLA", "Mapped transactions count: ${mappedTransactions.size}")
                return@withContext mappedTransactions
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting local transactions: ${e.message}")
                return@withContext emptyList<Transaction>()
            }
        }
    }

    private suspend fun getUnsyncedLocalTransactions(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedLocalTransactions = transactionDao.getUnsyncedTransactions()
                Log.d(
                    "HOLA",
                    "Unsynced local transactions count: ${unsyncedLocalTransactions.size}"
                )

                val mappedTransactions = unsyncedLocalTransactions.map {
                    val date = Date(it.date * 1000L)
                    Transaction(
                        transactionId = it.transactionId,
                        name = it.name,
                        amount = it.amount,
                        source = it.source,
                        type = it.type,
                        category = it.category,
                        date = Timestamp(date),
                        imageUri = it.imageUri
                    )
                }

                Log.d("HOLA", "Mapped unsynced transactions count: ${mappedTransactions.size}")
                return@withContext mappedTransactions
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting unsynced local transactions: ${e.message}")
                return@withContext emptyList<Transaction>()
            }
        }
    }

    override suspend fun syncTransactionsFirebase() {
        val unsyncedLocalTransactions = getUnsyncedLocalTransactions()

        withContext(Dispatchers.IO) {
            for (transaction in unsyncedLocalTransactions) {
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

                        db.collection("users")
                            .document(userId)
                            .collection("transactions")
                            .document(transactionId)
                            .update("transactionId", transactionId, "imageUri", imageUrl)
                            .await()

                    }
                } catch (e: Exception) {
                    Log.e("Sync", "Error syncing transaction: ${e.message}")
                }
            }
            transactionDao.deleteUnsyncedTransactions()
        }
    }


    // ----------------------------------------
    // ---------------- BUDGET ----------------
    // ----------------------------------------


    override suspend fun createBudget(budget: Budget): Boolean {
        if (isNetworkAvailable()) {
            try {
                val userId = UserSingleton.getUserInfoSingleton()?.userId
                if (userId != null) {
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
                }
            } catch (e: Exception) {
                Log.d("User", "Exception creating budget: ${e.message.toString()}")
            }
        } else {
            saveUnsyncedBudgetLocal(budget)
            return true
        }
        return false
    }

    override suspend fun getBudgets(): List<Budget> {
        if (isNetworkAvailable()) {
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

                        saveBudgetLocal(budget)
                    }

                    batch.commit().await()

                    return budgets
                } else {
                    return emptyList()
                }
            } catch (e: Exception) {
                Log.d("User", "Exception getting budgets: ${e.message.toString()}")
            }
        } else {
            return getLocalBudgets()
        }

        return emptyList()
    }

    override suspend fun updateBudgetContributions(
        budgetId: String,
        newContributions: Double
    ): Boolean {
        if (isNetworkAvailable()) {
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
                return false
            }
        } else {
            try {
                withContext(Dispatchers.IO) {
                    budgetDao.updateContributionsAndMarkForUpdate(budgetId, newContributions)
                }
                Log.d("HOLA", budgetId)
                Log.d("HOLA", "Budget updated successfully")
                return true
            } catch (e: Exception) {
                Log.e("HOLA", "Error updating local budget: ${e.message.toString()}")
                return false
            }
        }
        return false
    }

    override suspend fun deleteBudgetById(budgetId: String): Boolean {
        if(isNetworkAvailable()) {
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
                return false
            }
        }
        else{
            try {
                withContext(Dispatchers.IO) {
                    budgetDao.markBudgetForDeletion(budgetId)
                }
                Log.d("HOLA", budgetId)
                Log.d("HOLA", "Budget deleted successfully")
                return true
            } catch (e: Exception) {
                Log.e("HOLA", "Error updating local budget: ${e.message.toString()}")
                return false
            }
        }
        return false
    }

    private suspend fun saveBudgetLocal(budget: Budget) {
        val budgetEntity = BudgetEntity(
            budgetId = budget.budgetId,
            user = budget.user,
            name = budget.name,
            contributions = budget.contributions,
            total = budget.total,
            type = budget.type,
            date = budget.date.seconds
        )

        // Agregar registros de Log para verificar el proceso
        Log.d("HOLA", "Budget name: ${budget.name}")
        Log.d("HOLA", "Budget ID: ${budget.budgetId}")

        try {
            withContext(Dispatchers.IO) {
                budgetDao.insertBudget(budgetEntity)
            }
            Log.d("HOLA", "Budget inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting budget: ${e.message}")
        }
    }

    private suspend fun saveUnsyncedBudgetLocal(budget: Budget) {
        val budgetEntity = BudgetEntity(
            budgetId = generateProvisionalId(),
            user = budget.user,
            name = budget.name,
            contributions = budget.contributions,
            total = budget.total,
            type = budget.type,
            date = budget.date.seconds,
            isSynced = false
        )

        Log.d("HOLA", "Budget name: ${budget.name}")
        Log.d("HOLA", "Budget date: ${budget.date.seconds}")

        try {
            withContext(Dispatchers.IO) {
                budgetDao.insertBudget(budgetEntity)
            }
            Log.d("HOLA", "Unsynced Budget inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting unsynced budget: ${e.message}")
        }
    }

    private suspend fun getLocalBudgets(): List<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val localBudgets = budgetDao.getBudgets()
                Log.d("HOLA", "Local budgets count: ${localBudgets.size}")

                val mappedBudgets = localBudgets
                    .filterNot { it.isMarkedForDeletion } // Filtrar los marcados para eliminación
                    .map {
                        val date = Date(it.date * 1000L)
                        Budget(
                            budgetId = it.budgetId,
                            user = it.user,
                            name = it.name,
                            contributions = it.contributions,
                            total = it.total,
                            type = it.type,
                            date = Timestamp(date)
                        )
                    }

                Log.d("HOLA", "Mapped budgets count: ${mappedBudgets.size}")
                return@withContext mappedBudgets
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting local budgets: ${e.message}")
                return@withContext emptyList<Budget>()
            }
        }
    }


    private suspend fun getUnsyncedLocalBudgets(): List<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedLocalBudgets = budgetDao.getUnsyncedBudgets()
                Log.d("HOLA", "Unsynced local budgets count: ${unsyncedLocalBudgets.size}")

                val mappedBudgets = unsyncedLocalBudgets.map {
                    val date = Date(it.date * 1000L)
                    Budget(
                        budgetId = it.budgetId,
                        user = it.user,
                        name = it.name,
                        contributions = it.contributions,
                        total = it.total,
                        type = it.type,
                        date = Timestamp(date)
                    )
                }

                Log.d("HOLA", "Mapped unsynced budgets count: ${mappedBudgets.size}")
                return@withContext mappedBudgets
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting unsynced local budgets: ${e.message}")
                return@withContext emptyList<Budget>()
            }
        }
    }

    private suspend fun getUnsyncedNeedUpdateLocalBudgets(): List<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val needUpdateLocalBudgets = budgetDao.getNeedUpdateBudgets()
                Log.d("HOLA", "Need Update local budgets count: ${needUpdateLocalBudgets.size}")

                val mappedBudgets = needUpdateLocalBudgets.map {
                    val date = Date(it.date * 1000L)
                    Budget(
                        budgetId = it.budgetId,
                        user = it.user,
                        name = it.name,
                        contributions = it.contributions,
                        total = it.total,
                        type = it.type,
                        date = Timestamp(date)
                    )
                }

                Log.d("HOLA", "Mapped update budgets count: ${mappedBudgets.size}")
                return@withContext mappedBudgets
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting update local budgets: ${e.message}")
                return@withContext emptyList<Budget>()
            }
        }
    }

    private suspend fun getDeleteMarkLocalBudgets(): List<Budget> {
        return withContext(Dispatchers.IO) {
            try {
                val deleteLocalBudgets = budgetDao.getMarkedForDeletionBudgets()
                Log.d("HOLA", "Delete local budgets count: ${deleteLocalBudgets.size}")

                val mappedBudgets = deleteLocalBudgets.map {
                    val date = Date(it.date * 1000L)
                    Budget(
                        budgetId = it.budgetId,
                        user = it.user,
                        name = it.name,
                        contributions = it.contributions,
                        total = it.total,
                        type = it.type,
                        date = Timestamp(date)
                    )
                }

                Log.d("HOLA", "Mapped delete budgets count: ${mappedBudgets.size}")
                return@withContext mappedBudgets
            } catch (e: Exception) {
                Log.e("HOLA", "Error getting delete local budgets: ${e.message}")
                return@withContext emptyList<Budget>()
            }
        }
    }

    override suspend fun syncBudgetsFirebase() {
        val unsyncedLocalBudgets = getUnsyncedLocalBudgets()

        withContext(Dispatchers.IO) {
            for (budget in unsyncedLocalBudgets) {
                try {
                    val userId = UserSingleton.getUserInfoSingleton()?.userId
                    if (userId != null) {
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

                    }
                } catch (e: Exception) {
                    Log.e("Sync", "Error syncing budget: ${e.message}")
                }
            }
            budgetDao.deleteUnsyncedBudgets()
        }
    }

    override suspend fun syncUpdateBudgetsFirebase() {
        val needUpdateLocalBudgets = getUnsyncedNeedUpdateLocalBudgets()

        withContext(Dispatchers.IO) {
            for (budget in needUpdateLocalBudgets) {
                try {
                    Log.d("HOLA",budget.budgetId)
                    val userId = UserSingleton.getUserInfoSingleton()?.userId
                    if (userId != null) {
                        val budgetRef = db.collection("users")
                            .document(userId)
                            .collection("budgets")
                            .document(budget.budgetId)

                        budgetRef.update("contributions", budget.contributions).await()
                    }
                    Log.d("HOLA", "Success Updating contributions of Budget")
                } catch (e: Exception) {
                    Log.e("HOLA", "Error syncing in Update budget: ${e.message}")
                }
            }
            budgetDao.deleteNeedUpdateBudgets()
        }
    }

    override suspend fun syncDeleteBudgetsFirebase() {
        val deleteLocalBudgets = getDeleteMarkLocalBudgets()

        withContext(Dispatchers.IO) {
            for (budget in deleteLocalBudgets) {
                try {
                    val userId = UserSingleton.getUserInfoSingleton()?.userId
                    if (userId != null) {
                        val budgetRef = db.collection("users")
                            .document(userId)
                            .collection("budgets")
                            .document(budget.budgetId)

                        budgetRef.delete().await()
                        Log.d("HOLA", "Success Deleting Budget")
                    }
                } catch (e: Exception) {
                    Log.e("HOLA", "Error Deleting in Delete budget: ${e.message}")
                }
            }
            budgetDao.deleteMarkedForDeletionBudgets()
        }
    }



    // ----------------------------------------
    // ------------- PREDICTIONS --------------
    // ----------------------------------------

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

                    val prediction = Prediction(document.id, month, predictedExpense, year)
                    predictions.add(prediction)
                    savePredictionLocal(prediction)
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


    private suspend fun savePredictionLocal(prediction: Prediction) {
        val predictionEntity = PredictionEntity(
            predictionId = prediction.predictionID,
            month = prediction.month,
            predictedExpense = prediction.predicted_expense,
            year = prediction.year
        )

        Log.d("HOLA", "Prediction month: ${prediction.month}")
        Log.d("HOLA", "Prediction year: ${prediction.year}")

        try {
            withContext(Dispatchers.IO) {
                predictionDao.insertPrediction(predictionEntity)
            }
            Log.d("HOLA", "Prediction inserted successfully")
        } catch (e: Exception) {
            Log.e("HOLA", "Error inserting prediction: ${e.message}")
        }
    }


    // ----------------------------------------
    // ----------------- TAGS -----------------
    // ----------------------------------------

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


    // ----------------------------------------
    // ---------------- UTILS -----------------
    // ----------------------------------------

    private fun generateProvisionalId(): String {
        return UUID.randomUUID().toString()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            contextRepository.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

}