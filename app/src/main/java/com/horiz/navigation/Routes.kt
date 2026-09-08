package com.horiz.navigation

sealed class Routes(
    val route: String
) {
    data object Home : Routes("home")

    data object Today : Routes("today")

    data object Subjects : Routes("subjects")

    data object Schedules : Routes("schedules")

    data object Scanner : Routes("scanner")

    data object Tasks : Routes(
        "tasks/{scheduleName}/{scheduleEntryId}"
    ) {
        fun createRoute(
            scheduleName: String,
            scheduleEntryId: Long
        ): String {
            return "tasks/${
                android.net.Uri.encode(scheduleName)
            }/$scheduleEntryId"
        }
    }

    data object Qr : Routes("qr/{scheduleName}") {
        fun createRoute(scheduleName: String): String {
            return "qr/${android.net.Uri.encode(scheduleName)}"
        }
    }

    data object ScheduleView : Routes("schedule_view/{scheduleName}") {
        fun createRoute(scheduleName: String): String {
            return "schedule_view/${android.net.Uri.encode(scheduleName)}"
        }
    }

    data object ScheduleEditor : Routes("schedule_editor/{scheduleId}") {
        fun createRoute(scheduleId: Long): String {
            return "schedule_editor/$scheduleId"
        }
    }

    data object Trash : Routes("trash")

    data object Settings : Routes("settings")
}