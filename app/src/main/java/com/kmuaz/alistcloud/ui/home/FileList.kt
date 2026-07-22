package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

@Composable
fun FileList(

    files: List<FileItem>,

    currentPath: String,

    onFolderClick: (String) -> Unit

) {

    LazyColumn(

        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(16.dp)

    ) {

        items(files) { file ->

            FileItemRow(

                file = file,

                onClick = {

                    if (file.is_dir) {

                        val nextPath =

                            if (currentPath == "/")
                                "/${file.name}"
                            else
                                "$currentPath/${file.name}"

                        onFolderClick(nextPath)

                    }

                }

            )

        }

    }

}