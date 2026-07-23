package com.android.runmate.ui.place

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.PlaceRepository
import com.android.runmate.data.RunningCourse

class CourseListActivity : AppCompatActivity() {

    private var selectedCourse: RunningCourse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_list)

        val parkId = intent.getIntExtra("park_id", 1)
        val park = PlaceRepository.parks.find { it.id == parkId } ?: PlaceRepository.parks.first()
        val courses = PlaceRepository.getCoursesByPark(parkId)

        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.ivParkMap).setImageResource(park.mapImageRes)
        findViewById<TextView>(R.id.tvParkTitle).text = park.name + " 한강공원"
        findViewById<TextView>(R.id.tvCourseCount).text = "레벨에 맞는 추천 코스 ${courses.size}"

        selectedCourse = courses.firstOrNull()

        val rv = findViewById<RecyclerView>(R.id.rvCourses)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = CourseAdapter(courses) { course ->
            selectedCourse = course
        }

        findViewById<Button>(R.id.btnCreateMeeting).setOnClickListener {
            val intent = Intent(this, com.android.runmate.ui.meeting.CreateMeetingActivity::class.java)
            intent.putExtra("location_name", park.name + " 한강공원")
            intent.putExtra("lat", park.lat)
            intent.putExtra("lng", park.lng)
            selectedCourse?.let {
                intent.putExtra("course_name", it.title)
                intent.putExtra("course_level", it.level)
                intent.putExtra("course_distance", it.distanceKm)
            }
            startActivity(intent)
        }
    }
}