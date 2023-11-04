package com.example.movilesapp.model.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilesapp.model.local.entities.BudgetEntity

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBudget(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE isSynced = 0")
    fun getUnsyncedBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE needUpdate = 1")
    fun getNeedUpdateBudgets(): List<BudgetEntity>

    @Query("SELECT * FROM budgets")
    fun getBudgets(): List<BudgetEntity>

    @Query("UPDATE budgets SET contributions = :newContributions, needUpdate = 1 WHERE budgetId = :budgetId")
    fun updateContributionsAndMarkForUpdate(budgetId: String, newContributions: Double)

    @Query("DELETE FROM budgets WHERE needUpdate = 1")
    fun deleteNeedUpdateBudgets()

    @Query("DELETE FROM budgets WHERE isSynced = 0")
    fun deleteUnsyncedBudgets()

    @Query("DELETE FROM budgets WHERE isMarkedForDeletion = 1")
    fun deleteMarkedForDeletionBudgets()

    @Query("SELECT * FROM budgets WHERE isMarkedForDeletion = 1")
    fun getMarkedForDeletionBudgets(): List<BudgetEntity>

    @Query("UPDATE budgets SET isMarkedForDeletion = 1 WHERE budgetId = :budgetId")
    fun markBudgetForDeletion(budgetId: String)
}