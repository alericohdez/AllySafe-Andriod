package com.ieselrincon.allysafe.network

import android.content.Context
import com.ieselrincon.allysafe.util.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://localhost:8080/api/"

    fun getApi(context: Context? = null): AllySafeApiService {
        val clientBuilder = OkHttpClient.Builder()

        if (context != null) {
            val sessionManager = SessionManager(context.applicationContext)
            clientBuilder.addInterceptor { chain ->
                val token = sessionManager.obtenerToken()
                val requestBuilder = chain.request().newBuilder()
                if (token.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
        }

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AllySafeApiService::class.java)
    }

    val api: AllySafeApiService by lazy { getApi(null) }
}