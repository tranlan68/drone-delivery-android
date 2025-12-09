package com.delivery.core.model.mocking

import android.R
import com.google.gson.annotations.SerializedName

data class OrderCreateRequest(
    @SerializedName("is_demo") var isDemo: Boolean?,
    @SerializedName("source") val source: String?,
    @SerializedName("dest") val dest: String?,
    @SerializedName("user_create_id") val userCreateId: String?,
    @SerializedName("weight") val weight: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("priority") val priority: Int,
// 	@SerializedName("receiver_phone") val receiverPhone: String?
)
