package com.delivery.setting.ui.tracking

import androidx.lifecycle.viewModelScope
import com.delivery.core.base.BaseViewModel
import com.delivery.core.model.mocking.SegmentDto
import com.delivery.setting.R
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.repository.FlightLaneRepository
import com.delivery.setting.repository.LockerRepository
import com.delivery.setting.repository.OrderRepository
import com.delivery.setting.repository.TrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DeliveryTrackingViewModel
@Inject
constructor(
    private val orderRepository: OrderRepository,
    private val trackingRepository: TrackingRepository,
    private val flightLaneRepository: FlightLaneRepository,
    private val lockerRepository: LockerRepository,
) : BaseViewModel() {
    private val _uiState = MutableStateFlow(DeliveryTrackingViewState())
    val uiState: StateFlow<DeliveryTrackingViewState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DeliveryTrackingViewEvent>()
    val uiEvent: MutableSharedFlow<DeliveryTrackingViewEvent> = _uiEvent

    // Job for real-time drone position tracking
    private var droneTrackingJob: Job? = null
    private var fetchOrderJob: Job? = null

    fun handleEvent(event: DeliveryTrackingEvent) {
        when (event) {
            is DeliveryTrackingEvent.LoadOrderData -> loadOrderData(event.orderItem)
            is DeliveryTrackingEvent.LoadFlightLane -> loadFlightLane(event.laneId)
            is DeliveryTrackingEvent.CancelDelivery -> cancelDelivery()
            is DeliveryTrackingEvent.StartDroneTracking -> startDroneTracking(event.droneId)
            is DeliveryTrackingEvent.StopDroneTracking -> stopDroneTracking()
        }
    }

    private fun loadOrderData(orderItem: OrderHistoryItem) {
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
                    loadDirectRoute(orderItem)
                }
                startFetchOrder(orderItem.id)

                // Start real-time drone tracking if drone ID is available
                orderItem.droneId?.let { droneId ->
                    // TKL
                    startDroneTracking(droneId)
                    //startDroneTracking("1866ASDD0000VT2")
                } ?: run {
                    // Fallback to mock position if no drone ID
                    //  loadMockDronePosition()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_load_data_failed, e.message ?: "Unknown error"))
            }
        }
    }

    private fun loadFlightLane(laneId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isRouteLoading = true)

                flightLaneRepository.getFlightLaneById(laneId).collect { flightLane ->
                    if (flightLane != null) {
                        val routePoints =
                            flightLane.position.map { position ->
                                LatLng(position.latitude, position.longitude) // Convert FlightPosition to LatLng
                            }

                        _uiState.value =
                            _uiState.value.copy(
                                routePoints = routePoints,
                                flightLane = flightLane,
                                isRouteLoading = false,
                            )
                    } else {
                        _uiState.value = _uiState.value.copy(isRouteLoading = false)
                        _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_no_route_data))
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isRouteLoading = false)
                _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_load_route_failed, e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Load route data for each segment
     */
    private suspend fun loadSegmentRoutes(segments: List<SegmentDto>) {
        try {
            val segmentRoutes = mutableListOf<SegmentRoute>()
            val lockerPositions = mutableListOf<LockerPosition>()
            val allRoutePoints = mutableListOf<LatLng>()

            segments.forEachIndexed { index, segment ->
                Timber.d("Processing segment $index: ${segment.source} -> ${segment.dest}")

                // Get locker positions
                val startPosition = getLockerPosition(segment.source)
                val endPosition = getLockerPosition(segment.dest)

                if (startPosition != null && endPosition != null) {
                    // Get flight lane route if available
                    val routePoints =
                        if (!segment.flightLaneId.isNullOrEmpty()) {
                            getFlightLaneRoute(segment.flightLaneId!!)
                        } else {
                            // Direct route between two points
                            listOf(startPosition.position, endPosition.position)
                        }

                    // Determine segment status
                    val status =
                        when {
                            index < _uiState.value.currentSegmentIndex -> SegmentStatus.COMPLETED
                            index == _uiState.value.currentSegmentIndex -> SegmentStatus.IN_PROGRESS
                            else -> SegmentStatus.PENDING
                        }

                    val segmentRoute =
                        SegmentRoute(
                            segment = segment,
                            routePoints = routePoints,
                            startLocker = startPosition,
                            endLocker = endPosition,
                            status = status,
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

            _uiState.value =
                _uiState.value.copy(
                    segmentRoutes = segmentRoutes,
                    lockerPositions = lockerPositions,
                    routePoints = allRoutePoints,
                    isLoading = false,
                )
        } catch (e: Exception) {
            Timber.e(e, "Error loading segment routes")
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_load_data_failed, e.message))
        }
    }

    /**
     * Fallback method for orders without segments
     */
    private suspend fun loadDirectRoute(orderItem: OrderHistoryItem) {
        try {
            val lockerPositions = mutableListOf<LockerPosition>()
            val routePoints = mutableListOf<LatLng>()

            Timber.d("Loading direct route for order ${orderItem.id}: ${orderItem.idSource} -> ${orderItem.idDest}")

            // Get source position
            val sourcePosition =
                orderItem.idSource?.let { sourceId ->
                    Timber.d("Getting source position for: $sourceId")
                    getLockerPosition(sourceId)
                }

            val destPosition =
                orderItem.idDest?.let { destId ->
                    Timber.d("Getting dest position for: $destId")
                    getLockerPosition(destId)
                }

            sourcePosition?.let {
                lockerPositions.add(it)
                Timber.d("Added source position: ${it.lockerName} at ${it.position}")
            }
            destPosition?.let {
                lockerPositions.add(it)
                Timber.d("Added dest position: ${it.lockerName} at ${it.position}")
            }

            // Create direct route
            if (sourcePosition != null && destPosition != null) {
                routePoints.addAll(listOf(sourcePosition.position, destPosition.position))
                Timber.d("Created direct route with ${routePoints.size} points")
            } else {
                Timber.w("Could not create route: sourcePosition=$sourcePosition, destPosition=$destPosition")
            }

            _uiState.value =
                _uiState.value.copy(
                    lockerPositions = lockerPositions,
                    routePoints = routePoints,
                    isLoading = false,
                )
        } catch (e: Exception) {
            Timber.e(e, "Error loading direct route")
            _uiState.value = _uiState.value.copy(isLoading = false)
            _uiEvent.emit(DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_load_data_failed, e.message))
        }
    }

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
    private suspend fun getFlightLaneRoute(flightLaneId: String): List<LatLng> {
        return try {
            var routePoints = listOf<LatLng>()
            flightLaneRepository.getFlightLaneById(flightLaneId).collect { flightLane ->
                if (flightLane != null) {
                    routePoints =
                        flightLane.position.map { position ->
                            LatLng(position.latitude, position.longitude) // Convert FlightPosition to LatLng
                        }
                }
            }
            routePoints
        } catch (e: Exception) {
            Timber.e(e, "Error getting flight lane route for $flightLaneId")
            emptyList()
        }
    }

    private fun startFetchOrder(orderId: String) {
        // Stop existing tracking job if any
        stopFetchOrderJob()

        fetchOrderJob =
            viewModelScope.launch {
                try {
                    Timber.d("Starting order fetching for order ID: $orderId")

                    while (true) {
                        try {
                            // Call API to get current drone position
                            val orderItem = orderRepository.getOrderById(orderId)
                            if (orderItem != null) {
                                // TKL DEMO
                                /*val number = Random.nextInt(0, 5)
                                if (number == 0) {
                                    orderItem.status = OrderStatus.PENDING
                                } else if (number == 1) {
                                    orderItem.status = OrderStatus.CONFIRMED
                                } else if (number == 2) {
                                    orderItem.status = OrderStatus.IN_DELIVERY
                                } else if (number == 3) {
                                    orderItem.status = OrderStatus.DELIVERED
                                } else if (number == 4) {
                                    orderItem.status = OrderStatus.CANCEL
                                }*/
                                _uiState.value = _uiState.value.copy(orderItem = orderItem)
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error fetching order for $orderId")
                            // Continue tracking even if one call fails
                        }

                        // Wait 5 seconds before next update
                        delay(5000)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in drone tracking job")
                }
            }
    }

    /**
     * Stop real-time fetch order
     */
    private fun stopFetchOrderJob() {
        fetchOrderJob?.cancel()
        fetchOrderJob = null
        Timber.d("Fetch order stopped")
    }

    /**
     * Start real-time drone position tracking
     * Updates drone position every 5 seconds
     */
    private fun startDroneTracking(droneId: String) {
        // Stop existing tracking job if any
        stopDroneTracking()

        droneTrackingJob =
            viewModelScope.launch {
                try {
                    Timber.d("Starting drone tracking for drone ID: $droneId")

                    while (true) {
                        try {
                            // Call API to get current drone position
                            val response = trackingRepository.getDroneCurrentPosition(droneId)

                            // Convert to MapLibre LatLng
                            val dronePosition = trackingRepository.convertToMapLibreLatLng(response)

                            // Update state with new position
                            _uiState.value =
                                _uiState.value.copy(
                                    dronePosition = dronePosition,
                                    droneHeading = response.position.heading,
                                    droneSpeed = response.position.speed,
                                    droneLastUpdated = response.updatedAt,
                                )

                            Timber.d("Drone position updated: $dronePosition, heading: ${response.position.heading}, speed: ${response.position.speed}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error getting drone position for $droneId")
                            // Continue tracking even if one call fails
                        }

                        // Wait 5 seconds before next update
                        delay(1000)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error in drone tracking job")
                }
            }
    }

    /**
     * Stop real-time drone position tracking
     */
    private fun stopDroneTracking() {
        droneTrackingJob?.cancel()
        droneTrackingJob = null
        Timber.d("Drone tracking stopped")
    }

    /**
     * Load mock drone position (fallback when no drone ID available)
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
                            val dronePosition = currentSegmentRoute.getOrNull(progressIndex) ?: currentSegmentRoute.first()

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
                        DeliveryTrackingViewEvent.ShowMessageRes(R.string.error_cancel_delivery_failed, e.message ?: "Unknown error"),
                    )
                } finally {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Stop drone tracking when ViewModel is cleared
        stopDroneTracking()
        Timber.d("DeliveryTrackingViewModel cleared, drone tracking stopped")
    }
}

// MVI State
data class DeliveryTrackingViewState(
    val isLoading: Boolean = false,
    val isRouteLoading: Boolean = false,
    val orderItem: OrderHistoryItem? = null,
    val routePoints: List<LatLng> = emptyList(),
    val flightLane: com.delivery.core.model.network.FlightLaneDto? = null,
    val segmentRoutes: List<SegmentRoute> = emptyList(), // Danh sách route cho từng segment
    val lockerPositions: List<LockerPosition> = emptyList(), // Tọa độ các locker
    val dronePosition: LatLng? = null, // Vị trí drone hiện tại
    val droneHeading: Double? = null, // Hướng bay của drone (degrees)
    val droneSpeed: Double? = null, // Tốc độ drone (m/s)
    val droneLastUpdated: Long? = null, // Thời gian cập nhật cuối
    val currentSegmentIndex: Int = 0, // Segment hiện tại drone đang bay
)

// Data class cho segment route
data class SegmentRoute(
    val segment: SegmentDto,
    val routePoints: List<LatLng>, // Đường bay của segment này
    val startLocker: LockerPosition,
    val endLocker: LockerPosition,
    val status: SegmentStatus,
)

// Data class cho vị trí locker
data class LockerPosition(
    val lockerId: String,
    val lockerName: String,
    val position: LatLng,
)

// Status của segment
enum class SegmentStatus {
    PENDING, // Chưa bắt đầu
    IN_PROGRESS, // Đang bay
    COMPLETED, // Hoàn thành
}

// MVI Events (User Actions)
sealed class DeliveryTrackingEvent {
    data class LoadOrderData(val orderItem: OrderHistoryItem) : DeliveryTrackingEvent()

    data class LoadFlightLane(val laneId: String) : DeliveryTrackingEvent()

    object CancelDelivery : DeliveryTrackingEvent()

    data class StartDroneTracking(val droneId: String) : DeliveryTrackingEvent()

    object StopDroneTracking : DeliveryTrackingEvent()
}

// MVI View Events (UI Actions)
sealed class DeliveryTrackingViewEvent {
    data class ShowMessage(val message: String) : DeliveryTrackingViewEvent()

    data class ShowMessageRes(val messageResId: Int, val args: Any? = null) : DeliveryTrackingViewEvent()

    object NavigateBack : DeliveryTrackingViewEvent()
}
