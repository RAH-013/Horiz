package com.horiz.ui.screens.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.horiz.data.model.Schedule
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.schedule.components.ScheduleDayHeader
import com.horiz.ui.screens.schedule.components.ScheduleGrid
import com.horiz.ui.screens.schedule.components.ScheduleTextView
import com.horiz.ui.screens.schedule.components.ScheduleViewModeToggle
import java.util.Calendar

private const val DEFAULT_START_HOUR = 7
private const val DEFAULT_END_HOUR = 22

@Composable
fun ScheduleViewScreen(
    schedule: Schedule,
    onBackClick: (() -> Unit)? = null
) {
    val dayNames = remember {
        listOf(
            "LUN",
            "MAR",
            "MIÉ",
            "JUE",
            "VIE",
            "SÁB",
            "DOM"
        )
    }

    val todayIndex = remember {
        (
                Calendar.getInstance()
                    .get(Calendar.DAY_OF_WEEK) + 5
                ) % 7
    }

    val enabledDays = remember(schedule) {
        schedule.days.filter { it.enabled }
    }

    val visibleDays = remember(schedule) {
        schedule.days
            .filter { it.enabled }
            .map { dayNames[it.index] }
    }

    val timeRange = remember(schedule) {
        val entries = schedule.days
            .filter { it.enabled }
            .flatMap { it.entries }

        if (entries.isEmpty()) {
            DEFAULT_START_HOUR to DEFAULT_END_HOUR
        } else {
            val earliestMinute = entries.minOf { it.startMinute }
            val latestMinute = entries.maxOf { it.endMinute }

            val startHour = earliestMinute / 60
            val endHour = (latestMinute + 59) / 60

            startHour.coerceIn(0, 23) to
                    endHour.coerceIn(1, 24)
        }
    }

    var textMode by remember {
        mutableStateOf(false)
    }

    AppScreen(
        title = schedule.name,
        onBackClick = onBackClick,
        actions = {
            ScheduleViewModeToggle(
                textMode = textMode,
                onToggle = {
                    textMode = !textMode
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (textMode) {
                ScheduleTextView(
                    schedule = schedule
                )
            } else {
                ScheduleDayHeader(
                    days = visibleDays,
                    todayIndex = todayIndex
                )

                ScheduleGrid(
                    schedule = schedule,
                    days = enabledDays,
                    startHour = timeRange.first,
                    endHour = timeRange.second
                )
            }
        }
    }
}