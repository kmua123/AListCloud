package com.kmuaz.alistcloud.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

object RetrofitClient {

    fun create(baseUrl: String): ApiService {

        var url = baseUrl.trim()

        // 自动补协议
        if (!url.startsWith("http://") &&
            !url.startsWith("https://")) {

            url = "http://$url"
        }

        // 自动补 /
        if (!url.endsWith("/")) {
            url += "/"
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Log.d("AListCloud", "Retrofit URL = $url")

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    }

}