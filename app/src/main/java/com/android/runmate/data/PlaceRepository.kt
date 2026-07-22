package com.android.runmate.data

import com.android.runmate.R

object PlaceRepository {

    val parks = listOf(
        HanRiverPark(1, "여의도", "여의나루역 · 코스 4", 37.5285, 126.9335, R.drawable.map_yeouido),
        HanRiverPark(2, "반포", "신반포역 · 코스 3", 37.4970, 126.9970, R.drawable.map_banpo),
        HanRiverPark(3, "뚝섬", "뚝섬유원지역 · 코스 3", 37.5310, 127.0669, R.drawable.map_ttukseom),
        HanRiverPark(4, "잠실", "잠실나루역 · 코스 4", 37.5196, 127.0827, R.drawable.map_jamsil),
        HanRiverPark(5, "망원", "망원역 · 코스 2", 37.5556, 126.8983, R.drawable.map_mangwon),
        HanRiverPark(6, "이촌", "이촌역 · 코스 3", 37.5183, 126.9722, R.drawable.map_ichon),
        HanRiverPark(7, "잠원", "잠원역 · 코스 2", 37.5219, 127.0143, R.drawable.map_jamwon),
        HanRiverPark(8, "광나루", "광나루역 · 코스 2", 37.5459, 127.1104, R.drawable.map_gwangnaru),
        HanRiverPark(9, "난지", "월드컵경기장역 · 코스 3", 37.5697, 126.8791, R.drawable.map_nanji),
        HanRiverPark(10, "양화", "당산역 · 코스 2", 37.5384, 126.9017, R.drawable.map_yanghwa),
        HanRiverPark(11, "강서", "가양역 · 코스 2", 37.5658, 126.8309, R.drawable.map_gangseo)
    )

    fun getParkByName(name: String): HanRiverPark? = parks.find { name.contains(it.name) }

    fun getMapImageResource(locationName: String): Int {
        val park = parks.find { locationName.contains(it.name) }
        return park?.mapImageRes ?: R.drawable.map_default
    }

    fun getCoursesByPark(parkId: Int): List<RunningCourse> = allCourses.filter { it.parkId == parkId }

    private val allCourses = listOf(
        RunningCourse(1, 1, "초보", 3.2, "마포대교 왕복 코스", "여의나루역 → 마포대교 아래 → 원점", R.drawable.map_yeouido),
        RunningCourse(2, 1, "중급", 5.0, "서강대교 순환 코스", "여의도공원 → 서강대교 → 밤섬 조망 → 원점", R.drawable.map_yeouido),
        RunningCourse(3, 1, "고수", 10.0, "여의도-반포 장거리 코스", "여의도 → 한강대교 → 반포 잠수교 → 반환", R.drawable.map_yeouido)
    )
}