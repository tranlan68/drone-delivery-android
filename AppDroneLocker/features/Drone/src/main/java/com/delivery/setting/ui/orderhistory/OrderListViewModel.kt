package com.delivery.setting.ui.orderhistory

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.delivery.core.base.BaseViewModel
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderTab
import com.delivery.setting.repository.OrderHistoryPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class OrderListViewModel @Inject constructor() : BaseViewModel() {

    private val _uiState = MutableStateFlow(OrderListViewState())
    val uiState: StateFlow<OrderListViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OrderListViewEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var currentOrderTab: OrderTab = OrderTab.CURRENT
    private var currentSearchQuery: String = ""
    private var currentPagingSource: OrderHistoryPagingSource? = null

    private val _refreshTrigger = MutableStateFlow(0)
    
    val ordersPagingData: Flow<PagingData<OrderHistoryItem>> = _refreshTrigger
        .flatMapLatest { 
            createPagingFlow()
        }

    fun handleEvent(event: OrderListEvent) {
        when (event) {
            is OrderListEvent.LoadOrders -> loadOrders(event.orderTab)
            is OrderListEvent.SearchOrders -> searchOrders(event.query)
            is OrderListEvent.SelectOrder -> selectOrder(event.orderId)
            is OrderListEvent.RefreshOrders -> refreshOrders()
        }
    }

    private fun loadOrders(orderTab: OrderTab) {
        currentOrderTab = orderTab
        updateOrdersPagingData()
    }

    private fun searchOrders(query: String) {
        Timber.d("OrderListViewModel.searchOrders() - query: '$query', currentTab: $currentOrderTab")
        currentSearchQuery = query
        // Invalidate current paging source to force reload with new query
        currentPagingSource?.invalidate()
        updateOrdersPagingData()
    }

    private fun selectOrder(orderId: String) {
        viewModelScope.launch {
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
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSize = 10
            ),
            pagingSourceFactory = {
                Timber.d("Creating new PagingSource with query: '$currentSearchQuery', tab: $currentOrderTab")
                OrderHistoryPagingSource(
                    orderTab = currentOrderTab,
                    searchQuery = currentSearchQuery
                ).also { 
                    currentPagingSource = it
                }
            }
        ).flow.cachedIn(viewModelScope)
    }
}

data class OrderListViewState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class OrderListEvent {
    data class LoadOrders(val orderTab: OrderTab) : OrderListEvent()
    data class SearchOrders(val query: String) : OrderListEvent()
    data class SelectOrder(val orderId: String) : OrderListEvent()
    object RefreshOrders : OrderListEvent()
}

sealed class OrderListViewEvent {
    data class ShowMessage(val message: String) : OrderListViewEvent()
    data class ShowOrderDetail(val orderId: String) : OrderListViewEvent()
}
