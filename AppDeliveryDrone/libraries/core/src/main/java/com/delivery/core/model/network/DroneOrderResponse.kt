package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

data class DroneOrderResponse(
    @SerializedName("source") val source: String,
    @SerializedName("dest") val dest: String,
    @SerializedName("user_create_id") val userCreateId: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("size") val size: Int,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("drone_id") val droneId: String,
    @SerializedName("receiver_phone") val receiverPhone: String,
    @SerializedName("id") val id: String,
    @SerializedName("int_id") val intOrderId: String,
    @SerializedName("order_status") val orderStatus: Int,
    @SerializedName("flight_route") val flightRoute: List<FlightRoutePoint>,
    @SerializedName("priority") val priority: Int,
    @SerializedName("corridor_id") val corridorId: String,
    @SerializedName("start_lane_id") val startLaneId: String,
    @SerializedName("end_lane_id") val endLaneId: String,
    @SerializedName("eta") val eta: Long,
    @SerializedName("flight_notification_status") val flightNotificationStatus: String,
)

data class FlightRoutePoint(
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("altitude") val altitude: Double,
)
