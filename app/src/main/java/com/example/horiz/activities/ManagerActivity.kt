package com.example.horiz.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.horiz.screens.ManagerScreen
import com.example.horiz.storage.ScheduleStorage
import com.example.horiz.ui.theme.HorizTheme

class ManagerActivity : ComponentActivity() {

    private lateinit var storage: ScheduleStorage
    var refreshSchedules by mutableStateOf(false)

    private lateinit var scannerLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = ScheduleStorage(this)

        enableEdgeToEdge()

        scannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshSchedules = !refreshSchedules
        }

        setContent {
            HorizTheme {
                ManagerScreen(
                    onBack = { finish() },
                    storage = storage,
                    refreshFlag = refreshSchedules,
                    launchScanner = { intent -> scannerLauncher.launch(intent) }
                )
            }
        }
    }
}