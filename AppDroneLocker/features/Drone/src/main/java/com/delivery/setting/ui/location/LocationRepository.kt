package com.delivery.setting.ui.location

import com.delivery.setting.model.SearchResult
import javax.inject.Inject
import javax.inject.Singleton

data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

@Singleton
class LocationRepository @Inject constructor() {

    suspend fun searchPlaces(query: String): List<SearchResult> {
        // TODO: Implement Google Places API search
        // For now, return mock data
        return getMockSearchResults(query)
    }

    suspend fun getCurrentLocation(): CurrentLocation? {
        // TODO: Implement current location detection
        // For now, return mock current location
        return CurrentLocation(
            latitude = 21.028511,
            longitude = 105.804817,
            address = "Hà Nội, Việt Nam"
        )
    }

    private fun getMockSearchResults(query: String): List<SearchResult> {
        return listOf(
            SearchResult(
                placeId = "landmark72",
                name = "Landmark 72",
                address = "Keangnam Hanoi Landmark Tower, Phạm Hùng, Mễ Trì, Nam Từ Liêm, Hà Nội",
                latitude = 21.028511,
                longitude = 105.804817
            ),
            SearchResult(
                placeId = "lotte_tower",
                name = "Lotte Tower",
                address = "54 Liễu Giai, Cống Vị, Ba Đình, Hà Nội",
                latitude = 21.035242,
                longitude = 105.816119
            ),
            SearchResult(
                placeId = "vincom_ba_trieu",
                name = "Vincom Center Bà Triệu",
                address = "191 Bà Triệu, Lê Đại Hành, Hai Bà Trưng, Hà Nội",
                latitude = 21.009817,
                longitude = 105.848066
            )
        ).filter { it.name.contains(query, ignoreCase = true) || it.address.contains(query, ignoreCase = true) }
    }
}
