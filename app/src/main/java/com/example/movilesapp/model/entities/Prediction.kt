package com.example.movilesapp.model.entities

data class Prediction (
    val predictionID: String = "",
    val month: Int = 0,
    val predicted_expense: Double = 0.0,
    val year: Int = 0,
)