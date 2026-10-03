package com.kmuaz.alistcloud.ui.upload

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.ui.home.formatFileSize
import com.kmuaz.alistcloud.viewmodel.CloudToolsViewModel

@Composable
fun UploadScreen(tools: CloudToolsViewModel, onBrowse: () -> Unit) {
    val tasks by tools.uploads.collectAsState()
    Column(Modifier.fillMaxSize()) {
        Text("上传任务", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(20.dp))
        if (tasks.isEmpty()) Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("暂无上传任务")
            Text("在文件页点击上传，选择手机中的文件", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onBrowse) { Text("去上传文件") }
        } else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            items(tasks.reversed(), key = { it.id }) { task ->
                Surface(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(task.file.name, style = MaterialTheme.typography.titleMedium)
                        Text(task.destination, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("${task.state} · ${formatFileSize(task.sent).ifBlank { "0 B" }} / ${if (task.file.size >= 0) formatFileSize(task.file.size).ifBlank { "0 B" } else "未知大小"}", style = MaterialTheme.typography.bodySmall)
                        if (task.state in setOf("上传中", "检查目标目录", "等待上传")) {
                            if (task.file.size > 0) LinearProgressIndicator(progress = { (task.sent.toFloat() / task.file.size).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                            else LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                            TextButton(onClick = { tools.cancelUpload(task.id) }) { Text("取消上传") }
                        }
                        if (task.error.isNotBlank()) Text(task.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (task.state == "上传失败" || task.state == "已取消") TextButton(onClick = { tools.retry(task) }) { Text("重新上传") }
                    }
                }
            }
        }
    }
}
