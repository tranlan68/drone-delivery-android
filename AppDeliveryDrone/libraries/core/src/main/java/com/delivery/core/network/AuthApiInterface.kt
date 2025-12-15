package com.delivery.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface AuthApiInterface {
    @GET("/user/login")
    suspend fun loginUser(
        @Query("username") username: String,
        @Query("password") password: String,
    ): String

    @GET("/user/logout")
    suspend fun logoutUser(): Any
}
