package com.delivery.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.delivery.core.model.network.Album

@Entity(
    tableName = "albums",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist_name"])
    ]
)
data class AlbumEntity(
    @PrimaryKey 
    val id: String,
    
    @ColumnInfo(name = "title")
    var title: String,
    
    @ColumnInfo(name = "artist_name")
    var artistName: String?,
    
    @ColumnInfo(name = "release_date")
    var releaseDate: String?,
    
    @ColumnInfo(name = "image_url")
    var imageUrl: String?,
    
    @ColumnInfo(name = "track_count")
    var trackCount: Int?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions để convert giữa Album và AlbumEntity
fun Album.toEntity(): AlbumEntity {
    return AlbumEntity(
        id = this.id.toString() ?: "",
        title = "",
        artistName = "",
        releaseDate = "",
        imageUrl = "",
        trackCount = 1
    )
}

fun AlbumEntity.toModel(): Album {
    return Album().apply {
        id = 1
        title = this@toModel.title
        artistName = this@toModel.artistName
        releaseDate = this@toModel.releaseDate
        imageUrl = this@toModel.imageUrl
        trackCount = this@toModel.trackCount
    }
}
