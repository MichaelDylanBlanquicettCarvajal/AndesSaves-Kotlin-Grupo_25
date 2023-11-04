package com.example.movilesapp.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.movilesapp.model.local.daos.BudgetDao
import com.example.movilesapp.model.local.daos.PredictionDao
import com.example.movilesapp.model.local.daos.TransactionDao
import com.example.movilesapp.model.local.daos.UserDao
import com.example.movilesapp.model.local.entities.BudgetEntity
import com.example.movilesapp.model.local.entities.PredictionEntity
import com.example.movilesapp.model.local.entities.TransactionEntity
import com.example.movilesapp.model.local.entities.UserEntity

@Database(
    entities = [UserEntity::class, TransactionEntity::class, BudgetEntity::class, PredictionEntity::class],
    version = 11,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun predictionDao(): PredictionDao

    companion object {
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        fun getInstance(context: Context): LocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalDatabase::class.java,
                    "local_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

