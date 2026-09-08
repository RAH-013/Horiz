package com.horiz.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.horiz.alarms.AlarmPermissionHelper
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.data.preferences.AppPreferences
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.settings.components.SettingsContent
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val preferences = remember {
        AppPreferences(context.applicationContext)
    }

    val alarmScheduler = remember {
        AppAlarmScheduler(context.applicationContext)
    }

    val permissionHelper = remember {
        AlarmPermissionHelper(context.applicationContext)
    }

    val theme by preferences.theme.collectAsState(
        initial = AppTheme.SYSTEM
    )

    val baseColor by preferences.baseColor.collectAsState(
        initial = BaseColor.PURPLE
    )

    val isClassesReminderEnabled by
    preferences.isClassesReminderEnabled.collectAsState(
        initial = false
    )

    val isTasksReminderEnabled by
    preferences.isTasksReminderEnabled.collectAsState(
        initial = false
    )

    val tasksReminderDays by
    preferences.tasksReminderDays.collectAsState(
        initial = 1
    )

    val isWakeUpAlarmEnabled by
    preferences.isWakeUpAlarmEnabled.collectAsState(
        initial = false
    )

    val wakeUpOffsetMinutes by
    preferences.wakeUpOffsetMinutes.collectAsState(
        initial = 90
    )

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                scope.launch {
                    if (
                        permissionHelper.hasExactAlarmPermission()
                    ) {
                        if (isClassesReminderEnabled) {
                            alarmScheduler.scheduleClassReminders()
                        }

                        if (isTasksReminderEnabled) {
                            alarmScheduler.scheduleTaskReminders()
                        }

                        if (isWakeUpAlarmEnabled) {
                            alarmScheduler.scheduleWakeUpAlarms()
                        }
                    }
                }
            }
        }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                )
            )
        }
    }

    fun scheduleEnabledAlarms() {
        scope.launch {
            if (!permissionHelper.hasNotificationPermission()) {
                return@launch
            }

            if (!permissionHelper.hasExactAlarmPermission()) {
                return@launch
            }

            if (isClassesReminderEnabled) {
                alarmScheduler.scheduleClassReminders()
            }

            if (isTasksReminderEnabled) {
                alarmScheduler.scheduleTaskReminders()
            }

            if (isWakeUpAlarmEnabled) {
                alarmScheduler.scheduleWakeUpAlarms()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scheduleEnabledAlarms()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (
            permissionHelper.hasNotificationPermission() &&
            permissionHelper.hasExactAlarmPermission()
        ) {
            scheduleEnabledAlarms()
        }
    }

    AppScreen(
        title = "Configuración",
        onBackClick = onBackClick
    ) { paddingValues ->

        SettingsContent(
            paddingValues = paddingValues,
            theme = theme,
            baseColor = baseColor,
            isClassesReminderEnabled = isClassesReminderEnabled,
            isTasksReminderEnabled = isTasksReminderEnabled,
            tasksReminderDays = tasksReminderDays,
            isWakeUpAlarmEnabled = isWakeUpAlarmEnabled,
            wakeUpOffsetMinutes = wakeUpOffsetMinutes,

            onThemeChange = { newTheme ->
                scope.launch {
                    preferences.saveTheme(newTheme)
                }
            },

            onBaseColorChange = { newBaseColor ->
                scope.launch {
                    preferences.saveBaseColor(newBaseColor)
                }
            },

            onClassesReminderChange = { enabled ->
                scope.launch {
                    preferences.saveClassesReminderEnabled(enabled)

                    if (!enabled) {
                        alarmScheduler.cancelClassReminders()
                        return@launch
                    }

                    if (!permissionHelper.hasNotificationPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                        return@launch
                    }

                    if (!permissionHelper.hasExactAlarmPermission()) {
                        openExactAlarmSettings()
                        return@launch
                    }

                    alarmScheduler.scheduleClassReminders()
                }
            },

            onTasksReminderChange = { enabled ->
                scope.launch {
                    preferences.saveTasksReminderEnabled(enabled)

                    if (!enabled) {
                        alarmScheduler.cancelTaskReminders()
                        return@launch
                    }

                    if (!permissionHelper.hasNotificationPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                        return@launch
                    }

                    if (!permissionHelper.hasExactAlarmPermission()) {
                        openExactAlarmSettings()
                        return@launch
                    }

                    alarmScheduler.scheduleTaskReminders()
                }
            },

            onTasksReminderDaysChange = { days ->
                scope.launch {
                    preferences.saveTasksReminderDays(days)

                    if (
                        isTasksReminderEnabled &&
                        permissionHelper.hasNotificationPermission() &&
                        permissionHelper.hasExactAlarmPermission()
                    ) {
                        alarmScheduler.scheduleTaskReminders()
                    }
                }
            },

            onWakeUpAlarmChange = { enabled ->
                scope.launch {
                    preferences.saveWakeUpAlarmEnabled(enabled)

                    if (!enabled) {
                        alarmScheduler.cancelWakeUpAlarms()
                        return@launch
                    }

                    if (!permissionHelper.hasNotificationPermission()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                        return@launch
                    }

                    if (!permissionHelper.hasExactAlarmPermission()) {
                        openExactAlarmSettings()
                        return@launch
                    }

                    alarmScheduler.scheduleWakeUpAlarms()
                }
            },

            onWakeUpOffsetChange = { minutes ->
                scope.launch {
                    preferences.saveWakeUpOffsetMinutes(minutes)

                    if (
                        isWakeUpAlarmEnabled &&
                        permissionHelper.hasNotificationPermission() &&
                        permissionHelper.hasExactAlarmPermission()
                    ) {
                        alarmScheduler.scheduleWakeUpAlarms()
                    }
                }
            }
        )
    }
}