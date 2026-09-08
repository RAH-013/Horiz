package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val HOUR_HEIGHT = 72
private const val TIME_COLUMN_WIDTH = 56

@Composable
fun TimeColumn(
    startHour: Int,
    endHour: Int
) {
    Column(
        modifier = Modifier.width(
            TIME_COLUMN_WIDTH.dp
        )
    ) {
        for (hour in startHour until endHour) {
            Box(
                modifier = Modifier
                    .height(HOUR_HEIGHT.dp)
                    .fillMaxWidth()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "%02d:00".format(hour),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        top = 4.dp
                    )
                )
            }
        }
    }
}