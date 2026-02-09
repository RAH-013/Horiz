package com.example.horiz.componets

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.horiz.model.DayNode
import com.example.horiz.model.Schedule
import com.example.horiz.model.SubjectNode
import com.example.horiz.storage.ScheduleStorage

fun formatWords(text: String, capitalizeAll: Boolean = false): String {
    val trimmed = text.trim().replace(Regex("\\s+"), " ")
    return if (capitalizeAll) {
        trimmed.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
    } else {
        trimmed.replaceFirstChar { it.uppercaseChar() }
    }
}

@Composable
fun AddDialogComponent(
    dayIndex: Int,
    subject: SubjectNode?,
    schedule: Schedule,
    storage: ScheduleStorage,
    close: () -> Unit
) {
    val ctx = LocalContext.current
    val dayNode = schedule.e[dayIndex]

    // Campos de texto
    var title by remember { mutableStateOf(subject?.n ?: "") }
    var teacher by remember { mutableStateOf(subject?.t ?: "") }
    var place by remember { mutableStateOf(subject?.p ?: "") }

    // Última hora del día, envuelta en 24h
    val lastEnd = dayNode.e.maxOfOrNull { it.f } ?: 8 * 60
    val startMinutes = lastEnd % (24 * 60)
    val endMinutes = (startMinutes + 120) % (24 * 60)

    var start by remember {
        mutableStateOf(
            subject?.s?.let { "%02d:%02d".format(it / 60, it % 60) }
                ?: "%02d:%02d".format(startMinutes / 60, startMinutes % 60)
        )
    }

    var end by remember {
        mutableStateOf(
            subject?.f?.let { "%02d:%02d".format(it / 60, it % 60) }
                ?: "%02d:%02d".format(endMinutes / 60, endMinutes % 60)
        )
    }

    var color by remember { mutableStateOf(subject?.c ?: randColor()) }
    var showColorPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialogComponent(
            initial = color,
            onColorSelected = {
                color = it
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    AlertDialog(
        onDismissRequest = close,
        title = { Text(if (subject == null) "Agregar Materia" else "Editar Materia") },
        text = {
            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 20) title = it },
                    label = { Text("Título") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = teacher,
                    onValueChange = { if (it.length <= 20) teacher = it },
                    label = { Text("Profesor") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = place,
                    onValueChange = { if (it.length <= 20) place = it },
                    label = { Text("Lugar") },
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                Row {
                    TimePickerField("Inicio", start, { start = it }, Modifier.weight(1f), error)
                    Spacer(Modifier.width(8.dp))
                    TimePickerField("Fin", end, { end = it }, Modifier.weight(1f), error)
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .clickable { showColorPicker = true }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val formattedTitle = formatWords(title)
                val formattedTeacher = formatWords(teacher, true)
                val formattedPlace = formatWords(place, true)

                // Validación de campos vacíos o solo espacios / dobles espacios
                val invalidRegex = Regex("^\\s*\$|.*\\s{2,}.*")
                if (formattedTitle.isEmpty() || invalidRegex.matches(formattedTitle)
                    || formattedTeacher.isEmpty() || invalidRegex.matches(formattedTeacher)
                    || formattedPlace.isEmpty() || invalidRegex.matches(formattedPlace)
                ) {
                    Toast.makeText(ctx, "Los campos no pueden estar vacíos ni tener espacios dobles", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                val sMin = start.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
                val fMin = end.split(":").let { it[0].toInt() * 60 + it[1].toInt() }

                if (sMin >= fMin) {
                    Toast.makeText(ctx, "Rango de hora inválido", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                if (dayNode.hasConflict(sMin, fMin, subject?.i)) {
                    error = true
                    Toast.makeText(ctx, "Conflicto de horario", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }

                val newNode = SubjectNode(
                    i = subject?.i ?: 0,
                    n = formattedTitle,
                    t = formattedTeacher,
                    p = formattedPlace,
                    s = sMin,
                    f = fMin,
                    c = color,
                    d = dayNode.i
                )

                if (subject == null) dayNode.addElement(newNode)
                else dayNode.replaceElement(newNode)

                storage.createSchedule(schedule)
                close()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = close) { Text("Cancelar") } }
    )
}

@Composable
fun TimePickerField(
    label: String,
    time: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean
) {
    val ctx = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = time,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.Edit, null) },
            singleLine = true,
            isError = isError,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    LaunchedEffect(showPicker) {
        if (showPicker) {
            val parts = time.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()

            val dialog = TimePickerDialog(
                ctx,
                { _, hour, minute ->
                    onTimeSelected("%02d:%02d".format(hour, minute))
                    showPicker = false
                },
                h,
                m,
                true
            )

            dialog.setOnCancelListener { showPicker = false }
            dialog.setOnDismissListener { showPicker = false }

            dialog.show()
        }
    }
}
