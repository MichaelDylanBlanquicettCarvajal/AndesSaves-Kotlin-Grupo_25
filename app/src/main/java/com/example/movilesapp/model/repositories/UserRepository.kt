package com.example.movilesapp.model.repositories

import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.entities.User

interface UserRepository {
    suspend fun createUser(user: User): Boolean
    suspend fun getUserInformation(userId: String): User?
    suspend fun createTransaction(transaction: Transaction): Boolean
    suspend fun getTransactionsOfUser(): List<Transaction>
}
