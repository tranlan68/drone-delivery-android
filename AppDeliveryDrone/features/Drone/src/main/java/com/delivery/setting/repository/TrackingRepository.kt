package com.delivery.setting.repository

import com.delivery.core.base.BaseRepository
import com.delivery.core.model.network.DroneCurrentPositionResponse
import com.delivery.core.network.ApiInterface
import com.delivery.core.network.DirectionsApiService
import com.delivery.setting.model.DeliveryTrackingData
import com.delivery.setting.model.SampleDeliveryData
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

@Singleton
class TrackingRepository
    @Inject
    constructor(
        private val apiInterface: ApiInterface,
        private val directionsApiService: DirectionsApiService,
    ) : BaseRepository() {
        suspend fun getDeliveryTrackingData(orderId: String): DeliveryTrackingData {
            // For now, return sample data
            // In production, call API: apiInterface.getDeliveryTracking(orderId)
            return SampleDeliveryData.currentDelivery
        }

        suspend fun getDirectionsRoute(
            origin: LatLng,
            destination: LatLng,
        ): List<LatLng> {
            return try {
                val originStr = "${origin.latitude},${origin.longitude}"
                val destinationStr = "${destination.latitude},${destination.longitude}"

                val response =
                    directionsApiService.getDirections(
                        origin = originStr,
                        destination = destinationStr,
                        mode = "driving",
                        apiKey = "AIzaSyDP62SDUQKNS76xTqcfKjQYaY6WdnDkvXk", // Should be injected from BuildConfig or Constants
                    )

                // Parse response and extract route points
                parseDirectionsResponse(response)
            } catch (e: Exception) {
                // Fallback to straight line
                listOf(origin, destination)
            }
        }

        suspend fun cancelDelivery(orderId: String) {
            // Call API to cancel delivery
            // apiInterface.cancelDelivery(orderId)

            // For now, just simulate the call
            kotlinx.coroutines.delay(1000)
        }

        /**
         * Get current position of drone by drone ID
         * @param droneId The ID of the drone to track
         * @return DroneCurrentPositionResponse containing drone position data
         */
        suspend fun getDroneCurrentPosition(droneId: String): DroneCurrentPositionResponse {
            return try {
                apiInterface.getDroneCurrentPosition(droneId)
            } catch (e: Exception) {
                // Log error and rethrow for ViewModel to handle
                throw e
            }
        }

        /**
         * Convert DroneCurrentPositionResponse to MapLibre LatLng
         * @param response The API response containing drone position
         * @return MapLibre LatLng object for map display
         */
        fun convertToMapLibreLatLng(response: DroneCurrentPositionResponse): MapLibreLatLng {
            return MapLibreLatLng(
                response.position.latitude,
                response.position.longitude,
            )
        }

        private fun parseDirectionsResponse(response: Any): List<LatLng> {
            // TODO: Implement actual parsing of Google Directions API response
            // This would extract the polyline points from the response

            // For now, return empty list (will fallback to straight line)
            return emptyList()
        }
    }
