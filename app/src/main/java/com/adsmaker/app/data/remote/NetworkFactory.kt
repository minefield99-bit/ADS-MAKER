package com.adsmaker.app.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Builds the fal.ai [FalApiService]. The API key is injected once here as an
 * `Authorization: Key <key>` header on every request; it never touches the DTOs
 * or the UI layer.
 */
object NetworkFactory {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun createFalService(apiKey: String, enableLogging: Boolean): FalApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Key $apiKey")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            // Generations are long-running; keep read timeouts generous.
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)

        if (enableLogging) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
        }

        return Retrofit.Builder()
            .baseUrl(FalConfig.BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FalApiService::class.java)
    }
}
