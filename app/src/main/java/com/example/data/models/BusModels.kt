package com.example.data.models

data class BusRoute(
    val id: String,
    val number: String,
    val title: String,
    val color: String,
    val mapIframeSrc: String?,
    val routes: List<RouteDirection>,
    val directionRoutes: DirectionRoutesData?,
    val isUrban: Boolean,
    val fare: String,
    val fareNote: String?
)

data class RouteDirection(
    val direction: String,
    val workDays: String?,
    val stops: List<StopTimes>
)

data class StopTimes(
    val name: String,
    val times: List<String>
)

data class DirectionRoutesData(
    val forward: DirectionData?,
    val backward: DirectionData?
)

data class DirectionData(
    val name: String,
    val workDays: String?,
    val departureTimes: List<String>,
    val stops: List<DirectionStop>
)

data class DirectionStop(
    val name: String,
    val offsetMin: Int
)

data class TripPlanResult(
    val busNumber: String,
    val busTitle: String,
    val color: String,
    val dirName: String,
    val urban: Boolean,
    val fromName: String,
    val toName: String,
    val boardMin: Int,
    val arriveMin: Int?,
    val rideMin: Int?,
    val note: String?,
    val diff: Int,
    val today: Boolean,
    val bus: BusRoute
)

data class NextDepartureInfo(
    val minutes: Int,
    val timeFormatted: String,
    val stopName: String,
    val diffMinutes: Int,
    val note: String?
)
