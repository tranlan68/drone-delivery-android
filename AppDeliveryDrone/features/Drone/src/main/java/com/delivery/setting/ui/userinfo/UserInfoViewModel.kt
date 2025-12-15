package com.delivery.setting.ui.userinfo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
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
class UserInfoViewModel
    @Inject
    constructor(
        val rxPreferences: RxPreferences,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(UserInfoViewState())
        val uiState: StateFlow<UserInfoViewState> = _uiState.asStateFlow()

        private val _uiEvent = MutableSharedFlow<UserInfoViewEvent>()
        val uiEvent: SharedFlow<UserInfoViewEvent> = _uiEvent.asSharedFlow()
        val edtNameLiveData = MutableLiveData<String>()
        val edtPhoneLiveData = MutableLiveData<String>()

        init {
            loadUserInfo()
        }

        fun handleEvent(event: UserInfoEvent) {
            when (event) {
                is UserInfoEvent.SaveUserInfo -> saveUserInfo(event.name, event.phoneNumber)
                is UserInfoEvent.LoadUserInfo -> loadUserInfo()
            }
        }

        fun logout() {
            viewModelScope.launch {
                rxPreferences.logout()
                _uiEvent.emit(UserInfoViewEvent.NavigateBack)
            }
        }

        private fun saveUserInfo(
            name: String,
            phoneNumber: String,
        ) {
            isLoading.postValue(true)
            viewModelScope.launch {
                if (!validateInputs(name, phoneNumber)) {
                    isLoading.postValue(false)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isLoading = true)

                try {
                    // Simulate API call
                    delay(1000)
                    isLoading.postValue(false)

                    // Update state with saved data
                    _uiState.value =
                        _uiState.value.copy(
                            name = name,
                            phoneNumber = phoneNumber,
                            isLoading = false,
                        )

                    _uiEvent.emit(UserInfoViewEvent.ShowMessage("Thông tin đã được cập nhật thành công"))
                    _uiEvent.emit(UserInfoViewEvent.NavigateBack)
                } catch (exception: Exception) {
                    _uiEvent.emit(UserInfoViewEvent.ShowMessage("Đã xảy ra lỗi. Vui lòng thử lại"))
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }

        private fun loadUserInfo() {
            viewModelScope.launch {
                // Simulate loading existing user data
                // In real app, this would load from repository/database
                _uiState.value =
                    _uiState.value.copy(
                        name = "Nguyễn Văn A",
                        phoneNumber = "0999999999",
                    )
            }
        }

        private fun validateInputs(
            name: String,
            phoneNumber: String,
        ): Boolean {
            var isValid = true
            var nameError: Int? = null
            var phoneError: Int? = null

            // Validate name
            if (name.isEmpty()) {
                nameError = R.string.error_name_empty
                isValid = false
            } else if (name.length < 2) {
                nameError = R.string.error_name_too_short
                isValid = false
            } else if (name.length > 50) {
                nameError = R.string.error_name_too_long
                isValid = false
            }

            // Validate phone number
            if (phoneNumber.isEmpty()) {
                phoneError = R.string.error_phone_empty
                isValid = false
            } else if (!isValidPhoneNumber(phoneNumber)) {
                phoneError = R.string.error_phone_invalid_format
                isValid = false
            }

            _uiState.value =
                _uiState.value.copy(
                    nameError = nameError,
                    phoneError = phoneError,
                )

            return isValid
        }

        private fun isValidPhoneNumber(phoneNumber: String): Boolean {
            // Vietnamese phone number validation
            val phonePattern = "^(\\+84|0)(3[2-9]|5[2689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$"
            return phoneNumber.matches(phonePattern.toRegex())
        }
    }

// MVI State
data class UserInfoViewState(
    val isLoading: Boolean = false,
    val name: String = "",
    val phoneNumber: String = "",
    val nameError: Int? = null,
    val phoneError: Int? = null,
)

// MVI Events (User Actions)
sealed class UserInfoEvent {
    data class SaveUserInfo(val name: String, val phoneNumber: String) : UserInfoEvent()

    object LoadUserInfo : UserInfoEvent()
}

// MVI View Events (Navigation/UI Effects)
sealed class UserInfoViewEvent {
    data class ShowMessage(val message: String) : UserInfoViewEvent()

    object NavigateBack : UserInfoViewEvent()
}
