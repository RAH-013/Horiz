package com.example.horiz.activities

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.horiz.screens.MainScreen
import com.example.horiz.ui.theme.HorizTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val requestNotificationPermission =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (!granted) {
                        showPermissionDialog(
                            "Notificaciones",
                            "Para recibir recordatorios de tus clases ⏰ necesitamos permiso para enviar notificaciones 📢."
                        )
                    }
                }
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(AlarmManager::class.java)
            if (am != null && !am.canScheduleExactAlarms()) {
                showPermissionDialog(
                    "Alarmas exactas",
                    "Para avisarte con precisión sobre tus clases ⏰, la app necesita permiso para programar alarmas exactas ⚡."
                )
            }
        }

        setContent {
            HorizTheme {
                MainScreen { finish() }
            }
        }
    }

    private fun showPermissionDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ $title requerido")
            .setMessage(message)
            .setPositiveButton("Ir a ajustes") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}