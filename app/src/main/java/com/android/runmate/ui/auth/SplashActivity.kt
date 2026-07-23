package com.android.runmate.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import com.android.runmate.ui.home.HomeActivity
import com.android.runmate.util.SessionManager

/**
 * 스플래시 화면 (#1). 자동으로 넘어가지 않고,
 * 화면을 탭해야 다음 화면(로그인 또는 홈)으로 이동합니다.
 */
class SplashActivity : AppCompatActivity() {

    private var alreadyNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<View>(R.id.splashRoot).setOnClickListener {
            goNext()
        }
    }

    private fun goNext() {
        if (alreadyNavigated) return
        alreadyNavigated = true

        val savedUserId = SessionManager.getUserId(this)
        val nextActivity = if (savedUserId != null) {
            DBHelper.CURRENT_USER_ID = savedUserId
            HomeActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, nextActivity))
        finish()
    }
}