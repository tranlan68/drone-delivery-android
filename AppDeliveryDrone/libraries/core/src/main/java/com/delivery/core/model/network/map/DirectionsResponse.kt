package com.delivery.core.model.network.map

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String,
)

data class Route(
    val legs: List<Leg>,
    val overview_polyline: OverviewPolyline,
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    val end_address: String,
    val start_address: String,
    val steps: List<Step>,
)

data class Step(
    val distance: Distance,
    val duration: Duration,
    val end_location: Location,
    val start_location: Location,
    val polyline: OverviewPolyline,
)

data class Distance(
    val text: String,
    val value: Int,
)

data class Duration(
    val text: String,
    val value: Int,
)

data class Location(
    val lat: Double,
    val lng: Double,
)

data class OverviewPolyline(
    val points: String,
)
