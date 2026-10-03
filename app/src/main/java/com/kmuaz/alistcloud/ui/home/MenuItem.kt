package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuItem(

    title: String,

    onClick: () -> Unit

) {

    Text(

        text = title,

        style = MaterialTheme.typography.bodyLarge,

        modifier = Modifier
            .fillMaxWidth()
            .clickable {

                onClick()

            }
            .padding(vertical = 16.dp)

    )

}