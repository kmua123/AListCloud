package com.kmuaz.alistcloud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmuaz.alistcloud.data.datastore.DataStoreManager
import com.kmuaz.alistcloud.model.ServerConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dataStore =
        DataStoreManager(application)

    val serverConfig =
        dataStore.serverConfig.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServerConfig()
        )

    fun saveConfig(config: ServerConfig) {

        viewModelScope.launch {

            dataStore.saveServerConfig(config)

        }

    }

}