package com.android.runmate.util

import android.content.Context

/**
 * 로그인한 유저 id를 SharedPreferences에 저장해서,
 * 앱을 껐다 켜도 로그인 상태가 유지되게 해줍니다.
 */
object SessionManager {
    private const val PREF_NAME = "runmate_session"
    private const val KEY_USER_ID = "logged_in_user_id"

    fun saveUserId(context: Context, userId: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    /** 로그인 안 되어 있으면 null */
    fun getUserId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id == -1) null else id
    }

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != null

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}