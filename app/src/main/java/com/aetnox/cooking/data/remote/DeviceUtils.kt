package com.aetnox.cooking.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

/**
 * Формирование безопасного фингерпринта устройства для защиты квот.
 * Соль извлекается из BuildConfig (хранится локально в local.properties и не публикуется в Git).
 * В будущем (v2) запланирована миграция на Google ID (Credential Manager).
 */
object DeviceUtils {
    private const val PREFS_NAME = "cooking_secure_device_prefs"
    private const val KEY_INSTALLATION_UUID = "installation_secure_uuid"

    private val SECRET_SALT: String
        get() = com.aetnox.cooking.BuildConfig.DEVICE_SECRET_SALT

    @SuppressLint("HardwareIds")
    fun getHardwareDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "fallback_emul_id"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var installId = prefs.getString(KEY_INSTALLATION_UUID, null)
        if (installId.isNullOrBlank()) {
            installId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_INSTALLATION_UUID, installId).apply()
        }

        val input = "$androidId:$installId:$SECRET_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** Человекочитаемое описание устройства для поля device_info, напр. "Pixel 8 Pro (Android 14)". */
    fun getDeviceInfo(): String {
        val model = Build.MODEL ?: "Unknown"
        return "$model (Android ${Build.VERSION.RELEASE})"
    }

    /** Проверка, является ли устройство гаджетом Nothing OS (Nothing Phone). */
    fun isNothingDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER ?: ""
        val brand = Build.BRAND ?: ""
        val model = Build.MODEL ?: ""
        return manufacturer.contains("Nothing", ignoreCase = true) ||
                brand.contains("Nothing", ignoreCase = true) ||
                model.contains("Nothing", ignoreCase = true)
    }
}
