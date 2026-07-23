package com.android.runmate

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.data.DBHelper

class MyPageActivity : AppCompatActivity() {

    private val currentUserId = 1 // TODO: 로그인 연결되면 실제 유저 id로 교체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        loadProfile()
        loadStats()
        setupPhotoGrid()

        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
        loadStats()
    }

    private fun setupPhotoGrid() {
        val dummyPhotos = listOf(
            PhotoItem(R.drawable.running_photo1, "5.2km"),
            PhotoItem(R.drawable.running_photo2, "Day3"),
            PhotoItem(R.drawable.running_photo3, "8.0km")
        )

        val tvPhotoEmpty = findViewById<TextView>(R.id.tvPhotoEmpty)
        val recyclerPhotos = findViewById<RecyclerView>(R.id.recyclerPhotos)

        tvPhotoEmpty.visibility = android.view.View.GONE
        recyclerPhotos.visibility = android.view.View.VISIBLE
        recyclerPhotos.layoutManager = GridLayoutManager(this, 3)
        recyclerPhotos.adapter = PhotoGridAdapter(dummyPhotos)

        findViewById<TextView>(R.id.tvSeeAllPhotos).setOnClickListener {
            startActivity(Intent(this, PhotoGalleryActivity::class.java))
        }
    }

    private fun loadProfile() {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT nickname, level, profile_img FROM users WHERE id = ?",
            arrayOf(currentUserId.toString())
        )

        val tvNickname = findViewById<TextView>(R.id.tvNickname)
        val tvLevelBadge = findViewById<TextView>(R.id.tvLevelBadge)
        val tvProfileInitial = findViewById<TextView>(R.id.tvProfileInitial)

        cursor.use {
            if (it.moveToFirst()) {
                val nickname = it.getString(it.getColumnIndexOrThrow("nickname")) ?: "새 유저"
                val level = it.getString(it.getColumnIndexOrThrow("level")) ?: "초보"

                tvNickname.text = nickname
                tvLevelBadge.text = "$level 러너"
                tvProfileInitial.text = nickname.take(1)
            } else {
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

        val tvMeetingCount = findViewById<TextView>(R.id.tvMeetingCount)
        val tvTotalDistance = findViewById<TextView>(R.id.tvTotalDistance)
        val tvAttendRate = findViewById<TextView>(R.id.tvAttendRate)

        val meetingCursor = db.rawQuery(
            "SELECT COUNT(*) FROM meeting_participants WHERE user_id = ?",
            arrayOf(currentUserId.toString())
        )
        var meetingCount = 0
        meetingCursor.use { if (it.moveToFirst()) meetingCount = it.getInt(0) }

        val distanceCursor = db.rawQuery(
            "SELECT IFNULL(SUM(distance), 0) FROM running_records WHERE user_id = ?",
            arrayOf(currentUserId.toString())
        )
        var totalDistance = 0.0
        distanceCursor.use { if (it.moveToFirst()) totalDistance = it.getDouble(0) }

        tvMeetingCount.text = meetingCount.toString()
        tvTotalDistance.text = "${totalDistance}km"
        tvAttendRate.text = if (meetingCount == 0) "0%" else "계산 필요"

        db.close()
    }
}