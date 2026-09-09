package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.DayNode
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import java.time.LocalDate

private const val TIME_COLUMN_WIDTH = 52
private val MIN_HOUR_HEIGHT = 60.dp

private data class MergedEntryBlock(
    val entry: ScheduleEntry,
    val startDayIndex: Int,
    val daySpan: Int
)

@Composable
fun ScheduleGrid(
    schedule: Schedule,
    days: List<DayNode>,
    startHour: Int,
    endHour: Int,
    horizontalScrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val columnWidth = 112.dp
    val totalHours = (endHour - startHour).coerceAtLeast(1)

    var selectedEntry by remember { mutableStateOf<ScheduleEntry?>(null) }

    val currentDayIndex = remember {
        (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)
    }

    val mergedBlocks = remember(days, schedule) {
        calculateMergedBlocks(days, schedule)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableHeight = maxHeight
        val dynamicHourHeight = (availableHeight / totalHours).coerceAtLeast(MIN_HOUR_HEIGHT)
        val gridHeight = dynamicHourHeight * totalHours

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScroll)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .width(TIME_COLUMN_WIDTH.dp)
                        .height(gridHeight)
                ) {
                    TimeColumn(
                        startHour = startHour,
                        endHour = endHour,
                        hourHeight = dynamicHourHeight
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Box(
                        modifier = Modifier
                            .width(columnWidth * days.size)
                            .height(gridHeight)
                    ) {

                        Row(modifier = Modifier.fillMaxSize()) {
                            days.forEach { day ->
                                val isToday = day.index == currentDayIndex
                                val bgModifier = if (isToday) {
                                    Modifier.background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    )
                                } else {
                                    Modifier
                                }

                                Column(
                                    modifier = Modifier
                                        .width(columnWidth)
                                        .height(gridHeight)
                                        .then(bgModifier)
                                ) {
                                    for (hour in startHour until endHour) {
                                        Box(
                                            modifier = Modifier
                                                .height(dynamicHourHeight)
                                                .fillMaxWidth()
                                                .border(
                                                    width = 0.5.dp,
                                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        mergedBlocks.forEach { block ->
                            val startMinutesFromBase = block.entry.startMinute - (startHour * 60)
                            val durationMinutes = block.entry.endMinute - block.entry.startMinute

                            val topOffset = (startMinutesFromBase * dynamicHourHeight.value / 60).dp
                            val blockHeight = (durationMinutes * dynamicHourHeight.value / 60).dp
                            val leftOffset = columnWidth * block.startDayIndex
                            val blockWidth = columnWidth * block.daySpan

                            MergedScheduleBlock(
                                entry = block.entry,
                                schedule = schedule,
                                width = blockWidth,
                                height = blockHeight,
                                leftOffset = leftOffset,
                                topOffset = topOffset,
                                onClick = {
                                    selectedEntry = block.entry
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedEntry?.let { entry ->
        ScheduleEntryInfoDialog(
            entry = entry,
            schedule = schedule,
            onDismiss = { selectedEntry = null }
        )
    }
}

@Composable
private fun MergedScheduleBlock(
    entry: ScheduleEntry,
    schedule: Schedule,
    width: Dp,
    height: Dp,
    leftOffset: Dp,
    topOffset: Dp,
    onClick: () -> Unit
) {
    val displayName = remember(entry, schedule) { entry.getDisplayName(schedule) }
    val blockColor = remember(entry.color) { Color(entry.color) }

    Box(
        modifier = Modifier
            .offset(x = leftOffset, y = topOffset)
            .width(width)
            .height(height)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(blockColor)
            .clickable { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun calculateMergedBlocks(days: List<DayNode>, schedule: Schedule): List<MergedEntryBlock> {
    val result = mutableListOf<MergedEntryBlock>()
    val sortedDays = days.sortedBy { it.index }
    val visited = mutableSetOf<Pair<Int, Long>>()

    for (i in sortedDays.indices) {
        val currentDay = sortedDays[i]
        if (!currentDay.enabled) continue

        for (entry in currentDay.entries) {
            if (visited.contains(Pair(currentDay.index, entry.id))) continue

            var span = 1
            val entryTitle = entry.getDisplayName(schedule)

            for (j in (i + 1) until sortedDays.size) {
                val nextDay = sortedDays[j]
                if (!nextDay.enabled || nextDay.index != sortedDays[j - 1].index + 1) break

                val matchingEntry = nextDay.entries.find {
                    it.getDisplayName(schedule) == entryTitle &&
                            it.startMinute == entry.startMinute &&
                            it.endMinute == entry.endMinute
                }

                if (matchingEntry != null) {
                    span++
                    visited.add(Pair(nextDay.index, matchingEntry.id))
                } else {
                    break
                }
            }

            visited.add(Pair(currentDay.index, entry.id))
            result.add(
                MergedEntryBlock(
                    entry = entry,
                    startDayIndex = i,
                    daySpan = span
                )
            )
        }
    }

    return result
}

private fun ScheduleEntry.getDisplayName(schedule: Schedule): String {
    return if (type == SubjectType.BREAK) {
        name ?: "Recreo"
    } else {
        schedule.subjects.find { it.id == subjectId }?.name ?: name ?: "Materia"
    }
}