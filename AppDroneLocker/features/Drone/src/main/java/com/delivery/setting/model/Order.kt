package com.delivery.setting.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OrderDisplayStyle {
    SEND,     // Bên gửi gửi đi
    SENT,     // Bên gửi đã gửi
    WAITING,  // Bên nhận đang đợi để dỡ hàng
    UNLOAD,   // Bên nhận tháo hàng
    DONE      // Sau khi tháo hàng xong
}

val OrderDisplayStyle.buttonTitle: String
    get() = when (this) {
        OrderDisplayStyle.SEND -> "Bắt đầu" //bấm dc, màu cam
        OrderDisplayStyle.SENT -> "Đang giao" // không bấm dc, màu cam
        OrderDisplayStyle.WAITING -> "Đang đợi giao" // làm mờ, không bấm dc, màu xanh
        OrderDisplayStyle.UNLOAD -> "Kết thúc" // bấm dc, màu xanh
        OrderDisplayStyle.DONE -> "Đã xong" // Không hiển thị nút
    }

val OrderDisplayStyle.displayText: String
    get() = when (this) {
        OrderDisplayStyle.SEND -> "Chuẩn bị gửi"
        OrderDisplayStyle.SENT -> "Chuẩn bị gửi"
        OrderDisplayStyle.WAITING -> "Đang giao"
        OrderDisplayStyle.UNLOAD -> "Đang giao"
        OrderDisplayStyle.DONE -> "Đã đến nơi"
    }

enum class OrderSegmentStatus(val value: Int) {
    NONE(0),
    IN_PROGRESS(1),
    COMPLETED(2);

    companion object {
        fun fromValue(value: Int): OrderSegmentStatus {
            return values().find { it.value == value } ?: NONE
        }
    }
}

data class Order(
    @SerializedName("id") val id: String,
    @SerializedName("user_create_id") val userCreateId: String,
    @SerializedName("weight") val weight: Double,
    @SerializedName("size") val size: Int,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("receiver_phone") val receiverPhone: String,
    @SerializedName("segments") val segments: List<OrderSegment>,
    @SerializedName("order_status") val orderStatus: Int,
    @SerializedName("priority") val priority: Int,
    @SerializedName("corridor_id") val corridorId: String,
    @SerializedName("eta") val eta: Long,
    @SerializedName("source") val source: String,
    @SerializedName("dest") val dest: String,
    val sourceName: String = "",
    val destName: String = ""
): Serializable {
    fun getOrderStatus(): OrderDeliveryStatus = OrderDeliveryStatus.fromValue(orderStatus)
    
    fun getSizeText(): String {
        return when (size) {
            1 -> "S"
            2 -> "M"
            3 -> "L"
            4 -> "XL"
            else -> "S"
        }
    }
    
    fun getFormattedDate(): String {
        val date = Date(createdAt) // timestamp already in milliseconds
        val formatter = SimpleDateFormat("HH:mm dd MMM yyyy", Locale("vi", "VN"))
        return formatter.format(date)
    }

    fun getDisplayStyle(currentLockerId: String): OrderDisplayStyle {
        val orderStatus = getOrderStatus()
        
        return when (currentLockerId) {
            source -> {
                // Bên gửi
                when (orderStatus) {
                    OrderDeliveryStatus.PENDING -> OrderDisplayStyle.SEND
                    OrderDeliveryStatus.IN_DELIVERY -> {
                        if (segments.firstOrNull()?.getSegmentStatus() == OrderSegmentStatus.IN_PROGRESS) {
                            OrderDisplayStyle.SENT
                        } else {
                            OrderDisplayStyle.DONE
                        }
                    }
                    OrderDeliveryStatus.DELIVERED -> OrderDisplayStyle.DONE
                }
            }
            dest -> {
                // Bên nhận
                when (orderStatus) {
                    OrderDeliveryStatus.PENDING -> OrderDisplayStyle.WAITING
                    OrderDeliveryStatus.IN_DELIVERY -> {
                        if (segments.lastOrNull()?.getSegmentStatus() == OrderSegmentStatus.IN_PROGRESS) {
                            OrderDisplayStyle.UNLOAD
                        } else {
                            OrderDisplayStyle.WAITING
                        }
                    }
                    OrderDeliveryStatus.DELIVERED -> OrderDisplayStyle.DONE
                }
            }
            else -> {
                // Locker trung gian
                val previousSegment = segments.firstOrNull { it.dest == currentLockerId }
                val nextSegment = segments.firstOrNull { it.source == currentLockerId }
                
                if (previousSegment == null || nextSegment == null) {
                    return OrderDisplayStyle.DONE
                }
                
                when (previousSegment.getSegmentStatus() to nextSegment.getSegmentStatus()) {
                    OrderSegmentStatus.NONE to OrderSegmentStatus.NONE -> OrderDisplayStyle.WAITING
                    OrderSegmentStatus.IN_PROGRESS to OrderSegmentStatus.NONE -> OrderDisplayStyle.UNLOAD
                    OrderSegmentStatus.COMPLETED to OrderSegmentStatus.NONE -> OrderDisplayStyle.SEND
                    OrderSegmentStatus.COMPLETED to OrderSegmentStatus.IN_PROGRESS -> OrderDisplayStyle.SENT
                    OrderSegmentStatus.COMPLETED to OrderSegmentStatus.COMPLETED -> OrderDisplayStyle.DONE
                    else -> OrderDisplayStyle.DONE
                }
            }
        }
    }
}

data class OrderSegment(
    @SerializedName("segment_index") val segmentIndex: Int,
    @SerializedName("source") val source: String,
    @SerializedName("dest") val dest: String,
    @SerializedName("flight_lane_id") val flightLaneId: String,
    @SerializedName("drone_id") val droneId: String,
    @SerializedName("type") val type: Int,
    @SerializedName("status") val status: Int
): Serializable {
    fun getSegmentStatus(): OrderSegmentStatus = OrderSegmentStatus.fromValue(status)
}
