package com.kmuaz.alistcloud.ui.download

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.WindowInsets
import androidx.activity.compose.BackHandler
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kmuaz.alistcloud.data.download.DownloadStatus
import com.kmuaz.alistcloud.data.download.DownloadTask
import com.kmuaz.alistcloud.viewmodel.DownloadViewModel

private enum class DeleteMode {
    RECORD_ONLY,
    RECORD_AND_FILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(onBackClick: () -> Unit) {
    val viewModel: DownloadViewModel = viewModel()
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current
    var filter by remember { mutableStateOf("全部") }
    val visibleTasks = tasks.filter { task -> when (filter) {
        "下载中" -> task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING || task.status == DownloadStatus.PAUSED
        "已完成" -> task.status == DownloadStatus.COMPLETED
        "失败" -> task.status == DownloadStatus.FAILED
        else -> true
    } }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteMode by remember { mutableStateOf(DeleteMode.RECORD_ONLY) }

    BackHandler(enabled = selectionMode) { selectionMode = false; selectedIds = emptySet() }
    LaunchedEffect(filter) { selectedIds = emptySet() }
    LaunchedEffect(tasks) {
        selectedIds = selectedIds.intersect(visibleTasks.map { it.id }.toSet())
        if (tasks.isEmpty()) selectionMode = false
    }
    LaunchedEffect(Unit) { viewModel.startMonitoring() }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopMonitoring() }
    }
    LaunchedEffect(Unit) {
        viewModel.openFileEvent.collect { event ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(event.uri, event.mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (intent.resolveActivity(context.packageManager) == null) {
                Toast.makeText(context, "没有可打开 ${event.fileName} 的应用", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "无法打开 ${event.fileName}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Text(if (selectionMode) "已选择 ${selectedIds.size} 项" else "下载任务")
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (selectionMode) {
                        selectionMode = false
                        selectedIds = emptySet()
                    } else {
                        onBackClick()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                }
            },
            actions = {
                if (selectionMode) {
                    TextButton(onClick = {
                        selectedIds = if (visibleTasks.isNotEmpty() && selectedIds.size == visibleTasks.size) {
                            emptySet()
                        } else {
                            visibleTasks.map { it.id }.toSet()
                        }
                    }) {
                        Text(if (visibleTasks.isNotEmpty() && selectedIds.size == visibleTasks.size) "取消全选" else "全选")
                    }
                    TextButton(
                        enabled = selectedIds.isNotEmpty(),
                        onClick = {
                            deleteMode = DeleteMode.RECORD_ONLY
                            showDeleteDialog = true
                        }
                    ) { Text("删除") }
                } else if (tasks.isNotEmpty()) {
                    TextButton(onClick = { selectionMode = true }) {
                        Text("选择")
                    }
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0), colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Text("${tasks.count { it.status == DownloadStatus.COMPLETED }} 项已完成 · ${tasks.size} 个任务",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("全部", "下载中", "已完成", "失败").forEach { label ->
                FilterChip(selected = filter == label, onClick = { filter = label }, label = { Text(label) })
            }
        }
        if (visibleTasks.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.height(12.dp))
                Text(if (filter == "全部") "暂无下载任务" else "暂无${filter}任务", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("在文件菜单中选择下载，即可在这里查看进度", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onBackClick) { Text("去浏览文件") }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(visibleTasks, key = { it.id }) { task ->
                    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    DownloadTaskRow(
                        task = task,
                        selectionMode = selectionMode,
                        selected = task.id in selectedIds,
                        onClick = {
                            if (selectionMode) {
                                selectedIds = selectedIds.toggle(task.id)
                            } else if (task.status == DownloadStatus.COMPLETED) {
                                viewModel.openCompletedTask(task)
                            }
                        },
                        onCancel = { viewModel.cancelTask(task) }
                    )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        val selectedTasks = tasks.filter { it.id in selectedIds }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除 ${selectedTasks.size} 个下载任务？") },
            text = {
                Column {
                    DeleteOption(
                        selected = deleteMode == DeleteMode.RECORD_ONLY,
                        title = "仅删除任务记录",
                        description = "仅从本应用下载页隐藏，系统下载任务和本地文件会保留。",
                        onClick = { deleteMode = DeleteMode.RECORD_ONLY }
                    )
                    Spacer(Modifier.height(8.dp))
                    DeleteOption(
                        selected = deleteMode == DeleteMode.RECORD_AND_FILE,
                        title = "同时删除本地文件",
                        description = "取消系统任务，并删除该任务实际关联的部分或完整文件。",
                        onClick = { deleteMode = DeleteMode.RECORD_AND_FILE }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTasks(
                        selectedTasks,
                        deleteLocalFiles = deleteMode == DeleteMode.RECORD_AND_FILE
                    )
                    selectedIds = emptySet()
                    selectionMode = false
                    showDeleteDialog = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun DeleteOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DownloadTaskRow(
    task: DownloadTask,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = task.fileName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!selectionMode && task.status.isCancelable()) {
                TextButton(onClick = onCancel) { Text("取消") }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (task.status == DownloadStatus.DOWNLOADING || task.status == DownloadStatus.PENDING) {
            task.progress?.let {
                LinearProgressIndicator(
                    progress = { it.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(statusText(task), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Text(sizeText(task), style = MaterialTheme.typography.bodySmall)
            if (task.status == DownloadStatus.DOWNLOADING && task.speedBytesPerSecond > 0) {
                Spacer(Modifier.width(8.dp))
                Text("${formatBytes(task.speedBytesPerSecond)}/s", style = MaterialTheme.typography.bodySmall)
            }
        }
        task.failureReason?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Set<Long>.toggle(id: Long): Set<Long> = if (id in this) this - id else this + id

private fun DownloadStatus.isCancelable(): Boolean = this == DownloadStatus.PENDING ||
    this == DownloadStatus.DOWNLOADING || this == DownloadStatus.PAUSED

private fun statusText(task: DownloadTask): String = when (task.status) {
    DownloadStatus.PENDING -> "等待下载"
    DownloadStatus.DOWNLOADING -> task.progress?.let { "下载中 ${(it * 100).toInt()}%" } ?: "下载中"
    DownloadStatus.PAUSED -> "系统已暂停"
    DownloadStatus.COMPLETED -> "已完成（点击打开）"
    DownloadStatus.FAILED -> "下载失败"
}

private fun sizeText(task: DownloadTask): String = if (task.totalBytes > 0) {
    "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}"
} else {
    formatBytes(task.downloadedBytes)
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unit = -1
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    return "%.1f %s".format(java.util.Locale.getDefault(), value, units[unit])
}
