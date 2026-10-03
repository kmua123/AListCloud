package com.kmuaz.alistcloud.data.download

import android.net.Uri

data class DownloadTask(
    val id: Long,
    val fileName: String,
    val status: DownloadStatus,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSecond: Long,
    val mimeType: String?,
    val localUri: Uri?,
    val failureReason: String?
) {
    val progress: Float?
        get() = if (totalBytes > 0) {
            downloadedBytes.toFloat() / totalBytes.toFloat()
        } else {
            null
        }
}

enum class DownloadStatus {
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}
