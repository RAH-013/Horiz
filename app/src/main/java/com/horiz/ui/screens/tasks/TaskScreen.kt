package com.horiz.ui.screens.tasks

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.horiz.alarms.AppAlarmScheduler
import com.horiz.data.model.Schedule
import com.horiz.data.model.TaskNode
import com.horiz.data.model.TaskStatus
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.screens.tasks.components.DeleteTaskDialog
import com.horiz.ui.screens.tasks.components.TaskDialog
import com.horiz.ui.screens.tasks.components.TaskEmptyState
import com.horiz.ui.screens.tasks.components.TaskUtils
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
    scheduleEntryId: Long,
    storage: ScheduleStorage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val alarmScheduler = remember {
        AppAlarmScheduler(context)
    }

    val entry = schedule.findEntry(scheduleEntryId)

    if (entry == null) {
        onDismiss()
        return
    }

    var taskVersion by remember {
        mutableIntStateOf(0)
    }

    var showTaskDialog by remember {
        mutableStateOf(false)
    }

    var selectedDetailsTask by remember {
        mutableStateOf<TaskNode?>(null)
    }

    var editingTask by remember {
        mutableStateOf<TaskNode?>(null)
    }

    var deletingTask by remember {
        mutableStateOf<TaskNode?>(null)
    }

    var contextMenuTaskId by remember {
        mutableStateOf<Long?>(null)
    }

    var selectedFilter by remember {
        mutableStateOf(TaskFilter.PENDING)
    }

    var activeDraggingId by remember {
        mutableStateOf<Long?>(null)
    }

    var dragAccumulatedY by remember {
        mutableFloatStateOf(0f)
    }

    val listState = rememberLazyListState()

    val tasks = remember {
        mutableStateListOf<TaskNode>()
    }

    fun syncTasks() {
        tasks.clear()

        val subjectId = entry.subjectId ?: return

        tasks.addAll(
            schedule.findTasksForSubject(subjectId)
                .sortedBy { it.orderIndex }
        )
    }
    LaunchedEffect(
        scheduleEntryId,
        taskVersion
    ) {
        syncTasks()
    }

    val now = LocalDateTime.now()

    val filteredTasks = when (selectedFilter) {
        TaskFilter.PENDING -> {
            tasks.filter {
                it.status(now) == TaskStatus.PENDING
            }
        }

        TaskFilter.OVERDUE -> {
            tasks.filter {
                it.status(now) == TaskStatus.OVERDUE
            }
        }

        TaskFilter.COMPLETED -> {
            tasks.filter {
                it.status(now) == TaskStatus.COMPLETED
            }
        }

        TaskFilter.ALL -> {
            tasks.toList()
        }
    }

    fun refreshTasks() {
        taskVersion++
    }

    fun normalizeOrder() {
        tasks.forEachIndexed { index, task ->
            task.orderIndex = index

            val scheduleIndex =
                schedule.tasks.indexOfFirst {
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
        val vibrator = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
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

        if (currentIndex == -1) {
            return
        }

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

            val scheduleIndex =
                schedule.tasks.indexOfFirst {
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
        tonalElevation = 6.dp
    ) {
        Scaffold(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                Column(
                    modifier = Modifier.padding(
                        bottom = 8.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 12.dp,
                                top = 4.dp,
                                bottom = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = TaskUtils.getEntryTitle(
                                    schedule = schedule,
                                    entryId = scheduleEntryId
                                ),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "${filteredTasks.size} ${
                                    if (filteredTasks.size == 1) {
                                        "tarea"
                                    } else {
                                        "tareas"
                                    }
                                }",
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
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            )
                            .padding(
                                horizontal = 16.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.PENDING,
                            onClick = {
                                selectedFilter = TaskFilter.PENDING
                            },
                            label = {
                                Text("Pendientes")
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
                                Text("Vencidas")
                            },
                            shape = CircleShape
                        )

                        FilterChip(
                            selected =
                                selectedFilter == TaskFilter.COMPLETED,
                            onClick = {
                                selectedFilter = TaskFilter.COMPLETED
                            },
                            label = {
                                Text("Completadas")
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
                                Text("Todas")
                            },
                            shape = CircleShape
                        )
                    }
                }
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding()
            ) {
                if (filteredTasks.isEmpty()) {
                    TaskEmptyState(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        userScrollEnabled =
                            activeDraggingId == null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                horizontal = 16.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 96.dp
                        )
                    ) {
                        itemsIndexed(
                            items = filteredTasks,
                            key = { _, task ->
                                task.id
                            }
                        ) { _, task ->

                            val isBeingDragged =
                                activeDraggingId == task.id

                            val showMenu =
                                contextMenuTaskId == task.id

                            val elevation by animateFloatAsState(
                                targetValue =
                                    if (isBeingDragged) {
                                        10f
                                    } else {
                                        2f
                                    },
                                animationSpec = tween(150),
                                label = "task_elevation"
                            )

                            val status =
                                task.status(now)

                            val statusAccentColor =
                                when (status) {
                                    TaskStatus.OVERDUE ->
                                        MaterialTheme.colorScheme.error

                                    TaskStatus.COMPLETED ->
                                        Color(0xFF4CAF50)

                                    TaskStatus.PENDING ->
                                        MaterialTheme.colorScheme.primary
                                }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                                    .zIndex(
                                        if (isBeingDragged) {
                                            10f
                                        } else {
                                            1f
                                        }
                                    )
                                    .shadow(
                                        elevation.dp,
                                        RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                selectedDetailsTask =
                                                    task
                                            },
                                            onLongClick = {
                                                vibrate()
                                            }
                                        ),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(
                                                IntrinsicSize.Min
                                            ),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .fillMaxHeight()
                                                .background(
                                                    statusAccentColor
                                                )
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = 8.dp,
                                                    vertical = 12.dp
                                                ),
                                            verticalAlignment =
                                                Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector =
                                                    Icons.Default.DragHandle,
                                                contentDescription =
                                                    "Reordenar",
                                                tint =
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.6f
                                                    ),
                                                modifier = Modifier
                                                    .padding(
                                                        horizontal = 4.dp
                                                    )
                                                    .pointerInput(
                                                        task.id
                                                    ) {
                                                        detectDragGestures(
                                                            onDragStart = {
                                                                activeDraggingId =
                                                                    task.id
                                                                dragAccumulatedY =
                                                                    0f
                                                                vibrate()
                                                            },
                                                            onDrag = {
                                                                    change,
                                                                    dragAmount ->
                                                                change.consume()

                                                                dragAccumulatedY +=
                                                                    dragAmount.y

                                                                val step =
                                                                    70f

                                                                if (
                                                                    dragAccumulatedY >=
                                                                    step
                                                                ) {
                                                                    moveTask(
                                                                        task.id,
                                                                        1
                                                                    )

                                                                    dragAccumulatedY =
                                                                        0f
                                                                } else if (
                                                                    dragAccumulatedY <=
                                                                    -step
                                                                ) {
                                                                    moveTask(
                                                                        task.id,
                                                                        -1
                                                                    )

                                                                    dragAccumulatedY =
                                                                        0f
                                                                }
                                                            },
                                                            onDragEnd = {
                                                                activeDraggingId =
                                                                    null
                                                                dragAccumulatedY =
                                                                    0f
                                                                saveSchedule()
                                                            },
                                                            onDragCancel = {
                                                                activeDraggingId =
                                                                    null
                                                                dragAccumulatedY =
                                                                    0f
                                                            }
                                                        )
                                                    }
                                            )

                                            IconButton(
                                                onClick = {
                                                    task.toggleCompleted()

                                                    val index =
                                                        tasks.indexOfFirst {
                                                            it.id == task.id
                                                        }

                                                    if (index != -1) {
                                                        tasks[index] = task
                                                    }

                                                    saveSchedule()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector =
                                                        if (task.completed) {
                                                            Icons.Default.Check
                                                        } else {
                                                            Icons.Default.RadioButtonUnchecked
                                                        },
                                                    contentDescription =
                                                        "Estado",
                                                    tint =
                                                        if (task.completed) {
                                                            Color(0xFF4CAF50)
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                        }
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(
                                                        horizontal = 6.dp
                                                    )
                                            ) {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        textDecoration =
                                                            if (task.completed) {
                                                                TextDecoration.LineThrough
                                                            } else {
                                                                TextDecoration.None
                                                            },
                                                        fontWeight =
                                                            FontWeight.SemiBold
                                                    ),
                                                    color =
                                                        if (task.completed) {
                                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                alpha = 0.7f
                                                            )
                                                        } else {
                                                            MaterialTheme.colorScheme.onSurface
                                                        },
                                                    maxLines = 1,
                                                    overflow =
                                                        TextOverflow.Ellipsis
                                                )

                                                task.dueAt?.let { dueAt ->
                                                    Spacer(
                                                        modifier =
                                                            Modifier.height(2.dp)
                                                    )

                                                    Text(
                                                        text =
                                                            TaskUtils.formatDueAt(
                                                                dueAt
                                                            ),
                                                        style =
                                                            MaterialTheme.typography.bodySmall,
                                                        color =
                                                            when (status) {
                                                                TaskStatus.OVERDUE ->
                                                                    MaterialTheme.colorScheme.error

                                                                TaskStatus.COMPLETED ->
                                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                        alpha = 0.6f
                                                                    )

                                                                TaskStatus.PENDING ->
                                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                            }
                                                    )
                                                }
                                            }

                                            Box {
                                                IconButton(
                                                    onClick = {
                                                        contextMenuTaskId =
                                                            task.id
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector =
                                                            Icons.Default.MoreVert,
                                                        contentDescription =
                                                            "Opciones",
                                                        tint =
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = showMenu,
                                                    onDismissRequest = {
                                                        contextMenuTaskId =
                                                            null
                                                    }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text("Editar")
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector =
                                                                    Icons.Default.Edit,
                                                                contentDescription =
                                                                    null
                                                            )
                                                        },
                                                        onClick = {
                                                            contextMenuTaskId =
                                                                null
                                                            editingTask =
                                                                task
                                                            showTaskDialog =
                                                                true
                                                        }
                                                    )

                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                "Eliminar",
                                                                color =
                                                                    MaterialTheme.colorScheme.error
                                                            )
                                                        },
                                                        leadingIcon = {
                                                            Icon(
                                                                imageVector =
                                                                    Icons.Default.Delete,
                                                                contentDescription =
                                                                    null,
                                                                tint =
                                                                    MaterialTheme.colorScheme.error
                                                            )
                                                        },
                                                        onClick = {
                                                            contextMenuTaskId =
                                                                null
                                                            deletingTask =
                                                                task
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        showTaskDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    shape = CircleShape,
                    containerColor =
                        MaterialTheme.colorScheme.primary,
                    contentColor =
                        MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar tarea"
                    )
                }
            }
        }
    }

    selectedDetailsTask?.let { task ->
        AlertDialog(
            onDismissRequest = {
                selectedDetailsTask = null
            },
            title = {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "Sin descripción",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.6f
                                )
                        )
                    }

                    task.dueAt?.let { dueAt ->
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    end = 8.dp
                                )
                            )

                            Text(
                                text =
                                    TaskUtils.formatDueAt(
                                        dueAt
                                    ),
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

    if (showTaskDialog) {
        TaskDialog(
            task = editingTask,
            onDismiss = {
                showTaskDialog = false
            },
            onSave = {
                    title,
                    description,
                    dueAt ->

                if (editingTask == null) {
                    val newOrderIndex =
                        tasks.size

                    val newTask = TaskNode(
                        id = System.currentTimeMillis(),
                        subjectId = entry.subjectId!!,
                        title = title,
                        description = description,
                        dueAt = dueAt,
                        orderIndex = newOrderIndex
                    )

                    schedule.tasks.add(newTask)
                    tasks.add(newTask)
                } else {
                    val targetId =
                        editingTask!!.id

                    val scheduleIndex =
                        schedule.tasks.indexOfFirst {
                            it.id == targetId
                        }

                    if (scheduleIndex != -1) {
                        val current =
                            schedule.tasks[scheduleIndex]

                        schedule.tasks[scheduleIndex] =
                            current.copy(
                                title = title,
                                description = description,
                                dueAt = dueAt
                            )
                    }

                    val taskIndex =
                        tasks.indexOfFirst {
                            it.id == targetId
                        }

                    if (taskIndex != -1) {
                        tasks[taskIndex] =
                            tasks[taskIndex].copy(
                                title = title,
                                description = description,
                                dueAt = dueAt
                            )
                    }
                }

                saveSchedule()
                showTaskDialog = false
            }
        )
    }

    deletingTask?.let { task ->
        DeleteTaskDialog(
            task = task,
            onDismiss = {
                deletingTask = null
            },
            onConfirm = {
                schedule.tasks.removeIf {
                    it.id == task.id
                }

                tasks.removeIf {
                    it.id == task.id
                }

                saveSchedule()
                deletingTask = null
            }
        )
    }
}