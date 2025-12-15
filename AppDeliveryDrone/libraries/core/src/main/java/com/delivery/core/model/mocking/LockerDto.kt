package com.delivery.core.model.mocking

import com.delivery.core.model.network.BaseResponse
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("created_by") val createdBy: String? = null,
)

data class LockerDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("position") val position: List<Double>,
    @SerializedName("users") val users: List<UserDto>? = emptyList(),
    @SerializedName("lockers") val lockers: List<LockerDto>? = emptyList(),
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("updated_at") val updatedAt: Long? = null,
    @SerializedName("created_by") val createdBy: String? = null,
    @SerializedName("updated_by") val updatedBy: String? = null,
) : BaseResponse()
