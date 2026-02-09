package com.example.horiz.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.horiz.screens.QrScreen
import com.example.horiz.ui.theme.HorizTheme

class QrActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val scheduleName = intent.getStringExtra("schedule_name")
            ?: error("schedule_name es obligatorio")

        setContent {
            HorizTheme {
                QrScreen(
                    scheduleName = scheduleName,
                    onBack = { finish() }
                )
            }
        }
    }
}