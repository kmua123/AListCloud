package com.kmuaz.alistcloud.data.network.model

data class FileItem(

    val name: String,

    val is_dir: Boolean,

    val size: Long,

    val modified: String = "",
    val parent: String? = null

)
