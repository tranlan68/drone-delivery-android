package com.delivery.setting.model

enum class OrderDeliveryStatus(val value: Int) {
    PENDING(1),
    IN_DELIVERY(2),
    DELIVERED(3);
    companion object {
        fun fromValue(value: Int): OrderDeliveryStatus {
            return values().find { it.value == value } ?: PENDING
        }
    }
}
