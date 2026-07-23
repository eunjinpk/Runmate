package com.android.runmate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import com.android.runmate.ui.home.HomeActivity
import com.android.runmate.util.SessionManager

class SignupActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        dbHelper = DBHelper(this)

        findViewById<TextView>(R.id.btnBack).setOnClickListener { finish() }

        val etNickname = findViewById<EditText>(R.id.etNickname)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)

        findViewById<TextView>(R.id.btnSignup).setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val passwordConfirm = etPasswordConfirm.text.toString().trim()

            if (nickname.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "빈칸을 모두 채워주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != passwordConfirm) {
                Toast.makeText(this, "비밀번호가 서로 달라요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dbHelper.isUsernameTaken(username)) {
                Toast.makeText(this, "이미 쓰이고 있는 아이디예요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUserId = dbHelper.registerUser(nickname, username, password).toInt()
            if (newUserId == -1) {
                Toast.makeText(this, "가입에 실패했어요, 다시 시도해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DBHelper.CURRENT_USER_ID = newUserId
            SessionManager.saveUserId(this, newUserId)
            Toast.makeText(this, "가입 완료! 바로 시작해볼까요?", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}