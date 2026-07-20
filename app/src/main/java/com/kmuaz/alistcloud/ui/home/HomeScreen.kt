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

@Composable
fun HomeScreen(
    onSettingClick: () -> Unit
) {

    val viewModel: HomeViewModel = viewModel()

    val files by viewModel.files.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {

        viewModel.loadRootFiles()

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

    Box(
        modifier = Modifier.fillMaxSize()
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

                    Text(

                        text = file.name,

                        style = MaterialTheme.typography.bodyLarge,

                        modifier = Modifier.padding(
                            vertical = 8.dp
                        )

                    )

                }

            }

        }

    }

}