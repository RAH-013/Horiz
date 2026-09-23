package com.horiz.ui.screens.subjects.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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

public class FoldedCornerShape(
    private val cornerRadius: Dp,
    private val foldSize: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val radius = with(density) {
            cornerRadius.toPx()
        }

        val fold = with(density) {
            foldSize.toPx()
        }

        val path = Path().apply {
            moveTo(radius, 0f)

            lineTo(
                size.width - fold,
                0f
            )

            lineTo(
                size.width,
                fold
            )

            lineTo(
                size.width,
                size.height - radius
            )

            quadraticBezierTo(
                size.width,
                size.height,
                size.width - radius,
                size.height
            )

            lineTo(
                radius,
                size.height
            )

            quadraticBezierTo(
                0f,
                size.height,
                0f,
                size.height - radius
            )

            lineTo(
                0f,
                radius
            )

            quadraticBezierTo(
                0f,
                0f,
                radius,
                0f
            )

            close()
        }

        return Outline.Generic(path)
    }
}