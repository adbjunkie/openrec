package com.openrec.recorder.data

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RecordingsRepository {
    private const val DIR_NAME = "recordings"

    fun recordingsDir(context: Context): File {
        val dir = File(context.getExternalFilesDir(null), DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createOutputFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(recordingsDir(context), "openrec_$timestamp.mp4")
    }

    fun listRecordings(context: Context): List<RecordingFile> =
        recordingsDir(context)
            .listFiles { file -> file.isFile && file.extension.equals("mp4", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                RecordingFile(
                    file = file,
                    name = file.name,
                    sizeBytes = file.length(),
                    modifiedAt = file.lastModified(),
                )
            }
            .orEmpty()

    fun delete(file: File): Boolean = file.delete()
}