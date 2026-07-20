package com.kmuaz.alistcloud.data.repository

import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.LoginRequest
import com.kmuaz.alistcloud.data.network.model.LoginResponse
import android.util.Log

class LoginRepository {

    suspend fun login(
        server: String,
        username: String,
        password: String
    ): LoginResponse {


        val api = RetrofitClient.create(server)

        Log.d("AListCloud", "Repository server = $server")

        return api.login(
            LoginRequest(
                username = username,
                password = password
            )
        )
    }
}