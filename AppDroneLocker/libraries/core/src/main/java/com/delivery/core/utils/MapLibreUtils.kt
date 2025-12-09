package com.delivery.core.utils

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.annotation.ColorInt
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
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapLibreUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun initializeMapLibre() {
        MapLibre.getInstance(context)
    }

    fun setupMapView(mapView: MapView, savedInstanceState: Bundle?) {
        mapView.onCreate(savedInstanceState)
    }

    fun initializeMap(
        mapView: MapView,
        onMapReady: (MapLibreMap) -> Unit,
        onStyleLoaded: (Style) -> Unit
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
        strokeWidth: Float = 2.0f
    ) {
        val pointGeoJson = JSONObject(
            "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[${position.longitude},${position.latitude}]}}"
        )
        
        if (style.getSource(sourceId) == null) {
            style.addSource(GeoJsonSource(sourceId, pointGeoJson.toString()))
        }
        
        if (style.getLayer(layerId) == null) {
            val circleLayer = CircleLayer(layerId, sourceId)
                .withProperties(
                    PropertyFactory.circleColor(color),
                    PropertyFactory.circleRadius(radius),
                    PropertyFactory.circleStrokeColor(Color.WHITE),
                    PropertyFactory.circleStrokeWidth(strokeWidth)
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
        width: Float = 4.0f
    ) {
        val existingSource = style.getSource(sourceId) as? GeoJsonSource
        if (existingSource == null) {
            style.addSource(GeoJsonSource(sourceId, geoJsonFeature))
        } else {
            existingSource.setGeoJson(geoJsonFeature)
        }
        
        if (style.getLayer(layerId) == null) {
            val lineLayer = LineLayer(layerId, sourceId)
                .withProperties(
                    PropertyFactory.lineColor(color),
                    PropertyFactory.lineWidth(width),
                    PropertyFactory.lineCap("round"),
                    PropertyFactory.lineJoin("round")
                )
            style.addLayer(lineLayer)
        }
    }

    fun moveCameraToPosition(
        mapLibreMap: MapLibreMap?,
        position: LatLng,
        zoom: Double = 16.0
    ) {
        val cameraPosition = CameraPosition.Builder()
            .target(position)
            .zoom(zoom)
            .build()
        mapLibreMap?.cameraPosition = cameraPosition
    }

    fun removeLayerAndSource(style: Style, layerId: String, sourceId: String) {
        style.getLayer(layerId)?.let { style.removeLayer(it) }
        style.getSource(sourceId)?.let { style.removeSource(it) }
    }

    fun removeMultipleLayersAndSources(style: Style, prefix: String, count: Int) {
        for (i in 0 until count) {
            removeLayerAndSource(style, "$prefix-layer-$i", "$prefix-source-$i")
        }
    }

    fun handleMapLifecycle(
        mapView: MapView?,
        lifecycleMethod: LifecycleMethod
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
        ON_DESTROY
    }
}
