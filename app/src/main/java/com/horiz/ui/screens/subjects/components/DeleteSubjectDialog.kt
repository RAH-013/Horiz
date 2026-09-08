package com.horiz.ui.screens.subjects.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun DeleteSubjectDialog(
    entry: ScheduleEntry,
    schedule: Schedule,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBreak = entry.type == SubjectType.BREAK

    val name = if (isBreak) {
        entry.name
            ?.takeIf { it.isNotBlank() }
            ?: "Recreo"
    } else {
        entry.subjectId
            ?.let { schedule.findSubject(it)?.name }
            ?.takeIf { it.isNotBlank() }
            ?: "Materia"
    }

    val title = if (isBreak) {
        "Eliminar descanso"
    } else {
        "Eliminar materia"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title)
        },
        text = {
            Text(
                "¿Deseas eliminar \"$name\"?"
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}