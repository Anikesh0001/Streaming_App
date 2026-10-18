package com.livestreamplayer.app.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.livestreamplayer.app.model.WatchHistoryEntry

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC LIMIT 50")
    fun getRecentHistory(): LiveData<List<WatchHistoryEntry>>

    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC LIMIT 50")
    suspend fun getRecentHistorySync(): List<WatchHistoryEntry>

    @Query("SELECT * FROM watch_history ORDER BY watchCount DESC LIMIT 20")
    fun getMostWatched(): LiveData<List<WatchHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entry: WatchHistoryEntry)

    @Query("UPDATE watch_history SET lastWatched = :time, watchCount = watchCount + 1 WHERE channelId = :channelId")
    suspend fun updateWatchTime(channelId: String, time: Long = System.currentTimeMillis())

    @Query("SELECT EXISTS(SELECT 1 FROM watch_history WHERE channelId = :channelId)")
    suspend fun exists(channelId: String): Boolean

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()

    @Query("DELETE FROM watch_history WHERE channelId = :channelId")
    suspend fun remove(channelId: String)
}
