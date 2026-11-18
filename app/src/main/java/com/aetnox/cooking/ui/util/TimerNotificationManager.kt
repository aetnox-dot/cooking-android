package com.aetnox.cooking.ui.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aetnox.cooking.MainActivity
import com.aetnox.cooking.R

object TimerNotificationManager {
    const val CHANNEL_ID = "cooking_timer_channel"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = AppLocaleManager.tr("Таймеры готовки", "Cooking Timers")
            val channelDesc = AppLocaleManager.tr("Уведомления о завершении кулинарных таймеров", "Notifications when cooking timers complete")
            val channel = NotificationChannel(
                CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showOngoingTimerNotification(
        context: Context,
        recipeId: Long,
        recipeTitle: String,
        stepNumber: Int,
        secondsRemaining: Int,
        timerLabel: String = ""
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val notificationId = ((recipeId.hashCode() * 31) + stepNumber).let { if (it < 0) -it else it }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_RECIPE_ID", recipeId)
            putExtra("OPEN_STEP_INDEX", stepNumber)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addMinIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.aetnox.cooking.ADD_ONE_MINUTE"
            putExtra("RECIPE_ID", recipeId)
            putExtra("STEP_NUMBER", stepNumber)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val addMinPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            addMinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.aetnox.cooking.RESET_TIMER"
            putExtra("RECIPE_ID", recipeId)
            putExtra("STEP_NUMBER", stepNumber)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = com.aetnox.cooking.ui.viewmodel.StepTimerParser.formatTime(secondsRemaining)
        val text = AppLocaleManager.tr("Шаг $stepNumber • Осталось: $formattedTime", "Step $stepNumber • Left: $formattedTime")

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⏱️ $recipeTitle")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, AppLocaleManager.tr("+1 мин", "+1 min"), addMinPendingIntent)
            .addAction(0, AppLocaleManager.tr("Сбросить", "Reset"), resetPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    @SuppressLint("MissingPermission")
    fun sendTimerCompletionNotification(
        context: Context,
        recipeId: Long,
        recipeTitle: String,
        stepNumber: Int,
        timerLabel: String = ""
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                playAudioAlert(context)
                return
            }
        }

        val notificationId = ((recipeId.hashCode() * 31) + stepNumber).let { if (it < 0) -it else it }
        AppLogger.i("TIMER", "Posting system completion notification (id=$notificationId) for recipe '$recipeTitle' (id=$recipeId, step=$stepNumber)")

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_RECIPE_ID", recipeId)
            putExtra("OPEN_STEP_INDEX", stepNumber)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val addMinIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.aetnox.cooking.ADD_ONE_MINUTE"
            putExtra("RECIPE_ID", recipeId)
            putExtra("STEP_NUMBER", stepNumber)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val addMinPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            addMinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val resetIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "com.aetnox.cooking.RESET_TIMER"
            putExtra("RECIPE_ID", recipeId)
            putExtra("STEP_NUMBER", stepNumber)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val resetPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            resetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (stepNumber > 0) {
            AppLocaleManager.tr("Блюдо «$recipeTitle» • Шаг $stepNumber завершён!", "Dish \"$recipeTitle\" • Step $stepNumber completed!")
        } else {
            val fallbackLabel = AppLocaleManager.tr("Таймер", "Timer")
            AppLocaleManager.tr("Блюдо «$recipeTitle» • ${timerLabel.ifBlank { fallbackLabel }} завершён!", "Dish \"$recipeTitle\" • ${timerLabel.ifBlank { fallbackLabel }} completed!")
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(AppLocaleManager.tr("⏱️ Таймер завершён!", "⏱️ Timer completed!"))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, AppLocaleManager.tr("+1 мин", "+1 min"), addMinPendingIntent)
            .addAction(0, AppLocaleManager.tr("Сбросить", "Reset"), resetPendingIntent)

        runCatching {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }

        playAudioAlert(context)
    }

    fun cancelNotification(context: Context, recipeId: Long, stepNumber: Int) {
        val notificationId = ((recipeId.hashCode() * 31) + stepNumber).let { if (it < 0) -it else it }
        runCatching {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }

    fun playAudioAlert(context: Context) {
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, soundUri)
            ringtone?.play()

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }
}
