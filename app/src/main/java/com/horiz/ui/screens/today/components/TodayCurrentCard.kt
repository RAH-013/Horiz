package com.horiz.ui.screens.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun TodayCurrentCard(
    entry: ScheduleEntry,
    schedule: Schedule,
    currentMinute: Int,
    onClick: () -> Unit,
    onManageTasks: (() -> Unit)? = null
) {
    val isClass = entry.type == SubjectType.CLASS
    val colorScheme = MaterialTheme.colorScheme

    val name = if (isClass) {
        entry.subjectId
            ?.let { schedule.findSubject(it)?.name }
            ?.takeIf { it.isNotBlank() }
            ?: "Materia"
    } else {
        entry.name
            ?.takeIf { it.isNotBlank() }
            ?: "Recreo"
    }

    val elapsed = (currentMinute - entry.startMinute).coerceAtLeast(0)
    val duration = (entry.endMinute - entry.startMinute).coerceAtLeast(1)
    val progress = (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    val remaining = (entry.endMinute - currentMinute).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name.uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = colorScheme.onSurface
                )
            }

            Text(
                text = "${formatMinute(entry.startMinute)} – ${formatMinute(entry.endMinute)}",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )

            if (isClass) {
                entry.teacherId
                    ?.let { schedule.findTeacher(it)?.name }
                    ?.takeIf { it.isNotBlank() }
                    ?.let { teacher ->
                        Text(
                            text = teacher,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                entry.locationId
                    ?.let { schedule.findLocation(it)?.name }
                    ?.takeIf { it.isNotBlank() }
                    ?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
            }

            Spacer(modifier = Modifier.height(2.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp),
                color = colorScheme.primary,
                trackColor = colorScheme.surfaceContainerHighest,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Termina en",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Text(
                    text = countdownText(remaining),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.primary
                )
            }

            if (isClass && onManageTasks != null) {
                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onManageTasks,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Administrar tareas"
                    )
                }
            }
        }
    }
}

private fun countdownText(minutes: Int): String {
    val hours = minutes / 60
    val remaining = minutes % 60

    return when {
        hours > 0 && remaining > 0 -> "$hours h $remaining min"
        hours > 0 -> "$hours h"
        else -> "$remaining min"
    }
}

private fun formatMinute(minute: Int): String {
    return "%02d:%02d".format(
        minute / 60,
        minute % 60
    )
}