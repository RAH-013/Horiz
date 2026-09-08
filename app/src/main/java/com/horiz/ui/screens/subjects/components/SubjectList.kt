package com.horiz.ui.screens.subjects.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry

@Composable
fun SubjectList(
    items: List<ScheduleEntry>,
    schedule: Schedule,
    onEdit: (ScheduleEntry) -> Unit,
    onDelete: (ScheduleEntry) -> Unit,
    onClick: (ScheduleEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = 12.dp
        )
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            SubjectCard(
                item = item,
                schedule = schedule,
                onEdit = {
                    onEdit(item)
                },
                onDelete = {
                    onDelete(item)
                },
                onClick = {
                    onClick(item)
                }
            )
        }
    }
}