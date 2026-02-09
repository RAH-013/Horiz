package com.example.horiz.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.horiz.screens.DataScreen
import com.example.horiz.ui.theme.HorizTheme

class DataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { HorizTheme { DataScreen { finish() } } }
    }
}