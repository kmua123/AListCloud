package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

@Composable
fun FileItemRow(
    file: FileItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {

    Column(

        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    android.util.Log.d("AListTest", "click")
                    onClick()
                },
                onLongClick = {
                    android.util.Log.d("AListTest", "long")
                    onLongClick()
                }
            )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),

            verticalAlignment = Alignment.CenterVertically

        ) {

            Icon(

                imageVector = getFileIcon(file),

                contentDescription = null

            )

            Spacer(
                modifier = Modifier.width(16.dp)
            )

            Column(

                modifier = Modifier.weight(1f)

            ) {

                Text(

                    text = file.name,

                    style = MaterialTheme.typography.bodyLarge

                )

                if (!file.is_dir) {

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(

                        text = "${formatFileSize(file.size)} · ${formatTime(file.modified)}",

                        style = MaterialTheme.typography.bodySmall

                    )

                }

            }

            if (file.is_dir) {

                Icon(

                    imageVector = Icons.Default.KeyboardArrowRight,

                    contentDescription = null

                )

            }

        }

        HorizontalDivider()

    }

}