package com.kmuaz.alistcloud.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmuaz.alistcloud.data.download.DownloadTask
import com.kmuaz.alistcloud.data.repository.DownloadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DownloadRepository(application)
    private val _tasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val tasks = _tasks.asStateFlow()

    private val _openFileEvent = MutableSharedFlow<OpenDownloadedFileEvent>()
    val openFileEvent = _openFileEvent.asSharedFlow()

    private var monitorJob: Job? = null

    fun startMonitoring() {
        if (monitorJob?.isActive == true) return

        monitorJob = viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(1_000)
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    fun refresh() {
        _tasks.value = repository.getTasks()
    }

    fun openCompletedTask(task: DownloadTask) {
        val uri = repository.getCompletedFileUri(task.id) ?: return
        viewModelScope.launch {
            _openFileEvent.emit(
                OpenDownloadedFileEvent(uri, task.mimeType, task.fileName)
            )
        }
    }

    fun deleteTasks(tasks: List<DownloadTask>, deleteLocalFiles: Boolean) {
        repository.deleteTasks(tasks, deleteLocalFiles)
        refresh()
    }

    fun cancelTask(task: DownloadTask) {
        repository.deleteTasks(listOf(task), deleteLocalFiles = true)
        refresh()
    }

    override fun onCleared() {
        stopMonitoring()
        super.onCleared()
    }
}

data class OpenDownloadedFileEvent(
    val uri: Uri,
    val mimeType: String?,
    val fileName: String
)
