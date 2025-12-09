package com.delivery.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.delivery.core.model.network.Song

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist_name"])
    ]
)
data class SongEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "artist_name")
    var artistName: String?,
    
    @ColumnInfo(name = "duration")
    val duration: Int?,
    
    @ColumnInfo(name = "url")
    var url: String?,
    
    @ColumnInfo(name = "image_url")
    var imageUrl: String?,
    
    @ColumnInfo(name = "album_id")
    val albumId: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions để convert giữa Song và SongEntity
fun Song.toEntity(): SongEntity {
    return SongEntity(
        id = this.id ?: "",
        title = this.title ?: "",
        artistName = "",
        duration = this.duration,
        url = "",
        imageUrl = "",
        albumId = ""
    )
}

fun SongEntity.toModel(): Song {
    return Song().apply {
        id = this@toModel.id
        title = this@toModel.title
        artistName = this@toModel.artistName
        duration = this@toModel.duration
        url = this@toModel.url
        imageUrl = this@toModel.imageUrl
        albumId = 1
    }
}
