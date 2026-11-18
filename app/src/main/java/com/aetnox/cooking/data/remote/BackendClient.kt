package com.aetnox.cooking.data.remote

import android.content.Context
import com.aetnox.cooking.BuildConfig
import com.aetnox.cooking.data.remote.model.AuthInitRequest
import com.aetnox.cooking.data.remote.model.AuthInitResponse
import com.aetnox.cooking.domain.net.AppOkHttp
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Сборка сетевого стека Cooking Backend (документация, раздел 7):
 *  - AuthInterceptor: Bearer-токен на все запросы, кроме /app/config и /auth/init;
 *  - VersionInterceptor: X-App-Version на каждый запрос;
 *  - TokenAuthenticator: при 401 фоново вызывает /auth/init и повторяет запрос.
 */
object BackendClient {

    const val BASE_URL = "https://cooking.aetnox.workers.dev/api/v1/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Пути, которым не нужен Bearer-токен. */
    private val PUBLIC_PATHS = setOf("app/config", "auth/init")

    private fun isPublic(request: Request): Boolean {
        val path = request.url.encodedPath.removePrefix("/api/v1/")
        return path in PUBLIC_PATHS
    }

    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (isPublic(request)) return chain.proceed(request)

            val token = SessionManager.getTokenBlocking()
                ?: return chain.proceed(request)

            return chain.proceed(
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            )
        }
    }

    class VersionInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            return chain.proceed(
                chain.request().newBuilder()
                    .header("X-App-Version", BuildConfig.VERSION_CODE.toString())
                    .header("Content-Type", "application/json")
                    .build()
            )
        }
    }

    /**
     * Компактное логирование сетевых запросов и ответов в Logcat (тег CookingNetwork).
     */
    class DetailedLoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val startTime = System.currentTimeMillis()

            val reqMsg = StringBuilder()
            reqMsg.append("▶️ [REQUEST] ${request.method} ${request.url}")

            val reqBody = request.body
            if (reqBody != null) {
                try {
                    val buffer = okio.Buffer()
                    reqBody.writeTo(buffer)
                    val bodyString = buffer.readUtf8()
                    val truncatedBody = if (bodyString.length > 800) bodyString.take(800) + "... [всего ${bodyString.length} симв.]" else bodyString
                    reqMsg.append("\nPayload: ").append(truncatedBody)
                } catch (e: Exception) {
                    reqMsg.append("\nPayload: [Ошибка чтения тела запроса: ${e.message}]")
                }
            }
            android.util.Log.d("CookingNetwork", reqMsg.toString())

            return try {
                val response = chain.proceed(request)
                val duration = System.currentTimeMillis() - startTime

                val respMsg = StringBuilder()
                respMsg.append("◀️ [RESPONSE ${response.code}] ${request.method} ${request.url} (${duration} ms)")

                val responseBody = response.body
                if (responseBody != null) {
                    try {
                        val source = responseBody.source()
                        source.request(Long.MAX_VALUE)
                        val buffer = source.buffer
                        val bodyString = buffer.clone().readUtf8()
                        val truncatedResp = if (bodyString.length > 1000) bodyString.take(1000) + "... [всего ${bodyString.length} симв.]" else bodyString
                        respMsg.append("\nBody: ").append(truncatedResp)
                    } catch (e: Exception) {
                        respMsg.append("\nBody: [Ошибка чтения тела ответа: ${e.message}]")
                    }
                }
                android.util.Log.d("CookingNetwork", respMsg.toString())
                response
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                android.util.Log.e("CookingNetwork", "❌ [NETWORK ERROR] ${request.method} ${request.url} (${duration} ms): ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    /**
     * Обработка 401: синхронный вызов /auth/init (другим клиентом, без интерсепторов),
     * сохранение нового токена и повтор оригинального запроса.
     */
    class TokenAuthenticator(
        private val appContext: Context
    ) : Authenticator {

        private val refreshLock = Any()

        override fun authenticate(route: Route?, response: Response): Request? {
            // Не уходим в рекурсию на самом /auth/init.
            if (isPublic(response.request)) return null
            // Уже пробовали повторить — токен действительно невалиден.
            if (responseCount(response) >= 2) return null

            val newToken = synchronized(refreshLock) {
                // Возможно, параллельный поток уже обновил токен.
                val current = SessionManager.getTokenBlocking()
                val failedToken = response.request.header("Authorization")
                    ?.removePrefix("Bearer ")
                if (current != null && current != failedToken) {
                    return@synchronized current
                }
                refreshTokenBlocking()
            } ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }

        private fun refreshTokenBlocking(): String? {
            return try {
                val deviceId = DeviceUtils.getHardwareDeviceId(appContext)
                val body = json.encodeToString(
                    AuthInitRequest.serializer(),
                    AuthInitRequest(deviceId = deviceId, deviceInfo = DeviceUtils.getDeviceInfo())
                )
                val request = Request.Builder()
                    .url(BASE_URL + "auth/init")
                    .header("X-App-Version", BuildConfig.VERSION_CODE.toString())
                    .header("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                plainClient(appContext).newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) return null
                    val payload = resp.body?.string() ?: return null
                    val parsed = json.decodeFromString(AuthInitResponse.serializer(), payload)
                    if (!parsed.success) return null

                    // Authenticator синхронный — пишем в DataStore блокирующе.
                    kotlinx.coroutines.runBlocking {
                        SessionManager.saveSession(
                            appContext,
                            parsed.sessionToken,
                            parsed.accountId,
                            parsed.planLevel,
                            parsed.subscriptionUntil
                        )
                    }
                    parsed.sessionToken
                }
            } catch (e: Exception) {
                null
            }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }

    /** Базовый клиент с DNS-настройками и подробным логированием. */
    private fun baseBuilder(context: Context): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(DetailedLoggingInterceptor())

        val netSettings = ExperimentalSettingsManager.getSettings(context)
        AppOkHttp.createDns(netSettings)?.let { builder.dns(it) }
        return builder
    }

    /** Клиент без интерсепторов — для служебного /auth/init внутри Authenticator. */
    private fun plainClient(context: Context): OkHttpClient = baseBuilder(context).build()

    @Volatile
    private var cachedApi: CookingApiService? = null

    @Volatile
    private var cachedDnsKey: String? = null

    fun api(context: Context): CookingApiService {
        val netSettings = ExperimentalSettingsManager.getSettings(context)
        val dnsKey = "${netSettings.dnsMode}|${netSettings.customDnsUrl}"

        cachedApi?.let { if (cachedDnsKey == dnsKey) return it }

        synchronized(this) {
            cachedApi?.let { if (cachedDnsKey == dnsKey) return it }

            val client = baseBuilder(context)
                .addInterceptor(AuthInterceptor())
                .addInterceptor(VersionInterceptor())
                .authenticator(TokenAuthenticator(context.applicationContext))
                .build()

            val api = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CookingApiService::class.java)

            cachedApi = api
            cachedDnsKey = dnsKey
            return api
        }
    }
}
