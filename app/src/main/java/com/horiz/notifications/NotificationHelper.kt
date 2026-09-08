package com.horiz.alarms

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.horiz.MainActivity
import com.horiz.R

class NotificationHelper(
    private val context: Context
) {

    private val notificationManager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val reminderChannel =
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Recordatorios de HORIZ",
                    NotificationManager.IMPORTANCE_HIGH
                )

            val alarmSound =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_ALARM
                )

            val audioAttributes =
                AudioAttributes.Builder()
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .setUsage(
                        AudioAttributes.USAGE_ALARM
                    )
                    .build()

            val alarmChannel =
                NotificationChannel(
                    CHANNEL_WAKEUP,
                    "Despertador HORIZ",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    lockscreenVisibility =
                        Notification.VISIBILITY_PUBLIC

                    setSound(
                        alarmSound,
                        audioAttributes
                    )

                    enableVibration(true)
                }

            notificationManager.createNotificationChannel(
                reminderChannel
            )

            notificationManager.createNotificationChannel(
                alarmChannel
            )
        }
    }

    fun showClassReminder(
        subjectName: String,
        startTime: String
    ) {
        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_REMINDERS
            )
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(
                    "Próxima clase"
                )
                .setContentText(
                    "$subjectName comienza a las $startTime"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            nextNotificationId(),
            notification
        )
    }

    fun showTaskReminder(
        title: String
    ) {
        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_REMINDERS
            )
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(
                    "Recordatorio de tarea"
                )
                .setContentText(
                    title
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        notificationManager.notify(
            nextNotificationId(),
            notification
        )
    }

    fun showFullScreenAlarmTrigger(
        subjectName: String,
        classTime: String
    ) {
        val fullScreenIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP

                putExtra(
                    MainActivity.EXTRA_NAVIGATE_TO,
                    MainActivity.ROUTE_ALARM_TRIGGER
                )

                putExtra(
                    "subject_name",
                    subjectName
                )

                putExtra(
                    "class_time",
                    classTime
                )
            }

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                context,
                REQ_FULL_SCREEN,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val stopIntent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                action =
                    ACTION_STOP_ALARM
            }

        val stopPendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQ_STOP,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val snoozeIntent =
            Intent(
                context,
                TestAlarmReceiver::class.java
            ).apply {
                action =
                    ACTION_SNOOZE_ALARM

                putExtra(
                    TestAlarmReceiver.EXTRA_SUBJECT_NAME,
                    subjectName
                )

                putExtra(
                    TestAlarmReceiver.EXTRA_START_TIME,
                    classTime
                )
            }

        val snoozePendingIntent =
            PendingIntent.getBroadcast(
                context,
                REQ_SNOOZE,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_WAKEUP
            )
                .setSmallIcon(R.drawable.noti)
                .setContentTitle(
                    "¡Despertador HORIZ!"
                )
                .setContentText(
                    "Clase de $subjectName a las $classTime"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                    NotificationCompat.CATEGORY_ALARM
                )
                .setOngoing(true)
                .setFullScreenIntent(
                    fullScreenPendingIntent,
                    true
                )
                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )
                .addAction(
                    R.drawable.clock,
                    "Posponer 5 min",
                    snoozePendingIntent
                )
                .addAction(
                    R.drawable.stop,
                    "Detener",
                    stopPendingIntent
                )
                .build()

        notificationManager.notify(
            NOTIFICATION_ID_WAKEUP,
            notification
        )
    }

    fun cancelWakeUpNotification() {
        notificationManager.cancel(
            NOTIFICATION_ID_WAKEUP
        )
    }

    private fun nextNotificationId(): Int {
        return (
                System.currentTimeMillis() % 1_000_000
                ).toInt()
    }

    companion object {
        const val CHANNEL_REMINDERS =
            "horiz_reminders"

        const val CHANNEL_WAKEUP =
            "horiz_wakeup_alarm"

        const val NOTIFICATION_ID_WAKEUP =
            1003

        const val REQ_FULL_SCREEN =
            2001

        const val REQ_STOP =
            2002

        const val REQ_SNOOZE =
            2003

        const val ACTION_STOP_ALARM =
            "com.horiz.ACTION_STOP_ALARM"

        const val ACTION_SNOOZE_ALARM =
            "com.horiz.ACTION_SNOOZE_ALARM"
    }
}