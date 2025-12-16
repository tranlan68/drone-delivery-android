package com.delivery.core.model.network.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName

data class CommandDto(
	@SerializedName("command_id") val commandId: String,
	@SerializedName("command_type") val commandType: Int,
	@SerializedName("order_id") val orderId: String,
	@SerializedName("command_status") val commandStatus: Int
) : BaseResponse()



