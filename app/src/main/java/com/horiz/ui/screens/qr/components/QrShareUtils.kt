package com.horiz.ui.screens.qr.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object QrShareUtils {

    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        scheduleName: String
    ) {
        val imagesDir = File(
            context.cacheDir,
            "shared_images"
        ).apply {
            mkdirs()
        }

        val safeName = scheduleName
            .replace(
                Regex("[^a-zA-Z0-9._-]"),
                "_"
            )

        val file = File(
            imagesDir,
            "$safeName.jpg"
        )

        FileOutputStream(file).use { output ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                100,
                output
            )
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(
            Intent.ACTION_SEND
        ).apply {
            type = "image/jpeg"

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            putExtra(
                Intent.EXTRA_TEXT,
                "Estoy usando Horiz ⌚ para organizar mis horarios.\n" +
                        "Te comparto mi horario \"$scheduleName\" " +
                        "para que también lo uses. 📅✨"
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val chooser = Intent.createChooser(
            shareIntent,
            "Compartir horario mediante otras apps"
        )

        chooser.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
        )

        context.startActivity(chooser)
    }
}