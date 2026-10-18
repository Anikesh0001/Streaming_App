package com.livestreamplayer.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PrefsManager(private val context: Context) {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PLAYLIST_URLS = stringSetPreferencesKey("playlist_urls")
        val BUFFER_SIZE_MS = intPreferencesKey("buffer_size_ms")
        val LAST_CHANNEL_URL = stringPreferencesKey("last_channel_url")
        val AUTO_PLAY = booleanPreferencesKey("auto_play")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val playlistUrls: Flow<Set<String>> = context.dataStore.data.map { it[PLAYLIST_URLS] ?: emptySet() }
    val bufferSizeMs: Flow<Int> = context.dataStore.data.map { it[BUFFER_SIZE_MS] ?: 5000 }
    val autoPlay: Flow<Boolean> = context.dataStore.data.map { it[AUTO_PLAY] ?: true }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun addPlaylistUrl(url: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PLAYLIST_URLS] ?: emptySet()
            prefs[PLAYLIST_URLS] = current + url
        }
    }

    suspend fun removePlaylistUrl(url: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PLAYLIST_URLS] ?: emptySet()
            prefs[PLAYLIST_URLS] = current - url
        }
    }

    suspend fun clearPlaylists() {
        context.dataStore.edit { it[PLAYLIST_URLS] = emptySet() }
    }

    suspend fun setBufferSize(ms: Int) {
        context.dataStore.edit { it[BUFFER_SIZE_MS] = ms }
    }

    suspend fun setLastChannel(url: String) {
        context.dataStore.edit { it[LAST_CHANNEL_URL] = url }
    }
}
