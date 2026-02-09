package com.example.horiz.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.horiz.activities.ManagerActivity
import com.example.horiz.activities.ScheduleActivity
import com.example.horiz.activities.TodayActivity
import com.example.horiz.components.SecretAccessText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onBack: () -> Unit)  {

    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val animatedGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF4A148C),
            Color(0xFF6A1B9A),
            Color(0xFF8E24AA)
        ),
        startY = offset,
        endY = offset + 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedGradient)
            .padding(24.dp)
            .navigationBarsPadding()
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(100.dp)) // Mueve el título más abajo

            Text(
                text = "Horiz",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MainButton("Hoy") {
                    context.startActivity(Intent(context, TodayActivity::class.java))
                }

                MainButton("Horario") {
                    context.startActivity(Intent(context, ScheduleActivity::class.java))
                }

                MainButton("Administrar") {
                    context.startActivity(Intent(context, ManagerActivity::class.java))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SecretAccessText()

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun MainButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF6A1B9A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}