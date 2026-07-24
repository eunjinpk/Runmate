package com.android.runmate.ui.ranking

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.runmate.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

/** 랭킹 한 줄 데이터 */
data class Runner(val rank: Int, val name: String, val count: Int, val rate: Int)

class RankingActivity : AppCompatActivity() {

    // 명예 랭킹 더미 데이터 (나중에 서버/DB 데이터로 교체)
    private val honorList = listOf(
        Runner(1, "준호", 46, 100),
        Runner(2, "민지", 44, 96),
        Runner(3, "서연", 38, 88),
        Runner(4, "현우", 32, 84),
        Runner(5, "지호", 29, 81),
        Runner(6, "수민", 27, 78),
        Runner(7, "태윤", 25, 74)
    )

    // 내 랭킹
    private val myRank = Runner(1, "준호", 46, 100)

    private lateinit var layoutRankList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        layoutRankList = findViewById(R.id.layoutRankList)

        applyBottomNavInsets()
        bindPodium()
        bindRankList()
        bindMyRank()
        setupBottomNav()
    }

    /** 홈 화면과 동일하게 시스템 내비게이션 바 높이만큼 여백 주기 */
    private fun applyBottomNavInsets() {
        val navBar = findViewById<LinearLayout>(R.id.bottomNavBar) ?: return
        val basePadding = navBar.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(navBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                basePadding + systemBars.bottom
            )
            insets
        }
    }

    /** 하단 네비게이션 */
    private fun setupBottomNav() {
        // 홈 화면으로 이동
        findViewById<LinearLayout>(R.id.navHome)?.setOnClickListener {
            val intent = android.content.Intent(
                this,
                com.android.runmate.ui.home.HomeActivity::class.java
            )
            // 이미 떠 있는 홈 화면을 재사용 (화면이 계속 쌓이는 것 방지)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 장소 화면으로 이동
        findViewById<LinearLayout>(R.id.navPlace)?.setOnClickListener {
            startActivity(
                android.content.Intent(
                    this,
                    com.android.runmate.ui.place.ParkSelectActivity::class.java
                )
            )
        }

        // 이미 랭킹 화면이라 아무 동작 없음
        findViewById<LinearLayout>(R.id.navRanking)?.setOnClickListener { }

        // 마이페이지로 이동
        findViewById<LinearLayout>(R.id.navMy)?.setOnClickListener {
            startActivity(
                android.content.Intent(this, com.android.runmate.MyPageActivity::class.java)
            )
        }
    }

    /** 상위 3명 시상대 */
    private fun bindPodium() {
        setPodium(honorList.getOrNull(0), R.id.tvFirstName, R.id.tvFirstRank, R.id.tvFirstRate)
        setPodium(honorList.getOrNull(1), R.id.tvSecondName, R.id.tvSecondRank, R.id.tvSecondRate)
        setPodium(honorList.getOrNull(2), R.id.tvThirdName, R.id.tvThirdRank, R.id.tvThirdRate)
    }

    private fun setPodium(runner: Runner?, nameId: Int, rankId: Int, rateId: Int) {
        val tvName = findViewById<TextView>(nameId)
        val tvRank = findViewById<TextView>(rankId)
        val tvRate = findViewById<TextView>(rateId)

        if (runner == null) {
            tvName.text = ""
            tvRank.text = ""
            tvRate.text = ""
            return
        }
        tvName.text = runner.name
        tvRank.text = runner.rank.toString()
        tvRate.text = "${runner.rate}%"
    }

    /** 4위 이하 목록 (구분선 포함) */
    private fun bindRankList() {
        layoutRankList.removeAllViews()

        val rest = honorList.drop(3)
        rest.forEachIndexed { index, runner ->
            layoutRankList.addView(createRankRow(runner))
            // 마지막 줄 아래에는 선을 넣지 않음
            if (index < rest.lastIndex) {
                layoutRankList.addView(createDivider())
            }
        }
    }

    /** 목록 사이 구분선 */
    private fun createDivider(): View {
        return View(this).apply {
            setBackgroundColor(Color.parseColor("#EDF0F5"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1)
            ).apply {
                marginStart = dp(16)
                marginEnd = dp(16)
            }
        }
    }

    /** 4위 이하 한 줄 */
    private fun createRankRow(runner: Runner): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(13), dp(16), dp(13))
        }

        // 순위
        row.addView(TextView(this).apply {
            text = runner.rank.toString()
            setTextColor(Color.parseColor("#8A94A6"))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT)
        })

        // 원형 프로필 사진
        row.addView(createAvatar(dp(38)))

        // 이름 + 참여 횟수
        val nameBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        nameBox.addView(TextView(this).apply {
            text = runner.name
            setTextColor(Color.BLACK)
            textSize = 15f
        })
        nameBox.addView(TextView(this).apply {
            text = "참여 ${runner.count}회"
            setTextColor(Color.parseColor("#8A94A6"))
            textSize = 12f
        })
        row.addView(nameBox)

        // 참여율
        row.addView(TextView(this).apply {
            text = "${runner.rate}%"
            setTextColor(Color.parseColor("#2F80FF"))
            textSize = 15f
        })

        return row
    }

    /** 코드로 원형 프로필 이미지 만들기 */
    private fun createAvatar(sizePx: Int): ShapeableImageView {
        return ShapeableImageView(this).apply {
            setImageResource(R.drawable.ic_avatar)
            scaleType = ImageView.ScaleType.CENTER_CROP
            shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, sizePx / 2f)   // 완전한 원
                .build()
            layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                marginEnd = dp(12)
            }
        }
    }

    private fun bindMyRank() {
        findViewById<TextView>(R.id.tvMyRank).text = myRank.rank.toString()
        findViewById<TextView>(R.id.tvMyCount).text = "이번 달 참여 ${myRank.count}회"
        findViewById<TextView>(R.id.tvMyRate).text = "${myRank.rate}%"
    }

    /** dp → px 변환 */
    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}