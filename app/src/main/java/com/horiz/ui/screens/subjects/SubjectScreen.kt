package com.horiz.ui.screens.subjects

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.horiz.data.model.Schedule
import com.horiz.data.model.ScheduleEntry
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.subjects.components.ActiveDaysDialog
import com.horiz.ui.screens.subjects.components.DaySelector
import com.horiz.ui.screens.subjects.components.DeleteSubjectDialog
import com.horiz.ui.screens.subjects.components.DuplicateSubjectDialog
import com.horiz.ui.screens.subjects.components.EmptyDayView
import com.horiz.ui.screens.subjects.components.SubjectDialog
import com.horiz.ui.screens.subjects.components.SubjectList
import com.horiz.ui.screens.tasks.TaskScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@Composable
fun SubjectScreen(
    onBackClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storage = remember { ScheduleStorage(context) }

    val daysFull = remember {
        listOf(
            "Lunes",
            "Martes",
            "Miércoles",
            "Jueves",
            "Viernes",
            "Sábado",
            "Domingo"
        )
    }

    val daysShort = remember {
        listOf(
            "Lun",
            "Mar",
            "Mié",
            "Jue",
            "Vie",
            "Sáb",
            "Dom"
        )
    }

    val todayIndex = remember {
        (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    var schedule by remember {
        mutableStateOf<Schedule?>(null)
    }

    var editingEntry by remember {
        mutableStateOf<ScheduleEntry?>(null)
    }

    var deletingEntry by remember {
        mutableStateOf<ScheduleEntry?>(null)
    }

    var duplicatingEntry by remember {
        mutableStateOf<ScheduleEntry?>(null)
    }

    var taskEntry by remember {
        mutableStateOf<ScheduleEntry?>(null)
    }

    var showSubjectDialog by remember {
        mutableStateOf(false)
    }

    var showActiveDaysDialog by remember {
        mutableStateOf(false)
    }

    var hasAutoOpenedDialog by remember {
        mutableStateOf(false)
    }

    val reloadSchedule: () -> Unit = {
        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                storage.getKing()?.let { king ->
                    storage.getSchedule(king)
                }
            }

            schedule = loaded
        }
    }

    LaunchedEffect(Unit) {
        val loadedSchedule = withContext(Dispatchers.IO) {
            storage.getKing()?.let { king ->
                storage.getSchedule(king)
            }
        }

        schedule = loadedSchedule

        val totalEntriesCount =
            loadedSchedule?.days?.sumOf {
                it.entries.size
            } ?: 0

        if (
            loadedSchedule != null &&
            totalEntriesCount == 0 &&
            !hasAutoOpenedDialog
        ) {
            hasAutoOpenedDialog = true
            showActiveDaysDialog = true
        }
    }

    val currentSchedule = schedule

    val activeDaysMap = remember(currentSchedule) {
        currentSchedule?.days
            ?.mapIndexed { index, day ->
                index to day
            }
            ?.filter {
                it.second.enabled
            }
            ?: emptyList()
    }

    val activeDaysShort = remember(activeDaysMap) {
        activeDaysMap.map {
            daysShort[it.first]
        }
    }

    val pageCount = activeDaysMap.size

    val initialPageIndex = remember(
        activeDaysMap,
        todayIndex
    ) {
        if (activeDaysMap.isEmpty()) {
            0
        } else {
            val exactIndex = activeDaysMap.indexOfFirst {
                it.first == todayIndex
            }

            if (exactIndex != -1) {
                exactIndex
            } else {
                val nextIndex = activeDaysMap.indexOfFirst {
                    it.first > todayIndex
                }

                if (nextIndex != -1) {
                    nextIndex
                } else {
                    activeDaysMap.indexOfLast {
                        it.first < todayIndex
                    }.coerceAtLeast(0)
                }
            }
        }
    }

    val pagerState = rememberPagerState(
        initialPage = initialPageIndex.coerceIn(
            0,
            (pageCount - 1).coerceAtLeast(0)
        ),
        pageCount = {
            pageCount
        }
    )

    LaunchedEffect(
        activeDaysMap,
        todayIndex
    ) {
        if (activeDaysMap.isNotEmpty()) {
            val targetPage =
                if (
                    activeDaysMap.any {
                        it.first == todayIndex
                    }
                ) {
                    activeDaysMap.indexOfFirst {
                        it.first == todayIndex
                    }
                } else {
                    val nextPage =
                        activeDaysMap.indexOfFirst {
                            it.first > todayIndex
                        }

                    if (nextPage != -1) {
                        nextPage
                    } else {
                        activeDaysMap.indexOfLast {
                            it.first < todayIndex
                        }.coerceAtLeast(0)
                    }
                }

            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    val safePageIndex =
        pagerState.currentPage.coerceIn(
            0,
            (pageCount - 1).coerceAtLeast(0)
        )

    val currentDayPair =
        activeDaysMap.getOrNull(safePageIndex)

    val currentOriginalDayIndex =
        currentDayPair?.first ?: 0

    val currentDay =
        currentDayPair?.second

    val entries =
        currentDay?.entries ?: emptyList()

    AppScreen(
        title = if (currentDay != null) {
            "${daysFull[currentOriginalDayIndex]} (${entries.size})"
        } else {
            "Materias"
        },
        onBackClick = onBackClick,
        actions = {
            currentSchedule?.let {
                IconButton(
                    onClick = {
                        showActiveDaysDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Configurar días visibles"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeDaysMap.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        key = { pageIndex ->
                            (
                                    activeDaysMap
                                        .getOrNull(pageIndex)
                                        ?.first
                                        ?: pageIndex
                                    ).toLong()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val dayForPage =
                            activeDaysMap
                                .getOrNull(pageIndex)
                                ?.second

                        val entriesForPage =
                            dayForPage?.entries
                                ?: emptyList()

                        when {
                            dayForPage == null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No hay horario activo")
                                }
                            }

                            entriesForPage.isEmpty() -> {
                                EmptyDayView()
                            }

                            else -> {
                                SubjectList(
                                    items = entriesForPage,
                                    schedule = currentSchedule!!,
                                    onEdit = {
                                        editingEntry = it
                                        showSubjectDialog = true
                                    },
                                    onDelete = {
                                        deletingEntry = it
                                    },
                                    onDuplicate = {
                                        duplicatingEntry = it
                                    },
                                    onClick = {
                                        taskEntry = it
                                    }
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (entries.size >= 10) {
                                Toast.makeText(
                                    context,
                                    "Se ha alcanzado la capacidad máxima de 10 materias",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                editingEntry = null
                                showSubjectDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar materia"
                        )
                    }
                } else {
                    EmptyDayView()
                }
            }

            if (activeDaysShort.isNotEmpty()) {
                DaySelector(
                    days = activeDaysShort,
                    selected = safePageIndex,
                    onSelect = { page ->
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
            }
        }
    }

    currentSchedule?.let { nonNullSchedule ->
        if (showActiveDaysDialog) {
            ActiveDaysDialog(
                schedule = nonNullSchedule,
                daysNames = daysFull,
                onConfirm = { updatedStates ->
                    updatedStates.forEachIndexed { index, isEnabled ->
                        val dayNode =
                            nonNullSchedule.days
                                .getOrNull(index)

                        if (
                            dayNode != null &&
                            dayNode.enabled != isEnabled
                        ) {
                            dayNode.toggleStatus()
                        }
                    }

                    showActiveDaysDialog = false

                    scope.launch {
                        withContext(Dispatchers.IO) {
                            storage.createSchedule(
                                nonNullSchedule
                            )
                        }

                        reloadSchedule()
                    }
                },
                onDismiss = {
                    showActiveDaysDialog = false
                }
            )
        }

        if (showSubjectDialog) {
            SubjectDialog(
                dayIndex = currentOriginalDayIndex,
                entry = editingEntry,
                schedule = nonNullSchedule,
                storage = storage,
                onClose = {
                    showSubjectDialog = false
                    editingEntry = null
                    reloadSchedule()
                }
            )
        }

        duplicatingEntry?.let { entryToDuplicate ->
            DuplicateSubjectDialog(
                entry = entryToDuplicate,
                schedule = nonNullSchedule,
                currentDayIndex = currentOriginalDayIndex,
                daysNames = daysFull,
                enabledDayIndexes = activeDaysMap.map {
                    it.first
                },
                onConfirm = { targetDayIndex ->
                    val targetDay =
                        nonNullSchedule.days
                            .getOrNull(targetDayIndex)

                    if (targetDay != null) {
                        if (targetDay.entries.size >= 10) {
                            Toast.makeText(
                                context,
                                "El día ${daysFull[targetDayIndex]} ya tiene el límite de 10 materias",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val clonedEntry =
                                entryToDuplicate.copy(
                                    id = System.currentTimeMillis(),
                                    dayIndex = targetDayIndex
                                )

                            targetDay.entries.add(
                                clonedEntry
                            )

                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    storage.createSchedule(
                                        nonNullSchedule
                                    )
                                }

                                reloadSchedule()

                                Toast.makeText(
                                    context,
                                    "Materia duplicada en ${daysFull[targetDayIndex]}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    duplicatingEntry = null
                },
                onDismiss = {
                    duplicatingEntry = null
                }
            )
        }

        deletingEntry?.let { entry ->
            DeleteSubjectDialog(
                entry = entry,
                schedule = nonNullSchedule,
                onConfirm = {
                    nonNullSchedule.removeEntry(entry.id)
                    deletingEntry = null

                    scope.launch {
                        withContext(Dispatchers.IO) {
                            storage.createSchedule(
                                nonNullSchedule
                            )
                        }

                        reloadSchedule()
                    }
                },
                onDismiss = {
                    deletingEntry = null
                }
            )
        }

        taskEntry?.let { entry ->
            TaskScreen(
                schedule = nonNullSchedule,
                scheduleEntryId = entry.id,
                storage = storage,
                onDismiss = {
                    taskEntry = null
                    reloadSchedule()
                }
            )
        }
    }
}