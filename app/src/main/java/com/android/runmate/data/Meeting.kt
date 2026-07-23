package com.android.runmate.data

/**
 * meetings 테이블 + 참여인원 집계값 + 모임장 닉네임을 담는 화면용 모델.
 * DB 원본 스키마와 컬럼명을 최대한 그대로 맞췄습니다.
 */
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