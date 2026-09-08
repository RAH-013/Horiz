package com.horiz.ui.screens.subjects.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FreeBreakfast
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import com.horiz.storage.ScheduleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDialog(
    dayIndex: Int,
    entry: ScheduleEntry?,
    schedule: Schedule,
    storage: ScheduleStorage,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val dayNode = schedule.days.getOrNull(dayIndex)

    if (dayNode == null) {
        onClose()
        return
    }

    val currentSubject = entry?.subjectId?.let { schedule.findSubject(it) }
    val currentTeacher = entry?.teacherId?.let { schedule.findTeacher(it) }
    val currentLocation = entry?.locationId?.let { schedule.findLocation(it) }

    val lastEnd = remember(dayIndex, entry?.id) {
        entry?.endMinute
            ?: (dayNode.entries.maxOfOrNull { it.endMinute } ?: (8 * 60))
    }

    val initialStart = remember(dayIndex, entry?.id) {
        entry?.startMinute ?: lastEnd
    }

    val initialEnd = remember(dayIndex, entry?.id) {
        entry?.endMinute
            ?: (initialStart + 120).coerceAtMost(1440)
    }

    var name by remember(entry?.id) {
        mutableStateOf(currentSubject?.name ?: "")
    }

    var breakName by remember(entry?.id) {
        mutableStateOf(
            if (entry?.type == SubjectType.BREAK) {
                entry.name ?: ""
            } else {
                ""
            }
        )
    }

    var teacher by remember(entry?.id) {
        mutableStateOf(currentTeacher?.name ?: "")
    }

    var room by remember(entry?.id) {
        mutableStateOf(currentLocation?.name ?: "")
    }

    var startTime by remember(entry?.id) {
        mutableStateOf(formatMinute(initialStart.coerceIn(0, 1439)))
    }

    var endTime by remember(entry?.id) {
        mutableStateOf(formatMinute(initialEnd.coerceIn(1, 1440)))
    }

    var color by remember(entry?.id) {
        mutableStateOf(
            currentSubject?.color
                ?: entry?.color
                ?: randColor()
        )
    }

    var type by remember(entry?.id) {
        mutableStateOf(entry?.type ?: SubjectType.CLASS)
    }

    var showColorPicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (initialStart >= 1440) {
            startTime = "23:59"
        }

        if (initialEnd <= 0) {
            endTime = "00:01"
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (entry == null) Icons.Rounded.Book else Icons.Rounded.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (entry == null) "Agregar Horario" else "Editar Horario",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tipo de Bloque",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = type == SubjectType.CLASS,
                            onClick = {
                                type = SubjectType.CLASS
                                error = null
                            },
                            label = { Text("Materia") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )

                        FilterChip(
                            modifier = Modifier.weight(1f),
                            selected = type == SubjectType.BREAK,
                            onClick = {
                                type = SubjectType.BREAK
                                error = null
                            },
                            label = { Text("Hora Libre") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Coffee,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }

                if (type == SubjectType.CLASS) {
                    ModernEntityField(
                        label = "Materia",
                        value = name,
                        icon = Icons.Rounded.Book,
                        options = schedule.subjects.map { it.name },
                        onValueChange = {
                            name = it
                            error = null
                        },
                        onSelect = { selectedName ->
                            name = selectedName
                            schedule.subjects
                                .firstOrNull { it.name.equals(selectedName, ignoreCase = true) }
                                ?.let { subject -> color = subject.color }
                        }
                    )

                    ModernEntityField(
                        label = "Profesor",
                        value = teacher,
                        icon = Icons.Rounded.Person,
                        options = schedule.teachers.map { it.name },
                        onValueChange = {
                            teacher = it
                            error = null
                        },
                        onSelect = { teacher = it }
                    )

                    ModernEntityField(
                        label = "Aula",
                        value = room,
                        icon = Icons.Rounded.LocationOn,
                        options = schedule.locations.map { it.name },
                        onValueChange = {
                            room = it
                            error = null
                        },
                        onSelect = { room = it }
                    )
                } else {
                    OutlinedTextField(
                        value = breakName,
                        onValueChange = {
                            breakName = it
                            error = null
                        },
                        label = { Text("Nombre del descanso") },
                        placeholder = { Text("Recreo") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.FreeBreakfast,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Selector de Horas con TimePicker Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Inicio") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    showStartTimePicker = true
                                    error = null
                                }
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("Fin") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable {
                                    showEndTimePicker = true
                                    error = null
                                }
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showColorPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null
                        )

                        Text(
                            text = if (type == SubjectType.CLASS) "Color de la materia" else "Color de hora libre",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        )
                    }
                }

                error?.let { errorMessage ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedName = name.trim()
                    val trimmedTeacher = teacher.trim()
                    val trimmedRoom = room.trim()
                    val trimmedBreakName = breakName.trim().ifBlank { "Recreo" }

                    if (type == SubjectType.CLASS) {
                        if (trimmedName.isBlank()) {
                            error = "El nombre es obligatorio"
                            return@TextButton
                        }

                        if (!isValidSubjectText(trimmedName)) {
                            Toast.makeText(
                                context,
                                "No uses espacios múltiples o bordes",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }

                        if (trimmedTeacher.isBlank() || trimmedRoom.isBlank()) {
                            error = "Profesor y aula son obligatorios"
                            return@TextButton
                        }

                        if (!isValidSubjectText(trimmedTeacher) || !isValidSubjectText(trimmedRoom)) {
                            Toast.makeText(
                                context,
                                "Verifica el formato del texto ingresado",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }
                    } else {
                        if (!isValidSubjectText(trimmedBreakName)) {
                            Toast.makeText(
                                context,
                                "Verifica el formato del texto ingresado",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@TextButton
                        }
                    }

                    val start = parseTime(startTime)
                    val end = parseTime(endTime)

                    if (start == null || end == null) {
                        Toast.makeText(
                            context,
                            "Hora inválida",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TextButton
                    }

                    if (start >= end) {
                        Toast.makeText(
                            context,
                            "Rango de hora inválido",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TextButton
                    }

                    if (dayNode.hasConflict(startMinute = start, endMinute = end, ignoredEntryId = entry?.id)) {
                        Toast.makeText(
                            context,
                            "Existe un conflicto de horario",
                            Toast.LENGTH_SHORT
                        ).show()

                        error = "El horario se cruza con otra materia"
                        return@TextButton
                    }

                    val formattedName = if (type == SubjectType.CLASS) formatWords(trimmedName) else formatWords(trimmedBreakName)
                    val formattedTeacher = if (type == SubjectType.CLASS) formatWords(trimmedTeacher, capitalizeAll = true) else ""
                    val formattedRoom = if (type == SubjectType.CLASS) formatWords(trimmedRoom, capitalizeAll = true) else ""

                    val subjectEntity = if (type == SubjectType.CLASS) {
                        schedule.findOrCreateSubject(name = formattedName, color = color)
                    } else null

                    val teacherEntity = if (type == SubjectType.CLASS) {
                        schedule.findOrCreateTeacher(name = formattedTeacher)
                    } else null

                    val locationEntity = if (type == SubjectType.CLASS) {
                        schedule.findOrCreateLocation(name = formattedRoom)
                    } else null

                    val newEntry = ScheduleEntry(
                        id = entry?.id ?: System.nanoTime(),
                        subjectId = subjectEntity?.id,
                        teacherId = teacherEntity?.id,
                        locationId = locationEntity?.id,
                        startMinute = start,
                        endMinute = end,
                        dayIndex = dayIndex,
                        color = color,
                        type = type,
                        name = if (type == SubjectType.BREAK) formattedName else null
                    )

                    val success = if (entry == null) {
                        dayNode.addEntry(newEntry)
                    } else {
                        dayNode.replaceEntry(newEntry)
                    }

                    if (!success) {
                        Toast.makeText(
                            context,
                            "No se pudo guardar",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@TextButton
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        storage.createSchedule(schedule)
                        onClose()
                    }
                }
            ) {
                Text(
                    text = if (entry == null) "Agregar" else "Guardar",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancelar")
            }
        }
    )

    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime,
            onTimeSelected = { selectedTime ->
                startTime = selectedTime
                showStartTimePicker = false
            },
            onDismiss = { showStartTimePicker = false }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime,
            onTimeSelected = { selectedTime ->
                endTime = selectedTime
                showEndTimePicker = false
            },
            onDismiss = { showEndTimePicker = false }
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initial = color,
            onColorSelected = {
                color = it
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMinutes = parseTime(initialTime) ?: (8 * 60)
    val initialHour = initialMinutes / 60
    val initialMinute = initialMinutes % 60

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val formattedTime = String.format(
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(formattedTime)
                }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernEntityField(
    label: String,
    value: String,
    icon: ImageVector,
    options: List<String>,
    onValueChange: (String) -> Unit,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filteredOptions = options.filter { option ->
        option.contains(value, ignoreCase = true)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && filteredOptions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (options.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions
                    .take(5)
                    .forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
            }
        }
    }
}

private fun parseTime(value: String): Int? {
    val parts = value.split(":")

    if (parts.size != 2) return null

    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null

    if (hour !in 0..23 || minute !in 0..59) return null

    return hour * 60 + minute
}