package com.android.runmate.ui.place

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.HanRiverPark
import com.android.runmate.data.PlaceRepository
import android.widget.ImageView

class ParkSelectActivity : AppCompatActivity() {

    private var selectedPark: HanRiverPark = PlaceRepository.parks.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_park_select)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }


        val rv = findViewById<RecyclerView>(R.id.rvParks)
        rv.layoutManager = GridLayoutManager(this, 2)

        val adapter = ParkAdapter(PlaceRepository.parks) { park ->
            selectedPark = park
        }
        rv.adapter = adapter

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val intent = Intent(this, CourseListActivity::class.java)
            intent.putExtra("park_id", selectedPark.id)
            startActivity(intent)
        }
    }
}