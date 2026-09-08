package com.horiz.ui.screens.qr

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.qr.components.QrShareUtils
import com.horiz.utils.ShareSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QrScreen(
    scheduleName: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val storage = remember {
        ScheduleStorage(context)
    }

    var qrBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(scheduleName) {
        try {
            qrBitmap = withContext(Dispatchers.IO) {
                val schedule = storage.getSchedule(scheduleName)
                    ?: throw IllegalArgumentException(
                        "Horario no encontrado"
                    )

                ShareSchedule.createSingleQRCode(
                    schedule = schedule,
                    storage = storage,
                    context = context
                )
            }
        } catch (e: Exception) {
            error = e.message
                ?: "No se pudo generar el código QR"
        }
    }

    AppScreen(
        title = "Compartir \"$scheduleName\"",
        onBackClick = onBackClick,
        actions = {
            qrBitmap?.let { bitmap ->
                IconButton(
                    onClick = {
                        QrShareUtils.shareBitmap(
                            context = context,
                            bitmap = bitmap,
                            scheduleName = scheduleName
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Compartir QR"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                error != null -> {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                qrBitmap == null -> {
                    CircularProgressIndicator()
                }

                else -> {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR del horario",
                        modifier = Modifier.size(300.dp)
                    )
                }
            }
        }
    }
}