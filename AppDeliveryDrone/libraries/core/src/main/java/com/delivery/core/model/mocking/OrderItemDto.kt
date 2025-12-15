package com.delivery.core.model.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName

data class OrderItemDto(
    @SerializedName("id") val orderId: String,
    @SerializedName("int_id") val intOrderId: String,
    @SerializedName("source") val source: String?,
    @SerializedName("dest") val dest: String?,
    @SerializedName("order_status") val status: Int?,
    @SerializedName("drone_id") val droneId: String?,
    @SerializedName("flight_route") val flightRoute: List<FlightRoutePoint>?,
    @SerializedName("user_create_id") val userCreateId: String?,
    @SerializedName("weight") val weight: Double,
    @SerializedName("size") val size: Int,
    @SerializedName("priority") val priority: Int?,
) : BaseResponse()
