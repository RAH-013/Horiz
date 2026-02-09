package com.example.horiz.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.horiz.storage.ScheduleStorage
import com.example.horiz.utils.ShareSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    scheduleName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember { ScheduleStorage(context) }
    val scope = rememberCoroutineScope()

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val schedule = storage.getSchedule(scheduleName)
                    ?: throw IllegalArgumentException("Horario no encontrado")

                qrBitmap = ShareSchedule.createSingleQRCode(schedule, storage, context)
            } catch (e: Exception) {
                error = e.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compartir \"$scheduleName\"") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    qrBitmap?.let { bitmap ->
                        IconButton(onClick = { shareBitmap(context, bitmap, scheduleName) }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir QR")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6A1B9A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {

            when {
                error != null -> {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error)
                }

                qrBitmap == null -> {
                    CircularProgressIndicator()
                }

                else -> {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = "QR del schedule",
                        modifier = Modifier.size(300.dp)
                    )
                }
            }
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap, scheduleName: String) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "$scheduleName.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "Estoy usando Horiz ⌚ para organizar mis horarios.\n" +
                        "Te comparto mi horario \"$scheduleName\" para que también lo uses. 📅✨"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Compartir horario")
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}