package com.hussain.learningdashboard.feature.auth.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SessionStorage {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    suspend fun clear()
}

/**
 * Plain DataStore is fine for a mock token. A production build would keep the token in
 * Android Keystore-backed encrypted storage and the iOS Keychain (see README).
 */
class DataStoreSessionStorage(private val dataStore: DataStore<Preferences>) : SessionStorage {

    override suspend fun saveToken(token: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
    }

    override suspend fun getToken(): String? = dataStore.data.map { it[TOKEN_KEY] }.first()

    override suspend fun clear() {
        dataStore.edit { it.remove(TOKEN_KEY) }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
