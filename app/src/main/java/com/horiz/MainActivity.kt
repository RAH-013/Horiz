package com.horiz

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.alarms.NotificationHelper
import com.horiz.data.preferences.AppPreferences
import com.horiz.navigation.AppNavigation
import com.horiz.ui.screens.AlarmTriggerScreen
import com.horiz.ui.theme.HorizTheme
import com.horiz.widget.TodayScheduleWidgetReceiver
import com.horiz.widget.WidgetPromptBottomSheet
import com.horiz.widget.WidgetPromptManager
import com.horiz.widget.updateHorizWidgets
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var showAlarmScreen by mutableStateOf(false)
    private var alarmSubjectData by mutableStateOf<Pair<String, String>?>(null)

    private var destinationRoute by mutableStateOf<String?>(null)
    private var openTasksSubjectId by mutableStateOf<String?>(null)
    private var navigationEventId by mutableIntStateOf(0)

    private var showWidgetPrompt by mutableStateOf(false)

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmScheduler: AppAlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper = NotificationHelper(applicationContext)
        alarmScheduler = AppAlarmScheduler(applicationContext)

        lifecycleScope.launch { updateHorizWidgets(applicationContext) }

        enableEdgeToEdge()

        configureAlarmWindow()
        handleIntent(intent)

        setContent {
            val preferences = AppPreferences(applicationContext)

            val appTheme by preferences.theme
                .collectAsStateWithLifecycle(initialValue = null)

            val baseColor by preferences.baseColor
                .collectAsStateWithLifecycle(initialValue = null)

            LaunchedEffect(showAlarmScreen) {
                if (showAlarmScreen) {
                    showSystemNavigationBar()
                } else {
                    hideSystemNavigationBar()
                }
            }

            LaunchedEffect(Unit) {
                if (WidgetPromptManager.shouldShowPrompt(applicationContext, TodayScheduleWidgetReceiver::class.java)) {
                    showWidgetPrompt = true
                    WidgetPromptManager.markPromptShown(applicationContext)
                }
            }

            if (appTheme == null || baseColor == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                HorizTheme(
                    appTheme = appTheme!!,
                    baseColor = baseColor!!
                ) {
                    if (showAlarmScreen && alarmSubjectData != null) {
                        AlarmTriggerScreen(
                            subjectName = alarmSubjectData!!.first,
                            classTime = alarmSubjectData!!.second,
                            onStopAlarm = { stopAlarmScreen() },
                            onSnoozeAlarm = { snoozeAlarm() }
                        )
                    } else {
                        AppNavigation(
                            baseColor = baseColor!!,
                            startDestinationOverride = destinationRoute,
                            openTasksSubjectId = openTasksSubjectId,
                            navigationEventId = navigationEventId,
                            onDestinationConsumed = {
                                destinationRoute = null
                            },
                            onTasksSubjectConsumed = {
                                openTasksSubjectId = null
                            }
                        )
                    }

                    if (showWidgetPrompt) {
                        WidgetPromptBottomSheet(
                            onDismissRequest = { showWidgetPrompt = false },
                            onAddWidgetClicked = {
                                showWidgetPrompt = false
                                WidgetPromptManager.requestPinWidget(
                                    applicationContext,
                                    TodayScheduleWidgetReceiver::class.java
                                )
                            },
                            onMaybeLaterClicked = {
                                showWidgetPrompt = false
                            },
                            onDontAskAgainClicked = {
                                showWidgetPrompt = false
                                WidgetPromptManager.setDontAskAgain(applicationContext)
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.getStringExtra(EXTRA_NAVIGATE_TO) == ROUTE_ALARM_TRIGGER) {
            val subjectName = intent.getStringExtra("subject_name") ?: "Materia Desconocida"
            val classTime = intent.getStringExtra("class_time") ?: "--:--"
            alarmSubjectData = Pair(subjectName, classTime)
            showAlarmScreen = true
            return
        }

        val route = intent.getStringExtra(EXTRA_NAVIGATE_TO)
        val subjectId = intent.getStringExtra(EXTRA_OPEN_TASKS_SUBJECT_ID)

        if (!subjectId.isNullOrEmpty()) {
            openTasksSubjectId = subjectId
            destinationRoute = route ?: "subjects"
            navigationEventId++
        } else if (!route.isNullOrEmpty()) {
            destinationRoute = route
            navigationEventId++
        }
    }

    private fun stopAlarmScreen() {
        notificationHelper.cancelWakeUpNotification()
        showAlarmScreen = false
        alarmSubjectData = null
    }

    private fun snoozeAlarm() {
        val data = alarmSubjectData
        notificationHelper.cancelWakeUpNotification()
        showAlarmScreen = false
        alarmSubjectData = null

        alarmScheduler.scheduleSnoozeAlarm(
            subjectName = data?.first ?: "Materia Desconocida",
            classTime = data?.second ?: "--:--"
        )
    }

    private fun configureAlarmWindow() {
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun hideSystemNavigationBar() {
        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    private fun showSystemNavigationBar() {
        val controller = WindowCompat.getInsetsController(
            window,
            window.decorView
        )

        controller.show(
            WindowInsetsCompat.Type.navigationBars()
        )
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val EXTRA_OPEN_TASKS_SUBJECT_ID = "open_tasks_subject_id"
        const val ROUTE_ALARM_TRIGGER = "alarm_trigger"
    }
}