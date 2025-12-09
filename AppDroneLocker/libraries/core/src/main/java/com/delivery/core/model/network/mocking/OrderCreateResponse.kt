package com.delivery.core.model.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName

data class OrderCreateResponse(
	@SerializedName("order_id") val orderId: String,
	@SerializedName("source") val source: String?,
	@SerializedName("dest") val dest: String?,
	@SerializedName("status") val status: String?,
	@SerializedName("drone_id") val droneId: String?,
	@SerializedName("flight_route") val flightRoute: List<FlightRoutePoint>?,
	@SerializedName("user_create_id") val userCreateId: String?,
	@SerializedName("weight") val weight: Double,
	@SerializedName("size") val size: String
) : BaseResponse()



