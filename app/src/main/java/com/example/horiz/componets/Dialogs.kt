package com.example.horiz.componets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun NameScheduleDialog(
    title: String,
    existingNames: List<String> = emptyList(),
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue(initial)) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = text.text.trim()
                    when {
                        trimmed.isBlank() -> error = "El nombre no puede estar vacío"
                        trimmed in existingNames && trimmed != initial -> error = "Ya existe un horario con ese nombre"
                        else -> {
                            onConfirm(trimmed)
                            error = null
                        }
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        if (it.text.length <= 14) text = it
                        error = null
                    },
                    singleLine = true,
                    isError = error != null
                )
                if (error != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
fun DeleteScheduleDialog(
    scheduleName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar horario") },
        text = {
            Column {
                Text("Escribe el nombre del horario para confirmar:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    placeholder = { Text(scheduleName) },
                    onValueChange = { text = it },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = text == scheduleName,
                onClick = onConfirm
            ) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
