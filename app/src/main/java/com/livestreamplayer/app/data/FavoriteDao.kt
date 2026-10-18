package com.livestreamplayer.app.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.livestreamplayer.app.model.FavoriteChannel

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): LiveData<List<FavoriteChannel>>

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavoritesSync(): List<FavoriteChannel>

    @Query("SELECT channelId FROM favorites")
    suspend fun getAllFavoriteIds(): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE channelId = :channelId)")
    suspend fun isFavorite(channelId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(channel: FavoriteChannel)

    @Delete
    suspend fun removeFavorite(channel: FavoriteChannel)

    @Query("DELETE FROM favorites WHERE channelId = :channelId")
    suspend fun removeFavoriteById(channelId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
