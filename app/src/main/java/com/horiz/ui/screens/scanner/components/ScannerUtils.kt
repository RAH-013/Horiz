package com.horiz.ui.screens.scanner.components

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer

object ScannerUtils {

    fun decodeQrFromBitmap(
        bitmap: Bitmap
    ): String {
        val source = RGBLuminanceSource(
            bitmap.width,
            bitmap.height,
            getPixels(bitmap)
        )

        val hints = mapOf(
            DecodeHintType.POSSIBLE_FORMATS to listOf(
                BarcodeFormat.QR_CODE
            ),
            DecodeHintType.TRY_HARDER to true
        )

        val reader = MultiFormatReader()

        try {
            return reader
                .decode(
                    BinaryBitmap(
                        HybridBinarizer(source)
                    ),
                    hints
                )
                .text
        } catch (_: NotFoundException) {
        }

        return reader
            .decode(
                BinaryBitmap(
                    GlobalHistogramBinarizer(source)
                ),
                hints
            )
            .text
    }

    private fun getPixels(
        bitmap: Bitmap
    ): IntArray {
        val pixels = IntArray(
            bitmap.width * bitmap.height
        )

        bitmap.getPixels(
            pixels,
            0,
            bitmap.width,
            0,
            0,
            bitmap.width,
            bitmap.height
        )

        return pixels
    }
}