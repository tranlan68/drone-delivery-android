package com.delivery.setting.ui.tracking

import com.google.android.gms.maps.model.LatLng

object PolylineUtils {
    
    /**
     * Decodes a polyline string into a list of LatLng points
     * Based on Google's polyline algorithm
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }

        return poly
    }
    
    /**
     * Creates a simplified route between two points for demo purposes
     */
    fun createSimplifiedRoute(start: LatLng, end: LatLng): List<LatLng> {
        val points = mutableListOf<LatLng>()
        
        // Add start point
        points.add(start)
        
        // Add intermediate points to create a more realistic route
        val latDiff = end.latitude - start.latitude
        val lngDiff = end.longitude - start.longitude
        
        // Create 5 intermediate points with slight curves
        for (i in 1..5) {
            val ratio = i / 6.0
            val lat = start.latitude + (latDiff * ratio) + (Math.sin(ratio * Math.PI) * 0.001)
            val lng = start.longitude + (lngDiff * ratio) + (Math.cos(ratio * Math.PI) * 0.001)
            points.add(LatLng(lat, lng))
        }
        
        // Add end point
        points.add(end)
        
        return points
    }
}
