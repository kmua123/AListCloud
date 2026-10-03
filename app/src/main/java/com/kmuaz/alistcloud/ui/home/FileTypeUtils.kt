package com.kmuaz.alistcloud.ui.home

import com.kmuaz.alistcloud.data.network.model.FileItem

fun FileItem.isImage(): Boolean {

    if (is_dir) return false

    val ext = name.substringAfterLast('.', "")
        .lowercase()

    return ext in setOf(
        "jpg",
        "jpeg",
        "png",
        "gif",
        "webp",
        "bmp",
        "heic",
        "heif"
    )

}
