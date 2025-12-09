package com.delivery.setting.repository

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.model.OrderTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OrderHistoryPagingSource
    @Inject
    constructor(
        private val orderRepository: OrderRepository,
        private val orderTab: OrderTab,
        private val searchQuery: String = "",
        private val orderIds: List<String>,
    ) : PagingSource<Int, OrderHistoryItem>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, OrderHistoryItem> {
            return try {
                val page = params.key ?: 0
                val pageSize = params.loadSize

                Timber.d("OrderHistoryPagingSource.load() - orderTab: $orderTab, searchQuery: '$searchQuery', page: $page")

                // Determine which statuses to fetch based on tab
                val statuses =
                    when (orderTab) {
                        OrderTab.CURRENT -> listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.IN_DELIVERY)
                        OrderTab.HISTORY -> listOf(OrderStatus.DELIVERED, OrderStatus.CANCEL)
                    }

                // Get data from repository
                val initialOrders =
                    withContext(Dispatchers.IO) {
                        orderRepository.getOrders(
                            page = page,
                            pageSize = pageSize * 1000,
                            statuses = statuses,
                            searchQuery = searchQuery,
                        )
                    }
                val orders = if (orderIds.isNotEmpty()) {
                    initialOrders.filter { it.id in orderIds }
                } else {
                    mutableListOf<OrderHistoryItem>()
                }


                Timber.d("OrderHistoryPagingSource: Loaded page $page with ${orders.size} items for tab $orderTab and query '$searchQuery'")

                LoadResult.Page(
                    data = orders,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (orders.size < pageSize) null else page + 1,
                )
            } catch (exception: Exception) {
                Timber.e(exception, "Error loading orders")
                LoadResult.Error(exception)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, OrderHistoryItem>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }
    }
