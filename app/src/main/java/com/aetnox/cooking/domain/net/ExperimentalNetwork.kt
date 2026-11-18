package com.aetnox.cooking.domain.net

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.aetnox.cooking.domain.ai.GeminiSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.util.concurrent.TimeUnit

enum class DnsMode(val label: String) {
    OFF("Системный DNS (по умолчанию)"),
    CLOUDFLARE("Cloudflare (1.1.1.1)"),
    GOOGLE("Google (8.8.8.8)"),
    QUAD9("Quad9 (9.9.9.9)"),
    CUSTOM("Свой DoH-сервер");
}

enum class AssistantThinkingMode(val label: String, val budget: Int) {
    OFF("Выключены — быстрее и дешевле", 0),
    ECO("Экономные — короткий разбор", 512),
    FULL("Полные — глубокие рассуждения", 2048);
}

data class ExperimentalNetworkSettings(
    val dnsMode: DnsMode = DnsMode.OFF,
    val customDnsUrl: String = "",
    val cookAssistantEnabled: Boolean = true,
    val assistantThinkingMode: AssistantThinkingMode = AssistantThinkingMode.OFF,
    val aiDisabled: Boolean = false,
    val densityHintsEnabled: Boolean = false
)

object ExperimentalSettingsManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_DNS_MODE = "experimental_dns_mode"
    private const val KEY_DNS_CUSTOM_URL = "experimental_dns_custom_url"
    private const val KEY_COOK_ASSISTANT = "experimental_cook_assistant"
    private const val KEY_ASSISTANT_THINKING = "experimental_assistant_thinking"
    private const val KEY_AI_DISABLED = "experimental_ai_disabled"
    private const val KEY_DENSITY_HINTS = "experimental_density_hints"

    // Важно: URL с IP-литералом вместо домена — иначе для разрешения
    // адреса DoH-сервера нужен сам DNS, который мы как раз чиним (bootstrap-петля).
    const val DOH_CLOUDFLARE = "https://1.1.1.1/dns-query"
    const val DOH_GOOGLE = "https://8.8.8.8/dns-query"
    const val DOH_QUAD9 = "https://9.9.9.9/dns-query"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSettings(context: Context): ExperimentalNetworkSettings {
        val prefs = getPrefs(context)
        val modeName = prefs.getString(KEY_DNS_MODE, null)
        val mode = modeName?.let {
            runCatching { DnsMode.valueOf(it) }.getOrNull()
        } ?: DnsMode.OFF

        return ExperimentalNetworkSettings(
            dnsMode = mode,
            customDnsUrl = prefs.getString(KEY_DNS_CUSTOM_URL, null)?.trim().orEmpty(),
            cookAssistantEnabled = prefs.getBoolean(KEY_COOK_ASSISTANT, true),
            assistantThinkingMode = prefs.getString(KEY_ASSISTANT_THINKING, null)
                ?.let { runCatching { AssistantThinkingMode.valueOf(it) }.getOrNull() }
                ?: AssistantThinkingMode.OFF,
            aiDisabled = prefs.getBoolean(KEY_AI_DISABLED, false),
            densityHintsEnabled = prefs.getBoolean(KEY_DENSITY_HINTS, false)
        )
    }

    fun saveDensityHintsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_DENSITY_HINTS, enabled)
            .apply()
    }

    fun saveAiDisabled(context: Context, disabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_AI_DISABLED, disabled)
            .apply()
    }

    fun isAiDisabled(context: Context): Boolean {
        return getSettings(context).aiDisabled
    }

    fun saveAssistantThinkingMode(context: Context, mode: AssistantThinkingMode) {
        getPrefs(context).edit()
            .putString(KEY_ASSISTANT_THINKING, mode.name)
            .apply()
    }

    fun saveCookAssistantEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_COOK_ASSISTANT, enabled)
            .apply()
    }

    fun saveDnsMode(context: Context, mode: DnsMode) {
        getPrefs(context).edit()
            .putString(KEY_DNS_MODE, mode.name)
            .apply()
    }

    fun saveCustomDnsUrl(context: Context, url: String) {
        getPrefs(context).edit()
            .putString(KEY_DNS_CUSTOM_URL, url.trim())
            .apply()
    }
}

/**
 * Фабрика OkHttp-клиентов с применением экспериментальных сетевых настроек.
 * Клиент кэшируется; при смене настроек пересоздаётся.
 */
object AppOkHttp {

    @Volatile
    private var cachedClient: OkHttpClient? = null

    @Volatile
    private var cachedKey: String? = null

    fun client(context: Context): OkHttpClient {
        val netSettings = ExperimentalSettingsManager.getSettings(context)
        val key = "${netSettings.dnsMode}|${netSettings.customDnsUrl}"

        val existing = cachedClient
        if (existing != null && cachedKey == key) {
            return existing
        }

        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        createDns(netSettings)?.let { builder.dns(it) }

        val client = builder.build()
        cachedClient = client
        cachedKey = key
        return client
    }

    fun createDns(settings: ExperimentalNetworkSettings): Dns? {
        val dohUrl = when (settings.dnsMode) {
            DnsMode.OFF -> return null
            DnsMode.CLOUDFLARE -> ExperimentalSettingsManager.DOH_CLOUDFLARE
            DnsMode.GOOGLE -> ExperimentalSettingsManager.DOH_GOOGLE
            DnsMode.QUAD9 -> ExperimentalSettingsManager.DOH_QUAD9
            DnsMode.CUSTOM -> settings.customDnsUrl.ifBlank { return null }
        }

        return try {
            val url = dohUrl.toHttpUrl()
            val bootstrapClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            DnsOverHttps.Builder()
                .client(bootstrapClient)
                .url(url)
                .bootstrapDnsHosts(*bootstrapHostsFor(settings.dnsMode, url))
                .build()
        } catch (e: Exception) {
            null
        }
    }

    private fun bootstrapHostsFor(mode: DnsMode, url: okhttp3.HttpUrl): Array<InetAddress> {
        // Если URL уже с IP-литералом — bootstrap не нужен, OkHttp поймёт его сам.
        val ipList = when (mode) {
            DnsMode.CLOUDFLARE -> listOf("1.1.1.1", "1.0.0.1")
            DnsMode.GOOGLE -> listOf("8.8.8.8", "8.8.4.4")
            DnsMode.QUAD9 -> listOf("9.9.9.9", "149.112.112.112")
            else -> emptyList()
        }
        return ipList.mapNotNull { runCatching { InetAddress.getByName(it) }.getOrNull() }.toTypedArray()
    }
}

object DnsTester {

    suspend fun testResolution(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val geminiSettings = GeminiSettingsManager.getSettings(context)
            val host = Uri.parse(geminiSettings.customBaseUrl).host
                ?: "generativelanguage.googleapis.com"

            val netSettings = ExperimentalSettingsManager.getSettings(context)
            val dns = AppOkHttp.createDns(netSettings) ?: Dns.SYSTEM

            val startMs = System.currentTimeMillis()
            val addresses = dns.lookup(host)
            val elapsedMs = System.currentTimeMillis() - startMs

            if (addresses.isEmpty()) {
                Result.failure(IllegalStateException("DNS-сервер не вернул адресов для $host"))
            } else {
                val ips = addresses.joinToString(", ") { it.hostAddress ?: "?" }
                val modeLabel = if (netSettings.dnsMode == DnsMode.OFF) "системный DNS" else netSettings.dnsMode.label
                Result.success("✅ $host разрешён через $modeLabel за ${elapsedMs} мс:\n$ips")
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("❌ ${e.localizedMessage ?: e.javaClass.simpleName}"))
        }
    }
}
