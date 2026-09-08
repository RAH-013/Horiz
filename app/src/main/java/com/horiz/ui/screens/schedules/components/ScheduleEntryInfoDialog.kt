package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun ScheduleEntryInfoDialog(
    entry: ScheduleEntry,
    schedule: Schedule,
    onDismiss: () -> Unit
) {
    val subject = entry.subjectId?.let {
        schedule.findSubject(it)
    }

    val teacher = entry.teacherId?.let {
        schedule.findTeacher(it)
    }

    val location = entry.locationId?.let {
        schedule.findLocation(it)
    }

    val color =
        if (entry.type == SubjectType.CLASS) {
            Color(subject?.color ?: entry.color)
        } else {
            Color(entry.color)
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            if (entry.type == SubjectType.BREAK) {
                                subject?.name ?: "HORA LIBRE"
                            } else {
                                subject?.name ?: "MATERIA"
                            },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(5.dp)
                    )

                    Text(
                        text = formatTimeRange(
                            entry.startMinute,
                            entry.endMinute
                        ),
                        fontSize = 13.sp,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.AccessTime,
                    label = "Horario",
                    value = formatTimeRange(
                        entry.startMinute,
                        entry.endMinute
                    ),
                    color = color
                )

                if (
                    entry.type == SubjectType.CLASS &&
                    !teacher?.name.isNullOrBlank()
                ) {
                    InfoItem(
                        icon = Icons.Default.Person,
                        label = "Profesor",
                        value = teacher?.name.orEmpty(),
                        color = color
                    )
                }

                if (
                    entry.type == SubjectType.CLASS &&
                    !location?.name.isNullOrBlank()
                ) {
                    InfoItem(
                        icon = Icons.Default.LocationOn,
                        label = "Ubicación",
                        value = location?.name.orEmpty(),
                        color = color
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color
        )

        Column(
            modifier = Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatTimeRange(
    startMinute: Int,
    endMinute: Int
): String {
    return "${formatMinute(startMinute)} – ${formatMinute(endMinute)}"
}

private fun formatMinute(
    minute: Int
): String {
    val hour = minute / 60
    val minutes = minute % 60

    return "%02d:%02d".format(
        hour,
        minutes
    )
}