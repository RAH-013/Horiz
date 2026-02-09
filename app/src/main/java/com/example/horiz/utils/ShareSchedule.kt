package com.example.horiz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import com.example.horiz.R
import com.example.horiz.model.Schedule
import com.example.horiz.storage.ScheduleStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object ShareSchedule {

    private fun compress(text: String): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { it.write(text.toByteArray()) }
        return baos.toByteArray()
    }

    fun decompress(base64: String): String {
        val compressed = Base64.decode(base64, Base64.NO_WRAP)
        val gis = GZIPInputStream(compressed.inputStream())
        return gis.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun getScheduleText(schedule: Schedule, storage: ScheduleStorage): String {
        return storage.getSchedule(schedule.n)?.serialize()
            ?: throw IllegalArgumentException("Schedule no existe")
    }

    fun createSingleQRCode(
        schedule: Schedule,
        storage: ScheduleStorage,
        context: Context,
        size: Int = 512
    ): Bitmap {
        val text = getScheduleText(schedule, storage)
        val compressed = compress(text)
        val base64 = Base64.encodeToString(compressed, Base64.NO_WRAP)

        val qrBitmap = generateQR(base64, size)

        val drawable = context.getDrawable(R.mipmap.ic_launcher)
            ?: throw IllegalArgumentException("Icono no encontrado")
        val logo = drawableToBitmap(drawable)

        return addLogoToQRCode(qrBitmap, logo)
    }

    private fun drawableToBitmap(drawable: android.graphics.drawable.Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 1,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    private fun addLogoToQRCode(qrBitmap: Bitmap, logo: Bitmap): Bitmap {
        val combined = Bitmap.createBitmap(
            qrBitmap.width,
            qrBitmap.height,
            qrBitmap.config ?: Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(combined)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)

        val scaleFactor = 0.1f
        val logoWidth = (qrBitmap.width * scaleFactor).toInt()
        val logoHeight = (qrBitmap.height * scaleFactor).toInt()
        val resizedLogo = Bitmap.createScaledBitmap(logo, logoWidth, logoHeight, true)

        val left = (qrBitmap.width - resizedLogo.width) / 2f
        val top = (qrBitmap.height - resizedLogo.height) / 2f

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }

        val padding = 8f
        canvas.drawRect(left - padding, top - padding, left + resizedLogo.width + padding, top + resizedLogo.height + padding, paint)
        canvas.drawBitmap(resizedLogo, left, top, null)

        return combined
    }

    private fun generateQR(content: String, size: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }
}