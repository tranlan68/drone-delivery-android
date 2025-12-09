package com.delivery.core.network

import com.google.gson.annotations.SerializedName

data class ApiObjectResponse<T>(
    @SerializedName("code") var code: Int,
    @SerializedName("error") var error: Int,
    @SerializedName("msg") var msg: String,
    @SerializedName("data") var data: T,
)

data class ApiException(
    @SerializedName("error") var error: String,
    @SerializedName("msg") var msg: String,
    @SerializedName("data") var dataResponse: Any,
    @SerializedName("code") var code: Int,
    @SerializedName("message") var message: String,
    @SerializedName("errorCode") var errorCode: Int,
)

data class ApiBaseResponse<T>(
    @SerializedName("code") var code: Int,
    @SerializedName("message") var message: String,
    @SerializedName("msg") var msg: String,
    @SerializedName("data") var data: T,
) {
    fun isSuccess() = code == 200
}
