package com.horiz.ui.screens.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun TodayEntryCard(
    entry: ScheduleEntry,
    schedule: Schedule,
    currentMinute: Int,
    onClick: () -> Unit,
    onManageTasks: (() -> Unit)? = null
) {
    val isClass = entry.type == SubjectType.CLASS

    val name =
        if (isClass) {
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

    val remaining =
        (entry.startMinute - currentMinute)
            .coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color,
                            CircleShape
                        )
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = name.uppercase(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            "${formatMinute(entry.startMinute)} – " +
                                    formatMinute(entry.endMinute),
                        color = color,
                        fontSize = 13.sp
                    )
                }

                if (remaining > 0) {
                    Text(
                        text = countdownText(remaining),
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isClass && onManageTasks != null) {
                Spacer(
                    modifier = Modifier.size(10.dp)
                )

                Button(
                    onClick = onManageTasks,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Administrar Tareas")
                }
            }
        }
    }
}

private fun countdownText(
    minutes: Int
): String {
    val hours = minutes / 60
    val remaining = minutes % 60

    return when {
        hours > 0 && remaining > 0 ->
            "$hours h $remaining min"

        hours > 0 ->
            "$hours h"

        else ->
            "$remaining min"
    }
}

private fun formatMinute(
    minute: Int
): String {
    return "%02d:%02d".format(
        minute / 60,
        minute % 60
    )
}