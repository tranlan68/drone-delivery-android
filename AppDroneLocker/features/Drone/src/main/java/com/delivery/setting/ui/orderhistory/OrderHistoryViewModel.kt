package com.delivery.setting.ui.orderhistory

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor() : BaseViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryViewState())
    val uiState: StateFlow<OrderHistoryViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<OrderHistoryViewEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun handleEvent(event: OrderHistoryEvent) {
        when (event) {
            is OrderHistoryEvent.SearchOrders -> handleSearchOrders(event.query)
            is OrderHistoryEvent.ClearSearch -> handleClearSearch()
            is OrderHistoryEvent.SwitchTab -> handleSwitchTab(event.tabPosition)
        }
    }

    private fun handleSearchOrders(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchQuery = query
            )
            
// No need to emit event to fragments anymore
        }
    }

    private fun handleClearSearch() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchQuery = ""
            )
            
            // No need to emit event to fragments anymore
        }
    }

    private fun handleSwitchTab(tabPosition: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentTabPosition = tabPosition
            )
        }
    }
}

data class OrderHistoryViewState(
    val searchQuery: String = "",
    val currentTabPosition: Int = 0,
    val isLoading: Boolean = false
)

sealed class OrderHistoryEvent {
    data class SearchOrders(val query: String) : OrderHistoryEvent()
    object ClearSearch : OrderHistoryEvent()
    data class SwitchTab(val tabPosition: Int) : OrderHistoryEvent()
}

sealed class OrderHistoryViewEvent {
    data class UpdateSearchInFragments(val query: String) : OrderHistoryViewEvent()
}
