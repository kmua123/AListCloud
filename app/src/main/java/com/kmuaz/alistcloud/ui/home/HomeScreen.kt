package com.kmuaz.alistcloud.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api

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

        TopAppBar(

            title = {

                Text(currentPath)

            },

            navigationIcon = {

                if (currentPath != "/") {

                    IconButton(
                        onClick = {

                            val parent = currentPath.substringBeforeLast("/")

                            val newPath =
                                if (parent.isEmpty()) "/"
                                else parent

                            viewModel.loadFiles(newPath)

                        }
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )

                    }

                }

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

                                    viewModel.loadFiles(nextPath)


                                }

                            }

                        )

                    }

                }

            }

        }

    }

}