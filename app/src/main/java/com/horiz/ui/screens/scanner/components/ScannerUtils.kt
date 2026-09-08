package com.horiz.ui.screens.scanner.components

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

object ScannerUtils {

    fun decodeQrFromBitmap(
        bitmap: Bitmap
    ): String {
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

        val source = RGBLuminanceSource(
            bitmap.width,
            bitmap.height,
            pixels
        )

        val binaryBitmap = BinaryBitmap(
            HybridBinarizer(source)
        )

        return MultiFormatReader()
            .decode(binaryBitmap)
            .text
    }
}