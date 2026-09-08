package com.horiz.ui.screens.schedule

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.horiz.data.model.Schedule
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.schedule.components.DeleteScheduleDialog
import com.horiz.ui.screens.schedule.components.NameScheduleDialog
import com.horiz.ui.screens.schedule.components.ScheduleList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScheduleScreen(
    storage: ScheduleStorage,
    refreshFlag: Boolean,
    onBackClick: () -> Unit,
    onScannerClick: () -> Unit,
    onQrClick: (String) -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    val scope = rememberCoroutineScope()

    var schedules by remember {
        mutableStateOf(emptyList<String>())
    }

    var king by remember {
        mutableStateOf<String?>(null)
    }

    var showCreate by remember {
        mutableStateOf(false)
    }

    var showRename by remember {
        mutableStateOf<String?>(null)
    }

    var deleteTarget by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(refreshFlag) {
        val result = withContext(Dispatchers.IO) {
            storage.getSchedules() to storage.getKing()
        }

        schedules = result.first
        king = result.second
    }

    fun reloadSchedules() {
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                storage.getSchedules() to storage.getKing()
            }

            schedules = result.first
            king = result.second
        }
    }

    AppScreen(
        title = "Horarios",
        onBackClick = onBackClick,
        actions = {
            IconButton(
                onClick = onScannerClick
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Escanear QR"
                )
            }

            IconButton(
                onClick = {
                    showCreate = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar horario"
                )
            }
        }
    ) { paddingValues ->

        ScheduleList(
            schedules = schedules,
            king = king,
            contentPadding = paddingValues,
            onOpen = { name ->
                scope.launch {
                    val schedule =
                        withContext(Dispatchers.IO) {
                            storage.getSchedule(name)
                        }

                    schedule?.let {
                        onScheduleClick(it)
                    }
                }
            },
            onActivate = { name ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        storage.setKing(name)
                    }

                    king = name
                }
            },
            onEdit = { name ->
                showRename = name
            },
            onShare = { name ->
                onQrClick(name)
            },
            onDelete = { name ->
                deleteTarget = name
            }
        )
    }

    if (showCreate) {
        NameScheduleDialog(
            title = "Nuevo horario",
            existingNames = schedules,
            onDismiss = {
                showCreate = false
            },
            onConfirm = { name ->
                showCreate = false

                scope.launch {
                    withContext(Dispatchers.IO) {
                        storage.createSchedule(
                            Schedule(
                                name = name,
                                enabled = true
                            )
                        )
                    }

                    reloadSchedules()
                }
            }
        )
    }

    showRename?.let { oldName ->
        NameScheduleDialog(
            title = "Renombrar horario",
            initial = oldName,
            existingNames = schedules,
            onDismiss = {
                showRename = null
            },
            onConfirm = { newName ->
                showRename = null

                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        val schedule =
                            storage.getSchedule(oldName)
                                ?: return@withContext null

                        val wasKing =
                            storage.getKing() == oldName

                        storage.deleteSchedule(oldName)

                        storage.createSchedule(
                            Schedule(
                                name = newName,
                                enabled = schedule.enabled,
                                subjects = schedule.subjects,
                                teachers = schedule.teachers,
                                locations = schedule.locations,
                                days = schedule.days
                            )
                        )

                        if (wasKing) {
                            storage.setKing(newName)
                        }

                        storage.getSchedules() to storage.getKing()
                    }

                    result?.let {
                        schedules = it.first
                        king = it.second
                    }
                }
            }
        )
    }

    deleteTarget?.let { name ->
        DeleteScheduleDialog(
            scheduleName = name,
            onDismiss = {
                deleteTarget = null
            },
            onConfirm = {
                deleteTarget = null

                scope.launch {
                    withContext(Dispatchers.IO) {
                        storage.deleteSchedule(name)
                    }

                    reloadSchedules()
                }
            }
        )
    }
}