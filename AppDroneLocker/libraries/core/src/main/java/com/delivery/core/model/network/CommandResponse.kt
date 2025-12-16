package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

data class CommandResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("segment_index")
    val segmentIndex: Int,
    
    @SerializedName("created_at")
    val createdAt: Long,
    
    @SerializedName("command_status")
    val commandStatus: Int,
    
    @SerializedName("created_by")
    val createdBy: String,
    
    @SerializedName("order_id")
    val orderId: String,
    
    @SerializedName("source")
    val source: String,
    
    @SerializedName("is_request")
    val isRequest: Boolean,
    
    @SerializedName("drone_id")
    val droneId: String,
    
    @SerializedName("updated_at")
    val updatedAt: Long,
    
    @SerializedName("gcs_id")
    val gcsId: String,
    
    @SerializedName("command_type")
    val commandType: Int,
    
    @SerializedName("content")
    val content: String
)
