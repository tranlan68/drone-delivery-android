package com.delivery.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.delivery.database.converter.DateConverter
import com.delivery.database.dao.AlbumDao
import com.delivery.database.dao.ArtistDao
import com.delivery.database.dao.SongDao
import com.delivery.database.entity.AlbumEntity
import com.delivery.database.entity.ArtistEntity
import com.delivery.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun albumDao(): AlbumDao

    abstract fun artistDao(): ArtistDao

    companion object {
        const val DATABASE_NAME = "delivery_music_database"
    }
}
