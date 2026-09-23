package com.horiz.ui.screens.tasks.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.horiz.data.model.TaskNode
import com.horiz.data.model.TaskPriority
import com.horiz.widget.updateHorizWidgets
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

private fun getInitialDueDateTime(): LocalDateTime {
    val minimum = LocalDateTime.now().plusHours(1)

    return when {
        minimum.minute == 0 &&
                minimum.second == 0 &&
                minimum.nano == 0 -> minimum

        minimum.minute < 30 ->
            minimum.withMinute(30).withSecond(0).withNano(0)

        else ->
            minimum
                .plusHours(1)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    task: TaskNode? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueAt: Long?, priority: TaskPriority) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val minimumDueDateTime = remember {
        LocalDateTime.now().plusHours(1)
    }

    val initialDateTime = remember(task) {
        task?.dueAt ?: getInitialDueDateTime()
    }

    var title by remember(task) {
        mutableStateOf(task?.title.orEmpty())
    }

    var description by remember(task) {
        mutableStateOf(task?.description.orEmpty())
    }

    var priority by remember(task) {
        mutableStateOf(task?.priority ?: TaskPriority.LOW)
    }

    var hasDueDate by remember(task) {
        mutableStateOf(task?.dueAt != null)
    }

    var dueDate by remember(task) {
        mutableStateOf(initialDateTime.toLocalDate())
    }

    var dueTime by remember(task) {
        mutableStateOf(initialDateTime.toLocalTime())
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    var showTimePicker by remember {
        mutableStateOf(false)
    }

    val selectedDateTime = LocalDateTime.of(
        dueDate,
        dueTime
    )

    val titleIsValid = title.trim().isNotEmpty()

    val dateTimeIsValid =
        !hasDueDate ||
                !selectedDateTime.isBefore(minimumDueDateTime)

    val canSave =
        titleIsValid &&
                dateTimeIsValid

    fun resetInvalidDateTime() {
        val minimum = LocalDateTime.now().plusHours(1)

        if (selectedDateTime.isBefore(minimum)) {
            val corrected = getInitialDueDateTime()

            dueDate = corrected.toLocalDate()
            dueTime = corrected.toLocalTime()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (task == null) {
                    Icons.Outlined.AddTask
                } else {
                    Icons.Outlined.Edit
                },
                contentDescription = null
            )
        },
        title = {
            Text(
                text = if (task == null) {
                    "Nueva tarea"
                } else {
                    "Editar tarea"
                }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Título")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Title,
                            contentDescription = null
                        )
                    },
                    isError = title.isNotEmpty() && !titleIsValid
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    label = {
                        Text("Descripción")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null
                        )
                    }
                )

                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.entries.forEach { item ->
                        val isSelected = priority == item
                        val (label, color) = when (item) {
                            TaskPriority.LOW -> "Baja" to Color(0xFF4CAF50)
                            TaskPriority.MEDIUM -> "Media" to Color(0xFFFFC107)
                            TaskPriority.HIGH -> "Alta" to Color(0xFFF44336)
                        }

                        Surface(
                            onClick = { priority = item },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = if (isSelected) BorderStroke(2.dp, color) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = hasDueDate,
                            role = Role.Switch,
                            onValueChange = {
                                hasDueDate = it

                                if (it) {
                                    resetInvalidDateTime()
                                }
                            }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(
                            modifier = Modifier.width(12.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Fecha límite",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Switch(
                            checked = hasDueDate,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors()
                        )
                    }
                }

                AnimatedVisibility(
                    visible = hasDueDate,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateTimeSelector(
                            modifier = Modifier.weight(1f),
                            type = DateTimeSelectorType.DATE,
                            value = dueDate.toString(),
                            onClick = {
                                showDatePicker = true
                            }
                        )

                        DateTimeSelector(
                            modifier = Modifier.weight(1f),
                            type = DateTimeSelectorType.TIME,
                            value = String.format(
                                "%02d:%02d",
                                dueTime.hour,
                                dueTime.minute
                            ),
                            onClick = {
                                showTimePicker = true
                            }
                        )
                    }
                }

                if (hasDueDate && !dateTimeIsValid) {
                    Text(
                        text = "La fecha y hora deben ser al menos 1 hora después de ahora.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    val dueAt = if (hasDueDate) {
                        selectedDateTime
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    } else {
                        null
                    }

                    onSave(
                        title.trim(),
                        description.trim(),
                        dueAt,
                        priority
                    )

                    coroutineScope.launch {
                        updateHorizWidgets(context)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text("Guardar")
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant
                        .ofEpochMilli(utcTimeMillis)
                        .atZone(ZoneOffset.UTC)
                        .toLocalDate()

                    return !date.isBefore(
                        LocalDate.now()
                    )
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDate = Instant
                                .ofEpochMilli(it)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                        }

                        resetInvalidDateTime()
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }

    if (showTimePicker) {
        TaskTimePickerDialog(
            initialTime = dueTime,
            minimumDateTime = LocalDateTime.now().plusHours(1),
            selectedDate = dueDate,
            onDismissRequest = {
                showTimePicker = false
            },
            onConfirm = {
                dueTime = it
                showTimePicker = false
            }
        )
    }
}

private enum class DateTimeSelectorType {
    DATE,
    TIME
}

@Composable
private fun DateTimeSelector(
    modifier: Modifier,
    type: DateTimeSelectorType,
    value: String,
    onClick: () -> Unit
) {
    val icon = when (type) {
        DateTimeSelectorType.DATE ->
            Icons.Outlined.CalendarMonth

        DateTimeSelectorType.TIME ->
            Icons.Outlined.AccessTime
    }

    val label = when (type) {
        DateTimeSelectorType.DATE -> "Fecha"
        DateTimeSelectorType.TIME -> "Hora"
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTimePickerDialog(
    initialTime: LocalTime,
    minimumDateTime: LocalDateTime,
    selectedDate: LocalDate,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    val selectedDateTime = LocalDateTime.of(
        selectedDate,
        LocalTime.of(timePickerState.hour, timePickerState.minute)
    )

    val isValid = !selectedDateTime.isBefore(minimumDateTime)

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar hora",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                TimePicker(state = timePickerState)

                if (!isValid) {
                    Text(
                        text = "Selecciona una hora al menos 1 hora después de ahora.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        enabled = isValid,
                        onClick = {
                            onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}