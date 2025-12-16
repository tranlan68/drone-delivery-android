package com.delivery.core.utils

import com.delivery.core.model.mocking.LockerDto
import com.delivery.core.model.network.mocking.Locker
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to handle displaying all lockers including nested ones from LockerDto
 * This class flattens the hierarchical structure of lockers to show all available lockers on map
 */

@Singleton
class LockerDisplayHandler @Inject constructor() {

    /**
     * Flatten all lockers from LockerDto hierarchy to a flat list
     * This includes both main lockers and their nested lockers
     * 
     * @param lockerDtos List of LockerDto objects from API
     * @return Flattened list of all lockers (main + nested)
     */
    fun flattenAllLockers(lockerDtos: List<LockerDto>): List<Locker> {
        val allLockers = mutableListOf<Locker>()
        
        lockerDtos.forEach { mainLockerDto ->
            // Add main locker
            val mainLocker = mapToLocker(mainLockerDto, isMainLocker = true)
            allLockers.add(mainLocker)
            Timber.tag("LockerDisplayHandler").d("Added main locker: ${mainLocker.lockerName}")
            
            // Add nested lockers if they exist
            if (!mainLockerDto.lockers.isNullOrEmpty()) {
                mainLockerDto.lockers.forEach { nestedLockerDto ->
                    val nestedLocker = mapToLocker(nestedLockerDto, isMainLocker = false, parentLockerName = mainLockerDto.name)
                    allLockers.add(nestedLocker)
                    Timber.tag("LockerDisplayHandler").d("Added nested locker: ${nestedLocker.lockerName} (parent: ${mainLockerDto.name})")
                }
            }
        }
        
        Timber.tag("LockerDisplayHandler").d("Total lockers flattened: ${allLockers.size} (from ${lockerDtos.size} main lockers)")
        return allLockers
    }

    /**
     * Map LockerDto to Locker with additional information about locker hierarchy
     * 
     * @param lockerDto The LockerDto to convert
     * @param isMainLocker Whether this is a main locker or nested locker
     * @param parentLockerName Name of parent locker (for nested lockers)
     * @return Locker object with modified name to show hierarchy
     */
    private fun mapToLocker(
        lockerDto: LockerDto, 
        isMainLocker: Boolean = true,
        parentLockerName: String? = null
    ): Locker {
        // Modify name to show hierarchy for nested lockers
        val displayName = if (isMainLocker) {
            lockerDto.name
        } else {
            "${lockerDto.name} (${parentLockerName})"
        }
        
        return Locker(
            id = lockerDto.id,
            lockerName = displayName,
            description = lockerDto.description,
            position = lockerDto.position,
            createdBy = lockerDto.createdBy ?: ""
        )
    }

    /**
     * Get only main lockers (without nested ones) for specific use cases
     * 
     * @param lockerDtos List of LockerDto objects from API
     * @return List of only main lockers
     */
    fun getMainLockersOnly(lockerDtos: List<LockerDto>): List<Locker> {
        return lockerDtos.map { mapToLocker(it, isMainLocker = true) }
    }

    /**
     * Get only nested lockers for specific use cases
     * 
     * @param lockerDtos List of LockerDto objects from API
     * @return List of only nested lockers
     */
    fun getNestedLockersOnly(lockerDtos: List<LockerDto>): List<Locker> {
        val nestedLockers = mutableListOf<Locker>()
        
        lockerDtos.forEach { mainLockerDto ->
            if (mainLockerDto.lockers != null && mainLockerDto.lockers.isNotEmpty()) {
                mainLockerDto.lockers.forEach { nestedLockerDto ->
                    val nestedLocker = mapToLocker(nestedLockerDto, isMainLocker = false, parentLockerName = mainLockerDto.name)
                    nestedLockers.add(nestedLocker)
                }
            }
        }
        
        return nestedLockers
    }

    /**
     * Count total lockers (main + nested) for statistics
     * 
     * @param lockerDtos List of LockerDto objects from API
     * @return Total count of all lockers
     */
    fun getTotalLockerCount(lockerDtos: List<LockerDto>): Int {
        var totalCount = lockerDtos.size // Main lockers
        
        lockerDtos.forEach { mainLockerDto ->
            totalCount += mainLockerDto.lockers?.size ?: 0 // Nested lockers
        }
        
        return totalCount
    }

    /**
     * Get statistics about locker distribution
     * 
     * @param lockerDtos List of LockerDto objects from API
     * @return Map with statistics
     */
    fun getLockerStatistics(lockerDtos: List<LockerDto>): Map<String, Int> {
        val mainLockerCount = lockerDtos.size
        val nestedLockerCount = lockerDtos.sumOf { it.lockers?.size ?: 0 }
        val totalCount = mainLockerCount + nestedLockerCount
        
        return mapOf(
            "main_lockers" to mainLockerCount,
            "nested_lockers" to nestedLockerCount,
            "total_lockers" to totalCount
        )
    }
}
