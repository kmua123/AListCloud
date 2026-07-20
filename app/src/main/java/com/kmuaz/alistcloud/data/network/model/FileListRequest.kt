package com.kmuaz.alistcloud.data.network.model

data class FileListRequest(

    val path: String,

    val password: String = "",

    val page: Int = 1,

    val per_page: Int = 100,

    val refresh: Boolean = false

)