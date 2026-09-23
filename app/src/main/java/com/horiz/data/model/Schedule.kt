package com.horiz.data.model

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.zip.CRC32

data class Schedule(
    val name: String,
    var enabled: Boolean = true,
    val subjects: MutableList<Subject> = mutableListOf(),
    val teachers: MutableList<Teacher> = mutableListOf(),
    val locations: MutableList<Location> = mutableListOf(),
    val days: MutableList<DayNode> =
        MutableList(DAYS_PER_WEEK) { index ->
            DayNode(index = index)
        },
    val tasks: MutableList<TaskNode> = mutableListOf()
) {
    companion object {
        private const val FORMAT_VERSION = "HZ3"
        private const val DAYS_PER_WEEK = 7

        private const val FIELD_SEPARATOR = "|"
        private const val ENTRY_SEPARATOR = ","
        private const val VALUE_SEPARATOR = ";"

        private const val NULL_VALUE = "-"
        private const val CLASS_CODE = "C"
        private const val BREAK_CODE = "B"

        private const val QR_VERSION = 1
        private const val QR_CLASS = 0
        private const val QR_BREAK = 1
        private const val QR_NULL_INDEX = 0xFFFF

        private fun generateId(): Long =
            System.nanoTime()

        private fun generateUniqueId(
            usedIds: MutableSet<Long>
        ): Long {
            var id = System.nanoTime()

            while (id == 0L || !usedIds.add(id)) {
                id++
            }

            return id
        }

        private fun encode(value: String): String {
            return Base64.encodeToString(
                value.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP or Base64.URL_SAFE
            )
        }

        private fun decode(value: String): String {
            return String(
                Base64.decode(
                    value,
                    Base64.NO_WRAP or Base64.URL_SAFE
                ),
                Charsets.UTF_8
            )
        }

        private fun typeToCode(
            type: SubjectType
        ): String {
            return when (type) {
                SubjectType.CLASS -> CLASS_CODE
                SubjectType.BREAK -> BREAK_CODE
            }
        }

        private fun codeToType(
            code: String
        ): SubjectType {
            return when (code) {
                BREAK_CODE -> SubjectType.BREAK
                else -> SubjectType.CLASS
            }
        }

        fun parse(text: String): Schedule {
            val parts = text.split(
                FIELD_SEPARATOR,
                limit = 13
            )

            if (
                parts.size != 13 ||
                parts[0] != FORMAT_VERSION
            ) {
                throw IllegalArgumentException(
                    "Formato HZ3 inválido"
                )
            }

            val schedule = Schedule(
                name = decode(parts[1]),
                enabled = parts[2] == "1"
            )

            parseSubjects(
                parts[3],
                schedule
            )

            parseTeachers(
                parts[4],
                schedule
            )

            parseLocations(
                parts[5],
                schedule
            )

            for (dayIndex in 0 until DAYS_PER_WEEK) {
                parseDay(
                    parts[6 + dayIndex],
                    dayIndex,
                    schedule
                )
            }

            return schedule
        }

        private fun parseSubjects(
            serialized: String,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            serialized
                .split(VALUE_SEPARATOR)
                .forEach { item ->
                    val fields = item.split(
                        ENTRY_SEPARATOR,
                        limit = 2
                    )

                    if (fields.size != 2) return@forEach

                    val name =
                        runCatching {
                            decode(fields[0])
                        }.getOrNull()
                            ?: return@forEach

                    val color =
                        fields[1].toLongOrNull()
                            ?: return@forEach

                    schedule.subjects.add(
                        Subject(
                            id = generateId(),
                            name = name,
                            color = color
                        )
                    )
                }
        }

        private fun parseTeachers(
            serialized: String,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            serialized
                .split(VALUE_SEPARATOR)
                .forEach { value ->
                    val name =
                        runCatching {
                            decode(value)
                        }.getOrNull()
                            ?: return@forEach

                    schedule.teachers.add(
                        Teacher(
                            id = generateId(),
                            name = name
                        )
                    )
                }
        }

        private fun parseLocations(
            serialized: String,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            serialized
                .split(VALUE_SEPARATOR)
                .forEach { value ->
                    val name =
                        runCatching {
                            decode(value)
                        }.getOrNull()
                            ?: return@forEach

                    schedule.locations.add(
                        Location(
                            id = generateId(),
                            name = name
                        )
                    )
                }
        }

        private fun parseDay(
            serialized: String,
            dayIndex: Int,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            val fields = serialized.split(
                ENTRY_SEPARATOR
            )

            if (fields.isEmpty()) return

            val day = schedule.days[dayIndex]

            day.setEnabled(
                fields[0] == "1"
            )

            var position = 1

            while (position + 8 < fields.size) {
                val subjectName =
                    fields[position]
                        .takeUnless {
                            it == NULL_VALUE
                        }
                        ?.let {
                            runCatching {
                                decode(it)
                            }.getOrNull()
                        }

                val teacherName =
                    fields[position + 1]
                        .takeUnless {
                            it == NULL_VALUE
                        }
                        ?.let {
                            runCatching {
                                decode(it)
                            }.getOrNull()
                        }

                val locationName =
                    fields[position + 2]
                        .takeUnless {
                            it == NULL_VALUE
                        }
                        ?.let {
                            runCatching {
                                decode(it)
                            }.getOrNull()
                        }

                val startMinute =
                    fields[position + 3]
                        .toIntOrNull()

                val endMinute =
                    fields[position + 4]
                        .toIntOrNull()

                val color =
                    fields[position + 5]
                        .toLongOrNull()

                val type =
                    codeToType(
                        fields[position + 6]
                    )

                val entryName =
                    fields[position + 7]
                        .takeUnless {
                            it == NULL_VALUE
                        }
                        ?.let {
                            runCatching {
                                decode(it)
                            }.getOrNull()
                        }

                val subjectColor =
                    fields[position + 8]
                        .toLongOrNull()

                if (
                    startMinute == null ||
                    endMinute == null ||
                    color == null
                ) {
                    position += 9
                    continue
                }

                val subjectId =
                    if (
                        type == SubjectType.CLASS &&
                        !subjectName.isNullOrBlank()
                    ) {
                        val subject =
                            schedule.subjects.firstOrNull {
                                it.name.equals(
                                    subjectName,
                                    ignoreCase = true
                                )
                            }
                                ?: Subject(
                                    id = generateId(),
                                    name = subjectName,
                                    color =
                                        subjectColor
                                            ?: color
                                ).also {
                                    schedule.subjects.add(it)
                                }

                        subject.id
                    } else {
                        null
                    }

                val teacherId =
                    if (
                        type == SubjectType.CLASS &&
                        !teacherName.isNullOrBlank()
                    ) {
                        schedule.teachers
                            .firstOrNull {
                                it.name.equals(
                                    teacherName,
                                    ignoreCase = true
                                )
                            }
                            ?.id
                            ?: Teacher(
                                id = generateId(),
                                name = teacherName
                            ).also {
                                schedule.teachers.add(it)
                            }.id
                    } else {
                        null
                    }

                val locationId =
                    if (
                        type == SubjectType.CLASS &&
                        !locationName.isNullOrBlank()
                    ) {
                        schedule.locations
                            .firstOrNull {
                                it.name.equals(
                                    locationName,
                                    ignoreCase = true
                                )
                            }
                            ?.id
                            ?: Location(
                                id = generateId(),
                                name = locationName
                            ).also {
                                schedule.locations.add(it)
                            }.id
                    } else {
                        null
                    }

                day.entries.add(
                    ScheduleEntry(
                        id = generateId(),
                        subjectId = subjectId,
                        teacherId = teacherId,
                        locationId = locationId,
                        startMinute = startMinute,
                        endMinute = endMinute,
                        dayIndex = dayIndex,
                        color = color,
                        type = type,
                        name =
                            if (
                                type == SubjectType.BREAK
                            ) {
                                entryName ?: "Recreo"
                            } else {
                                null
                            }
                    )
                )

                position += 9
            }

            day.entries.sortBy {
                it.startMinute
            }
        }

        fun parseQr(data: ByteArray): Schedule {
            require(data.size >= 8) {
                "Datos HZ3 QR insuficientes"
            }

            val payloadSize =
                data.size - 4

            val payload =
                data.copyOfRange(
                    0,
                    payloadSize
                )

            val storedCrc =
                ((data[payloadSize].toInt() and 0xFF) shl 24) or
                        ((data[payloadSize + 1].toInt() and 0xFF) shl 16) or
                        ((data[payloadSize + 2].toInt() and 0xFF) shl 8) or
                        (data[payloadSize + 3].toInt() and 0xFF)

            val crc = CRC32()
            crc.update(payload)

            require(
                crc.value.toInt() == storedCrc
            ) {
                "El código QR está dañado"
            }

            val input =
                DataInputStream(
                    ByteArrayInputStream(payload)
                )

            val magic =
                ByteArray(3)

            input.readFully(magic)

            require(
                String(
                    magic,
                    Charsets.US_ASCII
                ) == FORMAT_VERSION
            ) {
                "Formato HZ3 QR inválido"
            }

            val version =
                input.readUnsignedByte()

            require(
                version == QR_VERSION
            ) {
                "Versión HZ3 QR no compatible"
            }

            val enabled =
                input.readBoolean()

            val name =
                readQrString(input)

            val usedIds =
                mutableSetOf<Long>()

            val subjects =
                readQrSubjects(
                    input,
                    usedIds
                )

            val teachers =
                readQrTeachers(
                    input,
                    usedIds
                )

            val locations =
                readQrLocations(
                    input,
                    usedIds
                )

            val schedule =
                Schedule(
                    name = name,
                    enabled = enabled,
                    subjects = subjects,
                    teachers = teachers,
                    locations = locations
                )

            for (dayIndex in 0 until DAYS_PER_WEEK) {
                readQrDay(
                    input = input,
                    dayIndex = dayIndex,
                    schedule = schedule,
                    usedIds = usedIds
                )
            }

            require(
                input.available() == 0
            ) {
                "Datos adicionales HZ3 QR inválidos"
            }

            return schedule
        }

        private fun readQrSubjects(
            input: DataInputStream,
            usedIds: MutableSet<Long>
        ): MutableList<Subject> {
            val count =
                input.readUnsignedShort()

            val subjects =
                ArrayList<Subject>(count)

            repeat(count) {
                val color =
                    input.readLong()

                val name =
                    readQrString(input)

                subjects.add(
                    Subject(
                        id = generateUniqueId(usedIds),
                        name = name,
                        color = color
                    )
                )
            }

            return subjects
        }

        private fun readQrTeachers(
            input: DataInputStream,
            usedIds: MutableSet<Long>
        ): MutableList<Teacher> {
            val count =
                input.readUnsignedShort()

            val teachers =
                ArrayList<Teacher>(count)

            repeat(count) {
                val name =
                    readQrString(input)

                teachers.add(
                    Teacher(
                        id = generateUniqueId(usedIds),
                        name = name
                    )
                )
            }

            return teachers
        }

        private fun readQrLocations(
            input: DataInputStream,
            usedIds: MutableSet<Long>
        ): MutableList<Location> {
            val count =
                input.readUnsignedShort()

            val locations =
                ArrayList<Location>(count)

            repeat(count) {
                val name =
                    readQrString(input)

                locations.add(
                    Location(
                        id = generateUniqueId(usedIds),
                        name = name
                    )
                )
            }

            return locations
        }

        private fun readQrDay(
            input: DataInputStream,
            dayIndex: Int,
            schedule: Schedule,
            usedIds: MutableSet<Long>
        ) {
            val dayEnabled =
                input.readBoolean()

            val entryCount =
                input.readUnsignedShort()

            val day =
                schedule.days[dayIndex]

            day.setEnabled(dayEnabled)

            repeat(entryCount) {

                val type =
                    input.readUnsignedByte()

                val startMinute =
                    input.readUnsignedShort()

                val endMinute =
                    input.readUnsignedShort()

                val color =
                    input.readLong()

                when (type) {

                    QR_CLASS -> {

                        val subjectIndex =
                            input.readUnsignedShort()

                        val teacherIndex =
                            input.readUnsignedShort()

                        val locationIndex =
                            input.readUnsignedShort()

                        val subject =
                            schedule.subjects.getOrNull(
                                subjectIndex
                            )
                                ?: throw IllegalArgumentException(
                                    "Materia HZ3 QR inexistente"
                                )

                        val teacherId =
                            if (
                                teacherIndex ==
                                QR_NULL_INDEX
                            ) {
                                null
                            } else {
                                schedule.teachers
                                    .getOrNull(
                                        teacherIndex
                                    )
                                    ?.id
                                    ?: throw IllegalArgumentException(
                                        "Profesor HZ3 QR inexistente"
                                    )
                            }

                        val locationId =
                            if (
                                locationIndex ==
                                QR_NULL_INDEX
                            ) {
                                null
                            } else {
                                schedule.locations
                                    .getOrNull(
                                        locationIndex
                                    )
                                    ?.id
                                    ?: throw IllegalArgumentException(
                                        "Ubicación HZ3 QR inexistente"
                                    )
                            }

                        day.entries.add(
                            ScheduleEntry(
                                id = generateUniqueId(
                                    usedIds
                                ),
                                subjectId =
                                    subject.id,
                                teacherId =
                                    teacherId,
                                locationId =
                                    locationId,
                                startMinute =
                                    startMinute,
                                endMinute =
                                    endMinute,
                                dayIndex =
                                    dayIndex,
                                color =
                                    color,
                                type =
                                    SubjectType.CLASS,
                                name = null
                            )
                        )
                    }

                    QR_BREAK -> {

                        val breakName =
                            readQrString(input)

                        require(
                            breakName.isNotBlank()
                        ) {
                            "Recreo HZ3 QR sin nombre"
                        }

                        day.entries.add(
                            ScheduleEntry(
                                id = generateUniqueId(
                                    usedIds
                                ),
                                subjectId = null,
                                teacherId = null,
                                locationId = null,
                                startMinute =
                                    startMinute,
                                endMinute =
                                    endMinute,
                                dayIndex =
                                    dayIndex,
                                color =
                                    color,
                                type =
                                    SubjectType.BREAK,
                                name =
                                    breakName
                            )
                        )
                    }

                    else -> {
                        throw IllegalArgumentException(
                            "Tipo de entrada HZ3 QR inválido"
                        )
                    }
                }
            }

            day.entries.sortBy {
                it.startMinute
            }
        }

        private fun readQrString(
            input: DataInputStream
        ): String {
            val length =
                input.readUnsignedShort()

            val bytes =
                ByteArray(length)

            input.readFully(bytes)

            return String(
                bytes,
                Charsets.UTF_8
            )
        }
    }

    fun serialize(): String {
        return buildString {
            append(FORMAT_VERSION)
            append(FIELD_SEPARATOR)

            append(encode(name))
            append(FIELD_SEPARATOR)

            append(
                if (enabled) "1" else "0"
            )
            append(FIELD_SEPARATOR)

            append(
                subjects.joinToString(
                    VALUE_SEPARATOR
                ) {
                    buildString {
                        append(encode(it.name))
                        append(ENTRY_SEPARATOR)
                        append(it.color)
                    }
                }
            )

            append(FIELD_SEPARATOR)

            append(
                teachers.joinToString(
                    VALUE_SEPARATOR
                ) {
                    encode(it.name)
                }
            )

            append(FIELD_SEPARATOR)

            append(
                locations.joinToString(
                    VALUE_SEPARATOR
                ) {
                    encode(it.name)
                }
            )

            days.forEach { day ->
                append(FIELD_SEPARATOR)

                append(
                    if (day.enabled) "1" else "0"
                )

                day.entries
                    .sortedBy {
                        it.startMinute
                    }
                    .forEach { entry ->
                        val subject =
                            entry.subjectId?.let {
                                findSubject(it)
                            }

                        val teacher =
                            entry.teacherId?.let {
                                findTeacher(it)
                            }

                        val location =
                            entry.locationId?.let {
                                findLocation(it)
                            }

                        append(ENTRY_SEPARATOR)

                        append(
                            if (subject != null) {
                                encode(subject.name)
                            } else {
                                NULL_VALUE
                            }
                        )

                        append(ENTRY_SEPARATOR)

                        append(
                            if (teacher != null) {
                                encode(teacher.name)
                            } else {
                                NULL_VALUE
                            }
                        )

                        append(ENTRY_SEPARATOR)

                        append(
                            if (location != null) {
                                encode(location.name)
                            } else {
                                NULL_VALUE
                            }
                        )

                        append(ENTRY_SEPARATOR)
                        append(entry.startMinute)

                        append(ENTRY_SEPARATOR)
                        append(entry.endMinute)

                        append(ENTRY_SEPARATOR)
                        append(entry.color)

                        append(ENTRY_SEPARATOR)
                        append(typeToCode(entry.type))

                        append(ENTRY_SEPARATOR)

                        if (
                            entry.type ==
                            SubjectType.BREAK
                        ) {
                            append(
                                encode(
                                    entry.name
                                        ?: "Recreo"
                                )
                            )
                        } else {
                            append(NULL_VALUE)
                        }

                        append(ENTRY_SEPARATOR)

                        append(
                            subject?.color
                                ?: entry.color
                        )
                    }
            }
        }
    }

    fun serializeForQr(): ByteArray {
        val payload =
            ByteArrayOutputStream()

        DataOutputStream(payload).use { output ->

            output.writeBytes(FORMAT_VERSION)
            output.writeByte(QR_VERSION)

            output.writeBoolean(enabled)

            writeQrString(
                output,
                name
            )

            require(subjects.size <= 65535)
            output.writeShort(subjects.size)

            for (subject in subjects) {
                output.writeLong(subject.color)

                writeQrString(
                    output,
                    subject.name
                )
            }

            require(teachers.size <= 65535)
            output.writeShort(teachers.size)

            for (teacher in teachers) {
                writeQrString(
                    output,
                    teacher.name
                )
            }

            require(locations.size <= 65535)
            output.writeShort(locations.size)

            for (location in locations) {
                writeQrString(
                    output,
                    location.name
                )
            }

            for (day in days.take(DAYS_PER_WEEK)) {

                output.writeBoolean(
                    day.enabled
                )

                val entries =
                    day.entries.sortedBy {
                        it.startMinute
                    }

                require(entries.size <= 65535)

                output.writeShort(
                    entries.size
                )

                for (entry in entries) {

                    output.writeByte(
                        when (entry.type) {
                            SubjectType.CLASS ->
                                QR_CLASS

                            SubjectType.BREAK ->
                                QR_BREAK
                        }
                    )

                    require(
                        entry.startMinute in 0..1439
                    )

                    require(
                        entry.endMinute in 1..1440
                    )

                    output.writeShort(
                        entry.startMinute
                    )

                    output.writeShort(
                        entry.endMinute
                    )

                    output.writeLong(
                        entry.color
                    )

                    when (entry.type) {

                        SubjectType.CLASS -> {

                            val subjectIndex =
                                subjects.indexOfFirst {
                                    it.id ==
                                            entry.subjectId
                                }

                            require(
                                subjectIndex >= 0
                            ) {
                                "La materia de una entrada HZ3 no existe"
                            }

                            output.writeShort(
                                subjectIndex
                            )

                            val teacherIndex =
                                teachers.indexOfFirst {
                                    it.id ==
                                            entry.teacherId
                                }

                            output.writeShort(
                                if (
                                    teacherIndex >= 0
                                ) {
                                    teacherIndex
                                } else {
                                    QR_NULL_INDEX
                                }
                            )

                            val locationIndex =
                                locations.indexOfFirst {
                                    it.id ==
                                            entry.locationId
                                }

                            output.writeShort(
                                if (
                                    locationIndex >= 0
                                ) {
                                    locationIndex
                                } else {
                                    QR_NULL_INDEX
                                }
                            )
                        }

                        SubjectType.BREAK -> {
                            writeQrString(
                                output,
                                entry.name
                                    ?: "Recreo"
                            )
                        }
                    }
                }
            }
        }

        val bytes =
            payload.toByteArray()

        val crc =
            CRC32().apply {
                update(bytes)
            }

        val result =
            ByteArrayOutputStream()

        DataOutputStream(result).use { output ->
            output.write(bytes)
            output.writeInt(
                crc.value.toInt()
            )
        }

        return result.toByteArray()
    }

    private fun writeQrString(
        output: DataOutputStream,
        value: String
    ) {
        val bytes =
            value.toByteArray(
                Charsets.UTF_8
            )

        require(
            bytes.size <= 65535
        ) {
            "Texto HZ3 demasiado largo"
        }

        output.writeShort(
            bytes.size
        )

        output.write(bytes)
    }

    fun findSubject(
        subjectId: Long
    ): Subject? {
        return subjects.firstOrNull {
            it.id == subjectId
        }
    }

    fun findTeacher(
        teacherId: Long
    ): Teacher? {
        return teachers.firstOrNull {
            it.id == teacherId
        }
    }

    fun findLocation(
        locationId: Long
    ): Location? {
        return locations.firstOrNull {
            it.id == locationId
        }
    }

    fun findEntry(
        entryId: Long
    ): ScheduleEntry? {
        return days
            .asSequence()
            .flatMap {
                it.entries.asSequence()
            }
            .firstOrNull {
                it.id == entryId
            }
    }

    fun findTasksForSubject(
        subjectId: Long
    ): List<TaskNode> {
        return tasks
            .filter {
                it.subjectId == subjectId
            }
            .sortedBy {
                it.orderIndex
            }
    }

    fun findTasksForEntry(
        scheduleEntryId: Long
    ): List<TaskNode> {
        val entry =
            findEntry(scheduleEntryId)
                ?: return emptyList()

        val subjectId =
            entry.subjectId
                ?: return emptyList()

        return findTasksForSubject(
            subjectId
        )
    }

    fun addTask(
        task: TaskNode
    ): Boolean {
        if (
            findSubject(task.subjectId) == null
        ) {
            return false
        }

        if (
            tasks.any {
                it.id == task.id
            }
        ) {
            return false
        }

        tasks.add(task)
        return true
    }

    fun removeEntry(
        entryId: Long
    ): Boolean {
        findEntry(entryId)
            ?: return false

        val removed =
            days.any {
                it.removeEntry(entryId)
            }

        if (!removed) {
            return false
        }

        cleanupUnusedResources()

        return true
    }

    private fun cleanupUnusedResources() {
        val activeEntries =
            days
                .flatMap {
                    it.entries
                }
                .filter {
                    it.type == SubjectType.CLASS
                }

        val usedSubjectIds =
            activeEntries
                .mapNotNull {
                    it.subjectId
                }
                .toSet()

        val usedTeacherIds =
            activeEntries
                .mapNotNull {
                    it.teacherId
                }
                .toSet()

        val usedLocationIds =
            activeEntries
                .mapNotNull {
                    it.locationId
                }
                .toSet()

        subjects.removeAll {
            it.id !in usedSubjectIds
        }

        teachers.removeAll {
            it.id !in usedTeacherIds
        }

        locations.removeAll {
            it.id !in usedLocationIds
        }

        tasks.removeAll {
            it.subjectId !in usedSubjectIds
        }
    }

    fun findOrCreateSubject(
        name: String,
        color: Long
    ): Subject {
        val existing =
            subjects.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            }

        if (existing != null) {
            existing.color = color
            return existing
        }

        return Subject(
            id = generateId(),
            name = name,
            color = color
        ).also {
            subjects.add(it)
        }
    }

    fun findOrCreateTeacher(
        name: String
    ): Teacher {
        val existing =
            teachers.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            }

        if (existing != null) {
            return existing
        }

        return Teacher(
            id = generateId(),
            name = name
        ).also {
            teachers.add(it)
        }
    }

    fun findOrCreateLocation(
        name: String
    ): Location {
        val existing =
            locations.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            }

        if (existing != null) {
            return existing
        }

        return Location(
            id = generateId(),
            name = name
        ).also {
            locations.add(it)
        }
    }
}