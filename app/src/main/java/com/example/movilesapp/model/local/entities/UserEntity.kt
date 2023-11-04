package com.example.movilesapp.model.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: Long,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "balance") val balance: Double,
    val isSynced: Boolean = true
)
