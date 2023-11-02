package com.example.movilesapp.model.entities
import com.google.firebase.Timestamp

data class Budget (
    val user: String = "",
    val name: String = "",
    val contributions: Double = 0.0,
    val total: Double = 0.0,
    val type: Int = 0, // 0 -> Individual / 1 -> Group
    val date: Timestamp = Timestamp.now(),
    val budgetId: String = "",
)

