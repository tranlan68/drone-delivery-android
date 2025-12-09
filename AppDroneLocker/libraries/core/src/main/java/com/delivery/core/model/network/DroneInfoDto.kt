package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DroneInfoDto(
    @SerializedName("id") val id: String,
    @SerializedName("rtsp_link") val rtspLink: String,
    @SerializedName("gcs_id") val gcsId: String,
    @SerializedName("deport_id") val deportId: String,
    @SerializedName("mass") val mass: Int,
    @SerializedName("max_payload") val maxPayload: Int,
    @SerializedName("max_battery") val maxBattery: Int,
    @SerializedName("max_power") val maxPower: Int,
    @SerializedName("max_speed") val maxSpeed: Int,
    @SerializedName("size") val size: List<Int>,
    @SerializedName("fixed_locker_ids") val fixedLockerIds: List<String>,
    @SerializedName("validate_status") val validateStatus: Int,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("created_by") val createdBy: String,
    @SerializedName("updated_by") val updatedBy: String
) : Serializable
