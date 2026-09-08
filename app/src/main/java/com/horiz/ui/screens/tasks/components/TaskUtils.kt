package com.horiz.ui.screens.tasks.components

import com.horiz.data.model.Schedule
import com.horiz.data.model.SubjectType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TaskUtils {

    private val dateFormatter =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val timeFormatter =
        DateTimeFormatter.ofPattern("HH:mm")

    fun generateTaskId(
        schedule: Schedule
    ): Long {
        return (
                schedule.tasks.maxOfOrNull { it.id } ?: 0L
                ) + 1L
    }

    fun getEntryTitle(
        schedule: Schedule,
        entryId: Long
    ): String {
        val entry = schedule.findEntry(entryId)
            ?: return "Tareas"

        if (entry.type == SubjectType.BREAK) {
            return entry.name?.takeIf { it.isNotBlank() }
                ?: "Hora libre"
        }

        return entry.subjectId
            ?.let { schedule.findSubject(it)?.name }
            ?.takeIf { it.isNotBlank() }
            ?: "Tareas"
    }

    fun formatDate(
        date: LocalDate
    ): String {
        return date.format(dateFormatter)
    }

    fun formatTime(
        time: LocalTime
    ): String {
        return time.format(timeFormatter)
    }

    fun formatDueAt(
        dueAt: LocalDateTime
    ): String {
        return "Entrega: ${formatDate(dueAt.toLocalDate())} · ${formatTime(dueAt.toLocalTime())}"
    }
}