package com.delivery.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.delivery.database.entity.LockerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data class for locker name mapping
 */
data class LockerNameMapping(
    val id: String,
    val lockerName: String,
)

@Dao
interface LockerDao {
    @Query("SELECT * FROM lockers WHERE is_active = 1 ORDER BY locker_name ASC")
    fun getAllActiveLockers(): Flow<List<LockerEntity>>

    @Query("SELECT * FROM lockers ORDER BY locker_name ASC")
    fun getAllLockers(): Flow<List<LockerEntity>>

    @Query("SELECT * FROM lockers WHERE id = :lockerId")
    suspend fun getLockerById(lockerId: String): LockerEntity?

    @Query("SELECT * FROM lockers WHERE id IN (:lockerIds)")
    suspend fun getLockersByIds(lockerIds: List<String>): List<LockerEntity>

    @Query("SELECT * FROM lockers WHERE locker_name LIKE '%' || :query || '%' AND is_active = 1 ORDER BY locker_name ASC LIMIT :limit")
    fun searchLockers(
        query: String,
        limit: Int = 50,
    ): Flow<List<LockerEntity>>

    @Query("SELECT * FROM lockers WHERE created_by = :createdBy AND is_active = 1 ORDER BY created_at DESC")
    fun getLockersByCreator(createdBy: String): Flow<List<LockerEntity>>

    @Query("SELECT * FROM lockers ORDER BY created_at DESC LIMIT :limit")
    fun getRecentLockers(limit: Int = 20): Flow<List<LockerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocker(locker: LockerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLockers(lockers: List<LockerEntity>)

    @Update
    suspend fun updateLocker(locker: LockerEntity)

    @Delete
    suspend fun deleteLocker(locker: LockerEntity)

    @Query("DELETE FROM lockers WHERE id = :lockerId")
    suspend fun deleteLockerById(lockerId: String)

    @Query("UPDATE lockers SET is_active = 0 WHERE id = :lockerId")
    suspend fun deactivateLocker(lockerId: String)

    @Query("UPDATE lockers SET is_active = 1 WHERE id = :lockerId")
    suspend fun activateLocker(lockerId: String)

    @Query("DELETE FROM lockers")
    suspend fun deleteAllLockers()

    @Query("DELETE FROM lockers WHERE created_at < :timestamp")
    suspend fun deleteOldLockers(timestamp: Long)

    @Query("SELECT COUNT(*) FROM lockers WHERE is_active = 1")
    suspend fun getActiveLockerCount(): Int

    @Query("SELECT COUNT(*) FROM lockers")
    suspend fun getTotalLockerCount(): Int

    @Query("SELECT * FROM lockers WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng AND is_active = 1")
    suspend fun getLockersInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
    ): List<LockerEntity>

    /**
     * Lấy locker name từ ID - dùng cho mapping trong OrderMapper
     */
    @Query("SELECT locker_name FROM lockers WHERE id = :lockerId AND is_active = 1")
    suspend fun getLockerNameById(lockerId: String): String?

    /**
     * Lấy multiple locker names từ IDs
     */
    @Query("SELECT id, locker_name as lockerName FROM lockers WHERE id IN (:lockerIds) AND is_active = 1")
    suspend fun getLockerNameMappings(lockerIds: List<String>): List<LockerNameMapping>

    /**
     * Kiểm tra locker có tồn tại không
     */
    @Query("SELECT EXISTS(SELECT 1 FROM lockers WHERE id = :lockerId AND is_active = 1)")
    suspend fun isLockerExists(lockerId: String): Boolean

    /**
     * Cập nhật timestamp khi sync với API
     */
    @Query("UPDATE lockers SET updated_at = :timestamp WHERE id = :lockerId")
    suspend fun updateLastSyncTime(
        lockerId: String,
        timestamp: Long = System.currentTimeMillis(),
    )
}
