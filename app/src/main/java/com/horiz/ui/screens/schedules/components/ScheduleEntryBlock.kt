package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import kotlin.math.max

private const val HOUR_HEIGHT = 72

@Composable
fun ScheduleEntryBlock(
    entry: ScheduleEntry,
    schedule: Schedule,
    startHour: Int,
    onClick: (() -> Unit)? = null
) {
    val subject = entry.subjectId?.let {
        schedule.findSubject(it)
    }

    val baseColor =
        if (entry.type == SubjectType.CLASS) {
            Color(subject?.color ?: entry.color)
        } else {
            Color(entry.color)
        }

    val textColor = contrastingTextColor(
        baseColor
    )

    val topOffset = minutesToOffset(
        minute = entry.startMinute,
        startHour = startHour
    )

    val blockHeight = minutesToHeight(
        entry.startMinute,
        entry.endMinute
    )

    val shape = RoundedCornerShape(9.dp)

    var showDialog by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 3.dp,
                vertical = 2.dp
            )
            .padding(
                top = topOffset
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(blockHeight)
                .clip(shape)
                .background(baseColor)
                .border(
                    width = 1.dp,
                    color = textColor.copy(
                        alpha = 0.16f
                    ),
                    shape = shape
                )
                .clickable {
                    if (onClick != null) {
                        onClick()
                    } else {
                        showDialog = true
                    }
                }
                .padding(
                    horizontal = 7.dp,
                    vertical = 5.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text =
                    if (entry.type == SubjectType.BREAK) {
                        entry.name ?: "RECREO"
                    } else {
                        subject?.name ?: "MATERIA"
                    },
                modifier = Modifier.fillMaxWidth(),
                color = textColor,
                fontSize = when {
                    blockHeight < 38.dp -> 9.sp
                    blockHeight < 55.dp -> 10.sp
                    blockHeight < 75.dp -> 11.sp
                    else -> 12.sp
                },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = maxLinesForHeight(
                    blockHeight
                ),
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }

    if (showDialog) {
        ScheduleEntryInfoDialog(
            entry = entry,
            schedule = schedule,
            onDismiss = {
                showDialog = false
            }
        )
    }
}

private fun maxLinesForHeight(
    height: Dp
): Int {
    return when {
        height < 38.dp -> 1
        height < 60.dp -> 2
        height < 90.dp -> 3
        else -> 4
    }
}

private fun contrastingTextColor(
    background: Color
): Color {
    val luminance =
        0.2126f * background.red +
                0.7152f * background.green +
                0.0722f * background.blue

    return if (luminance > 0.60f) {
        Color(0xFF171717)
    } else {
        Color.White
    }
}

private fun minutesToOffset(
    minute: Int,
    startHour: Int
): Dp {
    val visibleMinute =
        (minute - startHour * 60)
            .coerceAtLeast(0)

    return (
            visibleMinute.toFloat() /
                    60f *
                    HOUR_HEIGHT
            ).dp
}

private fun minutesToHeight(
    startMinute: Int,
    endMinute: Int
): Dp {
    val duration =
        max(
            1,
            endMinute - startMinute
        )

    return (
            duration.toFloat() /
                    60f *
                    HOUR_HEIGHT
            ).dp
}