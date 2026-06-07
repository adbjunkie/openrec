package com.openrec.recorder

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class OpenRecApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        LauncherIconManager.ensureApplied(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java) ?: return

        val normal = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }

        val discrete = NotificationChannel(
            CHANNEL_ID_DISCRETE,
            getString(R.string.notification_channel_discrete_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = getString(R.string.notification_channel_discrete_desc)
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(normal)
        manager.createNotificationChannel(discrete)
    }

    companion object {
        const val CHANNEL_ID = "openrec_recording"
        const val CHANNEL_ID_DISCRETE = "openrec_discrete"
    }
}