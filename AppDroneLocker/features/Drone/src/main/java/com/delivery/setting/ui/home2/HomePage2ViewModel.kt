package com.delivery.setting.ui.home2

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.R
import com.delivery.setting.model.Segment
import com.delivery.setting.model.SegmentCommandAction
import com.delivery.setting.repository.CommandRepository
import com.delivery.setting.repository.SegmentRepository
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
class HomePage2ViewModel @Inject constructor(
    private val segmentRepository: SegmentRepository,
    private val commandRepository: CommandRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    private var currentLockerId: String = ""

    private val _uiState = MutableStateFlow(HomePage2ViewState())
    val uiState: StateFlow<HomePage2ViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<HomePage2ViewEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Segments data
    private val _segments = MutableStateFlow<List<Segment>>(emptyList())
    val segments: StateFlow<List<Segment>> = _segments.asStateFlow()

    fun handleEvent(event: HomePage2Event) {
        when (event) {
            is HomePage2Event.LoadData -> loadData()
            is HomePage2Event.RefreshSegments -> refreshSegments()
            is HomePage2Event.SelectSegment -> selectSegment(event.segmentId)
            is HomePage2Event.SegmentAction -> handleSegmentAction(event.segment,event.segmentCommand)
        }
    }

    private fun loadData() {
        loadCurrentLockerId()
        loadSegments()
    }

    private fun loadCurrentLockerId() {
        viewModelScope.launch {
            try {
                currentLockerId = rxPreferences.getSelectedLockerId().first() ?: ""
                Timber.d("HomePage2ViewModel: Loaded current locker ID: $currentLockerId")
            } catch (e: Exception) {
                currentLockerId = ""
                Timber.e(e, "HomePage2ViewModel: Failed to load current locker ID")
            }
        }
    }

    private fun refreshSegments() {
        loadSegments()
    }

    private fun selectSegment(segmentId: String) {
        viewModelScope.launch {
            // Handle segment selection if needed
            Timber.d("HomePage2ViewModel: Selected segment: $segmentId")
        }
    }

    private fun handleSegmentAction(segment: Segment, action: SegmentCommandAction) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                when(action) {
                    SegmentCommandAction.START -> {
                        commandRepository.sendStartCommandForSegment(segment).collect { response ->
                            Timber.d("HomePage2ViewModel: START command sent successfully: ${response.id}")
                            _uiEvent.emit(HomePage2ViewEvent.ShowMessageRes(R.string.command_start_success))
                            // Refresh segments to update UI
                            refreshSegments()
                        }
                    }
                    SegmentCommandAction.END -> {
                        commandRepository.sendFinishCommandForSegment(segment).collect { response ->
                            Timber.d("HomePage2ViewModel: FINISH command sent successfully: ${response.id}")
                            _uiEvent.emit(HomePage2ViewEvent.ShowMessageRes(R.string.command_finish_success))
                            // Refresh segments to update UI
                            refreshSegments()
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(
                    e,
                    "HomePage2ViewModel: Error handling segment action for segment: ${segment.segmentIndex}"
                )
                _uiEvent.emit(
                    HomePage2ViewEvent.ShowMessageRes(
                        R.string.command_error,
                        e.message ?: "Unknown error"
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun updatePendingSegmentsCount() {
        val segments = _segments.value
        val pendingCount = segments.count { segment ->
            val displayStyle = segment.getDisplayStyle()
            displayStyle == com.delivery.setting.model.SegmentDisplayStyle.SEND ||
                    displayStyle == com.delivery.setting.model.SegmentDisplayStyle.UNLOAD
        }
        _uiState.value = _uiState.value.copy(pendingSegmentsCount = pendingCount)
    }

    private fun loadSegments() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                segmentRepository.getSegmentsList().collect { segments ->
                    _segments.value = segments
                    updatePendingSegmentsCount()
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

data class HomePage2ViewState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingSegmentsCount: Int = 0
)

sealed class HomePage2Event {
    data object LoadData : HomePage2Event()
    data object RefreshSegments : HomePage2Event()
    data class SelectSegment(val segmentId: String) : HomePage2Event()
    data class SegmentAction(val segment: Segment, val segmentCommand: SegmentCommandAction) : HomePage2Event()
}

sealed class HomePage2ViewEvent {
    data class ShowMessage(val message: String) : HomePage2ViewEvent()
    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) : HomePage2ViewEvent()
    data class NavigateToSegmentDetail(val segmentId: String) : HomePage2ViewEvent()
}
