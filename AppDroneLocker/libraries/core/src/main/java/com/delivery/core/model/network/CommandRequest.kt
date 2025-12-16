package com.delivery.core.model.network

import com.google.gson.annotations.SerializedName

data class CommandRequest(
    @SerializedName("order_id")
    val orderId: String,
    
    @SerializedName("updated_by")
    val updatedBy: String = "mobile_locker",
    
    @SerializedName("command_type")
    val commandType: Int,
    
    @SerializedName("drone_id")
    val droneId: String = "1004",
    
    @SerializedName("gcs_id")
    val gcsId: String = "gcs01",
    
    @SerializedName("source")
    val source: String,
    
    @SerializedName("updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @SerializedName("is_request")
    val isRequest: Boolean = true,
    
    @SerializedName("segment_index")
    val segmentIndex: Int,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("command_status")
    val commandStatus: Int = 1,
    
    @SerializedName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerializedName("created_by")
    val createdBy: String = "mobile_locker"
) {
    constructor(
        orderId: String,
        commandType: Int,
        segmentIndex: Int,
        source: String,
        content: String,
        droneId: String = "1004",
        gcsId: String = "gcs01",
        updatedAt: Long = System.currentTimeMillis(),
        createdAt: Long = System.currentTimeMillis()
    ) : this(
        orderId = orderId,
        updatedBy = "mobile_locker",
        commandType = commandType,
        droneId = droneId,
        gcsId = gcsId,
        source = source,
        updatedAt = updatedAt,
        isRequest = true,
        segmentIndex = segmentIndex,
        content = content,
        commandStatus = 1,
        createdAt = createdAt,
        createdBy = "mobile_locker"
    )
}
