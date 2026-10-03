package com.kmuaz.alistcloud.data.network

import com.kmuaz.alistcloud.data.network.model.LoginRequest
import com.kmuaz.alistcloud.data.network.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import com.kmuaz.alistcloud.data.network.model.FileListRequest
import com.kmuaz.alistcloud.data.network.model.FileListResponse
import com.kmuaz.alistcloud.data.network.model.FileGetRequest
import com.kmuaz.alistcloud.data.network.model.GetFileResponse

import com.kmuaz.alistcloud.data.network.model.*

interface ApiService {
    @retrofit2.http.PUT("/api/fs/put")
    suspend fun upload(@Header("Authorization") token: String,
        @Header("File-Path") path: String, @Header("As-Task") asTask: String = "false",
        @Header("Overwrite") overwrite: String = "false",
        @Body body: okhttp3.RequestBody): CloudResponse

    @POST("/api/fs/mkdir")
    suspend fun mkdir(@Header("Authorization") token: String, @Body request: Map<String, String>): CloudResponse

    @POST("/api/fs/archive/list")
    suspend fun archiveList(@Header("Authorization") token: String, @Body request: ArchiveListRequest): FileListResponse

    @POST("/api/fs/archive/decompress")
    suspend fun decompress(@Header("Authorization") token: String, @Body request: DecompressRequest): CloudResponse

    @POST("/api/task/decompress/info")
    suspend fun decompressInfo(@Header("Authorization") token: String, @retrofit2.http.Query("tid") id: String): CloudResponse

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("/api/fs/list")
    suspend fun getFileList(
        @Header("Authorization")
        token: String,
        @Body request: FileListRequest
    ): FileListResponse

    @POST("/api/fs/get")
    suspend fun getFile(

        @Header("Authorization")
        token: String,

        @Body
        request: FileGetRequest

    ): GetFileResponse

}
