package com.kmuaz.alistcloud.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.kmuaz.alistcloud.data.download.DownloadStatus
import com.kmuaz.alistcloud.data.download.DownloadTask

class DownloadRepository(context: Context) {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private val samples = mutableMapOf<Long, DownloadSample>()
    private val hiddenTaskPreferences = context.getSharedPreferences(
        "download_tasks",
        Context.MODE_PRIVATE
    )

    fun getTasks(): List<DownloadTask> {
        val now = System.currentTimeMillis()
        val hiddenTaskIds = hiddenTaskPreferences
            .getStringSet(HIDDEN_TASK_IDS, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()
        val activeIds = mutableSetOf<Long>()
        val tasks = mutableListOf<DownloadTask>()

        downloadManager.query(DownloadManager.Query()).use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID)
            val titleColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE)
            val statusColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            val downloadedColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE)
            val localUriColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            val reasonColumn = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val downloadedBytes = cursor.getLong(downloadedColumn).coerceAtLeast(0)
                val totalBytes = cursor.getLong(totalColumn)
                val status = cursor.getInt(statusColumn).toDownloadStatus()
                val previous = samples[id]
                val speed = if (status == DownloadStatus.DOWNLOADING && previous != null) {
                    val elapsedMillis = (now - previous.timestampMillis).coerceAtLeast(1)
                    ((downloadedBytes - previous.downloadedBytes).coerceAtLeast(0) * 1000L) / elapsedMillis
                } else {
                    0L
                }

                samples[id] = DownloadSample(downloadedBytes, now)
                activeIds += id
                if (id in hiddenTaskIds) continue

                tasks += DownloadTask(
                    id = id,
                    fileName = cursor.getString(titleColumn).orEmpty().ifBlank { "未命名文件" },
                    status = status,
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                    speedBytesPerSecond = speed,
                    mimeType = cursor.getString(mimeTypeColumn),
                    localUri = cursor.getString(localUriColumn)?.let(Uri::parse),
                    failureReason = cursor.getInt(reasonColumn).toFailureReason(status)
                )
            }
        }

        samples.keys.retainAll(activeIds)
        return tasks.sortedByDescending { it.id }
    }

    fun getCompletedFileUri(id: Long): Uri? = downloadManager.getUriForDownloadedFile(id)

    fun deleteTasks(tasks: List<DownloadTask>, deleteLocalFiles: Boolean) {
        val taskIds = tasks
            .map { it.id }
            .toLongArray()

        if (taskIds.isEmpty()) return

        if (deleteLocalFiles) {
            // DownloadManager removes the exact file associated with each task ID.
            downloadManager.remove(*taskIds)
            taskIds.forEach(samples::remove)
        } else {
            val hiddenIds = hiddenTaskPreferences
                .getStringSet(HIDDEN_TASK_IDS, emptySet())
                .orEmpty()
                .toMutableSet()
            hiddenIds += taskIds.map(Long::toString)
            hiddenTaskPreferences.edit()
                .putStringSet(HIDDEN_TASK_IDS, hiddenIds)
                .apply()
        }
    }

    private companion object {
        const val HIDDEN_TASK_IDS = "hidden_task_ids"
    }

    private fun Int.toDownloadStatus(): DownloadStatus = when (this) {
        DownloadManager.STATUS_PENDING -> DownloadStatus.PENDING
        DownloadManager.STATUS_RUNNING -> DownloadStatus.DOWNLOADING
        DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
        DownloadManager.STATUS_SUCCESSFUL -> DownloadStatus.COMPLETED
        else -> DownloadStatus.FAILED
    }

    private fun Int.toFailureReason(status: DownloadStatus): String? {
        if (status != DownloadStatus.FAILED) return null

        return when (this) {
            DownloadManager.ERROR_CANNOT_RESUME -> "无法继续下载"
            DownloadManager.ERROR_DEVICE_NOT_FOUND -> "未找到存储设备"
            DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "目标文件已存在"
            DownloadManager.ERROR_FILE_ERROR -> "文件写入失败"
            DownloadManager.ERROR_HTTP_DATA_ERROR -> "网络数据错误"
            DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足"
            DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "重定向次数过多"
            DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "服务器响应异常"
            DownloadManager.ERROR_UNKNOWN -> "下载失败"
            else -> "下载失败"
        }
    }

    private data class DownloadSample(
        val downloadedBytes: Long,
        val timestampMillis: Long
    )
}
