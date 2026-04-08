package com.example.autoelite.network

import com.example.autoelite_android.data.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://1192.168.1.163:8080/api"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // Aquí puedes añadir un interceptor para pasar el token de Firebase
        .addInterceptor { chain ->
            val original = chain.request()
            // TODO: añadir token Firebase Auth en la cabecera
            val token = runBlocking { FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token }
            val request = original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(request)
        }
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
