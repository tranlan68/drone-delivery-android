package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

/**
 * Response model for drone current position API
 * API: GET /at-drone/mobile/drone/{droneId}
 */
data class DroneCurrentPositionResponse(
    @SerializedName("drone_id") val droneId: String,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("position") val position: DronePosition,
)

data class DronePosition(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("altitude") val altitude: Double? = null,
    @SerializedName("heading") val heading: Double? = null,
    @SerializedName("speed") val speed: Double? = null,
)
