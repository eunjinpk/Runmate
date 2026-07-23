package com.android.runmate.ui.privatejoin

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.R

class PrivateJoinActivity : AppCompatActivity() {

    private lateinit var etCode: EditText
    private lateinit var tvError: TextView

    // 실제로는 모임 데이터에서 받아온 코드와 비교
    private var correctCode = "yeouido27"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_join)

        intent.getStringExtra("invite_code")?.let { correctCode = it }

        etCode = findViewById(R.id.etCode)
        tvError = findViewById(R.id.tvError)

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnCancel).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnJoin).setOnClickListener { tryJoin() }
    }

    private fun tryJoin() {
        val input = etCode.text.toString().trim()

        if (input.isEmpty()) {
            showError("초대코드를 입력해주세요")
            return
        }

        if (input.equals(correctCode, ignoreCase = true)) {
            Toast.makeText(this, "입장했습니다!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            showError("초대코드가 올바르지 않습니다")
            etCode.text.clear()
        }
    }

    private fun showError(msg: String) {
        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }
}