package com.delivery.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.delivery.database.entity.ArtistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE name = :name")
    suspend fun getArtistByName(name: String): ArtistEntity?

    @Query("SELECT * FROM artists WHERE name LIKE '%' || :query || '%' OR genre LIKE '%' || :query || '%' ORDER BY name ASC LIMIT :limit")
    fun searchArtists(
        query: String,
        limit: Int = 50,
    ): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists WHERE genre = :genre ORDER BY followers_count DESC")
    fun getArtistsByGenre(genre: String): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists ORDER BY followers_count DESC LIMIT :limit")
    fun getPopularArtists(limit: Int = 20): Flow<List<ArtistEntity>>

    @Query("SELECT * FROM artists ORDER BY created_at DESC LIMIT :limit")
    fun getRecentArtists(limit: Int = 20): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Update
    suspend fun updateArtist(artist: ArtistEntity)

    @Delete
    suspend fun deleteArtist(artist: ArtistEntity)

    @Query("DELETE FROM artists WHERE id = :artistId")
    suspend fun deleteArtistById(artistId: String)

    @Query("DELETE FROM artists")
    suspend fun deleteAllArtists()

    @Query("DELETE FROM artists WHERE created_at < :timestamp")
    suspend fun deleteOldArtists(timestamp: Long)

    @Query("SELECT COUNT(*) FROM artists")
    suspend fun getArtistsCount(): Int

    @Query("SELECT COUNT(*) FROM artists WHERE genre = :genre")
    suspend fun getArtistsCountByGenre(genre: String): Int
}
