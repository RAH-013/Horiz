package com.example.horiz.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.horiz.model.SubjectNode

@Composable
fun SubjectCard(
    item: SubjectNode,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardShape = FoldedCornerShape(24.dp, 26.dp)
    val base = Color(item.c)

    val colorBlur = Color(
        red = (base.red * .5f).coerceIn(0f, 1f),
        green = (base.green * .5f).coerceIn(0f, 1f),
        blue = (base.blue * .5f).coerceIn(0f, 1f),
        alpha = base.alpha
    )

    val colorText =
        if ((0.299 * base.red + 0.587 * base.green + 0.114 * base.blue) > 0.7)
            Color.Black else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(1.5.dp, colorText.copy(alpha = .7f), cardShape),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .border(1.dp, colorText.copy(alpha = .25f), cardShape)
                .shadow(2.dp, cardShape)
        ) {

            Box(Modifier.matchParentSize().clip(cardShape).background(colorBlur.copy(alpha = .6f)))
            Box(Modifier.matchParentSize().clip(cardShape).background(Color.White.copy(alpha = .07f)))

            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

                Box(Modifier.width(20.dp).fillMaxHeight().background(base))

                Column(Modifier.weight(1f).padding(16.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val yOffset = 5.dp.toPx()
                                drawLine(
                                    color = colorText.copy(alpha = .4f),
                                    start = Offset(0f, size.height + yOffset),
                                    end = Offset(size.width, size.height + yOffset),
                                    strokeWidth = strokeWidth
                                )
                            }
                    ) {

                        Box(
                            Modifier.size(8.dp).background(
                                if (item.isNow()) Color(0xFF3CFF6B) else Color(0xFFFF3C3C),
                                CircleShape
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "${"%02d".format(item.s / 60)}:${"%02d".format(item.s % 60)} - ${"%02d".format(item.f / 60)}:${"%02d".format(item.f % 60)}",
                            color = colorText.copy(alpha = .9f),
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(Modifier.weight(1f)) {
                            Text(
                                item.n.uppercase(),
                                color = colorText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(item.t, color = colorText.copy(alpha = .85f), fontSize = 18.sp)
                            Text(item.p, color = colorText.copy(alpha = .75f), fontSize = 15.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircleActionButton(Icons.Default.Edit, Color.Black.copy(alpha = .3f), colorText, onEdit)
                            CircleActionButton(Icons.Default.Delete, Color.Red.copy(alpha = .3f), colorText, onDelete)
                        }
                    }
                }
            }

            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = .12f))
            )
        }
    }
}

@Composable
fun CircleActionButton(
    icon: ImageVector,
    bg: Color,
    bc: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .border(2.dp, bc, CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = bc, modifier = Modifier.size(20.dp))
    }
}

class FoldedCornerShape(
    private val cornerRadius: Dp,
    private val foldSize: Dp
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {

        val r = with(density) { cornerRadius.toPx() }
        val f = with(density) { foldSize.toPx() }

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(size.width - f, 0f)
            lineTo(size.width, f)
            lineTo(size.width, size.height - r)
            quadraticBezierTo(size.width, size.height, size.width - r, size.height)
            lineTo(r, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - r)
            lineTo(0f, r)
            quadraticBezierTo(0f, 0f, r, 0f)
            close()
        }

        return Outline.Generic(path)
    }
}