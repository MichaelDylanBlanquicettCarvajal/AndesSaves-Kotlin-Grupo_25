package com.example.movilesapp.model.entities

data class User(
    val userId: String,
    val name: String,
    val phone: Int,
    val email: String,
    val balance: Int
)