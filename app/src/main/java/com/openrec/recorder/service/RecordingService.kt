package com.openrec.recorder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.openrec.recorder.OpenRecApp
import com.openrec.recorder.Prefs
import com.openrec.recorder.RecordingState
import com.openrec.recorder.R
import com.openrec.recorder.camera.CameraRecorder
import com.openrec.recorder.data.RecordingsRepository
import com.openrec.recorder.ui.MainActivity
import java.io.File
import java.util.concurrent.Executors

class RecordingService : LifecycleService(), CameraRecorder.Listener {

    private var cameraRecorder: CameraRecorder? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentFile: File? = null
    private var isRecording = false

    override fun onBind(intent: Intent): IBinder? = super.onBind(intent)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
        }
        return START_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        val outputFile = RecordingsRepository.createOutputFile(this)
        currentFile = outputFile
        isRecording = true
        RecordingState.setRecording(true)

        acquireWakeLock()
        startForegroundWithNotification()

        val executor = Executors.newSingleThreadExecutor()
        cameraRecorder = CameraRecorder(this, this, mainExecutor).also { recorder ->
            recorder.setListener(this)
            recorder.bind(
                useFrontCamera = Prefs.useFrontCamera(this),
                recordAudio = Prefs.recordAudio(this),
                outputFile = outputFile,
            )
        }
        executor.shutdown()
    }

    private fun stopRecording() {
        if (!isRecording) {
            stopSelf()
            return
        }
        cameraRecorder?.stop()
    }

    override fun onRecordingStarted(outputFile: File) {
        updateNotification(recording = true)
        sendBroadcast(Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_IS_RECORDING, true))
    }

    override fun onRecordingStopped(outputFile: File) {
        teardown()
        sendBroadcast(
            Intent(ACTION_STATE_CHANGED)
                .putExtra(EXTRA_IS_RECORDING, false)
                .putExtra(EXTRA_FILE_PATH, outputFile.absolutePath)
        )
        stopSelf()
    }

    override fun onError(message: String) {
        teardown()
        sendBroadcast(
            Intent(ACTION_STATE_CHANGED)
                .putExtra(EXTRA_IS_RECORDING, false)
                .putExtra(EXTRA_ERROR, message)
        )
        stopSelf()
    }

    private fun teardown() {
        isRecording = false
        RecordingState.setRecording(false)
        cameraRecorder?.release()
        cameraRecorder = null
        releaseWakeLock()
        currentFile = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        cameraRecorder?.release()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification(recording = true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(recording: Boolean) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, buildNotification(recording))
    }

    private fun buildNotification(recording: Boolean): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Prefs.discreteNotification(this)) {
            return NotificationCompat.Builder(this, DISCRETE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_silent)
                .setContentTitle(getString(R.string.notification_discrete_title))
                .setContentText(getString(R.string.notification_discrete_text))
                .setContentIntent(openAppIntent)
                .setOngoing(recording)
                .setOnlyAlertOnce(true)
                .setSilent(true)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                .build()
        }

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RecordingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(
                if (recording) getString(R.string.notification_recording)
                else getString(R.string.notification_idle)
            )
            .setContentIntent(openAppIntent)
            .setOngoing(recording)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(R.drawable.ic_stop, getString(R.string.stop_recording), stopIntent)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(PowerManager::class.java) ?: return
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "OpenRec::RecordingWakeLock"
        ).apply { acquire(4 * 60 * 60 * 1000L) }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    companion object {
        const val ACTION_START = "com.openrec.recorder.START"
        const val ACTION_STOP = "com.openrec.recorder.STOP"
        const val ACTION_STATE_CHANGED = "com.openrec.recorder.STATE_CHANGED"
        const val EXTRA_IS_RECORDING = "is_recording"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_ERROR = "error"

        private const val CHANNEL_ID = OpenRecApp.CHANNEL_ID
        private const val DISCRETE_CHANNEL_ID = OpenRecApp.CHANNEL_ID_DISCRETE
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, RecordingService::class.java).setAction(ACTION_START)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RecordingService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}