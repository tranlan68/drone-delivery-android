package com.delivery.core.model.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Order DTO based on actual API response
 * Represents the complete order structure from API
 */
data class OrderDto(
    @SerializedName("id") val id: String,
    @SerializedName("int_id") val int_id: String? = null,
    @SerializedName("source") val source: String? = null, // Direct source in order
    @SerializedName("dest") val dest: String? = null, // Direct dest in order
    @SerializedName("order_status") val orderStatus: Int? = null,
    @SerializedName("drone_id") val droneId: String? = null,
    @SerializedName("gcs_id") val gcsId: String? = null,
    @SerializedName("user_create_id") val userCreateId: String? = null,
    @SerializedName("weight") val weight: Double? = null,
    @SerializedName("size") val size: Int? = null,
    @SerializedName("priority") val priority: Int? = null,
    @SerializedName("corridor_id") val corridorId: String? = null,
    @SerializedName("eta") val eta: Double? = null,
    @SerializedName("updated_at") val updatedAt: Long? = null,
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("receiver_phone") val receiverPhone: String? = null,
    @SerializedName("segments") val segments: List<SegmentDto>? = null, // Can be null
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("updated_by") val updatedBy: String? = null,
    @SerializedName("time_window") val timeWindow: Any? = null, // Unknown structure
    @SerializedName("flight_notification_status") val flightNotificationStatus: String,
) : BaseResponse()

/**
 * Segment DTO representing flight segments in an order
 */
data class SegmentDto(
    @SerializedName("segment_index") val segmentIndex: Int? = null,
    @SerializedName("source") val source: String? = null, // API uses "source" not "src"
    @SerializedName("dest") val dest: String? = null,
    @SerializedName("flight_lane_id") val flightLaneId: String? = null,
    @SerializedName("drone_id") val droneId: String? = null,
    @SerializedName("segment_status") val segmentStatus: Int? = null, // API uses "segment_status"
    @SerializedName("segment_type") val segmentType: Int? = null, // API uses "segment_type"
) : Serializable
