package com.android.runmate.data

data class HanRiverPark(
    val id: Int,
    val name: String,
    val subInfo: String,
    val lat: Double,
    val lng: Double,
    val mapImageRes: Int
)

data class RunningCourse(
    val id: Int,
    val parkId: Int,
    val level: String,
    val distanceKm: Double,
    val title: String,
    val description: String,
    val routeImageRes: Int
)