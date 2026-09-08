package com.horiz.data.model

import java.time.LocalDateTime

data class TaskNode(
    val id: Long,
    val subjectId: Long,
    var title: String,
    var description: String = "",
    var dueAt: LocalDateTime? = null,
    var completed: Boolean = false,
    var orderIndex: Int = 0
) {
    init {
        require(title.isNotBlank()) {
            "El título no puede estar vacío"
        }

        require(subjectId > 0) {
            "El subjectId debe ser válido"
        }
    }

    fun toggleCompleted() {
        completed = !completed
    }

    fun isOverdue(
        now: LocalDateTime = LocalDateTime.now()
    ): Boolean {
        return !completed &&
                dueAt != null &&
                dueAt!!.isBefore(now)
    }

    fun status(
        now: LocalDateTime = LocalDateTime.now()
    ): TaskStatus {
        return when {
            completed -> TaskStatus.COMPLETED
            isOverdue(now) -> TaskStatus.OVERDUE
            else -> TaskStatus.PENDING
        }
    }
}