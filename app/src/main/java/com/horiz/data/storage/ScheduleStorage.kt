package com.horiz.storage

import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.glance.appwidget.updateAll
import com.horiz.data.model.Schedule
import com.horiz.data.model.TaskNode
import com.horiz.data.model.TaskPriority
import com.horiz.widget.TodayScheduleWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ScheduleStorage(
    context: Context
) {

    private val context = context.applicationContext

    private val dir = File(
        context.filesDir,
        "schedules"
    ).apply {
        mkdirs()
    }

    private val key = SecretKeySpec(
        "HZSchedSecureKey".toByteArray().copyOf(16),
        "AES"
    )

    private val prefs = context.getSharedPreferences(
        "hzsch_prefs",
        Context.MODE_PRIVATE
    )

    init {
        ensureKingExists()
    }

    private fun encrypt(text: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(
            cipher.doFinal(text.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )
    }

    private fun decrypt(text: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(
            cipher.doFinal(Base64.decode(text, Base64.NO_WRAP)),
            Charsets.UTF_8
        )
    }

    private fun encode(value: String): String {
        return Base64.encodeToString(
            value.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }

    private fun decode(value: String): String {
        return String(
            Base64.decode(value, Base64.NO_WRAP or Base64.URL_SAFE),
            Charsets.UTF_8
        )
    }

    private fun notifyScheduleChanged() {
        prefs.edit()
            .putLong("schedule_version", System.currentTimeMillis())
            .apply()

        context.sendBroadcast(
            Intent(ACTION_SCHEDULE_CHANGED).setPackage(context.packageName)
        )

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                TodayScheduleWidget().updateAll(context)
            }
        }
    }

    fun createSchedule(schedule: Schedule) {
        val file = File(dir, "${schedule.name}.hzsch")

        file.writeText(
            encrypt(schedule.serialize()),
            Charsets.UTF_8
        )

        saveTasks(schedule)

        if (getKing() == null) {
            setKing(schedule.name)
        }

        notifyScheduleChanged()
    }

    fun getSchedule(name: String): Schedule? {
        val file = File(dir, "$name.hzsch")

        if (!file.exists()) {
            return null
        }

        return runCatching {
            val schedule = Schedule.parse(
                decrypt(file.readText(Charsets.UTF_8))
            )

            loadTasks(schedule = schedule, name = name)
            schedule
        }.getOrNull()
    }

    fun getSchedules(): List<String> {
        return dir.listFiles()
            ?.filter { it.isFile && it.extension == "hzsch" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    fun deleteSchedule(name: String) {
        File(dir, "$name.hzsch").delete()
        File(dir, "$name.hztasks").delete()

        if (getKing() == name) {
            prefs.edit().remove("king_schedule").apply()
        }

        ensureKingExists()
        notifyScheduleChanged()
    }

    fun setKing(name: String) {
        if (name !in getSchedules()) {
            return
        }

        prefs.edit()
            .putString("king_schedule", name)
            .apply()

        notifyScheduleChanged()
    }

    fun getKing(): String? {
        return prefs.getString("king_schedule", null)
    }

    fun getScheduleVersion(): Long {
        return prefs.getLong("schedule_version", 0L)
    }

    fun getPendingTasksCountBySubject(scheduleName: String? = getKing()): Map<String, Int> {
        if (scheduleName == null) return emptyMap()
        val schedule = getSchedule(scheduleName) ?: return emptyMap()

        val counts = mutableMapOf<String, Int>()
        val pendingTasks = schedule.tasks.filter { !it.completed }

        for (task in pendingTasks) {
            val idKey = task.subjectId.toString()
            counts[idKey] = (counts[idKey] ?: 0) + 1

            val subject = schedule.findSubject(task.subjectId)
            if (subject != null) {
                val nameKey = subject.name.lowercase().trim()
                counts[nameKey] = (counts[nameKey] ?: 0) + 1
            }
        }

        return counts
    }

    fun saveTasks(schedule: Schedule) {
        val file = File(dir, "${schedule.name}.hztasks")

        if (schedule.tasks.isEmpty()) {
            file.delete()
            notifyScheduleChanged()
            return
        }

        val content = schedule.tasks.joinToString("\n") { task ->
            val subjectName = schedule.findSubject(task.subjectId)?.name ?: ""

            buildString {
                append(task.id)
                append("|")
                append(encode(subjectName))
                append("|")
                append(encode(task.title))
                append("|")
                append(encode(task.description))
                append("|")
                append(task.dueAt?.toString() ?: "")
                append("|")
                append(if (task.completed) "1" else "0")
                append("|")
                append(task.orderIndex)
                append("|")
                append(task.priority.name)
            }
        }

        file.writeText(
            encrypt(content),
            Charsets.UTF_8
        )

        notifyScheduleChanged()
    }

    private fun loadTasks(schedule: Schedule, name: String) {
        val file = File(dir, "$name.hztasks")

        if (!file.exists()) {
            return
        }

        runCatching {
            decrypt(file.readText(Charsets.UTF_8))
        }.getOrNull()
            ?.lines()
            ?.filter { it.isNotBlank() }
            ?.forEach { line ->
                parseTask(line = line, schedule = schedule)
            }

        schedule.tasks.sortBy { it.orderIndex }
    }

    private fun parseTask(line: String, schedule: Schedule) {
        val fields = line.split("|")

        if (fields.size < 6) {
            return
        }

        val id = fields[0].toLongOrNull() ?: return
        val refField = fields[1]
        val directId = refField.toLongOrNull()

        val subjectId = if (directId != null) {
            resolveLegacySubjectId(schedule, directId) ?: directId
        } else {
            val decodedName = runCatching { decode(refField) }.getOrNull()
            if (decodedName != null) {
                schedule.subjects.firstOrNull {
                    it.name.equals(decodedName, ignoreCase = true)
                }?.id
            } else null
        }

        val finalSubjectId = subjectId ?: 0L

        val titleIndex = 2
        val descriptionIndex = 3
        val dueAtIndex = 4
        val completedIndex = 5

        val orderIndexField = fields.getOrNull(6)?.toIntOrNull() ?: 0

        val priority = fields.getOrNull(7)?.let {
            runCatching { TaskPriority.valueOf(it) }.getOrNull()
        } ?: TaskPriority.LOW

        val title = runCatching { decode(fields[titleIndex]) }.getOrNull() ?: return
        val description = runCatching { decode(fields[descriptionIndex]) }.getOrNull() ?: return

        val dueAt = if (fields[dueAtIndex].isBlank()) {
            null
        } else {
            runCatching { LocalDateTime.parse(fields[dueAtIndex]) }.getOrNull()
        }

        val completed = fields[completedIndex] == "1"

        val task = TaskNode(
            id = id,
            subjectId = finalSubjectId,
            title = title,
            description = description,
            dueAt = dueAt,
            completed = completed,
            orderIndex = orderIndexField,
            priority = priority
        )

        schedule.addTask(task)
    }

    private fun resolveLegacySubjectId(
        schedule: Schedule,
        storedReference: Long
    ): Long? {
        if (schedule.findSubject(storedReference) != null) {
            return storedReference
        }

        val entry = schedule.findEntry(storedReference)
        return entry?.subjectId
    }

    private fun ensureKingExists() {
        val schedules = getSchedules()

        if (schedules.isEmpty()) {
            val defaultSchedule = Schedule(
                name = "Horario",
                enabled = true
            )
            createSchedule(defaultSchedule)
            setKing(defaultSchedule.name)
            return
        }

        val king = getKing()

        if (king == null || king !in schedules) {
            setKing(schedules.first())
        }
    }

    companion object {
        private const val ACTION_SCHEDULE_CHANGED = "com.horiz.ACTION_SCHEDULE_CHANGED"
    }
}