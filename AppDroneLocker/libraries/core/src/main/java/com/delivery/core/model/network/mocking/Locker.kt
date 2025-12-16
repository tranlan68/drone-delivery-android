package com.delivery.core.model.network.mocking

import com.google.gson.annotations.SerializedName

data class Locker(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("locker_name")
    val lockerName: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("position")
    val position: List<Double>, // [latitude, longitude]
    
    @SerializedName("created_by")
    val createdBy: String
) {
    val latitude: Double
        get() = position.getOrNull(0) ?: 0.0
    
    val longitude: Double
        get() = position.getOrNull(1) ?: 0.0
}


