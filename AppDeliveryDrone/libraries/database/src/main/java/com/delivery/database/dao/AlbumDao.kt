package com.delivery.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.delivery.database.entity.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY title ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE artist_name = :artistName ORDER BY release_date DESC")
    fun getAlbumsByArtist(artistName: String): Flow<List<AlbumEntity>>

    @Query(
        "SELECT * FROM albums " +
            "WHERE title LIKE '%' || :query || '%' " +
            "OR artist_name LIKE '%' || :query || '%' " +
            "ORDER BY title ASC " +
            "LIMIT :limit",
    )
    fun searchAlbums(
        query: String,
        limit: Int = 50,
    ): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY created_at DESC LIMIT :limit")
    fun getRecentAlbums(limit: Int = 20): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums ORDER BY release_date DESC LIMIT :limit")
    fun getLatestReleases(limit: Int = 10): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Update
    suspend fun updateAlbum(album: AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("DELETE FROM albums WHERE id = :albumId")
    suspend fun deleteAlbumById(albumId: String)

    @Query("DELETE FROM albums")
    suspend fun deleteAllAlbums()

    @Query("DELETE FROM albums WHERE created_at < :timestamp")
    suspend fun deleteOldAlbums(timestamp: Long)

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getAlbumsCount(): Int

    @Query("SELECT COUNT(*) FROM albums WHERE artist_name = :artistName")
    suspend fun getAlbumsCountByArtist(artistName: String): Int
}
