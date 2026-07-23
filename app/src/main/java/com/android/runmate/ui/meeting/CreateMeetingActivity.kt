package com.android.runmate.ui.meeting

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import java.util.Calendar

/**
 * 모임 만들기 화면 (#2).
 * 벌금 제도는 팀 확정으로 폐지되어 이 화면엔 없습니다.
 *
 * 장소는 지도/장소 담당(②번)의 한강공원 선택(#6)→러닝코스 추천(#6-2) 화면에서
 * 고른 다음 이 화면으로 넘어오는 게 정식 흐름입니다.
 * 그 화면에서 아래 Intent extra로 값을 담아서 이 액티비티를 열면,
 * 장소가 자동으로 채워지고 직접 타이핑할 필요가 없어집니다.
 */
class CreateMeetingActivity : AppCompatActivity() {

    companion object {
        // 지도/장소 담당(②번) 쪽에서 이 키로 값을 담아 Intent를 보내주면 됩니다.
        // 지도/장소 담당(②번) CourseListActivity가 실제로 보내는 키 이름과 맞춰야 합니다.
        const val EXTRA_LOCATION_NAME = "location_name" // 필수: 예) "여의도 한강공원"
        const val EXTRA_LAT = "lat" // 선택: Double
        const val EXTRA_LNG = "lng" // 선택: Double

        // 코스 레벨(초보/중급/고수) 표시를 위해 추가로 받고 싶은 값들 (팀원 쪽에 요청 필요)
        const val EXTRA_COURSE_NAME = "course_name" // 선택: 예) "서강대교 순환 코스"
        const val EXTRA_COURSE_LEVEL = "course_level" // 선택: 예) "중급"
        const val EXTRA_COURSE_DISTANCE = "course_distance" // 선택: Double, 예) 5.0
    }

    private lateinit var dbHelper: DBHelper

    private lateinit var etTitle: EditText
    private lateinit var etLocation: EditText
    private lateinit var etInviteCode: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnPickDate: TextView
    private lateinit var btnPickTime: TextView
    private lateinit var tvPeopleCount: TextView
    private lateinit var btnPublic: TextView
    private lateinit var btnPrivate: TextView

    private var selectedYear = 0
    private var selectedMonth = 0
    private var selectedDay = 0
    private var selectedHour = -1
    private var selectedMinute = -1
    private var peopleCount = 6
    private var isPublic = true
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_meeting)

        dbHelper = DBHelper(this)

        etTitle = findViewById(R.id.etTitle)
        etLocation = findViewById(R.id.etLocation)
        etInviteCode = findViewById(R.id.etInviteCode)
        etDescription = findViewById(R.id.etDescription)
        btnPickDate = findViewById(R.id.btnPickDate)
        btnPickTime = findViewById(R.id.btnPickTime)
        tvPeopleCount = findViewById(R.id.tvPeopleCount)
        btnPublic = findViewById(R.id.btnPublic)
        btnPrivate = findViewById(R.id.btnPrivate)

        // 장소 선택 화면(#6/#6-2)에서 넘어온 경우, 자동으로 채우고 직접 수정 못 하게 잠급니다.
        val incomingLocation = intent.getStringExtra(EXTRA_LOCATION_NAME)
        if (!incomingLocation.isNullOrBlank()) {
            val courseName = intent.getStringExtra(EXTRA_COURSE_NAME)
            val courseLevel = intent.getStringExtra(EXTRA_COURSE_LEVEL)
            val courseDistance = if (intent.hasExtra(EXTRA_COURSE_DISTANCE)) {
                intent.getDoubleExtra(EXTRA_COURSE_DISTANCE, 0.0)
            } else null

            val displayText = buildString {
                append(incomingLocation)
                if (!courseName.isNullOrBlank()) append(" · $courseName")
                if (!courseLevel.isNullOrBlank() || courseDistance != null) {
                    val detail = listOfNotNull(
                        courseLevel,
                        courseDistance?.let { "${it}km" }
                    ).joinToString(" · ")
                    append(" ($detail)")
                }
            }

            etLocation.setText(displayText)
            etLocation.isFocusable = false
            etLocation.isClickable = false
            if (intent.hasExtra(EXTRA_LAT)) selectedLat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
            if (intent.hasExtra(EXTRA_LNG)) selectedLng = intent.getDoubleExtra(EXTRA_LNG, 0.0)
        }

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        btnPickDate.setOnClickListener { showDatePicker() }
        btnPickTime.setOnClickListener { showTimePicker() }

        findViewById<TextView>(R.id.btnPeopleMinus).setOnClickListener {
            if (peopleCount > 2) {
                peopleCount--
                tvPeopleCount.text = "${peopleCount}명"
            }
        }
        findViewById<TextView>(R.id.btnPeoplePlus).setOnClickListener {
            if (peopleCount < 50) {
                peopleCount++
                tvPeopleCount.text = "${peopleCount}명"
            }
        }

        btnPublic.setOnClickListener { setPublic(true) }
        btnPrivate.setOnClickListener { setPublic(false) }

        findViewById<TextView>(R.id.btnSubmit).setOnClickListener { submit() }
    }

    private fun showDatePicker() {
        val today = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedYear = year
                selectedMonth = month
                selectedDay = dayOfMonth
                val isToday = year == today.get(Calendar.YEAR) &&
                        month == today.get(Calendar.MONTH) &&
                        dayOfMonth == today.get(Calendar.DAY_OF_MONTH)
                btnPickDate.text = if (isToday) {
                    "📅 오늘"
                } else {
                    "📅 %d월 %d일".format(month + 1, dayOfMonth)
                }
            },
            today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                btnPickTime.text = "⏰ %02d:%02d".format(hourOfDay, minute)
            },
            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
        ).show()
    }

    private fun setPublic(publicSelected: Boolean) {
        isPublic = publicSelected
        if (publicSelected) {
            btnPublic.setBackgroundResource(R.drawable.bg_chip_selected)
            btnPublic.setTextColor(getColor(R.color.surface_white))
            btnPrivate.setBackgroundResource(R.drawable.bg_chip_unselected)
            btnPrivate.setTextColor(getColor(R.color.text_primary))
            etInviteCode.visibility = android.view.View.GONE
        } else {
            btnPrivate.setBackgroundResource(R.drawable.bg_chip_selected)
            btnPrivate.setTextColor(getColor(R.color.surface_white))
            btnPublic.setBackgroundResource(R.drawable.bg_chip_unselected)
            btnPublic.setTextColor(getColor(R.color.text_primary))
            etInviteCode.visibility = android.view.View.VISIBLE
        }
    }

    private fun submit() {
        val title = etTitle.text.toString().trim()
        val location = etLocation.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val inviteCode = etInviteCode.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "모임 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "장소를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDay == 0) {
            Toast.makeText(this, "날짜를 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedHour == -1) {
            Toast.makeText(this, "시간을 선택해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isPublic && inviteCode.isEmpty()) {
            Toast.makeText(this, "비공개 모임은 초대코드를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val dateStr = "%04d-%02d-%02d".format(selectedYear, selectedMonth + 1, selectedDay)
        val timeStr = "%02d:%02d".format(selectedHour, selectedMinute)

        dbHelper.insertMeeting(
            hostId = DBHelper.CURRENT_USER_ID,
            title = title,
            date = dateStr,
            time = timeStr,
            locationName = location,
            lat = selectedLat,
            lng = selectedLng,
            maxPeople = peopleCount,
            isPublic = isPublic,
            inviteCode = if (isPublic) null else inviteCode,
            description = description.ifEmpty { null }
        )

        Toast.makeText(this, "모임이 생성되었습니다", Toast.LENGTH_SHORT).show()

        // 장소선택(#6)→코스추천(#6-2)→모임만들기(#4)로 쌓인 화면 스택을 다 정리하고
        // 곧바로 홈 화면으로 이동합니다 (뒤로가기 여러 번 안 눌러도 되게).
        val homeIntent = android.content.Intent(this, com.android.runmate.ui.home.HomeActivity::class.java)
        homeIntent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(homeIntent)
        finish()
    }
}