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

        // 1. 여의도
        RunningCourse(101, 1, "초보", 3.2, "마포대교 왕복 코스", "여의나루역 → 마포대교 아래 → 원점", R.drawable.map_yeouido),
        RunningCourse(102, 1, "중급", 5.0, "서강대교 순환 코스", "여의도공원 → 서강대교 → 밤섬 조망 → 원점", R.drawable.map_yeouido),
        RunningCourse(103, 1, "고수", 10.0, "여의도-반포 장거리 코스", "여의도 → 한강대교 → 반포 잠수교 → 반환", R.drawable.map_yeouido),

        // 2. 반포
        RunningCourse(201, 2, "초보", 3.0, "잠수교 왕복 코스", "신반포역 → 잠수교 → 원점", R.drawable.map_banpo),
        RunningCourse(202, 2, "중급", 5.5, "반포 달빛무지개분수 코스", "반포한강공원 → 달빛무지개분수 → 동작대교 → 반환", R.drawable.map_banpo),
        RunningCourse(203, 2, "고수", 9.5, "반포-이촌 장거리 코스", "반포 → 한남대교 → 이촌한강공원 → 반환", R.drawable.map_banpo),

        // 3. 뚝섬
        RunningCourse(301, 3, "초보", 3.5, "자벌레 전망대 코스", "뚝섬유원지역 → 자벌레 전망대 → 원점", R.drawable.map_ttukseom),
        RunningCourse(302, 3, "중급", 6.0, "뚝섬 수영장 순환 코스", "뚝섬한강공원 → 청담대교 → 수영장 순환 → 원점", R.drawable.map_ttukseom),
        RunningCourse(303, 3, "고수", 10.5, "뚝섬-잠실 장거리 코스", "뚝섬 → 청담대교 → 잠실한강공원 → 반환", R.drawable.map_ttukseom),

        // 4. 잠실
        RunningCourse(401, 4, "초보", 3.3, "잠실 수변무대 코스", "잠실나루역 → 수변무대 → 원점", R.drawable.map_jamsil),
        RunningCourse(402, 4, "중급", 5.8, "잠실철교 순환 코스", "잠실한강공원 → 잠실철교 → 자연학습장 → 원점", R.drawable.map_jamsil),
        RunningCourse(403, 4, "고수", 11.0, "잠실-광나루 장거리 코스", "잠실 → 강동대교 → 광나루한강공원 → 반환", R.drawable.map_jamsil),

        // 5. 망원
        RunningCourse(501, 5, "초보", 2.8, "망원 유수지 코스", "망원역 → 망원 유수지 생태공원 → 원점", R.drawable.map_mangwon),
        RunningCourse(502, 5, "중급", 4.8, "망원-양화대교 코스", "망원한강공원 → 양화대교 → 선유도 조망 → 반환", R.drawable.map_mangwon),

        // 6. 이촌
        RunningCourse(601, 6, "초보", 3.0, "이촌 자전거공원 코스", "이촌역 → 자전거공원 → 원점", R.drawable.map_ichon),
        RunningCourse(602, 6, "중급", 5.2, "이촌 반포대교 코스", "이촌한강공원 → 반포대교 → 노들섬 조망 → 반환", R.drawable.map_ichon),
        RunningCourse(603, 6, "고수", 9.8, "이촌-여의도 장거리 코스", "이촌 → 한강철교 → 여의도한강공원 → 반환", R.drawable.map_ichon),

        // 7. 잠원
        RunningCourse(701, 7, "초보", 2.5, "잠원 나들목 코스", "잠원역 → 신반포로 나들목 → 원점", R.drawable.map_jamwon),
        RunningCourse(702, 7, "중급", 4.5, "잠원-반포 순환 코스", "잠원한강공원 → 한남대교 → 반포 방향 반환", R.drawable.map_jamwon),

        // 8. 광나루
        RunningCourse(801, 8, "초보", 2.7, "광나루 조정경기장 코스", "광나루역 → 서울조정경기장 → 원점", R.drawable.map_gwangnaru),
        RunningCourse(802, 8, "중급", 4.9, "광나루-잠실 코스", "광나루한강공원 → 강동대교 → 잠실 방향 반환", R.drawable.map_gwangnaru),

        // 9. 난지
        RunningCourse(901, 9, "초보", 3.6, "난지 캠핑장 코스", "월드컵경기장역 → 난지캠핑장 → 원점", R.drawable.map_nanji),
        RunningCourse(902, 9, "중급", 6.2, "난지 하늘공원 순환 코스", "난지한강공원 → 하늘공원 둘레 → 원점", R.drawable.map_nanji),
        RunningCourse(903, 9, "고수", 10.2, "난지-망원 장거리 코스", "난지 → 성산대교 → 망원한강공원 → 반환", R.drawable.map_nanji),

        // 10. 양화
        RunningCourse(1001, 10, "초보", 2.6, "선유도공원 조망 코스", "당산역 → 선유도공원 조망 → 원점", R.drawable.map_yanghwa),
        RunningCourse(1002, 10, "중급", 4.7, "양화대교 순환 코스", "양화한강공원 → 양화대교 → 원점", R.drawable.map_yanghwa),

        // 11. 강서
        RunningCourse(1101, 11, "초보", 3.1, "강서습지생태공원 코스", "가양역 → 강서습지생태공원 → 원점", R.drawable.map_gangseo),
        RunningCourse(1102, 11, "중급", 5.3, "방화대교 순환 코스", "강서한강공원 → 방화대교 → 원점", R.drawable.map_gangseo)
    )
}