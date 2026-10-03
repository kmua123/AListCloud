package com.kmuaz.alistcloud.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.*
import kotlinx.coroutines.ensureActive
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

fun cloudPath(parent: String, name: String): String = parent.trimEnd('/') + "/" + name
fun FileItem.fullPath(currentPath: String): String = cloudPath(parent ?: currentPath, name)

data class LocalUpload(val uri: Uri, val name: String, val size: Long)
fun ContentResolver.describeUpload(uri: Uri): LocalUpload {
    var name = ""; var size = -1L
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            name = cursor.getString(0).orEmpty()
            if (!cursor.isNull(1)) size = cursor.getLong(1)
        }
    }
    require(name.isNotBlank() && name !in setOf(".", "..") && !name.contains('/') && !name.contains('\\')) { "无效的文件名" }
    return LocalUpload(uri, name, size)
}

class CloudRepository {
    suspend fun listAll(server: String, token: String, path: String, refresh: Boolean = false): List<FileItem> {
        val api = RetrofitClient.create(server)
        val items = mutableListOf<FileItem>()
        var page = 1
        do {
            val response = api.getFileList(token, FileListRequest(path, page = page, per_page = 500, refresh = refresh && page == 1))
            check(response.code == 200) { response.message }
            val batch = response.data?.content.orEmpty()
            val previousSize = items.size
            items += batch.map { it.copy(parent = path) }.filter { next -> items.none { it.name == next.name } }
            val total = response.data?.total ?: 0
            if (batch.isEmpty() || (total > 0 && items.size >= total) || (total <= 0 && batch.size < 500) || items.size == previousSize) break
            page++
        } while (true)
        return items
    }
    suspend fun upload(server: String, token: String, destination: String, file: LocalUpload,
        resolver: ContentResolver, coroutine: CoroutineContext, progress: (Long, Long) -> Unit) {
        val body = object : RequestBody() {
            override fun contentType() = (resolver.getType(file.uri) ?: "application/octet-stream").toMediaTypeOrNull()
            override fun contentLength() = file.size
            override fun writeTo(sink: BufferedSink) {
                resolver.openInputStream(file.uri)?.use { stream ->
                    val buffer = ByteArray(64 * 1024); var sent = 0L
                    while (true) {
                        coroutine.ensureActive()
                        val count = stream.read(buffer)
                        if (count < 0) break
                        sink.write(buffer, 0, count); sent += count
                        progress(sent, file.size)
                    }
                } ?: error("无法读取手机文件")
            }
        }
        val encodedPath = URLEncoder.encode(cloudPath(destination, file.name), "UTF-8").replace("+", "%20")
        val response = RetrofitClient.create(server).upload(token, encodedPath, body = body)
        check(response.code == 200) { response.message }
    }
}
