package com.horiz.ui.screens.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.Schedule
import com.horiz.data.model.TaskNode
import com.horiz.data.model.TaskStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TodayTaskCard(
    task: TaskNode,
    schedule: Schedule,
    now: LocalDateTime,
    onClick: () -> Unit
) {
    val status = task.status(now)

    val subject =
        schedule.findSubject(task.subjectId)

    val color =
        if (status == TaskStatus.OVERDUE) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
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
                    modifier = Modifier.width(10.dp)
                )

                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration =
                        if (task.completed) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                    color =
                        if (task.completed) {
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme
                                .onSurface
                        }
                )
            }

            if (task.description.isNotBlank()) {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = task.description,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                task.dueAt?.let {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = color
                    )

                    Spacer(
                        modifier = Modifier.width(5.dp)
                    )

                    Text(
                        text = it.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        ),
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                subject?.name?.let {
                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    Text(
                        text = it,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}