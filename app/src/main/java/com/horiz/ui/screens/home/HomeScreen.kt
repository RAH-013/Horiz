package com.horiz.ui.screens.home

import android.content.Intent
import android.net.Uri
import java.time.Year
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horiz.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    onTodayClick: () -> Unit,
    onSubjectsClick: () -> Unit,
    onSchedulesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        CascadingBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp)
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Horiz",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onPrimary,
                    letterSpacing = 6.sp
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                MainButton(
                    text = "Hoy",
                    onClick = onTodayClick
                )

                MainButton(
                    text = "Materias",
                    onClick = onSubjectsClick
                )

                MainButton(
                    text = "Horarios",
                    onClick = onSchedulesClick
                )

                MainButton(
                    text = "Preferencias",
                    onClick = onSettingsClick
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        bottom = 8.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/RAH-013")
                            )
                        )
                    }
                ) {
                    Text(
                        text = "RAH-013 / © ${Year.now().value}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.onPrimary.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CascadingBackground() {
    val colors = MaterialTheme.colorScheme

    val isDark = colors.surface.luminance() < 0.5f

    val primary = colors.primary
    val primaryContainer = colors.primaryContainer

    val backgroundStart: Color
    val backgroundMiddle: Color
    val backgroundEnd: Color

    val orb1Color: Color
    val orb2Color: Color
    val orb3Color: Color

    if (isDark) {
        backgroundStart = Color(
            red = primary.red * 0.32f,
            green = primary.green * 0.32f,
            blue = primary.blue * 0.32f
        )

        backgroundMiddle = Color(
            red = primary.red * 0.42f,
            green = primary.green * 0.42f,
            blue = primary.blue * 0.42f
        )

        backgroundEnd = Color(
            red = primary.red * 0.28f,
            green = primary.green * 0.28f,
            blue = primary.blue * 0.28f
        )

        orb1Color = primary.copy(alpha = 0.30f)
        orb2Color = primaryContainer.copy(alpha = 0.20f)
        orb3Color = primary.copy(alpha = 0.24f)

    } else {
        backgroundStart = Color(
            red = primary.red * 0.72f,
            green = primary.green * 0.72f,
            blue = primary.blue * 0.72f
        )

        backgroundMiddle = Color(
            red = primary.red * 0.82f,
            green = primary.green * 0.82f,
            blue = primary.blue * 0.82f
        )

        backgroundEnd = Color(
            red = primary.red * 0.68f,
            green = primary.green * 0.68f,
            blue = primary.blue * 0.68f
        )

        orb1Color = primary.copy(alpha = 0.18f)
        orb2Color = primary.copy(alpha = 0.13f)
        orb3Color = primaryContainer.copy(alpha = 0.20f)
    }

    val transition = rememberInfiniteTransition(
        label = "background"
    )

    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 14000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 22000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 18000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundStart,
                        backgroundMiddle,
                        backgroundEnd
                    )
                )
            )
    ) {
        val width = size.width
        val height = size.height

        val x1 =
            width * 0.5f +
                    width * 0.4f * cos(phase1)

        val y1 =
            height * 0.3f +
                    height * 0.2f * sin(phase1)

        val r1 =
            width * 0.7f +
                    width * 0.2f * sin(phase1 * 1.5f)

        val x2 =
            width * 0.4f +
                    width * 0.5f * sin(phase2)

        val y2 =
            height * 0.7f +
                    height * 0.3f * cos(phase2)

        val r2 =
            width * 0.8f +
                    width * 0.3f * cos(phase2 * 1.2f)

        val x3 =
            width * 0.6f +
                    width * 0.4f * cos(phase3)

        val y3 =
            height * 0.5f +
                    height * 0.4f * sin(phase3)

        val r3 =
            width * 0.6f +
                    width * 0.25f * cos(phase3 * 0.8f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orb1Color,
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = r1.coerceAtLeast(1f)
            ),
            radius = r1.coerceAtLeast(1f),
            center = Offset(x1, y1)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orb2Color,
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = r2.coerceAtLeast(1f)
            ),
            radius = r2.coerceAtLeast(1f),
            center = Offset(x2, y2)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orb3Color,
                    Color.Transparent
                ),
                center = Offset(x3, y3),
                radius = r3.coerceAtLeast(1f)
            ),
            radius = r3.coerceAtLeast(1f),
            center = Offset(x3, y3)
        )
    }
}

@Composable
private fun MainButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.surface,
            contentColor = colors.primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
    ) {
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}