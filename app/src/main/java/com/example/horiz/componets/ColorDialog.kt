package com.example.horiz.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ColorPickerDialogComponent(
    initial: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var r by remember { mutableStateOf(((initial shr 16) and 0xFF).toInt()) }
    var g by remember { mutableStateOf(((initial shr 8) and 0xFF).toInt()) }
    var b by remember { mutableStateOf((initial and 0xFF).toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar color") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = r.toFloat(),
                        onValueChange = { r = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("R: $r", modifier = Modifier.width(50.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = g.toFloat(),
                        onValueChange = { g = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("G: $g", modifier = Modifier.width(50.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = b.toFloat(),
                        onValueChange = { b = it.toInt() },
                        valueRange = 0f..255f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("B: $b", modifier = Modifier.width(50.dp))
                }
                Box(
                    Modifier
                        .size(50.dp)
                        .background(Color(r, g, b))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected((0xFF shl 24 or (r shl 16) or (g shl 8) or b).toLong()) }) { Text("Ok") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

fun randColor(): Long {
    val r = Random.nextInt(120, 256)
    val g = Random.nextInt(120, 256)
    val b = Random.nextInt(120, 256)
    return ((0xFF shl 24) or (r shl 16) or (g shl 8) or b).toLong()
}