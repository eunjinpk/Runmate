package com.android.runmate.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.Meeting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MeetingAdapter(
    private val onItemClick: (Meeting) -> Unit
) : RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder>() {

    private val items = mutableListOf<Meeting>()

    fun submitList(newItems: List<Meeting>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meeting, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        holder.bind(items[position], onItemClick)
    }

    override fun getItemCount(): Int = items.size

    class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        private val tvUrgentBadge: TextView = itemView.findViewById(R.id.tvUrgentBadge)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvHostAvatar: TextView = itemView.findViewById(R.id.tvHostAvatar)
        private val tvPeopleCount: TextView = itemView.findViewById(R.id.tvPeopleCount)
        private val tvFineBadge: TextView = itemView.findViewById(R.id.tvFineBadge)

        // meetings 테이블의 date("yyyy-MM-dd") + time("HH:mm")를 합쳐 파싱
        private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

        fun bind(meeting: Meeting, onItemClick: (Meeting) -> Unit) {
            tvTitle.text = meeting.title
            tvDateTime.text = "📅 ${formatRelativeDateTime(meeting)}"
            tvLocation.text = "📍 ${meeting.locationName}"
            tvPeopleCount.text = "${meeting.joinedCount}/${meeting.maxPeople}명 참여중"
            tvHostAvatar.text = meeting.hostNickname.take(1)

            if (meeting.fineAmount > 0) {
                tvFineBadge.visibility = View.VISIBLE
                tvFineBadge.text = "벌금 ${"%,d".format(meeting.fineAmount)}원"
            } else {
                tvFineBadge.visibility = View.GONE
            }

            if (meeting.description.isNullOrBlank()) {
                tvDescription.visibility = View.GONE
            } else {
                tvDescription.visibility = View.VISIBLE
                tvDescription.text = meeting.description
            }

            if (meeting.isPublic) {
                tvBadge.text = "공개"
                tvBadge.backgroundTintList = itemView.context.getColorStateList(R.color.chip_public_bg)
                tvBadge.setTextColor(itemView.context.getColor(R.color.chip_public_text))
            } else {
                tvBadge.text = "비공개"
                tvBadge.backgroundTintList = itemView.context.getColorStateList(R.color.chip_private_bg)
                tvBadge.setTextColor(itemView.context.getColor(R.color.chip_private_text))
            }

            // DB에 별도 컬럼 없이, 모임 시작까지 24시간 이내면 "마감임박"으로 자동 표시
            tvUrgentBadge.visibility = if (isWithin24Hours(meeting)) View.VISIBLE else View.GONE

            itemView.setOnClickListener { onItemClick(meeting) }
        }

        private fun formatRelativeDateTime(meeting: Meeting): String {
            val dayText = try {
                val meetingDate = dateOnlyFormat.parse(meeting.date) ?: return "${meeting.date} ${meeting.time}"

                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val target = Calendar.getInstance().apply {
                    time = meetingDate
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
                meeting.date
            }

            val timeText = try {
                val (hour, minute) = meeting.time.split(":").map { it.toInt() }
                val period = if (hour < 12) "오전" else "오후"
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$period ${hour12}:${minute.toString().padStart(2, '0')}"
            } catch (e: Exception) {
                meeting.time
            }

            return "$dayText $timeText"
        }

        private fun isWithin24Hours(meeting: Meeting): Boolean {
            return try {
                val meetingTime = dateTimeFormat.parse("${meeting.date} ${meeting.time}") ?: return false
                val diffMillis = meetingTime.time - Date().time
                diffMillis in 0..(24 * 60 * 60 * 1000L)
            } catch (e: Exception) {
                false
            }
        }
    }
}