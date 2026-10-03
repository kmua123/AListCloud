package com.kmuaz.alistcloud.data.network.model

data class GetFileResponse(

    val code: Int,

    val message: String,

    val data: GetFileData?

)

data class GetFileData(

    val raw_url: String

)