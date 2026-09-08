package com.horiz.data.model

import android.util.Base64

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
        private const val LEGACY_FORMAT_VERSION = "HZ2"
        private const val OLDEST_FORMAT_VERSION = "HZ1"

        private const val DAYS_PER_WEEK = 7

        private const val LEGACY_SUBJECT_FIELDS = 6
        private const val LEGACY_SUBJECT_FIELDS_V2 = 7

        private const val FIELD_SEPARATOR = "|"
        private const val ENTRY_SEPARATOR = ","
        private const val VALUE_SEPARATOR = ";"

        private const val HZ3_ENTRY_FIELDS = 9
        private const val HZ3_LEGACY_ENTRY_FIELDS = 8

        private fun generateId(): Long {
            return System.nanoTime()
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

        fun parse(text: String): Schedule {
            val parts = text.split(FIELD_SEPARATOR)

            if (parts.isEmpty()) {
                throw IllegalArgumentException(
                    "Formato de schedule inválido"
                )
            }

            return when (parts[0]) {
                FORMAT_VERSION -> parseHz3(parts)

                LEGACY_FORMAT_VERSION,
                OLDEST_FORMAT_VERSION -> parseLegacy(parts)

                else -> {
                    throw IllegalArgumentException(
                        "Versión de schedule no soportada: ${parts[0]}"
                    )
                }
            }
        }

        private fun parseHz3(
            parts: List<String>
        ): Schedule {
            if (parts.size < 6 + DAYS_PER_WEEK) {
                throw IllegalArgumentException(
                    "Formato HZ3 inválido"
                )
            }

            val schedule = Schedule(
                name = decode(parts[1]),
                enabled = parts[2] == "1"
            )

            parseSubjects(parts[3], schedule)
            parseTeachers(parts[4], schedule)
            parseLocations(parts[5], schedule)

            for (dayIndex in 0 until DAYS_PER_WEEK) {
                parseDay(
                    serialized = parts[6 + dayIndex],
                    dayIndex = dayIndex,
                    schedule = schedule
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
                    val fields = item.split(ENTRY_SEPARATOR)

                    if (fields.size != 3) {
                        return@forEach
                    }

                    val id =
                        fields[0].toLongOrNull()
                            ?: return@forEach

                    val color =
                        fields[2].toLongOrNull()
                            ?: return@forEach

                    runCatching {
                        schedule.subjects.add(
                            Subject(
                                id = id,
                                name = decode(fields[1]),
                                color = color
                            )
                        )
                    }
                }
        }

        private fun parseTeachers(
            serialized: String,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            serialized
                .split(VALUE_SEPARATOR)
                .forEach { item ->
                    val fields = item.split(ENTRY_SEPARATOR)

                    if (fields.size != 2) {
                        return@forEach
                    }

                    val id =
                        fields[0].toLongOrNull()
                            ?: return@forEach

                    runCatching {
                        schedule.teachers.add(
                            Teacher(
                                id = id,
                                name = decode(fields[1])
                            )
                        )
                    }
                }
        }

        private fun parseLocations(
            serialized: String,
            schedule: Schedule
        ) {
            if (serialized.isEmpty()) return

            serialized
                .split(VALUE_SEPARATOR)
                .forEach { item ->
                    val fields = item.split(ENTRY_SEPARATOR)

                    if (fields.size != 2) {
                        return@forEach
                    }

                    val id =
                        fields[0].toLongOrNull()
                            ?: return@forEach

                    runCatching {
                        schedule.locations.add(
                            Location(
                                id = id,
                                name = decode(fields[1])
                            )
                        )
                    }
                }
        }

        private fun parseDay(
            serialized: String,
            dayIndex: Int,
            schedule: Schedule
        ) {
            val fields = serialized.split(ENTRY_SEPARATOR)

            if (fields.isEmpty()) return

            val day = schedule.days[dayIndex]

            day.setEnabled(fields[0] == "1")

            if (fields.size == 1) return

            val dataFields = fields.size - 1

            val fieldsPerEntry =
                when {
                    dataFields % HZ3_ENTRY_FIELDS == 0 ->
                        HZ3_ENTRY_FIELDS

                    dataFields % HZ3_LEGACY_ENTRY_FIELDS == 0 ->
                        HZ3_LEGACY_ENTRY_FIELDS

                    else ->
                        return
                }

            var position = 1

            while (
                position + fieldsPerEntry <= fields.size
            ) {
                val id =
                    fields[position]
                        .toLongOrNull()
                        ?: break

                val subjectId =
                    fields[position + 1]
                        .toLongOrNull()

                val teacherId =
                    fields[position + 2]
                        .toLongOrNull()

                val locationId =
                    fields[position + 3]
                        .toLongOrNull()

                val startMinute =
                    fields[position + 4]
                        .toIntOrNull()

                val endMinute =
                    fields[position + 5]
                        .toIntOrNull()

                val color =
                    fields[position + 6]
                        .toLongOrNull()

                val type =
                    runCatching {
                        SubjectType.valueOf(
                            fields[position + 7]
                        )
                    }.getOrDefault(
                        SubjectType.CLASS
                    )

                val name =
                    if (
                        fieldsPerEntry ==
                        HZ3_ENTRY_FIELDS
                    ) {
                        fields[position + 8]
                            .takeIf { it.isNotEmpty() }
                            ?.let { encoded ->
                                runCatching {
                                    decode(encoded)
                                }.getOrNull()
                            }
                    } else {
                        null
                    }

                if (
                    startMinute != null &&
                    endMinute != null &&
                    color != null
                ) {
                    runCatching {
                        day.entries.add(
                            ScheduleEntry(
                                id = id,
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
                                        type ==
                                        SubjectType.BREAK
                                    ) {
                                        name ?: "Recreo"
                                    } else {
                                        null
                                    }
                            )
                        )
                    }
                }

                position += fieldsPerEntry
            }

            day.entries.sortBy {
                it.startMinute
            }
        }

        private fun parseLegacy(
            parts: List<String>
        ): Schedule {
            if (parts.size < 3) {
                throw IllegalArgumentException(
                    "Formato legacy inválido"
                )
            }

            val schedule = Schedule(
                name = parts[1],
                enabled = parts[2] == "1"
            )

            val isHz2 =
                parts[0] == LEGACY_FORMAT_VERSION

            val fieldsPerSubject =
                if (isHz2) {
                    LEGACY_SUBJECT_FIELDS_V2
                } else {
                    LEGACY_SUBJECT_FIELDS
                }

            for (dayIndex in 0 until DAYS_PER_WEEK) {
                val position = 3 + dayIndex

                if (position >= parts.size) break

                val serializedDay = parts[position]

                if (serializedDay.isEmpty()) continue

                val fields =
                    serializedDay.split(ENTRY_SEPARATOR)

                if (fields.isEmpty()) continue

                val day = schedule.days[dayIndex]

                day.setEnabled(fields[0] == "1")

                var fieldPosition = 1

                while (
                    fieldPosition + fieldsPerSubject <=
                    fields.size
                ) {
                    val startMinute =
                        fields[fieldPosition]
                            .toIntOrNull()

                    val endMinute =
                        fields[fieldPosition + 1]
                            .toIntOrNull()

                    val subjectName =
                        fields[fieldPosition + 2]

                    val teacherName =
                        fields[fieldPosition + 3]

                    val locationName =
                        fields[fieldPosition + 4]

                    val color =
                        fields[fieldPosition + 5]
                            .toLongOrNull()

                    val type =
                        if (isHz2) {
                            runCatching {
                                SubjectType.valueOf(
                                    fields[fieldPosition + 6]
                                )
                            }.getOrDefault(
                                SubjectType.CLASS
                            )
                        } else {
                            SubjectType.CLASS
                        }

                    if (
                        startMinute != null &&
                        endMinute != null &&
                        color != null
                    ) {
                        val subject =
                            if (
                                type == SubjectType.CLASS &&
                                subjectName.isNotBlank()
                            ) {
                                findOrCreateSubject(
                                    schedule,
                                    subjectName,
                                    color
                                )
                            } else {
                                null
                            }

                        val teacher =
                            if (
                                type == SubjectType.CLASS &&
                                teacherName.isNotBlank()
                            ) {
                                findOrCreateTeacher(
                                    schedule,
                                    teacherName
                                )
                            } else {
                                null
                            }

                        val location =
                            if (
                                type == SubjectType.CLASS &&
                                locationName.isNotBlank()
                            ) {
                                findOrCreateLocation(
                                    schedule,
                                    locationName
                                )
                            } else {
                                null
                            }

                        if (
                            type == SubjectType.CLASS &&
                            subject == null
                        ) {
                            fieldPosition += fieldsPerSubject
                            continue
                        }

                        runCatching {
                            day.entries.add(
                                ScheduleEntry(
                                    id = generateId(),
                                    subjectId = subject?.id,
                                    teacherId = teacher?.id,
                                    locationId = location?.id,
                                    startMinute = startMinute,
                                    endMinute = endMinute,
                                    dayIndex = dayIndex,
                                    color = color,
                                    type = type,
                                    name =
                                        if (
                                            type ==
                                            SubjectType.BREAK
                                        ) {
                                            "Recreo"
                                        } else {
                                            null
                                        }
                                )
                            )
                        }
                    }

                    fieldPosition += fieldsPerSubject
                }

                day.entries.sortBy {
                    it.startMinute
                }
            }

            return schedule
        }

        private fun findOrCreateSubject(
            schedule: Schedule,
            name: String,
            color: Long
        ): Subject {
            return schedule.subjects.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            } ?: Subject(
                id = generateId(),
                name = name,
                color = color
            ).also {
                schedule.subjects.add(it)
            }
        }

        private fun findOrCreateTeacher(
            schedule: Schedule,
            name: String
        ): Teacher {
            return schedule.teachers.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            } ?: Teacher(
                id = generateId(),
                name = name
            ).also {
                schedule.teachers.add(it)
            }
        }

        private fun findOrCreateLocation(
            schedule: Schedule,
            name: String
        ): Location {
            return schedule.locations.firstOrNull {
                it.name.equals(
                    name,
                    ignoreCase = true
                )
            } ?: Location(
                id = generateId(),
                name = name
            ).also {
                schedule.locations.add(it)
            }
        }
    }

    fun serialize(): String {
        return buildString {
            append(FORMAT_VERSION)
            append(FIELD_SEPARATOR)
            append(encode(name))
            append(FIELD_SEPARATOR)
            append(if (enabled) "1" else "0")
            append(FIELD_SEPARATOR)

            append(
                subjects.joinToString(VALUE_SEPARATOR) {
                    "${it.id}$ENTRY_SEPARATOR" +
                            "${encode(it.name)}$ENTRY_SEPARATOR" +
                            it.color
                }
            )

            append(FIELD_SEPARATOR)

            append(
                teachers.joinToString(VALUE_SEPARATOR) {
                    "${it.id}$ENTRY_SEPARATOR" +
                            encode(it.name)
                }
            )

            append(FIELD_SEPARATOR)

            append(
                locations.joinToString(VALUE_SEPARATOR) {
                    "${it.id}$ENTRY_SEPARATOR" +
                            encode(it.name)
                }
            )

            days.forEach { day ->
                append(FIELD_SEPARATOR)

                append(if (day.enabled) "1" else "0")

                day.entries
                    .sortedBy { it.startMinute }
                    .forEach { entry ->
                        append(ENTRY_SEPARATOR)
                        append(entry.id)
                        append(ENTRY_SEPARATOR)
                        append(entry.subjectId ?: "")
                        append(ENTRY_SEPARATOR)
                        append(entry.teacherId ?: "")
                        append(ENTRY_SEPARATOR)
                        append(entry.locationId ?: "")
                        append(ENTRY_SEPARATOR)
                        append(entry.startMinute)
                        append(ENTRY_SEPARATOR)
                        append(entry.endMinute)
                        append(ENTRY_SEPARATOR)
                        append(entry.color)
                        append(ENTRY_SEPARATOR)
                        append(entry.type.name)
                        append(ENTRY_SEPARATOR)

                        append(
                            if (
                                entry.type ==
                                SubjectType.BREAK
                            ) {
                                encode(
                                    entry.name ?: "Recreo"
                                )
                            } else {
                                ""
                            }
                        )
                    }
            }
        }
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

    fun findTask(
        taskId: Long
    ): TaskNode? {
        return tasks.firstOrNull {
            it.id == taskId
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
        val entry = findEntry(scheduleEntryId)
            ?: return emptyList()

        val subjectId = entry.subjectId
            ?: return emptyList()

        return findTasksForSubject(subjectId)
    }

    fun addTask(
        task: TaskNode
    ): Boolean {
        if (findSubject(task.subjectId) == null) {
            return false
        }

        if (tasks.any { it.id == task.id }) {
            return false
        }

        tasks.add(task)
        return true
    }

    fun updateTask(
        task: TaskNode
    ): Boolean {
        val position = tasks.indexOfFirst {
            it.id == task.id
        }

        if (position == -1) {
            return false
        }

        if (findSubject(task.subjectId) == null) {
            return false
        }

        tasks[position] = task
        return true
    }

    fun removeTask(
        taskId: Long
    ): Boolean {
        return tasks.removeIf {
            it.id == taskId
        }
    }

    fun addEntry(
        entry: ScheduleEntry
    ): Boolean {
        val day =
            days.getOrNull(entry.dayIndex)
                ?: return false

        return day.addEntry(entry)
    }

    fun replaceEntry(
        entry: ScheduleEntry
    ): Boolean {
        val day =
            days.getOrNull(entry.dayIndex)
                ?: return false

        return day.replaceEntry(entry)
    }

    fun removeEntry(
        entryId: Long
    ): Boolean {
        val entry = findEntry(entryId)
            ?: return false

        val subjectId = entry.subjectId

        val removed = days.any {
            it.removeEntry(entryId)
        }

        if (removed && subjectId != null) {
            val subjectStillExists =
                days
                    .flatMap { it.entries }
                    .any {
                        it.type == SubjectType.CLASS &&
                                it.subjectId == subjectId
                    }

            if (!subjectStillExists) {
                tasks.removeAll {
                    it.subjectId == subjectId
                }
            }
        }

        return removed
    }

    fun clear() {
        days.forEach {
            it.clearEntries()
        }

        tasks.clear()
    }

    fun findOrCreateSubject(
        name: String,
        color: Long
    ): Subject {
        val existing = subjects.firstOrNull {
            it.name.equals(
                name,
                ignoreCase = true
            )
        }

        if (existing != null) {
            return existing
        }

        return Subject(
            id = System.nanoTime(),
            name = name,
            color = color
        ).also {
            subjects.add(it)
        }
    }

    fun findOrCreateTeacher(
        name: String
    ): Teacher {
        val existing = teachers.firstOrNull {
            it.name.equals(
                name,
                ignoreCase = true
            )
        }

        if (existing != null) {
            return existing
        }

        return Teacher(
            id = System.nanoTime(),
            name = name
        ).also {
            teachers.add(it)
        }
    }

    fun findOrCreateLocation(
        name: String
    ): Location {
        val existing = locations.firstOrNull {
            it.name.equals(
                name,
                ignoreCase = true
            )
        }

        if (existing != null) {
            return existing
        }

        return Location(
            id = System.nanoTime(),
            name = name
        ).also {
            locations.add(it)
        }
    }
}