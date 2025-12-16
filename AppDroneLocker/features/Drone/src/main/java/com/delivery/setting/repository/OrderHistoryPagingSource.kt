package com.delivery.setting.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.model.OrderTab
import kotlinx.coroutines.delay
import timber.log.Timber

class OrderHistoryPagingSource(
    private val orderTab: OrderTab,
    private val searchQuery: String = ""
) : PagingSource<Int, OrderHistoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OrderHistoryItem> {
        return try {
            val page = params.key ?: 0
            
            Timber.d("OrderHistoryPagingSource.load() - orderTab: $orderTab, searchQuery: '$searchQuery', page: $page")
            
            // Simulate network delay
            delay(1000)
            
            val mockData = generateMockData(orderTab)
            
            // Filter by search query if provided
            val filteredData = if (searchQuery.isNotEmpty()) {
                val filtered = mockData.filter { item ->
                    item.fromLocation.contains(searchQuery, ignoreCase = true) ||
                    item.toLocation.contains(searchQuery, ignoreCase = true) ||
                    item.fromAddress.contains(searchQuery, ignoreCase = true) ||
                    item.toAddress.contains(searchQuery, ignoreCase = true)
                }
                Timber.d("Filtered data: ${filtered.size} items (original: ${mockData.size})")
                filtered
            } else {
                Timber.d("No filter applied, returning all ${mockData.size} items")
                mockData
            }

            Timber.d("OrderHistoryPagingSource: Loaded page $page with ${filteredData.size} items for tab $orderTab and query '$searchQuery'")
            // Simulate pagination
            val pageSize = params.loadSize
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, filteredData.size)
            
            val pageData = if (startIndex < filteredData.size) {
                filteredData.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            val result = LoadResult.Page(
                data = pageData,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (endIndex >= filteredData.size) null else page + 1
            )
            Timber.d("Returning page data: ${pageData.size} items")
            result
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, OrderHistoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    private fun generateMockData(orderTab: OrderTab): List<OrderHistoryItem> {
        return when (orderTab) {
            OrderTab.CURRENT -> generateCurrentOrders()
            OrderTab.HISTORY -> generateHistoryOrders()
        }
    }

    private fun generateCurrentOrders(): List<OrderHistoryItem> {
        return listOf(
            OrderHistoryItem(
                id = "current_1",
                fromLocation = "Nguyễn Trọng Thịnh",
                fromAddress = "380 Lạc Long Quân",
                toLocation = "Hương",
                toAddress = "Keangnam Landmark 72",
                packageType = "Loại hàng",
                packageWeight = "S • 1 kg",
                orderDate = "04 Aug",
                status = OrderStatus.IN_PROGRESS,
                orderTime = "14:30"
            ),
            OrderHistoryItem(
                id = "current_2",
                fromLocation = "Trần Văn Nam",
                fromAddress = "123 Nguyễn Du, Hai Bà Trưng",
                toLocation = "Mai Anh",
                toAddress = "Lotte Tower Hanoi",
                packageType = "Loại hàng",
                packageWeight = "M • 2 kg",
                orderDate = "04 Aug",
                status = OrderStatus.PENDING,
                orderTime = "13:15"
            ),
            OrderHistoryItem(
                id = "current_3",
                fromLocation = "Lê Thị Hoa",
                fromAddress = "456 Hoàng Hoa Thám, Ba Đình",
                toLocation = "Phương",
                toAddress = "Times City",
                packageType = "Loại hàng",
                packageWeight = "L • 3 kg",
                orderDate = "04 Aug",
                status = OrderStatus.CONFIRMED,
                orderTime = "12:00"
            ),
            OrderHistoryItem(
                id = "current_4",
                fromLocation = "Nguyễn Văn Minh",
                fromAddress = "789 Trần Phú, Cầu Giấy",
                toLocation = "Lan",
                toAddress = "Royal City",
                packageType = "Loại hàng",
                packageWeight = "S • 0.5 kg",
                orderDate = "03 Aug",
                status = OrderStatus.IN_PROGRESS,
                orderTime = "16:45"
            ),
            OrderHistoryItem(
                id = "current_5",
                fromLocation = "Phạm Thị Thu",
                fromAddress = "321 Láng Hạ, Đống Đa",
                toLocation = "Anh",
                toAddress = "Vincom Bà Triệu",
                packageType = "Loại hàng",
                packageWeight = "XL • 5 kg",
                orderDate = "03 Aug",
                status = OrderStatus.PENDING,
                orderTime = "11:20"
            )
        )
    }

    private fun generateHistoryOrders(): List<OrderHistoryItem> {
        return listOf(
            OrderHistoryItem(
                id = "history_1",
                fromLocation = "Nguyễn Trọng Thịnh",
                fromAddress = "380 Lạc Long Quân",
                toLocation = "Hương",
                toAddress = "Keangnam Landmark 72",
                packageType = "Loại hàng",
                packageWeight = "S • 1 kg",
                orderDate = "02 Aug",
                status = OrderStatus.DELIVERED,
                orderTime = "09:30"
            ),
            OrderHistoryItem(
                id = "history_2",
                fromLocation = "Trần Văn Nam",
                fromAddress = "123 Nguyễn Du, Hai Bà Trưng",
                toLocation = "Mai Anh",
                toAddress = "Lotte Tower Hanoi",
                packageType = "Loại hàng",
                packageWeight = "M • 2 kg",
                orderDate = "01 Aug",
                status = OrderStatus.DELIVERED,
                orderTime = "15:45"
            ),
            OrderHistoryItem(
                id = "history_3",
                fromLocation = "Lê Thị Hoa",
                fromAddress = "456 Hoàng Hoa Thám, Ba Đình",
                toLocation = "Phương",
                toAddress = "Times City",
                packageType = "Loại hàng",
                packageWeight = "L • 3 kg",
                orderDate = "31 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "10:15"
            ),
            OrderHistoryItem(
                id = "history_4",
                fromLocation = "Nguyễn Văn Minh",
                fromAddress = "789 Trần Phú, Cầu Giấy",
                toLocation = "Lan",
                toAddress = "Royal City",
                packageType = "Loại hàng",
                packageWeight = "S • 0.5 kg",
                orderDate = "30 Jul",
                status = OrderStatus.CANCELLED,
                orderTime = "14:20"
            ),
            OrderHistoryItem(
                id = "history_5",
                fromLocation = "Phạm Thị Thu",
                fromAddress = "321 Láng Hạ, Đống Đa",
                toLocation = "Anh",
                toAddress = "Vincom Bà Triệu",
                packageType = "Loại hàng",
                packageWeight = "XL • 5 kg",
                orderDate = "29 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "13:10"
            ),
            OrderHistoryItem(
                id = "history_6",
                fromLocation = "Hoàng Văn Đức",
                fromAddress = "654 Kim Mã, Ba Đình",
                toLocation = "Linh",
                toAddress = "Big C Thăng Long",
                packageType = "Loại hàng",
                packageWeight = "M • 1.5 kg",
                orderDate = "28 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "17:30"
            ),
            OrderHistoryItem(
                id = "history_7",
                fromLocation = "Vũ Thị Mai",
                fromAddress = "987 Giải Phóng, Hoàng Mai",
                toLocation = "Tuấn",
                toAddress = "Aeon Mall Long Biên",
                packageType = "Loại hàng",
                packageWeight = "S • 0.8 kg",
                orderDate = "27 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "08:45"
            ),
            OrderHistoryItem(
                id = "history_8",
                fromLocation = "Đỗ Văn Hùng",
                fromAddress = "147 Xuân Thủy, Cầu Giấy",
                toLocation = "Nga",
                toAddress = "Vincom Mega Mall",
                packageType = "Loại hàng",
                packageWeight = "L • 4 kg",
                orderDate = "26 Jul",
                status = OrderStatus.DELIVERED,
                orderTime = "12:25"
            )
        )
    }
}
