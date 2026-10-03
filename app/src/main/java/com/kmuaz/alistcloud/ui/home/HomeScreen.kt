package com.kmuaz.alistcloud.ui.home

import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kmuaz.alistcloud.data.network.model.FileItem
import com.kmuaz.alistcloud.data.repository.*
import com.kmuaz.alistcloud.ui.util.DownloadHelper
import com.kmuaz.alistcloud.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onSettingClick: () -> Unit, onDownloadsClick: () -> Unit,
    tools: CloudToolsViewModel, onUploadTasksClick: () -> Unit) {
    val model: HomeViewModel = viewModel()
    val files by model.files.collectAsState()
    val loading by model.loading.collectAsState()
    val error by model.error.collectAsState()
    val currentPath by model.currentPath.collectAsState()
    val category by model.category.collectAsState()
    val categoryStatus by model.categoryStatus.collectAsState()
    val scanning by model.scanning.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val visibleFiles = remember(files, query) {
        files.filter { it.name.contains(query, true) }.sortedWith(compareBy<FileItem> { !it.is_dir }.thenBy { it.name.lowercase() })
    }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var propertyFile by remember { mutableStateOf<FileItem?>(null) }
    var previewUrl by remember { mutableStateOf<String?>(null) }
    var media by remember { mutableStateOf<OpenFileEvent?>(null) }
    var archive by remember { mutableStateOf<String?>(null) }
    var uploads by remember { mutableStateOf<List<LocalUpload>>(emptyList()) }
    var uploadDestination by remember { mutableStateOf("/") }
    var opening by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun toast(message: String) { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
    fun goBack() { if (category != FileCategory.ALL) model.loadFiles(currentPath) else model.loadFiles(currentPath.substringBeforeLast('/', "").ifBlank { "/" }) }
    BackHandler(enabled = category != FileCategory.ALL || currentPath != "/") { goBack() }
    LaunchedEffect(Unit) { if (files.isEmpty() && category == FileCategory.ALL) model.loadFiles(currentPath) }
    LaunchedEffect(currentPath, category) { query = "" }
    LaunchedEffect(error) { if (error.isNotBlank()) toast(error) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        try {
            uploads = uris.map { uri ->
                runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                context.contentResolver.describeUpload(uri)
            }
        } catch (e: Exception) { toast(e.message ?: "无法读取选中文件") }
    }
    fun external(event: OpenFileEvent) {
        scope.launch {
            opening = true
            try { openDocumentFile(context, event.url, event.fileName) }
            catch (e: Exception) { toast(e.message ?: "没有可打开此文件的应用") }
            finally { opening = false }
        }
    }
    fun open(file: FileItem) {
        val path = file.fullPath(currentPath)
        when {
            file.is_dir -> model.loadFiles(path)
            file.isImage() -> model.getPreviewUrl(path, file.name)
            file.name.isArchiveName() -> archive = path
            else -> model.openFile(path, file.name)
        }
    }
    LaunchedEffect(Unit) {
        model.downloadEvent.collect { event ->
            runCatching { DownloadHelper.download(context, event.url, event.fileName) }
                .onSuccess { toast("已加入下载队列：${event.fileName}") }.onFailure { toast(it.message ?: "无法开始下载") }
            selectedFile = null
        }
    }
    LaunchedEffect(Unit) {
        model.shareEvent.collect { event ->
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"; putExtra(Intent.EXTRA_TEXT, event.url); putExtra(Intent.EXTRA_TITLE, event.fileName)
            }, "分享 ${event.fileName}"))
        }
    }
    LaunchedEffect(Unit) {
        model.openFileEvent.collect { event ->
            if (event.fileName.isAudioName() || event.fileName.isVideoName()) media = event else external(event)
            selectedFile = null
        }
    }
    LaunchedEffect(Unit) { model.imagePreviewEvent.collect { previewUrl = it.url; selectedFile = null } }
    Column(Modifier.fillMaxSize()) {
        FileBrowserHeader(currentPath, query, category, visibleFiles.size, { query = it }, { model.selectCategory(it) }, { goBack() },
            { if (category == FileCategory.ALL) model.loadFiles(currentPath, true) else model.selectCategory(category) },
            { uploadDestination = currentPath; picker.launch(arrayOf("*/*")) })
        if (category != FileCategory.ALL) Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(categoryStatus, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            if (scanning) TextButton(onClick = { model.stopCategoryScan() }) { Text("停止扫描") }
        }
        if (scanning) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (opening) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Box(Modifier.weight(1f)) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error.isNotEmpty() && files.isEmpty() -> BrowserEmptyState("暂时无法加载文件", error, "重新加载") { if (category == FileCategory.ALL) model.loadFiles(currentPath) else model.selectCategory(category) }
                visibleFiles.isEmpty() && scanning -> BrowserEmptyState("正在查找${category.label}", "可访问目录中的文件会逐步出现在这里", "停止扫描") { model.stopCategoryScan() }
                visibleFiles.isEmpty() -> BrowserEmptyState("暂无匹配文件", if (category == FileCategory.ALL) "当前目录中没有匹配的文件" else "${categoryStatus}，可以刷新重新查找", "浏览文件") { model.loadFiles(currentPath) }
                else -> FileList(visibleFiles, currentPath, { open(it) }, { selectedFile = it })
            }
        }
    }
    selectedFile?.let { file ->
        FileBottomSheet(file, { selectedFile = null }, { selectedFile = null; open(file) },
            { model.getFile(file.fullPath(currentPath), file.name) },
            { model.shareFile(file.fullPath(currentPath), file.name); selectedFile = null },
            { propertyFile = file; selectedFile = null })
    }
    propertyFile?.let { file -> FilePropertyDialog(file, file.parent ?: currentPath) { propertyFile = null } }
    previewUrl?.let { ImagePreviewDialog(it) { previewUrl = null } }
    media?.let { event -> MediaPreviewDialog(event.url, event.fileName, { media = null }, { external(event) }) }
    archive?.let { path -> ArchiveDialog(path, tools, { archive = null }, { destination -> archive = null; model.loadFiles(destination, true) }) }
    if (uploads.isNotEmpty()) AlertDialog(onDismissRequest = { uploads = emptyList() }, title = { Text("上传 ${uploads.size} 个文件？") },
        text = { Text("上传到：$uploadDestination\n${uploads.take(5).joinToString("\n") { it.name }}${if (uploads.size > 5) "\n…" else ""}\n同名文件会跳过，不覆盖。上传时请保持应用运行。") },
        confirmButton = { TextButton(onClick = { tools.upload(uploads, uploadDestination); uploads = emptyList(); onUploadTasksClick() }) { Text("开始上传") } },
        dismissButton = { TextButton(onClick = { uploads = emptyList() }) { Text("取消") } })
}
