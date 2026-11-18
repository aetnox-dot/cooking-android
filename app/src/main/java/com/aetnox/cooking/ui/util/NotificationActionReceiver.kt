package com.aetnox.cooking.ui.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val recipeId = intent.getLongExtra("RECIPE_ID", -1L)
        val stepNumber = intent.getIntExtra("STEP_NUMBER", -1)
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)

        if (notificationId != -1) {
            runCatching {
                NotificationManagerCompat.from(context).cancel(notificationId)
            }
        }

        when (intent.action) {
            "com.aetnox.cooking.ADD_ONE_MINUTE" -> {
                if (recipeId > 0 && stepNumber >= 0) {
                    ActiveCookingManager.addSecondsToTimer(
                        context,
                        recipeId,
                        stepNumber,
                        60
                    )
                }
            }
            "com.aetnox.cooking.RESET_TIMER" -> {
                if (recipeId > 0 && stepNumber >= 0) {
                    ActiveCookingManager.resetTimer(context, recipeId, stepNumber)
                }
            }
        }
    }
}
