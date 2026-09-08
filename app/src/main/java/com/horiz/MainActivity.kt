package com.horiz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.alarms.NotificationHelper
import com.horiz.data.preferences.AppPreferences
import com.horiz.ui.screens.AlarmTriggerScreen
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor
import com.horiz.ui.theme.HorizTheme

class MainActivity : ComponentActivity() {

    private var showAlarmScreen by mutableStateOf(false)
    private var alarmSubjectData by mutableStateOf<Pair<String, String>?>(null)

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmScheduler: AppAlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper = NotificationHelper(this)
        alarmScheduler = AppAlarmScheduler(this)

        enableEdgeToEdge()

        handleAlarmIntent(intent)

        setContent {
            val preferences = AppPreferences(applicationContext)

            val appTheme by preferences.theme.collectAsStateWithLifecycle(
                initialValue = AppTheme.SYSTEM
            )

            val baseColor by preferences.baseColor.collectAsStateWithLifecycle(
                initialValue = BaseColor.PURPLE
            )

            LaunchedEffect(showAlarmScreen) {
                if (showAlarmScreen) {
                    showSystemNavigationBar()
                } else {
                    hideSystemNavigationBar()
                }
            }

            HorizTheme(
                appTheme = appTheme,
                baseColor = baseColor
            ) {
                if (showAlarmScreen && alarmSubjectData != null) {
                    AlarmTriggerScreen(
                        subjectName = alarmSubjectData!!.first,
                        classTime = alarmSubjectData!!.second,
                        onStopAlarm = {
                            showAlarmScreen = false
                            alarmSubjectData = null
                            notificationHelper.cancelWakeUpNotification()
                        },
                        onSnoozeAlarm = {
                            val data = alarmSubjectData

                            showAlarmScreen = false
                            alarmSubjectData = null

                            notificationHelper.cancelWakeUpNotification()

                            alarmScheduler.scheduleSnoozeAlarm(
                                subjectName =
                                    data?.first
                                        ?: "Materia Desconocida",
                                classTime =
                                    data?.second
                                        ?: "--:--"
                            )
                        }
                    )
                } else {
                    App()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        if (
            intent?.getStringExtra(EXTRA_NAVIGATE_TO) ==
            ROUTE_ALARM_TRIGGER
        ) {
            val subjectName =
                intent.getStringExtra("subject_name")
                    ?: "Materia Desconocida"

            val classTime =
                intent.getStringExtra("class_time")
                    ?: "--:--"

            alarmSubjectData = Pair(
                subjectName,
                classTime
            )

            showAlarmScreen = true
        }
    }

    private fun hideSystemNavigationBar() {
        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat
                .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    private fun showSystemNavigationBar() {
        val controller =
            WindowCompat.getInsetsController(
                window,
                window.decorView
            )

        controller.show(
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val ROUTE_ALARM_TRIGGER = "alarm_trigger"
    }
}