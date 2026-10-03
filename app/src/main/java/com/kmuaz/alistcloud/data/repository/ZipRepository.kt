package com.kmuaz.alistcloud.data.repository

import android.content.Context
import androidx.core.content.FileProvider
import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.FileGetRequest
import com.kmuaz.alistcloud.data.network.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.zip.ZipFile
import kotlin.coroutines.CoroutineContext

/** Standard ZIP fallback for servers without archive endpoints. Paths are validated before any extraction. */
class ZipRepository(private val context: Context) {
    suspend fun download(server: String, token: String, path: String): File = withContext(Dispatchers.IO) {
        val response = RetrofitClient.create(server).getFile(token, FileGetRequest(path))
        check(response.code == 200) { response.message }
        val directory = File(context.cacheDir, "documents").apply { mkdirs() }
        val target = File.createTempFile("archive-", ".zip", directory)
        try {
            OkHttpClient().newCall(Request.Builder().url(response.data!!.raw_url).build()).execute().use { network ->
                check(network.isSuccessful) { "无法下载压缩包：HTTP ${network.code}" }
                val body = network.body ?: error("压缩包为空")
                check(body.contentLength() <= MAX_COMPRESSED_BYTES) { "本地 ZIP 模式最大支持 256 MB，请使用云端解压或其他应用" }
                body.byteStream().use { input -> target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024); var total = 0L
                    while (true) {
                        coroutineContext.ensureActive()
                        val count = input.read(buffer); if (count < 0) break
                        total += count; check(total <= MAX_COMPRESSED_BYTES) { "压缩包超过本地模式 256 MB 限制" }
                        output.write(buffer, 0, count)
                    }
                } }
            }
            ZipFile(target).use { zip -> check(zip.size() <= 10000) { "压缩包条目过多" }; zip.entries().asSequence().forEach { validateZipPath(it.name) } }
            target
        } catch (e: Exception) { target.delete(); throw e }
    }
    fun list(file: File, innerPath: String): List<FileItem> = ZipFile(file).use { zip ->
        val prefix = innerPath.trim('/').let { if (it.isEmpty()) "" else "$it/" }
        val children = linkedMapOf<String, FileItem>()
        zip.entries().asSequence().forEach { entry ->
            val path = validateZipPath(entry.name)
            if (path.startsWith(prefix)) {
                val remaining = path.removePrefix(prefix)
                if (remaining.isNotEmpty()) {
                    val name = remaining.substringBefore('/')
                    val directory = remaining.contains('/') || entry.isDirectory
                    children[name] = FileItem(name, directory, if (directory) 0 else entry.size.coerceAtLeast(0))
                }
            }
        }
        children.values.sortedWith(compareBy<FileItem> { !it.is_dir }.thenBy { it.name })
    }
    suspend fun decompressToCloud(file: File, server: String, token: String, destination: String,
        coroutine: CoroutineContext, progress: (Int, Int) -> Unit) = withContext(Dispatchers.IO) {
        val api = RetrofitClient.create(server); val cloud = CloudRepository()
        val createdDirs = mutableSetOf(""); val uploadedPaths = mutableSetOf<String>()
        var totalBytes = 0L; var processed = 0
        ZipFile(file).use { zip ->
            val entries = zip.entries().asSequence().toList()
            check(entries.size <= 10000) { "压缩包条目过多" }
            entries.forEach { validateZipPath(it.name) }
            entries.forEach { entry ->
                coroutine.ensureActive()
                val path = validateZipPath(entry.name)
                val directory = if (entry.isDirectory) path.trimEnd('/') else path.substringBeforeLast('/', "")
                var relative = ""
                directory.split('/').filter { it.isNotEmpty() }.forEach { segment ->
                    relative = if (relative.isBlank()) segment else "$relative/$segment"
                    if (createdDirs.add(relative)) {
                        val mkdir = api.mkdir(token, mapOf("path" to cloudPath(destination, relative)))
                        check(mkdir.code == 200) { mkdir.message }
                    }
                }
                if (!entry.isDirectory) {
                    check(uploadedPaths.add(path)) { "压缩包包含重复文件：$path" }
                    val cache = File(context.cacheDir, "documents").apply { mkdirs() }
                    val extracted = File.createTempFile("unzip-", ".tmp", cache)
                    try {
                        zip.getInputStream(entry).use { input -> extracted.outputStream().use { output ->
                            val buffer = ByteArray(64 * 1024)
                            while (true) {
                                coroutine.ensureActive()
                                val count = input.read(buffer); if (count < 0) break
                                totalBytes += count; check(totalBytes <= MAX_EXTRACTED_BYTES) { "本地模式解压总量超过 1 GB，请使用服务器解压" }
                                output.write(buffer, 0, count)
                            }
                        } }
                        val uri = FileProvider.getUriForFile(context, context.packageName + ".files", extracted)
                        cloud.upload(server, token, if (directory.isBlank()) destination else cloudPath(destination, directory),
                            LocalUpload(uri, path.substringAfterLast('/'), extracted.length()), context.contentResolver, coroutine) { _, _ -> }
                    } finally { extracted.delete() }
                }
                processed++; progress(processed, entries.size)
            }
        }
    }
    companion object {
        const val MAX_COMPRESSED_BYTES = 256L * 1024 * 1024
        const val MAX_EXTRACTED_BYTES = 1024L * 1024 * 1024
    }
}
fun validateZipPath(name: String): String {
    require(name.isNotBlank() && !name.startsWith('/') && !name.contains('\\') && !name.contains(':') && !name.contains('\u0000')) { "压缩包包含不安全路径" }
    require(name.trimEnd('/').split('/').none { it.isBlank() || it == "." || it == ".." }) { "压缩包包含不安全路径" }
    return name
}
