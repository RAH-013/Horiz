package com.horiz.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.data.model.SubjectType
import com.horiz.data.preferences.AppPreferences
import com.horiz.storage.ScheduleStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppAlarmScheduler(
    context: Context
) {

    private val context = context.applicationContext

    private val alarmManager =
        this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val preferences = AppPreferences(this.context)
    private val storage = ScheduleStorage(this.context)
    private val permissionHelper = AlarmPermissionHelper(this.context)

    private val alarmPrefs =
        this.context.getSharedPreferences(
            ALARM_PREFS,
            Context.MODE_PRIVATE
        )

    fun scheduleAll() {
        cancelRegisteredAlarms()

        if (!hasRequiredPermissions()) return

        val schedule = getActiveSchedule() ?: return

        if (!schedule.enabled) return

        scheduleClassRemindersInternal(schedule)
        scheduleTaskRemindersInternal(schedule)
        scheduleWakeUpAlarmsInternal(schedule)
    }

    fun scheduleTaskReminders() {
        cancelRegisteredAlarms(TYPE_TASK)

        if (!hasRequiredPermissions()) return

        val schedule = getActiveSchedule() ?: return

        if (!schedule.enabled) return

        scheduleTaskRemindersInternal(schedule)
    }

    fun scheduleSnoozeAlarm(
        subjectName: String,
        classTime: String
    ) {
        if (!hasRequiredPermissions()) return

        cancelSnoozeAlarm()

        val triggerAt =
            System.currentTimeMillis() + SNOOZE_DELAY_MILLIS

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
            triggerAt,
            pendingIntent
        )
    }

    fun rescheduleRecurringAlarm(
        alarmType: Int,
        entryId: Long
    ) {
        if (!hasRequiredPermissions()) return

        val schedule = getActiveSchedule() ?: return

        if (!schedule.enabled) return

        val entry =
            schedule.findEntry(entryId) ?: return

        if (entry.type != SubjectType.CLASS) return

        val now = LocalDateTime.now()

        val classDateTime =
            nextOccurrence(
                entry.dayIndex,
                entry.startMinute,
                now
            )

        val triggerAt =
            when (alarmType) {
                TestAlarmReceiver.TYPE_REMINDER_CLASSES -> {
                    var value =
                        classDateTime.minusMinutes(
                            CLASS_REMINDER_MINUTES
                        )

                    if (!value.isAfter(now)) {
                        value = value.plusWeeks(1)
                    }

                    value
                }

                TestAlarmReceiver.TYPE_WAKEUP -> {
                    val offset =
                        runBlocking {
                            preferences.wakeUpOffsetMinutes.first()
                        }

                    var value =
                        classDateTime.minusMinutes(
                            offset.toLong()
                        )

                    if (!value.isAfter(now)) {
                        value = value.plusWeeks(1)
                    }

                    value
                }

                else -> return
            }

        val subjectName =
            getSubjectName(
                schedule,
                entry
            ) ?: "Materia Desconocida"

        val type =
            when (alarmType) {
                TestAlarmReceiver.TYPE_REMINDER_CLASSES ->
                    TYPE_CLASS

                TestAlarmReceiver.TYPE_WAKEUP ->
                    TYPE_WAKEUP

                else -> return
            }

        scheduleAlarm(
            requestCode = requestCode(type, entry.id),
            triggerAt = triggerAt,
            alarmType = alarmType,
            entryId = entry.id,
            subjectName = subjectName,
            startTime = formatTime(entry.startMinute)
        )
    }

    private fun scheduleClassRemindersInternal(
        schedule: Schedule
    ) {
        val enabled =
            runBlocking {
                preferences.isClassesReminderEnabled.first()
            }

        if (!enabled) return

        val now = LocalDateTime.now()

        schedule.days
            .filter { it.enabled }
            .forEach { day ->

                day.entries
                    .filter {
                        it.type == SubjectType.CLASS &&
                                it.subjectId != null
                    }
                    .forEach { entry ->

                        val subjectName =
                            getSubjectName(
                                schedule,
                                entry
                            ) ?: return@forEach

                        val classDateTime =
                            nextOccurrence(
                                day.index,
                                entry.startMinute,
                                now
                            )

                        val reminderAt =
                            classDateTime.minusMinutes(
                                CLASS_REMINDER_MINUTES
                            )

                        if (reminderAt.isAfter(now)) {
                            scheduleAlarm(
                                requestCode = requestCode(
                                    TYPE_CLASS,
                                    entry.id
                                ),
                                triggerAt = reminderAt,
                                alarmType =
                                    TestAlarmReceiver
                                        .TYPE_REMINDER_CLASSES,
                                entryId = entry.id,
                                subjectName = subjectName,
                                startTime =
                                    formatTime(
                                        entry.startMinute
                                    )
                            )
                        }
                    }
            }
    }

    private fun scheduleTaskRemindersInternal(
        schedule: Schedule
    ) {
        val enabled =
            runBlocking {
                preferences.isTasksReminderEnabled.first()
            }

        if (!enabled) return

        val daysBefore =
            runBlocking {
                preferences.tasksReminderDays.first()
            }

        val now = LocalDateTime.now()

        schedule.tasks
            .filter {
                !it.completed &&
                        it.dueAt != null
            }
            .forEach { task ->

                val dueAt =
                    task.dueAt ?: return@forEach

                val reminderAt =
                    dueAt.minusDays(
                        daysBefore.toLong()
                    )

                if (!reminderAt.isAfter(now)) {
                    return@forEach
                }

                scheduleAlarm(
                    requestCode = requestCode(
                        TYPE_TASK,
                        task.id
                    ),
                    triggerAt = reminderAt,
                    alarmType =
                        TestAlarmReceiver
                            .TYPE_REMINDER_TASKS,
                    taskTitle = task.title
                )
            }
    }

    private fun scheduleWakeUpAlarmsInternal(
        schedule: Schedule
    ) {
        val enabled =
            runBlocking {
                preferences.isWakeUpAlarmEnabled.first()
            }

        if (!enabled) return

        val offset =
            runBlocking {
                preferences.wakeUpOffsetMinutes.first()
            }

        val now = LocalDateTime.now()

        schedule.days
            .filter { it.enabled }
            .forEach { day ->

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

                val classDateTime =
                    nextOccurrence(
                        day.index,
                        firstClass.startMinute,
                        now
                    )

                var alarmAt =
                    classDateTime.minusMinutes(
                        offset.toLong()
                    )

                if (!alarmAt.isAfter(now)) {
                    alarmAt = alarmAt.plusWeeks(1)
                }

                val subjectName =
                    getSubjectName(
                        schedule,
                        firstClass
                    ) ?: "Materia Desconocida"

                scheduleAlarm(
                    requestCode = requestCode(
                        TYPE_WAKEUP,
                        firstClass.id
                    ),
                    triggerAt = alarmAt,
                    alarmType =
                        TestAlarmReceiver.TYPE_WAKEUP,
                    entryId = firstClass.id,
                    subjectName = subjectName,
                    startTime =
                        formatTime(
                            firstClass.startMinute
                        )
                )
            }
    }

    private fun scheduleAlarm(
        requestCode: Int,
        triggerAt: LocalDateTime,
        alarmType: Int,
        entryId: Long? = null,
        subjectName: String? = null,
        startTime: String? = null,
        taskTitle: String? = null
    ) {
        val triggerMillis =
            triggerAt
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        if (triggerMillis <= System.currentTimeMillis()) {
            return
        }

        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                putExtra(
                    TestAlarmReceiver.EXTRA_ALARM_TYPE,
                    alarmType
                )

                entryId?.let {
                    putExtra(
                        TestAlarmReceiver.EXTRA_ENTRY_ID,
                        it
                    )
                }

                subjectName?.let {
                    putExtra(
                        TestAlarmReceiver.EXTRA_SUBJECT_NAME,
                        it
                    )
                }

                startTime?.let {
                    putExtra(
                        TestAlarmReceiver.EXTRA_START_TIME,
                        it
                    )
                }

                taskTitle?.let {
                    putExtra(
                        TestAlarmReceiver.EXTRA_TASK_TITLE,
                        it
                    )
                }
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )

        registerAlarm(
            alarmType,
            requestCode
        )
    }

    private fun getActiveSchedule(): Schedule? {
        val name = storage.getKing() ?: return null
        return storage.getSchedule(name)
    }

    private fun getSubjectName(
        schedule: Schedule,
        entry: ScheduleEntry
    ): String? {
        val subjectId = entry.subjectId ?: return null
        return schedule.findSubject(subjectId)?.name
    }

    private fun nextOccurrence(
        dayIndex: Int,
        startMinute: Int,
        now: LocalDateTime
    ): LocalDateTime {

        val targetDay = DayOfWeek.of(dayIndex + 1)

        var date = now.toLocalDate()

        repeat(8) {
            if (date.dayOfWeek == targetDay) {
                val candidate =
                    LocalDateTime.of(
                        date,
                        LocalTime.of(
                            startMinute / 60,
                            startMinute % 60
                        )
                    )

                if (candidate.isAfter(now)) {
                    return candidate
                }
            }

            date = date.plusDays(1)
        }

        return LocalDateTime.of(
            date,
            LocalTime.of(
                startMinute / 60,
                startMinute % 60
            )
        )
    }

    private fun formatTime(
        startMinute: Int
    ): String =
        LocalTime.of(
            startMinute / 60,
            startMinute % 60
        ).format(
            DateTimeFormatter.ofPattern(
                "hh:mm a"
            )
        )

    private fun hasRequiredPermissions(): Boolean =
        permissionHelper.hasNotificationPermission() &&
                permissionHelper.hasExactAlarmPermission()

    private fun registerAlarm(
        type: Int,
        requestCode: Int
    ) {
        val alarms =
            getRegisteredAlarms().toMutableSet()

        alarms.add(
            encodeAlarm(
                type,
                requestCode
            )
        )

        saveRegisteredAlarms(alarms)
    }

    private fun cancelRegisteredAlarms(
        type: Int? = null
    ) {
        val alarms = getRegisteredAlarms()
        val remaining = mutableSetOf<String>()

        alarms.forEach { value ->

            val alarm =
                decodeAlarm(value)
                    ?: return@forEach

            if (
                type == null ||
                alarm.type == type
            ) {
                cancelRequestCode(
                    alarm.requestCode
                )
            } else {
                remaining.add(value)
            }
        }

        saveRegisteredAlarms(remaining)
    }

    private fun cancelRequestCode(
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

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun cancelSnoozeAlarm() {
        val intent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            )

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQ_SNOOZE,
                intent,
                PendingIntent.FLAG_NO_CREATE or
                        PendingIntent.FLAG_IMMUTABLE
            )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun getRegisteredAlarms(): Set<String> =
        alarmPrefs
            .getStringSet(
                KEY_REGISTERED_ALARMS,
                emptySet()
            )
            ?.toSet()
            ?: emptySet()

    private fun saveRegisteredAlarms(
        alarms: Set<String>
    ) {
        alarmPrefs
            .edit()
            .putStringSet(
                KEY_REGISTERED_ALARMS,
                alarms
            )
            .apply()
    }

    private fun encodeAlarm(
        type: Int,
        requestCode: Int
    ): String =
        "$type:$requestCode"

    private fun decodeAlarm(
        value: String
    ): RegisteredAlarm? {

        val parts =
            value.split(
                ":",
                limit = 2
            )

        if (parts.size != 2) return null

        val type =
            parts[0].toIntOrNull()
                ?: return null

        val requestCode =
            parts[1].toIntOrNull()
                ?: return null

        return RegisteredAlarm(
            type = type,
            requestCode = requestCode
        )
    }

    private fun requestCode(
        type: Int,
        id: Long
    ): Int =
        type *
                REQUEST_CODE_MULTIPLIER +
                (id and 0x7FFFFF).toInt()

    private data class RegisteredAlarm(
        val type: Int,
        val requestCode: Int
    )

    companion object {
        private const val ALARM_PREFS = "horiz_alarm_registry"
        private const val KEY_REGISTERED_ALARMS =
            "registered_alarm_request_codes"

        private const val REQUEST_CODE_MULTIPLIER = 1_000_000

        private const val TYPE_CLASS = 1
        private const val TYPE_TASK = 2
        private const val TYPE_WAKEUP = 3

        private const val REQ_SNOOZE = 9001

        private const val CLASS_REMINDER_MINUTES = 10L
        private const val SNOOZE_DELAY_MILLIS =
            5 * 60 * 1000L
    }
}