package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

data class FlightLaneDto(
    @SerializedName("id") val id: String,
    @SerializedName("supplier_id") val supplierId: String,
    @SerializedName("points") val points: List<List<Double>>,
    @SerializedName("name") val name: String,
    @SerializedName("width") val width: Double,
    @SerializedName("corridor_id") val corridorId: String,
    @SerializedName("created_by") val createdBy: String
)

