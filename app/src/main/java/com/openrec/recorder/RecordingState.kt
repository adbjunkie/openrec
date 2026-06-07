package com.openrec.recorder

import android.app.ActivityManager
import android.content.Context
import com.openrec.recorder.service.RecordingService

object RecordingState {
    @Volatile
    var isRecording: Boolean = false
        private set

    fun setRecording(value: Boolean) {
        isRecording = value
    }

    fun isServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Int.MAX_VALUE)
            .any { it.service.className == RecordingService::class.java.name }
    }
}