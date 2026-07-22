package com.android.runmate.ui.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.RunningCourse

class CourseAdapter(
    private val courses: List<RunningCourse>,
    private val onSelect: (RunningCourse) -> Unit
) : RecyclerView.Adapter<CourseAdapter.VH>() {

    private var selectedId: Int = courses.firstOrNull()?.id ?: -1

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val course = courses[position]
        holder.view.findViewById<TextView>(R.id.tvLevel).text = course.level
        holder.view.findViewById<TextView>(R.id.tvCourseTitle).text = course.title
        holder.view.findViewById<TextView>(R.id.tvDistance).text = "${course.distanceKm}km"
        holder.view.findViewById<TextView>(R.id.tvDescription).text = course.description

        holder.view.setOnClickListener {
            selectedId = course.id
            notifyDataSetChanged()
            onSelect(course)
        }
    }

    override fun getItemCount() = courses.size
}