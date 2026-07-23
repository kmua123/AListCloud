package com.kmuaz.alistcloud.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kmuaz.alistcloud.viewmodel.HomeViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import com.kmuaz.alistcloud.data.network.model.FileItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSettingClick: () -> Unit
) {

    val viewModel: HomeViewModel = viewModel()

    val files by viewModel.files.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    var selectedFile by remember {
        mutableStateOf<FileItem?>(null)
    }

    val sheetState = rememberModalBottomSheetState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        viewModel.loadFiles("/")

    }

    LaunchedEffect(error) {

        if (error.isNotEmpty()) {

            Toast.makeText(
                context,
                error,
                Toast.LENGTH_SHORT
            ).show()

        }

    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        HomeTopBar(

            currentPath = currentPath,

            onBackClick = {

                val parent =

                    currentPath.substringBeforeLast(
                        "/",
                        ""
                    )

                viewModel.loadFiles(

                    if (parent.isBlank())
                        "/"
                    else
                        parent

                )

            }

        )

        Box(
            modifier = Modifier.weight(1f)
        ) {

            if (loading) {

                CircularProgressIndicator(
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )

            } else {

                FileList(

                    files = files,

                    currentPath = currentPath,

                    onItemClick = {

                        if (it.is_dir) {

                            viewModel.loadFiles(

                                if (currentPath == "/")
                                    "/${it.name}"
                                else
                                    "$currentPath/${it.name}"

                            )

                        }

                    },

                    onItemLongClick = {

                        selectedFile = it

                        Toast.makeText(
                            context,
                            "长按：" + it.name,
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                )

            }

        }

    }
    if (selectedFile != null) {

        ModalBottomSheet(

            onDismissRequest = {

                selectedFile = null

            },

            sheetState = sheetState

        ) {

            Text(
                text = selectedFile!!.name
            )

            Button(

                onClick = {

                    selectedFile = null

                }

            ) {

                Text("关闭")

            }

        }

    }

}