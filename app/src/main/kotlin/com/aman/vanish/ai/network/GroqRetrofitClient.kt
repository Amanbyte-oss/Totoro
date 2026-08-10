package com.aman.vanish.ai.network

import android.util.Log
import com.aman.vanish.ai.AiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GroqRetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.aman.vanish.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${AiConfig.GROQ_API_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(AiConfig.GROQ_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(AiConfig.GROQ_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(AiConfig.GROQ_REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    val apiService: GroqApiService by lazy {
        Log.d("GROQ_DEBUG", "Initializing GroqRetrofitClient with baseUrl: ${AiConfig.GROQ_BASE_URL}")
        Retrofit.Builder()
            .baseUrl(AiConfig.GROQ_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GroqApiService::class.java)
    }
}
