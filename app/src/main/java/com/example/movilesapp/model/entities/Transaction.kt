package com.example.movilesapp.model.entities

data class Transaction (
    val transactionId: String,
    val name: String,
    val amount: Double,
    val source: String,
    val type: String, // Income or Expense
    val category: String
)