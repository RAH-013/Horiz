package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.horiz.data.model.DayNode
import com.horiz.data.model.Schedule

private const val HOUR_HEIGHT = 72

@Composable
fun DayColumn(
    day: DayNode,
    schedule: Schedule,
    startHour: Int,
    endHour: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                ((endHour - startHour) * HOUR_HEIGHT).dp
            )
    ) {
        if (day.enabled) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (hour in startHour until endHour) {
                    Box(
                        modifier = Modifier
                            .height(HOUR_HEIGHT.dp)
                            .fillMaxWidth()
                            .border(
                                width = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            day.entries.forEach { entry ->
                ScheduleEntryBlock(
                    entry = entry,
                    schedule = schedule,
                    startHour = startHour
                )
            }
        } else {
            DisabledDayOverlay()
        }
    }
}

@Composable
private fun DisabledDayOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.88f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .padding(12.dp)
                .alpha(0.8f),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Block,
                contentDescription = "Día deshabilitado",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.height(24.dp)
            )
        }
    }
}