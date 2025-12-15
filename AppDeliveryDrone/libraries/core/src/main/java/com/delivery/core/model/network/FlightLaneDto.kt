package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

data class FlightLaneDto(
    @SerializedName("id") val id: String,
    @SerializedName("offset") val offset: Int,
    @SerializedName("position") val position: List<FlightPosition>,
    @SerializedName("corridor_id") val corridorId: String,
    @SerializedName("start_locker_id") val startLockerId: String,
    @SerializedName("end_locker_id") val endLockerId: String,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("created_by") val createdBy: String,
)

data class FlightPosition(
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("altitude") val altitude: Double,
    @SerializedName("polar_velocity") val polarVelocity: PolarVelocity,
    @SerializedName("eta") val eta: Double,
)

data class PolarVelocity(
    @SerializedName("speed") val speed: Double? = null,
    @SerializedName("heading") val heading: Double? = null,
)
