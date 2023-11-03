package com.example.movilesapp.model.entities

data class User(
    val userId: String,
    val name: String,
    val phone: Long,
    val email: String,
    val balance: Double
)