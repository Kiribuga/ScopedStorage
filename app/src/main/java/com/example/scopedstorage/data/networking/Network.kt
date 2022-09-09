package com.example.scopedstorage.data.networking

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create

object Network {

    private val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(
            HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY)
        )
        .followRedirects(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://google.com")
        .client(okHttpClient)
        .build()

    val api: Api
        get() = retrofit.create()
}