package com.horiz.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import com.horiz.data.preferences.AppPreferences
import com.horiz.storage.ScheduleStorage
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first

class AppAlarmScheduler(
    private val context: Context
) {
    private val alarmManager =
        context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    private val preferences =
        AppPreferences(
            context.applicationContext
        )

    private val storage =
        ScheduleStorage(
            context.applicationContext
        )

    suspend fun scheduleAll() {
        cancelAll()

        val scheduleName =
            storage.getKing()
                ?: return

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return

        if (!schedule.enabled) return

        scheduleClassReminders(schedule)
        scheduleTaskReminders(schedule)
        scheduleWakeUpAlarms(schedule)
    }

    suspend fun scheduleClassReminders() {
        cancelClassReminders()

        val scheduleName =
            storage.getKing()
                ?: return

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return

        if (!schedule.enabled) return

        scheduleClassReminders(schedule)
    }

    suspend fun scheduleTaskReminders() {
        cancelTaskReminders()

        val scheduleName =
            storage.getKing()
                ?: return

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return

        if (!schedule.enabled) return

        scheduleTaskReminders(schedule)
    }

    suspend fun scheduleWakeUpAlarms() {
        cancelWakeUpAlarms()

        val scheduleName =
            storage.getKing()
                ?: return

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return

        if (!schedule.enabled) return

        scheduleWakeUpAlarms(schedule)
    }

    fun scheduleSnoozeAlarm(
        subjectName: String,
        classTime: String
    ) {
        if (!hasExactAlarmPermission()) return

        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                putExtra(
                    TestAlarmReceiver.EXTRA_ALARM_TYPE,
                    TestAlarmReceiver.TYPE_WAKEUP
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_SUBJECT_NAME,
                    subjectName
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_START_TIME,
                    classTime
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQ_SNOOZE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() +
                    5 * 60 * 1000L,
            pendingIntent
        )
    }

    fun cancelAll() {
        cancelClassReminders()
        cancelTaskReminders()
        cancelWakeUpAlarms()
    }

    fun cancelClassReminders() {
        cancelType(
            TestAlarmReceiver.TYPE_REMINDER_CLASSES
        )
    }

    fun cancelTaskReminders() {
        cancelType(
            TestAlarmReceiver.TYPE_REMINDER_TASKS
        )
    }

    fun cancelWakeUpAlarms() {
        cancelType(
            TestAlarmReceiver.TYPE_WAKEUP
        )

        cancelPendingIntent(
            REQ_SNOOZE
        )
    }

    private suspend fun scheduleClassReminders(
        schedule: Schedule
    ) {
        if (
            !preferences
                .isClassesReminderEnabled
                .first()
        ) {
            return
        }

        if (!hasExactAlarmPermission()) {
            return
        }

        val now =
            LocalDateTime.now()

        schedule.days.forEach { day ->
            if (!day.enabled) {
                return@forEach
            }

            day.entries
                .filter {
                    it.type == SubjectType.CLASS &&
                            it.subjectId != null
                }
                .forEach { entry ->

                    val classTime =
                        nextOccurrence(
                            entry.dayIndex,
                            entry.startMinute,
                            now
                        )

                    val reminderTime =
                        classTime.minusMinutes(10)

                    if (reminderTime.isAfter(now)) {
                        scheduleEntryAlarm(
                            entry = entry,
                            triggerAt = reminderTime,
                            alarmType =
                                TestAlarmReceiver
                                    .TYPE_REMINDER_CLASSES
                        )
                    }
                }
        }
    }

    private suspend fun scheduleTaskReminders(
        schedule: Schedule
    ) {
        if (
            !preferences
                .isTasksReminderEnabled
                .first()
        ) {
            return
        }

        if (!hasExactAlarmPermission()) {
            return
        }

        val daysBefore =
            preferences
                .tasksReminderDays
                .first()

        val now =
            LocalDateTime.now()

        schedule.tasks
            .filter {
                !it.completed &&
                        it.dueAt != null
            }
            .forEach { task ->

                val dueAt =
                    task.dueAt
                        ?: return@forEach

                val reminderAt =
                    dueAt.minusDays(
                        daysBefore.toLong()
                    )

                val triggerAt =
                    if (reminderAt.isAfter(now)) {
                        reminderAt
                    } else {
                        now.plusSeconds(5)
                    }

                scheduleTaskAlarm(
                    taskId = task.id,
                    title = task.title,
                    triggerAt = triggerAt
                )
            }
    }

    private suspend fun scheduleWakeUpAlarms(
        schedule: Schedule
    ) {
        if (
            !preferences
                .isWakeUpAlarmEnabled
                .first()
        ) {
            return
        }

        if (!hasExactAlarmPermission()) {
            return
        }

        val offset =
            preferences
                .wakeUpOffsetMinutes
                .first()

        val now =
            LocalDateTime.now()

        schedule.days.forEach { day ->
            if (!day.enabled) {
                return@forEach
            }

            val firstClass =
                day.entries
                    .filter {
                        it.type == SubjectType.CLASS &&
                                it.subjectId != null
                    }
                    .minByOrNull {
                        it.startMinute
                    }
                    ?: return@forEach

            val classOccurrence =
                nextOccurrence(
                    firstClass.dayIndex,
                    firstClass.startMinute,
                    now
                )

            var wakeUpTime =
                classOccurrence.minusMinutes(
                    offset.toLong()
                )

            if (!wakeUpTime.isAfter(now)) {
                wakeUpTime =
                    classOccurrence
                        .plusWeeks(1)
                        .minusMinutes(
                            offset.toLong()
                        )
            }

            val subjectName =
                getSubjectName(firstClass)
                    ?: return@forEach

            val classTime =
                formatTime(
                    firstClass.startMinute
                )

            scheduleWakeUpAlarm(
                entry = firstClass,
                subjectName = subjectName,
                classTime = classTime,
                triggerAt = wakeUpTime
            )
        }
    }

    private fun scheduleEntryAlarm(
        entry: ScheduleEntry,
        triggerAt: LocalDateTime,
        alarmType: Int
    ) {
        val subjectName =
            getSubjectName(entry)
                ?: return

        val classTime =
            formatTime(
                entry.startMinute
            )

        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                putExtra(
                    TestAlarmReceiver.EXTRA_ALARM_TYPE,
                    alarmType
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_ENTRY_ID,
                    entry.id
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_SUBJECT_NAME,
                    subjectName
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_START_TIME,
                    classTime
                )
            }

        scheduleExact(
            requestCode(
                alarmType,
                entry.id
            ),
            triggerAt,
            intent
        )
    }

    private fun scheduleWakeUpAlarm(
        entry: ScheduleEntry,
        subjectName: String,
        classTime: String,
        triggerAt: LocalDateTime
    ) {
        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                putExtra(
                    TestAlarmReceiver.EXTRA_ALARM_TYPE,
                    TestAlarmReceiver.TYPE_WAKEUP
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_ENTRY_ID,
                    entry.id
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_SUBJECT_NAME,
                    subjectName
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_START_TIME,
                    classTime
                )
            }

        scheduleExact(
            requestCode(
                TestAlarmReceiver.TYPE_WAKEUP,
                entry.id
            ),
            triggerAt,
            intent
        )
    }

    private fun scheduleTaskAlarm(
        taskId: Long,
        title: String,
        triggerAt: LocalDateTime
    ) {
        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                putExtra(
                    TestAlarmReceiver.EXTRA_ALARM_TYPE,
                    TestAlarmReceiver.TYPE_REMINDER_TASKS
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_TASK_ID,
                    taskId
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_TASK_TITLE,
                    title
                )
            }

        scheduleExact(
            requestCode(
                TestAlarmReceiver.TYPE_REMINDER_TASKS,
                taskId
            ),
            triggerAt,
            intent
        )
    }

    private fun scheduleExact(
        requestCode: Int,
        triggerAt: LocalDateTime,
        intent: Intent
    ) {
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val triggerMillis =
            triggerAt
                .atZone(
                    ZoneId.systemDefault()
                )
                .toInstant()
                .toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }

    private fun cancelType(
        type: Int
    ) {
        val scheduleName =
            storage.getKing()
                ?: return

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return

        when (type) {
            TestAlarmReceiver.TYPE_REMINDER_CLASSES -> {
                schedule.days
                    .flatMap {
                        it.entries
                    }
                    .filter {
                        it.type == SubjectType.CLASS
                    }
                    .forEach { entry ->
                        cancelPendingIntent(
                            requestCode(
                                type,
                                entry.id
                            )
                        )
                    }
            }

            TestAlarmReceiver.TYPE_REMINDER_TASKS -> {
                schedule.tasks.forEach { task ->
                    cancelPendingIntent(
                        requestCode(
                            type,
                            task.id
                        )
                    )
                }
            }

            TestAlarmReceiver.TYPE_WAKEUP -> {
                schedule.days
                    .flatMap {
                        it.entries
                    }
                    .filter {
                        it.type == SubjectType.CLASS
                    }
                    .forEach { entry ->
                        cancelPendingIntent(
                            requestCode(
                                type,
                                entry.id
                            )
                        )
                    }
            }
        }
    }

    private fun cancelPendingIntent(
        requestCode: Int
    ) {
        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or
                        PendingIntent.FLAG_IMMUTABLE
            )

        if (pendingIntent != null) {
            alarmManager.cancel(
                pendingIntent
            )

            pendingIntent.cancel()
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun nextOccurrence(
        dayIndex: Int,
        minute: Int,
        now: LocalDateTime
    ): LocalDateTime {
        val targetDay =
            DayOfWeek.of(
                dayIndex + 1
            )

        var date =
            now.toLocalDate()

        while (
            date.dayOfWeek != targetDay
        ) {
            date = date.plusDays(1)
        }

        var result =
            LocalDateTime.of(
                date,
                LocalTime.of(
                    minute / 60,
                    minute % 60
                )
            )

        if (!result.isAfter(now)) {
            result = result.plusWeeks(1)
        }

        return result
    }

    private fun getSubjectName(
        entry: ScheduleEntry
    ): String? {
        val scheduleName =
            storage.getKing()
                ?: return null

        val schedule =
            storage.getSchedule(scheduleName)
                ?: return null

        val subjectId =
            entry.subjectId
                ?: return null

        return schedule
            .findSubject(subjectId)
            ?.name
    }

    private fun formatTime(
        minute: Int
    ): String {
        return LocalTime.of(
            minute / 60,
            minute % 60
        ).format(
            DateTimeFormatter.ofPattern(
                "hh:mm a"
            )
        )
    }

    private fun requestCode(
        type: Int,
        id: Long
    ): Int {
        return (
                type * 1_000_000L +
                        (id and 0x7FFFFF)
                ).toInt()
    }

    companion object {
        private const val REQ_SNOOZE = 9001
    }
}