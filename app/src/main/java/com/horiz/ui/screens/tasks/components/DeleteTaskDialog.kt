package com.horiz.ui.screens.tasks.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.horiz.data.model.TaskNode

@Composable
fun DeleteTaskDialog(
    task: TaskNode,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Eliminar tarea")
        },
        text = {
            Text(
                "¿Quieres eliminar la tarea \"${task.title}\"?"
            )
        },
        confirmButton = {
            Button(
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