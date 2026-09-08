package com.horiz.ui.screens.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.horiz.data.model.Schedule
import com.horiz.storage.ScheduleStorage
import com.horiz.ui.components.AppScreen
import com.horiz.ui.screens.scanner.components.ScannerUtils
import com.horiz.utils.ShareSchedule
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannerScreen(
    onBackClick: () -> Unit,
    onImported: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val storage = remember { ScheduleStorage(context) }
    var barcodeViewInstance by remember { mutableStateOf<BarcodeView?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isFinished by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Animación de pulso para el borde transparente/brillante
    val infiniteTransition = rememberInfiniteTransition(label = "borderPulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnim"
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            errorMessage = "Se requiere acceso a la cámara para escanear los códigos QR."
        } else {
            errorMessage = null
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null || isFinished) return@rememberLauncherForActivityResult

        isProcessing = true
        errorMessage = null

        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                } ?: throw IllegalArgumentException("No se pudo abrir la imagen.")

                val qrText = ScannerUtils.decodeQrFromBitmap(bitmap)

                withContext(Dispatchers.Main) {
                    handleImportedSchedule(
                        qrText = qrText,
                        storage = storage,
                        onSuccess = {
                            isFinished = true
                            isProcessing = false
                            vibrateDevice(context)
                            onImported()
                        },
                        onError = { error ->
                            isProcessing = false
                            errorMessage = error
                        }
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    errorMessage = "No se encontró ningún código QR válido en la imagen seleccionada."
                }
            }
        }
    }

    val processCameraResult: (BarcodeResult?) -> Unit = { result ->
        if (result != null && !isFinished && !isProcessing) {
            if (result.barcodeFormat == BarcodeFormat.QR_CODE) {
                isProcessing = true
                barcodeViewInstance?.pause()

                handleImportedSchedule(
                    qrText = result.text,
                    storage = storage,
                    onSuccess = {
                        isFinished = true
                        isProcessing = false
                        vibrateDevice(context)
                        onImported()
                    },
                    onError = { error ->
                        isProcessing = false
                        errorMessage = error
                        barcodeViewInstance?.resume()
                    }
                )
            }
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeViewInstance?.pause()
            barcodeViewInstance?.stopDecoding()
        }
    }

    AppScreen(
        title = "Escanear horario",
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (hasCameraPermission) {
                    // Usamos BarcodeView limpio en lugar de DecoratedBarcodeView
                    AndroidView(
                        factory = { ctx ->
                            BarcodeView(ctx).apply {
                                decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult?) {
                                        processCameraResult(result)
                                    }

                                    override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
                                })
                                resume()
                                barcodeViewInstance = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay oscuro con recorte central
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    ) {
                        drawRect(Color.Black.copy(alpha = 0.5f))

                        val boxSize = 260.dp.toPx()
                        val topLeftX = (size.width - boxSize) / 2
                        val topLeftY = (size.height - boxSize) / 2
                        val cornerRadius = 24.dp.toPx()

                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(topLeftX, topLeftY),
                            size = Size(boxSize, boxSize),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            blendMode = BlendMode.Clear
                        )
                    }

                    // Marco exterior con pulso/parpadeo suave
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(260.dp)
                            .alpha(borderAlpha)
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Se requiere acceso a la cámara para escanear el código QR.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Icon(imageVector = Icons.Rounded.Cameraswitch, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Conceder Permiso")
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = errorMessage != null && hasCameraPermission,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { errorText ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = errorText,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (isProcessing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Procesando código QR...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Text(
                            text = "Apunta la cámara al código QR de tu horario",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Rounded.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Seleccionar desde Galería")
                    }
                }
            }
        }
    }
}

private fun handleImportedSchedule(
    qrText: String?,
    storage: ScheduleStorage,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (qrText.isNullOrBlank()) {
        onError("El contenido del código QR está vacío.")
        return
    }

    try {
        val decompressed = ShareSchedule.decompress(qrText)
        var schedule = Schedule.parse(decompressed)

        val schedulesList: List<*> = storage.getSchedules()
        val existingNames = schedulesList.filterIsInstance<Schedule>().map { it.name }.toSet()

        val baseName = schedule.name
        var suffix = 1

        while (schedule.name in existingNames) {
            schedule = schedule.copy(name = "$baseName ($suffix)")
            suffix++
        }

        storage.createSchedule(schedule)
        onSuccess()
    } catch (e: Exception) {
        onError("El código QR no contiene un formato de horario válido.")
    }
}

private fun vibrateDevice(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(
            VibrationEffect.createOneShot(
                120,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(120)
    }
}