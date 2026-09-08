package com.horiz.ui.screens.subjects.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DaySelector(
    days: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary
            )
            .navigationBarsPadding()
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(
                modifier = Modifier.width(4.dp)
            )

            days.forEachIndexed { index, day ->
                DayItem(
                    text = day,
                    selected = index == selected,
                    onClick = {
                        onSelect(index)
                    }
                )
            }

            Spacer(
                modifier = Modifier.width(4.dp)
            )
        }
    }
}

@Composable
private fun DayItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundAlpha by animateDpAsState(
        targetValue = if (selected) 1.dp else 0.dp,
        animationSpec = tween(180),
        label = "dayBackground"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary.copy(
                alpha = 0.14f
            )
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(180),
        label = "dayColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onPrimary.copy(
                alpha = 0.55f
            )
        },
        animationSpec = tween(180),
        label = "dayTextColor"
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 28.dp else 0.dp,
        animationSpec = tween(180),
        label = "dayIndicator"
    )

    Column(
        modifier = Modifier
            .clip(
                RoundedCornerShape(14.dp)
            )
            .clickable(
                onClick = onClick
            )
            .background(backgroundColor)
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(
            modifier = Modifier.height(5.dp)
        )

        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(backgroundAlpha * 3f)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    MaterialTheme.colorScheme.onPrimary
                )
        )
    }
}