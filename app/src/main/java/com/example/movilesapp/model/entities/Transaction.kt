package com.example.movilesapp.model.entities
import com.google.firebase.Timestamp

data class Transaction (
    val transactionId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val source: String = "",
    val type: String = "", // Income or Expense
    val category: String = "",
    val date: Timestamp = Timestamp.now(),
    val imageUri: String = ""
)

