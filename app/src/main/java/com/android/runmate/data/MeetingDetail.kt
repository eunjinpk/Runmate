package com.android.runmate.data

/**
 * 모임 상세(#3) 화면에서 쓰는 모델. meetings 테이블 + 호스트 닉네임 + 참여인원 집계.
 */
data class MeetingDetail(
    val id: Int,
    val hostId: Int,
    val hostNickname: String,
    val title: String,
    val date: String,
    val time: String,
    val locationName: String,
    val description: String?,
    val maxPeople: Int,
    val isPublic: Boolean,
    val joinedCount: Int,
    val pace: String?
)

/**
 * 모임 상세 화면의 참여자 리스트 항목.
 * participationRate는 저장된 컬럼이 아니라 meeting_participants 기록으로 계산한 값입니다.
 */
data class Participant(
    val userId: Int,
    val nickname: String,
    val level: String,
    val participationRate: Int
)