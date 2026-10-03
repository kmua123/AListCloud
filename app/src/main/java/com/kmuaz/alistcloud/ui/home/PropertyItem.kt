package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PropertyItem(

    title: String,

    value: String

) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)

    ) {

        Text(

            text = title,

            style = MaterialTheme.typography.labelMedium

        )

        Text(

            text = value,

            style = MaterialTheme.typography.bodyLarge

        )

    }

}