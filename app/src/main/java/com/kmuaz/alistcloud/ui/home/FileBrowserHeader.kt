package com.kmuaz.alistcloud.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kmuaz.alistcloud.data.network.model.FileItem

enum class FileCategory(val label: String) {
    ALL("全部"), IMAGE("图片"), VIDEO("视频"), DOCUMENT("文档"), AUDIO("音乐"), ARCHIVE("压缩包");
    fun matches(file: FileItem): Boolean {
        if (this == ALL) return true
        if (file.is_dir) return false
        val extension = file.name.substringAfterLast('.', "").lowercase()
        return when (this) {
            IMAGE -> file.isImage()
            VIDEO -> file.name.isVideoName()
            DOCUMENT -> extension in setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "epub")
            AUDIO -> file.name.isAudioName()
            ARCHIVE -> file.name.isArchiveName()
            ALL -> true
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileBrowserHeader(path: String, query: String, category: FileCategory, count: Int,
    onQuery: (String) -> Unit, onCategory: (FileCategory) -> Unit, onBack: () -> Unit, onRefresh: () -> Unit, onUpload: () -> Unit) {
    Column {
        TopAppBar(title = { Column {
            Text("我的云盘", style = MaterialTheme.typography.headlineSmall)
            Text("文件随身，随时访问", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } }, actions = { IconButton(onClick = onUpload) { Icon(Icons.Default.FileUpload, "上传文件") }; IconButton(onClick = onRefresh) { Icon(Icons.Default.Refresh, "刷新当前目录") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            windowInsets = WindowInsets(0, 0, 0, 0))
        OutlinedTextField(value = query, onValueChange = onQuery, placeholder = { Text(if (category == FileCategory.ALL) "搜索当前目录中的文件" else "搜索全盘${category.label}") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { onQuery("") }) { Icon(Icons.Default.Close, "清空搜索") } },
            singleLine = true, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FileCategory.entries.forEach { item -> FilterChip(selected = category == item, onClick = { onCategory(item) }, label = { Text(item.label) }) }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (path != "/" || category != FileCategory.ALL) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回文件目录") }
            Column(Modifier.weight(1f)) {
                Text(if (category != FileCategory.ALL) "全部${category.label}" else if (path == "/") "全部文件" else path.substringAfterLast('/'), style = MaterialTheme.typography.titleMedium)
                Text(if (category != FileCategory.ALL) "可访问的云盘目录 · $count 项" else if (path == "/") "根目录 · $count 项" else "$path · $count 项", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}
@Composable
internal fun BrowserEmptyState(title: String, description: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp).size(48.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onAction) { Text(action) }
    }
}
