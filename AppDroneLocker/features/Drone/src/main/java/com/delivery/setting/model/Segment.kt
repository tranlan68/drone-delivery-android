package com.delivery.setting.model

import com.delivery.setting.R
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SegmentDisplayStyle {
    SEND,     // Bên gửi gửi đi
    SENT,     // Bên gửi đã gửi
    WAITING,  // Bên nhận đang đợi để dỡ hàng
    UNLOAD,   // Bên nhận tháo hàng
    DONE      // Sau khi tháo hàng xong
}
enum class SegmentCommandAction {
    START,
    END
}

val SegmentDisplayStyle.buttonTitle: String
    get() = when (this) {
        SegmentDisplayStyle.SEND -> "Bắt đầu" //bấm dc, màu cam
        SegmentDisplayStyle.SENT -> "Đang giao" // không bấm dc, màu cam
        SegmentDisplayStyle.WAITING -> "Đang đợi giao" // làm mờ, không bấm dc, màu xanh
        SegmentDisplayStyle.UNLOAD -> "Kết thúc" // bấm dc, màu xanh
        SegmentDisplayStyle.DONE -> "Đã xong" // Không hiển thị nút
    }

val SegmentDisplayStyle.displayText: String
    get() = when (this) {
        SegmentDisplayStyle.SEND -> "Chuẩn bị gửi"
        SegmentDisplayStyle.SENT -> "Chuẩn bị gửi"
        SegmentDisplayStyle.WAITING -> "Đang giao"
        SegmentDisplayStyle.UNLOAD -> "Đang giao"
        SegmentDisplayStyle.DONE -> "Đã đến nơi"
    }


enum class SegmentStatus(val value: Int) {
    NONE(0),
    IN_PROGRESS(1),
    COMPLETED(2);

    companion object {
        fun fromValue(value: Int): SegmentStatus {
            return values().find { it.value == value } ?: NONE
        }
    }
}

data class Segment(
    @SerializedName("segment_index") val segmentIndex: Int,
    @SerializedName("source") val source: String,
    @SerializedName("dest") val dest: String,
    @SerializedName("flight_lane_id") val flightLaneId: String,
    @SerializedName("drone_id") val droneId: String,
    @SerializedName("status") val status: Int,
    @SerializedName("order_id") val orderId: String = "",
    @SerializedName("created_at") val createdAt: Long = 0L,
    val sourceName: String = "",
    val destName: String = "",
    val estimatedStartTime: Long = 0L,
    val estimatedEndTime: Long = 0L,
    val gcsId: String = "",
    val lockerId: String? = "",
    val idDestOrder: String? = null,
    val idSourceOrder: String? = null,
): Serializable {
    
    fun getSegmentStatus(): SegmentStatus = SegmentStatus.fromValue(status)

    fun getStatusText(): String {
        return when (getSegmentStatus()) {
            SegmentStatus.NONE -> "Chưa bắt đầu"
            SegmentStatus.IN_PROGRESS -> "Đang tiến hành"
            SegmentStatus.COMPLETED -> "Đã hoàn thành"
        }
    }
    fun getColorHex(): Int {
        return when (getSegmentStatus()) {
            SegmentStatus.NONE ->  R.color.blue
            SegmentStatus.IN_PROGRESS ->  R.color.green
            SegmentStatus.COMPLETED -> R.color.gray
        }
    }

    fun isShowStartButton(): Boolean {
        val segmentStatus = getSegmentStatus()
        val isOrderSender = lockerId == idSourceOrder
        val isOrderReceiver = lockerId == idDestOrder
        val isSegmentSource = lockerId == source
        val isSegmentDest = lockerId == dest
        return isOrderSender && isSegmentSource && segmentStatus == SegmentStatus.NONE
    }

    fun isShowEndButton(): Boolean {
        val segmentStatus = getSegmentStatus()
        val isOrderSender = lockerId == idSourceOrder
        val isOrderReceiver = lockerId == idDestOrder
        val isSegmentSource = lockerId == source
        val isSegmentDest = lockerId == dest
        return  isOrderReceiver &&isSegmentSource && segmentStatus == SegmentStatus.IN_PROGRESS

    }

    fun getDisplayStyle(): SegmentDisplayStyle {
        val segmentStatus = getSegmentStatus()
        val isOrderSender = lockerId == idSourceOrder
        val isOrderReceiver = lockerId == idDestOrder
        val isSegmentSource = lockerId == source
        val isSegmentDest = lockerId == dest
        return when {
            isOrderSender && isSegmentSource -> {
                when (segmentStatus) {
                    SegmentStatus.NONE -> SegmentDisplayStyle.SEND
                    SegmentStatus.IN_PROGRESS -> SegmentDisplayStyle.SENT
                    SegmentStatus.COMPLETED -> SegmentDisplayStyle.DONE
                }
            }
            isOrderSender && isSegmentDest -> {
                when (segmentStatus) {
                    SegmentStatus.NONE -> SegmentDisplayStyle.WAITING
                    SegmentStatus.IN_PROGRESS -> SegmentDisplayStyle.UNLOAD
                    SegmentStatus.COMPLETED -> SegmentDisplayStyle.DONE
                }
            }
            isOrderReceiver && isSegmentDest -> {
                when (segmentStatus) {
                    SegmentStatus.NONE -> SegmentDisplayStyle.SEND
                    SegmentStatus.IN_PROGRESS -> SegmentDisplayStyle.SENT
                    SegmentStatus.COMPLETED -> SegmentDisplayStyle.DONE
                }
            }
            isOrderReceiver && isSegmentSource -> {
                when (segmentStatus) {
                    SegmentStatus.NONE -> SegmentDisplayStyle.WAITING
                    SegmentStatus.IN_PROGRESS -> SegmentDisplayStyle.UNLOAD
                    SegmentStatus.COMPLETED -> SegmentDisplayStyle.DONE
                }
            }
            else -> {
                SegmentDisplayStyle.DONE
            }
        }
    }
    
    fun getFormattedStartTime(): String {
        return if (estimatedStartTime > 0) {
            formatTime(estimatedStartTime)
        } else ""
    }
    
    fun getFormattedEndTime(): String {
        return if (estimatedEndTime > 0) {
            formatTime(estimatedEndTime)
        } else ""
    }
    
    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("HH:mm dd MMM yyyy", Locale("vi", "VN"))
        return formatter.format(date)
    }
    
    fun getDroneDisplayName(): String {
        return "Máy bay: $droneId"
    }
}
