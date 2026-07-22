package com.android.runmate.ui.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.HanRiverPark
import com.google.android.material.card.MaterialCardView

class ParkAdapter(
    private val parks: List<HanRiverPark>,
    private val onSelect: (HanRiverPark) -> Unit
) : RecyclerView.Adapter<ParkAdapter.VH>() {

    private var selectedId: Int = parks.first().id

    inner class VH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_park, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val park = parks[position]
        val isSelected = park.id == selectedId

        holder.view.findViewById<TextView>(R.id.tvParkName).text = park.name
        holder.view.findViewById<TextView>(R.id.tvSubInfo).text = park.subInfo
        holder.view.findViewById<ImageView>(R.id.ivCheck).visibility =
            if (isSelected) View.VISIBLE else View.GONE

        val card = holder.view.findViewById<MaterialCardView>(R.id.cardPark)
        if (isSelected) {
            card.strokeColor = android.graphics.Color.parseColor("#2196F3")
            card.strokeWidth = 4
            card.setCardBackgroundColor(android.graphics.Color.parseColor("#F0F7FF"))
        } else {
            card.strokeColor = android.graphics.Color.parseColor("#E0E0E0")
            card.strokeWidth = 3
            card.setCardBackgroundColor(android.graphics.Color.WHITE)
        }

        holder.view.setOnClickListener {
            selectedId = park.id
            notifyDataSetChanged()
            onSelect(park)
        }
    }

    override fun getItemCount() = parks.size
}