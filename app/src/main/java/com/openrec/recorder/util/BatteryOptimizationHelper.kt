package com.openrec.recorder.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.openrec.recorder.Prefs

object BatteryOptimizationHelper {
    private const val PREFS_KEY_DISMISSED = "battery_opt_prompt_dismissed"

    fun isIgnoringOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun shouldPrompt(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        if (isIgnoringOptimizations(context)) return false
        return !Prefs.getBoolean(context, PREFS_KEY_DISMISSED, false)
    }

    fun markPromptDismissed(context: Context) {
        Prefs.setBoolean(context, PREFS_KEY_DISMISSED, true)
    }

    fun requestExemption(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val directIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        if (directIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(directIntent)
            return
        }

        val settingsIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(settingsIntent)
    }
}