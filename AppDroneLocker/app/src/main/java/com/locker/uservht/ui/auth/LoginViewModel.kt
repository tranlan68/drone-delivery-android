package com.locker.uservht.ui.auth

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.model.network.mocking.Locker
import com.delivery.core.pref.RxPreferences
import com.delivery.core.repositopry.LockerRepository
import com.locker.uservht.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val lockerRepository: LockerRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    private val _uiState: MutableStateFlow<LoginViewState> = MutableStateFlow(LoginViewState())
    val uiState: StateFlow<LoginViewState> = _uiState.asStateFlow()

    private val _uiEvent: MutableSharedFlow<LoginViewEvent> = MutableSharedFlow()
    val uiEvent: SharedFlow<LoginViewEvent> = _uiEvent.asSharedFlow()

    fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.LoadLockers -> loadLockers()
            is LoginEvent.SelectLocker -> selectLocker(event.locker)
            is LoginEvent.PerformLogin -> performLogin()
        }
    }

    private fun loadLockers() {
        isLoading.postValue(true)
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                lockerRepository.getLockers().collect { lockers ->
                    _uiState.value = _uiState.value.copy(
                        lockers = lockers,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _uiEvent.emit(LoginViewEvent.ShowMessage("Không thể tải danh sách Locker"))
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    private fun selectLocker(locker: Locker) {
        _uiState.value = _uiState.value.copy(
            selectedLocker = locker,
            lockerError = null
        )
    }

    private fun performLogin() {
        viewModelScope.launch {
            val locker: Locker? = _uiState.value.selectedLocker
            if (locker == null) {
                _uiState.value = _uiState.value.copy(lockerError = R.string.error_locker_empty)
                return@launch
            }
            
            // Save selected locker ID to preferences
            try {
                rxPreferences.setSelectedLockerId(locker.id)
                _uiEvent.emit(LoginViewEvent.NavigateToHome)
                _uiEvent.emit(LoginViewEvent.ShowMessage("Đăng nhập thành công"))
            } catch (e: Exception) {
                _uiEvent.emit(LoginViewEvent.ShowMessage("Lỗi lưu thông tin locker: ${e.message}"))
            }
        }
    }
}

data class LoginViewState(
    val isLoading: Boolean = false,
    val lockers: List<Locker> = emptyList(),
    val selectedLocker: Locker? = null,
    val lockerError: Int? = null
)

sealed class LoginEvent {
    object LoadLockers : LoginEvent()
    data class SelectLocker(val locker: Locker) : LoginEvent()
    object PerformLogin : LoginEvent()
}

sealed class LoginViewEvent {
    data class ShowMessage(val message: String) : LoginViewEvent()
    object NavigateToHome : LoginViewEvent()
}