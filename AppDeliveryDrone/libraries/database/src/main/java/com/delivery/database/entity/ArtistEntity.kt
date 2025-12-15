package com.delivery.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.delivery.core.model.network.Artist

@Entity(
    tableName = "artists",
    indices = [
        Index(value = ["name"], unique = true),
    ],
)
data class ArtistEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "bio")
    var bio: String?,
    @ColumnInfo(name = "image_url")
    var imageUrl: String?,
    @ColumnInfo(name = "genre")
    var genre: String?,
    @ColumnInfo(name = "followers_count")
    var followersCount: Int?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)

// Extension functions để convert giữa Artist và ArtistEntity
fun Artist.toEntity(): ArtistEntity {
    return ArtistEntity(
        id = this.id.toString() ?: "",
        name = this.name ?: "",
        bio = "",
        imageUrl = "",
        genre = "",
        followersCount = 1,
    )
}

fun ArtistEntity.toModel(): Artist {
    return Artist().apply {
        id = 1
        name = ""
        bio = this@toModel.bio ?: ""
        imageUrl = this@toModel.imageUrl
        genre = this@toModel.genre
        followersCount = this@toModel.followersCount
    }
}
