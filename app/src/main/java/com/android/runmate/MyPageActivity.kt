package com.android.runmate

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.data.DBHelper
import com.android.runmate.ui.auth.LoginActivity
import com.android.runmate.util.SessionManager

class MyPageActivity : AppCompatActivity() {

    private val currentUserId = DBHelper.CURRENT_USER_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // 3버튼 내비게이션 등 시스템 내비게이션 바가 하단 탭바랑 안 겹치도록 여백 처리
        val bottomNavBar = findViewById<LinearLayout>(R.id.bottomNavBar)
        val bottomNavBarBasePadding = bottomNavBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomNavBarBasePadding + systemBars.bottom)
            insets
        }

        loadProfile()
        loadStats()
        setupPhotoGrid()

        // 정보수정(설정) 버튼 → ProfileSettingsActivity로 이동
        findViewById<ImageView>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        // 하단 탭바 이동
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, com.android.runmate.ui.home.HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navPlace).setOnClickListener {
            startActivity(Intent(this, com.android.runmate.ui.place.ParkSelectActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navRanking).setOnClickListener {
            startActivity(Intent(this, com.android.runmate.ui.ranking.RankingActivity::class.java))
        }

        findViewById<TextView>(R.id.btnRunProofDirect).setOnClickListener {
            openRunProofPicker()
        }

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }

    /** 참여한 모임 중 어느 모임 러닝을 인증할지 골라서 RunProofActivity로 이동 */
    private fun openRunProofPicker() {
        val dbHelper = DBHelper(this)
        val joinedMeetings = dbHelper.getJoinedMeetingsForUser(currentUserId)

        if (joinedMeetings.isEmpty()) {
            android.widget.Toast.makeText(this, "참여한 모임이 없어요. 먼저 모임에 참여해주세요!", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val titles = joinedMeetings.map { it.second }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("어느 러닝을 인증할까요?")
            .setItems(titles) { _, index ->
                val (meetingId, meetingTitle) = joinedMeetings[index]
                val intent = Intent(this, com.android.runmate.ui.proof.RunProofActivity::class.java)
                intent.putExtra(com.android.runmate.ui.proof.RunProofActivity.EXTRA_MEETING_ID, meetingId)
                intent.putExtra(com.android.runmate.ui.proof.RunProofActivity.EXTRA_MEETING_NAME, meetingTitle)
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // 정보수정 화면 등 다녀온 뒤 최신 정보로 다시 불러오기
        loadProfile()
        loadStats()
    }

    /** 로그아웃: 세션 지우고 로그인 화면으로 이동, 뒤로가기로 다시 못 돌아오게 스택 정리 */
    private fun logout() {
        SessionManager.clear(this)
        DBHelper.CURRENT_USER_ID = 1 // 로그인 전 기본값으로 되돌림

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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