package com.horiz.ui.screens.schedule.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DeleteScheduleDialog(
    scheduleName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }

    val thumbSize = 48.dp
    val trackPadding = 4.dp

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "¿Eliminar horario?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Esta acción no se puede deshacer. Estás a punto de eliminar \"$scheduleName\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(trackPadding),
                    contentAlignment = Alignment.CenterStart
                ) {
                    val density = LocalDensity.current
                    val maxOffsetPx = with(density) {
                        (maxWidth - thumbSize - (trackPadding * 2)).toPx()
                    }

                    val progress = if (maxOffsetPx > 0) (offset.value / maxOffsetPx).coerceIn(0f, 1f) else 0f

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .size(22.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "Desliza para confirmar",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(1f - progress),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )

                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .graphicsLayer {
                                translationX = offset.value
                            }
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                            .pointerInput(maxOffsetPx) {
                                detectHorizontalDragGestures(
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            val newOffset = (offset.value + dragAmount)
                                                .coerceIn(0f, maxOffsetPx)
                                            offset.snapTo(newOffset)
                                        }
                                    },
                                    onDragEnd = {
                                        scope.launch {
                                            if (offset.value >= maxOffsetPx * 0.75f) {
                                                offset.animateTo(
                                                    targetValue = maxOffsetPx,
                                                    animationSpec = tween(150)
                                                )
                                                onConfirm()
                                            } else {
                                                offset.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec = tween(250)
                                                )
                                            }
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Confirmar eliminación",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        confirmButton = {}
    )
}