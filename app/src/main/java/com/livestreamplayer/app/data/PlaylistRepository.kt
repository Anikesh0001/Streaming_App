package com.livestreamplayer.app.data

import android.content.Context
import com.livestreamplayer.app.App
import com.livestreamplayer.app.model.Channel
import com.livestreamplayer.app.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class PlaylistRepository {

    suspend fun loadPlaylist(playlistUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val url = URL(playlistUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
            connection.setRequestProperty("Accept", "*/*")
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val content = connection.inputStream.bufferedReader().readText()
                connection.disconnect()
                M3UParser.parse(content)
            } else {
                connection.disconnect()
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun loadBundledPlaylist(context: Context): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open(App.BUNDLED_PLAYLIST_ASSET)
            M3UParser.parse(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getCategories(channels: List<Channel>): List<Category> {
        return channels.groupBy { it.group }
            .map { (name, channelList) ->
                Category(
                    name = name,
                    channelCount = channelList.size,
                    iconUrl = channelList.firstOrNull()?.logoUrl ?: ""
                )
            }
            .sortedBy { it.name }
    }

    fun searchChannels(channels: List<Channel>, query: String): List<Channel> {
        if (query.isBlank()) return channels
        val lowerQuery = query.lowercase()
        return channels.filter {
            it.name.lowercase().contains(lowerQuery) ||
                    it.group.lowercase().contains(lowerQuery) ||
                    it.tvgName.lowercase().contains(lowerQuery)
        }
    }

    fun filterByCategory(channels: List<Channel>, category: String): List<Channel> {
        if (category.isBlank() || category == "All") return channels
        return channels.filter { it.group == category }
    }
}
