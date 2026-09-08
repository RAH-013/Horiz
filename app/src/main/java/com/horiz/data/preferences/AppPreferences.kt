package com.horiz.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.horiz.ui.theme.AppTheme
import com.horiz.ui.theme.BaseColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "horiz_preferences"
)

class AppPreferences(
    private val context: Context
) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val BASE_COLOR = stringPreferencesKey("base_color")

        val ACTIVE_SCHEDULE_ID = longPreferencesKey("active_schedule_id")

        val CLASSES_REMINDER = booleanPreferencesKey("classes_reminder_enabled")
        val TASKS_REMINDER = booleanPreferencesKey("tasks_reminder_enabled")
        val TASKS_REMINDER_DAYS = intPreferencesKey("tasks_reminder_days")

        val WAKE_UP_ALARM = booleanPreferencesKey("wake_up_alarm_enabled")
        val WAKE_UP_OFFSET = intPreferencesKey("wake_up_offset_minutes")
    }

    val theme: Flow<AppTheme> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.THEME]
                ?.let { value ->
                    runCatching {
                        AppTheme.valueOf(value)
                    }.getOrNull()
                }
                ?: AppTheme.SYSTEM
        }

    val baseColor: Flow<BaseColor> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.BASE_COLOR]
                ?.let { value ->
                    runCatching {
                        BaseColor.valueOf(value)
                    }.getOrNull()
                }
                ?: BaseColor.PURPLE
        }

    val isClassesReminderEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.CLASSES_REMINDER] ?: false
        }

    val isTasksReminderEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.TASKS_REMINDER] ?: false
        }

    val tasksReminderDays: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.TASKS_REMINDER_DAYS] ?: 1
        }

    val isWakeUpAlarmEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.WAKE_UP_ALARM] ?: false
        }

    val wakeUpOffsetMinutes: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[Keys.WAKE_UP_OFFSET] ?: 90
        }

    suspend fun saveTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun saveBaseColor(baseColor: BaseColor) {
        context.dataStore.edit { preferences ->
            preferences[Keys.BASE_COLOR] = baseColor.name
        }
    }

    suspend fun saveClassesReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CLASSES_REMINDER] = enabled
        }
    }

    suspend fun saveTasksReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TASKS_REMINDER] = enabled
        }
    }

    suspend fun saveTasksReminderDays(days: Int) {
        require(days >= 0)

        context.dataStore.edit { preferences ->
            preferences[Keys.TASKS_REMINDER_DAYS] = days
        }
    }

    suspend fun saveWakeUpAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WAKE_UP_ALARM] = enabled
        }
    }

    suspend fun saveWakeUpOffsetMinutes(minutes: Int) {
        require(minutes > 0)

        context.dataStore.edit { preferences ->
            preferences[Keys.WAKE_UP_OFFSET] = minutes
        }
    }

    suspend fun saveActiveScheduleId(id: Long) {
        require(id > 0)

        context.dataStore.edit { preferences ->
            preferences[Keys.ACTIVE_SCHEDULE_ID] = id
        }
    }

    suspend fun getActiveScheduleId(): Long? {
        return context.dataStore.data
            .map { preferences ->
                preferences[Keys.ACTIVE_SCHEDULE_ID]
            }
            .first()
    }

    suspend fun clearActiveScheduleId() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.ACTIVE_SCHEDULE_ID)
        }
    }
}