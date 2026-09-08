package com.horiz.ui.screens.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor

@Composable
fun SettingsContent(
    paddingValues: PaddingValues,
    theme: AppTheme,
    baseColor: BaseColor,
    isClassesReminderEnabled: Boolean,
    isTasksReminderEnabled: Boolean,
    tasksReminderDays: Int,
    isWakeUpAlarmEnabled: Boolean,
    wakeUpOffsetMinutes: Int,
    onThemeChange: (AppTheme) -> Unit,
    onBaseColorChange: (BaseColor) -> Unit,
    onClassesReminderChange: (Boolean) -> Unit,
    onTasksReminderChange: (Boolean) -> Unit,
    onTasksReminderDaysChange: (Int) -> Unit,
    onWakeUpAlarmChange: (Boolean) -> Unit,
    onWakeUpOffsetChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsSection(
            title = "Apariencia",
            icon = Icons.Rounded.Palette
        ) {
            AppearanceSelector(
                selected = theme,
                onSelected = onThemeChange
            )
        }

        SettingsSection(
            title = "Personalización",
            icon = Icons.Rounded.ColorLens
        ) {
            BaseColorSelector(
                selected = baseColor,
                onSelected = onBaseColorChange
            )
        }

        SettingsSection(
            title = "Notificaciones y alarmas",
            icon = Icons.Rounded.Notifications
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsSwitchOption(
                    title = "Recordatorio de materias",
                    description = "Recibe una notificación antes de cada materia.",
                    checked = isClassesReminderEnabled,
                    onCheckedChange = onClassesReminderChange
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                SettingsSwitchOption(
                    title = "Recordatorio de tareas",
                    description = "Recibe recordatorios de tus tareas pendientes.",
                    checked = isTasksReminderEnabled,
                    onCheckedChange = onTasksReminderChange
                )

                AnimatedVisibility(
                    visible = isTasksReminderEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsSliderOption(
                            title = "Días de anticipación",
                            description = "Anticipación para los recordatorios de tareas.",
                            value = tasksReminderDays,
                            valueRange = 1f..7f,
                            steps = 5,
                            valueLabel = if (tasksReminderDays == 1) "1 día" else "$tasksReminderDays días",
                            onValueChange = onTasksReminderDaysChange
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                SettingsSwitchOption(
                    title = "Despertador",
                    description = "Despiértate antes de tu primera materia.",
                    checked = isWakeUpAlarmEnabled,
                    onCheckedChange = onWakeUpAlarmChange
                )

                AnimatedVisibility(
                    visible = isWakeUpAlarmEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsSliderOption(
                            title = "Anticipación del despertador",
                            description = "Tiempo antes de tu primera materia.",
                            value = wakeUpOffsetMinutes,
                            valueRange = 15f..180f,
                            steps = 10,
                            valueLabel = "$wakeUpOffsetMinutes min",
                            onValueChange = onWakeUpOffsetChange
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}