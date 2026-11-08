package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.data.repository.BusRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.SmilaBusTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = BusRepository(applicationContext)
        val prefs = getSharedPreferences("smilabus_theme", Context.MODE_PRIVATE)

        setContent {
            val systemDark = isSystemInDarkTheme()
            val initialDark = remember { prefs.getBoolean("dark_theme", systemDark) }
            var isDarkTheme by remember { mutableStateOf(initialDark) }

            SmilaBusTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        repository = repository,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = {
                            val newTheme = !isDarkTheme
                            isDarkTheme = newTheme
                            prefs.edit().putBoolean("dark_theme", newTheme).apply()
                        }
                    )
                }
            }
        }
    }
}
