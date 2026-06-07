package com.openrec.recorder

import android.content.Context
import androidx.core.content.edit

object Prefs {
    const val NAME = "openrec_prefs"
    private const val KEY_USE_FRONT_CAMERA = "use_front_camera"
    private const val KEY_RECORD_AUDIO = "record_audio"
    private const val KEY_LAUNCHER_ICON = "launcher_icon"
    private const val KEY_DISCRETE_NOTIFICATION = "discrete_notification"

    fun useFrontCamera(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_USE_FRONT_CAMERA, false)

    fun setUseFrontCamera(context: Context, value: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_USE_FRONT_CAMERA, value)
        }
    }

    fun recordAudio(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_RECORD_AUDIO, true)

    fun setRecordAudio(context: Context, value: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_RECORD_AUDIO, value)
        }
    }

    fun launcherIcon(context: Context): String =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAUNCHER_ICON, LauncherIcon.DEFAULT.name)
            ?: LauncherIcon.DEFAULT.name

    fun setLauncherIcon(context: Context, value: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_LAUNCHER_ICON, value)
        }
    }

    fun discreteNotification(context: Context): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DISCRETE_NOTIFICATION, false)

    fun setDiscreteNotification(context: Context, value: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_DISCRETE_NOTIFICATION, value)
        }
    }

    fun getBoolean(context: Context, key: String, default: Boolean): Boolean =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getBoolean(key, default)

    fun setBoolean(context: Context, key: String, value: Boolean) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit {
            putBoolean(key, value)
        }
    }
}