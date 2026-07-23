package com.android.runmate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PhotoGalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        val photos = listOf(
            PhotoItem(R.drawable.running_photo1, "5.2km"),
            PhotoItem(R.drawable.running_photo2, "Day3"),
            PhotoItem(R.drawable.running_photo3, "8.0km")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerAllPhotos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = PhotoGridAdapter(photos)

        findViewById<android.widget.ImageView>(R.id.btnBackGallery).setOnClickListener {
            finish()
        }
    }
}