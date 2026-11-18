package com.aetnox.cooking

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aetnox.cooking.ui.navigation.CookingNavHost
import com.aetnox.cooking.ui.theme.CookingTheme
import com.aetnox.cooking.ui.theme.ThemeManager
import com.aetnox.cooking.ui.util.ActiveCookingManager
import com.aetnox.cooking.ui.util.LogoManager
import com.aetnox.cooking.ui.util.ViewModeManager

class MainActivity : ComponentActivity() {

    private var initialRecipeId by mutableStateOf<Long?>(null)
    private var initialStepIndex by mutableStateOf<Int?>(null)

    override fun attachBaseContext(newBase: android.content.Context) {
        val prefs = newBase.getSharedPreferences("cooking_prefs", MODE_PRIVATE)
        val code = prefs.getString("app_language", "ru") ?: "ru"
        val locale = java.util.Locale.forLanguageTag(code)
        java.util.Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val localizedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        ViewModeManager.init(this)
        LogoManager.init(this)
        com.aetnox.cooking.ui.util.AppIconManager.init(this)
        com.aetnox.cooking.ui.util.AppLocaleManager.init(this)
        ActiveCookingManager.init(this)
        enableEdgeToEdge()

        checkNotificationPermission()
        handleDeepLinkIntent(intent)

        setContent {
            CookingTheme {
                CookingNavHost(
                    initialRecipeId = initialRecipeId,
                    initialStepIndex = initialStepIndex,
                    onNavigatedToDeepLink = {
                        initialRecipeId = null
                        initialStepIndex = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val recipeId = intent?.getLongExtra("OPEN_RECIPE_ID", -1L) ?: -1L
        val stepIndex = intent?.getIntExtra("OPEN_STEP_INDEX", -1) ?: -1
        if (recipeId > 0) {
            initialRecipeId = recipeId
            initialStepIndex = if (stepIndex >= 0) stepIndex else 0
        }
    }
}
