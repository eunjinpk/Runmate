package com.android.runmate.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class HomeActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: MeetingAdapter

    private lateinit var rvMeetings: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvRecruitingCount: TextView
    private lateinit var tvPublicOnlyToggle: TextView
    private lateinit var layoutLocationChips: LinearLayout
    private lateinit var etSearch: EditText

    // 지하철역 근처 한강공원 칩 목록 (지도 기반 장소 검색 화면이 붙기 전까지는 하드코딩)
    private val nearbyLocations = linkedMapOf(
        "여의도" to "5호선",
        "반포" to "9호선",
        "뚝섬" to "7호선",
        "잠실" to "2호선"
    )

    private var selectedLocation: String? = "여의도" // 시안 기본값과 동일하게 여의도로 시작
    private var isPublicOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dbHelper = DBHelper(this)

        rvMeetings = findViewById(R.id.rvMeetings)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvRecruitingCount = findViewById(R.id.tvRecruitingCount)
        tvPublicOnlyToggle = findViewById(R.id.tvPublicOnlyToggle)
        layoutLocationChips = findViewById(R.id.layoutLocationChips)
        etSearch = findViewById(R.id.etSearch)

        val tvSelectedLocation = findViewById<TextView>(R.id.tvSelectedLocation)
        val layoutLocationSelector = findViewById<LinearLayout>(R.id.layoutLocationSelector)
        val tvNotificationBell = findViewById<TextView>(R.id.tvNotificationBell)
        val fabCreateMeeting = findViewById<ExtendedFloatingActionButton>(R.id.fabCreateMeeting)

        adapter = MeetingAdapter { meeting ->
            // TODO: 모임 상세 화면으로 이동하며 meeting.id 전달
            Toast.makeText(this, "${meeting.title} 상세 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }
        rvMeetings.layoutManager = LinearLayoutManager(this)
        rvMeetings.adapter = adapter

        buildLocationChips()

        // 검색창: 입력할 때마다 실시간으로 목록 필터링
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                refreshList(searchQuery = s?.toString())
            }
        })

        // 공개만 토글
        tvPublicOnlyToggle.setOnClickListener {
            isPublicOnly = !isPublicOnly
            tvPublicOnlyToggle.setBackgroundResource(
                if (isPublicOnly) R.drawable.bg_toggle_active else R.drawable.bg_sort_button
            )
            tvPublicOnlyToggle.setTextColor(
                getColor(if (isPublicOnly) R.color.surface_white else R.color.text_secondary)
            )
            refreshList(searchQuery = etSearch.text?.toString())
        }

        // 내 러닝 장소 선택 영역 (지금은 장소 화면이 없어서 자리표시자)
        layoutLocationSelector.setOnClickListener {
            Toast.makeText(this, "장소 선택 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }

        tvNotificationBell.setOnClickListener {
            Toast.makeText(this, "알림 (연결 예정)", Toast.LENGTH_SHORT).show()
        }

        fabCreateMeeting.setOnClickListener {
            // TODO: 모임 만들기 화면(#2)으로 이동
            Toast.makeText(this, "모임 만들기 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }

        setupBottomNav()

        tvSelectedLocation.text = "📍 $selectedLocation 한강공원 ▾"
        refreshList()
    }

    override fun onResume() {
        super.onResume()
        // 모임 만들기/참여 화면에서 돌아왔을 때 리스트가 최신 상태로 보이도록 새로고침
        refreshList(searchQuery = etSearch.text?.toString())
    }

    /** 지하철역 근처 한강공원 칩을 코드로 생성 (여의도/반포/뚝섬/잠실) */
    private fun buildLocationChips() {
        layoutLocationChips.removeAllViews()
        nearbyLocations.forEach { (location, subwayLine) ->
            val chip = TextView(this).apply {
                text = "$location $subwayLine"
                textSize = 13f
                setPadding(28, 16, 28, 16)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = 8
                layoutParams = params
                applyChipStyle(this, location == selectedLocation)
                setOnClickListener {
                    selectedLocation = location
                    findViewById<TextView>(R.id.tvSelectedLocation).text = "📍 $location 한강공원 ▾"
                    buildLocationChips()
                    refreshList(searchQuery = etSearch.text?.toString())
                }
            }
            layoutLocationChips.addView(chip)
        }
    }

    private fun applyChipStyle(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        chip.setTextColor(getColor(if (selected) R.color.surface_white else R.color.text_primary))
    }

    private fun setupBottomNav() {
        // 지금은 홈 화면만 있어서 다른 탭은 자리표시자입니다.
        findViewById<LinearLayout>(R.id.navPlace).setOnClickListener {
            Toast.makeText(this, "장소 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navChallenge).setOnClickListener {
            Toast.makeText(this, "챌린지 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navRanking).setOnClickListener {
            Toast.makeText(this, "랭킹 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.navMy).setOnClickListener {
            Toast.makeText(this, "마이페이지 화면 (연결 예정)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshList(searchQuery: String? = null) {
        val meetings = dbHelper.getRecruitingMeetings(
            locationKeyword = selectedLocation,
            publicOnly = isPublicOnly,
            searchQuery = searchQuery
        )
        adapter.submitList(meetings)
        tvRecruitingCount.text = "모집 중 ${meetings.size}"

        if (meetings.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvMeetings.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvMeetings.visibility = View.VISIBLE
        }
    }
}