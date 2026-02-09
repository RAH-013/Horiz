package com.example.horiz.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.horiz.R
import com.example.horiz.activities.MainActivity

class ClassReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "class_alarm_channel"
        private const val CHANNEL_NAME = "Clase próxima"
        private const val NOTIFICATION_BASE_ID = 1000
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val subjectName = intent?.getStringExtra("subject_name") ?: "Clase"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación si es Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "⏰ Te avisamos 10 minutos antes de tu clase"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app al tocar la notificación
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Icono grande en color (logo de la app)
        val largeIcon = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)

        // Construir la notificación con emojis, vibración y sonido
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // simple y legible en barra
            .setLargeIcon(largeIcon) // logo completo a color en el panel expandido
            .setContentTitle("📚 ¡Hora de tu clase!")
            .setContentText("🔥 $subjectName empieza en 10 minutos.\n ¡Prepárate! ✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // sonido + vibración + luces
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .build()

        // Mostrar la notificación
        notificationManager.notify(NOTIFICATION_BASE_ID + subjectName.hashCode(), notification)
    }
}