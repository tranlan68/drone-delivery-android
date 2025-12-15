package com.delivery.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lockers",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["locker_name"]),
        Index(value = ["created_at"]),
    ],
)
data class LockerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "locker_name")
    val lockerName: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "latitude")
    val latitude: Double,
    @ColumnInfo(name = "longitude")
    val longitude: Double,
    @ColumnInfo(name = "created_by")
    val createdBy: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
) {
    /**
     * Tạo position array từ latitude và longitude
     */
    fun getPositionArray(): List<Double> {
        return listOf(latitude, longitude)
    }

    /**
     * Kiểm tra xem locker có hợp lệ không
     */
    fun isValid(): Boolean {
        return id.isNotEmpty() &&
            lockerName.isNotEmpty() &&
            latitude != 0.0 &&
            longitude != 0.0
    }

    /**
     * Tạo display name cho locker
     */
    fun getDisplayName(): String {
        return lockerName.takeIf { it.isNotEmpty() } ?: "Locker $id"
    }
}
