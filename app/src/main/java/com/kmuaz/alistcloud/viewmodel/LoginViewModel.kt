package com.kmuaz.alistcloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kmuaz.alistcloud.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kmuaz.alistcloud.data.datastore.DataStoreManager
import com.kmuaz.alistcloud.model.ServerConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = LoginRepository()
    private val dataStore =
        DataStoreManager(application)

    val serverConfig =
        dataStore.serverConfig.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ServerConfig()
        )

    private val _loginState = MutableStateFlow(false)
    val loginState: StateFlow<Boolean> = _loginState

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    fun login(
        server: String,
        username: String,
        password: String
    ) {

        viewModelScope.launch {

            _loading.value = true
            _error.value = ""

            try {

                val result = repository.login(
                    server,
                    username,
                    password
                )

                if (result.code == 200) {

                    result.data?.token?.let {

                        dataStore.saveToken(it)

                    }

                    _loginState.value = true

                } else {

                    _error.value = result.message

                }

            } catch (e: Exception) {

                _error.value = e.message ?: "连接失败"

            }

            _loading.value = false

        }

    }

}