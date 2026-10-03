package com.kmuaz.alistcloud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmuaz.alistcloud.data.datastore.DataStoreManager
import com.kmuaz.alistcloud.data.network.RetrofitClient
import com.kmuaz.alistcloud.data.network.model.*
import com.kmuaz.alistcloud.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Uploads continue while switching tabs; the system's selected document grants remain in force.
data class UploadItem(val id: Long, val file: LocalUpload, val destination: String,
    val state: String = "等待上传", val sent: Long = 0, val error: String = "")
class CloudToolsViewModel(app: Application) : AndroidViewModel(app) {
    private val settings = DataStoreManager(app)
    private val repository = CloudRepository()
    private val _uploads = MutableStateFlow<List<UploadItem>>(emptyList())
    val uploads = _uploads.asStateFlow()
    private val uploadJobs = mutableMapOf<Long, Job>()
    private var nextId = 0L
    private val uploadMutex = Mutex()
    private val _archiveFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val archiveFiles = _archiveFiles.asStateFlow()
    val archiveBusy = MutableStateFlow(false)
    val archiveMessage = MutableStateFlow("")
    val localZipMode = MutableStateFlow(false)
    private var archiveJob: Job? = null
    private val zipRepository = ZipRepository(app)
    private var cachedZip: java.io.File? = null
    private var cachedZipPath = ""
    private fun unsupported(error: Exception): Boolean = error.message.orEmpty().contains("服务器不支持此接口") ||
        (error is retrofit2.HttpException && error.code() in setOf(404, 405, 501))
    private suspend fun localZip(server: String, token: String, path: String): java.io.File {
        if (cachedZipPath != path || cachedZip?.exists() != true) {
            cachedZip?.delete(); cachedZip = zipRepository.download(server, token, path); cachedZipPath = path
        }
        return cachedZip!!
    }

    private fun update(id: Long, transform: (UploadItem) -> UploadItem) { _uploads.update { list -> list.map { if (it.id == id) transform(it) else it } } }
    fun upload(files: List<LocalUpload>, destination: String) {
        files.forEach { file ->
            val id = ++nextId
            _uploads.update { it + UploadItem(id, file, destination) }
            uploadJobs[id] = viewModelScope.launch {
                try {
                    uploadMutex.withLock {
                    val config = settings.serverConfig.first(); val token = settings.token.first()
                    update(id) { it.copy(state = "检查目标目录") }
                    val existing = repository.listAll(config.server, token, destination)
                    check(existing.none { it.name == file.name }) { "云盘已存在同名文件，请先改名后上传" }
                    update(id) { it.copy(state = "上传中") }
                    withContext(Dispatchers.IO) {
                        repository.upload(config.server, token, destination, file, getApplication<Application>().contentResolver, coroutineContext) { sent, _ ->
                            update(id) { it.copy(sent = sent) }
                        }
                    }
                    update(id) { it.copy(state = "上传完成", sent = file.size.coerceAtLeast(it.sent)) }
                    }
                } catch (e: CancellationException) {
                    update(id) { it.copy(state = "已取消", error = "服务器可能保留已接收的部分文件，请刷新目录确认") }; throw e
                } catch (e: Exception) {
                    update(id) { it.copy(state = "上传失败", error = e.message ?: "上传失败") }
                }
            }
        }
    }
    fun cancelUpload(id: Long) { uploadJobs[id]?.cancel() }
    fun retry(item: UploadItem) { upload(listOf(item.file), item.destination) }

    fun listArchive(path: String, innerPath: String, password: String) {
        archiveJob?.cancel()
        _archiveFiles.value = emptyList(); archiveMessage.value = ""; archiveBusy.value = true
        archiveJob = viewModelScope.launch {
            try {
                val config = settings.serverConfig.first(); val token = settings.token.first()
                val api = RetrofitClient.create(config.server)
                val all = mutableListOf<FileItem>(); var page = 1
                do {
                    val response = api.archiveList(token, ArchiveListRequest(path, innerPath, password, page = page))
                    check(response.code == 200) { response.message }
                    val batch = response.data?.content.orEmpty()
                    all += batch
                    if (batch.isEmpty() || all.size >= (response.data?.total ?: 0) || batch.size < 500) break
                    page++
                } while (true)
                _archiveFiles.value = all
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (path.endsWith(".zip", true) && unsupported(e)) {
                    try {
                        localZipMode.value = true; archiveMessage.value = "服务器不支持压缩包接口，正在加载 ZIP 兼容模式…"
                        val config = settings.serverConfig.first(); val token = settings.token.first()
                        val zip = localZip(config.server, token, path)
                        _archiveFiles.value = withContext(Dispatchers.IO) { zipRepository.list(zip, innerPath) }
                        archiveMessage.value = "ZIP 兼容模式 · 解压后上传到新文件夹 · 最大 256 MB 压缩包 / 1 GB 解压内容"
                    } catch (cancel: CancellationException) { throw cancel }
                    catch (failure: Exception) { archiveMessage.value = "无法读取 ZIP：${failure.message}。加密 ZIP 请使用支持密码的服务器或其他应用。" }
                } else archiveMessage.value = "无法浏览压缩包：${e.message}。请确认服务器支持此格式并授予压缩包读取权限。"
            }
            finally { if (isActive) archiveBusy.value = false }
        }
    }
    fun closeArchive() {
        archiveJob?.cancel(); archiveBusy.value = false; cachedZip?.delete(); cachedZip = null; cachedZipPath = ""; localZipMode.value = false
    }
    fun decompress(path: String, destination: String, password: String) {
        archiveJob?.cancel(); archiveBusy.value = true; archiveMessage.value = "正在提交解压任务…"
        archiveJob = viewModelScope.launch {
            try {
                val config = settings.serverConfig.first(); val token = settings.token.first()
                val api = RetrofitClient.create(config.server)
                val parent = destination.substringBeforeLast('/', "").ifBlank { "/" }
                val folder = destination.substringAfterLast('/')
                require(folder.isNotBlank() && folder !in setOf(".", "..")) { "请输入有效的目标文件夹" }
                check(repository.listAll(config.server, token, parent).none { it.name == folder }) { "目标文件夹已存在，请选择新的文件夹名" }
                val mkdir = api.mkdir(token, mapOf("path" to destination))
                check(mkdir.code == 200) { mkdir.message }
                val response = try {
                    api.decompress(token, DecompressRequest(path.substringBeforeLast('/', "").ifBlank { "/" }, destination,
                        listOf(path.substringAfterLast('/')), password))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    if (!path.endsWith(".zip", true) || !unsupported(e)) throw e
                    require(password.isBlank()) { "ZIP 兼容模式不支持加密文件，请使用支持压缩包接口的服务器" }
                    localZipMode.value = true
                    archiveMessage.value = "正在解压 ZIP 并上传…关闭窗口会停止兼容模式处理"
                    val zip = localZip(config.server, token, path)
                    zipRepository.decompressToCloud(zip, config.server, token, destination, coroutineContext) { done, total ->
                        archiveMessage.value = "解压并上传 $done / $total 项 · $destination"
                    }
                    archiveMessage.value = "解压完成：$destination"
                    return@launch
                }
                check(response.code == 200) { response.message }
                val task = response.objectData?.getAsJsonArray("task")?.firstOrNull()?.asJsonObject
                val id = task?.get("id")?.asString
                if (id.isNullOrBlank()) { archiveMessage.value = "服务器已完成解压：$destination"; return@launch }
                archiveMessage.value = "解压任务已提交：$destination"
                while (true) {
                    delay(1500)
                    val info = api.decompressInfo(token, id)
                    if (info.code != 200) { archiveMessage.value = "任务已提交，无法获取进度，请稍后刷新目标目录"; break }
                    val data = info.objectData ?: break
                    val state = data.get("state")?.asInt ?: -1
                    val taskError = data.get("error")?.asString.orEmpty()
                    if (state == 2) { archiveMessage.value = "解压完成：$destination"; break }
                    if (state == 4 || state == 7) { archiveMessage.value = "解压未完成：${taskError.ifBlank { "任务已取消" }}"; break }
                    archiveMessage.value = "服务器解压中 ${data.get("progress")?.asDouble?.toInt() ?: 0}% · $destination"
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { archiveMessage.value = "解压失败：${e.message}。需要服务器解压及目标目录写入权限。" }
            finally { if (isActive) archiveBusy.value = false }
        }
    }
}
