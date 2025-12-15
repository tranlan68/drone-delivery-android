package com.delivery.setting.ui.location

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.Constants
import com.delivery.core.utils.MapLibreUtils
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentSelectLocationLockerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SelectLocationLockerFragment :
    BaseFragment<FragmentSelectLocationLockerBinding, SelectLocationLockerViewModel>(
        R.layout.fragment_select_location_locker,
    ) {
    private val viewModel: SelectLocationLockerViewModel by viewModels()
    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var locationType: String = Constants.LocationType.PICKUP
    private var preselectedLockerName: String? = null
    private var hasZoomedToInitialLocation = false

    @Inject
    lateinit var appNavigation: DemoNavigation

    @Inject
    lateinit var mapLibreUtils: MapLibreUtils

    override fun getVM(): SelectLocationLockerViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        // Get location type from arguments
        arguments?.getString(Constants.LockerBundleKeys.LOCATION_TYPE)?.let { type ->
            locationType = type
        }

        // Get preselected locker name from arguments
        arguments?.getString(Constants.LockerBundleKeys.SELECTED_LOCKER_NAME)?.let { name ->
            preselectedLockerName = name
        }

        // Update toolbar title based on location type
        updateToolbarTitle()

        mapLibreUtils.initializeMapLibre()
        mapView = binding.mapView
        mapLibreUtils.setupMapView(mapView!!, savedInstanceState)
        initializeMap()

        // Load lockers when view is initialized
        viewModel.handleEvent(SelectLocationLockerEvent.LoadLockers)

        // Load locker statistics
        loadLockerStatistics()
    }

    private fun initializeMap() {
        mapLibreUtils.initializeMap(
            mapView = mapView!!,
            onMapReady = { map ->
                mapLibreMap = map
                setupMapClickListeners()
            },
            onStyleLoaded = { style ->
                moveCameraToDefaultLocation()

                // Draw initial markers if data is already loaded
                viewModel.uiState.value.lockers.takeIf { it.isNotEmpty() }?.let { lockers ->
                    drawLockerMarkers(style, lockers)

                    // Handle initial zoom after markers are drawn
                    handleInitialZoom(lockers)
                }
            },
        )
    }

    private fun setupMapClickListeners() {
        mapLibreMap?.addOnMapClickListener { point ->
            // Find the nearest locker to the clicked point
            val nearestLocker = findNearestLocker(point)
            if (nearestLocker != null) {
                viewModel.handleEvent(SelectLocationLockerEvent.SelectLocker(nearestLocker))
                updateSelectedLockerMarker(nearestLocker)
                getString(R.string.success_locker_selected, nearestLocker.lockerName).toast(
                    requireContext(),
                )
            } else {
                getString(R.string.error_no_locker_nearby).toast(requireContext())
            }
            true
        }
    }

    private fun findNearestLocker(clickedPoint: LatLng): com.delivery.core.model.network.Locker? {
        var nearestLocker: com.delivery.core.model.network.Locker? = null
        var minDistance = Double.MAX_VALUE

        viewModel.uiState.value.lockers.forEach { locker ->
            val lockerPoint = LatLng(locker.latitude, locker.longitude)
            val distance = calculateDistance(clickedPoint, lockerPoint)
            if (distance < minDistance && distance < 200) { // Within 1km
                minDistance = distance
                nearestLocker = locker
            }
        }

        return nearestLocker
    }

    private fun calculateDynamicClickRadius(lockers: List<com.delivery.core.model.network.Locker>): Double {
        if (lockers.size <= 1) return 1000.0 // Default 1km for single locker

        var minDistanceBetweenLockers = Double.MAX_VALUE

        // Find minimum distance between any two lockers
        for (i in lockers.indices) {
            for (j in i + 1 until lockers.size) {
                val locker1 = LatLng(lockers[i].latitude, lockers[i].longitude)
                val locker2 = LatLng(lockers[j].latitude, lockers[j].longitude)
                val distance = calculateDistance(locker1, locker2)
                if (distance < minDistanceBetweenLockers) {
                    minDistanceBetweenLockers = distance
                }
            }
        }

        // Set click radius to 40% of minimum distance between lockers
        // This ensures we don't accidentally select multiple nearby lockers
        val dynamicRadius = (minDistanceBetweenLockers * 0.4).coerceIn(50.0, 500.0) // Min 50m, Max 500m

        Timber.tag(
            "SelectLocationLocker",
        ).d("Dynamic click radius: ${dynamicRadius.toInt()}m (min distance between lockers: ${minDistanceBetweenLockers.toInt()}m)")

        return dynamicRadius
    }

    private fun calculateDistance(
        point1: LatLng,
        point2: LatLng,
    ): Double {
        val earthRadius = 6371000.0 // Earth's radius in meters
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLngRad = Math.toRadians(point2.longitude - point1.longitude)

        val a =
            kotlin.math.sin(deltaLatRad / 2) * kotlin.math.sin(deltaLatRad / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLngRad / 2) * kotlin.math.sin(deltaLngRad / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

        return earthRadius * c
    }

    private fun updateSelectedLockerMarker(locker: com.delivery.core.model.network.Locker) {
        mapLibreMap?.getStyle { style ->
            try {
                val sourceId = "selected-locker-source"
                val layerId = "selected-locker-layer"

                // Remove existing selected marker safely
                style.getLayer(layerId)?.let { style.removeLayer(it) }
                style.getSource(sourceId)?.let { style.removeSource(it) }

                // Add new selected marker using MapLibreUtils
                mapLibreUtils.addCircleMarker(
                    style = style,
                    sourceId = sourceId,
                    layerId = layerId,
                    position = LatLng(locker.latitude, locker.longitude),
                    color = Color.parseColor("#FF5722"),
                    radius = 8.0f,
                    strokeWidth = 3.0f,
                )

                // Move camera to selected locker
                mapLibreUtils.moveCameraToPosition(
                    mapLibreMap = mapLibreMap,
                    position = LatLng(locker.latitude, locker.longitude),
                    zoom = 16.0,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun drawLockerMarkers(
        style: Style,
        lockers: List<com.delivery.core.model.network.Locker>,
    ) {
        try {
            // Debug: Log locker data with hierarchy information
            Timber.tag("SelectLocationLocker").d("Drawing ${lockers.size} lockers (including nested lockers)")
            lockers.forEach { locker ->
                val isNestedLocker = locker.lockerName.contains("(") && locker.lockerName.contains(")")
                val lockerType = if (isNestedLocker) "NESTED" else "MAIN"
                Timber.tag("SelectLocationLocker")
                    .d("$lockerType Locker: ${locker.lockerName} at lat=${locker.latitude}, lng=${locker.longitude}")
            }

            // Remove existing markers safely (remove all individual locker markers)
            mapLibreUtils.removeMultipleLayersAndSources(style, "locker", 20)

            // Get currently selected locker from ViewModel
            val currentlySelectedLocker = viewModel.uiState.value.selectedLocker

            // Draw each locker marker individually with different colors for main vs nested lockers
            lockers.forEachIndexed { index, locker ->
                val sourceId = "locker-source-$index"
                val layerId = "locker-layer-$index"
                val isNestedLocker = locker.lockerName.contains("(") && locker.lockerName.contains(")")

                // Only highlight if this locker is currently selected
                if (currentlySelectedLocker != null && locker.id == currentlySelectedLocker.id) {
                    // Highlight currently selected locker
                    mapLibreUtils.addCircleMarker(
                        style = style,
                        sourceId = sourceId,
                        layerId = layerId,
                        position = LatLng(locker.latitude, locker.longitude),
                        color = Color.parseColor("#FF5722"), // Orange for selected
                        radius = 10.0f,
                        strokeWidth = 4.0f,
                    )
                } else {
                    // Regular locker marker with different colors for main vs nested
                    val markerColor =
                        if (isNestedLocker) {
                            Color.parseColor("#4CAF50") // Green for nested lockers
                        } else {
                            Color.parseColor("#2196F3") // Blue for main lockers
                        }

                    val markerRadius = if (isNestedLocker) 6.0f else 8.0f

                    mapLibreUtils.addCircleMarker(
                        style = style,
                        sourceId = sourceId,
                        layerId = layerId,
                        position = LatLng(locker.latitude, locker.longitude),
                        color = markerColor,
                        radius = markerRadius,
                        strokeWidth = 2.0f,
                    )
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }

    private fun moveCameraToDefaultLocation() {
        val defaultLocation = LatLng(21.0285, 105.8542) // Hanoi coordinates
        mapLibreUtils.moveCameraToPosition(mapLibreMap, defaultLocation, 12.0)
    }

    private fun handleInitialZoom(lockers: List<com.delivery.core.model.network.Locker>) {
        if (!hasZoomedToInitialLocation && mapLibreMap != null && lockers.isNotEmpty()) {
            if (preselectedLockerName != null) {
                // Priority 1: Zoom to preselected locker
                val preselectedLocker = lockers.find { it.lockerName == preselectedLockerName }
                preselectedLocker?.let { locker ->
                    if (viewModel.uiState.value.selectedLocker == null) {
                        viewModel.handleEvent(SelectLocationLockerEvent.SelectLocker(locker))
                    }
                    mapLibreUtils.moveCameraToPosition(
                        mapLibreMap = mapLibreMap,
                        position = LatLng(locker.latitude, locker.longitude),
                        zoom = 16.0,
                    )
                    hasZoomedToInitialLocation = true
                    Timber.tag("SelectLocationLocker").d("Zoomed to preselected locker: ${locker.lockerName}")
                }
            } else {
                // Priority 2: Zoom to first locker if no preselected
                val firstLocker = lockers.firstOrNull()
                firstLocker?.let { locker ->
                    mapLibreUtils.moveCameraToPosition(
                        mapLibreMap = mapLibreMap,
                        position = LatLng(locker.latitude, locker.longitude),
                        zoom = 14.0,
                    )
                    hasZoomedToInitialLocation = true
                    Timber.tag("SelectLocationLocker").d("Zoomed to first locker: ${locker.lockerName}")
                }
            }
        }
    }

    private fun updateToolbarTitle() {
        val titleResId =
            when (locationType) {
                Constants.LocationType.PICKUP -> R.string.select_location_locker_title
                Constants.LocationType.DELIVERY -> R.string.select_location_delivery_title
                else -> R.string.select_location_locker_title
            }
        binding.toolbar.setTitle(getString(titleResId))
    }

    private fun loadLockerStatistics() {
        lifecycleScope.launch {
            try {
                val statistics = viewModel.getLockerStatistics()
                val mainCount = statistics["main_lockers"] ?: 0
                val nestedCount = statistics["nested_lockers"] ?: 0
                val totalCount = statistics["total_lockers"] ?: 0

                Timber.tag("SelectLocationLocker").d("Locker Statistics - Main: $mainCount, Nested: $nestedCount, Total: $totalCount")
            } catch (e: Exception) {
                Timber.e(e, "Error loading locker statistics")
            }
        }
    }

    private fun moveCameraToLocker(locker: com.delivery.core.model.network.Locker) {
        updateSelectedLockerMarker(locker)
    }

    override fun bindingStateView() {
        super.bindingStateView()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    // Update lockers on map - only if map is ready
                    if (mapLibreMap != null && uiState.lockers.isNotEmpty()) {
                        mapLibreMap?.getStyle { style ->
                            drawLockerMarkers(style, uiState.lockers)

                            // Handle initial zoom after markers are drawn
                            handleInitialZoom(uiState.lockers)
                        }
                    }

                    // Update selected locker UI
                    if (uiState.selectedLocker != null) {
                        binding.selectedLockerContainer.visibility = android.view.View.VISIBLE
                        binding.tvSelectedLockerName.text = uiState.selectedLocker.lockerName
                        binding.tvSelectedLockerDescription.text =
                            uiState.selectedLocker.description
                        binding.btnSelectLocation.isEnabled = true
                    } else {
                        binding.selectedLockerContainer.visibility = android.view.View.GONE
                        binding.btnSelectLocation.isEnabled = false
                    }
                }
            }
        }

        // Handle UI events
        viewModel.uiEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SelectLocationLockerViewEvent.ShowMessage -> {
                    event.message.toast(requireContext())
                }

                is SelectLocationLockerViewEvent.ShowMessageRes -> {
                    val message =
                        if (event.args != null) {
                            getString(event.messageResId, event.args)
                        } else {
                            getString(event.messageResId)
                        }
                    message.toast(requireContext())
                }

                is SelectLocationLockerViewEvent.NavigateBackWithResult -> {
                    val result =
                        Bundle().apply {
                            putString(Constants.LockerBundleKeys.LOCKER_ID, event.locker.id)
                            putString(Constants.LockerBundleKeys.LOCKER_NAME, event.locker.lockerName)
                            putString(
                                Constants.LockerBundleKeys.LOCKER_DESCRIPTION,
                                event.locker.description,
                            )
                            putDouble(Constants.LockerBundleKeys.LOCKER_LATITUDE, event.locker.latitude)
                            putDouble(
                                Constants.LockerBundleKeys.LOCKER_LONGITUDE,
                                event.locker.longitude,
                            )
                            putString(Constants.LockerBundleKeys.LOCATION_TYPE, locationType)
                        }
                    setFragmentResult(Constants.FragmentResultKeys.SELECTED_LOCKER, result)
                    appNavigation.navigateUp()
                }
            }
        }
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.toolbar.setOnToolBarClickListener(
            object : ToolBarCommon.OnToolBarClickListener() {
                override fun onClickLeft() {
                    appNavigation.navigateUp()
                }

                override fun onClickRight() {
                    // Not used
                }
            },
        )

        binding.btnSelectLocation.setOnSafeClickListener {
            viewModel.handleEvent(SelectLocationLockerEvent.ConfirmSelection)
        }
    }

    override fun onStart() {
        super.onStart()
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_START)
    }

    override fun onResume() {
        super.onResume()
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_RESUME)
    }

    override fun onPause() {
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_PAUSE)
        super.onPause()
    }

    override fun onStop() {
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_STOP)
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_LOW_MEMORY)
    }

    override fun onDestroyView() {
        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_DESTROY)
        mapView = null
        mapLibreMap = null
        hasZoomedToInitialLocation = false // Reset flag for next time
        super.onDestroyView()
    }
}
