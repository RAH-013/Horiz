package com.example.horiz.model

data class Schedule(
    val n: String,
    var a: Boolean,
    val e: MutableList<DayNode> = MutableList(7) { index -> DayNode( index + 1) }
) {
    fun serialize(): String {
        return buildString {
            append("HZ1|$n|${if (a) 1 else 0}")
            e.forEach { day ->
                append("|${if (day.a) 1 else 0}")
                day.e.forEach { subj ->
                    append(",${subj.s},${subj.f},${subj.n},${subj.t},${subj.p},${subj.c}")
                }
            }
        }
    }

    companion object {
        fun parse(text: String): Schedule {
            val parts = text.split("|")
            if (parts.size < 10) throw IllegalArgumentException("Formato de schedule inválido")

            val schedule = Schedule(parts[1], parts[2] == "1")

            for (i in 0 until 7) {
                val dayParts = parts[3 + i].split(",")
                if (dayParts.isEmpty()) continue

                val day = schedule.e[i]
                day.a = dayParts[0] == "1"

                var j = 1
                while (j + 5 < dayParts.size) {
                    try {
                        day.e.add(
                            SubjectNode(
                                i = day.e.size + 1,
                                s = dayParts[j].toInt(),
                                f = dayParts[j + 1].toInt(),
                                n = dayParts[j + 2],
                                t = dayParts[j + 3],
                                p = dayParts[j + 4],
                                c = dayParts[j + 5].toLong(),
                                d = i
                            )
                        )
                    } catch (_: Exception) {
                    }
                    j += 6
                }
            }

            return schedule
        }
    }
}