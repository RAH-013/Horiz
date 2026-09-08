package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleList(
    schedules: List<String>,
    king: String?,
    contentPadding: PaddingValues,
    onOpen: (String) -> Unit,
    onActivate: (String) -> Unit,
    onEdit: (String) -> Unit,
    onShare: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 12.dp
        )
    ) {
        items(
            items = schedules,
            key = { it }
        ) { scheduleName ->
            ScheduleCard(
                scheduleName = scheduleName,
                isKing = scheduleName == king,
                onOpen = {
                    onOpen(scheduleName)
                },
                onActivate = {
                    onActivate(scheduleName)
                },
                onEdit = {
                    onEdit(scheduleName)
                },
                onShare = {
                    onShare(scheduleName)
                },
                onDelete = {
                    onDelete(scheduleName)
                }
            )
        }
    }
}