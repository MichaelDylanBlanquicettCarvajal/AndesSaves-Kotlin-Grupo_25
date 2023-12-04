package com.example.movilesapp.model.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey
    val predictionId: String,
    val month: Int,
    val predictedExpense: Double,
    val year: Int,
    val isSynced: Boolean = true
)