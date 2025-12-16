package com.delivery.setting.mapper

import com.delivery.core.model.mocking.OrderDto
import com.delivery.core.model.mocking.OrderItemDto
import com.delivery.core.model.network.DroneOrderResponse
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.repository.LockerRepository
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderMapper
    @Inject
    constructor(
        private val lockerRepository: LockerRepository,
    ) {
        /**
         * Chuyển đổi từ OrderDto (API mới theo actual response) sang OrderHistoryItem
         */
        suspend fun mapToOrderHistoryItem(orderDto: OrderDto): OrderHistoryItem {
            Timber.d("Mapping order ${orderDto.id}: source=${orderDto.source}, dest=${orderDto.dest}, segments=${orderDto.segments?.size}")

            // Determine source and destination - prioritize direct fields, fallback to segments
            val sourceId =
                when {
                    !orderDto.source.isNullOrEmpty() && orderDto.source != "string" -> orderDto.source
                    !orderDto.segments.isNullOrEmpty() -> orderDto.segments?.firstOrNull()?.source
                    else -> ""
                } ?: ""

            val destId =
                when {
                    !orderDto.dest.isNullOrEmpty() && orderDto.dest != "string" -> orderDto.dest
                    !orderDto.segments.isNullOrEmpty() ->
                        orderDto.segments?.lastOrNull()?.dest
                            ?: orderDto.segments?.firstOrNull()?.dest

                    else -> ""
                } ?: ""

            // Get locker names by IDs
            val sourceName =
                if (sourceId.isNotEmpty()) {
                    lockerRepository.getLockerNameById(sourceId)
                } else {
                    "N/A"
                }

            val destName =
                if (destId.isNotEmpty()) {
                    lockerRepository.getLockerNameById(destId)
                } else {
                    "N/A"
                }

            // Get drone ID - prioritize direct field, fallback to first segment
            val droneId =
                when {
                    !orderDto.droneId.isNullOrEmpty() && orderDto.droneId != "string" -> orderDto.droneId
                    !orderDto.segments.isNullOrEmpty() -> orderDto.segments?.firstOrNull()?.droneId
                    else -> null
                }

            // Safe defaults for required fields
            val weight = orderDto.weight ?: 0.0
            val size = orderDto.size ?: 1
            val createdAt = orderDto.createdAt ?: System.currentTimeMillis()
            val orderStatus = orderDto.orderStatus ?: 0
            val deliveryAt = (orderDto.createdAt ?: System.currentTimeMillis()) + (orderDto.eta?.toLong() ?: 0) * 1000

            // Convert to OrderHistoryItem
            return OrderHistoryItem(
                id = orderDto.id,
                intId = orderDto.int_id,
                sourceLocker = sourceName,
                destLocker = destName,
                idSource = sourceId.takeIf { it.isNotEmpty() },
                idDest = destId.takeIf { it.isNotEmpty() },
                packageType = "Loại hàng",
                packageWeight = "${getSizeText(size)} • $weight kg",
                orderDate = formatDate(createdAt),
                status = mapOrderStatus(orderStatus, orderDto.flightNotificationStatus),
                orderTime = formatTime(createdAt),
                droneId = droneId,
                receiverPhone = orderDto.receiverPhone,
                eta = orderDto.eta?.toLong(),
                segments = orderDto.segments,
                priority = orderDto.priority,
                deliveryDate = formatDate(deliveryAt),
                deliveryTime = formatTime(deliveryAt),
                createdAt = createdAt,
                flightNotificationStatus = orderDto.flightNotificationStatus,
            )
        }

        /**
         * Chuyển đổi từ DroneOrderResponse (API cũ) sang OrderHistoryItem
         */
        fun mapToOrderHistoryItem(apiResponse: DroneOrderResponse): OrderHistoryItem {
            return OrderHistoryItem(
                id = apiResponse.id,
                intId = apiResponse.intOrderId,
                sourceLocker = apiResponse.source, // Tên locker điểm lấy hàng
                destLocker = apiResponse.dest, // Tên locker điểm giao hàng
                idSource = null, // DroneOrderResponse không có ID riêng
                idDest = null, // DroneOrderResponse không có ID riêng
                packageType = "Loại hàng",
                packageWeight = "${getSizeText(apiResponse.size)} • ${apiResponse.weight} kg",
                orderDate = formatDate(apiResponse.createdAt),
                status = mapOrderStatus(apiResponse.orderStatus, apiResponse.flightNotificationStatus),
                orderTime = formatTime(apiResponse.createdAt),
                droneId = apiResponse.droneId,
                receiverPhone = apiResponse.receiverPhone,
                eta = apiResponse.eta,
                segments = null, // DroneOrderResponse không có segments
                deliveryDate = formatDate(apiResponse.createdAt),
                deliveryTime = formatTime(apiResponse.createdAt),
                createdAt = apiResponse.createdAt,
                flightNotificationStatus = apiResponse.flightNotificationStatus,
            )
        }

        /**
         * Chuyển đổi từ API response cũ sang OrderHistoryItem (backward compatibility)
         */
        suspend fun mapToOrderHistoryItem(apiResponse: OrderItemDto): OrderHistoryItem {
            val sourceName =
                if (!apiResponse.source.isNullOrEmpty()) {
                    lockerRepository.getLockerNameById(apiResponse.source ?: "")
                } else {
                    "N/A"
                }

            val destName =
                if (!apiResponse.dest.isNullOrEmpty()) {
                    lockerRepository.getLockerNameById(apiResponse.dest ?: "")
                } else {
                    "N/A"
                }

            return OrderHistoryItem(
                id = apiResponse.orderId,
                intId = apiResponse.intOrderId,
                sourceLocker = sourceName,
                destLocker = destName,
                idSource = apiResponse.source?.takeIf { it.isNotEmpty() },
                idDest = apiResponse.dest?.takeIf { it.isNotEmpty() },
                packageType = "Loại hàng",
                packageWeight = "${getSizeText(apiResponse.size)} • ${apiResponse.weight} kg",
                orderDate = formatDate(System.currentTimeMillis()), // API không có timestamp, dùng current time tạm
                status = mapOrderStatus(apiResponse.status ?: 0, apiResponse.flightNotificationStatus),
                orderTime = formatTime(System.currentTimeMillis()), // API không có timestamp, dùng current time tạm
                droneId = apiResponse.droneId,
                receiverPhone = null,
                segments = null, // OrderItemDto không có segments
                deliveryDate = formatDate(System.currentTimeMillis()),
                deliveryTime = formatTime(System.currentTimeMillis()),
                createdAt = System.currentTimeMillis(),
                flightNotificationStatus = apiResponse.flightNotificationStatus,
            )
        }

        /**
         * Map order status từ API (0,1,2,3) sang OrderStatus enum
         */
        private fun mapOrderStatus(apiStatus: Int, flightNotificationStatus: String): OrderStatus {
            var orderStatus = OrderStatus.PENDING
            if (apiStatus == 0) {
                orderStatus = OrderStatus.PENDING
            } else if (apiStatus == 1) {
                orderStatus = OrderStatus.PENDING
            } else if (apiStatus == 2) {
                orderStatus = OrderStatus.IN_DELIVERY
            } else if (apiStatus == 3) {
                orderStatus = OrderStatus.DELIVERED
            } else if (apiStatus == 4) {
                orderStatus = OrderStatus.CANCEL
            }
            if (orderStatus == OrderStatus.PENDING && flightNotificationStatus == "ACTIVATED") {
                orderStatus = OrderStatus.CONFIRMED
            }
            return orderStatus
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
