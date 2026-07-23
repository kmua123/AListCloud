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
    fun loadFiles(
        path: String,
        refresh: Boolean = false
    ) {

        _currentPath.value = path

        viewModelScope.launch {

            if (refresh) {
                _refreshing.value = true
            } else {
                _loading.value = true
            }
            _error.value = ""

            try {

                val config = dataStore.serverConfig.first()
                val token = dataStore.token.first()

                val result =
                    repository.getFiles(
                        config.server,
                        token,
                        path
                    )

                if (result.code == 200) {

                    _files.value =
                        result.data?.content ?: emptyList()

                } else {

                    _error.value =
                        result.message

                }

            } catch (e: Exception) {

                _error.value =
                    e.message ?: "加载失败"

            }

            _loading.value = false
            _refreshing.value = false

        }

    }
}