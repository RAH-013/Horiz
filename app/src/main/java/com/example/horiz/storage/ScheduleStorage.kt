package com.example.horiz.storage

import android.content.Context
import android.util.Base64
import com.example.horiz.model.Schedule
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ScheduleStorage(context: Context) {

    private val dir = File(context.filesDir, "schedules").apply { mkdirs() }
    private val key = SecretKeySpec("HZSchedSecureKey".toByteArray().copyOf(16), "AES")
    private val prefs = context.getSharedPreferences("hzsch_prefs", Context.MODE_PRIVATE)

    init {
        ensureKingExists()
    }

    private fun enc(text: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.DEFAULT)
    }

    private fun dec(text: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        return String(cipher.doFinal(Base64.decode(text, Base64.DEFAULT)))
    }

    fun createSchedule(schedule: Schedule) {
        val file = File(dir, "${schedule.n}.hzsch")
        file.writeText(enc(schedule.serialize()))

        if (getKing() == null) {
            setKing(schedule.n)
        }
    }

    fun getSchedule(name: String): Schedule? {
        val file = File(dir, "$name.hzsch")
        if (!file.exists()) return null
        return Schedule.parse(dec(file.readText()))
    }

    fun getSchedules(): List<String> {
        return dir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun deleteSchedule(name: String) {
        File(dir, "$name.hzsch").delete()
        ensureKingExists()
    }


    fun removeAllSchedules() {
        val allSchedules = getSchedules()
        allSchedules.forEach { name ->
            deleteSchedule(name)
        }
    }

    fun setKing(name: String) {
        prefs.edit().putString("king_schedule", name).apply()
    }

    fun getKing(): String? {
        return prefs.getString("king_schedule", null)
    }

    // 🔍 DEBUG TEMPORAL
    fun debugFiles(): String {
        val sb = StringBuilder()

        dir.listFiles()?.forEach { file ->
            val size = file.length()
            val kb = size / 1024.0
            val content = runCatching { dec(file.readText()) }.getOrNull() ?: "ERROR AL LEER"

            sb.appendLine("📁 ${file.name}")
            sb.appendLine("Peso: %.2f KB (%d bytes)".format(kb, size))
            sb.appendLine("Contenido plano:")
            sb.appendLine(content)
            sb.appendLine("────────────────────")
        }

        return sb.toString()
    }

    private fun ensureKingExists() {
        val king = getKing()
        val files = getSchedules()

        if (files.isEmpty()) {
            createSchedule(Schedule("Horario", true))
            setKing("Horario")
            return
        }

        if (king == null) {
            setKing("Horario")
            return
        }

        if (king !in files) {
            setKing("Horario")
        }
    }
}