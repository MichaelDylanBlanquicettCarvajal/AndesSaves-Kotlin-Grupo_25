package com.example.movilesapp.model.repositories

import com.example.movilesapp.model.entities.Budget
import com.example.movilesapp.model.entities.Tag
import com.example.movilesapp.model.entities.Transaction
import com.example.movilesapp.model.entities.User

interface UserRepository {
    suspend fun createUser(user: User): Boolean
    suspend fun getUserInformation(userId: String): User?
    suspend fun createTransaction(transaction: Transaction): Boolean
    suspend fun getTransactionsOfUser(): List<Transaction>
    suspend fun getUserTags(): List<Tag>
    suspend fun createBudget(budget: Budget): Boolean
    suspend fun getBudgets(): List<Budget>
    suspend fun updateBudgetContributions(budgetId: String, newContributions: Double): Boolean
    suspend fun deleteBudgetById(budgetId: String): Boolean
}
