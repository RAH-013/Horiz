package com.horiz.ui.screens.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.horiz.LauncherIconManager
import com.horiz.alarms.AlarmPermissionHelper
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.data.preferences.AppPreferences
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.settings.components.SettingsContent
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor
import com.horiz.widget.WidgetPromptManager
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

    var selectedBaseColor by remember(baseColor) {
        mutableStateOf(baseColor)
    }

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

    var pendingFeature by remember {
        mutableStateOf<PendingFeature?>(null)
    }

    fun getActivity(context: Context): Activity? {
        var current = context

        while (current is ContextWrapper) {
            if (current is Activity) {
                return current
            }

            current = current.baseContext
        }

        return null
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {
                    data = android.net.Uri.parse(
                        "package:${context.packageName}"
                    )
                }
            )
        }
    }

    fun hasRequiredPermissions(): Boolean {
        return permissionHelper.hasNotificationPermission() &&
                permissionHelper.hasExactAlarmPermission()
    }

    fun disableAlarmFeatures() {
        scope.launch {
            preferences.saveClassesReminderEnabled(false)
            preferences.saveTasksReminderEnabled(false)
            preferences.saveWakeUpAlarmEnabled(false)

            alarmScheduler.scheduleAll()
        }
    }

    fun scheduleEnabledAlarms() {
        if (!hasRequiredPermissions()) {
            disableAlarmFeatures()
            return
        }

        alarmScheduler.scheduleAll()
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                pendingFeature = null
                disableAlarmFeatures()
                return@rememberLauncherForActivityResult
            }

            if (!permissionHelper.hasExactAlarmPermission()) {
                openExactAlarmSettings()
                return@rememberLauncherForActivityResult
            }

            when (pendingFeature) {
                PendingFeature.CLASSES -> {
                    scope.launch {
                        preferences.saveClassesReminderEnabled(true)
                        alarmScheduler.scheduleAll()
                    }
                }

                PendingFeature.TASKS -> {
                    scope.launch {
                        preferences.saveTasksReminderEnabled(true)
                        alarmScheduler.scheduleAll()
                    }
                }

                PendingFeature.WAKE_UP -> {
                    scope.launch {
                        preferences.saveWakeUpAlarmEnabled(true)
                        alarmScheduler.scheduleAll()
                    }
                }

                null -> {
                    scheduleEnabledAlarms()
                }
            }

            pendingFeature = null
        }

    fun enableAlarmFeature(
        feature: PendingFeature
    ) {
        pendingFeature = feature

        if (!permissionHelper.hasNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

            return
        }

        if (!permissionHelper.hasExactAlarmPermission()) {
            openExactAlarmSettings()
            return
        }

        when (feature) {
            PendingFeature.CLASSES -> {
                scope.launch {
                    preferences.saveClassesReminderEnabled(true)
                    alarmScheduler.scheduleAll()
                }
            }

            PendingFeature.TASKS -> {
                scope.launch {
                    preferences.saveTasksReminderEnabled(true)
                    alarmScheduler.scheduleAll()
                }
            }

            PendingFeature.WAKE_UP -> {
                scope.launch {
                    preferences.saveWakeUpAlarmEnabled(true)
                    alarmScheduler.scheduleAll()
                }
            }
        }

        pendingFeature = null
    }

    fun validatePermissionsAfterResume() {
        if (!hasRequiredPermissions()) {
            pendingFeature = null
            disableAlarmFeatures()
            return
        }

        when (pendingFeature) {
            PendingFeature.CLASSES -> {
                scope.launch {
                    preferences.saveClassesReminderEnabled(true)
                    alarmScheduler.scheduleAll()
                }
                pendingFeature = null
            }

            PendingFeature.TASKS -> {
                scope.launch {
                    preferences.saveTasksReminderEnabled(true)
                    alarmScheduler.scheduleAll()
                }
                pendingFeature = null
            }

            PendingFeature.WAKE_UP -> {
                scope.launch {
                    preferences.saveWakeUpAlarmEnabled(true)
                    alarmScheduler.scheduleAll()
                }
                pendingFeature = null
            }

            null -> {
                scheduleEnabledAlarms()
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    validatePermissionsAfterResume()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        validatePermissionsAfterResume()
    }

    AppScreen(
        title = "Configuración",
        onBackClick = onBackClick
    ) { paddingValues ->

        SettingsContent(
            paddingValues = paddingValues,
            theme = theme,
            baseColor = selectedBaseColor,
            canApplyBaseColor = selectedBaseColor != baseColor,
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
                selectedBaseColor = newBaseColor
            },

            onApplyBaseColor = {
                if (selectedBaseColor != baseColor) {
                    scope.launch {
                        preferences.saveBaseColor(
                            selectedBaseColor
                        )

                        LauncherIconManager.update(
                            context = context,
                            baseColor = selectedBaseColor
                        )

                        getActivity(context)
                            ?.finishAndRemoveTask()
                    }
                }
            },

            onClassesReminderChange = { enabled ->
                if (enabled) {
                    enableAlarmFeature(
                        PendingFeature.CLASSES
                    )
                } else {
                    scope.launch {
                        preferences.saveClassesReminderEnabled(
                            false
                        )
                        alarmScheduler.scheduleAll()
                    }
                }
            },

            onTasksReminderChange = { enabled ->
                if (enabled) {
                    enableAlarmFeature(
                        PendingFeature.TASKS
                    )
                } else {
                    scope.launch {
                        preferences.saveTasksReminderEnabled(
                            false
                        )
                        alarmScheduler.scheduleAll()
                    }
                }
            },

            onTasksReminderDaysChange = { days ->
                scope.launch {
                    preferences.saveTasksReminderDays(
                        days
                    )

                    if (
                        isTasksReminderEnabled &&
                        hasRequiredPermissions()
                    ) {
                        alarmScheduler.scheduleAll()
                    }
                }
            },

            onWakeUpAlarmChange = { enabled ->
                if (enabled) {
                    enableAlarmFeature(
                        PendingFeature.WAKE_UP
                    )
                } else {
                    scope.launch {
                        preferences.saveWakeUpAlarmEnabled(
                            false
                        )
                        alarmScheduler.scheduleAll()
                    }
                }
            },

            onWakeUpOffsetChange = { minutes ->
                scope.launch {
                    preferences.saveWakeUpOffsetMinutes(
                        minutes
                    )

                    if (
                        isWakeUpAlarmEnabled &&
                        hasRequiredPermissions()
                    ) {
                        alarmScheduler.scheduleAll()
                    }
                }
            },

            onResetPreferences = {
                scope.launch {
                    preferences.saveTheme(AppTheme.SYSTEM)
                    preferences.saveBaseColor(BaseColor.PURPLE)
                    preferences.saveClassesReminderEnabled(false)
                    preferences.saveTasksReminderEnabled(false)
                    preferences.saveTasksReminderDays(1)
                    preferences.saveWakeUpAlarmEnabled(false)
                    preferences.saveWakeUpOffsetMinutes(90)

                    WidgetPromptManager.resetAllPromptPreferences(context)

                    selectedBaseColor = BaseColor.PURPLE
                    alarmScheduler.scheduleAll()
                }
            }
        )
    }
}

private enum class PendingFeature {
    CLASSES,
    TASKS,
    WAKE_UP
}