package com.kmuaz.alistcloud.ui.home

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.kmuaz.alistcloud.data.network.model.FileItem

fun formatFileSize(size: Long): String {

    if (size <= 0L) return ""

    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        size >= gb -> String.format("%.2f GB", size / gb)
        size >= mb -> String.format("%.2f MB", size / mb)
        size >= kb -> String.format("%.2f KB", size / kb)
        else -> "$size B"
    }
}

fun formatTime(time: String): String {

    return try {

        val date = OffsetDateTime.parse(time)

        date.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )

    } catch (e: Exception) {

        time

    }

}


fun getFileIcon(file: FileItem): ImageVector {

    if (file.is_dir) {
        return Icons.Default.Folder
    }

    val name = file.name.lowercase()

    return when {

        name.endsWith(".jpg") ||
                name.endsWith(".jpeg") ||
                name.endsWith(".png") ||
                name.endsWith(".gif") ||
                name.endsWith(".webp") ->
            Icons.Default.Image

        name.endsWith(".mp4") ||
                name.endsWith(".mkv") ||
                name.endsWith(".avi") ||
                name.endsWith(".mov") ->
            Icons.Default.Movie

        name.endsWith(".mp3") ||
                name.endsWith(".flac") ||
                name.endsWith(".wav") ->
            Icons.Default.MusicNote

        name.endsWith(".pdf") ->
            Icons.Default.PictureAsPdf

        name.endsWith(".zip") ||
                name.endsWith(".rar") ||
                name.endsWith(".7z") ->
            Icons.Default.FolderZip

        else ->
            Icons.Default.InsertDriveFile

    }

}

