package com.openrec.recorder

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

enum class LauncherIcon(
    val aliasClass: String,
    val labelRes: Int,
    val previewRes: Int,
) {
    DEFAULT(
        aliasClass = "com.openrec.recorder.LauncherDefault",
        labelRes = R.string.app_name,
        previewRes = R.mipmap.ic_launcher,
    ),
    CALCULATOR(
        aliasClass = "com.openrec.recorder.LauncherCalculator",
        labelRes = R.string.icon_label_calculator,
        previewRes = R.mipmap.ic_launcher_calculator,
    ),
    NOTES(
        aliasClass = "com.openrec.recorder.LauncherNotes",
        labelRes = R.string.icon_label_notes,
        previewRes = R.mipmap.ic_launcher_notes,
    ),
    WEATHER(
        aliasClass = "com.openrec.recorder.LauncherWeather",
        labelRes = R.string.icon_label_weather,
        previewRes = R.mipmap.ic_launcher_weather,
    ),
    CLOCK(
        aliasClass = "com.openrec.recorder.LauncherClock",
        labelRes = R.string.icon_label_clock,
        previewRes = R.mipmap.ic_launcher_clock,
    ),
}

object LauncherIconManager {
    fun current(context: Context): LauncherIcon {
        val saved = Prefs.launcherIcon(context)
        return LauncherIcon.entries.find { it.name == saved } ?: LauncherIcon.DEFAULT
    }

    fun ensureApplied(context: Context) {
        apply(context, current(context))
    }

    fun apply(context: Context, icon: LauncherIcon) {
        val packageManager = context.packageManager
        LauncherIcon.entries.forEach { entry ->
            val component = ComponentName(context.packageName, entry.aliasClass)
            val state = if (entry == icon) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            packageManager.setComponentEnabledSetting(
                component,
                state,
                PackageManager.DONT_KILL_APP,
            )
        }
        Prefs.setLauncherIcon(context, icon.name)
    }
}