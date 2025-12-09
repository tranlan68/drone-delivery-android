package com.delivery.setting.ui.tracking

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.core.utils.MapLibreUtils
import com.delivery.core.utils.custom.ToolBarCommon
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.adapter.dp
import com.delivery.setting.databinding.FragmentDeliveryTrackingBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.ui.createorder.DeliveryPriority
import com.delivery.setting.ui.createorder.OrderProducts
import com.delivery.setting.ui.createorder.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
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

    @Inject
    lateinit var rxPreferences: RxPreferences
    private var selectedProducts: List<Product> = emptyList()

    var isFirstMoveCamera = true

    private var routeAnimator: ObjectAnimator? = null
    private var dronePath: MutableList<LatLng> = mutableListOf<LatLng>()
    private var viewAlive = true

    override fun getVM(): DeliveryTrackingViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        isFirstMoveCamera = true
        dronePath.clear()

        val orderItem =
            arguments?.getSerializable(Constants.BundleKeys.ORDER_DETAIL) as? OrderHistoryItem
        if (orderItem != null) {
            lifecycleScope.launch {
                val gson = Gson()
                val orderProductsListStr = rxPreferences.getOrderProducts().first().toString()
                val type = object : TypeToken<List<OrderProducts>>() {}.type
                val orderProductsList =
                    gson.fromJson<List<OrderProducts>>(orderProductsListStr, type)

                if (orderProductsList != null) {
                    for (op in orderProductsList) {
                        if (op.orderId == orderItem.id) {
                            selectedProducts = op.products
                            renderSelectedProducts(selectedProducts)
                            break
                        }
                    }
                }

                viewModel.handleEvent(DeliveryTrackingEvent.LoadOrderData(orderItem))
            }
        } else {
            Timber.w("No order item found in arguments")
        }

        mapLibreUtils.initializeMapLibre()
        mapView = binding.mapView
        mapLibreUtils.setupMapView(mapView!!, savedInstanceState)
        initializeMap()

        binding.btnCancelOrder.setOnClickListener {
            viewModel.handleEvent(DeliveryTrackingEvent.CancelDelivery)
        }
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
        binding.toolbar.setOnToolBarClickListener(
            object : ToolBarCommon.OnToolBarClickListener() {
                override fun onClickLeft() {
                    appNavigation.navigateUp()
                }

                override fun onClickRight() {
                }
            },
        )
    }

    private fun handleViewState(state: DeliveryTrackingViewState) {
        state.orderItem?.let { orderItem ->
            binding.tvOrderId.text = "#" + orderItem.intId
            binding.tvFromLocation.text = orderItem.sourceLocker
            binding.tvToLocation.text = orderItem.destLocker
            //binding.tvOrderStatus.text = orderItem.status.displayName
            binding.tvPackageType.text = "Loại hàng" //orderItem.packageType
            binding.tvPackageWeight.text = orderItem.packageWeight
            binding.tvOderDate.text = formatOrderDate(orderItem.orderDate, orderItem.orderTime)
            binding.btnCancelOrder.isEnabled = true

            if (orderItem.status == OrderStatus.DELIVERED) {
                binding.tvExpectedDate.text = "Đã giao: ${formatOrderDate(orderItem.deliveryDate, orderItem.deliveryTime )}"
            } else if (orderItem.status == OrderStatus.PENDING) {
                //binding.tvExpectedDate.text = ""
                binding. tvExpectedDate.text = "Đang chờ xác nhận đơn hàng"
            } else if (orderItem.status == OrderStatus.CANCEL) {
                binding. tvExpectedDate.text = "Đơn hàng đã bị hủy"
                binding.btnCancelOrder.isEnabled = false
            } else {
               // binding.tvExpectedDate.text = "Dự kiến: ${formatOrderDate(orderItem.deliveryDate, orderItem.deliveryTime)}"
                binding. tvExpectedDate.text = "Giao trong: ${kotlin.math.ceil((orderItem.eta ?: 300L).toDouble()/60.0).toInt()} phút"
            }

            binding.tvOrderId.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
            //binding.tvOrderId.setTextSize(18.0f)

            // Set status color based on order status
            val statusColor = getStatusColor(orderItem.status)
            binding.tvExpectedDate.setTextColor(binding.root.context.getColor(statusColor))
            //binding.tvExpectedDate.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_light))

            when (orderItem.priority) {
                DeliveryPriority.STANDARD.value -> {
                    binding.tvProperty.text = getString(com.delivery.core.R.string.string_standard)
                }
                DeliveryPriority.EXPRESS.value -> {
                    binding.tvProperty.text = getString(com.delivery.core.R.string.string_express)
                }
            }
            updateStatusSteps(orderItem.status)//

            startGrabStyleRouteAnimation()
        }

        // Update drone information display
        updateDroneInfo(state)

        // Draw multi-segment route when data is available
        if (/*state.segmentRoutes.isNotEmpty() &&*/ mapLibreMap != null && state.lockerPositions.isNotEmpty()) {
            drawMultiSegmentRoute(state.segmentRoutes, state.lockerPositions, state.dronePosition, state.droneHeading, state.orderItem?.droneId)
        } /*else if (state.routePoints.isNotEmpty() && mapLibreMap != null) {
            // Fallback to simple route
            drawRouteOnMap(state.routePoints)
        }*/
        else {
            binding.root.postDelayed({
                if (mapLibreMap != null && state.lockerPositions.isNotEmpty()) {
                    drawMultiSegmentRoute(state.segmentRoutes, state.lockerPositions, state.dronePosition, state.droneHeading, state.orderItem?.droneId)
                }
            }, 200)
        }
    }

    private fun getStatusColor(status: OrderStatus): Int {
        val statusColor =
            when (status) {
                OrderStatus.PENDING -> android.R.color.holo_blue_dark
                OrderStatus.CONFIRMED -> android.R.color.holo_orange_light
                OrderStatus.IN_DELIVERY -> android.R.color.holo_orange_dark
                OrderStatus.DELIVERED -> android.R.color.holo_green_dark
                OrderStatus.CANCEL -> android.R.color.holo_red_dark
            }
        return statusColor
    }

    private fun updateStatusSteps(status: OrderStatus) = with(binding) {
        fun active(tv: TextView, status: OrderStatus) {
            val statusColor = getStatusColor(status)
            tv.setTextColor(binding.root.context.getColor(statusColor))
            //tv.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_light))
            tv.setTypeface(tv.typeface, android.graphics.Typeface.BOLD)
        }
        fun inactive(tv: TextView) {
            tv.setTextColor(Color.parseColor("#BBBBBB"))
            tv.setTypeface(tv.typeface, android.graphics.Typeface.NORMAL)
        }

        inactive(tvStepPending)
        inactive(tvStepWaiting)
        inactive(tvStepDelivering)
        inactive(tvStepDone)
        inactive(tvArrowStepWaiting)
        inactive(tvArrowStepDelivering)
        inactive(tvArrowStepDone)

        when (status) {
            OrderStatus.PENDING -> {
                active(tvStepPending, OrderStatus.PENDING)
            }
            OrderStatus.CONFIRMED -> { // Chờ giao
                active(tvStepPending, OrderStatus.PENDING)
                active(tvArrowStepWaiting, OrderStatus.CONFIRMED)
                active(tvStepWaiting, OrderStatus.CONFIRMED)
            }
            OrderStatus.IN_DELIVERY -> {
                active(tvStepPending, OrderStatus.PENDING)
                active(tvArrowStepWaiting, OrderStatus.CONFIRMED)
                active(tvStepWaiting, OrderStatus.CONFIRMED)
                active(tvArrowStepDelivering, OrderStatus.IN_DELIVERY)
                active(tvStepDelivering, OrderStatus.IN_DELIVERY)
            }
            OrderStatus.DELIVERED -> {
                active(tvStepPending, OrderStatus.PENDING)
                active(tvArrowStepWaiting, OrderStatus.CONFIRMED)
                active(tvStepWaiting, OrderStatus.CONFIRMED)
                active(tvArrowStepDelivering, OrderStatus.IN_DELIVERY)
                active(tvStepDelivering, OrderStatus.IN_DELIVERY)
                active(tvArrowStepDone, OrderStatus.DELIVERED)
                active(tvStepDone, OrderStatus.DELIVERED)
            }
            OrderStatus.CANCEL -> {
                // tùy bạn, có thể tô đỏ/ghi hẳn "Đã hủy"
                inactive(tvStepPending)
                inactive(tvArrowStepWaiting)
                inactive(tvStepWaiting)
                inactive(tvArrowStepDelivering)
                inactive(tvStepDelivering)
                inactive(tvArrowStepDone)
                inactive(tvStepDone)
            }
        }
    }


    private fun startGrabStyleRouteAnimation() {
        val movingDot = binding.viewMovingDot
        val trailContainer = binding.trailContainer

        movingDot.post {

            if (!viewAlive || binding == null) return@post

            val dotsCount = 6
            val dotSizePx = 7.dp
            val containerWidth = trailContainer.width.toFloat()
            val spacing = (containerWidth - dotSizePx) / (dotsCount - 1)

            trailContainer.removeAllViews()
            val dots = mutableListOf<View>()
            val centerY = trailContainer.height / 2f - dotSizePx / 2f

            // tạo dots
            for (i in 0 until dotsCount) {
                if (!viewAlive || binding == null) return@post
                val dot = View(trailContainer.context).apply {
                    layoutParams = FrameLayout.LayoutParams(dotSizePx.toInt(), dotSizePx.toInt())
                    x = i * spacing
                    y = centerY
                    background = ContextCompat.getDrawable(context, R.drawable.bg_dot_future)
                }
                trailContainer.addView(dot)
                dots.add(dot)
            }

            fun animateRoute() {
                if (!viewAlive || binding == null) return

                movingDot.translationX = 0f
                movingDot.alpha = 1f

                // hủy animator cũ
                routeAnimator?.cancel()

                val animator = ObjectAnimator.ofFloat(movingDot, "translationX", 0f, containerWidth)
                routeAnimator = animator

                animator.duration = 5000
                animator.interpolator = LinearInterpolator()

                animator.addUpdateListener { anim ->
                    if (!viewAlive || binding == null) return@addUpdateListener

                    val currentX = anim.animatedValue as Float
                    val currentIndex = (currentX / spacing).toInt().coerceIn(0, dotsCount - 1)

                    dots.forEachIndexed { index, dot ->
                        if (!viewAlive || binding == null) return@addUpdateListener

                        dot.background =
                            when {
                                index < currentIndex -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot_passed)
                                index == currentIndex -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot_current)
                                else -> ContextCompat.getDrawable(requireContext(), R.drawable.bg_dot_future)
                            }
                    }
                }

                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (!viewAlive || binding == null) return
                        movingDot.alpha = 0f
                        animateRoute() // loop
                    }
                })

                animator.start()
            }

            animateRoute()
        }
    }

    private fun renderSelectedProducts(products: List<Product>) {
        val grid = binding.glSelectedProducts
        val card = binding.cardSelectedProducts

        grid.removeAllViews()

        if (products.isEmpty()) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE

        val inflater = LayoutInflater.from(requireContext())

        products.forEach { product ->
            val item = inflater.inflate(R.layout.item_selected_product_vertical, grid, false)

            item.findViewById<ImageView>(R.id.ivIcon).setImageResource(product.iconRes)
            item.findViewById<TextView>(R.id.tvName).text = product.name

            grid.addView(item)
        }
    }

    /**
     * Update drone information display
     */
    private fun updateDroneInfo(state: DeliveryTrackingViewState) {
        state.dronePosition?.let { position ->
            // Update drone position info if available
            state.droneSpeed?.let { speed ->
                // Convert m/s to km/h for display
                val speedKmh = (speed * 3.6).toInt()
                // You can add a TextView to display speed if needed
                Timber.d("Drone speed: $speedKmh km/h")
            }

            state.droneHeading?.let { heading ->
                // You can add a TextView to display heading if needed
                Timber.d("Drone heading: ${heading.toInt()}°")
            }

            state.droneLastUpdated?.let { lastUpdated ->
                val timeAgo = formatTimeAgo(lastUpdated)
                // You can add a TextView to display last update time if needed
                Timber.d("Drone last updated: $timeAgo")
            }
        }
    }

    /**
     * Format timestamp to "X minutes ago" format
     */
    private fun formatTimeAgo(timestamp: Long): String {
        val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds
        val timeDiff = currentTime - timestamp

        return when {
            timeDiff < 60 -> "Vừa cập nhật"
            timeDiff < 3600 -> "${timeDiff / 60} phút trước"
            timeDiff < 86400 -> "${timeDiff / 3600} giờ trước"
            else -> "${timeDiff / 86400} ngày trước"
        }
    }

    private fun formatOrderDate(
        date: String,
        time: String?,
    ): String {
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
                val message =
                    if (event.args != null) {
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
                setupMapClickListener()
            },
            onStyleLoaded = { style ->
                // Map is ready, route will be drawn when data is loaded
            },
        )
    }

    /**
     * Setup map click listener to show tooltips
     */
    private fun setupMapClickListener() {
        mapLibreMap?.addOnMapClickListener { latLng ->
            try {
                // Convert LatLng to PointF for queryRenderedFeatures
                val pointF = mapLibreMap?.projection?.toScreenLocation(latLng)
                if (pointF != null) {
                    val features = mapLibreMap?.queryRenderedFeatures(pointF)
                    features?.let { featureList ->
                        for (feature in featureList) {
                            val tooltip = feature.getStringProperty("tooltip")
                            if (!tooltip.isNullOrEmpty()) {
                                tooltip.toast(requireContext())
                                return@addOnMapClickListener true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling map click")
            }
            false
        }
    }

    /**
     * Draw multi-segment route with different colors for each segment
     */
    private fun drawMultiSegmentRoute(
        segmentRoutes: List<SegmentRoute>,
        lockerPositions: List<LockerPosition>,
        dronePosition: LatLng?,
        droneHeading: Double? = null,
        droneId: String? = ""
    ) {
        //if (segmentRoutes.isEmpty()) return

        mapLibreMap?.getStyle { style ->
            // Clear existing layers
            clearAllMapLayers(style)

            /*// Draw each segment with different colors/styles
            segmentRoutes.forEachIndexed { index, segmentRoute ->
                drawSegmentRoute(style, segmentRoute, index)
            }

            // Draw locker markers
            drawLockerMarkers(style, lockerPositions)

            // Draw drone position if available
            dronePosition?.let { position ->
                drawDroneMarker(style, position, droneHeading)
            }

            // Draw flight position markers
            val allRoutePoints = segmentRoutes.flatMap { it.routePoints }
            drawFlightPositionMarkers(style, allRoutePoints)

            // Move camera to show all segments
            val allPoints = segmentRoutes.flatMap { it.routePoints }
            moveCameraToRoute(allPoints)*/


            // FOR DEMO
            var routePoints = createRoutePointsForDemo(lockerPositions.first(), lockerPositions.last())
            drawRouteOnMapDemo(style, routePoints)

            // TKL Hardcode
            /*val sourceLockerPosition = LockerPosition(
                lockerId = "c0088490-d4c9-4e35-93e6-59234c1ac20c",
                lockerName = "Hub VMC",
                position = LatLng(
                    latitude = 21.0016758,
                    longitude = 105.5369555
                )
            )*/
            // TKL Hardcode
            drawLockerMarkerForDemo(style, 0, true, lockerPositions.first())
            /*val destinationLockerPosition = LockerPosition(
                lockerId = "f7de2f41-6eaa-462c-9a87-3acd64d545c5",
                lockerName = "Hub DVTC",
                position = LatLng(
                    latitude = 21.0048467,
                    longitude = 105.5278548
                )
            )*/
            drawLockerMarkerForDemo(style, 1, false, lockerPositions.last())

            dronePosition?.let { position ->
                if (position.latitude != 0.0 && position.longitude != 0.0) {
                    drawDroneMarker(style, position, droneHeading, droneId)
                    dronePath.add(position)
                    //drawDronePath(style, dronePath)
                }
            }
            if (dronePath.isNotEmpty()) {
                drawDronePath(style, dronePath)
            }
            // Move camera to show all segments

            if (isFirstMoveCamera) {
                /*val allPoints = segmentRoutes.flatMap {routePoints }
                moveCameraToRoute(allPoints)*/
                moveCameraToRoute(routePoints)
                isFirstMoveCamera = false
            }
        }
    }

    // TKL Hardcode
    private fun createRoutePointsForDemo(startHub: LockerPosition, endHub: LockerPosition): List<LatLng> {
        if ((startHub.lockerId == "693673e2bbb6e622e589b03d" /*Hub*/ && endHub.lockerId == "693673e2bbb6e622e589b03e") /*K1*/ ||
            (startHub.lockerId == "693673e2bbb6e622e589b03e" /*K1*/ && endHub.lockerId == "693673e2bbb6e622e589b03d") /*Hub*/) {
            val routePoints: List<LatLng> = listOf(
                LatLng(21.001363, 105.530246),
                LatLng(21.003276, 105.529469),
                LatLng(21.003765, 105.531626),
            )
            return routePoints
        } else if ((startHub.lockerId == "693673e2bbb6e622e589b03d" /*Hub*/ && endHub.lockerId == "693673e2bbb6e622e589b03f") /*K2*/ ||
            (startHub.lockerId == "693673e2bbb6e622e589b03f" /*K2*/ && endHub.lockerId == "693673e2bbb6e622e589b03d") /*Hub*/) {
            val routePoints: List<LatLng> = listOf(
                LatLng(21.001363, 105.530246),
                LatLng(21.003276, 105.529469),
                LatLng(21.002660, 105.527070),
            )
            return routePoints
        }
        /*val routePoints: List<LatLng> = listOf(
            LatLng(21.0016758, 105.5369555),
            LatLng(21.0020301, 105.5352129),
            LatLng(21.0023375, 105.5338972),
            LatLng(21.0027208, 105.5326431),
            LatLng(21.0030109, 105.5312604),
            LatLng(21.0032873, 105.5301468),
            LatLng(21.0035616, 105.5290312),
            LatLng(21.0038705, 105.5282057),
            LatLng(21.0040567, 105.5280218),
            LatLng(21.0043128, 105.5289812),
            LatLng(21.0045890, 105.5294023),
            LatLng(21.0048467, 105.5278548)
        )*/
        val routePoints: List<LatLng> = emptyList()
        return routePoints
    }

    private fun drawLockerMarkerForDemo(
        style: Style,
        index: Int,
        isFromLocation: Boolean,
        lockerPosition: LockerPosition,
    ) {
        var sourceId = "locker-source-$index"
        var layerId = "locker-layer-$index"
        var iconRes = R.drawable.ic_dot_blue
        if (!isFromLocation) {
            sourceId = "locker-destination-$index"
            layerId = "locker-layer-$index"
            iconRes = R.drawable.ic_location_to1
        }

        try {
            mapLibreUtils.addIconMarker(
                style = style,
                sourceId = sourceId,
                layerId = layerId,
                position = lockerPosition.position,
                iconRes = iconRes,
                tooltipText = lockerPosition.lockerName,
                iconSize = 1.2f,
            )

            Timber.d("Drew locker marker: ${lockerPosition.lockerName} at ${lockerPosition.position}")
        } catch (e: Exception) {
            Timber.e(e, "Error drawing locker marker for ${lockerPosition.lockerId}")
        }
    }

    /**
     * Draw a single segment route
     */
    private fun drawSegmentRoute(
        style: Style,
        segmentRoute: SegmentRoute,
        segmentIndex: Int,
    ) {
        if (segmentRoute.routePoints.isEmpty()) return

        val sourceId = "segment-route-source-$segmentIndex"
        val layerId = "segment-route-layer-$segmentIndex"

        // Choose color based on segment status
        val color =
            when (segmentRoute.status) {
                SegmentStatus.COMPLETED -> Color.parseColor("#4CAF50") // Green
                SegmentStatus.IN_PROGRESS -> Color.parseColor("#FF4444") // Red
                SegmentStatus.PENDING -> Color.parseColor("#FF4444") // Red
            }

        // Choose line width based on status
        val lineWidth =
            when (segmentRoute.status) {
                SegmentStatus.IN_PROGRESS -> 8.0f
                else -> 6.0f
            }

        try {
            // Create GeoJSON for the segment route
            val coordinates =
                segmentRoute.routePoints.map { point ->
                    arrayOf(point.longitude, point.latitude)
                }

            val coordinatesJsonArray = org.json.JSONArray()
            coordinates.forEach { coord ->
                val pointArray = org.json.JSONArray()
                pointArray.put(coord[0])
                pointArray.put(coord[1])
                coordinatesJsonArray.put(pointArray)
            }

            val geoJson =
                JSONObject().apply {
                    put("type", "Feature")
                    put(
                        "geometry",
                        JSONObject().apply {
                            put("type", "LineString")
                            put("coordinates", coordinatesJsonArray)
                        },
                    )
                    put(
                        "properties",
                        JSONObject().apply {
                            put("segment_index", segmentIndex)
                            put("status", segmentRoute.status.name)
                        },
                    )
                }

            // Add route source and layer
            mapLibreUtils.addLineLayer(
                style = style,
                sourceId = sourceId,
                layerId = layerId,
                geoJsonFeature = geoJson.toString(),
                color = color,
                width = lineWidth,
            )

            Timber.d("Drew segment $segmentIndex with ${segmentRoute.routePoints.size} points, status: ${segmentRoute.status}")
        } catch (e: Exception) {
            Timber.e(e, "Error drawing segment route $segmentIndex")
        }
    }

    /**
     * Draw locker markers
     */
    private fun drawLockerMarkers(
        style: Style,
        lockerPositions: List<LockerPosition>,
    ) {
        lockerPositions.forEachIndexed { index, locker ->
            val sourceId = "locker-source-$index"
            val layerId = "locker-layer-$index"

            try {
                // Use different colors for different lockers
                val colors =
                    listOf(
                        Color.parseColor("#FF5722"), // Red-Orange
                        Color.parseColor("#9C27B0"), // Purple
                        Color.parseColor("#FF9800"), // Orange
                        Color.parseColor("#607D8B"), // Blue-Gray
                        Color.parseColor("#795548"), // Brown
                    )
                val color = colors[index % colors.size]

                mapLibreUtils.addIconMarker(
                    style = style,
                    sourceId = sourceId,
                    layerId = layerId,
                    position = locker.position,
                    iconRes = R.drawable.ic_map_pin_red,
                    tooltipText = locker.lockerName,
                    iconSize = 0.8f,
                )

                Timber.d("Drew locker marker: ${locker.lockerName} at ${locker.position}")
            } catch (e: Exception) {
                Timber.e(e, "Error drawing locker marker for ${locker.lockerId}")
            }
        }
    }

    /**
     * Draw drone marker with icon and heading
     */
    private fun drawDroneMarker(
        style: Style,
        dronePosition: LatLng,
        heading: Double? = null,
        droneId: String? = ""
    ) {
        try {
            // Create tooltip text with drone info
            val tooltipText =
                buildString {
                    append("Drone " + droneId)
                    heading?.let {
                        append("\nHướng: ${it.toInt()}°")
                    }
                }

            mapLibreUtils.addIconMarker(
                style = style,
                sourceId = "drone-source",
                layerId = "drone-layer",
                position = dronePosition,
                iconRes = com.delivery.core.R.drawable.ic_drone,
                tooltipText = tooltipText,
                iconSize = 1.0f,
            )

            Timber.d("Drew drone marker at: $dronePosition, heading: $heading")
        } catch (e: Exception) {
            Timber.e(e, "Error drawing drone marker")
        }
    }

    /**
     * Draw flight position markers with icons and tooltips
     */
    private fun drawFlightPositionMarkers(
        style: Style,
        routePoints: List<LatLng>,
    ) {
        if (routePoints.isEmpty()) return

        try {
            mapLibreUtils.addFlightPositionMarkers(
                style = style,
                positions = routePoints,
                iconRes = R.drawable.ic_map_pin_red,
                tooltipPrefix = "Hub",
                iconSize = 1.0f,
            )

            Timber.d("Drew ${routePoints.size} flight position markers")
        } catch (e: Exception) {
            Timber.e(e, "Error drawing flight position markers")
        }
    }

    /**
     * Clear all existing map layers
     */
    private fun clearAllMapLayers(style: Style) {
        val layersToRemove =
            listOf(
                "route-layer",
                "origin-layer",
                "destination-layer",
                "drone-layer",
            )
        val sourcesToRemove =
            listOf(
                "route-source",
                "origin-source",
                "destination-source",
                "drone-source",
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

        // Remove locker, segment, and flight position layers
        for (i in 0..20) {
            try {
                listOf("locker-layer-$i", "segment-route-layer-$i", "flight-position-layer-$i").forEach { layerName ->
                    if (style.getLayer(layerName) != null) {
                        style.removeLayer(layerName)
                    }
                }
                listOf("locker-source-$i", "segment-route-source-$i", "flight-position-source-$i").forEach { sourceName ->
                    if (style.getSource(sourceName) != null) {
                        style.removeSource(sourceName)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun drawDronePath(style: Style, dronePath: MutableList<LatLng>) {
        if (dronePath.isEmpty()) return

        // Create GeoJSON for the route
        val coordinates =
            dronePath.map { point ->
                arrayOf(point.longitude, point.latitude)
            }

        val coordinatesJsonArray = org.json.JSONArray()
        coordinates.forEach { coord ->
            val pointArray = org.json.JSONArray()
            pointArray.put(coord[0])
            pointArray.put(coord[1])
            coordinatesJsonArray.put(pointArray)
        }

        val geoJson =
            JSONObject().apply {
                put("type", "Feature")
                put(
                    "geometry",
                    JSONObject().apply {
                        put("type", "LineString")
                        put("coordinates", coordinatesJsonArray)
                    },
                )
            }

        Timber.tag("DronePath").d("GeoJSON: $geoJson")
        if (style.getLayer("drone-source") != null) {
            style.removeLayer("drone-source")
        }
        if (style.getSource("drone-layer") != null) {
            style.removeSource("drone-layer")
        }

        // Add route source and layer
        mapLibreUtils.addLineLayer(
            style = style,
            sourceId = "drone-source-${dronePath.size}",
            layerId = "drone-layer-${dronePath.size}",
            geoJsonFeature = geoJson.toString(),
            color = Color.parseColor("#FF4444"),
            width = 2.0f,
        )
    }

    private fun drawRouteOnMapDemo(style: Style, routePoints: List<LatLng>) {
        if (routePoints.isEmpty()) return

        // Create GeoJSON for the route
        val coordinates =
            routePoints.map { point ->
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

        val geoJson =
            JSONObject().apply {
                put("type", "Feature")
                put(
                    "geometry",
                    JSONObject().apply {
                        put("type", "LineString")
                        put("coordinates", coordinatesJsonArray)
                    },
                )
            }

        Timber.tag("DeliveryTracking").d("GeoJSON: $geoJson")

        // Add route source and layer
        mapLibreUtils.addLineLayer(
            style = style,
            sourceId = "route-source",
            layerId = "route-layer",
            geoJsonFeature = geoJson.toString(),
            color = Color.parseColor("#4AFF4444"),
            width = 6.0f,
        )

        // Move camera to show the route
        if (isFirstMoveCamera) {
            moveCameraToRoute(routePoints)
        }
    }

    private fun drawRouteOnMap(routePoints: List<LatLng>) {
        if (routePoints.isEmpty()) return

        mapLibreMap?.getStyle { style ->
            // Clear existing route layers safely
            clearAllMapLayers(style)

            // Create GeoJSON for the route
            val coordinates =
                routePoints.map { point ->
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

            val geoJson =
                JSONObject().apply {
                    put("type", "Feature")
                    put(
                        "geometry",
                        JSONObject().apply {
                            put("type", "LineString")
                            put("coordinates", coordinatesJsonArray)
                        },
                    )
                }

            Timber.tag("DeliveryTracking").d("GeoJSON: $geoJson")

            // Add route source and layer
            mapLibreUtils.addLineLayer(
                style = style,
                sourceId = "route-source",
                layerId = "route-layer",
                geoJsonFeature = geoJson.toString(),
                color = Color.parseColor("#4AFF4444"),
                width = 6.0f,
            )

//            // Draw markers for start and end points
//            if (routePoints.size >= 2) {
//                drawStartEndMarkers(style, routePoints.first(), routePoints.last())
//            }

            /*// Draw flight position markers
            drawFlightPositionMarkers(style, routePoints)*/

            // Move camera to show the route
            if (isFirstMoveCamera) {
            moveCameraToRoute(routePoints)
                }
        }
    }

    private fun drawStartEndMarkers(
        style: Style,
        startPoint: LatLng,
        endPoint: LatLng,
    ) {
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

        // Add start marker (green pin)
        mapLibreUtils.addIconMarker(
            style = style,
            sourceId = "origin-source",
            layerId = "origin-layer",
            position = startPoint,
            iconRes = R.drawable.ic_map_pin_red,
            tooltipText = "Điểm bắt đầu",
            iconSize = 1.0f,
        )

        // Add end marker (red pin)
        mapLibreUtils.addIconMarker(
            style = style,
            sourceId = "destination-source",
            layerId = "destination-layer",
            position = endPoint,
            iconRes = R.drawable.ic_map_pin_red,
            tooltipText = "Điểm kết thúc",
            iconSize = 1.0f,
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
                val bounds =
                    LatLngBounds.Builder()
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
                        val cameraPosition =
                            CameraPosition.Builder()
                                .target(bounds.center)
                                .zoom(13.0)
                                .build()
                        map.animateCamera(
                            CameraUpdateFactory.newCameraPosition(cameraPosition),
                            1000,
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
        viewAlive = false
        routeAnimator?.cancel()
        routeAnimator = null

        mapLibreUtils.handleMapLifecycle(mapView, MapLibreUtils.LifecycleMethod.ON_DESTROY)
        mapView = null
        mapLibreMap = null
        super.onDestroyView()
    }
}
