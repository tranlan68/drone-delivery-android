package com.delivery.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.delivery.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE album_id = :albumId ORDER BY title ASC")
    fun getSongsByAlbum(albumId: String): Flow<List<SongEntity>>

    @Query(
        "SELECT * FROM songs " +
            "WHERE title LIKE '%' || :query || '%' " +
            "OR artist_name LIKE '%' || :query || '%' " +
            "ORDER BY title ASC " +
            "LIMIT :limit",
    )
    fun searchSongs(
        query: String,
        limit: Int = 50,
    ): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY created_at DESC LIMIT :limit")
    fun getRecentSongs(limit: Int = 20): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Delete
    suspend fun deleteSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()

    @Query("DELETE FROM songs WHERE created_at < :timestamp")
    suspend fun deleteOldSongs(timestamp: Long)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongsCount(): Int

    @Query("SELECT COUNT(*) FROM songs WHERE album_id = :albumId")
    suspend fun getSongsCountByAlbum(albumId: String): Int
}
