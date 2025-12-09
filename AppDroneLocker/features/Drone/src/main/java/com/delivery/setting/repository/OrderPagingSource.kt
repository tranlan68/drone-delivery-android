package com.delivery.setting.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.delivery.setting.model.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber

class OrderPagingSource(
    private val orderRepository: OrderRepository
) : PagingSource<Int, Order>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Order> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            
            Timber.d("OrderPagingSource.load() - page: $page, size: $pageSize")
            
            // Call real API
            val orders = orderRepository.getOrdersList()
            val ordersList = orders.first()
            
            Timber.d("API returned ${ordersList.size} orders")
            
            // Simulate pagination by slicing the list
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, ordersList.size)
            
            val pagedOrders = if (startIndex < ordersList.size) {
                ordersList.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
            
            Timber.d("Returning ${pagedOrders.size} orders for page $page")
            
            LoadResult.Page(
                data = pagedOrders,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (pagedOrders.isEmpty() || endIndex == ordersList.size) null else page + 1
            )
        } catch (e: Exception) {
            Timber.e(e, "Error loading orders from API")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Order>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
