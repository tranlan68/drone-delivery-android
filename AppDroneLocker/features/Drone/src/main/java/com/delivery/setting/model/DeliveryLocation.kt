package com.delivery.setting.model

data class DeliveryLocation(
    val id: String,
    val name: String? = null,
    val address: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val lastDeliveryTime: String? = null
)
