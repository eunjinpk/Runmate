package com.android.runmate.ui.place

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.HanRiverPark
import com.android.runmate.data.PlaceRepository
import com.google.android.material.card.MaterialCardView

class ParkAdapter(
    private val parks: List<HanRiverPark>,
    private val onSelect: (HanRiverPark) -> Unit,
    private val onFavoriteChanged: () -> Unit = {}
) : RecyclerView.Adapter<ParkAdapter.VH>() {

    private var selectedId: Int = parks.firstOrNull()?.id ?: -1

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val park = parks[position]
        val isSelected = park.id == selectedId

        val tvName = holder.view.findViewById<TextView>(R.id.tvParkName)
        val tvSub = holder.view.findViewById<TextView>(R.id.tvSubInfo)
        val ivPin = holder.view.findViewById<ImageView>(R.id.ivPin)
        val ivCheck = holder.view.findViewById<ImageView>(R.id.ivCheck)
        val ivFavorite = holder.view.findViewById<ImageView>(R.id.ivFavorite)
        val card = holder.view.findViewById<MaterialCardView>(R.id.cardPark)

        tvName.text = park.name
        tvSub.text = park.subInfo
        ivCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
        ivFavorite.setImageResource(
            if (park.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_empty
        )

        val blue = Color.parseColor("#2196F3")
        val darkText = Color.parseColor("#212121")
        val grayText = Color.parseColor("#9E9E9E")

        if (isSelected) {
            tvName.setTextColor(blue)
            tvSub.setTextColor(blue)
            card.strokeColor = blue
            card.strokeWidth = 4
            card.setCardBackgroundColor(Color.parseColor("#F0F7FF"))
        } else {
            tvName.setTextColor(darkText)
            tvSub.setTextColor(grayText)
            card.strokeColor = Color.parseColor("#E0E0E0")
            card.strokeWidth = 3
            card.setCardBackgroundColor(Color.WHITE)
        }
        ivPin.setColorFilter(blue)

        ivFavorite.setOnClickListener {
            PlaceRepository.toggleFavorite(it.context, park.id)
            notifyItemChanged(position)
            onFavoriteChanged()
        }

        holder.view.setOnClickListener {
            selectedId = park.id
            notifyDataSetChanged()
            onSelect(park)
        }
    }

    override fun getItemCount() = parks.size
}