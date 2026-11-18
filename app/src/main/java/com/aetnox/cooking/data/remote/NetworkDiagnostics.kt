package com.aetnox.cooking.data.remote

import android.content.Context
import com.aetnox.cooking.ui.util.AppLocaleManager
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Интеллектуальный диагностический модуль сетевых ошибок.
 * Различает:
 *  1. Полное отсутствие сети на устройстве (нет Wi-Fi / Mobile Data)
 *  2. Доступность интернета, но недоступность/блокировку сервера (рекомендация VPN / DoH DNS)
 *  3. Истечение таймаута соединения (30 сек)
 *  4. Ошибки шлюза, SSL и техническое обслуживание (503 / 50x)
 */
object NetworkDiagnostics {

    fun formatErrorMessage(context: Context, throwable: Throwable): String {
        val isOnline = NetworkObserver.isOnline(context)

        // 1. Устройство физически отключено от сети
        if (!isOnline) {
            return AppLocaleManager.tr(
                "📡 Нет подключения к сети. Проверьте Wi-Fi или мобильный интернет.",
                "📡 No network connection. Please check your Wi-Fi or mobile data."
            )
        }

        val cause = throwable.cause ?: throwable

        // 2. Интернет есть, но возникла специфическая сетевая ошибка
        return when {
            cause is UnknownHostException ->
                AppLocaleManager.tr(
                    "🌐 Сервер приложения недоступен или заблокирован провайдером. Попробуйте включить VPN или настроить DNS (DoH) в настройках приложения.",
                    "🌐 App server is unavailable or blocked by ISP. Try enabling VPN or configuring DNS (DoH) in app settings."
                )

            cause is SocketTimeoutException ->
                AppLocaleManager.tr(
                    "⏱ Превышено время ожидания ответа от сервера (таймаут 30 сек). Сервер не ответил вовремя.",
                    "⏱ Server response timed out (30 sec timeout). The server did not respond in time."
                )

            cause is ConnectException ->
                AppLocaleManager.tr(
                    "🔌 Не удалось установить соединение с сервером. Возможно, адрес заблокирован — попробуйте включить VPN.",
                    "🔌 Could not connect to the server. The address might be blocked — try enabling VPN."
                )

            cause is SSLException ->
                AppLocaleManager.tr(
                    "🔒 Ошибка безопасного соединения (SSL/TLS). Возможна блокировка защищенного шлюза — попробуйте включить VPN.",
                    "🔒 Secure connection error (SSL/TLS). Protected gateway might be blocked — try enabling VPN."
                )

            throwable is ApiException -> {
                when {
                    throwable.httpCode == 503 || throwable.errorCode == "MAINTENANCE" ->
                        AppLocaleManager.tr(
                            "🛠 На сервере идут плановые технические работы. Пожалуйста, попробуйте позже.",
                            "🛠 Server is undergoing scheduled maintenance. Please try again later."
                        )
                    throwable.errorCode == "QUOTA_EXCEEDED" ->
                        AppLocaleManager.tr(
                            "📊 Превышен месячный лимит витаминов ИИ. Квоты обновятся в начале следующего месяца.",
                            "📊 Monthly AI vitamins quota exceeded. Quotas will reset at the beginning of next month."
                        )
                    throwable.errorCode == "PHOTO_QUOTA_EXCEEDED" ->
                        AppLocaleManager.tr(
                            "📸 Превышен месячный лимит фото-оцифровок. Квоты обновятся в начале следующего месяца.",
                            "📸 Monthly photo scan quota exceeded. Quotas will reset at the beginning of next month."
                        )
                    throwable.errorCode == "CLOUD_QUOTA_EXCEEDED" ->
                        AppLocaleManager.tr(
                            "☁️ Достигнут общий лимит хранения облачных рецептов.",
                            "☁️ Cloud recipe storage limit reached."
                        )
                    throwable.errorCode == "FREE_TIER_QUARANTINE" ->
                        AppLocaleManager.tr(
                            "🛡️ Аккаунт находится в карантине после переноса подписки. Активируйте ключ доступа для разблокировки.",
                            "🛡️ Account is in quarantine following subscription transfer. Activate an access key to unlock."
                        )
                    throwable.httpCode in 500..599 ->
                        AppLocaleManager.tr(
                            "⚠️ Временный сбой на сервере (код ${throwable.httpCode}). Попробуйте повторить запрос.",
                            "⚠️ Temporary server error (code ${throwable.httpCode}). Please retry your request."
                        )
                    else -> throwable.message ?: AppLocaleManager.tr(
                        "Сетевая ошибка (код ${throwable.httpCode})",
                        "Network error (code ${throwable.httpCode})"
                    )
                }
            }

            else -> AppLocaleManager.tr(
                "Сетевая ошибка: ${throwable.localizedMessage ?: "неизвестный сбой соединения"}",
                "Network error: ${throwable.localizedMessage ?: "unknown connection failure"}"
            )
        }
    }
}
