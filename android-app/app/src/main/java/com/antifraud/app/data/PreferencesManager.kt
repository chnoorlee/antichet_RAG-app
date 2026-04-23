package com.antifraud.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "antifraud_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        const val DEFAULT_API_URL = "http://10.0.2.2:8000"
    }

    val apiBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_BASE_URL] ?: DEFAULT_API_URL
    }

    suspend fun saveApiBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[API_BASE_URL] = url
        }
    }
}
