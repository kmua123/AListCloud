package com.kmuaz.alistcloud.data.network.model

import com.google.gson.JsonObject
import com.google.gson.JsonElement

data class CloudResponse(val code: Int, val message: String, val data: JsonElement?) {
    val objectData: JsonObject? get() = data?.takeIf { it.isJsonObject }?.asJsonObject
}
data class ArchiveListRequest(val path: String, val inner_path: String = "/", val archive_pass: String = "",
    val password: String = "", val page: Int = 1, val per_page: Int = 500, val refresh: Boolean = false)
data class DecompressRequest(val src_dir: String, val dst_dir: String, val name: List<String>,
    val archive_pass: String = "", val inner_path: String = "/", val put_into_new_dir: Boolean = false,
    val cache_full: Boolean = false)
