package com.kmuaz.alistcloud.data.network.model

data class FileListResponse(

    val code: Int,

    val message: String,

    val data: FileListData?

)

data class FileListData(

    val content: List<FileItem> = emptyList(),
    val total: Long = 0

)
