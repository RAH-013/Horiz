package com.horiz.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.horiz.data.model.Schedule
import com.horiz.storage.ScheduleStorage
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.Deflater
import java.util.zip.Inflater

object ShareSchedule {
    private const val CHARACTER_SET = "UTF-8"

    fun getScheduleText(
        schedule: Schedule,
        storage: ScheduleStorage
    ): String {
        val storedSchedule =
            storage.getSchedule(schedule.name)
                ?: throw IllegalArgumentException(
                    "El horario no existe"
                )

        return encode(
            storedSchedule.serializeForQr()
        )
    }

    fun createSingleQRCode(
        schedule: Schedule,
        storage: ScheduleStorage,
        size: Int = 768
    ): Bitmap {
        val content = getScheduleText(
            schedule = schedule,
            storage = storage
        )

        return generateQR(
            content = content,
            size = size
        )
    }

    fun encode(data: ByteArray): String {
        val compressed = compress(data)

        return Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(compressed)
    }

    fun decode(text: String): ByteArray {
        if (text.isBlank()) {
            throw IllegalArgumentException(
                "El contenido del código QR está vacío."
            )
        }

        val compressed = try {
            Base64.getUrlDecoder().decode(text)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException(
                "El código QR contiene datos inválidos."
            )
        }

        return decompress(compressed)
    }

    fun decodeSchedule(text: String): Schedule {
        return Schedule.parseQr(
            decode(text)
        )
    }

    private fun compress(
        data: ByteArray
    ): ByteArray {
        val deflater = Deflater(
            Deflater.BEST_COMPRESSION,
            true
        )

        return try {
            deflater.setInput(data)
            deflater.finish()

            val output = ByteArrayOutputStream(
                data.size
            )

            val buffer = ByteArray(8192)

            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)

                if (count > 0) {
                    output.write(
                        buffer,
                        0,
                        count
                    )
                }
            }

            output.toByteArray()
        } finally {
            deflater.end()
        }
    }

    private fun decompress(
        data: ByteArray
    ): ByteArray {
        val inflater = Inflater(true)

        return try {
            inflater.setInput(data)

            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)

            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)

                if (count > 0) {
                    output.write(
                        buffer,
                        0,
                        count
                    )
                    continue
                }

                if (inflater.needsInput()) {
                    throw IllegalArgumentException(
                        "Datos comprimidos incompletos."
                    )
                }

                if (inflater.needsDictionary()) {
                    throw IllegalArgumentException(
                        "Diccionario de compresión no válido."
                    )
                }

                if (!inflater.finished()) {
                    throw IllegalArgumentException(
                        "Datos comprimidos inválidos."
                    )
                }
            }

            output.toByteArray()
        } finally {
            inflater.end()
        }
    }

    private fun generateQR(
        content: String,
        size: Int
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to CHARACTER_SET,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L,
            EncodeHintType.MARGIN to 2
        )

        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            val rowOffset = y * size

            for (x in 0 until size) {
                pixels[rowOffset + x] =
                    if (bitMatrix[x, y]) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }
            }
        }

        return Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        ).apply {
            setPixels(
                pixels,
                0,
                size,
                0,
                0,
                size,
                size
            )
        }
    }
}