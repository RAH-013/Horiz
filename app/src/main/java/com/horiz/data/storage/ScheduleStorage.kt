package com.horiz.storage

import android.content.Context
import android.util.Base64
import com.horiz.data.model.Schedule
import com.horiz.data.model.TaskNode
import java.io.File
import java.time.LocalDateTime
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ScheduleStorage(context: Context) {

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

    private val prefs =
        context.getSharedPreferences(
            "hzsch_prefs",
            Context.MODE_PRIVATE
        )

    init {
        ensureKingExists()
    }

    private fun encrypt(
        text: String
    ): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)

        return Base64.encodeToString(
            cipher.doFinal(
                text.toByteArray(Charsets.UTF_8)
            ),
            Base64.NO_WRAP
        )
    }

    private fun decrypt(
        text: String
    ): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)

        return String(
            cipher.doFinal(
                Base64.decode(
                    text,
                    Base64.NO_WRAP
                )
            ),
            Charsets.UTF_8
        )
    }

    private fun encode(
        value: String
    ): String {
        return Base64.encodeToString(
            value.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP or Base64.URL_SAFE
        )
    }

    private fun decode(
        value: String
    ): String {
        return String(
            Base64.decode(
                value,
                Base64.NO_WRAP or Base64.URL_SAFE
            ),
            Charsets.UTF_8
        )
    }

    fun createSchedule(
        schedule: Schedule
    ) {
        val file = File(
            dir,
            "${schedule.name}.hzsch"
        )

        file.writeText(
            encrypt(schedule.serialize()),
            Charsets.UTF_8
        )

        saveTasks(schedule)

        if (getKing() == null) {
            setKing(schedule.name)
        }
    }

    fun getSchedule(
        name: String
    ): Schedule? {
        val file = File(
            dir,
            "$name.hzsch"
        )

        if (!file.exists()) {
            return null
        }

        return runCatching {
            val schedule =
                Schedule.parse(
                    decrypt(
                        file.readText(
                            Charsets.UTF_8
                        )
                    )
                )

            loadTasks(
                schedule = schedule,
                name = name
            )

            schedule
        }.getOrNull()
    }

    fun getSchedules(): List<String> {
        return dir
            .listFiles()
            ?.filter {
                it.isFile &&
                        it.extension == "hzsch"
            }
            ?.map {
                it.nameWithoutExtension
            }
            ?.sorted()
            ?: emptyList()
    }

    fun deleteSchedule(
        name: String
    ) {
        File(
            dir,
            "$name.hzsch"
        ).delete()

        File(
            dir,
            "$name.hztasks"
        ).delete()

        if (getKing() == name) {
            prefs.edit()
                .remove("king_schedule")
                .apply()
        }

        ensureKingExists()
    }

    fun removeAllSchedules() {
        dir
            .listFiles()
            ?.filter {
                it.isFile &&
                        (
                                it.extension == "hzsch" ||
                                        it.extension == "hztasks"
                                )
            }
            ?.forEach {
                it.delete()
            }

        prefs.edit()
            .remove("king_schedule")
            .apply()

        ensureKingExists()
    }

    fun setKing(
        name: String
    ) {
        if (name !in getSchedules()) {
            return
        }

        prefs.edit()
            .putString(
                "king_schedule",
                name
            )
            .apply()
    }

    fun getKing(): String? {
        return prefs.getString(
            "king_schedule",
            null
        )
    }

    fun debugFiles(): String {
        val sb = StringBuilder()

        dir
            .listFiles()
            ?.filter {
                it.isFile &&
                        (
                                it.extension == "hzsch" ||
                                        it.extension == "hztasks"
                                )
            }
            ?.forEach { file ->
                val size = file.length()
                val kb = size / 1024.0

                val content = runCatching {
                    decrypt(
                        file.readText(
                            Charsets.UTF_8
                        )
                    )
                }.getOrElse {
                    "ERROR AL LEER"
                }

                sb.appendLine(
                    "Archivo: ${file.name}"
                )

                sb.appendLine(
                    "Peso: %.2f KB (%d bytes)".format(
                        kb,
                        size
                    )
                )

                sb.appendLine(
                    "Contenido plano:"
                )

                sb.appendLine(content)
                sb.appendLine(
                    "────────────────────"
                )
            }

        return sb.toString()
    }

    private fun saveTasks(
        schedule: Schedule
    ) {
        val file = File(
            dir,
            "${schedule.name}.hztasks"
        )

        if (schedule.tasks.isEmpty()) {
            file.delete()
            return
        }

        val content =
            schedule.tasks.joinToString("\n") { task ->
                buildString {
                    append(task.id)
                    append("|")
                    append(task.subjectId)
                    append("|")
                    append(encode(task.title))
                    append("|")
                    append(encode(task.description))
                    append("|")
                    append(
                        task.dueAt?.toString() ?: ""
                    )
                    append("|")
                    append(
                        if (task.completed) {
                            "1"
                        } else {
                            "0"
                        }
                    )
                    append("|")
                    append(task.orderIndex)
                }
            }

        file.writeText(
            encrypt(content),
            Charsets.UTF_8
        )
    }

    private fun loadTasks(
        schedule: Schedule,
        name: String
    ) {
        val file = File(
            dir,
            "$name.hztasks"
        )

        if (!file.exists()) {
            return
        }

        runCatching {
            decrypt(
                file.readText(
                    Charsets.UTF_8
                )
            )
        }.getOrNull()
            ?.lines()
            ?.filter {
                it.isNotBlank()
            }
            ?.forEach { line ->
                parseTask(
                    line = line,
                    schedule = schedule
                )
            }

        schedule.tasks.sortBy {
            it.orderIndex
        }
    }

    private fun parseTask(
        line: String,
        schedule: Schedule
    ) {
        val fields = line.split("|")

        if (fields.size < 6) {
            return
        }

        val id =
            fields[0].toLongOrNull()
                ?: return

        val storedReference =
            fields[1].toLongOrNull()
                ?: return

        val subjectId =
            resolveSubjectId(
                schedule = schedule,
                storedReference = storedReference
            ) ?: return

        val title =
            runCatching {
                decode(fields[2])
            }.getOrNull()
                ?: return

        val description =
            runCatching {
                decode(fields[3])
            }.getOrNull()
                ?: return

        val dueAt =
            if (fields[4].isBlank()) {
                null
            } else {
                runCatching {
                    LocalDateTime.parse(
                        fields[4]
                    )
                }.getOrNull()
            }

        val completed =
            fields[5] == "1"

        val orderIndex =
            fields
                .getOrNull(6)
                ?.toIntOrNull()
                ?: 0

        val task =
            runCatching {
                TaskNode(
                    id = id,
                    subjectId = subjectId,
                    title = title,
                    description = description,
                    dueAt = dueAt,
                    completed = completed,
                    orderIndex = orderIndex
                )
            }.getOrNull()
                ?: return

        schedule.addTask(task)
    }

    private fun resolveSubjectId(
        schedule: Schedule,
        storedReference: Long
    ): Long? {
        if (
            schedule.findSubject(
                storedReference
            ) != null
        ) {
            return storedReference
        }

        val entry =
            schedule.findEntry(
                storedReference
            )

        return entry?.subjectId
    }

    private fun ensureKingExists() {
        val schedules = getSchedules()

        if (schedules.isEmpty()) {
            val defaultSchedule =
                Schedule(
                    name = "Horario",
                    enabled = true
                )

            createSchedule(defaultSchedule)
            setKing(defaultSchedule.name)
            return
        }

        val king = getKing()

        if (
            king == null ||
            king !in schedules
        ) {
            setKing(schedules.first())
        }
    }
}