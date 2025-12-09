package com.delivery.setting.ui.tracking

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.pref.RxPreferences
import com.delivery.core.repositopry.FlightLaneRepository
import com.delivery.core.repositopry.LockerRepository
import com.delivery.setting.R
import com.delivery.setting.model.Order
import com.delivery.setting.model.OrderDisplayStyle
import com.delivery.setting.model.OrderSegment
import com.delivery.setting.repository.CommandRepository
import com.delivery.setting.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DeliveryTrackingViewModel @Inject constructor(
    private val trackingRepository: TrackingRepository,
    private val commandRepository: CommandRepository,
    private val flightLaneRepository: FlightLaneRepository,
    private val lockerRepository: LockerRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {
    var currentLockerId = ""

    init {
        viewModelScope.launch {
            currentLockerId = rxPreferences.getSelectedLockerId().first() ?: ""
        }
    }

    private val _uiState = MutableStateFlow(DeliveryTrackingViewState())
    val uiState: StateFlow<DeliveryTrackingViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DeliveryTrackingViewEvent>()
    val uiEvent: MutableSharedFlow<DeliveryTrackingViewEvent> = _uiEvent

    fun handleEvent(event: DeliveryTrackingEvent) {
        when (event) {
            is DeliveryTrackingEvent.LoadOrderData -> loadOrderData(event.orderItem)
            is DeliveryTrackingEvent.LoadFlightLane -> loadFlightLane(event.laneId)
            is DeliveryTrackingEvent.CancelDelivery -> cancelDelivery()
            is DeliveryTrackingEvent.OrderAction -> handleOrderAction(event.order, event.displayStyle)
        }
    }

    private fun loadOrderData(orderItem: Order) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Set order data
                _uiState.value = _uiState.value.copy(orderItem = orderItem)

                // Process segments if available
                if (!orderItem.segments.isNullOrEmpty()) {
                    Timber.d("Processing ${orderItem.segments.size} segments for order ${orderItem.id}")
                    loadSegmentRoutes(orderItem.segments)
                } else {
                    // Fallback: use source/dest from order directly
                    Timber.d("No segments found, using direct source/dest for order ${orderItem.id}")
                    //  loadDirectRoute(orderItem)
                }

                // Load mock drone position
                loadMockDronePosition()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _uiEvent.emit(
                    DeliveryTrackingViewEvent.ShowMessageRes(
                        R.string.error_load_data_failed,
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    private fun loadFlightLane(laneId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRouteLoading = true)

                flightLaneRepository.getFlightLaneById(laneId).collect { flightLanes ->
                    if (flightLanes.isNotEmpty()) {
                        val flightLane = flightLanes.first()
                        val routePoints = flightLane.points.map { point ->
                            org.maplibre.android.geometry.LatLng(
                                point[1],
                                point[0]
                            ) // Convert [longitude, latitude] to LatLng
                        }

                        _uiState.value = _uiState.value.copy(
                            routePoints = routePoints,
                            flightLane = flightLane,
                            isRouteLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isRouteLoading = false)
                        _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_no_route_data))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRouteLoading = false)
                _uiEvent.emit(
                    DeliveryTrackingViewEvent.ShowMessageRes(
                        R.string.error_load_route_failed,
                        e.message ?: "Unknown error"
                    )
                )
            }
        }
    }

    /**
     * Load route data for each segment
     */
    private suspend fun loadSegmentRoutes(segments: List<OrderSegment>) {
        try {
            val segmentRoutes = mutableListOf<SegmentRoute>()
            val lockerPositions = mutableListOf<LockerPosition>()
            val allRoutePoints = mutableListOf<org.maplibre.android.geometry.LatLng>()

            segments.forEachIndexed { index, segment ->
                Timber.d("Processing segment $index: ${segment.source} -> ${segment.dest}")

                // Get locker positions
                val startPosition = getLockerPosition(segment.source)
                val endPosition = getLockerPosition(segment.dest)

                if (startPosition != null && endPosition != null) {
                    // Get flight lane route if available
                    val routePoints = if (!segment.flightLaneId.isNullOrEmpty()) {
                        getFlightLaneRoute(segment.flightLaneId!!)
                    } else {
                        // Direct route between two points
                        listOf(startPosition.position, endPosition.position)
                    }

                    // Determine segment status
                    val status = when {
                        index < _uiState.value.currentSegmentIndex -> SegmentStatus.COMPLETED
                        index == _uiState.value.currentSegmentIndex -> SegmentStatus.IN_PROGRESS
                        else -> SegmentStatus.PENDING
                    }

                    val segmentRoute = SegmentRoute(
                        segment = segment,
                        routePoints = routePoints,
                        startLocker = startPosition,
                        endLocker = endPosition,
                        status = status
                    )

                    segmentRoutes.add(segmentRoute)
                    allRoutePoints.addAll(routePoints)

                    // Add unique locker positions
                    if (!lockerPositions.any { it.lockerId == startPosition.lockerId }) {
                        lockerPositions.add(startPosition)
                    }
                    if (!lockerPositions.any { it.lockerId == endPosition.lockerId }) {
                        lockerPositions.add(endPosition)
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                segmentRoutes = segmentRoutes,
                lockerPositions = lockerPositions,
                routePoints = allRoutePoints,
                isLoading = false
            )

        } catch (e: Exception) {
            Timber.e(e, "Error loading segment routes")
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.emit(
                DeliveryTrackingViewEvent.ShowMessageRes(
                    R.string.error_load_data_failed,
                    e.message
                )
            )
        }
    }

    /**
     * Fallback method for orders without segments
     */
//    private suspend fun loadDirectRoute(orderItem: Order) {
//        try {
//            val lockerPositions = mutableListOf<LockerPosition>()
//            val routePoints = mutableListOf<org.maplibre.android.geometry.LatLng>()
//
//            Timber.d("Loading direct route for order ${orderItem.id}: ${orderItem.idSource} -> ${orderItem.idDest}")
//
//            // Get source position
//            val sourcePosition = orderItem.idSource?.let { sourceId ->
//                Timber.d("Getting source position for: $sourceId")
//                getLockerPosition(sourceId)
//            }
//
//            val destPosition = orderItem.idDest?.let { destId ->
//                Timber.d("Getting dest position for: $destId")
//                getLockerPosition(destId)
//            }
//
//            sourcePosition?.let {
//                lockerPositions.add(it)
//                Timber.d("Added source position: ${it.lockerName} at ${it.position}")
//            }
//            destPosition?.let {
//                lockerPositions.add(it)
//                Timber.d("Added dest position: ${it.lockerName} at ${it.position}")
//            }
//
//            // Create direct route
//            if (sourcePosition != null && destPosition != null) {
//                routePoints.addAll(listOf(sourcePosition.position, destPosition.position))
//                Timber.d("Created direct route with ${routePoints.size} points")
//            } else {
//                Timber.w("Could not create route: sourcePosition=$sourcePosition, destPosition=$destPosition")
//            }
//
//            _uiState.value = _uiState.value.copy(
//                lockerPositions = lockerPositions,
//                routePoints = routePoints,
//                isLoading = false
//            )
//
//        } catch (e: Exception) {
//            Timber.e(e, "Error loading direct route")
//            _uiState.value = _uiState.value.copy(isLoading = false)
//            _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_load_data_failed, e.message))
//        }
//    }

    /**
     * Get locker position by ID
     */
    private suspend fun getLockerPosition(lockerId: String?): LockerPosition? {
        if (lockerId.isNullOrEmpty() || lockerId == "string") {
            Timber.d("Skipping invalid locker ID: $lockerId")
            return null
        }

        return try {
            Timber.d("Getting locker position for ID: $lockerId")
            val position = lockerRepository.getLockerPositionById(lockerId)
            val name = lockerRepository.getLockerNameById(lockerId)

            if (position != null) {
                val lockerPosition = LockerPosition(lockerId, name, position)
                Timber.d("Successfully created LockerPosition: ${lockerPosition.lockerName} at ${lockerPosition.position}")
                lockerPosition
            } else {
                Timber.w("Could not get position for locker $lockerId")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting locker position for $lockerId")
            null
        }
    }

    /**
     * Get flight lane route points
     */
    private suspend fun getFlightLaneRoute(flightLaneId: String): List<org.maplibre.android.geometry.LatLng> {
        return try {
            var routePoints = listOf<org.maplibre.android.geometry.LatLng>()
            flightLaneRepository.getFlightLaneById(flightLaneId).collect { flightLanes ->
                if (flightLanes.isNotEmpty()) {
                    val flightLane = flightLanes.first()
                    routePoints = flightLane.points.map { point ->
                        org.maplibre.android.geometry.LatLng(
                            point[1],
                            point[0]
                        ) // Convert [longitude, latitude] to LatLng
                    }
                }
            }
            routePoints
        } catch (e: Exception) {
            Timber.e(e, "Error getting flight lane route for $flightLaneId")
            emptyList()
        }
    }

    /**
     * Load mock drone position (hardcode tạm)
     */
    private fun loadMockDronePosition() {
        viewModelScope.launch {
            try {
                // Mock: Drone position somewhere on the current route
                val routePoints = _uiState.value.routePoints
                if (routePoints.isNotEmpty()) {
                    // Place drone at 30% of the current segment route
                    val currentSegmentIndex = _uiState.value.currentSegmentIndex
                    val segmentRoutes = _uiState.value.segmentRoutes

                    if (currentSegmentIndex < segmentRoutes.size) {
                        val currentSegmentRoute = segmentRoutes[currentSegmentIndex].routePoints
                        if (currentSegmentRoute.isNotEmpty()) {
                            val progressIndex = (currentSegmentRoute.size * 0.3).toInt()
                            val dronePosition = currentSegmentRoute.getOrNull(progressIndex)
                                ?: currentSegmentRoute.first()

                            _uiState.value = _uiState.value.copy(dronePosition = dronePosition)
                            Timber.d("Mock drone position set to: $dronePosition")
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading mock drone position")
            }
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
                            _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.command_start_success))
                            // Refresh order data to update UI
                            loadOrderData(order)
                        }
                    }
                    OrderDisplayStyle.UNLOAD -> {
                        // Send FINISH command (command_type = 2)
                        val segmentIndex = order.segments.lastOrNull()?.segmentIndex ?: 1
                        commandRepository.sendFinishCommand(order, segmentIndex).collect { response ->
                            Timber.d("FINISH command sent successfully: ${response.id}")
                            _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.command_finish_success))
                            // Refresh order data to update UI
                            loadOrderData(order)
                        }
                    }
                    else -> {
                        // No action for other display styles
                        Timber.w("No action defined for display style: $displayStyle")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling order action for order: ${order.id}")
                _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.command_error, e.message ?: "Unknown error"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun cancelDelivery() {
        _uiState.value.orderItem?.let { orderItem ->
            viewModelScope.launch {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    trackingRepository.cancelDelivery(orderItem.id)
                    _uiEvent.emit(DeliveryTrackingViewEvent.NavigateBack)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _uiEvent.emit(
                        DeliveryTrackingViewEvent.ShowMessageRes(
                            R.string.error_cancel_delivery_failed,
                            e.message ?: "Unknown error"
                        )
                    )
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
}

// MVI State
data class DeliveryTrackingViewState(
    val isLoading: Boolean = false,
    val isRouteLoading: Boolean = false,
    val orderItem: Order? = null,
    val routePoints: List<org.maplibre.android.geometry.LatLng> = emptyList(),
    val flightLane: com.delivery.core.model.network.FlightLaneDto? = null,
    val segmentRoutes: List<SegmentRoute> = emptyList(), // Danh sách route cho từng segment
    val lockerPositions: List<LockerPosition> = emptyList(), // Tọa độ các locker
    val dronePosition: org.maplibre.android.geometry.LatLng? = null, // Vị trí drone hiện tại
    val currentSegmentIndex: Int = 0 // Segment hiện tại drone đang bay
)

// Data class cho segment route
data class SegmentRoute(
    val segment: OrderSegment,
    val routePoints: List<org.maplibre.android.geometry.LatLng>, // Đường bay của segment này
    val startLocker: LockerPosition,
    val endLocker: LockerPosition,
    val status: SegmentStatus
)

// Data class cho vị trí locker
data class LockerPosition(
    val lockerId: String,
    val lockerName: String,
    val position: org.maplibre.android.geometry.LatLng
)

// Status của segment
enum class SegmentStatus {
    PENDING,    // Chưa bắt đầu
    IN_PROGRESS, // Đang bay
    COMPLETED   // Hoàn thành
}

// MVI Events (User Actions)
sealed class DeliveryTrackingEvent {
    data class LoadOrderData(val orderItem: Order) : DeliveryTrackingEvent()
    data class LoadFlightLane(val laneId: String) : DeliveryTrackingEvent()
    data class OrderAction(val order: Order, val displayStyle: OrderDisplayStyle) : DeliveryTrackingEvent()
    object CancelDelivery : DeliveryTrackingEvent()
}

// MVI View Events (UI Actions)
sealed class DeliveryTrackingViewEvent {
    data class ShowMessage(val message: String) : DeliveryTrackingViewEvent()
    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) :
        DeliveryTrackingViewEvent()

    object NavigateBack : DeliveryTrackingViewEvent()
}
