package com.example.movilesapp.model.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movilesapp.model.local.entities.TransactionEntity
import com.example.movilesapp.model.local.entities.UserEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions")
    fun getTransactions(): List<TransactionEntity>

    @Query("DELETE FROM transactions WHERE isSynced = 0")
    fun deleteUnsyncedTransactions()
}
