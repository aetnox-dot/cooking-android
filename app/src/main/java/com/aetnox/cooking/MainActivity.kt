package com.aetnox.cooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aetnox.cooking.ui.navigation.CookingNavHost
import com.aetnox.cooking.ui.theme.CookingTheme
import com.aetnox.cooking.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            CookingTheme {
                CookingNavHost()
            }
        }
    }
}
