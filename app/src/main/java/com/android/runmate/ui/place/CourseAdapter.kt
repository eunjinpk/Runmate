package com.android.runmate.ui.place

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.RunningCourse
import com.google.android.material.card.MaterialCardView

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
        val isSelected = course.id == selectedId

        val tvLevel = holder.view.findViewById<TextView>(R.id.tvLevel)
        tvLevel.text = course.level

        when (course.level) {
            "초보" -> {
                tvLevel.setBackgroundResource(R.drawable.bg_badge_beginner)
                tvLevel.setTextColor(Color.parseColor("#2E7D32"))
            }
            "중급" -> {
                tvLevel.setBackgroundResource(R.drawable.bg_badge_intermediate)
                tvLevel.setTextColor(Color.parseColor("#EF6C00"))
            }
            "고수" -> {
                tvLevel.setBackgroundResource(R.drawable.bg_badge_advanced)
                tvLevel.setTextColor(Color.parseColor("#C62828"))
            }
        }

        holder.view.findViewById<TextView>(R.id.tvCourseTitle).text = course.title

        val distanceText = "${course.distanceKm}km"
        val numberLength = "${course.distanceKm}".length
        val spannable = SpannableString(distanceText)
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#2196F3")),
            0, numberLength,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(Color.parseColor("#9E9E9E")),
            numberLength, distanceText.length,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        holder.view.findViewById<TextView>(R.id.tvDistance).text = spannable

        holder.view.findViewById<TextView>(R.id.tvDescription).text = course.description

        val card = holder.view.findViewById<MaterialCardView>(R.id.cardCourse)
        if (isSelected) {
            card.strokeColor = Color.parseColor("#2196F3")
            card.strokeWidth = 4
            card.setCardBackgroundColor(Color.parseColor("#F0F7FF"))
        } else {
            card.strokeColor = Color.parseColor("#EEEEEE")
            card.strokeWidth = 3
            card.setCardBackgroundColor(Color.WHITE)
        }

        holder.view.setOnClickListener {
            selectedId = course.id
            notifyDataSetChanged()
            onSelect(course)
        }
    }

    override fun getItemCount() = courses.size
}