package com.horiz.ui.screens.schedule.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScheduleCard(
    scheduleName: String,
    isKing: Boolean,
    onOpen: () -> Unit,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
            .clickable(
                onClick = onOpen
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isKing) {
                Color(0xFF2E7D32)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = scheduleName.uppercase(),
                    color = if (isKing) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Row {
                IconButton(
                    onClick = onActivate,
                    enabled = !isKing
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (isKing) {
                            "Horario activo"
                        } else {
                            "Activar horario"
                        },
                        tint = if (isKing) {
                            Color(0xFFFFD700)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Renombrar",
                        tint = if (isKing) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                IconButton(
                    onClick = onShare
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = if (isKing) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !isKing
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isKing) {
                            "No se puede eliminar el horario activo"
                        } else {
                            "Eliminar"
                        },
                        tint = when {
                            isKing -> Color.White.copy(alpha = 0.35f)
                            else -> Color.Red
                        }
                    )
                }
            }
        }
    }
}