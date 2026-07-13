package com.kmuaz.alistcloud.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.kmuaz.alistcloud.model.ServerConfig
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {

        private val SERVER =
            stringPreferencesKey("server")

        private val USERNAME =
            stringPreferencesKey("username")

        private val PASSWORD =
            stringPreferencesKey("password")

        private val TOKEN =
            stringPreferencesKey("token")
    }

    suspend fun saveServerConfig(config: ServerConfig) {
        suspend fun saveToken(token: String) {

            context.dataStore.edit { preferences ->

                preferences[TOKEN] = token

            }

        }

        context.dataStore.edit { preferences ->

            preferences[SERVER] = config.server
            preferences[USERNAME] = config.username
            preferences[PASSWORD] = config.password
        }
    }

    val serverConfig: Flow<ServerConfig> =

        context.dataStore.data

            .catch { exception ->

                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }

            }

            .map { preferences ->

                ServerConfig(

                    server = preferences[SERVER] ?: "",

                    username = preferences[USERNAME] ?: "",

                    password = preferences[PASSWORD] ?: ""

                )

            }
    val token: Flow<String> =

        context.dataStore.data.map {

            it[TOKEN] ?: ""

        }
}
