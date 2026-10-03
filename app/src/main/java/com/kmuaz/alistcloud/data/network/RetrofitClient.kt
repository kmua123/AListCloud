package com.kmuaz.alistcloud.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
            level = HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (chain.request().url.encodedPath.startsWith("/api/") && response.isSuccessful &&
                    response.header("Content-Type").orEmpty().contains("text/html")) {
                    response.close()
                    throw java.io.IOException("服务器不支持此接口：${chain.request().url.encodedPath}，请检查 AList 版本")
                }
                response
            }
            .addInterceptor(logging).connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS).readTimeout(120, java.util.concurrent.TimeUnit.SECONDS).writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    }

}
