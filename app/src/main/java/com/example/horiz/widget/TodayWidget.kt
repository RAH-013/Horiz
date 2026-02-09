package com.example.horiz.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.horiz.activities.TodayActivity
import com.example.horiz.model.Schedule
import com.example.horiz.storage.ScheduleStorage

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val storage = ScheduleStorage(context)
        val kingName = storage.getKing()
        val schedule: Schedule? = storage.getSchedule(kingName ?: "")
        val todayIndex = (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7
        val dayNode = schedule?.e?.getOrNull(todayIndex)
        val subjects = dayNode?.e ?: emptyList()

        provideContent {

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF6A1B9A))
                    .clickable(actionStartActivity<TodayActivity>()),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Título
                Text(
                    text = "Hoy",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.padding(8.dp)
                )

                if (subjects.isEmpty() || dayNode?.a == false) {
                    Text(
                        text = "¡No hay clases hoy! 😎",
                        style = TextStyle(
                            fontSize = 14.sp
                        ),
                        modifier = GlanceModifier.padding(4.dp)
                    )
                } else {
                    subjects.forEach { subject ->
                        Column(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(Color(subject.c))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = subject.n,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Profesor: ${subject.t}",
                                style = TextStyle(
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "Lugar: ${subject.p}",
                                style = TextStyle(
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }
            }
        }
    }
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}