package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kmuaz.alistcloud.data.network.model.FileItem
import androidx.compose.material.icons.filled.KeyboardArrowRight

@Composable
fun FileItemRow(
    file: FileItem,
    onClick: () -> Unit
) {

    ListItem(

        headlineContent = {
            Text(file.name)
        },supportingContent = {

            if (!file.is_dir) {

                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall
                )

            }

        },

        leadingContent = {

            Icon(
                imageVector =
                    if (file.is_dir)
                        Icons.Default.Folder
                    else
                        Icons.Default.InsertDriveFile,

                contentDescription = null
            )

        },

        trailingContent = {

            if (file.is_dir) {

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null
                )

            }

        },

        modifier = Modifier.clickable {

            onClick()

        }

    )


}
private fun formatFileSize(size: Long): String {

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