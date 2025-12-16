package com.delivery.setting.ui.createorder

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.setting.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor() : BaseViewModel() {

    private val _uiState = MutableStateFlow(CreateOrderViewState())
    val uiState: StateFlow<CreateOrderViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreateOrderViewEvent>()
    val uiEvent: SharedFlow<CreateOrderViewEvent> = _uiEvent.asSharedFlow()

    fun handleEvent(event: CreateOrderEvent) {
        when (event) {
            is CreateOrderEvent.SelectSize -> selectSize(event.size)
            is CreateOrderEvent.CreateOrder -> createOrder(
                event.fromLocation,
                event.toLocation,
                event.recipientName,
                event.recipientPhone,
                event.packageType,
                event.weight
            )
        }
    }

    private fun selectSize(size: BoxSize) {
        val weight = when (size) {
            BoxSize.SMALL -> "1"
            BoxSize.MEDIUM -> "2"
            BoxSize.LARGE -> "3"
            BoxSize.EXTRA_LARGE -> "4"
        }
        
        _uiState.value = _uiState.value.copy(
            selectedSize = size,
            weight = weight
        )
    }

    private fun createOrder(
        fromLocation: String,
        toLocation: String,
        recipientName: String,
        recipientPhone: String,
        packageType: String,
        weight: String
    ) {
        isLoading.postValue(true)
        viewModelScope.launch {
            if (!validateInputs(fromLocation, toLocation, recipientName, recipientPhone, packageType)) {
                isLoading.postValue(false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Simulate API call
                delay(1500)
                isLoading.postValue(false)
                
                // Create order object
                val order = CreateOrderRequest(
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    recipientName = recipientName,
                    recipientPhone = recipientPhone,
                    packageType = packageType,
                    size = _uiState.value.selectedSize,
                    weight = weight
                )
                
                _uiEvent.emit(CreateOrderViewEvent.ShowMessage("Đơn hàng đã được tạo thành công"))
                _uiEvent.emit(CreateOrderViewEvent.NavigateToOrderSuccess(order))
                
            } catch (exception: Exception) {
                _uiEvent.emit(CreateOrderViewEvent.ShowMessage("Đã xảy ra lỗi. Vui lòng thử lại"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun validateInputs(
        fromLocation: String,
        toLocation: String,
        recipientName: String,
        recipientPhone: String,
        packageType: String
    ): Boolean {
        var isValid = true
        var fromLocationError: Int? = null
        var toLocationError: Int? = null
        var recipientNameError: Int? = null
        var recipientPhoneError: Int? = null
        var packageTypeError: Int? = null

        // Validate from location
        if (fromLocation.isEmpty()) {
            fromLocationError = R.string.error_from_location_empty
            isValid = false
        }

        // Validate to location
        if (toLocation.isEmpty()) {
            toLocationError = R.string.error_to_location_empty
            isValid = false
        }

        // Validate recipient name
        if (recipientName.isEmpty()) {
            recipientNameError = R.string.error_recipient_name_empty
            isValid = false
        } else if (recipientName.length < 2) {
            recipientNameError = R.string.error_recipient_name_too_short
            isValid = false
        }

        // Validate recipient phone
        if (recipientPhone.isEmpty()) {
            recipientPhoneError = R.string.error_recipient_phone_empty
            isValid = false
        } else if (!isValidPhoneNumber(recipientPhone)) {
            recipientPhoneError = R.string.error_recipient_phone_invalid_format
            isValid = false
        }

        // Validate package type
        if (packageType.isEmpty()) {
            packageTypeError = R.string.error_package_type_empty
            isValid = false
        }

        _uiState.value = _uiState.value.copy(
            fromLocationError = fromLocationError,
            toLocationError = toLocationError,
            recipientNameError = recipientNameError,
            recipientPhoneError = recipientPhoneError,
            packageTypeError = packageTypeError
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
    EXTRA_LARGE(4)
}

// Data Classes
data class CreateOrderRequest(
    val fromLocation: String,
    val toLocation: String,
    val recipientName: String,
    val recipientPhone: String,
    val packageType: String,
    val size: BoxSize,
    val weight: String
)

// MVI State
data class CreateOrderViewState(
    val isLoading: Boolean = false,
    val selectedSize: BoxSize = BoxSize.SMALL,
    val weight: String = "1",
    val fromLocationError: Int? = null,
    val toLocationError: Int? = null,
    val recipientNameError: Int? = null,
    val recipientPhoneError: Int? = null,
    val packageTypeError: Int? = null
)

// MVI Events (User Actions)
sealed class CreateOrderEvent {
    data class SelectSize(val size: BoxSize) : CreateOrderEvent()
    data class CreateOrder(
        val fromLocation: String,
        val toLocation: String,
        val recipientName: String,
        val recipientPhone: String,
        val packageType: String,
        val weight: String
    ) : CreateOrderEvent()
}

sealed class CreateOrderViewEvent {
    data class ShowMessage(val message: String) : CreateOrderViewEvent()
    object NavigateBack : CreateOrderViewEvent()
    data class NavigateToOrderSuccess(val order: CreateOrderRequest) : CreateOrderViewEvent()
}
