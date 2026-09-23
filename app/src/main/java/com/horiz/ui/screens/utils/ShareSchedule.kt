package com.horiz.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.horiz.data.model.Schedule
import com.horiz.storage.ScheduleStorage
import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

object ShareSchedule {

    private const val CHARACTER_SET = "ISO-8859-1"

    private fun compress(text: String): String {
        val input = text.toByteArray(Charsets.UTF_8)

        val deflater = Deflater(
            Deflater.BEST_COMPRESSION,
            true
        )

        return try {
            deflater.setInput(input)
            deflater.finish()

            val output = ByteArrayOutputStream(
                input.size
            )

            val buffer = ByteArray(8192)

            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                output.write(buffer, 0, count)
            }

            String(
                output.toByteArray(),
                Charsets.ISO_8859_1
            )
        } finally {
            deflater.end()
        }
    }

    fun decompress(data: String): String {
        val compressed =
            data.toByteArray(Charsets.ISO_8859_1)

        val inflater = Inflater(true)

        return try {
            inflater.setInput(compressed)

            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)

            while (!inflater.finished()) {
                val count =
                    inflater.inflate(buffer)

                if (count == 0) {
                    if (inflater.needsInput()) {
                        throw IllegalArgumentException(
                            "Datos comprimidos incompletos"
                        )
                    }

                    if (inflater.needsDictionary()) {
                        throw IllegalArgumentException(
                            "Diccionario de compresión no válido"
                        )
                    }
                }

                output.write(
                    buffer,
                    0,
                    count
                )
            }

            String(
                output.toByteArray(),
                Charsets.UTF_8
            )
        } finally {
            inflater.end()
        }
    }

    fun getScheduleText(
        schedule: Schedule,
        storage: ScheduleStorage
    ): String {
        return storage.getSchedule(schedule.name)
            ?.serialize()
            ?: throw IllegalArgumentException(
                "El horario no existe"
            )
    }

    fun createSingleQRCode(
        schedule: Schedule,
        storage: ScheduleStorage,
        size: Int = 768
    ): Bitmap {
        val text = getScheduleText(
            schedule = schedule,
            storage = storage
        )

        val compressed = compress(text)

        return generateQR(
            content = compressed,
            size = size
        )
    }

    private fun generateQR(
        content: String,
        size: Int
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to CHARACTER_SET,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 4
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