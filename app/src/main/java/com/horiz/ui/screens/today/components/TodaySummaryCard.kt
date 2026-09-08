package com.horiz.ui.screens.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import com.horiz.data.model.TaskNode

@Composable
fun TodaySummaryCard(
    entries: List<ScheduleEntry>,
    tasks: List<TaskNode>
) {
    val classes =
        entries.count {
            it.type == SubjectType.CLASS
        }

    val breaks =
        entries.count {
            it.type == SubjectType.BREAK
        }

    val completed =
        tasks.count {
            it.completed
        }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement =
                Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                value = classes.toString(),
                label = "Clases"
            )

            SummaryItem(
                value = breaks.toString(),
                label = "Descansos"
            )

            SummaryItem(
                value = tasks.size.toString(),
                label = "Tareas"
            )

            SummaryItem(
                value = completed.toString(),
                label = "Completadas"
            )
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}