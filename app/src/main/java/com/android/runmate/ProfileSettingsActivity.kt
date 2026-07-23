package com.android.runmate

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.data.DBHelper

class ProfileSettingsActivity : AppCompatActivity() {

    private var selectedLevel = "중급"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        val etNickname = findViewById<EditText>(R.id.etNickname)
        val tvProfileInitial = findViewById<TextView>(R.id.tvProfileInitial)

        val cardBeginner = findViewById<LinearLayout>(R.id.cardBeginner)
        val cardIntermediate = findViewById<LinearLayout>(R.id.cardIntermediate)
        val cardAdvanced = findViewById<LinearLayout>(R.id.cardAdvanced)

        loadProfile(etNickname, tvProfileInitial)
        updateLevelUI(cardBeginner, cardIntermediate, cardAdvanced)

        cardBeginner.setOnClickListener {
            selectedLevel = "초보"
            updateLevelUI(cardBeginner, cardIntermediate, cardAdvanced)
        }
        cardIntermediate.setOnClickListener {
            selectedLevel = "중급"
            updateLevelUI(cardBeginner, cardIntermediate, cardAdvanced)
        }
        cardAdvanced.setOnClickListener {
            selectedLevel = "고수"
            updateLevelUI(cardBeginner, cardIntermediate, cardAdvanced)
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveProfile(etNickname.text.toString())
        }

        findViewById<LinearLayout>(R.id.rowCustomerService).setOnClickListener {
            startActivity(Intent(this, CustomerServiceActivity::class.java))
        }
    }

    private fun loadProfile(etNickname: EditText, tvProfileInitial: TextView) {
        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT nickname, level FROM users WHERE id = ?",
            arrayOf(DBHelper.CURRENT_USER_ID.toString())
        )
        cursor.use {
            if (it.moveToFirst()) {
                val nickname = it.getString(it.getColumnIndexOrThrow("nickname")) ?: ""
                val level = it.getString(it.getColumnIndexOrThrow("level")) ?: "중급"
                etNickname.setText(nickname)
                tvProfileInitial.text = if (nickname.isNotEmpty()) nickname.take(1) else "새"
                selectedLevel = level
            }
        }
        db.close()
    }

    private fun updateLevelUI(
        cardBeginner: LinearLayout,
        cardIntermediate: LinearLayout,
        cardAdvanced: LinearLayout
    ) {
        val cards = mapOf(
            "초보" to cardBeginner,
            "중급" to cardIntermediate,
            "고수" to cardAdvanced
        )
        val texts = mapOf(
            "초보" to findViewById<TextView>(R.id.tvBeginner),
            "중급" to findViewById<TextView>(R.id.tvIntermediate),
            "고수" to findViewById<TextView>(R.id.tvAdvanced)
        )

        for ((level, card) in cards) {
            if (level == selectedLevel) {
                card.setBackgroundResource(R.drawable.bg_level_card_selected)
                texts[level]?.setTextColor(getColor(R.color.run_blue))
            } else {
                card.setBackgroundResource(R.drawable.bg_level_card_unselected)
                texts[level]?.setTextColor(getColor(R.color.text_primary))
            }
        }
    }

    private fun saveProfile(nickname: String) {
        if (nickname.isBlank()) {
            Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val dbHelper = DBHelper(this)
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nickname", nickname)
            put("level", selectedLevel)
        }

        val rows = db.update("users", values, "id = ?", arrayOf(DBHelper.CURRENT_USER_ID.toString()))
        if (rows == 0) {
            values.put("id", DBHelper.CURRENT_USER_ID)
            db.insert("users", null, values)
        }
        db.close()

        Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }
}