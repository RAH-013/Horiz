package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

private const val MAX_NAME_LENGTH = 14

@Composable
fun NameScheduleDialog(
    title: String,
    existingNames: List<String> = emptyList(),
    initial: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    val focusRequester = remember { FocusRequester() }

    val trimmedText = text.trim()

    val isDuplicate by remember(trimmedText, existingNames, initial) {
        derivedStateOf {
            existingNames.any { it.equals(trimmedText, ignoreCase = true) } &&
                    !trimmedText.equals(initial, ignoreCase = true)
        }
    }

    val isValid by remember(trimmedText, isDuplicate) {
        derivedStateOf {
            trimmedText.isNotBlank() && !isDuplicate
        }
    }

    // Enfocar automáticamente el campo de texto al abrir el diálogo
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val submitAction = {
        when {
            trimmedText.isBlank() -> {
                error = "El nombre no puede estar vacío"
            }
            isDuplicate -> {
                error = "Ya existe un horario con ese nombre"
            }
            else -> {
                onConfirm(trimmedText)
            }
        }
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { input ->
                        if (input.length <= MAX_NAME_LENGTH) {
                            text = input
                            error = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text("Nombre del horario") },
                    placeholder = { Text("Ej. Semestre 1") },
                    singleLine = true,
                    isError = error != null || isDuplicate,
                    trailingIcon = {
                        if (text.isNotEmpty()) {
                            IconButton(onClick = {
                                text = ""
                                error = null
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Limpiar texto"
                                )
                            }
                        }
                    },
                    supportingText = {
                        val activeError = when {
                            error != null -> error
                            isDuplicate -> "Ya existe un horario con ese nombre"
                            else -> null
                        }

                        if (activeError != null) {
                            Text(
                                text = activeError,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "${text.length}/$MAX_NAME_LENGTH",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) submitAction()
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = submitAction
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}