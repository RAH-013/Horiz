package com.horiz.ui.screens.subjects.components

import kotlin.random.Random

fun formatWords(
    text: String,
    capitalizeAll: Boolean = false
): String {
    val trimmed = text
        .trim()
        .replace(Regex("\\s+"), " ")

    if (trimmed.isBlank()) {
        return ""
    }

    return if (capitalizeAll) {
        trimmed
            .split(" ")
            .joinToString(" ") {
                it.replaceFirstChar { character ->
                    character.uppercaseChar()
                }
            }
    } else {
        trimmed.replaceFirstChar {
            it.uppercaseChar()
        }
    }
}

fun randColor(): Long {
    val red = Random.nextInt(120, 256)
    val green = Random.nextInt(120, 256)
    val blue = Random.nextInt(120, 256)

    return (
            (0xFF shl 24) or
                    (red shl 16) or
                    (green shl 8) or
                    blue
            ).toLong()
}

fun isValidSubjectText(text: String): Boolean {
    if (text.isBlank()) {
        return false
    }

    if (text != text.trim()) {
        return false
    }

    if (text.contains(Regex("\\s{2,}"))) {
        return false
    }

    return true
}

fun formatMinute(minute: Int): String {
    return "%02d:%02d".format(
        minute / 60,
        minute % 60
    )
}