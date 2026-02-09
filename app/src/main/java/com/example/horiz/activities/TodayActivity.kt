package com.example.horiz.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.horiz.screens.TodayScreen
import com.example.horiz.ui.theme.HorizTheme

class TodayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HorizTheme {
                TodayScreen { finish() }
            }
        }
    }
}
