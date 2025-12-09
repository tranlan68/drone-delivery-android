package com.delivery.core.network

import com.delivery.core.model.mocking.CommandCreateRequest
import com.delivery.core.model.mocking.CommandDto
import com.delivery.core.model.mocking.DroneDto
import com.delivery.core.model.mocking.LockerDto
import com.delivery.core.model.mocking.OrderCreateRequest
import com.delivery.core.model.mocking.OrderCreateResponse
import com.delivery.core.model.mocking.OrderDto
import com.delivery.core.model.mocking.OrderItemDto
import com.delivery.core.model.network.DroneCurrentPositionResponse
import com.delivery.core.model.network.FlightLaneDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {
    @GET("/user/login")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String,
    ): String

    @GET("/user/logout")
    suspend fun logoutUser(): Any

    @POST("/user")
    suspend fun createUser(
        @Body body: Map<String, Any?>,
    ): Any

    @GET("/at-drone/drones/{drone_id}")
    suspend fun getDroneById(
        @Path("drone_id") droneId: String,
    ): DroneDto

    @POST("/at-order/orders")
    suspend fun createOrder(
        @Body body: OrderCreateRequest,
    ): OrderCreateResponse

    @GET("/at-order/orders")
    suspend fun getOrderByUser(
        @Query("filter[user_create_id]") userId: String?,
    ): List<OrderItemDto>

    @GET("/at-order/orders/{order_id}")
    suspend fun getOrderById(
        @Path("order_id") orderId: String,
    ): OrderDto

    @GET("/at-order/orders")
    suspend fun getAllOrders(): List<OrderDto>

    @GET("/at-locker/hubs")
    suspend fun getLockerList(): List<LockerDto>

    @POST("/at-command/commands")
    suspend fun createCommand(
        @Body body: CommandCreateRequest,
    ): CommandDto

    @GET("at-flight-corridor/flight_lanes/{lane_id}")
    suspend fun getFlightLaneById(
        @Path("lane_id") laneId: String,
    ): FlightLaneDto

    @GET("/at-drone/mobile/drone/{drone_id}")
    suspend fun getDroneCurrentPosition(
        @Path("drone_id") droneId: String,
    ): DroneCurrentPositionResponse
}
