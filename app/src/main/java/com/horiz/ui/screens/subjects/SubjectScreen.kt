package com.horiz.ui.screens.subjects

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.subjects.components.DaySelector
import com.horiz.ui.screens.subjects.components.DayStatusButton
import com.horiz.ui.screens.subjects.components.DeleteSubjectDialog
import com.horiz.ui.screens.subjects.components.EmptyDayView
import com.horiz.ui.screens.subjects.components.SubjectDialog
import com.horiz.ui.screens.subjects.components.SubjectList
import com.horiz.ui.screens.tasks.TaskScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun SubjectScreen(
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val storage = remember { ScheduleStorage(context) }

    val daysFull = remember {
        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    }

    val daysShort = remember {
        listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    }

    val todayIndex = remember {
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    // Usamos el estado nativo del pager como FUENTE ÚNICA DE VERDAD para el día
    val pagerState = rememberPagerState(
        initialPage = todayIndex,
        pageCount = { daysFull.size }
    )

    // Usamos derivedStateOf para no provocar recomposiciones innecesarias al arrastrar
    val selectedDay by remember {
        derivedStateOf { pagerState.currentPage }
    }

    var schedule by remember { mutableStateOf<Schedule?>(null) }
    var editingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var taskEntry by remember { mutableStateOf<ScheduleEntry?>(null) }
    var showSubjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        schedule = withContext(Dispatchers.IO) {
            storage.getKing()?.let { king -> storage.getSchedule(king) }
        }
    }

    fun reloadSchedule() {
        scope.launch {
            schedule = withContext(Dispatchers.IO) {
                storage.getKing()?.let { king -> storage.getSchedule(king) }
            }
        }
    }

    val currentSchedule = schedule
    val currentDay = remember(currentSchedule, selectedDay) {
        currentSchedule?.days?.getOrNull(selectedDay)
    }
    val entries = remember(currentDay) {
        currentDay?.entries ?: emptyList()
    }

    AppScreen(
        title = if (currentDay != null) {
            "${daysFull[selectedDay]} (${entries.size})"
        } else {
            "Materias"
        },
        onBackClick = onBackClick,
        actions = {
            currentDay?.let { day ->
                DayStatusButton(
                    enabled = day.enabled,
                    onClick = {
                        currentSchedule?.let { current ->
                            day.toggleStatus()
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    storage.createSchedule(current)
                                }
                                reloadSchedule()
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pagerState,
                    key = { pageIndex -> pageIndex }, // Clave única para evitar destrucción/recreación innecesaria de vistas
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    // Calculamos los datos del día correspondiente a CADA PÁGINA de forma aislada
                    val dayForPage = remember(currentSchedule, pageIndex) {
                        currentSchedule?.days?.getOrNull(pageIndex)
                    }
                    val entriesForPage = remember(dayForPage) {
                        dayForPage?.entries ?: emptyList()
                    }

                    when {
                        dayForPage == null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay horario activo")
                            }
                        }

                        entriesForPage.isEmpty() -> {
                            EmptyDayView(enabled = dayForPage.enabled)
                        }

                        else -> {
                            SubjectList(
                                items = entriesForPage,
                                schedule = currentSchedule!!,
                                onEdit = {
                                    editingEntry = it
                                    showSubjectDialog = true
                                },
                                onDelete = {
                                    deletingEntry = it
                                },
                                onClick = {
                                    taskEntry = it
                                }
                            )
                        }
                    }
                }

                if (currentDay?.enabled == true) {
                    FloatingActionButton(
                        onClick = {
                            if (entries.size >= 10) {
                                Toast.makeText(
                                    context,
                                    "Se ha alcanzado la capacidad máxima de 10 materias",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                editingEntry = null
                                showSubjectDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar materia"
                        )
                    }
                }
            }

            DaySelector(
                days = daysShort,
                selected = selectedDay,
                onSelect = { day ->
                    // Animamos directamente el pager sin usar variables intermedias
                    scope.launch {
                        pagerState.animateScrollToPage(day)
                    }
                }
            )
        }
    }

    currentSchedule?.let { nonNullSchedule ->
        if (showSubjectDialog) {
            SubjectDialog(
                dayIndex = selectedDay,
                entry = editingEntry,
                schedule = nonNullSchedule,
                storage = storage,
                onClose = {
                    showSubjectDialog = false
                    editingEntry = null
                    reloadSchedule()
                }
            )
        }

        deletingEntry?.let { entry ->
            DeleteSubjectDialog(
                entry = entry,
                schedule = nonNullSchedule,
                onConfirm = {
                    nonNullSchedule.removeEntry(entry.id)
                    deletingEntry = null
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            storage.createSchedule(nonNullSchedule)
                        }
                        reloadSchedule()
                    }
                },
                onDismiss = {
                    deletingEntry = null
                }
            )
        }

        taskEntry?.let { entry ->
            TaskScreen(
                schedule = nonNullSchedule,
                scheduleEntryId = entry.id,
                storage = storage,
                onDismiss = {
                    taskEntry = null
                    reloadSchedule()
                }
            )
        }
    }
}