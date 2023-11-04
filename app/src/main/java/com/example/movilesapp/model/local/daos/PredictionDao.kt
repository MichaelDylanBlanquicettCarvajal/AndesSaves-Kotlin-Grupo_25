package com.example.movilesapp.model.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.movilesapp.model.local.entities.PredictionEntity

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrediction(prediction: PredictionEntity)

}