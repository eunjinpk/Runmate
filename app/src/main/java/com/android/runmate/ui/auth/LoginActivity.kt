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

/**
 * 로그인 화면 (#2).
 * 교수님 피드백에 따라 카카오/네이버/구글/애플 같은 소셜 로그인은 빼고,
 * 아이디/비밀번호로만 로그인합니다.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        dbHelper = DBHelper(this)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        findViewById<TextView>(R.id.btnLogin).setOnClickListener { login() }

        findViewById<TextView>(R.id.btnGoSignup).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // 로그인 없이 데모 유저(id=1)로 둘러보기
        findViewById<TextView>(R.id.btnSkip).setOnClickListener {
            DBHelper.CURRENT_USER_ID = 1
            SessionManager.saveUserId(this, 1)
            goToHome()
        }
    }

    private fun login() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = dbHelper.login(username, password)
        if (userId == null) {
            Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않아요", Toast.LENGTH_SHORT).show()
            return
        }

        DBHelper.CURRENT_USER_ID = userId
        SessionManager.saveUserId(this, userId)
        goToHome()
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}