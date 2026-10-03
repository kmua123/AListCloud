package com.kmuaz.alistcloud.viewmodel

import kotlinx.coroutines.flow.first
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmuaz.alistcloud.data.datastore.DataStoreManager
import com.kmuaz.alistcloud.data.network.model.FileItem
import com.kmuaz.alistcloud.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import android.util.Log
import kotlinx.coroutines.*
import com.kmuaz.alistcloud.data.repository.CloudRepository
import com.kmuaz.alistcloud.data.repository.fullPath
import com.kmuaz.alistcloud.ui.home.FileCategory
import com.kmuaz.alistcloud.ui.home.DownloadEvent
import com.kmuaz.alistcloud.ui.home.ImagePreviewEvent
import com.kmuaz.alistcloud.ui.home.OpenFileEvent
import com.kmuaz.alistcloud.ui.home.ShareEvent

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FileRepository()

    private val dataStore =
        DataStoreManager(application)

    private val _currentPath =
        MutableStateFlow("/")

    val currentPath: StateFlow<String> =
        _currentPath

    private val _files =
        MutableStateFlow<List<FileItem>>(emptyList())

    val files: StateFlow<List<FileItem>> =
        _files

    private val _refreshing =
        MutableStateFlow(false)

    val refreshing: StateFlow<Boolean> =
        _refreshing

    private val _loading =
        MutableStateFlow(false)

    val loading: StateFlow<Boolean> =
        _loading

    private val _error =
        MutableStateFlow("")


    val error: StateFlow<String> =
        _error

    private val _downloadEvent =
        MutableSharedFlow<DownloadEvent>()

    val downloadEvent =
        _downloadEvent.asSharedFlow()

    private val _shareEvent =
        MutableSharedFlow<ShareEvent>()

    val shareEvent =
        _shareEvent.asSharedFlow()

    private val _openFileEvent =
        MutableSharedFlow<OpenFileEvent>()

    val openFileEvent =
        _openFileEvent.asSharedFlow()

    private val _imagePreviewEvent =
        MutableSharedFlow<ImagePreviewEvent>()

    val imagePreviewEvent =
        _imagePreviewEvent.asSharedFlow()


    val category = MutableStateFlow(FileCategory.ALL)
    val categoryStatus = MutableStateFlow("")
    val scanning = MutableStateFlow(false)
    private var browseJob: Job? = null
    private val cloudRepository = CloudRepository()
    fun loadFiles(path: String, refresh: Boolean = false) {
        browseJob?.cancel()
        category.value = FileCategory.ALL; categoryStatus.value = ""; scanning.value = false
        _currentPath.value = path; _files.value = emptyList(); _loading.value = true; _error.value = ""
        browseJob = viewModelScope.launch {
            try {
                val config = dataStore.serverConfig.first(); val token = dataStore.token.first()
                _files.value = cloudRepository.listAll(config.server, token, path, refresh)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = e.message ?: "加载失败" }
            finally { if (isActive) { _loading.value = false; _refreshing.value = false } }
        }
    }
    fun selectCategory(selected: FileCategory) {
        if (selected == FileCategory.ALL) { loadFiles(_currentPath.value); return }
        browseJob?.cancel()
        category.value = selected; _files.value = emptyList(); _error.value = ""; _loading.value = false
        scanning.value = true; categoryStatus.value = "正在汇总云盘中的${selected.label}…"
        browseJob = viewModelScope.launch {
            try {
                val config = dataStore.serverConfig.first(); val token = dataStore.token.first()
                val pending = ArrayDeque<String>(); pending.add("/")
                val seen = mutableSetOf<String>(); val matches = mutableListOf<FileItem>()
                var skipped = 0
                while (pending.isNotEmpty() && seen.size < 5000) {
                    ensureActive()
                    val path = pending.removeFirst()
                    if (!seen.add(path)) continue
                    try {
                        val batch = cloudRepository.listAll(config.server, token, path)
                        batch.filter { it.is_dir }.forEach { pending.add(it.fullPath(path)) }
                        matches += batch.filter { selected.matches(it) }
                        _files.value = matches.toList()
                    } catch (e: CancellationException) { throw e }
                    catch (e: Exception) { skipped++ }
                    categoryStatus.value = "已检查 ${seen.size} 个目录 · 找到 ${matches.size} 项 · 可停止"
                }
                categoryStatus.value = "全盘分类 · ${matches.size} 项" + (if (skipped > 0) " · $skipped 个目录无法读取" else "") +
                    (if (pending.isNotEmpty()) " · 已达扫描上限，结果未完整" else "")
                if (seen.size == skipped) _error.value = "无法读取云盘目录，请检查连接和访问权限"
            } finally { if (isActive) scanning.value = false }
        }
    }
    fun stopCategoryScan() { browseJob?.cancel(); scanning.value = false; categoryStatus.value = "扫描已停止 · 当前结果 ${_files.value.size} 项" }
    fun getFile(
        path: String,
        fileName: String
    ) {
        resolveFileUrl(path, fileName, FileAction.Download)
    }

    fun shareFile(
        path: String,
        fileName: String
    ) {
        resolveFileUrl(path, fileName, FileAction.Share)
    }

    fun openFile(
        path: String,
        fileName: String
    ) {
        resolveFileUrl(path, fileName, FileAction.Open)
    }

    private fun resolveFileUrl(
        path: String,
        fileName: String,
        action: FileAction
    ) {

        viewModelScope.launch {

            try {

                val config =
                    dataStore.serverConfig.first()

                val token =
                    dataStore.token.first()

                val result =
                    repository.getFile(

                        config.server,

                        token,

                        path

                    )

                if (result.code == 200) {

                    val rawUrl = result.data?.raw_url.orEmpty()

                    if (rawUrl.isBlank()) {
                        _error.value = "未获取到文件地址"
                        return@launch
                    }

                    when (action) {
                        FileAction.Download -> {
                            Log.d("AListDownload", "emit")
                            _downloadEvent.emit(
                                DownloadEvent(
                                    url = rawUrl,
                                    fileName = fileName
                                )
                            )
                        }

                        FileAction.Share -> {
                            _shareEvent.emit(
                                ShareEvent(
                                    url = rawUrl,
                                    fileName = fileName
                                )
                            )
                        }

                        FileAction.Open -> {
                            _openFileEvent.emit(
                                OpenFileEvent(
                                    url = rawUrl,
                                    fileName = fileName
                                )
                            )
                        }
                    }

                } else {

                    _error.value =
                        result.message

                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "获取下载地址失败"

            }

        }

    }

    private enum class FileAction {
        Download,
        Share,
        Open
    }

    fun getPreviewUrl(
        path: String,
        fileName: String
    ) {

        viewModelScope.launch {

            try {

                val config =
                    dataStore.serverConfig.first()

                val token =
                    dataStore.token.first()

                val result =
                    repository.getFile(

                        config.server,

                        token,

                        path

                    )

                if (result.code == 200) {

                    Log.d("AListPreview", "emit")

                    _imagePreviewEvent.emit(

                        ImagePreviewEvent(

                            url = result.data?.raw_url

                                ?: "",

                            fileName = fileName

                        )

                    )

                } else {

                    _error.value =
                        result.message

                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "获取图片地址失败"

            }

        }

    }
}
