package com.delivery.setting.ui.tracking

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.Constants
import com.delivery.core.utils.MapLibreUtils
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentDeliveryTrackingBinding
import com.delivery.setting.model.Order
import com.delivery.setting.model.buttonTitle
import com.delivery.setting.model.displayText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DeliveryTrackingFragment :
    BaseFragment<FragmentDeliveryTrackingBinding, DeliveryTrackingViewModel>(R.layout.fragment_delivery_tracking) {

    private val viewModel: DeliveryTrackingViewModel by viewModels()
    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null

    @Inject
    lateinit var appNavigation: DemoNavigation

    @Inject
    lateinit var mapLibreUtils: MapLibreUtils

    override fun getVM(): DeliveryTrackingViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val orderItem =
            arguments?.getSerializable(Constants.BundleKeys.ORDER_DETAIL) as? Order
        if (orderItem != null) {
            viewModel.handleEvent(DeliveryTrackingEvent.LoadOrderData(orderItem))
        } else {
            Timber.w("No order item found in arguments")
        }

        mapLibreUtils.initializeMapLibre()
        mapView = binding.mapView
        mapLibreUtils.setupMapView(mapView!!, savedInstanceState)
        initializeMap()
    }

    override fun bindingStateView() {
        super.bindingStateView()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    handleViewState(uiState)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleViewEvent(event)
                }
            }
        }
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnToolBarClickListener(object : ToolBarCommon.OnToolBarClickListener() {
            override fun onClickLeft() {
                appNavigation.navigateUp()
            }

            override fun onClickRight() {
            }
        })
    }

    private fun handleViewState(state: DeliveryTrackingViewState) {

        state.orderItem?.let { orderItem ->
            setUIOrder(orderItem)
        }

        // Draw multi-segment route when data is available
        if (state.segmentRoutes.isNotEmpty() && mapLibreMap != null) {
            drawMultiSegmentRoute(state.segmentRoutes, state.lockerPositions, state.dronePosition)
        } else if (state.routePoints.isNotEmpty() && mapLibreMap != null) {
            // Fallback to simple route
            drawRouteOnMap(state.routePoints)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUIOrder(order: Order) {
        val displayStyle = order.getDisplayStyle(viewModel.currentLockerId)

        // Set order ID
        binding.tvOrderId.text = "#${order.id.take(12)}"

        // Set drone type (simplified - you can enhance this based on your data)
        binding.tvDroneType.text = "Máy bay: ${order.segments.firstOrNull()?.droneId ?: "Không xác định"}"

        // Set size and weight
        binding.tvSizeWeight.text = "${order.getSizeText()} • ${order.weight} kg"

        // Set status and date using displayStyle
        binding.tvStatusDate.text = "${displayStyle.displayText} • ${order.getFormattedDate()}"

        // Set status text color based on displayStyle
        val statusColor = when (displayStyle) {
            com.delivery.setting.model.OrderDisplayStyle.SEND -> ContextCompat.getColor(binding.root.context, R.color.orange)
            com.delivery.setting.model.OrderDisplayStyle.SENT -> ContextCompat.getColor(binding.root.context, R.color.blue)
            com.delivery.setting.model.OrderDisplayStyle.WAITING -> ContextCompat.getColor(binding.root.context, R.color.orange)
            com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> ContextCompat.getColor(binding.root.context, R.color.green)
            com.delivery.setting.model.OrderDisplayStyle.DONE -> ContextCompat.getColor(binding.root.context, R.color.gray)
        }
        binding.tvStatusDate.setTextColor(statusColor)

        // Set accent background (left border) based on displayStyle
        val accentBackground = when (displayStyle) {
            com.delivery.setting.model.OrderDisplayStyle.SEND -> R.drawable.bg_accent_send
            com.delivery.setting.model.OrderDisplayStyle.SENT -> R.drawable.bg_accent_sent
            com.delivery.setting.model.OrderDisplayStyle.WAITING -> R.drawable.bg_accent_waiting
            com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> R.drawable.bg_accent_unload
            com.delivery.setting.model.OrderDisplayStyle.DONE -> R.drawable.bg_accent_done
        }
        binding.viewAccent.setBackgroundResource(accentBackground)

        // Set action button based on displayStyle
        val buttonText = displayStyle.buttonTitle
        if (displayStyle != com.delivery.setting.model.OrderDisplayStyle.DONE) {
            binding.btnAction.text = buttonText
            binding.btnAction.visibility = View.VISIBLE

            // Set button background and text color based on displayStyle
            when (displayStyle) {
                com.delivery.setting.model.OrderDisplayStyle.SEND -> {
                    // Bấm được, màu cam
                    binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_send)
                    binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    binding.btnAction.isEnabled = true
                    binding.btnAction.alpha = 1.0f
                }
                com.delivery.setting.model.OrderDisplayStyle.SENT -> {
                    // Không bấm được, màu cam
                    binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_sent)
                    binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    binding.btnAction.isEnabled = false
                    binding.btnAction.alpha = 0.6f
                }
                com.delivery.setting.model.OrderDisplayStyle.WAITING -> {
                    // Làm mờ, không bấm được, màu xanh
                    binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_waiting)
                    binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    binding.btnAction.isEnabled = false
                    binding.btnAction.alpha = 0.5f
                }
                com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> {
                    // Bấm được, màu xanh
                    binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_unload)
                    binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                    binding.btnAction.isEnabled = true
                    binding.btnAction.alpha = 1.0f
                }
                com.delivery.setting.model.OrderDisplayStyle.DONE -> {
                    // Không hiển thị nút
                    binding.btnAction.visibility = View.GONE
                }
            }

            // Set click listener only for enabled buttons
            if (binding.btnAction.isEnabled) {
                binding.btnAction.setOnClickListener {
                    viewModel.handleEvent(DeliveryTrackingEvent.OrderAction(order, displayStyle))
                }
            } else {
                binding.btnAction.setOnClickListener(null)
            }
        } else {
            binding.btnAction.visibility = View.GONE
        }

    }

    private fun formatOrderDate(date: String, time: String?): String {
        return if (time != null) {
            "$date • $time"
        } else {
            date
        }
    }

    private fun handleViewEvent(event: DeliveryTrackingViewEvent) {
        when (event) {
            is DeliveryTrackingViewEvent.ShowMessage -> {
                event.message.toast(requireContext())
            }

            is DeliveryTrackingViewEvent.ShowMessageRes -> {
                val message = if (event.args != null) {
                    getString(event.messageResId, event.args)
                } else {
                    getString(event.messageResId)
                }
                message.toast(requireContext())
            }

            is DeliveryTrackingViewEvent.NavigateBack -> {
                appNavigation.navigateUp()
            }
        }
    }

    private fun initializeMap() {
        mapLibreUtils.initializeMap(
            mapView = mapView!!,
            onMapReady = { map ->
                mapLibreMap = map
            },
            onStyleLoaded = { style ->
                // Map is ready, route will be drawn when data is loaded
            }
        )
    }

    /**
     * Draw multi-segment route with different colors for each segment
     */
    private fun drawMultiSegmentRoute(
        segmentRoutes: List<SegmentRoute>,
        lockerPositions: List<LockerPosition>,
        dronePosition: LatLng?
    ) {
        if (segmentRoutes.isEmpty()) return

        mapLibreMap?.getStyle { style ->
            // Clear existing layers
            clearAllMapLayers(style)

            // Draw each segment with different colors/styles
            segmentRoutes.forEachIndexed { index, segmentRoute ->
                drawSegmentRoute(style, segmentRoute, index)
            }

            // Draw locker markers
            drawLockerMarkers(style, lockerPositions)

            // Draw drone position if available
            dronePosition?.let { position ->
                drawDroneMarker(style, position)
            }

            // Move camera to show all segments
            val allPoints = segmentRoutes.flatMap { it.routePoints }
            moveCameraToRoute(allPoints)
        }
    }

    /**
     * Draw a single segment route
     */
    private fun drawSegmentRoute(style: Style, segmentRoute: SegmentRoute, segmentIndex: Int) {
        if (segmentRoute.routePoints.isEmpty()) return

        val sourceId = "segment-route-source-$segmentIndex"
        val layerId = "segment-route-layer-$segmentIndex"

        // Choose color based on segment status
        val color = when (segmentRoute.status) {
            SegmentStatus.COMPLETED -> Color.parseColor("#4CAF50") // Green
            SegmentStatus.IN_PROGRESS -> Color.parseColor("#2196F3") // Blue
            SegmentStatus.PENDING -> Color.parseColor("#9E9E9E") // Gray
        }

        // Choose line width based on status
        val lineWidth = when (segmentRoute.status) {
            SegmentStatus.IN_PROGRESS -> 8.0f
            else -> 6.0f
        }

        try {
            // Create GeoJSON for the segment route
            val coordinates = segmentRoute.routePoints.map { point ->
                arrayOf(point.longitude, point.latitude)
            }

            val coordinatesJsonArray = org.json.JSONArray()
            coordinates.forEach { coord ->
                val pointArray = org.json.JSONArray()
                pointArray.put(coord[0])
                pointArray.put(coord[1])
                coordinatesJsonArray.put(pointArray)
            }

            val geoJson = JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "LineString")
                    put("coordinates", coordinatesJsonArray)
                })
                put("properties", JSONObject().apply {
                    put("segment_index", segmentIndex)
                    put("status", segmentRoute.status.name)
                })
            }

            // Add route source and layer
            mapLibreUtils.addLineLayer(
                style = style,
                sourceId = sourceId,
                layerId = layerId,
                geoJsonFeature = geoJson.toString(),
                color = color,
                width = lineWidth
            )

            Timber.d("Drew segment $segmentIndex with ${segmentRoute.routePoints.size} points, status: ${segmentRoute.status}")

        } catch (e: Exception) {
            Timber.e(e, "Error drawing segment route $segmentIndex")
        }
    }

    /**
     * Draw locker markers
     */
    private fun drawLockerMarkers(style: Style, lockerPositions: List<LockerPosition>) {
        lockerPositions.forEachIndexed { index, locker ->
            val sourceId = "locker-source-$index"
            val layerId = "locker-layer-$index"

            try {
                // Use different colors for different lockers
                val colors = listOf(
                    Color.parseColor("#FF5722"), // Red-Orange
                    Color.parseColor("#9C27B0"), // Purple
                    Color.parseColor("#FF9800"), // Orange
                    Color.parseColor("#607D8B"), // Blue-Gray
                    Color.parseColor("#795548")  // Brown
                )
                val color = colors[index % colors.size]

                mapLibreUtils.addCircleMarker(
                    style = style,
                    sourceId = sourceId,
                    layerId = layerId,
                    position = locker.position,
                    color = color,
                    radius = 12f
                )

                Timber.d("Drew locker marker: ${locker.lockerName} at ${locker.position}")

            } catch (e: Exception) {
                Timber.e(e, "Error drawing locker marker for ${locker.lockerId}")
            }
        }
    }

    /**
     * Draw drone marker
     */
    private fun drawDroneMarker(style: Style, dronePosition: LatLng) {
        try {
            mapLibreUtils.addCircleMarker(
                style = style,
                sourceId = "drone-source",
                layerId = "drone-layer",
                position = dronePosition,
                color = Color.parseColor("#FFC107"), // Amber
                radius = 8f
            )

            Timber.d("Drew drone marker at: $dronePosition")

        } catch (e: Exception) {
            Timber.e(e, "Error drawing drone marker")
        }
    }

    /**
     * Clear all existing map layers
     */
    private fun clearAllMapLayers(style: Style) {
        val layersToRemove = listOf(
            "route-layer", "origin-layer", "destination-layer", "drone-layer"
        )
        val sourcesToRemove = listOf(
            "route-source", "origin-source", "destination-source", "drone-source"
        )

        // Remove segment layers (dynamic)
        for (i in 0..10) { // Assume max 10 segments
            layersToRemove.forEach { baseLayer ->
                try {
                    val layerName = "$baseLayer-$i"
                    if (style.getLayer(layerName) != null) {
                        style.removeLayer(layerName)
                    }
                } catch (e: Exception) {
                    // Layer doesn't exist, ignore
                }
            }

            sourcesToRemove.forEach { baseSource ->
                try {
                    val sourceName = "$baseSource-$i"
                    if (style.getSource(sourceName) != null) {
                        style.removeSource(sourceName)
                    }
                } catch (e: Exception) {
                    // Source doesn't exist, ignore
                }
            }
        }

        // Remove standard layers
        (layersToRemove + sourcesToRemove).forEach { name ->
            try {
                if (style.getLayer(name) != null) {
                    style.removeLayer(name)
                }
                if (style.getSource(name) != null) {
                    style.removeSource(name)
                }
            } catch (e: Exception) {
                // Layer/source doesn't exist, ignore
            }
        }

        // Remove locker and segment layers
        for (i in 0..20) {
            try {
                listOf("locker-layer-$i", "segment-route-layer-$i").forEach { layerName ->
                    if (style.getLayer(layerName) != null) {
                        style.removeLayer(layerName)
                    }
                }
                listOf("locker-source-$i", "segment-route-source-$i").forEach { sourceName ->
                    if (style.getSource(sourceName) != null) {
                        style.removeSource(sourceName)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun drawRouteOnMap(routePoints: List<LatLng>) {
        if (routePoints.isEmpty()) return

        mapLibreMap?.getStyle { style ->
            // Clear existing route layers safely
            clearAllMapLayers(style)

            // Create GeoJSON for the route
            val coordinates = routePoints.map { point ->
                arrayOf(point.longitude, point.latitude)
            }
            Timber.tag("DeliveryTracking").d("Drawing route with ${routePoints.size} points")
            Timber.tag("DeliveryTracking").d("coordinates: $coordinates")

            val coordinatesJsonArray = org.json.JSONArray()
            coordinates.forEach { coord ->
                val pointArray = org.json.JSONArray()
                pointArray.put(coord[0])
                pointArray.put(coord[1])
                coordinatesJsonArray.put(pointArray)
            }

            val geoJson = JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "LineString")
                    put("coordinates", coordinatesJsonArray)
                })
            }

            Timber.tag("DeliveryTracking").d("GeoJSON: ${geoJson.toString()}")

            // Add route source and layer
            mapLibreUtils.addLineLayer(
                style = style,
                sourceId = "route-source",
                layerId = "route-layer",
                geoJsonFeature = geoJson.toString(),
                color = Color.parseColor("#1E88E5"),
                width = 6.0f
            )

            // Draw markers for start and end points
            if (routePoints.size >= 2) {
                drawStartEndMarkers(style, routePoints.first(), routePoints.last())
            }

            // Move camera to show the route
            moveCameraToRoute(routePoints)
        }
    }

    private fun drawStartEndMarkers(style: Style, startPoint: LatLng, endPoint: LatLng) {
        // Clear existing markers safely
        try {
            if (style.getLayer("origin-layer") != null) {
                style.removeLayer("origin-layer")
            }
            if (style.getSource("origin-source") != null) {
                style.removeSource("origin-source")
            }
            if (style.getLayer("destination-layer") != null) {
                style.removeLayer("destination-layer")
            }
            if (style.getSource("destination-source") != null) {
                style.removeSource("destination-source")
            }
        } catch (e: Exception) {
            Timber.w(e, "Error removing existing markers")
        }

        // Add start marker (green)
        mapLibreUtils.addCircleMarker(
            style = style,
            sourceId = "origin-source",
            layerId = "origin-layer",
            position = startPoint,
            color = Color.parseColor("#4CAF50")
        )

        // Add end marker (red)
        mapLibreUtils.addCircleMarker(
            style = style,
            sourceId = "destination-source",
            layerId = "destination-layer",
            position = endPoint,
            color = Color.parseColor("#F44336")
        )

        Timber.tag("DeliveryTracking").d("Added markers - Start: $startPoint, End: $endPoint")
    }

    private fun moveCameraToRoute(routePoints: List<LatLng>) {
        if (routePoints.isEmpty()) return

        try {
            if (routePoints.size == 1) {
                // Single point - just center on it
                mapLibreUtils.moveCameraToPosition(mapLibreMap, routePoints.first(), 16.0)
            } else {
                // Multiple points - calculate bounds to show entire route
                val latitudes = routePoints.map { it.latitude }
                val longitudes = routePoints.map { it.longitude }

                val minLat = latitudes.minOrNull() ?: 0.0
                val maxLat = latitudes.maxOrNull() ?: 0.0
                val minLng = longitudes.minOrNull() ?: 0.0
                val maxLng = longitudes.maxOrNull() ?: 0.0

                // Add padding to bounds
                val padding = 0.002 // roughly 200m
                val bounds = LatLngBounds.Builder()
                    .include(LatLng(minLat - padding, minLng - padding))
                    .include(LatLng(maxLat + padding, maxLng + padding))
                    .build()

                mapLibreMap?.let { map ->
                    try {
                        // Use newLatLngBounds to automatically calculate the best zoom level
                        val paddingInPixels = 100 // 100 pixels padding on all sides
                        val cameraUpdate =
                            CameraUpdateFactory.newLatLngBounds(bounds, paddingInPixels)

                        // Animate camera to show the bounds
                        map.animateCamera(cameraUpdate, 1000)

                        Timber.tag("DeliveryTracking").d("Camera animated to bounds: $bounds")
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to animate to bounds, using fallback")
                        // Fallback: center on bounds center with fixed zoom
                        val cameraPosition = CameraPosition.Builder()
                            .target(bounds.center)
                            .zoom(13.0)
                            .build()
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            1000
                        )
                    }
                }

                Timber.tag("DeliveryTracking").d("Camera moved to show route bounds: $bounds")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error moving camera to route")
            // Fallback to first point
            mapLibreUtils.moveCameraToPosition(mapLibreMap, routePoints.first(), 16.0)
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
        super.onDestroyView()
    }
}
