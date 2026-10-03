package com.kmuaz.alistcloud.data.repository

import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.FileListRequest
import com.kmuaz.alistcloud.data.network.model.FileListResponse
import com.kmuaz.alistcloud.data.network.model.FileGetRequest
import com.kmuaz.alistcloud.data.network.model.GetFileResponse

class FileRepository {

    suspend fun getFiles(
        server: String,
        token: String,
        path: String
    ): FileListResponse {

        return RetrofitClient
            .create(server)
            .getFileList(

                token = token,

                request = FileListRequest(
                    path = path
                )

            )

    }

    suspend fun getFile(

        server: String,

        token: String,

        path: String

    ): GetFileResponse {

        return RetrofitClient
            .create(server)
            .getFile(

                token = token,

                request = FileGetRequest(

                    path = path

                )

            )

    }

}