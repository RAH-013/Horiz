package com.horiz.ui.screens.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun TodayNextCard(
    entry: ScheduleEntry,
    schedule: Schedule,
    currentMinute: Int,
    onClick: () -> Unit,
    onManageTasks: (() -> Unit)? = null
) {
    val isClass = entry.type == SubjectType.CLASS

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

    val color = Color(
        if (isClass) {
            entry.subjectId
                ?.let { schedule.findSubject(it)?.color }
                ?: entry.color
        } else {
            entry.color
        }
    )

    val remaining = (entry.startMinute - currentMinute).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = name.uppercase(),
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "${formatMinute(entry.startMinute)} – ${formatMinute(entry.endMinute)}",
                color = color
            )

            if (isClass) {
                entry.teacherId
                    ?.let { schedule.findTeacher(it)?.name }
                    ?.takeIf { it.isNotBlank() }
                    ?.let { teacher ->
                        Text(
                            text = teacher,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                entry.locationId
                    ?.let { schedule.findLocation(it)?.name }
                    ?.takeIf { it.isNotBlank() }
                    ?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Comienza en",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = countdownText(remaining),
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }

            if (isClass && onManageTasks != null) {
                Button(
                    onClick = onManageTasks,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Administrar Tareas")
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
    return "%02d:%02d".format(minute / 60, minute % 60)
}