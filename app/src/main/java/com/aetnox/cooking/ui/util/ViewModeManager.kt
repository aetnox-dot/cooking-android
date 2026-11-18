package com.aetnox.cooking.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.aetnox.cooking.ui.component.ViewMode

object ViewModeManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_VIEW_MODE = "app_view_mode"

    val currentViewMode = mutableStateOf(ViewMode.GRID)

    fun init(context: Context) {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_VIEW_MODE, ViewMode.GRID.name)
        currentViewMode.value = runCatching { ViewMode.valueOf(name!!) }.getOrDefault(ViewMode.GRID)
    }

    fun setViewMode(context: Context, mode: ViewMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_VIEW_MODE, mode.name)
            .apply()
        currentViewMode.value = mode
    }
}
