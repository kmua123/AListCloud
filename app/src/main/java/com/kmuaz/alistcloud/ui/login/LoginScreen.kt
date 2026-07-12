package com.kmuaz.alistcloud.ui.login
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun LoginScreen() {

    var server by remember {
        mutableStateOf("")
    }

    var username by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "☁",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AList Cloud",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "现代化开源 AList 客户端"
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = server,
            onValueChange = {
                server = it
            },
            label = {
                Text("服务器地址")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = {
                Text("用户名")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = {
                Text("密码")
            },

            visualTransformation =
                if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),

            trailingIcon = {

                IconButton(
                    onClick = {
                        passwordVisible = !passwordVisible
                    }
                ) {

                    Icon(
                        imageVector =
                            if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,

                        contentDescription = null
                    )

                }

            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("登录")
        }
    }
}