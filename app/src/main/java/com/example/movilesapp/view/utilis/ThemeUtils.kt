package com.example.movilesapp.view.utilis

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar

// ThemeUtils.kt
object ThemeUtils {
    private var currentNightMode: Int = -1

    private fun isNightTime(): Boolean {
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        return currentHour >= 18 || currentHour < 6
    }

    fun checkAndSetNightMode(context: Context) {
        val isNightTime = isNightTime()
        val newNightMode = if (isNightTime) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        if (newNightMode != currentNightMode) {
            setNightMode(context, newNightMode)
            currentNightMode = newNightMode
        }
    }

    private fun setNightMode(context: Context, nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)

        if (context is AppCompatActivity) {
            context.recreate()
        }
    }

    fun isDarkModeEnabled(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}

