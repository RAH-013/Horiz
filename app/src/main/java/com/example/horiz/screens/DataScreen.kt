package com.example.horiz.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.horiz.storage.ScheduleStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val storage = remember { ScheduleStorage(context) }
    val scope = rememberCoroutineScope()

    var prefsText by remember { mutableStateOf("Cargando SharedPreferences...") }
    var debugText by remember { mutableStateOf("Cargando archivos de horarios...") }

    fun loadPrefs() {
        val prefs = context.getSharedPreferences("hzsch_prefs", Context.MODE_PRIVATE)
        val all = prefs.all

        val sb = StringBuilder()
        sb.appendLine("📦 SharedPreferences (hzsch_prefs)")
        sb.appendLine("──────────────────────────────")

        if (all.isEmpty()) sb.appendLine("VACÍO")
        else all.forEach { (k, v) -> sb.appendLine("$k = $v") }

        prefsText = sb.toString()
    }

    fun loadDebugFiles() {
        scope.launch(Dispatchers.IO) {
            debugText = storage.debugFiles()
        }
    }

    fun resetAllData() {
        val prefs = context.getSharedPreferences("hzsch_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        scope.launch(Dispatchers.IO) {
            storage.removeAllSchedules()
            loadDebugFiles()
        }

        loadPrefs()
    }

    LaunchedEffect(Unit) {
        loadPrefs()
        loadDebugFiles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HZSCH Files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { loadPrefs(); loadDebugFiles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar todo")
                    }
                    IconButton(onClick = { resetAllData() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Resetear todo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text("📦 SharedPreferences", color = Color(0xFFB3E5FC), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(prefsText, style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
            ) {
                Text("📁 Archivos de Horarios", color = Color(0xFFB3E5FC), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(debugText, style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
        }
    }
}