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
import com.kmuaz.alistcloud.ui.home.formatFileSize
import com.kmuaz.alistcloud.ui.home.formatTime
import com.kmuaz.alistcloud.ui.home.getFileIcon

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
                    text = "${formatFileSize(file.size)} · ${formatTime(file.modified)}",
                    style = MaterialTheme.typography.bodySmall
                )

            }

        },

        leadingContent = {

            Icon(

                imageVector = getFileIcon(file),

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
