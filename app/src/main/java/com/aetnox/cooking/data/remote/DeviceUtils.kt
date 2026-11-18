package com.aetnox.cooking.data.remote

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest

/**
 * Формирование фингерпринта устройства для защиты квот от сброса
 * переустановкой APK / очисткой данных (документация, раздел 2.1).
 */
object DeviceUtils {
    private const val SECRET_SALT = "cookin_salt_v1_secure_hash_8941"

    @SuppressLint("HardwareIds")
    fun getHardwareDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "fallback_emul_id"

        val input = "$androidId:$SECRET_SALT"
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
