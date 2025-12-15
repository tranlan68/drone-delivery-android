package com.delivery.setting.ui.home

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.utils.SingleLiveEvent
import com.delivery.setting.model.DeliveryLocation
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomePageViewModel
    @Inject
    constructor(
        private val orderRepository: OrderRepository,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(HomePageViewState())
        val uiState: StateFlow<HomePageViewState> = _uiState.asStateFlow()

        private val _uiEvent = SingleLiveEvent<HomePageViewEvent>()
        val uiEvent: SingleLiveEvent<HomePageViewEvent> = _uiEvent

        private val _pickupLocation = MutableStateFlow<String?>(null)
        val pickupLocation: StateFlow<String?> = _pickupLocation

        private val _deliveryLocation = MutableStateFlow<String?>(null)
        val deliveryLocation: StateFlow<String?> = _deliveryLocation

        init {
            loadDeliveredOrders()
        }

        fun handleEvent(event: HomePageEvent) {
            when (event) {
                is HomePageEvent.LoadDeliveredOrders -> loadDeliveredOrders()
                is HomePageEvent.RefreshData -> refreshData()
            }
        }

        private fun loadDeliveredOrders() {
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)

                    // Load delivered orders for reorder functionality
                    val deliveredOrders =
                        orderRepository.getOrders(
                            statuses = listOf(OrderStatus.DELIVERED),
                            page = 0,
                            pageSize = 10,
                        )

                    // Use OrderHistoryItem directly
                    val reorderItems = deliveredOrders

                    // Extract unique delivery locations for old locations
                    val uniqueLocations =
                        deliveredOrders
                            .map { it.destLocker }
                            .distinct()
                            .mapIndexed { index, lockerName ->
                                DeliveryLocation(
                                    id = "location_$index",
                                    address = lockerName,
                                )
                            }

                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            reorderItems = reorderItems,
                            oldLocations = uniqueLocations,
                            hasError = false,
                        )

                    Timber.d("Loaded ${reorderItems.size} reorder items and ${uniqueLocations.size} old locations")
                } catch (e: Exception) {
                    Timber.e(e, "Error loading delivered orders")
                    _uiState.value =
                        _uiState.value.copy(
                            isLoading = false,
                            hasError = true,
                        )
                    _uiEvent.postValue(
                        HomePageViewEvent.ShowMessageRes(
                            com.delivery.setting.R.string.error_load_orders_failed,
                            e.message ?: "Unknown error",
                        ),
                    )
                }
            }
        }

        private fun refreshData() {
            loadDeliveredOrders()
        }

        fun setPickupLocation(location: String) {
            _pickupLocation.value = location
        }

        fun setDeliveryLocation(location: String) {
            _deliveryLocation.value = location
        }

        fun setSwapLocations() {
            val currentPickup = _pickupLocation.value
            _pickupLocation.value = _deliveryLocation.value
            _deliveryLocation.value = currentPickup
        }

        fun selectQuickDeliveryLocation(location: String) {
            _deliveryLocation.value = location
        }

        fun canCreateOrder(): Boolean {
            return _pickupLocation.value != null && _deliveryLocation.value != null
        }

        fun reorderItem(orderItem: OrderHistoryItem) {
            viewModelScope.launch {
                // Set locations from order item
                _pickupLocation.value = orderItem.sourceLocker
                _deliveryLocation.value = orderItem.destLocker
            }
        }

        fun createOrder() {
            if (canCreateOrder()) {
                // TODO: Implement order creation logic
                viewModelScope.launch {
                    try {
                        isLoading.value = true
                        // Call API to create order
                        // Navigate to tracking screen
                    } catch (e: Exception) {
                        messageError.value = e.message
                    } finally {
                        isLoading.value = false
                    }
                }
            }
        }
    }

// MVI State
data class HomePageViewState(
    val isLoading: Boolean = false,
    val reorderItems: List<OrderHistoryItem> = emptyList(),
    val oldLocations: List<DeliveryLocation> = emptyList(),
    val hasError: Boolean = false,
)

// MVI Events (User Actions)
sealed class HomePageEvent {
    object LoadDeliveredOrders : HomePageEvent()

    object RefreshData : HomePageEvent()
}

// MVI View Events (UI Actions)
sealed class HomePageViewEvent {
    data class ShowMessage(val message: String) : HomePageViewEvent()

    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) : HomePageViewEvent()
}
