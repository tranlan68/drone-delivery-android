package com.delivery.core.model.network

data class FlightLaneResponse(
    val id: String,
    val supplierId: String,
    val points: List<List<Double>>,
    val name: String,
    val width: Double,
    val corridorId: String,
    val createdBy: String
)

