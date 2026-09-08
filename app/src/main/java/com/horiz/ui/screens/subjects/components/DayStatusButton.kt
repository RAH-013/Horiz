package com.horiz.ui.screens.subjects.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DayStatusButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDotColor = Color(0xFF4CAF50)
    val inactiveDotColor = Color(0xFFE53935)

    val dotColor by animateColorAsState(
        targetValue = if (enabled) activeDotColor else inactiveDotColor,
        animationSpec = tween(durationMillis = 200),
        label = "statusDotColor"
    )

    val staticContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val staticBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    FilterChip(
        selected = enabled,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = dotColor,
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (enabled) "Activo" else "Desactivado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        modifier = modifier,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = staticContainerColor,
            selectedContainerColor = staticContainerColor,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = enabled,
            borderColor = staticBorderColor,
            selectedBorderColor = staticBorderColor
        )
    )
}