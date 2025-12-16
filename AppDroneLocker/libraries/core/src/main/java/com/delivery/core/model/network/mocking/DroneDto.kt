package com.delivery.core.model.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName

data class DroneDto(
	@SerializedName("id") val id: String,
	@SerializedName("speed") val speed: Double?,
	@SerializedName("heading") val heading: Double?,
	@SerializedName("flight_route") val flightRoute: FlightRoutePoint,
	@SerializedName("remain_time") val remainTime: Int
) : BaseResponse()



