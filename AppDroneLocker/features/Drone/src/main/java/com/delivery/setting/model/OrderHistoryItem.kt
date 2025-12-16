package com.delivery.setting.model

data class OrderHistoryItem(
    val id: String,
    val fromLocation: String,
    val fromAddress: String,
    val toLocation: String,
    val toAddress: String,
    val packageType: String,
    val packageWeight: String,
    val orderDate: String,
    val status: OrderStatus,
    val price: String? = null,
    val orderTime: String? = null
)

enum class OrderStatus(val displayName: String) {
    PENDING("Đang giao"),
    IN_PROGRESS("Đang xử lý"),
    DELIVERED("Đã hoàn tất"),
    CANCELLED("Đã hủy"),
    CONFIRMED("Đã xác nhận")
}

enum class OrderTab(val tabName: String) {
    CURRENT("Hiện tại"),
    HISTORY("Lịch sử")
}
