package com.openrec.recorder.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingFile(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val modifiedAt: Long,
) {
    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "%.1f KB".format(sizeBytes / 1024.0)
            else -> "%.1f MB".format(sizeBytes / (1024.0 * 1024.0))
        }

    val formattedDate: String
        get() = SimpleDateFormat("MMM d, yyyy · HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
}