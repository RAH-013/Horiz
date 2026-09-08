package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TIME_COLUMN_WIDTH = 56

@Composable
fun ScheduleDayHeader(
    days: List<String>,
    todayIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                MaterialTheme.colorScheme.surface
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        Box(
            modifier = Modifier
                .width(TIME_COLUMN_WIDTH.dp)
                .fillMaxHeight()
        )

        days.forEachIndexed { index, day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (index == todayIndex) {
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.12f
                            )
                        } else {
                            Color.Transparent
                        }
                    )
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = if (index == todayIndex) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}