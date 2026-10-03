package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

@Composable
fun FilePropertyDialog(

    file: FileItem,

    currentPath: String,

    onDismiss: () -> Unit

) {

    AlertDialog(

        onDismissRequest = onDismiss,

        confirmButton = {

            TextButton(

                onClick = onDismiss

            ) {

                Text("关闭")

            }

        },

        title = {

            Text("文件属性")

        },

        text = {

            Column {

                PropertyItem(
                    "名称",
                    file.name
                )

                PropertyItem(
                    "大小",
                    if (file.is_dir)
                        "-"
                    else
                        formatFileSize(file.size)
                )

                PropertyItem(
                    "修改时间",
                    formatTime(file.modified)
                )

                PropertyItem(
                    "类型",
                    if (file.is_dir)
                        "文件夹"
                    else
                        "文件"
                )

                PropertyItem(
                    "路径",
                    if (currentPath == "/")
                        "/${file.name}"
                    else
                        "$currentPath/${file.name}"
                )

            }

        }

    )

}
