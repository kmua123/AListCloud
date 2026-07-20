package com.kmuaz.alistcloud.data.repository

import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.FileListRequest
import com.kmuaz.alistcloud.data.network.model.FileListResponse

class FileRepository {

    suspend fun getRootFiles(
        server: String,
        token: String
    ): FileListResponse {

        return RetrofitClient
            .create(server)
            .getFileList(

                token = token,

                request = FileListRequest(
                    path = "/"
                )

            )

    }

}