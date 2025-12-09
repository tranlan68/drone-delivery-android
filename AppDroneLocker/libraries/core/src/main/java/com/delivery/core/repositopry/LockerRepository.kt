package com.delivery.core.repositopry

import com.delivery.core.base.BaseRepository
import com.delivery.core.model.mocking.LockerDto
import com.delivery.core.model.network.mocking.Locker
import com.delivery.core.network.ApiInterface
import com.delivery.core.utils.LockerDisplayHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockerRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val lockerDisplayHandler: LockerDisplayHandler
) : BaseRepository() {

    private var cachedLockers: List<LockerDto>? = null

    /**
     * Get all lockers from API with caching (returns flattened Locker list including nested lockers)
     * This includes both main lockers and their nested lockers
     */
    suspend fun getLockers(): Flow<List<Locker>> = flow {
        try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API")
                cachedLockers = apiInterface.getLockerList()
            }
            
            val lockers = cachedLockers?.let { lockerDtos ->
                // Use LockerDisplayHandler to flatten all lockers (main + nested)
                lockerDisplayHandler.flattenAllLockers(lockerDtos)
            } ?: emptyList()
            
            Timber.d("Returning ${lockers.size} lockers (flattened from ${cachedLockers?.size} main lockers)")
            emit(lockers)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching lockers")
            emit(emptyList())
        }
    }

    /**
     * Get only main lockers (without nested ones) from API
     */
    suspend fun getMainLockersOnly(): Flow<List<Locker>> = flow {
        try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API")
                cachedLockers = apiInterface.getLockerList()
            }
            
            val mainLockers = cachedLockers?.let { lockerDtos ->
                lockerDisplayHandler.getMainLockersOnly(lockerDtos)
            } ?: emptyList()
            
            Timber.d("Returning ${mainLockers.size} main lockers only")
            emit(mainLockers)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching main lockers")
            emit(emptyList())
        }
    }

    /**
     * Get only nested lockers from API
     */
    suspend fun getNestedLockersOnly(): Flow<List<Locker>> = flow {
        try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API")
                cachedLockers = apiInterface.getLockerList()
            }
            
            val nestedLockers = cachedLockers?.let { lockerDtos ->
                lockerDisplayHandler.getNestedLockersOnly(lockerDtos)
            } ?: emptyList()
            
            Timber.d("Returning ${nestedLockers.size} nested lockers only")
            emit(nestedLockers)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching nested lockers")
            emit(emptyList())
        }
    }

    /**
     * Get locker statistics
     */
    suspend fun getLockerStatistics(): Map<String, Int> {
        return try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API for statistics")
                cachedLockers = apiInterface.getLockerList()
            }
            
            cachedLockers?.let { lockerDtos ->
                lockerDisplayHandler.getLockerStatistics(lockerDtos)
            } ?: mapOf(
                "main_lockers" to 0,
                "nested_lockers" to 0,
                "total_lockers" to 0
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting locker statistics")
            mapOf(
                "main_lockers" to 0,
                "nested_lockers" to 0,
                "total_lockers" to 0
            )
        }
    }

    /**
     * Get all locker DTOs from API with caching (for internal use)
     */
    private suspend fun getLockerDtos(): Flow<List<LockerDto>> = flow {
        try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API")
                cachedLockers = apiInterface.getLockerList()
            }
            emit(cachedLockers ?: emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Error fetching lockers")
            emit(emptyList())
        }
    }

    /**
     * Get locker name by ID
     */
    suspend fun getLockerNameById(lockerId: String): String {
        return try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API for name lookup")
                cachedLockers = apiInterface.getLockerList()
                Timber.d("Cached ${cachedLockers?.size} lockers from API")
            }
            
            // Check real API data first
            val apiLocker = cachedLockers?.find { it.id == lockerId }
            if (apiLocker != null) {
                Timber.d("Found API locker $lockerId: ${apiLocker.name}")
                return apiLocker.name
            }
            
            Timber.d("Locker $lockerId not found in API, using mock data")
            return ""
        } catch (e: Exception) {
            Timber.e(e, "Error getting locker name for ID: $lockerId")
            return ""
        }
    }

    /**
     * Get locker position (LatLng) by ID
     */
    suspend fun getLockerPositionById(lockerId: String): org.maplibre.android.geometry.LatLng? {
        return try {
            if (cachedLockers == null) {
                Timber.d("Fetching lockers from API for position lookup")
                cachedLockers = apiInterface.getLockerList()
                Timber.d("Cached ${cachedLockers?.size} lockers from API")
                
                // Debug: Log all locker IDs we have
                cachedLockers?.forEach { locker ->
                    Timber.d("Available locker: ${locker.id} - ${locker.name} at ${locker.position}")
                }
            }
            
            // Check real API data first
            val apiLocker = cachedLockers?.find { it.id == lockerId }
            if (apiLocker != null && apiLocker.position.size >= 2) {
                val longitude = apiLocker.position[0]
                val latitude = apiLocker.position[1]
                
                Timber.d("API Locker $lockerId: position=[${longitude}, ${latitude}]")
                
                // Validate coordinates
                if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
                    return org.maplibre.android.geometry.LatLng(latitude, longitude)
                } else {
                    Timber.w("Invalid coordinates for locker $lockerId: lat=$latitude, lng=$longitude")
                    // Try swapped coordinates in case API format is different
                    if (longitude >= -90 && longitude <= 90 && latitude >= -180 && latitude <= 180) {
                        Timber.d("Trying swapped coordinates for locker $lockerId")
                        return org.maplibre.android.geometry.LatLng(longitude, latitude)
                    }
                    return null
                }
            }
            
            Timber.d("Locker $lockerId not found in API (${cachedLockers?.size} lockers), using mock data")
            return null
        } catch (e: Exception) {
            return null
        }
    }



    /**
     * Clear cached lockers (useful for refresh)
     */
    fun clearCache() {
        cachedLockers = null
    }

    /**
     * Map LockerDto to Locker (for UI compatibility) - DEPRECATED
     * Use LockerDisplayHandler methods instead for better hierarchy handling
     */
    @Deprecated("Use LockerDisplayHandler methods instead for better hierarchy handling")
    private fun mapToLocker(lockerDto: LockerDto): Locker {
        return Locker(
            id = lockerDto.id,
            lockerName = lockerDto.name,
            description = lockerDto.description,
            position = lockerDto.position,
            createdBy = lockerDto.createdBy ?: ""
        )
    }
}