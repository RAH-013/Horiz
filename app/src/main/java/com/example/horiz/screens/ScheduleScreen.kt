package com.example.horiz.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.horiz.componets.AddDialogComponent
import com.example.horiz.componets.SubjectList
import com.example.horiz.model.SubjectNode
import com.example.horiz.model.Schedule
import com.example.horiz.model.DayNode
import com.example.horiz.storage.ScheduleStorage
import java.util.Calendar
import com.example.horiz.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {

    val ctx = LocalContext.current
    val storage = remember { ScheduleStorage(ctx) }

    val daysFull = listOf("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo")
    val daysShort = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")

    var kingName by remember { mutableStateOf(storage.getKing()) }
    var schedule by remember { mutableStateOf<Schedule?>(storage.getSchedule(kingName ?: "")) }

    val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    var selectedDay by remember { mutableStateOf(todayIndex) }

    val dayNode = schedule?.e?.getOrNull(selectedDay)

    var addSubject by remember { mutableStateOf(false) }
    var editSubject by remember { mutableStateOf<SubjectNode?>(null) }
    var deleteSubject by remember { mutableStateOf<SubjectNode?>(null) }

    Scaffold(
        topBar = {
            CrudTopBar(
                title = dayNode?.let { "${daysFull[selectedDay]} (${it.e.size})" } ?: daysFull[selectedDay],
                enabled = schedule?.e?.get(selectedDay)?.a ?: true,
                onBack = onBack,
                onToggle = {
                    schedule?.e?.get(selectedDay)?.toggleStatusDay()
                    schedule?.let { storage.createSchedule(it) }
                }
            )
        },
        bottomBar = {
            DaySelector(
                days = daysShort,
                selected = selectedDay,
                onSelect = { selectedDay = it }
            )
        },
        floatingActionButton = {
            val dayNode = schedule?.e?.getOrNull(selectedDay)
            AddFab(dayNode = dayNode, enabled = dayNode?.a ?: false) {
                addSubject = true
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val subjects = dayNode?.e ?: emptyList()

            if (subjects.isEmpty() || dayNode?.a == false) {
                EmptyDayView(enabled = dayNode?.a ?: false)
            } else {
                SubjectList(
                    items = subjects,
                    onEdit = { editSubject = it },
                    onDelete = { deleteSubject = it }
                )
            }
        }
    }

    // Agregar materia
    if (addSubject && schedule != null) {
        AddDialogComponent(
            dayIndex = selectedDay,
            subject = null,
            schedule = schedule!!,
            storage = storage,
            close = { addSubject = false }
        )
    }

    // Editar materia
    editSubject?.let { subj ->
        if (schedule != null) {
            AddDialogComponent(
                dayIndex = selectedDay,
                subject = subj,
                schedule = schedule!!,
                storage = storage,
                close = { editSubject = null }
            )
        }
    }

    // Eliminar materia
    deleteSubject?.let { subj ->
        DeleteDialog(
            item = subj,
            onConfirm = {
                schedule?.e?.get(selectedDay)?.removeElement(subj)
                schedule?.let { storage.createSchedule(it) }
                deleteSubject = null
            },
            onDismiss = { deleteSubject = null }
        )
    }
}
