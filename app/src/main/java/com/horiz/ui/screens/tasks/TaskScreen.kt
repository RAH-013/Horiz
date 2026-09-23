package com.horiz.ui.screens.tasks

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.data.model.Schedule
import com.horiz.data.model.TaskNode
import com.horiz.data.model.TaskStatus
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.screens.tasks.components.DeleteTaskDialog
import com.horiz.ui.screens.tasks.components.TaskCard
import com.horiz.ui.screens.tasks.components.TaskDialog
import com.horiz.ui.screens.tasks.components.TaskEmptyState
import com.horiz.ui.screens.tasks.components.TaskUtils
import com.horiz.ui.screens.tasks.components.toLocalDateTime
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class TaskFilter {
    PENDING,
    OVERDUE,
    COMPLETED,
    ALL
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TaskScreen(
    schedule: Schedule,
    subjectId: Long,
    storage: ScheduleStorage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val alarmScheduler = remember { AppAlarmScheduler(context) }

    val subject = schedule.findSubject(subjectId)

    if (subject == null) {
        LaunchedEffect(Unit) {
            onDismiss()
        }
        return
    }

    var taskVersion by remember { mutableIntStateOf(0) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var selectedDetailsTask by remember { mutableStateOf<TaskNode?>(null) }
    var editingTask by remember { mutableStateOf<TaskNode?>(null) }
    var deletingTask by remember { mutableStateOf<TaskNode?>(null) }
    var contextMenuTaskId by remember { mutableStateOf<Long?>(null) }
    var selectedFilter by remember { mutableStateOf(TaskFilter.PENDING) }
    var activeDraggingId by remember { mutableStateOf<Long?>(null) }
    var dragAccumulatedY by remember { mutableFloatStateOf(0f) }

    val listState = rememberLazyListState()
    val tasks = remember { mutableStateListOf<TaskNode>() }

    fun syncTasks() {
        tasks.clear()
        tasks.addAll(
            schedule.findTasksForSubject(subjectId)
                .sortedBy { it.orderIndex }
        )
    }

    LaunchedEffect(subjectId, taskVersion) {
        syncTasks()
    }

    val now = LocalDateTime.now()

    val pendingCount = tasks.count {
        it.status(now) == TaskStatus.PENDING
    }

    val overdueCount = tasks.count {
        it.status(now) == TaskStatus.OVERDUE
    }

    val completedCount = tasks.count {
        it.status(now) == TaskStatus.COMPLETED
    }

    val totalCount = tasks.size

    val progress = if (totalCount > 0) {
        completedCount.toFloat() / totalCount.toFloat()
    } else {
        0f
    }

    val filteredTasks = when (selectedFilter) {
        TaskFilter.PENDING ->
            tasks.filter { it.status(now) == TaskStatus.PENDING }

        TaskFilter.OVERDUE ->
            tasks.filter { it.status(now) == TaskStatus.OVERDUE }

        TaskFilter.COMPLETED ->
            tasks.filter { it.status(now) == TaskStatus.COMPLETED }

        TaskFilter.ALL ->
            tasks.toList()
    }

    fun refreshTasks() {
        taskVersion++
    }

    fun normalizeOrder() {
        tasks.forEachIndexed { index, task ->
            task.orderIndex = index

            val scheduleIndex = schedule.tasks.indexOfFirst {
                it.id == task.id
            }

            if (scheduleIndex != -1) {
                schedule.tasks[scheduleIndex].orderIndex = index
            }
        }
    }

    fun saveSchedule() {
        normalizeOrder()
        storage.createSchedule(schedule)
        refreshTasks()

        coroutineScope.launch {
            alarmScheduler.scheduleTaskReminders()
        }
    }

    fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (
                    context.getSystemService(
                        Context.VIBRATOR_MANAGER_SERVICE
                    ) as VibratorManager
                    ).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(
                Context.VIBRATOR_SERVICE
            ) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    35L,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35L)
        }
    }

    fun moveTask(
        taskId: Long,
        direction: Int
    ) {
        val currentIndex = tasks.indexOfFirst {
            it.id == taskId
        }

        if (currentIndex == -1) return

        val targetIndex = currentIndex + direction

        if (
            targetIndex < 0 ||
            targetIndex >= tasks.size
        ) {
            return
        }

        val movingTask = tasks[currentIndex]
        val targetTask = tasks[targetIndex]

        tasks[currentIndex] = targetTask
        tasks[targetIndex] = movingTask

        tasks.forEachIndexed { index, task ->
            task.orderIndex = index

            val scheduleIndex = schedule.tasks.indexOfFirst {
                it.id == task.id
            }

            if (scheduleIndex != -1) {
                schedule.tasks[scheduleIndex].orderIndex = index
            }
        }

        vibrate()
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = activeDraggingId == null,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(
            alpha = 0.42f
        ),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(
                        top = 8.dp,
                        bottom = 4.dp
                    )
                    .size(
                        width = 36.dp,
                        height = 4.dp
                    )
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .height(680.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 12.dp,
                                top = 4.dp,
                                bottom = 4.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = if (totalCount > 0) {
                                    "$completedCount de $totalCount completadas"
                                } else {
                                    "Sin tareas registradas"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = totalCount > 0
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = 6.dp
                                )
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor =
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            strokeCap = StrokeCap.Round
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            ),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.PENDING,
                            onClick = {
                                selectedFilter = TaskFilter.PENDING
                            },
                            label = {
                                Text("Pendientes ($pendingCount)")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        FilterChipDefaults.IconSize
                                    )
                                )
                            },
                            shape = CircleShape
                        )

                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.OVERDUE,
                            onClick = {
                                selectedFilter = TaskFilter.OVERDUE
                            },
                            label = {
                                Text("Vencidas ($overdueCount)")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        FilterChipDefaults.IconSize
                                    )
                                )
                            },
                            colors =
                                FilterChipDefaults.filterChipColors(
                                    selectedContainerColor =
                                        MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor =
                                        MaterialTheme.colorScheme.onErrorContainer,
                                    selectedLeadingIconColor =
                                        MaterialTheme.colorScheme.onErrorContainer
                                ),
                            shape = CircleShape
                        )

                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.COMPLETED,
                            onClick = {
                                selectedFilter = TaskFilter.COMPLETED
                            },
                            label = {
                                Text("Completadas ($completedCount)")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        FilterChipDefaults.IconSize
                                    )
                                )
                            },
                            shape = CircleShape
                        )

                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.ALL,
                            onClick = {
                                selectedFilter = TaskFilter.ALL
                            },
                            label = {
                                Text("Todas ($totalCount)")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        FilterChipDefaults.IconSize
                                    )
                                )
                            },
                            shape = CircleShape
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        showTaskDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar tarea"
                    )
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(paddingValues)
                    .navigationBarsPadding()
            ) {
                if (filteredTasks.isEmpty()) {
                    TaskEmptyState(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        userScrollEnabled =
                            activeDraggingId == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            top = 12.dp,
                            bottom = 96.dp
                        )
                    ) {
                        itemsIndexed(
                            items = filteredTasks,
                            key = { _, task -> task.id }
                        ) { _, task ->

                            TaskCard(
                                modifier = Modifier.animateItem(),
                                task = task,
                                now = now,
                                isBeingDragged =
                                    activeDraggingId == task.id,
                                showMenu =
                                    contextMenuTaskId == task.id,
                                onClick = {
                                    selectedDetailsTask = task
                                },
                                onLongClick = {
                                    vibrate()
                                },
                                onToggleCompleted = {
                                    task.toggleCompleted()

                                    val index =
                                        tasks.indexOfFirst {
                                            it.id == task.id
                                        }

                                    if (index != -1) {
                                        tasks[index] = task
                                    }

                                    saveSchedule()
                                },
                                onMenuClick = {
                                    contextMenuTaskId = task.id
                                },
                                onMenuDismiss = {
                                    contextMenuTaskId = null
                                },
                                onEditClick = {
                                    editingTask = task
                                    contextMenuTaskId = null
                                },
                                onDeleteClick = {
                                    deletingTask = task
                                    contextMenuTaskId = null
                                },
                                onDragStart = {
                                    activeDraggingId = task.id
                                    dragAccumulatedY = 0f
                                    vibrate()
                                },
                                onDrag = { dragAmountY ->
                                    dragAccumulatedY += dragAmountY

                                    val step = 70f

                                    if (dragAccumulatedY >= step) {
                                        moveTask(task.id, 1)
                                        dragAccumulatedY = 0f
                                    } else if (
                                        dragAccumulatedY <= -step
                                    ) {
                                        moveTask(task.id, -1)
                                        dragAccumulatedY = 0f
                                    }
                                },
                                onDragEnd = {
                                    activeDraggingId = null
                                    dragAccumulatedY = 0f
                                    saveSchedule()
                                },
                                onDragCancel = {
                                    activeDraggingId = null
                                    dragAccumulatedY = 0f
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTaskDialog || editingTask != null) {
        TaskDialog(
            task = editingTask,
            onDismiss = {
                showTaskDialog = false
                editingTask = null
            },
            onSave = {
                    title,
                    description,
                    dueAtMillis,
                    priority
                ->
                val currentTask = editingTask
                val dueAtDateTime =
                    dueAtMillis?.toLocalDateTime()

                if (currentTask != null) {
                    currentTask.title = title
                    currentTask.description = description
                    currentTask.dueAt = dueAtDateTime
                    currentTask.priority = priority

                    val index =
                        schedule.tasks.indexOfFirst {
                            it.id == currentTask.id
                        }

                    if (index != -1) {
                        schedule.tasks[index] = currentTask
                    }
                } else {
                    val newTask = TaskNode(
                        id = TaskUtils.generateTaskId(schedule),
                        subjectId = subjectId,
                        title = title,
                        description = description,
                        dueAt = dueAtDateTime,
                        priority = priority,
                        completed = false,
                        orderIndex = tasks.size
                    )

                    schedule.tasks.add(newTask)
                }

                saveSchedule()

                showTaskDialog = false
                editingTask = null
            }
        )
    }

    deletingTask?.let { taskToDelete ->
        DeleteTaskDialog(
            task = taskToDelete,
            onDismiss = {
                deletingTask = null
            },
            onConfirm = {
                schedule.tasks.removeAll {
                    it.id == taskToDelete.id
                }

                saveSchedule()
                deletingTask = null
            }
        )
    }

    selectedDetailsTask?.let { taskDetails ->
        AlertDialog(
            onDismissRequest = {
                selectedDetailsTask = null
            },
            title = {
                Text(taskDetails.title)
            },
            text = {
                Column {
                    taskDetails.description
                        .takeIf { it.isNotBlank() }
                        ?.let { description ->
                            Text(
                                text = description,
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )
                        }

                    taskDetails.dueAt?.let { dueAt ->
                        Text(
                            text = TaskUtils.formatDueAt(dueAt),
                            style =
                                MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDetailsTask = null
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}