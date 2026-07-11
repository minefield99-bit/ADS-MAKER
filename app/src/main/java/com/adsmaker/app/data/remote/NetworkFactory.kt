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

    /**
     * [keyProvider] is read on every request, so a key the user enters in the app
     * takes effect immediately without rebuilding the network stack. When it's
     * blank we omit the header entirely (the request 401s and surfaces a clear
     * "check your key" message rather than sending "Key ").
     */
    fun createFalService(keyProvider: () -> String, enableLogging: Boolean): FalApiService {
        val authInterceptor = okhttp3.Interceptor { chain ->
            val builder = chain.request().newBuilder()
                .header("Accept", "application/json")
            val key = keyProvider().trim()
            if (key.isNotEmpty()) {
                builder.header("Authorization", "Key $key")
            }
            chain.proceed(builder.build())
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
