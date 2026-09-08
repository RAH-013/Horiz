package com.horiz.ui.screens.schedule.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun ScheduleViewModeToggle(
    textMode: Boolean,
    onToggle: () -> Unit
) {
    IconButton(
        onClick = onToggle
    ) {
        Icon(
            imageVector = if (textMode) {
                Icons.Default.ViewWeek
            } else {
                Icons.Default.Code
            },
            contentDescription = if (textMode) {
                "Vista de horario"
            } else {
                "Vista HZ3"
            }
        )
    }
}