package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.horiz.data.model.DayNode
import com.horiz.data.model.Schedule

@Composable
fun ScheduleGrid(
    schedule: Schedule,
    days: List<DayNode>,
    startHour: Int,
    endHour: Int
) {
    val verticalScroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            TimeColumn(
                startHour = startHour,
                endHour = endHour
            )

            days.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    DayColumn(
                        day = day,
                        schedule = schedule,
                        startHour = startHour,
                        endHour = endHour
                    )
                }
            }
        }
    }
}