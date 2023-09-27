package com.example.movilesapp.model

import com.example.movilesapp.model.entities.User

object UserSingleton {
    private var userSingleton: User? = null

    fun getUserInfoSingleton(): User? {
        return userSingleton
    }

    fun saveUserInfoSingleton(newUser: User) {
        userSingleton = newUser
    }
}
