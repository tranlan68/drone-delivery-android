package com.delivery.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.delivery.core.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapLibreUtils
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun initializeMapLibre() {
            MapLibre.getInstance(context)
        }

        fun setupMapView(
            mapView: MapView,
            savedInstanceState: Bundle?,
        ) {
            mapView.onCreate(savedInstanceState)
        }

        fun initializeMap(
            mapView: MapView,
            onMapReady: (MapLibreMap) -> Unit,
            onStyleLoaded: (Style) -> Unit,
        ) {
            val styleUri = "https://api.maptiler.com/maps/streets-v2/style.json?key=${BuildConfig.MAPTILER_API_KEY}"
            mapView.getMapAsync { map ->
                map.uiSettings?.apply {
                    isAttributionEnabled = false
                    isLogoEnabled = false
                }
                onMapReady(map)
                map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                    onStyleLoaded(style)
                }
            }
        }

        fun addCircleMarker(
            style: Style,
            sourceId: String,
            layerId: String,
            position: LatLng,
            @ColorInt color: Int,
            radius: Float = 6.0f,
            strokeWidth: Float = 2.0f,
        ) {
            val pointGeoJson =
                JSONObject(
                    "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[${position.longitude},${position.latitude}]}}",
                )

            if (style.getSource(sourceId) == null) {
                style.addSource(GeoJsonSource(sourceId, pointGeoJson.toString()))
            }

            if (style.getLayer(layerId) == null) {
                val circleLayer =
                    CircleLayer(layerId, sourceId)
                        .withProperties(
                            PropertyFactory.circleColor(color),
                            PropertyFactory.circleRadius(radius),
                            PropertyFactory.circleStrokeColor(Color.WHITE),
                            PropertyFactory.circleStrokeWidth(strokeWidth),
                        )
                style.addLayer(circleLayer)
            }
        }

        fun addLineLayer(
            style: Style,
            sourceId: String,
            layerId: String,
            geoJsonFeature: String,
            @ColorInt color: Int = Color.parseColor("#1E88E5"),
            width: Float = 4.0f,
        ) {
            val existingSource = style.getSource(sourceId) as? GeoJsonSource
            if (existingSource == null) {
                style.addSource(GeoJsonSource(sourceId, geoJsonFeature))
            } else {
                existingSource.setGeoJson(geoJsonFeature)
            }

            if (style.getLayer(layerId) == null) {
                val lineLayer =
                    LineLayer(layerId, sourceId)
                        .withProperties(
                            PropertyFactory.lineColor(color),
                            PropertyFactory.lineWidth(width),
//                            PropertyFactory.lineCap("round"),
//                            PropertyFactory.lineJoin("round"),
                        )
                style.addLayer(lineLayer)
            }
        }

        fun moveCameraToPosition(
            mapLibreMap: MapLibreMap?,
            position: LatLng,
            zoom: Double = 16.0,
        ) {
            val cameraPosition =
                CameraPosition.Builder()
                    .target(position)
                    .zoom(zoom)
                    .build()
            mapLibreMap?.cameraPosition = cameraPosition
        }

        fun removeLayerAndSource(
            style: Style,
            layerId: String,
            sourceId: String,
        ) {
            style.getLayer(layerId)?.let { style.removeLayer(it) }
            style.getSource(sourceId)?.let { style.removeSource(it) }
        }

        fun removeMultipleLayersAndSources(
            style: Style,
            prefix: String,
            count: Int,
        ) {
            for (i in 0 until count) {
                removeLayerAndSource(style, "$prefix-layer-$i", "$prefix-source-$i")
            }
        }

        /**
         * Convert drawable to bitmap
         */
        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            val bitmap =
                Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888,
                )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

    private fun drawableToBitmap(drawable: Drawable, intrinsicWidth: Int, intrinsicHeight: Int): Bitmap {
        val bitmap =
            Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

        /**
         * Add icon marker with tooltip
         */
        fun addIconMarker(
            style: Style,
            sourceId: String,
            layerId: String,
            position: LatLng,
            @DrawableRes iconRes: Int,
            tooltipText: String? = null,
            iconSize: Float = 1.0f,
        ) {
            try {
                // Convert drawable to bitmap
                val drawable = ContextCompat.getDrawable(context, iconRes)
                if (drawable == null) {
                    return
                }

                var bitmap: Bitmap? = null
                if (iconRes == com.delivery.core.R.drawable.ic_drone) {
                    bitmap = drawableToBitmap(drawable, 128, 128)
                } else {
                    bitmap = drawableToBitmap(drawable)
                }
                val imageId = "icon_${iconRes}_$sourceId"

                // Add image to style if not already added
                if (style.getImage(imageId) == null) {
                    style.addImage(imageId, bitmap)
                }

                // Create GeoJSON for the marker position
                val pointGeoJson =
                    JSONObject(
                        "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[${position.longitude},${position.latitude}]},\"properties\":{\"tooltip\":\"${tooltipText ?: ""}\"}}",
                    )

                // Add source
                if (style.getSource(sourceId) == null) {
                    style.addSource(GeoJsonSource(sourceId, pointGeoJson.toString()))
                }

                // Add symbol layer
                if (style.getLayer(layerId) == null) {
                    val symbolLayer =
                        SymbolLayer(layerId, sourceId)
                            .withProperties(
                                PropertyFactory.textField("{tooltip}"),
                                PropertyFactory.textSize(12f),
                                PropertyFactory.textColor(Color.BLACK),
                                PropertyFactory.textHaloColor(Color.WHITE),
                                PropertyFactory.textHaloWidth(1.5f),
                                PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
                                PropertyFactory.textOffset(arrayOf(0f, -1.2f)),
                                PropertyFactory.iconImage(imageId),
                                PropertyFactory.iconSize(iconSize),
                                PropertyFactory.iconAllowOverlap(true),
                                PropertyFactory.iconIgnorePlacement(true),
                                PropertyFactory.textAllowOverlap(true),
                                PropertyFactory.textIgnorePlacement(true)
                            )
                    style.addLayer(symbolLayer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Add multiple icon markers for flight positions
         */
        fun addFlightPositionMarkers(
            style: Style,
            positions: List<LatLng>,
            @DrawableRes iconRes: Int,
            tooltipPrefix: String = "Điểm",
            iconSize: Float = 1.0f,
        ) {
            positions.forEachIndexed { index, position ->
                val sourceId = "flight-position-source-$index"
                val layerId = "flight-position-layer-$index"
                val tooltipText =
                    when (index) {
                        0 -> "Hub VMC"
                        1 -> "Hub Trung Chuyển"
                        2 -> "Hub Giao Hàng"
                        else -> "$tooltipPrefix ${index + 1}"
                    }

                addIconMarker(
                    style = style,
                    sourceId = sourceId,
                    layerId = layerId,
                    position = position,
                    iconRes = iconRes,
                    tooltipText = tooltipText,
                    iconSize = iconSize,
                )
            }
        }

        fun handleMapLifecycle(
            mapView: MapView?,
            lifecycleMethod: LifecycleMethod,
        ) {
            when (lifecycleMethod) {
                LifecycleMethod.ON_START -> mapView?.onStart()
                LifecycleMethod.ON_RESUME -> mapView?.onResume()
                LifecycleMethod.ON_PAUSE -> mapView?.onPause()
                LifecycleMethod.ON_STOP -> mapView?.onStop()
                LifecycleMethod.ON_LOW_MEMORY -> mapView?.onLowMemory()
                LifecycleMethod.ON_DESTROY -> mapView?.onDestroy()
            }
        }

        enum class LifecycleMethod {
            ON_START,
            ON_RESUME,
            ON_PAUSE,
            ON_STOP,
            ON_LOW_MEMORY,
            ON_DESTROY,
        }
    }
