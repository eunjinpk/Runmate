package com.android.runmate.ui.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.runmate.R
import com.android.runmate.data.HanRiverPark

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
        holder.view.findViewById<TextView>(R.id.tvParkName).text = park.name
        holder.view.findViewById<TextView>(R.id.tvSubInfo).text = park.subInfo
        holder.view.findViewById<ImageView>(R.id.ivCheck).visibility =
            if (park.id == selectedId) View.VISIBLE else View.GONE

        holder.view.setOnClickListener {
            selectedId = park.id
            notifyDataSetChanged()
            onSelect(park)
        }
    }

    override fun getItemCount() = parks.size
}