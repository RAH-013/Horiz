package com.example.horiz.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.horiz.activities.QrActivity
import com.example.horiz.activities.ScannerActivity
import com.example.horiz.componets.DeleteScheduleDialog
import com.example.horiz.componets.NameScheduleDialog
import com.example.horiz.storage.ScheduleStorage
import com.example.horiz.model.Schedule
import compose.icons.FeatherIcons
import compose.icons.feathericons.Camera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerScreen(
    onBack: () -> Unit,
    storage: ScheduleStorage,
    refreshFlag: Boolean,
    launchScanner: (Intent) -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var schedules by remember { mutableStateOf(listOf<String>()) }
    var king by remember { mutableStateOf<String?>(null) }

    var showCreate by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }

    // Refresca cada vez que refreshFlag cambia (cuando vuelve del scanner)
    LaunchedEffect(refreshFlag) {
        schedules = storage.getSchedules()
        king = storage.getKing()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            launchScanner(Intent(context, ScannerActivity::class.java))
                        }
                    ) {
                        Icon(FeatherIcons.Camera, contentDescription = "Escanear QR")
                    }
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Default.Add, "Agregar")
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(schedules) { name ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (king == name) Color(0xFF2E7D32) else Color(0xFF1E1E1E)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(name.uppercase(), color = Color.White)
                            if (king == name) Text("Horario activo", color = Color(0xFFB9F6CA))
                        }

                        Row {
                            IconButton(onClick = {
                                storage.setKing(name)
                                king = name
                            }) {
                                Icon(Icons.Default.Star, null, tint = if (king == name) Color.Yellow else Color.Gray)
                            }

                            IconButton(onClick = { showRename = name }) {
                                Icon(Icons.Default.Edit, null, tint = Color.White)
                            }

                            IconButton(
                                onClick = {
                                    val intent = Intent(context, QrActivity::class.java)
                                    intent.putExtra("schedule_name", name)
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            }

                            IconButton(onClick = { deleteTarget = name }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------- CREATE ----------
    if (showCreate) {
        NameScheduleDialog(
            title = "Nuevo horario",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                scope.launch(Dispatchers.IO) {
                    storage.createSchedule(Schedule(name, true))
                    schedules = storage.getSchedules()
                    king = storage.getKing()
                }
                showCreate = false
            }
        )
    }

    // ---------- RENAME ----------
    showRename?.let { oldName ->
        NameScheduleDialog(
            title = "Renombrar horario",
            initial = oldName,
            existingNames = schedules,
            onDismiss = { showRename = null },
            onConfirm = { newName ->
                scope.launch(Dispatchers.IO) {
                    val sch = storage.getSchedule(oldName) ?: return@launch
                    storage.deleteSchedule(oldName)
                    storage.createSchedule(sch.copy(n = newName))
                    schedules = storage.getSchedules()
                    if (storage.getKing() == oldName) {
                        storage.setKing(newName)
                    }
                    king = storage.getKing()
                }
                showRename = null
            }
        )
    }

    // ---------- DELETE ----------
    deleteTarget?.let { name ->
        DeleteScheduleDialog(
            scheduleName = name,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                storage.deleteSchedule(name)
                schedules = storage.getSchedules()
                king = storage.getKing()
                deleteTarget = null
            }
        )
    }
}