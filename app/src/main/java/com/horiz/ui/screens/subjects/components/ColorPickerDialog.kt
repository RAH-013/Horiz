package com.horiz.ui.screens.subjects.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private val VSCodePresets = listOf(
    0xFFFF5555L,
    0xFFFFB86CL,
    0xFFF1FA8CL,
    0xFF50FA7BL,
    0xFF8BE9FDL,
    0xFFBD93F9L,
    0xFFFF79C6L,
    0xFF6272A4L
)

private fun colorToHsv(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue

    val maxVal = max(r, max(g, b))
    val minVal = min(r, min(g, b))
    val delta = maxVal - minVal

    var h = 0f
    if (delta != 0f) {
        h = when (maxVal) {
            r -> ((g - b) / delta) % 6f
            g -> ((b - r) / delta) + 2f
            else -> ((r - g) / delta) + 4f
        } * 60f
        if (h < 0) h += 360f
    }

    val s = if (maxVal == 0f) 0f else delta / maxVal
    val v = maxVal

    return floatArrayOf(h, s, v)
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1 - abs((h / 60f) % 2f - 1))
    val m = v - c

    val (r1, g1, b1) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = r1 + m,
        green = g1 + m,
        blue = b1 + m,
        alpha = 1f
    )
}

private fun parseHexToColor(hex: String): Color? {
    val cleanHex = hex.trim().removePrefix("#")
    if (cleanHex.length != 6) return null
    return try {
        val colorInt = cleanHex.toLong(16)
        Color((0xFF000000 or colorInt).toInt())
    } catch (_: Exception) {
        null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    initial: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val initialColor = Color(initial or 0xFF000000L)
    val initialHsv = remember(initial) { colorToHsv(initialColor) }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    val currentColor = remember(hue, saturation, value) {
        hsvToColor(hue, saturation, value)
    }

    val red = (currentColor.red * 255).toInt()
    val green = (currentColor.green * 255).toInt()
    val blue = (currentColor.blue * 255).toInt()

    var hexText by remember {
        mutableStateOf(String.format("#%02X%02X%02X", red, green, blue))
    }

    LaunchedEffect(currentColor) {
        val formatted = String.format("#%02X%02X%02X", red, green, blue)
        if (hexText.uppercase() != formatted) {
            hexText = formatted
        }
    }

    // Adaptación a los colores del tema base de Material 3
    val themeBackground = MaterialTheme.colorScheme.surface
    val themeBorder = MaterialTheme.colorScheme.outlineVariant
    val themeHeader = MaterialTheme.colorScheme.surfaceVariant
    val themeAccent = MaterialTheme.colorScheme.primary
    val themeOnSurface = MaterialTheme.colorScheme.onSurface
    val themeOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = themeBackground,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = themeAccent
                )
                Text(
                    text = "SELECTOR DE COLOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    color = themeOnSurface
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = themeHeader,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, themeBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentColor)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            BasicTextField(
                                value = hexText,
                                onValueChange = { input ->
                                    hexText = input
                                    val parsedColor = parseHexToColor(input)
                                    if (parsedColor != null) {
                                        val hsv = colorToHsv(parsedColor)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                    }
                                },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = themeOnSurface
                                ),
                                cursorBrush = SolidColor(themeAccent),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    .border(1.dp, themeBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )

                            Text(
                                text = "rgb($red, $green, $blue)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = themeOnSurfaceVariant,
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Color", hexText))
                                    Toast.makeText(context, "Copiado $hexText", Toast.LENGTH_SHORT).show()
                                }
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copiar HEX",
                                tint = themeOnSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SatValPanel(
                        hue = hue,
                        saturation = saturation,
                        value = value,
                        onSatValChanged = { newSat, newVal ->
                            saturation = newSat
                            value = newVal
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                    )

                    HueBar(
                        hue = hue,
                        onHueChanged = { hue = it },
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PRESETS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = themeOnSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        VSCodePresets.forEach { colorLong ->
                            val presetColor = Color(colorLong)
                            val isSelected = (red == (presetColor.red * 255).toInt()) &&
                                    (green == (presetColor.green * 255).toInt()) &&
                                    (blue == (presetColor.blue * 255).toInt())

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(presetColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = themeOnSurface
                                    )
                                    .clickable {
                                        val hsv = colorToHsv(presetColor)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                    }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalColorLong = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue
                    onColorSelected(finalColorLong.toLong() and 0xFFFFFFFFL)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "APLICAR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = themeOnSurface
                )
            ) {
                Text(
                    text = "CANCELAR",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    )
}

@Composable
private fun SatValPanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSatValChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueColor = remember(hue) { hsvToColor(hue, 1f, 1f) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val sat = (offset.x / size.width).coerceIn(0f, 1f)
                    val valVal = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onSatValChanged(sat, valVal)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val sat = (change.position.x / size.width).coerceIn(0f, 1f)
                    val valVal = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onSatValChanged(sat, valVal)
                }
            }
    ) {
        drawRect(color = pureHueColor)

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, Color.Transparent)
            )
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black)
            )
        )

        val selectorX = saturation * size.width
        val selectorY = (1f - value) * size.height

        drawCircle(
            color = Color.White,
            radius = 7.dp.toPx(),
            center = Offset(selectorX, selectorY),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = 8.dp.toPx(),
            center = Offset(selectorX, selectorY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
private fun HueBar(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainbowColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newHue = (offset.y / size.height).coerceIn(0f, 1f) * 360f
                    onHueChanged(newHue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val newHue = (change.position.y / size.height).coerceIn(0f, 1f) * 360f
                    onHueChanged(newHue)
                }
            }
    ) {
        drawRect(
            brush = Brush.verticalGradient(colors = rainbowColors)
        )

        val thumbY = (hue / 360f) * size.height
        val thumbHeight = 4.dp.toPx()

        drawRect(
            color = Color.White,
            topLeft = Offset(0f, thumbY - thumbHeight / 2),
            size = Size(size.width, thumbHeight)
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(0f, thumbY - thumbHeight / 2),
            size = Size(size.width, thumbHeight),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}