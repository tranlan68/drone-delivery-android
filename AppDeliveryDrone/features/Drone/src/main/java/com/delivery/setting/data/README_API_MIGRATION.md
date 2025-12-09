# API Migration Guide

## Current Status
Currently using **Mock Data** for locker locations. The app displays 10 hardcoded locations in Hanoi.

## Files to Update When API is Ready

### 1. LockerRepository.kt
**Location**: `features/Drone/src/main/java/com/delivery/setting/repository/LockerRepository.kt`

**Current Code** (using mock data):
```kotlin
suspend fun getLockers(): Flow<List<Locker>> = flow {
    try {
        // TODO: Replace with real API call when API is ready
        // For now, use mock data
        val lockers = MockLockerDataSource.getMockLockers()
        emit(lockers)
        
        /* 
        // TODO: Uncomment this when API is ready
        val lockerDto = mockingApiInterface.getLockerList()
        // Convert LockerDto to Locker
        val lockers = listOf(
            Locker(
                id = lockerDto.lockerId,
                lockerName = lockerDto.name,
                description = lockerDto.description,
                position = lockerDto.position,
                createdBy = "system"
            )
        )
        emit(lockers)
        */
    } catch (e: Exception) {
        emit(emptyList())
    }
}
```

**Steps to Migrate**:
1. Comment out the mock data line: `val lockers = MockLockerDataSource.getMockLockers()`
2. Uncomment the API call code block
3. Update the API response handling if needed
4. Test the API integration

### 2. MockLockerDataSource.kt
**Location**: `features/Drone/src/main/java/com/delivery/setting/data/MockLockerDataSource.kt`

**Action**: This file can be **deleted** after API migration is complete.

### 3. API Interface
**Location**: `libraries/core/src/main/java/com/delivery/core/network/MockingApiInterface.kt`

**Current Endpoint**:
```kotlin
@GET("/at-locker/lockers")
suspend fun getLockerList(): LockerDto
```

**Note**: Verify this endpoint matches the actual API specification.

## Mock Data Locations
The current mock data includes 10 locations in Hanoi:
1. 380 Lạc Long Quân (Viettel Building)
2. Hồ Tây (West Lake)
3. Hồ Hoàn Kiếm (Lake of Restored Sword)
4. Văn Miếu – Quốc Tử Giám (Temple of Literature)
5. Lăng Bác (Ho Chi Minh Mausoleum)
6. Cầu Thăng Long (Thang Long Bridge)
7. Chùa Một Cột (One Pillar Pagoda)
8. Nhà hát Lớn Hà Nội (Hanoi Opera House)
9. Trúc Bạch (Trúc Bạch Lake area)
10. Cầu Long Biên (Long Bien Bridge)

## Testing Checklist
When migrating to API:
- [ ] Verify API endpoint is working
- [ ] Test data loading in SelectLocationLockerFragment
- [ ] Test map markers display correctly
- [ ] Test location selection functionality
- [ ] Test auto-selection of previously selected locations
- [ ] Test error handling when API fails
- [ ] Remove MockLockerDataSource.kt file
- [ ] Update this README or delete it

## API Response Format
Expected API response should match the `Locker` data class:
```kotlin
data class Locker(
    val id: String,
    val lockerName: String,
    val description: String,
    val position: List<Double>, // [longitude, latitude]
    val createdBy: String
)
```

## Error Handling
The current implementation handles API errors by emitting an empty list. Consider updating error handling to:
- Show user-friendly error messages
- Retry mechanism for network failures
- Offline data caching if needed


