package com.kmuaz.alistcloud.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    currentPath: String,
    onBackClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {

    TopAppBar(

        title = {

            Text(currentPath)

        },

        navigationIcon = {

            if (currentPath != "/") {

                IconButton(
                    onClick = onBackClick
                ) {

                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )

                }

            }

        },

        actions = {
            IconButton(onClick = onDownloadsClick) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "下载任务"
                )
            }
        }

    )

}
