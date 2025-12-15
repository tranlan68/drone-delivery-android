package com.delivery.setting.model

import com.delivery.core.model.mocking.SegmentDto
import java.io.Serializable

data class OrderHistoryItem(
    val id: String,
    val intId: String?,
    val sourceLocker: String, // Tên locker điểm lấy hàng
    val destLocker: String, // Tên locker điểm giao hàng
    val idSource: String? = null, // ID locker điểm lấy hàng
    val idDest: String? = null, // ID locker điểm giao hàng
    val packageType: String,
    val packageWeight: String,
    val orderDate: String,
    var status: OrderStatus,
    val price: String? = null,
    val orderTime: String? = null,
    val droneId: String? = null,
    val receiverPhone: String? = null,
    val eta: Long? = null, // Estimated time of arrival
    val segments: List<SegmentDto>? = null, // Danh sách segments của order
    val priority: Int? = null,
    val deliveryDate: String,
    val deliveryTime: String,
    val createdAt: Long,
) : Serializable

enum class OrderStatus(val displayName: String) {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Chờ giao"),
    IN_DELIVERY("Đang giao"),
    DELIVERED("Đã giao"),
    CANCEL("Đã hủy"),
}

enum class OrderTab(val tabName: String) {
    CURRENT("Đang thực hiện"),
    HISTORY("Đã hoàn thành"),
}
