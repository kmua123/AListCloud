package com.kmuaz.alistcloud.ui.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.kmuaz.alistcloud.model.ServerConfig
import com.kmuaz.alistcloud.viewmodel.SettingViewModel

@Composable
fun SettingScreen() {

    val viewModel: SettingViewModel = viewModel()

    val config by viewModel.serverConfig.collectAsState()

    val context = LocalContext.current

    var server by remember(config.server) {
        mutableStateOf(config.server)
    }

    var username by remember(config.username) {
        mutableStateOf(config.username)
    }

    var password by remember(config.password) {
        mutableStateOf(config.password)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "⚙ 设置",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = server,
            onValueChange = { server = it },
            label = { Text("服务器地址") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("测试连接")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {

                viewModel.saveConfig(

                    ServerConfig(

                        server = server,

                        username = username,

                        password = password

                    )

                )

                Toast.makeText(
                    context,
                    "保存成功",
                    Toast.LENGTH_SHORT
                ).show()

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }

    }
}