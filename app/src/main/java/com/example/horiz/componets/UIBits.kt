package com.example.horiz.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.horiz.R
import com.example.horiz.model.DayNode
import com.example.horiz.model.SubjectNode
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import com.example.horiz.activities.DataActivity
import java.util.Calendar

@Composable
fun AddFab(dayNode: DayNode?, enabled: Boolean, onClick: () -> Unit) {
    if (!enabled) return
    val ctx = LocalContext.current

    val isFull = dayNode?.e?.size ?: 0 >= 10
    val fabColor = if (isFull) Color.Gray else Color(0xFF4CAF50)

    FloatingActionButton(
        onClick = {
            if (isFull) {
                Toast.makeText(ctx, "Se ha alcanzado la capacidad máxima de 10 materias", Toast.LENGTH_SHORT).show()
            } else {
                onClick()
            }
        },
        containerColor = fabColor,
        contentColor = Color.White,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}
@Composable
fun EmptyDayView(enabled: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (enabled) {
            Text("Sin materias", fontSize = 16.sp)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.sleep),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Día deshabilitado", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrudTopBar(
    title: String,
    enabled: Boolean,
    onBack: () -> Unit,
    onToggle: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title, color = Color.White, fontSize = 22.sp) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (enabled) "ON" else "OFF",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = { onToggle() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6A1B9A))
    )
}

@Composable
fun DaySelector(
    days: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val scroll = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF7B1FA2))
            .padding(vertical = 20.dp)
            .navigationBarsPadding()
            .horizontalScroll(scroll),
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.width(16.dp))

        days.forEachIndexed { i, d ->
            Text(
                text = d,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .clickable { onSelect(i) },
                color = if (i == selected) Color.White else Color.White.copy(alpha = .4f)
            )
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
fun DeleteDialog(
    item: SubjectNode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Materia") },
        text = { Text("¿Deseas eliminar \"${item.n}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SecretAccessText(secretCode: String = "2004") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lastClickTime by remember { mutableStateOf(0L) }
    var showDialog by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(16.dp)
            .clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 400) {
                    showDialog = true
                    input = ""
                    error = false
                }
                lastClickTime = currentTime
            }
    ) {
        Text(
            text = "RAH-013 / ©$currentYear",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (input == secretCode) {
                        context.startActivity(Intent(context, DataActivity::class.java))
                        showDialog = false
                    } else {
                        error = true
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Ingrese el código") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { value ->
                        if (value.length <= 4 && value.all { it.isDigit() }) input = value
                    },
                    singleLine = true,
                    isError = error
                )
            }
        )
    }
}
