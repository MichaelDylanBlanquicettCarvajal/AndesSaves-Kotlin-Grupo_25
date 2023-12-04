package com.example.movilesapp.model.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "amount") val amount: Double,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "type") val type: String, // Income or Expense
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "image_uri") val imageUri: String,
    val isSynced: Boolean = true
)
