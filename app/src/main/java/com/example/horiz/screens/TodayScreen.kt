package com.example.horiz.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.horiz.components.EmptyDayView
import com.example.horiz.model.SubjectNode
import com.example.horiz.model.Schedule
import com.example.horiz.storage.ScheduleStorage
import com.example.horiz.utils.getMillisUntilNextChange
import com.example.horiz.utils.scheduleClassAlarms
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(onBack: () -> Unit) {

    val ctx = LocalContext.current
    val storage = remember { ScheduleStorage(ctx) }

    var kingName by remember { mutableStateOf(storage.getKing()) }
    var schedule by remember { mutableStateOf<Schedule?>(storage.getSchedule(kingName ?: "")) }

    val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    val dayNode = schedule?.e?.getOrNull(todayIndex)
    val subjects = dayNode?.e ?: emptyList()

    var nowMinutes by remember {
        mutableStateOf(Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) })
    }

    val activeSubjects = subjects.filter { it.d == todayIndex }.sortedBy { it.s }

    LaunchedEffect(activeSubjects, nowMinutes) {
        if (activeSubjects.isNotEmpty()) {
            val delayMillis = getMillisUntilNextChange(activeSubjects, nowMinutes)
            if (delayMillis != Long.MAX_VALUE) {
                kotlinx.coroutines.delay(delayMillis)
                nowMinutes = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
            }
        }
    }

    LaunchedEffect(activeSubjects) {
        scheduleClassAlarms(ctx, activeSubjects, todayIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoy") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {

            if (subjects.isEmpty() || dayNode?.a == false || activeSubjects.isEmpty()) {
                EmptyDayView(enabled = dayNode?.a ?: false)
                return@Box
            }

            val lastClassEnd = activeSubjects.lastOrNull()?.f
            val isDayFinished = lastClassEnd != null && nowMinutes >= lastClassEnd

            if (isDayFinished) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Ya no hay clases pendientes. ¡Es hora de descansar!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                return@Box
            }

            // ====== Clase anterior, actual y próxima ======
            val currentIndex = activeSubjects.indexOfFirst { nowMinutes in it.s until it.f }

            val current = if (currentIndex >= 0) activeSubjects[currentIndex] else null
            val previous = if (currentIndex > 0) activeSubjects[currentIndex - 1] else null
            val next = when {
                currentIndex >= 0 && currentIndex + 1 < activeSubjects.size -> activeSubjects[currentIndex + 1]
                currentIndex == -1 && nowMinutes < activeSubjects.first().s -> activeSubjects.first()
                else -> null
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(0.25f).fillMaxWidth()) {
                    previous?.let { SubjectItem(it) } ?: Box(
                        Modifier.fillMaxSize().background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(12.dp)
                    ) {
                        Text("No hay clase anterior", Modifier.align(Alignment.Center), color = Color.Gray)
                    }
                }

                Box(Modifier.weight(0.5f).fillMaxWidth()) {
                    current?.let { SubjectItem(it, isCurrent = true) } ?: Box(
                        Modifier.fillMaxSize().background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(12.dp)
                    ) {
                        Text("No hay clase actualmente", Modifier.align(Alignment.Center), color = Color.Gray)
                    }
                }

                Box(Modifier.weight(0.25f).fillMaxWidth()) {
                    next?.let { SubjectItem(it) } ?: Box(
                        Modifier.fillMaxSize().background(Color(0xFFEEEEEE), RoundedCornerShape(12.dp)).padding(12.dp)
                    ) {
                        Text("No hay próxima clase", Modifier.align(Alignment.Center), color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectItem(subject: SubjectNode, isCurrent: Boolean = false) {
    val startHour = subject.s / 60
    val startMinute = subject.s % 60
    val endHour = subject.f / 60
    val endMinute = subject.f % 60

    val backgroundColor = if (isCurrent) Color(subject.c) else Color(subject.c).copy(alpha = 0.7f)
    val shadowElevation = if (isCurrent) 8.dp else 2.dp

    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(subject.n, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Profesor: ${subject.t}", fontSize = 14.sp, color = Color.White)
            Text("Lugar: ${subject.p}", fontSize = 14.sp, color = Color.White)
            Text("Horario: %02d:%02d - %02d:%02d".format(startHour, startMinute, endHour, endMinute),
                fontSize = 14.sp, color = Color.White
            )
        }
    }
}