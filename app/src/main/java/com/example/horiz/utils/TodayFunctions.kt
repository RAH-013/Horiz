package com.example.horiz.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.horiz.model.SubjectNode
import com.example.horiz.notifications.ClassReminderReceiver
import java.util.Calendar


fun scheduleClassAlarms(context: Context, subjects: List<SubjectNode>, todayIndex: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    subjects.filter { it.d == todayIndex }.forEach { subject ->
        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, subject.s / 60)
            set(Calendar.MINUTE, (subject.s % 60) - 10)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        if (triggerTime > System.currentTimeMillis()) {
            val intent = Intent(context, ClassReminderReceiver::class.java).apply {
                putExtra("subject_name", subject.n)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                subject.i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}

fun getMillisUntilNextChange(activeSubjects: List<SubjectNode>, nowMinutes: Int): Long {
    val futureTimes = activeSubjects.flatMap { listOf(it.s, it.f) }.filter { it > nowMinutes }.sorted()
    if (futureTimes.isEmpty()) return Long.MAX_VALUE
    val nextMinute = futureTimes.first()
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, nextMinute / 60)
        set(Calendar.MINUTE, nextMinute % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return target.timeInMillis - now.timeInMillis
}