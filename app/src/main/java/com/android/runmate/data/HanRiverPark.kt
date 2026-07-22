package com.android.runmate.data

data class HanRiverPark(
    val id: Int,
    val name: String,
    val subInfo: String,
    val lat: Double,
    val lng: Double,
    val mapImageRes: Int,
    val region: String,          // "강북" or "강남"
    var isFavorite: Boolean = false
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