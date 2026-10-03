package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kmuaz.alistcloud.viewmodel.CloudToolsViewModel

@Composable
fun ArchiveDialog(path: String, tools: CloudToolsViewModel, onDismiss: () -> Unit, onOpenDestination: (String) -> Unit) {
    var inner by remember { mutableStateOf("/") }
    var password by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf(path.substringBeforeLast('/') + "/" + path.substringAfterLast('/').substringBeforeLast('.') + "-解压-" + System.currentTimeMillis()) }
    var confirm by remember { mutableStateOf(false) }
    val files by tools.archiveFiles.collectAsState()
    val busy by tools.archiveBusy.collectAsState()
    val message by tools.archiveMessage.collectAsState()
    val localMode by tools.localZipMode.collectAsState()
    LaunchedEffect(path, inner) { tools.listArchive(path, inner, password) }
    DisposableEffect(Unit) { onDispose { tools.closeArchive() } }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().fillMaxHeight(.94f).padding(12.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(path.substringAfterLast('/'), style = MaterialTheme.typography.titleLarge)
                Text("云端压缩包 · $inner", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(password, { password = it }, label = { Text("压缩包密码（可选）") },
                    visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                Row {
                    TextButton(enabled = !busy, onClick = { tools.listArchive(path, inner, password) }) { Text("加载内容") }
                    if (inner != "/") TextButton(enabled = !busy, onClick = { inner = inner.substringBeforeLast('/', "").ifBlank { "/" } }) { Text("返回上级") }
                }
                if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
                LazyColumn(Modifier.weight(1f)) {
                    items(files, key = { it.name }) { file ->
                        ListItem(headlineContent = { Text(file.name) }, supportingContent = { Text(if (file.is_dir) "文件夹" else formatFileSize(file.size)) },
                            trailingContent = { if (file.is_dir) TextButton(enabled = !busy, onClick = { inner = inner.trimEnd('/') + "/" + file.name }) { Text("进入") } })
                    }
                    if (files.isEmpty() && !busy && message.isBlank()) item { Text("压缩包中没有内容") }
                }
                OutlinedTextField(destination, { destination = it }, label = { Text("解压到新文件夹") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onDismiss) { Text("关闭") }
                    TextButton(onClick = { onOpenDestination(destination) }) { Text("查看目标目录") }
                    Button(enabled = !busy && destination.startsWith('/') && destination.substringAfterLast('/').isNotBlank(), onClick = { confirm = true }) { Text(if (localMode) "解压并上传" else "云端解压") }
                }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("解压到云盘？") },
        text = { Text("创建新文件夹：$destination\n原压缩包保留。" + if (localMode) "ZIP 兼容模式需要保持窗口打开。关闭会停止处理，目标中可能保留部分文件。" else "关闭窗口后服务器任务仍会继续。") },
        confirmButton = { TextButton(onClick = { confirm = false; tools.decompress(path, destination.trim(), password) }) { Text("开始解压") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } })
}
