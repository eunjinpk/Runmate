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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * 홈 화면 (#1) - 모집 중인 러닝 모임 리스트
 *
 * 시안 기준으로 아래 항목까지 포함:
 *  - 내 러닝 장소 선택 영역(현재는 표시만, TODO: 장소 화면과 연결)
 *  - 검색창 (모임명/장소명 검색 → DB LIKE 쿼리로 실제 필터링됨)
 *  - 지하철역 근처 한강공원 칩 (여의도/반포/뚝섬/잠실) → 클릭 시 해당 장소로 필터링
 *  - "공개만" 토글 → 클릭 시 공개 모임만 필터링
 *  - 하단 탭바 (홈/장소/챌린지/랭킹/마이) → 홈 이외 탭은 아직 자리표시자
 *
 * 정렬은 기본적으로 날짜/시간이 가까운 순(마감임박순)입니다.
 *
 * TODO(다음 단계):
 *  - fabCreateMeeting 클릭 시 "모임 만들기" 화면(#2)으로 이동
 *  - 리스트 아이템 클릭 시 "모임 상세" 화면(#3)으로 이동 (meeting.id 전달)
 *  - 하단 탭바의 장소/챌린지/랭킹/마이 탭에 실제 화면 연결
 */
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
    private var isPublicOnly = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 엣지-투-엣지 화면에서 3버튼 내비게이션 등 시스템 내비게이션 바가
        // 우리 앱의 하단 탭바랑 겹치지 않도록, 시스템 바 높이만큼 아래쪽에 여백을 줍니다.
        val bottomNavBar = findViewById<LinearLayout>(R.id.bottomNavBar)
        val bottomNavBarBasePadding = bottomNavBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomNavBarBasePadding + systemBars.bottom)
            insets
        }

        dbHelper = DBHelper(this)

        rvMeetings = findViewById(R.id.rvMeetings)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvRecruitingCount = findViewById(R.id.tvRecruitingCount)
        tvPublicOnlyToggle = findViewById(R.id.tvPublicOnlyToggle)
        layoutLocationChips = findViewById(R.id.layoutLocationChips)
        etSearch = findViewById(R.id.etSearch)

        val tvNotificationBell = findViewById<TextView>(R.id.tvNotificationBell)
        val fabCreateMeeting = findViewById<ExtendedFloatingActionButton>(R.id.fabCreateMeeting)

        adapter = MeetingAdapter { meeting ->
            val intent = android.content.Intent(this, com.android.runmate.ui.meeting.MeetingDetailActivity::class.java)
            intent.putExtra(com.android.runmate.ui.meeting.MeetingDetailActivity.EXTRA_MEETING_ID, meeting.id)
            startActivity(intent)
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
            applyPublicOnlyStyle()
            refreshList(searchQuery = etSearch.text?.toString())
        }

        tvNotificationBell.setOnClickListener {
            Toast.makeText(this, "알림 (연결 예정)", Toast.LENGTH_SHORT).show()
        }

        fabCreateMeeting.setOnClickListener {
            startActivity(android.content.Intent(this, com.android.runmate.ui.meeting.CreateMeetingActivity::class.java))
        }

        setupBottomNav()

        // 시안 기본값: "공개만" 필터가 처음부터 켜져 있는 상태로 시작
        applyPublicOnlyStyle()
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
                    buildLocationChips()
                    refreshList(searchQuery = etSearch.text?.toString())
                }
            }
            layoutLocationChips.addView(chip)
        }
    }

    private fun applyPublicOnlyStyle() {
        tvPublicOnlyToggle.setBackgroundResource(
            if (isPublicOnly) R.drawable.bg_toggle_active else R.drawable.bg_sort_button
        )
        tvPublicOnlyToggle.setTextColor(
            getColor(if (isPublicOnly) R.color.surface_white else R.color.text_primary)
        )
    }

    private fun applyChipStyle(chip: TextView, selected: Boolean) {
        chip.setBackgroundResource(
            if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip_unselected
        )
        chip.setTextColor(getColor(if (selected) R.color.surface_white else R.color.text_primary))
    }

    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navPlace).setOnClickListener {
            // 지도/장소 담당(②번)의 ParkSelectActivity로 이동 (main에 merge됨)
            startActivity(
                android.content.Intent(this, com.android.runmate.ui.place.ParkSelectActivity::class.java)
            )
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