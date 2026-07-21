package com.android.runmate.data

data class Meeting(
    val id: Int,
    val title: String,
    val date: String,
    val time: String,
    val locationName: String,
    val description: String?,
    val maxPeople: Int,
    val isPublic: Boolean,
    val joinedCount: Int,
    val hostNickname: String
)