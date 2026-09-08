package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType

@Composable
fun ScheduleTextView(
    schedule: Schedule
) {
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()

    val lines = remember(schedule) {
        buildHz3Display(schedule)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .background(
                MaterialTheme.colorScheme.surface
            )
            .padding(16.dp)
    ) {
        lines.forEach { line ->
            when (line.type) {
                HZ3LineType.TITLE -> {
                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HZ3LineType.SECTION -> {
                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HZ3LineType.FIELD -> {
                    Text(
                        text = line.text,
                        modifier = Modifier
                            .horizontalScroll(
                                horizontalScroll
                            ),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HZ3LineType.ENTRY -> {
                    Text(
                        text = line.text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                horizontalScroll
                            ),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HZ3LineType.SEPARATOR -> {
                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = line.text,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private enum class HZ3LineType {
    TITLE,
    SECTION,
    FIELD,
    ENTRY,
    SEPARATOR
}

private data class HZ3Line(
    val type: HZ3LineType,
    val text: String
)

private fun buildHz3Display(
    schedule: Schedule
): List<HZ3Line> {
    val lines = mutableListOf<HZ3Line>()

    lines.add(
        HZ3Line(
            HZ3LineType.TITLE,
            "HZ3"
        )
    )

    lines.add(
        HZ3Line(
            HZ3LineType.SEPARATOR,
            "────────────────────────────────────────"
        )
    )

    lines.add(
        HZ3Line(
            HZ3LineType.SECTION,
            "SCHEDULE"
        )
    )

    lines.add(
        HZ3Line(
            HZ3LineType.FIELD,
            "name     = ${schedule.name}"
        )
    )

    lines.add(
        HZ3Line(
            HZ3LineType.FIELD,
            "enabled  = ${if (schedule.enabled) "1" else "0"}"
        )
    )

    lines.add(
        HZ3Line(
            HZ3LineType.SECTION,
            "SUBJECTS"
        )
    )

    if (schedule.subjects.isEmpty()) {
        lines.add(
            HZ3Line(
                HZ3LineType.FIELD,
                "(empty)"
            )
        )
    } else {
        schedule.subjects.forEach { subject ->
            lines.add(
                HZ3Line(
                    HZ3LineType.FIELD,
                    "${subject.id} | ${subject.name} | ${subject.color}"
                )
            )
        }
    }

    lines.add(
        HZ3Line(
            HZ3LineType.SECTION,
            "TEACHERS"
        )
    )

    if (schedule.teachers.isEmpty()) {
        lines.add(
            HZ3Line(
                HZ3LineType.FIELD,
                "(empty)"
            )
        )
    } else {
        schedule.teachers.forEach { teacher ->
            lines.add(
                HZ3Line(
                    HZ3LineType.FIELD,
                    "${teacher.id} | ${teacher.name}"
                )
            )
        }
    }

    lines.add(
        HZ3Line(
            HZ3LineType.SECTION,
            "LOCATIONS"
        )
    )

    if (schedule.locations.isEmpty()) {
        lines.add(
            HZ3Line(
                HZ3LineType.FIELD,
                "(empty)"
            )
        )
    } else {
        schedule.locations.forEach { location ->
            lines.add(
                HZ3Line(
                    HZ3LineType.FIELD,
                    "${location.id} | ${location.name}"
                )
            )
        }
    }

    val dayNames = listOf(
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
        "SATURDAY",
        "SUNDAY"
    )

    schedule.days.forEachIndexed { index, day ->
        lines.add(
            HZ3Line(
                HZ3LineType.SECTION,
                dayNames[index]
            )
        )

        lines.add(
            HZ3Line(
                HZ3LineType.FIELD,
                "enabled = ${if (day.enabled) "1" else "0"}"
            )
        )

        if (day.entries.isEmpty()) {
            lines.add(
                HZ3Line(
                    HZ3LineType.FIELD,
                    "(empty)"
                )
            )
        } else {
            day.entries
                .sortedBy { it.startMinute }
                .forEach { entry ->
                    lines.add(
                        HZ3Line(
                            HZ3LineType.ENTRY,
                            formatEntry(
                                entry,
                                schedule
                            )
                        )
                    )
                }
        }
    }

    lines.add(
        HZ3Line(
            HZ3LineType.SEPARATOR,
            "────────────────────────────────────────"
        )
    )

    return lines
}

private fun formatEntry(
    entry: ScheduleEntry,
    schedule: Schedule
): String {
    val subject = entry.subjectId?.let {
        schedule.findSubject(it)
    }

    val teacher = entry.teacherId?.let {
        schedule.findTeacher(it)
    }

    val location = entry.locationId?.let {
        schedule.findLocation(it)
    }

    val start = formatMinute(
        entry.startMinute
    )

    val end = formatMinute(
        entry.endMinute
    )

    val subjectId =
        entry.subjectId?.toString() ?: ""

    val teacherId =
        entry.teacherId?.toString() ?: ""

    val locationId =
        entry.locationId?.toString() ?: ""

    val subjectName =
        subject?.name ?: ""

    val teacherName =
        teacher?.name ?: ""

    val locationName =
        location?.name ?: ""

    return buildString {
        append(
            "  ${entry.id}"
        )

        append(
            " | subject=$subjectId"
        )

        append(
            " | teacher=$teacherId"
        )

        append(
            " | location=$locationId"
        )

        append(
            " | $start-$end"
        )

        append(
            " | color=${entry.color}"
        )

        append(
            " | type=${entry.type.name}"
        )

        if (entry.type == SubjectType.CLASS) {
            append(
                " | $subjectName"
            )

            if (teacherName.isNotBlank()) {
                append(
                    " | $teacherName"
                )
            }

            if (locationName.isNotBlank()) {
                append(
                    " | $locationName"
                )
            }
        } else {
            append(
                " | ${entry.name ?: "Recreo"}"
            )
        }
    }
}

private fun formatMinute(
    minute: Int
): String {
    val hour = minute / 60
    val minutes = minute % 60

    return "%02d:%02d".format(
        hour,
        minutes
    )
}