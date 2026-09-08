package com.horiz.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationHelper =
                    NotificationHelper(context)

                when (intent.action) {

                    ACTION_STOP_ALARM -> {
                        notificationHelper.cancelWakeUpNotification()
                    }

                    ACTION_SNOOZE_ALARM -> {
                        val subjectName =
                            intent.getStringExtra(
                                EXTRA_SUBJECT_NAME
                            ) ?: "Materia Desconocida"

                        val startTime =
                            intent.getStringExtra(
                                EXTRA_START_TIME
                            ) ?: "--:--"

                        notificationHelper.cancelWakeUpNotification()

                        AppAlarmScheduler(context)
                            .scheduleSnoozeAlarm(
                                subjectName = subjectName,
                                classTime = startTime
                            )
                    }

                    else -> {
                        when (
                            intent.getIntExtra(
                                EXTRA_ALARM_TYPE,
                                -1
                            )
                        ) {

                            TYPE_REMINDER_CLASSES -> {
                                val subjectName =
                                    intent.getStringExtra(
                                        EXTRA_SUBJECT_NAME
                                    ) ?: return@launch

                                val startTime =
                                    intent.getStringExtra(
                                        EXTRA_START_TIME
                                    ) ?: return@launch

                                notificationHelper.showClassReminder(
                                    subjectName = subjectName,
                                    startTime = startTime
                                )
                            }

                            TYPE_REMINDER_TASKS -> {
                                val taskTitle =
                                    intent.getStringExtra(
                                        EXTRA_TASK_TITLE
                                    ) ?: return@launch

                                notificationHelper.showTaskReminder(
                                    title = taskTitle
                                )
                            }

                            TYPE_WAKEUP -> {
                                val subjectName =
                                    intent.getStringExtra(
                                        EXTRA_SUBJECT_NAME
                                    ) ?: "Materia Desconocida"

                                val startTime =
                                    intent.getStringExtra(
                                        EXTRA_START_TIME
                                    ) ?: "--:--"

                                notificationHelper.showFullScreenAlarmTrigger(
                                    subjectName = subjectName,
                                    classTime = startTime
                                )
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_TYPE =
            "ALARM_TYPE"

        const val EXTRA_ENTRY_ID =
            "ENTRY_ID"

        const val EXTRA_TASK_ID =
            "TASK_ID"

        const val EXTRA_TASK_TITLE =
            "TASK_TITLE"

        const val EXTRA_SUBJECT_NAME =
            "SUBJECT_NAME"

        const val EXTRA_START_TIME =
            "START_TIME"

        const val TYPE_REMINDER_CLASSES = 1
        const val TYPE_REMINDER_TASKS = 2
        const val TYPE_WAKEUP = 3

        const val ACTION_STOP_ALARM =
            "com.horiz.ACTION_STOP_ALARM"

        const val ACTION_SNOOZE_ALARM =
            "com.horiz.ACTION_SNOOZE_ALARM"
    }
}