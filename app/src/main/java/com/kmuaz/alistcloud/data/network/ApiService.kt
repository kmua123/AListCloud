package com.kmuaz.alistcloud.data.network

import com.kmuaz.alistcloud.data.network.model.LoginRequest
import com.kmuaz.alistcloud.data.network.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import com.kmuaz.alistcloud.data.network.model.FileListRequest
import com.kmuaz.alistcloud.data.network.model.FileListResponse

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(

        @Body request: LoginRequest

    ): LoginResponse
    @POST("api/fs/list")
    suspend fun getFileList(

        @Header("Authorization")
        token: String,

        @Body
        request: FileListRequest

    ): FileListResponse
}
