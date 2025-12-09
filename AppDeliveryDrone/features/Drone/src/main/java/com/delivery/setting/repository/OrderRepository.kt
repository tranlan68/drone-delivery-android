package com.delivery.setting.repository

import com.delivery.core.model.mocking.OrderCreateRequest
import com.delivery.core.model.mocking.OrderCreateResponse
import com.delivery.core.model.mocking.SegmentDto
import com.delivery.core.network.ApiInterface
import com.delivery.setting.mapper.OrderMapper
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.mapNotNull

@Singleton
class OrderRepository
@Inject
constructor(
    private val apiInterface: ApiInterface,
    private val orderMapper: OrderMapper,
) {
    suspend fun createOrder(orderRequest: OrderCreateRequest): Result<OrderCreateResponse> {
        return try {
            orderRequest.isDemo = true
            val response = apiInterface.createOrder(orderRequest)
            Result.success(response)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái
     * Sử dụng API mới theo documentation
     */
    suspend fun getOrders(
        statuses: List<OrderStatus>,
        searchQuery: String = "",
        page: Int = 0,
        pageSize: Int = 10,
    ): List<OrderHistoryItem> {
        return try {
            // Sử dụng API thực tế theo documentation mới
            getOrdersFromApi(statuses, searchQuery, page, pageSize)
        } catch (e: Exception) {
            Timber.w(e, "API call failed, falling back to mock data")
            // Fallback to mock data nếu API có lỗi
            //getMockOrders(statuses, searchQuery, page, pageSize)
            return emptyList();
        }
    }

    /**
     * Lấy orders từ API thực tế theo documentation mới
     */
    private suspend fun getOrdersFromApi(
        statuses: List<OrderStatus>,
        searchQuery: String = "",
        page: Int = 0,
        pageSize: Int = 10,
    ): List<OrderHistoryItem> {
        try {
            // Gọi API mới - lấy tất cả orders
            val apiResponses = apiInterface.getAllOrders()
            Timber.d("Received ${apiResponses.size} orders from API")

            // Map sang OrderHistoryItem với locker names
            val allOrders =
                apiResponses.mapNotNull { orderDto ->
                    try {
                        orderMapper.mapToOrderHistoryItem(orderDto)
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to map order ${orderDto.id}, skipping")
                        null
                    }
                }.sortedByDescending { it.createdAt }
            Timber.d("Successfully mapped and sorted ${allOrders.size} orders")
            Timber.tag("Trong").d("All orders: $allOrders")

            // Filter theo status
            val filteredByStatus = allOrders.filter { it.status in statuses }

            // Apply search filter locally (since API doesn't support search yet)
            val filteredBySearch =
                if (searchQuery.isNotEmpty()) {
                    filteredByStatus.filter { order ->
                        order.sourceLocker.contains(searchQuery, ignoreCase = true) ||
                                order.destLocker.contains(searchQuery, ignoreCase = true) ||
                                order.packageType.contains(searchQuery, ignoreCase = true)
                    }
                } else {
                    filteredByStatus
                }

            // Simulate pagination
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, filteredBySearch.size)

            return if (startIndex < filteredBySearch.size) {
                filteredBySearch.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching orders from API")
            throw e
        }
    }

    suspend fun getOrderById(orderId: String): OrderHistoryItem? {
        try {
            val apiResponse = apiInterface.getOrderById(orderId)
            val order = orderMapper.mapToOrderHistoryItem(apiResponse)
            return order
        } catch (e: Exception) {
            // Log error and rethrow for ViewModel to handle
            //throw e
            return null
        }
    }

    /**
     * Lấy mock orders (tạm thời)
     */
    private suspend fun getMockOrders(
        statuses: List<OrderStatus>,
        searchQuery: String = "",
        page: Int = 0,
        pageSize: Int = 10,
    ): List<OrderHistoryItem> {
        // Simulate network delay
        delay(1000)

        Timber.d("OrderRepository.getMockOrders() - statuses: $statuses, searchQuery: '$searchQuery', page: $page")

        // Generate mock data based on statuses
        val allOrders = generateMockOrders()
        val filteredByStatus = allOrders.filter { it.status in statuses }

        // Apply search filter locally (since API doesn't support search yet)
        val filteredBySearch =
            if (searchQuery.isNotEmpty()) {
                filteredByStatus.filter { order ->
                    order.sourceLocker.contains(searchQuery, ignoreCase = true) ||
                            order.destLocker.contains(searchQuery, ignoreCase = true) ||
                            order.packageType.contains(searchQuery, ignoreCase = true)
                }
            } else {
                filteredByStatus
            }

        // Simulate pagination
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, filteredBySearch.size)

        return if (startIndex < filteredBySearch.size) {
            filteredBySearch.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }

    private fun generateMockOrders(): List<OrderHistoryItem> {
        return listOf(
            // Current orders (PENDING, IN_PROGRESS)
            OrderHistoryItem(
                id = "current_1",
                intId = "1",
                sourceLocker = "Locker Lạc Long Quân",
                destLocker = "Locker Keangnam",
                idSource = "locker_laclong_quan",
                idDest = "locker_keangnam",
                packageType = "Loại hàng",
                packageWeight = "S • 1 kg",
                orderDate = "04 Aug",
                status = OrderStatus.IN_DELIVERY,
                orderTime = "14:30",
                droneId = "DRONE_001",
                receiverPhone = "0901234567",
                eta = 1672531200000L, // Sample timestamp
                segments =
                    listOf(
                        SegmentDto(
                            segmentIndex = 1,
                            source = "locker_laclong_quan",
                            dest = "locker_intermediate_1",
                            flightLaneId = "lane_001_segment_1",
                            droneId = "DRONE_001",
                            segmentStatus = 2, // Completed
                            segmentType = 1,
                        ),
                        SegmentDto(
                            segmentIndex = 2,
                            source = "locker_intermediate_1",
                            dest = "locker_keangnam",
                            flightLaneId = "lane_001_segment_2",
                            droneId = "DRONE_001",
                            segmentStatus = 1, // In progress
                            segmentType = 1,
                        ),
                    ),
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "current_2",
                intId = "2",
                sourceLocker = "Locker Nguyễn Du",
                destLocker = "Locker Lotte Tower",
                idSource = "locker_nguyen_du",
                idDest = "locker_lotte_tower",
                packageType = "Loại hàng",
                packageWeight = "M • 2 kg",
                orderDate = "04 Aug",
                status = OrderStatus.PENDING,
                orderTime = "13:15",
                droneId = "DRONE_002",
                receiverPhone = "0907654321",
                eta = 1672534800000L,
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "current_3",
                intId = "3",
                sourceLocker = "Locker Hoàng Hoa Thám",
                destLocker = "Locker Times City",
                idSource = "locker_hoang_hoa_tham",
                idDest = "locker_times_city",
                packageType = "Loại hàng",
                packageWeight = "L • 3 kg",
                orderDate = "04 Aug",
                status = OrderStatus.IN_DELIVERY,
                orderTime = "12:00",
                droneId = "DRONE_003",
                receiverPhone = "0912345678",
                eta = 1672538400000L,
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "current_4",
                intId = "4",
                sourceLocker = "Locker Trần Phú",
                destLocker = "Locker Royal City",
                idSource = "locker_tran_phu",
                idDest = "locker_royal_city",
                packageType = "Loại hàng",
                packageWeight = "S • 0.5 kg",
                orderDate = "03 Aug",
                status = OrderStatus.IN_DELIVERY,
                orderTime = "16:45",
                droneId = "DRONE_004",
                receiverPhone = "0987654321",
                eta = 1672542000000L,
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "current_5",
                intId = "5",
                sourceLocker = "Locker Láng Hạ",
                destLocker = "Locker Vincom Bà Triệu",
                idSource = "locker_lang_ha",
                idDest = "locker_vincom_ba_trieu",
                packageType = "Loại hàng",
                packageWeight = "XL • 5 kg",
                orderDate = "03 Aug",
                status = OrderStatus.PENDING,
                orderTime = "11:20",
                droneId = "DRONE_005",
                receiverPhone = "0923456789",
                eta = 1672545600000L,
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            // History orders (DELIVERED)
            OrderHistoryItem(
                id = "history_1",
                intId = "1",
                sourceLocker = "Locker Lạc Long Quân",
                destLocker = "Locker Keangnam",
                idSource = "locker_laclong_quan",
                idDest = "locker_keangnam",
                packageType = "Loại hàng",
                packageWeight = "S • 1 kg",
                orderDate = "02 Aug",
                status = OrderStatus.DELIVERED,
                orderTime = "09:30",
                droneId = "DRONE_001",
                receiverPhone = "0901234567",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_2",
                intId = "2",
                sourceLocker = "Locker Nguyễn Du",
                destLocker = "Locker Lotte Tower",
                idSource = "locker_nguyen_du",
                idDest = "locker_lotte_tower",
                packageType = "Loại hàng",
                packageWeight = "M • 2 kg",
                orderDate = "01 Aug",
                status = OrderStatus.DELIVERED,
                orderTime = "15:45",
                droneId = "DRONE_002",
                receiverPhone = "0907654321",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_3",
                intId = "3",
                sourceLocker = "Locker Hoàng Hoa Thám",
                destLocker = "Locker Times City",
                idSource = "locker_hoang_hoa_tham",
                idDest = "locker_times_city",
                packageType = "Loại hàng",
                packageWeight = "L • 3 kg",
                orderDate = "31 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "10:15",
                droneId = "DRONE_003",
                receiverPhone = "0912345678",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_4",
                intId = "4",
                sourceLocker = "Locker Trần Phú",
                destLocker = "Locker Royal City",
                idSource = "locker_tran_phu",
                idDest = "locker_royal_city",
                packageType = "Loại hàng",
                packageWeight = "S • 0.5 kg",
                orderDate = "30 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "14:20",
                droneId = "DRONE_004",
                receiverPhone = "0987654321",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_5",
                intId = "5",
                sourceLocker = "Locker Láng Hạ",
                destLocker = "Locker Vincom Bà Triệu",
                idSource = "locker_lang_ha",
                idDest = "locker_vincom_ba_trieu",
                packageType = "Loại hàng",
                packageWeight = "XL • 5 kg",
                orderDate = "29 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "13:10",
                droneId = "DRONE_005",
                receiverPhone = "0923456789",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_6",
                intId = "6",
                sourceLocker = "Locker Kim Mã",
                destLocker = "Locker Big C Thăng Long",
                idSource = "locker_kim_ma",
                idDest = "locker_bigc_thang_long",
                packageType = "Loại hàng",
                packageWeight = "M • 1.5 kg",
                orderDate = "28 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "17:30",
                droneId = "DRONE_006",
                receiverPhone = "0934567890",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_7",
                intId = "7",
                sourceLocker = "Locker Giải Phóng",
                destLocker = "Locker Aeon Long Biên",
                idSource = "locker_giai_phong",
                idDest = "locker_aeon_long_bien",
                packageType = "Loại hàng",
                packageWeight = "S • 0.8 kg",
                orderDate = "27 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "08:45",
                droneId = "DRONE_007",
                receiverPhone = "0945678901",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
            OrderHistoryItem(
                id = "history_8",
                intId = "8",
                sourceLocker = "Locker Xuân Thủy",
                destLocker = "Locker Vincom Mega Mall",
                idSource = "locker_xuan_thuy",
                idDest = "locker_vincom_mega_mall",
                packageType = "Loại hàng",
                packageWeight = "L • 4 kg",
                orderDate = "26 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "12:25",
                droneId = "DRONE_008",
                receiverPhone = "0956789012",
                segments = null,
                deliveryDate = "04 Aug",
                deliveryTime = "14:30",
                createdAt = 1672531200000L
            ),
        )
    }
}
