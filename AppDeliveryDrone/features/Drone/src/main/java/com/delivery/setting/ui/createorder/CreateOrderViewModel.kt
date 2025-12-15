package com.delivery.setting.ui.createorder

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.model.mocking.OrderCreateResponse
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.SingleLiveEvent
import com.delivery.setting.R
import com.delivery.setting.domain.usecase.CreateOrderUseCase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel
    @Inject
    constructor(
        private val createOrderUseCase: CreateOrderUseCase,
    ) : BaseViewModel() {

        @Inject
        lateinit var rxPreferences: RxPreferences

        private val _uiState = MutableStateFlow(CreateOrderViewState())
        val uiState: StateFlow<CreateOrderViewState> = _uiState.asStateFlow()

        private val _uiEvent = SingleLiveEvent<CreateOrderViewEvent>()
        val uiEvent: SingleLiveEvent<CreateOrderViewEvent> = _uiEvent

        fun handleEvent(event: CreateOrderEvent) {
            when (event) {
                is CreateOrderEvent.SelectSize -> selectSize(event.size)
                is CreateOrderEvent.SelectFromLocation -> selectFromLocation(event.locationName, event.id)
                is CreateOrderEvent.SelectToLocation -> selectToLocation(event.locationName, event.id)
                is CreateOrderEvent.SelectPriority -> selectPriority(event.priority)
                is CreateOrderEvent.CreateOrder -> createOrder(event.weight, event.receiverPhone, event.products)
            }
        }

        private fun selectSize(size: BoxSize) {
            val weight =
                when (size) {
                    BoxSize.SMALL -> 1
                    BoxSize.MEDIUM -> 2
                    BoxSize.LARGE -> 3
                    BoxSize.EXTRA_LARGE -> 4
                }

            _uiState.value =
                _uiState.value.copy(
                    selectedSize = size,
                    weight = weight.toString(),
                )
        }

        private fun selectFromLocation(
            locationName: String,
            id: String,
        ) {
            _uiState.value =
                _uiState.value.copy(
                    fromLocation = locationName,
                    idSource = id,
                    fromLocationError = null,
                )
        }

        private fun selectToLocation(
            locationName: String,
            idDest: String,
        ) {
            _uiState.value =
                _uiState.value.copy(
                    toLocation = locationName,
                    idDest = idDest,
                    toLocationError = null,
                )
        }

        private fun selectPriority(priority: DeliveryPriority) {
            _uiState.value =
                _uiState.value.copy(
                    selectedPriority = priority,
                )
        }

        private fun createOrder(
            weight: String,
            receiverPhone: String,
            products: List<Product>
        ) {
            isLoading.postValue(true)
            viewModelScope.launch {
                val currentState = _uiState.value
                if (!validateInputs(currentState.fromLocation, currentState.toLocation, weight, receiverPhone)) {
                    isLoading.postValue(false)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    // Call UseCase
                    val result =
                        createOrderUseCase.execute(
                            source = currentState.idSource ?: "",
                            dest = currentState.idDest ?: "",
                            weight = weight.toInt() ?: 1,
                            size = currentState.selectedSize.value,
                            priority = currentState.selectedPriority.value,
                            receiverPhone = receiverPhone,
                        )

                    result.fold(
                        onSuccess = { response ->
                            try {
                                val gson = Gson()
                                val orderProductsListStr = rxPreferences.getOrderProducts().first().toString()
                                val type = object : com.google.gson.reflect.TypeToken<List<OrderProducts>>() {}.type
                                val orderProductsList = gson.fromJson<List<OrderProducts>>(orderProductsListStr, type)

                                val orderProducts = OrderProducts(response.orderId, products)
                                var mutable  = mutableListOf<OrderProducts>()
                                if (orderProductsList != null) {
                                    mutable = orderProductsList.toMutableList()
                                }
                                mutable.add(orderProducts)

                                val newOrderProductsListStr = gson.toJson(mutable)
                                rxPreferences.setOrderProducts(newOrderProductsListStr)
                                // Create local order object for UI
                                val order =
                                    CreateOrderRequest(
                                        fromLocation = currentState.fromLocation ?: "",
                                        toLocation = currentState.toLocation ?: "",
                                        size = currentState.selectedSize,
                                        priority = currentState.selectedPriority,
                                        weight = weight,
                                        receiverPhone = receiverPhone,
                                    )

                                _uiEvent.postValue(CreateOrderViewEvent.ShowMessageRes(com.delivery.core.R.string.order_create_success))
                                _uiEvent.postValue(CreateOrderViewEvent.NavigateToOrderSuccess(order, response))
                            } catch (exception: Exception) {
                                Log.e("Create order exception", exception.stackTraceToString().toString())
                            }
                        },
                        onFailure = { exception ->
                            when (exception) {
                                is IllegalArgumentException -> {
                                    if (exception.message != null) {
                                        _uiEvent.postValue(CreateOrderViewEvent.ShowMessage(exception.message!!))
                                    } else {
                                        _uiEvent.postValue(
                                            CreateOrderViewEvent.ShowMessageRes(com.delivery.core.R.string.order_create_error_invalid_data),
                                        )
                                    }
                                }
                                else -> {
                                    handleError(exception) { errorResponse ->
                                        if (errorResponse.message != null) {
                                            _uiEvent.postValue(CreateOrderViewEvent.ShowMessage(errorResponse.message!!))
                                        } else {
                                            _uiEvent.postValue(
                                                CreateOrderViewEvent.ShowMessageRes(com.delivery.core.R.string.order_create_error_generic),
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                } catch (exception: Exception) {
                    handleError(exception) { errorResponse ->
                        _uiEvent.postValue(CreateOrderViewEvent.ShowMessageRes(com.delivery.core.R.string.order_create_error_generic))
//                    if (errorResponse.message != null) {
//                        _uiEvent.postValue(CreateOrderViewEvent.ShowMessage(errorResponse.message!!))
//                    } else {
//                        _uiEvent.postValue(CreateOrderViewEvent.ShowMessageRes(com.delivery.core.R.string.order_create_error_generic))
//                    }
                    }
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    isLoading.postValue(false)
                }
            }
        }

        private fun validateInputs(
            fromLocation: String?,
            toLocation: String?,
            weight: String,
            receiverPhone: String,
        ): Boolean {
            var isValid = true
            var fromLocationError: Int? = null
            var toLocationError: Int? = null
            var receiverPhoneError: Int? = null

            // Validate from location
            if (fromLocation.isNullOrEmpty()) {
                fromLocationError = R.string.error_from_location_empty
                isValid = false
            }

            // Validate to location
            if (toLocation.isNullOrEmpty()) {
                toLocationError = R.string.error_to_location_empty
                isValid = false
            }

//        // Validate receiver phone
//        if (receiverPhone.isEmpty()) {
//            receiverPhoneError = R.string.error_recipient_phone_empty
//            isValid = false
//        } else if (!isValidPhoneNumber(receiverPhone)) {
//            receiverPhoneError = R.string.error_recipient_phone_invalid_format
//            isValid = false
//        }

            // Validate weight
            if (weight.isEmpty()) {
                _uiEvent.postValue(CreateOrderViewEvent.ShowMessageRes(R.string.error_weight_empty))
                isValid = false
            } else {
                val weightValue = weight.toDoubleOrNull()
                if (weightValue == null || weightValue <= 0) {
                    _uiEvent.postValue(CreateOrderViewEvent.ShowMessageRes(R.string.error_weight_invalid))
                    isValid = false
                }
            }

            _uiState.value =
                _uiState.value.copy(
                    fromLocationError = fromLocationError,
                    toLocationError = toLocationError,
                    receiverPhoneError = receiverPhoneError,
                )

            return isValid
        }

        private fun isValidPhoneNumber(phoneNumber: String): Boolean {
            // Vietnamese phone number validation
            val phonePattern = "^(\\+84|0)(3[2-9]|5[2689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$"
            return phoneNumber.matches(phonePattern.toRegex())
        }
    }

// Box Size Enum
enum class BoxSize(val value: Int) {
    SMALL(1),
    MEDIUM(2),
    LARGE(3),
    EXTRA_LARGE(4),
}

// Delivery Priority Enum
enum class DeliveryPriority(val value: Int) {
    STANDARD(0),
    EXPRESS(1),
}

// Data Classes
data class CreateOrderRequest(
    val fromLocation: String,
    val toLocation: String,
    val size: BoxSize,
    val priority: DeliveryPriority,
    val weight: String,
    val receiverPhone: String,
)

// MVI State
data class CreateOrderViewState(
    val isLoading: Boolean = false,
    val selectedSize: BoxSize = BoxSize.SMALL,
    val selectedPriority: DeliveryPriority = DeliveryPriority.EXPRESS,
    val weight: String = "1",
    val fromLocation: String? = null,
    val toLocation: String? = null,
    val idSource: String? = null,
    val fromLocationError: Int? = null,
    val toLocationError: Int? = null,
    val idDest: String? = null,
    val receiverPhoneError: Int? = null,
)

// MVI Events (User Actions)
sealed class CreateOrderEvent {
    data class SelectSize(val size: BoxSize) : CreateOrderEvent()

    data class SelectFromLocation(val locationName: String, val id: String) : CreateOrderEvent()

    data class SelectToLocation(val locationName: String, val id: String) : CreateOrderEvent()

    data class SelectPriority(val priority: DeliveryPriority) : CreateOrderEvent()

    data class CreateOrder(val weight: String, val receiverPhone: String, val products: List<Product>) : CreateOrderEvent()
}

sealed class CreateOrderViewEvent {
    data class ShowMessage(val message: String) : CreateOrderViewEvent()

    data class ShowMessageRes(val messageResId: Int) : CreateOrderViewEvent()

    object NavigateBack : CreateOrderViewEvent()

    data class NavigateToOrderSuccess(val order: CreateOrderRequest, val response: OrderCreateResponse?) : CreateOrderViewEvent()
}
