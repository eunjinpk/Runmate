package com.android.runmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.data.DBHelper

class MyPageActivity : AppCompatActivity() {

    private val currentUserId = 1 // TODO: 로그인 연결되면 실제 유저 id로 교체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        loadProfile()
        loadStats()
        // 사진/이력은 나중에 연결 예정이라 지금은 빈 상태 유지
    }

    private fun loadProfile() {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT nickname, level, profile_img FROM users WHERE id = ?",
            arrayOf(currentUserId.toString())
        )

        val tvNickname = findViewById<android.widget.TextView>(R.id.tvNickname)
        val tvLevelBadge = findViewById<android.widget.TextView>(R.id.tvLevelBadge)
        val tvProfileInitial = findViewById<android.widget.TextView>(R.id.tvProfileInitial)

        cursor.use {
            if (it.moveToFirst()) {
                val nickname = it.getString(it.getColumnIndexOrThrow("nickname")) ?: "새 유저"
                val level = it.getString(it.getColumnIndexOrThrow("level")) ?: "초보"

                tvNickname.text = nickname
                tvLevelBadge.text = "$level 러너"
                tvProfileInitial.text = nickname.take(1)
            } else {
                // 유저 정보가 아직 없는 초기 상태
                tvNickname.text = "새 유저"
                tvLevelBadge.text = "초보 러너"
                tvProfileInitial.text = "새"
            }
        }
        db.close()
    }

    private fun loadStats() {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val tvMeetingCount = findViewById<android.widget.TextView>(R.id.tvMeetingCount)
        val tvTotalDistance = findViewById<android.widget.TextView>(R.id.tvTotalDistance)
        val tvAttendRate = findViewById<android.widget.TextView>(R.id.tvAttendRate)

        // 참여 모임 수
        val meetingCursor = db.rawQuery(
            "SELECT COUNT(*) FROM meeting_participants WHERE user_id = ?",
            arrayOf(currentUserId.toString())
        )
        var meetingCount = 0
        meetingCursor.use { if (it.moveToFirst()) meetingCount = it.getInt(0) }

        // 누적 거리
        val distanceCursor = db.rawQuery(
            "SELECT IFNULL(SUM(distance), 0) FROM running_records WHERE user_id = ?",
            arrayOf(currentUserId.toString())
        )
        var totalDistance = 0.0
        distanceCursor.use { if (it.moveToFirst()) totalDistance = it.getDouble(0) }

        tvMeetingCount.text = meetingCount.toString()
        tvTotalDistance.text = "${totalDistance}km"

        // 참여율: 신규 유저는 계산할 기록 자체가 없으니 0%
        tvAttendRate.text = if (meetingCount == 0) "0%" else "계산 필요"

        db.close()
    }
}