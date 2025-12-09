package com.delivery.setting.ui.home

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.R
import com.delivery.setting.model.Order
import com.delivery.setting.model.OrderDisplayStyle
import com.delivery.setting.repository.CommandRepository
import com.delivery.setting.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val commandRepository: CommandRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    private var currentLockerId: String = ""

    private val _uiState = MutableStateFlow(HomePageViewState())
    val uiState: StateFlow<HomePageViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomePageViewEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Orders data
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    fun handleEvent(event: HomePageEvent) {
        when (event) {
            is HomePageEvent.LoadData -> loadData()
            is HomePageEvent.RefreshOrders -> refreshOrders()
            is HomePageEvent.SelectOrder -> selectOrder(event.orderId)
            is HomePageEvent.OrderAction -> handleOrderAction(event.order, event.displayStyle)
            else -> {}
        }
    }

    private fun loadData() {
        loadOrders()
    }

    private fun refreshOrders() {
        loadOrders()
    }

    private fun selectOrder(orderId: String) {
        viewModelScope.launch {
        //    _uiEvent.emit(HomePageViewEvent.ShowOrderDetail(orderId))
        }
    }

    private fun handleOrderAction(order: Order, displayStyle: OrderDisplayStyle) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                when (displayStyle) {
                    OrderDisplayStyle.SEND -> {
                        // Send START command (command_type = 1)
                        val segmentIndex = order.segments.firstOrNull()?.segmentIndex ?: 1
                        commandRepository.sendStartCommand(order, segmentIndex).collect { response ->
                            Timber.d("START command sent successfully: ${response.id}")
                            _uiEvent.emit(HomePageViewEvent.ShowMessageRes(R.string.command_start_success))
                            // Refresh orders to update UI
                            refreshOrders()
                        }
                    }
                    OrderDisplayStyle.UNLOAD -> {
                        // Send FINISH command (command_type = 2)
                        val segmentIndex = order.segments.lastOrNull()?.segmentIndex ?: 1
                        commandRepository.sendFinishCommand(order, segmentIndex).collect { response ->
                            Timber.d("FINISH command sent successfully: ${response.id}")
                            _uiEvent.emit(HomePageViewEvent.ShowMessageRes(R.string.command_finish_success))
                            // Refresh orders to update UI
                            refreshOrders()
                        }
                    }
                    else -> {
                        // No action for other display styles
                        Timber.w("No action defined for display style: $displayStyle")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling order action for order: ${order.id}")
                _uiEvent.emit(HomePageViewEvent.ShowMessageRes(R.string.command_error, e.message ?: "Unknown error"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun updatePendingOrdersCount() {
        val orders = _orders.value
        val pendingCount = orders.count { order ->
            val displayStyle = order.getDisplayStyle(currentLockerId)
            displayStyle == com.delivery.setting.model.OrderDisplayStyle.SEND ||
            displayStyle == com.delivery.setting.model.OrderDisplayStyle.WAITING
        }
        _uiState.value = _uiState.value.copy(pendingOrdersCount = pendingCount)
    }

    private fun loadOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                orderRepository.getOrdersList().collect { orders ->
                    _orders.value = orders
                    updatePendingOrdersCount()
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }


}

data class HomePageViewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingOrdersCount: Int = 0
)

sealed class HomePageEvent {
    data object LoadData : HomePageEvent()
    data object RefreshOrders : HomePageEvent()
    data class SelectOrder(val orderId: String) : HomePageEvent()
    data class OrderAction(val order: Order, val displayStyle: OrderDisplayStyle) : HomePageEvent()
}

sealed class HomePageViewEvent {
    data class ShowMessage(val message: String) : HomePageViewEvent()
    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) : HomePageViewEvent()
    data class NavigateToOrderDetail(val orderId: String) : HomePageViewEvent()
}