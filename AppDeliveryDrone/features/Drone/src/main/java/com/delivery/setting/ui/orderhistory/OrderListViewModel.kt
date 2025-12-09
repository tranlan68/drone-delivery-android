package com.delivery.setting.ui.orderhistory

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.model.OrderTab
import com.delivery.setting.repository.OrderHistoryPagingSource
import com.delivery.setting.repository.OrderRepository
import com.delivery.setting.ui.createorder.OrderProducts
import com.delivery.setting.ui.createorder.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class OrderListViewModel
    @Inject
    constructor(
        private val orderRepository: OrderRepository,
    ) : BaseViewModel() {

        @Inject
        lateinit var rxPreferences: RxPreferences

        private val _uiState = MutableStateFlow(OrderListViewState())
        val uiState: StateFlow<OrderListViewState> = _uiState.asStateFlow()

        private val _uiEvent = MutableSharedFlow<OrderListViewEvent>()
        val uiEvent = _uiEvent.asSharedFlow()

        private var currentOrderTab: OrderTab = OrderTab.CURRENT
        private var currentSearchQuery: String = ""
        private var currentPagingSource: OrderHistoryPagingSource? = null

        private val _refreshTrigger = MutableStateFlow(0)

        private val orderIdList = mutableListOf<String>()

        private val _updatedOrders = MutableStateFlow<Map<String, OrderHistoryItem>>(emptyMap())
        val updatedOrders = _updatedOrders.asStateFlow()

    fun refreshOrderDetails(ids: List<String>) {
        viewModelScope.launch {
            val statuses =
                when (currentOrderTab) {
                    OrderTab.CURRENT -> listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.IN_DELIVERY)
                    OrderTab.HISTORY -> listOf(OrderStatus.DELIVERED, OrderStatus.CANCEL)
                }
            try {
                val freshOrders = orderRepository.getOrders(
                    page = 0,
                    pageSize = 10000,
                    statuses = statuses,
                    searchQuery = currentSearchQuery,
                )
                val orders = if (orderIdList.isNotEmpty()) {
                    freshOrders.filter { it.id in orderIdList }
                } else {
                    mutableListOf<OrderHistoryItem>()
                }
                for (orderItem in orders) {
                    // TKL DEMO
                    /*val number = Random.nextInt(0, 5)
                    if (number == 0) {
                        orderItem.status = OrderStatus.PENDING
                    } else if (number == 1) {
                        orderItem.status = OrderStatus.CONFIRMED
                    } else if (number == 2) {
                        orderItem.status = OrderStatus.IN_DELIVERY
                    } else if (number == 3) {
                        orderItem.status = OrderStatus.DELIVERED
                    }*/
                }
                _updatedOrders.value = orders.associateBy { it.id }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


        val ordersPagingData: Flow<PagingData<OrderHistoryItem>> =
            _refreshTrigger
                .flatMapLatest {
                    createPagingFlow()
                }

        fun handleEvent(event: OrderListEvent) {
            when (event) {
                is OrderListEvent.LoadOrders -> loadOrders(event.orderIds, event.orderTab)
                is OrderListEvent.SearchOrders -> searchOrders(event.orderIds, event.query)
                is OrderListEvent.SelectOrder -> selectOrder(event.orderIds, event.orderId)
                is OrderListEvent.RefreshOrders -> refreshOrders()
            }
        }

        private fun loadOrders(orderIds: List<String>, orderTab: OrderTab) {
            orderIdList.addAll(orderIds)
            currentOrderTab = orderTab
            updateOrdersPagingData()
        }

        private fun searchOrders(orderIds: List<String>, query: String) {
            orderIdList.addAll(orderIds)
            Timber.d("OrderListViewModel.searchOrders() - query: '$query', currentTab: $currentOrderTab")
            currentSearchQuery = query
            // Invalidate current paging source to force reload with new query
            currentPagingSource?.invalidate()
            updateOrdersPagingData()
        }

        private fun selectOrder(orderIds: List<String>, orderId: String) {
            orderIdList.addAll(orderIds)
            viewModelScope.launch {
                // Find the order item by ID from current data
                // For now, we'll emit the orderId and let the Fragment handle finding the item
                _uiEvent.emit(OrderListViewEvent.ShowOrderDetail(orderId))
            }
        }

        private fun refreshOrders() {
            updateOrdersPagingData()
        }

        private fun updateOrdersPagingData() {
            // Trigger refresh by incrementing the trigger value
            Timber.d("updateOrdersPagingData() - triggering refresh with query: '$currentSearchQuery'")
            _refreshTrigger.value = _refreshTrigger.value + 1
        }

        private fun createPagingFlow(): Flow<PagingData<OrderHistoryItem>> {
            Timber.d("createPagingFlow() - creating new paging flow with query: '$currentSearchQuery', tab: $currentOrderTab")
            return Pager(
                config =
                    PagingConfig(
                        pageSize = 10,
                        enablePlaceholders = false,
                        initialLoadSize = 10,
                    ),
                pagingSourceFactory = {
                    Timber.d("Creating new PagingSource with query: '$currentSearchQuery', tab: $currentOrderTab")
                    OrderHistoryPagingSource(
                        orderRepository = orderRepository,
                        orderTab = currentOrderTab,
                        searchQuery = currentSearchQuery,
                        orderIds = orderIdList,
                    ).also {
                        currentPagingSource = it
                    }
                },
            ).flow.cachedIn(viewModelScope)
        }

    fun startAutoRefresh(ids: List<String>) {
        viewModelScope.launch {
            while (true) {
                refreshOrderDetails(ids)
                delay(10_000)
            }
        }
    }
    }

data class OrderListViewState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed class OrderListEvent {
    data class LoadOrders(val orderIds: List<String>, val orderTab: OrderTab) : OrderListEvent()

    data class SearchOrders(val orderIds: List<String>, val query: String) : OrderListEvent()

    data class SelectOrder(val orderIds: List<String>, val orderId: String) : OrderListEvent()

    object RefreshOrders : OrderListEvent()
}

sealed class OrderListViewEvent {
    data class ShowMessage(val message: String) : OrderListViewEvent()

    data class ShowOrderDetail(val orderId: String) : OrderListViewEvent()
}
