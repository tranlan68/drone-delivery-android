package com.delivery.setting.model.mapper

import com.delivery.core.model.network.mocking.OrderDto
import com.delivery.core.model.network.mocking.SegmentDto
import com.delivery.core.repositopry.LockerRepository
import com.delivery.setting.model.Order
import com.delivery.setting.model.OrderSegment
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderMapper @Inject constructor(
    private val lockerRepository: LockerRepository
) {

    /**
     * Chuyển đổi từ OrderDto sang Order model
     */
    suspend fun mapToOrder(orderDto: OrderDto): Order {
        Timber.d("Mapping OrderDto to Order: ${orderDto.id}")

        // Determine source and destination - prioritize direct fields, fallback to segments
        val sourceId = when {
            !orderDto.source.isNullOrEmpty() && orderDto.source != "string" -> orderDto.source
            !orderDto.segments.isNullOrEmpty() -> orderDto.segments?.firstOrNull()?.source
            else -> ""
        } ?: ""

        val destId = when {
            !orderDto.dest.isNullOrEmpty() && orderDto.dest != "string" -> orderDto.dest
            !orderDto.segments.isNullOrEmpty() -> orderDto.segments?.lastOrNull()?.dest
                ?: orderDto.segments?.firstOrNull()?.dest
            else -> ""
        } ?: ""

        // Get locker names by IDs
        val sourceName = if (sourceId.isNotEmpty()) {
            lockerRepository.getLockerNameById(sourceId)
        } else "N/A"

        val destName = if (destId.isNotEmpty()) {
            lockerRepository.getLockerNameById(destId)
        } else "N/A"

        // Map segments
        val segments = orderDto.segments?.map { segmentDto ->
            mapSegmentDtoToOrderSegment(segmentDto)
        } ?: emptyList()

        return Order(
            id = orderDto.id,
            userCreateId = orderDto.userCreateId ?: "",
            weight = orderDto.weight ?: 0.0,
            size = orderDto.size ?: 1,
            updatedAt = orderDto.updatedAt ?: 0L,
            createdAt = orderDto.createdAt ?: 0L,
            receiverPhone = orderDto.receiverPhone ?: "",
            segments = segments,
            orderStatus = orderDto.orderStatus ?: 1,
            priority = orderDto.priority ?: 0,
            corridorId = orderDto.corridorId ?: "",
            eta = orderDto.eta?.toLong() ?: 0L,
            source = sourceId,
            dest = destId,
            sourceName = sourceName,
            destName = destName
        )
    }

    /**
     * Chuyển đổi từ SegmentDto sang OrderSegment
     */
    private fun mapSegmentDtoToOrderSegment(segmentDto: SegmentDto): OrderSegment {
        return OrderSegment(
            segmentIndex = segmentDto.segmentIndex ?: 0,
            source = segmentDto.source ?: "",
            dest = segmentDto.dest ?: "",
            flightLaneId = segmentDto.flightLaneId ?: "",
            droneId = segmentDto.droneId ?: "",
            type = segmentDto.segmentType ?: 0,
            status = segmentDto.segmentStatus ?: 0
        )
    }



    /**
     * Chuyển đổi size number sang text
     */
    private fun getSizeText(size: Int): String {
        return when (size) {
            0 -> "XS"
            1 -> "S"
            2 -> "M"
            3 -> "L"
            4 -> "XL"
            else -> "M"
        }
    }

    /**
     * Format timestamp thành date string
     */
    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp) // timestamp already in milliseconds
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("vi", "VN"))
        return formatter.format(date)
    }

    /**
     * Format timestamp thành time string
     */
    private fun formatTime(timestamp: Long): String {
        val date = Date(timestamp) // timestamp already in milliseconds
        val formatter = SimpleDateFormat("HH:mm", Locale("vi", "VN"))
        return formatter.format(date)
    }
}