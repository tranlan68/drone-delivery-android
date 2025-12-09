package com.delivery.core.network

import com.delivery.core.model.mocking.LockerDto
import com.delivery.core.model.network.CommandRequest
import com.delivery.core.model.network.CommandResponse
import com.delivery.core.model.network.FlightLaneDto
import com.delivery.core.model.network.FlightLaneDetailDto
import com.delivery.core.model.network.DroneInfoDto
import com.delivery.core.model.network.mocking.OrderDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @GET("/at-locker/hubs")
    suspend fun getLockerList(): List<LockerDto>

    @GET("/at-order/orders")
    suspend fun getOrdersList(): List<OrderDto>

    @GET("/at-flight-lane/flight-lanes/{lane_id}")
    suspend fun getFlightLaneById(@Path("lane_id") laneId: String): List<FlightLaneDto>

    @POST("/at-command/commands")
    suspend fun sendCommand(@Body commandRequest: CommandRequest): CommandResponse

    @GET("/at-flight-corridor/flight_lanes/{lane_id}")
    suspend fun getFlightLaneDetail(@Path("lane_id") laneId: String): FlightLaneDetailDto

    @GET("/at-drone/drones/{drone_id}")
    suspend fun getDroneInfo(@Path("drone_id") droneId: String): DroneInfoDto

}
