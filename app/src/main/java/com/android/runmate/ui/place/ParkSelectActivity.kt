package com.android.runmate.ui.place

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.HanRiverPark
import com.android.runmate.data.PlaceRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ParkSelectActivity : AppCompatActivity() {

    private var selectedPark: HanRiverPark = PlaceRepository.parks.first()
    private lateinit var adapter: ParkAdapter
    private lateinit var rv: RecyclerView
    private lateinit var btnConfirm: Button
    private var currentFilter = "전체"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_select)

        PlaceRepository.loadFavorites(this)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        btnConfirm = findViewById(R.id.btnConfirm)
        updateButtonText()

        rv = findViewById(R.id.rvParks)
        rv.layoutManager = GridLayoutManager(this, 2)

        setupAdapter(PlaceRepository.getFilteredParks(currentFilter))

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupFilter)
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(checkedIds.first())
            currentFilter = chip.text.toString().let {
                if (it.startsWith("전체")) "전체" else it
            }
            applyFilter()
        }

        btnConfirm.setOnClickListener {
            val intent = Intent(this, CourseListActivity::class.java)
            intent.putExtra("park_id", selectedPark.id)
            startActivity(intent)
        }
    }

    private fun setupAdapter(list: List<HanRiverPark>) {
        adapter = ParkAdapter(
            parks = list,
            onSelect = { park ->
                selectedPark = park
                updateButtonText()
            },
            onFavoriteChanged = {
                if (currentFilter == "즐겨찾기") applyFilter()
            }
        )
        rv.adapter = adapter
    }

    private fun applyFilter() {
        val filtered = PlaceRepository.getFilteredParks(currentFilter)
        setupAdapter(filtered)
        if (filtered.isNotEmpty()) {
            selectedPark = filtered.first()
            updateButtonText()
        }
    }

    private fun updateButtonText() {
        btnConfirm.text = "${selectedPark.name} 한강공원 코스 보기"
    }
}