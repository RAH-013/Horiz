package com.horiz.ui.screens.subjects.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun SubjectList(
    items: List<ScheduleEntry>,
    schedule: Schedule,
    onEdit: (ScheduleEntry) -> Unit,
    onDelete: (ScheduleEntry) -> Unit,
    onDuplicate: (ScheduleEntry) -> Unit,
    onClick: (ScheduleEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
    ) {
        items.forEach { item ->
            SubjectCard(
                item = item,
                schedule = schedule,
                onEdit = {
                    onEdit(item)
                },
                onDelete = {
                    onDelete(item)
                },
                onDuplicate = {
                    onDuplicate(item)
                },
                onClick = {
                    onClick(item)
                }
            )
        }
    }
}