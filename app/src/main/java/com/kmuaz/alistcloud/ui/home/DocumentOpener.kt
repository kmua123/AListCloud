package com.kmuaz.alistcloud.ui.home

import android.content.Context
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

suspend fun openDocumentFile(context: Context, url: String, name: String) {
    // External document viewers usually need a local content URI, not an HTTP URL.
    if (name.isAudioName() || name.isVideoName()) {
        val mime = if (name.isAudioName()) "audio/*" else "video/*"
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).setDataAndType(android.net.Uri.parse(url), mime), "选择播放器"))
        return
    }
    val file = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "documents").apply { mkdirs() }
        directory.listFiles()?.filter { System.currentTimeMillis() - it.lastModified() > 24 * 60 * 60 * 1000 }?.forEach { it.delete() }
        val extension = name.substringAfterLast('.', "bin").filter { it.isLetterOrDigit() }.take(12).ifBlank { "bin" }
        val target = File.createTempFile("preview-", ".$extension", directory)
        try {
            OkHttpClient().newCall(Request.Builder().url(url).build()).execute().use { response ->
                check(response.isSuccessful) { "文件读取失败：HTTP ${response.code}" }
                val body = response.body ?: error("文件内容为空")
                body.byteStream().use { input -> target.outputStream().use { output -> input.copyTo(output) } }
            }
            target
        } catch (e: Exception) { target.delete(); throw e }
    }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".files", file)
    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "application/octet-stream"
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION), "打开 $name"))
}
