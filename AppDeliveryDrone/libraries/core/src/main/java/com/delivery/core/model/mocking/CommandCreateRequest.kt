package com.delivery.core.model.mocking

import com.google.gson.annotations.SerializedName

data class CommandCreateRequest(
    @SerializedName("command_type") val commandType: Int?,
    @SerializedName("order_id") val orderId: String,
)
