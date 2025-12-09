package com.delivery.setting.ui.location

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.model.network.Locker
import com.delivery.core.utils.SingleLiveEvent
import com.delivery.setting.R
import com.delivery.setting.repository.LockerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SelectLocationLockerViewModel
    @Inject
    constructor(
        private val lockerRepository: LockerRepository,
    ) : BaseViewModel() {
        private val _uiState = MutableStateFlow(SelectLocationLockerViewState())
        val uiState: StateFlow<SelectLocationLockerViewState> = _uiState.asStateFlow()

        private val _uiEvent = SingleLiveEvent<SelectLocationLockerViewEvent>()
        val uiEvent: SingleLiveEvent<SelectLocationLockerViewEvent> = _uiEvent

        fun handleEvent(event: SelectLocationLockerEvent) {
            when (event) {
                is SelectLocationLockerEvent.LoadLockers -> loadLockers()
                is SelectLocationLockerEvent.SelectLocker -> selectLocker(event.locker)
                is SelectLocationLockerEvent.ConfirmSelection -> confirmSelection()
            }
        }

        private fun loadLockers() {
            isLoading.postValue(true)
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)

                    // Execute API call on IO dispatcher
                    withContext(Dispatchers.IO) {
                        lockerRepository.getLockers().collect { lockersList ->
                            // Switch back to Main dispatcher for UI updates
                            withContext(Dispatchers.Main) {
                                _uiState.value =
                                    _uiState.value.copy(
                                        lockers = lockersList,
                                        isLoading = false,
                                    )
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.postValue(
                        SelectLocationLockerViewEvent.ShowMessageRes(R.string.error_load_lockers_failed, e.message ?: "Unknown error"),
                    )
                } finally {
                    isLoading.postValue(false)
                }
            }
        }

        private fun selectLocker(locker: Locker) {
            _uiState.value = _uiState.value.copy(selectedLocker = locker)
        }

        private fun confirmSelection() {
            val selectedLocker = _uiState.value.selectedLocker
            if (selectedLocker != null) {
                _uiEvent.postValue(SelectLocationLockerViewEvent.NavigateBackWithResult(selectedLocker))
            } else {
                _uiEvent.postValue(SelectLocationLockerViewEvent.ShowMessageRes(R.string.error_no_locker_selected))
            }
        }

        fun getSelectedLocker(): Locker? {
            return _uiState.value.selectedLocker
        }

        suspend fun getLockerStatistics(): Map<String, Int> {
            return lockerRepository.getLockerStatistics()
        }
    }

// MVI State
data class SelectLocationLockerViewState(
    val isLoading: Boolean = false,
    val lockers: List<Locker> = emptyList(),
    val selectedLocker: Locker? = null,
)

// MVI Events (User Actions)
sealed class SelectLocationLockerEvent {
    data object LoadLockers : SelectLocationLockerEvent()

    data class SelectLocker(val locker: Locker) : SelectLocationLockerEvent()

    data object ConfirmSelection : SelectLocationLockerEvent()
}

// MVI View Events (UI Actions)
sealed class SelectLocationLockerViewEvent {
    data class ShowMessage(val message: String) : SelectLocationLockerViewEvent()

    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) : SelectLocationLockerViewEvent()

    data class NavigateBackWithResult(val locker: Locker) : SelectLocationLockerViewEvent()
}
