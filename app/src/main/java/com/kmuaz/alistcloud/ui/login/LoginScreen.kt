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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.kmuaz.alistcloud.viewmodel.LoginViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.Cloud

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {

    val viewModel: LoginViewModel = viewModel()

    val config by viewModel.serverConfig.collectAsState()

    val loading by viewModel.loading.collectAsState()

    val loginState by viewModel.loginState.collectAsState()

    val error by viewModel.error.collectAsState()

    var server by remember(config.server) {
        mutableStateOf(config.server)
    }

    var username by remember(config.username) {
        mutableStateOf(config.username)
    }

    var password by remember(config.password) {
        mutableStateOf(config.password)
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    LaunchedEffect(error) {

        if (error.isNotEmpty()) {

            Toast.makeText(
                context,
                error,
                Toast.LENGTH_SHORT
            ).show()

        }

    }


    LaunchedEffect(loginState) {

        if (loginState) {

            onLoginSuccess()

        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding().verticalScroll(rememberScrollState()).padding(28.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Icon(Icons.Default.Cloud, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(64.dp))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AList Cloud",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "连接你的云盘，让文件触手可及"
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

                viewModel.login(
                    server = server,
                    username = username,
                    password = password
                )

            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), enabled = !loading && server.isNotBlank()
        )

          {
            Text(if (loading) "正在连接…" else "连接云盘")
        }
    }
}
