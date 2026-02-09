package com.example.horiz.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.horiz.R
import com.example.horiz.model.Schedule
import com.example.horiz.storage.ScheduleStorage
import com.example.horiz.utils.ShareSchedule
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.*

class ScannerActivity : AppCompatActivity() {

    private lateinit var scanner: DecoratedBarcodeView
    private var finished = false

    // Solicitud de cámara
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startScanner() else finish()
        }

    // Selección de imagen desde galería
    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) scanQrFromImage(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scanner_view)

        scanner = findViewById(R.id.barcode_scanner)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnFile = findViewById<Button>(R.id.btnFile)

        // Permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startScanner()
        }

        btnCancel.setOnClickListener { finish() }

        // Botón para seleccionar QR desde imagen
        btnFile.setOnClickListener {
            imageLauncher.launch("image/*")
        }
    }

    // ===== Escaneo con cámara =====
    private fun startScanner() {
        scanner.barcodeView.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
        scanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                handleQrResult(result) // Para cámara
            }
            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
        })
    }

    // ===== Escaneo desde imagen =====
    private fun scanQrFromImage(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)

            handleQrResult(result.text) // Llama a la versión String

        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo leer el QR de la imagen", Toast.LENGTH_LONG).show()
        }
    }

    // ===== Procesar QR desde cámara =====
    private fun handleQrResult(result: BarcodeResult?) {
        if (result == null || finished) return
        if (result.barcodeFormat != BarcodeFormat.QR_CODE) return

        handleQrResult(result.text) // Reutilizamos el método String
    }

    // ===== Procesar QR desde String (imagen o cámara) =====
    private fun handleQrResult(qrText: String?) {
        if (qrText == null || finished) return

        finished = true
        scanner.pause()

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }

        try {
            val decompressed = ShareSchedule.decompress(qrText)
            var schedule = Schedule.parse(decompressed)
            val storage = ScheduleStorage(this)

            val existing = storage.getSchedules().toMutableSet()
            val baseName = schedule.n
            var suffix = 1
            while (schedule.n in existing) {
                schedule = schedule.copy(n = "$baseName ($suffix)")
                suffix++
            }

            storage.createSchedule(schedule)

            Toast.makeText(
                this,
                "'${schedule.n}' importado correctamente.",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error al procesar el QR: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            scanner.resume()
            finished = false
        }
    }

    // ===== Ciclo de vida =====
    override fun onResume() {
        super.onResume()
        if (!finished) {
            scanner.resume()
            scanner.decodeContinuous(object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    handleQrResult(result)
                }
                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            })
        }
    }

    override fun onPause() {
        super.onPause()
        scanner.pause()
    }
}