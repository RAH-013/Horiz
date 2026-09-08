package com.horiz.ui.screens.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import com.horiz.data.model.TaskNode
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.today.components.TodayCurrentCard
import com.horiz.ui.screens.today.components.TodayDayFinishedCard
import com.horiz.ui.screens.today.components.TodayEmptyCard
import com.horiz.ui.screens.today.components.TodayEntryCard
import com.horiz.ui.screens.today.components.TodayHeader
import com.horiz.ui.screens.today.components.TodayNextCard
import com.horiz.ui.screens.today.components.TodaySummaryCard
import com.horiz.ui.screens.today.components.TodayTaskCard
import com.horiz.ui.screens.today.components.toHorizIndex
import java.time.LocalDateTime
import kotlinx.coroutines.delay

@Composable
fun TodayScreen(
    schedule: Schedule,
    onBackClick: (() -> Unit)? = null,
    onTaskClick: ((TaskNode) -> Unit)? = null,
    onEntryClick: ((ScheduleEntry) -> Unit)? = null,
    onManageTasks: ((ScheduleEntry) -> Unit)? = null
) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    val today = remember(now) { now.toLocalDate() }
    val currentMinute = remember(now) { now.hour * 60 + now.minute }
    val dayIndex = remember(today) { today.dayOfWeek.toHorizIndex() }
    val day = remember(schedule, dayIndex) { schedule.days.getOrNull(dayIndex) }

    val isDayDisabled = day == null || !day.enabled

    val entries = remember(day) {
        if (isDayDisabled) emptyList()
        else day?.entries?.sortedBy { it.startMinute } ?: emptyList()
    }

    val currentEntry = remember(entries, currentMinute) {
        entries.firstOrNull { currentMinute >= it.startMinute && currentMinute < it.endMinute }
    }

    val previousEntry = remember(entries, currentMinute) {
        entries.lastOrNull { it.endMinute <= currentMinute }
    }

    val nextEntries = remember(entries, currentMinute) {
        entries.filter { it.startMinute > currentMinute }
    }

    val nextEntry = remember(nextEntries) { nextEntries.firstOrNull() }
    val previewEntries = remember(nextEntries) { nextEntries.drop(1) }

    val firstEntry = remember(entries) { entries.firstOrNull() }
    val lastEntry = remember(entries) { entries.lastOrNull() }

    val beforeDay = remember(firstEntry, currentMinute) {
        firstEntry != null && currentMinute < firstEntry.startMinute
    }

    val afterDay = remember(lastEntry, currentMinute) {
        lastEntry != null && currentMinute >= lastEntry.endMinute
    }

    val todayTasks by remember(schedule.tasks, today) {
        derivedStateOf {
            schedule.tasks
                .filter { it.dueAt?.toLocalDate() == today }
                .sortedWith(compareBy<TaskNode> { it.completed }.thenBy { it.dueAt })
        }
    }

    val pendingTasks = remember(todayTasks) {
        todayTasks.count { !it.completed }
    }

    AppScreen(
        title = "Hoy",
        onBackClick = onBackClick
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                TodayHeader(
                    date = today,
                    pendingTasks = pendingTasks
                )
            }

            when {
                isDayDisabled -> {
                    item {
                        TodayEmptyCard(
                            title = "Día libre",
                            message = "Hoy no tienes clases u horarios programados."
                        )
                    }
                }

                entries.isEmpty() -> {
                    item {
                        TodayEmptyCard(
                            title = "Sin actividades",
                            message = "No hay actividades programadas para el día de hoy."
                        )
                    }
                }

                afterDay -> {
                    item {
                        TodayDayFinishedCard()
                    }
                }

                else -> {
                    if (currentEntry != null && previousEntry != null) {
                        item {
                            SectionLabel(text = "ANTERIOR")
                        }
                        item {
                            TodayEntryCard(
                                entry = previousEntry,
                                schedule = schedule,
                                currentMinute = currentMinute,
                                onClick = { onEntryClick?.invoke(previousEntry) }
                            )
                        }
                    }

                    when {
                        beforeDay && firstEntry != null -> {
                            item {
                                SectionLabel(text = "PRIMERA ACTIVIDAD DEL DÍA")
                            }
                            item {
                                TodayNextCard(
                                    entry = firstEntry,
                                    schedule = schedule,
                                    currentMinute = currentMinute,
                                    onClick = { onEntryClick?.invoke(firstEntry) },
                                    onManageTasks = if (firstEntry.type == SubjectType.CLASS) {
                                        { onManageTasks?.invoke(firstEntry) }
                                    } else null
                                )
                            }
                        }

                        currentEntry != null -> {
                            item {
                                SectionLabel(text = "AHORA")
                            }
                            item {
                                TodayCurrentCard(
                                    entry = currentEntry,
                                    schedule = schedule,
                                    currentMinute = currentMinute,
                                    onClick = { onEntryClick?.invoke(currentEntry) },
                                    onManageTasks = if (currentEntry.type == SubjectType.CLASS) {
                                        { onManageTasks?.invoke(currentEntry) }
                                    } else null
                                )
                            }
                        }

                        nextEntry != null -> {
                            item {
                                SectionLabel(text = "SIGUIENTE ACTIVIDAD")
                            }
                            item {
                                TodayNextCard(
                                    entry = nextEntry,
                                    schedule = schedule,
                                    currentMinute = currentMinute,
                                    onClick = { onEntryClick?.invoke(nextEntry) },
                                    onManageTasks = if (nextEntry.type == SubjectType.CLASS) {
                                        { onManageTasks?.invoke(nextEntry) }
                                    } else null
                                )
                            }
                        }
                    }

                    if (currentEntry != null && nextEntry != null) {
                        item {
                            SectionLabel(text = "A CONTINUACIÓN")
                        }
                        item {
                            TodayNextCard(
                                entry = nextEntry,
                                schedule = schedule,
                                currentMinute = currentMinute,
                                onClick = { onEntryClick?.invoke(nextEntry) },
                                onManageTasks = if (nextEntry.type == SubjectType.CLASS) {
                                    { onManageTasks?.invoke(nextEntry) }
                                } else null
                            )
                        }
                    }

                    if (previewEntries.isNotEmpty()) {
                        item {
                            SectionLabel(text = "MÁS TARDE")
                        }

                        items(
                            items = previewEntries,
                            key = { "preview_${it.id}" }
                        ) { entry ->
                            TodayEntryCard(
                                entry = entry,
                                schedule = schedule,
                                currentMinute = currentMinute,
                                onClick = { onEntryClick?.invoke(entry) }
                            )
                        }
                    }

                    if (todayTasks.isNotEmpty()) {
                        item {
                            SectionLabel(
                                text = "Tareas de hoy",
                                isHeader = true
                            )
                        }

                        items(
                            items = todayTasks,
                            key = { "task_${it.id}" }
                        ) { task ->
                            TodayTaskCard(
                                task = task,
                                schedule = schedule,
                                now = now,
                                onClick = { onTaskClick?.invoke(task) }
                            )
                        }
                    }

                    if (entries.isNotEmpty()) {
                        item {
                            TodaySummaryCard(
                                entries = entries,
                                tasks = todayTasks
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        style = if (isHeader) {
            MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}