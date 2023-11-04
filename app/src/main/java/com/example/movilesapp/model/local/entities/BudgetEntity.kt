package com.example.movilesapp.model.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val budgetId: String,
    val user: String,
    val name: String,
    val contributions: Double,
    val total: Double,
    val type: Int, // 0 -> Individual / 1 -> Group
    val date: Long,
    val needUpdate: Boolean = false,
    val isSynced: Boolean = true,
    val isMarkedForDeletion: Boolean = false
)