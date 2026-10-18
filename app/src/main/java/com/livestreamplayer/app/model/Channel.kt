package com.livestreamplayer.app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Channel(
    val name: String,
    val url: String,
    val logoUrl: String = "",
    val group: String = "Uncategorized",
    val language: String = "",
    val country: String = "",
    val tvgId: String = "",
    val tvgName: String = "",
    val userAgent: String = "",
    val referrer: String = "",
    val isFavorite: Boolean = false
) {
    val id: String get() = "${name}_${url}".hashCode().toString()
}

@Entity(tableName = "favorites")
data class FavoriteChannel(
    @PrimaryKey val channelId: String,
    val name: String,
    val url: String,
    val logoUrl: String = "",
    val group: String = "Uncategorized",
    val addedAt: Long = System.currentTimeMillis()
)

data class Category(
    val name: String,
    val channelCount: Int,
    val iconUrl: String = ""
)

data class Playlist(
    val name: String,
    val url: String,
    val channels: List<Channel> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntry(
    @PrimaryKey val channelId: String,
    val name: String,
    val url: String,
    val logoUrl: String = "",
    val group: String = "Uncategorized",
    val lastWatched: Long = System.currentTimeMillis(),
    val watchCount: Int = 1
)
