package com.horiz.ui.screens.subjects.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun SubjectCard(
    item: ScheduleEntry,
    schedule: Schedule,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val cardShape = FoldedCornerShape(
        cornerRadius = 24.dp,
        foldSize = 26.dp
    )

    val subject = item.subjectId?.let {
        schedule.findSubject(it)
    }

    val teacher = item.teacherId?.let {
        schedule.findTeacher(it)
    }

    val location = item.locationId?.let {
        schedule.findLocation(it)
    }

    val taskCount =
        if (item.type == SubjectType.CLASS) {
            schedule.findTasksForEntry(item.id).size
        } else {
            0
        }

    val base =
        if (item.type == SubjectType.CLASS) {
            Color(subject?.color ?: item.color)
        } else {
            Color(item.color)
        }

    val colorBlur = Color(
        red = (base.red * 0.5f).coerceIn(0f, 1f),
        green = (base.green * 0.5f).coerceIn(0f, 1f),
        blue = (base.blue * 0.5f).coerceIn(0f, 1f),
        alpha = base.alpha
    )

    val contentColor =
        MaterialTheme.colorScheme.onSurfaceVariant

    // Estado reactivo para actualizar la hora actual dinámicamente cada minuto
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10_000) // Reevaluar cada 10 segundos
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
        }
    }

    val currentMinute = currentTime.hour * 60 + currentTime.minute

    // Obtener el día actual de la semana en formato 0..6 (donde Lunes = 0 o según el estándar de tu modelo)
    // java.time.DayOfWeek da Lunes = 1 .. Domingo = 7.
    // Ajustamos a base 0 (Lunes = 0) o 1 según cómo esté configurado tu `item.dayIndex`
    val todayIndex = currentDate.dayOfWeek.value - 1 // Suponiendo Lunes = 0

    // Condición estricta: debe coincidir el día real con el día de la clase Y estar dentro del rango de tiempo
    val isClassActive = item.dayIndex == todayIndex && item.isNow(
        currentDayIndex = todayIndex,
        currentMinute = currentMinute
    )

    val subjectName =
        if (item.type == SubjectType.BREAK) {
            item.name ?: "RECREO"
        } else {
            subject?.name ?: "MATERIA"
        }

    val teacherName =
        teacher?.name ?: ""

    val locationName =
        location?.name ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
            .then(
                if (item.type == SubjectType.CLASS) {
                    Modifier.clickable {
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .border(
                width = 1.5.dp,
                color = contentColor.copy(alpha = 0.7f),
                shape = cardShape
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = cardShape
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .background(
                        colorBlur.copy(alpha = 0.6f)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(cardShape)
                    .background(
                        contentColor.copy(alpha = 0.07f)
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                        .drawBehind {
                            drawRect(
                                color = base
                            )

                            drawLine(
                                color = colorBlur,
                                start = Offset(
                                    size.width,
                                    0f
                                ),
                                end = Offset(
                                    size.width,
                                    size.height
                                ),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // Fila superior: Punto de estado, Hora e Indicador de tareas al extremo derecho
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .drawBehind {
                                val strokeWidth =
                                    1.dp.toPx()

                                val yOffset =
                                    5.dp.toPx()

                                drawLine(
                                    color =
                                        contentColor.copy(
                                            alpha = 0.4f
                                        ),
                                    start = Offset(
                                        0f,
                                        size.height +
                                                yOffset
                                    ),
                                    end = Offset(
                                        size.width,
                                        size.height +
                                                yOffset
                                    ),
                                    strokeWidth =
                                        strokeWidth
                                )
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (isClassActive) {
                                        Color(0xFF3CFF6B) // Verde sólo si es HOY y en la HORA
                                    } else {
                                        Color(0xFFFF3C3C) // Rojo de lo contrario
                                    },
                                    CircleShape
                                )
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text =
                                "${formatMinute(item.startMinute)} - " +
                                        formatMinute(item.endMinute),
                            color =
                                contentColor.copy(
                                    alpha = 0.9f
                                ),
                            fontSize = 16.sp
                        )

                        // Indicador de tareas
                        if (
                            item.type ==
                            SubjectType.CLASS &&
                            taskCount > 0
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            TaskCountIndicator(
                                count = taskCount,
                                color = contentColor
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text =
                                    subjectName.uppercase(),
                                color = contentColor,
                                fontSize = 24.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            if (
                                item.type ==
                                SubjectType.CLASS
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(
                                    text = teacherName,
                                    color =
                                        contentColor.copy(
                                            alpha = 0.85f
                                        ),
                                    fontSize = 18.sp
                                )

                                Text(
                                    text = locationName,
                                    color =
                                        contentColor.copy(
                                            alpha = 0.75f
                                        ),
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Column(
                            horizontalAlignment =
                                Alignment.End,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            CircleActionButton(
                                icon = Icons.Default.Edit,
                                bg =
                                    Color.Black.copy(
                                        alpha = 0.3f
                                    ),
                                bc = contentColor,
                                contentDescription =
                                    "Editar",
                                onClick = onEdit
                            )

                            CircleActionButton(
                                icon =
                                    Icons.Default.Delete,
                                bg =
                                    Color.Red.copy(
                                        alpha = 0.3f
                                    ),
                                bc = contentColor,
                                contentDescription =
                                    "Eliminar",
                                onClick = onDelete
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        Color.Black.copy(alpha = 0.12f)
                    )
            )
        }
    }
}

@Composable
private fun TaskCountIndicator(
    count: Int,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(
                color.copy(alpha = 0.16f)
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.45f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 3.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count == 1) {
                "1 tarea"
            } else {
                "$count tareas"
            },
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CircleActionButton(
    icon: ImageVector,
    bg: Color,
    bc: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = bc,
                shape = CircleShape
            )
            .background(bg)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = bc,
            modifier = Modifier.size(20.dp)
        )
    }
}

private class FoldedCornerShape(
    private val cornerRadius: Dp,
    private val foldSize: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = with(density) {
            cornerRadius.toPx()
        }

        val fold = with(density) {
            foldSize.toPx()
        }

        val path = Path().apply {
            moveTo(radius, 0f)

            lineTo(
                size.width - fold,
                0f
            )

            lineTo(
                size.width,
                fold
            )

            lineTo(
                size.width,
                size.height - radius
            )

            quadraticBezierTo(
                size.width,
                size.height,
                size.width - radius,
                size.height
            )

            lineTo(
                radius,
                size.height
            )

            quadraticBezierTo(
                0f,
                size.height,
                0f,
                size.height - radius
            )

            lineTo(
                0f,
                radius
            )

            quadraticBezierTo(
                0f,
                0f,
                radius,
                0f
            )

            close()
        }

        return Outline.Generic(path)
    }
}