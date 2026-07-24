package com.android.runmate.ui.meeting

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import com.android.runmate.data.MeetingDetail
import com.android.runmate.data.Participant
import com.android.runmate.data.PlaceRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 모임 상세 화면 (#3).
 * 거리 표시는 현재 확정된 스키마에 없어서 이 화면엔 없습니다. 벌금 제도는 팀 확정으로 폐지되었습니다.
 * 지도는 지도/장소 담당 화면이 완성되면 상단 자리표시자 영역을 실제 지도로 교체하면 됩니다.
 */
class MeetingDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEETING_ID = "extra_meeting_id"
    }

    private lateinit var dbHelper: DBHelper
    private var meetingId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meeting_detail)

        dbHelper = DBHelper(this)
        meetingId = intent.getIntExtra(EXTRA_MEETING_ID, -1)

        // 엣지-투-엣지 화면에서 3버튼 내비게이션 등 시스템 내비게이션 바가
        // 하단 버튼(참여하기 등)이랑 겹치지 않도록 여백 처리
        val bottomButtonBar = findViewById<LinearLayout>(R.id.bottomButtonBar)
        val bottomButtonBarBasePadding = bottomButtonBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bottomButtonBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomButtonBarBasePadding + systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        if (meetingId == -1) {
            Toast.makeText(this, "모임 정보를 불러올 수 없어요", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadDetail()
    }

    override fun onResume() {
        super.onResume()
        if (meetingId != -1) loadDetail()
    }

    private fun loadDetail() {
        val meeting = dbHelper.getMeetingDetail(meetingId)
        if (meeting == null) {
            Toast.makeText(this, "모임을 찾을 수 없어요", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        bindMeeting(meeting)
        bindParticipants(meeting)
    }

    private fun bindMeeting(meeting: MeetingDetail) {
        findViewById<TextView>(R.id.tvTitle).text = meeting.title
        findViewById<TextView>(R.id.tvLocation).text = "📍 ${meeting.locationName}"
        findViewById<TextView>(R.id.tvDateTime).text = "📅 ${formatRelativeDateTime(meeting.date, meeting.time)}"

        // 모임 장소명(예: "여의도한강공원")으로 실제 한강공원 이미지를 찾아서 보여줌
        findViewById<ImageView>(R.id.ivMapImage).setImageResource(
            PlaceRepository.getMapImageResource(meeting.locationName)
        )

        // 내가 만든 모임일 때만 삭제 버튼 노출
        val btnDelete = findViewById<TextView>(R.id.btnDeleteMeeting)
        val isHost = meeting.hostId == DBHelper.CURRENT_USER_ID
        if (isHost) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("모임을 삭제할까요?")
                    .setMessage("'${meeting.title}' 모임이 삭제되고, 참여자 기록도 함께 사라집니다.")
                    .setPositiveButton("삭제") { _, _ ->
                        dbHelper.deleteMeeting(meeting.id)
                        Toast.makeText(this, "모임을 삭제했어요", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        val tvFineAndPace = findViewById<TextView>(R.id.tvFineAndPace)
        val hasPace = !meeting.pace.isNullOrBlank()
        if (hasPace) {
            tvFineAndPace.visibility = View.VISIBLE
            tvFineAndPace.text = "⏰ 페이스 ${meeting.pace}/km"
        } else {
            tvFineAndPace.visibility = View.GONE
        }

        val tvPublicBadge = findViewById<TextView>(R.id.tvPublicBadge)
        if (meeting.isPublic) {
            tvPublicBadge.text = "공개"
            tvPublicBadge.backgroundTintList = getColorStateList(R.color.chip_public_bg)
            tvPublicBadge.setTextColor(getColor(R.color.chip_public_text))
        } else {
            tvPublicBadge.text = "비공개"
            tvPublicBadge.backgroundTintList = getColorStateList(R.color.chip_private_bg)
            tvPublicBadge.setTextColor(getColor(R.color.chip_private_text))
        }

        findViewById<TextView>(R.id.tvUrgentBadge).visibility =
            if (isWithin24Hours(meeting.date, meeting.time)) View.VISIBLE else View.GONE

        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        if (meeting.description.isNullOrBlank()) {
            tvDescription.visibility = View.GONE
        } else {
            tvDescription.visibility = View.VISIBLE
            tvDescription.text = meeting.description
        }

        findViewById<TextView>(R.id.tvParticipantCount).text =
            "참여자 ${meeting.joinedCount}/${meeting.maxPeople}"

        val spotsLeft = meeting.maxPeople - meeting.joinedCount
        val tvSpotsLeft = findViewById<TextView>(R.id.tvSpotsLeft)
        if (spotsLeft <= 0) {
            tvSpotsLeft.text = "모집 마감"
        } else {
            tvSpotsLeft.text = "${spotsLeft}자리 남음"
        }

        val btnJoin = findViewById<TextView>(R.id.btnJoin)
        val btnSecondary = findViewById<TextView>(R.id.btnSecondary)
        val alreadyJoined = dbHelper.hasUserJoined(meeting.id, DBHelper.CURRENT_USER_ID)

        when {
            alreadyJoined -> {
                // 참여 확정 상태: 왼쪽 = 취소하기, 오른쪽 = 종료하기
                btnSecondary.visibility = View.VISIBLE
                btnSecondary.text = "취소하기"
                btnSecondary.isEnabled = true
                btnSecondary.setBackgroundResource(R.drawable.bg_chip_unselected)
                btnSecondary.setTextColor(getColor(R.color.text_primary))
                btnSecondary.setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("참여를 취소할까요?")
                        .setMessage("'${meeting.title}' 모임 참여를 취소합니다.")
                        .setPositiveButton("확인") { _, _ ->
                            dbHelper.leaveMeeting(meeting.id, DBHelper.CURRENT_USER_ID)
                            Toast.makeText(this, "참여를 취소했어요", Toast.LENGTH_SHORT).show()
                            loadDetail()
                        }
                        .setNegativeButton("닫기", null)
                        .show()
                }

                btnJoin.text = "종료하기"
                btnJoin.isEnabled = true
                btnJoin.setBackgroundResource(R.drawable.bg_chip_selected)
                btnJoin.setTextColor(getColor(R.color.surface_white))
                btnJoin.setOnClickListener {
                    // 러닝 인증 화면(#7)으로 이동
                    val intent = Intent(this, com.android.runmate.ui.proof.RunProofActivity::class.java)
                    intent.putExtra(com.android.runmate.ui.proof.RunProofActivity.EXTRA_MEETING_ID, meeting.id)
                    intent.putExtra(com.android.runmate.ui.proof.RunProofActivity.EXTRA_MEETING_NAME, meeting.title)
                    startActivity(intent)
                }
            }
            spotsLeft <= 0 -> {
                btnSecondary.visibility = View.GONE

                btnJoin.text = "모집 마감"
                btnJoin.isEnabled = false
                btnJoin.setBackgroundResource(R.drawable.bg_chip_unselected)
                btnJoin.setTextColor(getColor(R.color.text_secondary))
            }
            else -> {
                // 아직 참여 전: 참여하기 버튼 하나만 보임
                btnSecondary.visibility = View.GONE

                btnJoin.text = "참여하기"
                btnJoin.isEnabled = true
                btnJoin.setBackgroundResource(R.drawable.bg_chip_selected)
                btnJoin.setTextColor(getColor(R.color.surface_white))
                btnJoin.setOnClickListener {
                    if (meeting.isPublic) {
                        showJoinConfirmDialog(meeting)
                    } else {
                        showInviteCodeDialog(meeting)
                    }
                }
            }
        }
    }

    /** 공개 모임: 그냥 확인만 하고 바로 참여 */
    private fun showJoinConfirmDialog(meeting: MeetingDetail) {
        AlertDialog.Builder(this)
            .setTitle("모임에 참여할까요?")
            .setMessage("'${meeting.title}'\n${formatRelativeDateTime(meeting.date, meeting.time)} · ${meeting.locationName}")
            .setPositiveButton("확인") { _, _ ->
                dbHelper.joinMeeting(meeting.id, DBHelper.CURRENT_USER_ID)
                Toast.makeText(this, "참여했어요!", Toast.LENGTH_SHORT).show()
                loadDetail()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /** 비공개 모임: 호스트한테 받은 초대코드를 입력해야 참여 가능 (시안 #14) */
    private fun showInviteCodeDialog(meeting: MeetingDetail) {
        val etCode = EditText(this).apply {
            hint = "초대코드 입력"
            setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(16))
        }

        AlertDialog.Builder(this)
            .setTitle("🔒 비공개 모임입니다")
            .setMessage("호스트에게 받은 초대코드를 입력하면\n모임에 입장할 수 있어요")
            .setView(etCode)
            .setPositiveButton("입장하기") { _, _ ->
                val inputCode = etCode.text.toString().trim()
                if (inputCode.isEmpty()) {
                    Toast.makeText(this, "초대코드를 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (inputCode.equals(meeting.inviteCode, ignoreCase = true)) {
                    dbHelper.joinMeeting(meeting.id, DBHelper.CURRENT_USER_ID)
                    Toast.makeText(this, "참여했어요!", Toast.LENGTH_SHORT).show()
                    loadDetail()
                } else {
                    Toast.makeText(this, "초대코드가 일치하지 않아요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun bindParticipants(meeting: MeetingDetail) {
        val container = findViewById<LinearLayout>(R.id.layoutParticipants)
        container.removeAllViews()

        val participants = dbHelper.getParticipants(meeting.id)

        // 호스트를 목록 맨 위에 보여주기 위해 항목 하나 추가
        val hostRow = buildParticipantRow(
            nickname = meeting.hostNickname,
            level = "-",
            rate = 100,
            isHost = true
        )
        container.addView(hostRow)

        participants.forEach { participant ->
            container.addView(buildParticipantRow(participant.nickname, participant.level, participant.participationRate, false))
        }
    }

    private fun buildParticipantRow(nickname: String, level: String, rate: Int, isHost: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 10, 0, 10)
        }

        val avatar = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(36, 36).apply {
                width = dp(36); height = dp(36)
            }
            background = androidx.core.content.ContextCompat.getDrawable(this@MeetingDetailActivity, R.drawable.bg_circle)
            backgroundTintList = getColorStateList(R.color.run_sky)
            gravity = android.view.Gravity.CENTER
            text = nickname.take(1)
            setTextColor(getColor(R.color.surface_white))
            textSize = 13f
        }

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(10)
            }
        }
        val nameView = TextView(this).apply {
            text = nickname
            setTextColor(getColor(R.color.text_primary))
            textSize = 14f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val subView = TextView(this).apply {
            text = "$level · 참여율 ${rate}%"
            setTextColor(getColor(R.color.text_secondary))
            textSize = 12f
        }
        textContainer.addView(nameView)
        textContainer.addView(subView)

        row.addView(avatar)
        row.addView(textContainer)

        if (isHost) {
            val hostTag = TextView(this).apply {
                text = "호스트"
                setTextColor(getColor(R.color.chip_public_text))
                background = androidx.core.content.ContextCompat.getDrawable(this@MeetingDetailActivity, R.drawable.bg_rounded_badge)
                backgroundTintList = getColorStateList(R.color.chip_public_bg)
                setPadding(dp(8), dp(4), dp(8), dp(4))
                textSize = 11f
            }
            row.addView(hostTag)
        }

        return row
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // ---- 날짜 포맷/마감임박 판정 (홈 화면 MeetingAdapter와 동일한 로직) ----

    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    private fun formatRelativeDateTime(date: String, time: String): String {
        val dayText = try {
            val meetingDate = dateOnlyFormat.parse(date) ?: return "$date $time"
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val target = Calendar.getInstance().apply {
                this.time = meetingDate
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val diffDays = ((target.timeInMillis - today.timeInMillis) / (24 * 60 * 60 * 1000L)).toInt()
            when (diffDays) {
                0 -> "오늘"
                1 -> "내일"
                2 -> "모레"
                else -> SimpleDateFormat("M월 d일(EEE)", Locale.KOREA).format(meetingDate)
            }
        } catch (e: Exception) {
            date
        }

        val timeText = try {
            val (hour, minute) = time.split(":").map { it.toInt() }
            val period = if (hour < 12) "오전" else "오후"
            val hour12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            "$period ${hour12}:${minute.toString().padStart(2, '0')}"
        } catch (e: Exception) {
            time
        }

        return "$dayText $timeText"
    }

    private fun isWithin24Hours(date: String, time: String): Boolean {
        return try {
            val meetingTime = dateTimeFormat.parse("$date $time") ?: return false
            val diffMillis = meetingTime.time - Date().time
            diffMillis in 0..(24 * 60 * 60 * 1000L)
        } catch (e: Exception) {
            false
        }
    }
}