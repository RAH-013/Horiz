package com.horiz.ui.screens.subjects.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.luminance
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

data class PresetCategory(
    val name: String,
    val colors: List<Long>
)

private val ColorPresetCategories = listOf(
    PresetCategory(
        name = "Vibrantes",
        colors = listOf(
            0xFFFF5555L, 0xFFFFB86CL, 0xFFF1FA8CL, 0xFF50FA7BL,
            0xFF8BE9FDL, 0xFFBD93F9L, 0xFFFF79C6L, 0xFF6272A4L
        )
    ),
    PresetCategory(
        name = "Pastel",
        colors = listOf(
            0xFFFFB3BAL, 0xFFFFDFBAL, 0xFFFFFFC5L, 0xFFBAFFC9L,
            0xFFBAE1FFL, 0xFFE8AEFFL, 0xFFD5AAFFL, 0xFFC5A3FFL
        )
    ),
    PresetCategory(
        name = "Neón",
        colors = listOf(
            0xFFFF007FL, 0xFF00F0FFL, 0xFF00FF66L, 0xFFFFE600L,
            0xFFBF00FFL, 0xFFFF3300L, 0xFF0033FFL, 0xFF33FF00L
        )
    ),
    PresetCategory(
        name = "Cálidos",
        colors = listOf(
            0xFF8D5B4CL, 0xFFD4A373L, 0xFFFAEDCDL, 0xFFE9EDC9L,
            0xFFCCD5AEL, 0xFFBC6C25L, 0xFFDDA15EL, 0xFF606C38L
        )
    ),
    PresetCategory(
        name = "Oscuros",
        colors = listOf(
            0xFF1E1E2EL, 0xFF282A36L, 0xFF181825L, 0xFF0F172AL,
            0xFF1F2937L, 0xFF1A1B26L, 0xFF2D1B69L, 0xFF1E293BL
        )
    ),
    PresetCategory(
        name = "Monocromo",
        colors = listOf(
            0xFF121212L, 0xFF2D2D2DL, 0xFF4A4A4AL, 0xFF717171L,
            0xFF9E9E9EL, 0xFFC4C4C4L, 0xFFE0E0E0L, 0xFFF5F5F5L
        )
    )
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

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(currentColor) {
        val formatted = String.format("#%02X%02X%02X", red, green, blue)
        if (hexText.uppercase() != formatted) {
            hexText = formatted
        }
    }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    color = themeHeader,
                    shape = RoundedCornerShape(14.dp),
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(currentColor)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        )

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
                                .weight(1f)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, themeBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )

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
                        .height(170.dp),
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
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, themeBorder, RoundedCornerShape(14.dp))
                    )

                    HueBar(
                        hue = hue,
                        onHueChanged = { hue = it },
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, themeBorder, RoundedCornerShape(14.dp))
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(ColorPresetCategories) { index, category ->
                            FilterChip(
                                selected = selectedCategoryIndex == index,
                                onClick = { selectedCategoryIndex = index },
                                label = {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeAccent,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    val currentCategory = ColorPresetCategories[selectedCategoryIndex]

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentCategory.colors.forEach { colorLong ->
                            val presetColor = Color(colorLong or 0xFF000000L)
                            val isSelected = (red == (presetColor.red * 255).toInt()) &&
                                    (green == (presetColor.green * 255).toInt()) &&
                                    (blue == (presetColor.blue * 255).toInt())

                            val contentColor = if (presetColor.luminance() > 0.5f) Color.Black else Color.White

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(presetColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) themeOnSurface else Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        val hsv = colorToHsv(presetColor)
                                        hue = hsv[0]
                                        saturation = hsv[1]
                                        value = hsv[2]
                                    }
                            ) {
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
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