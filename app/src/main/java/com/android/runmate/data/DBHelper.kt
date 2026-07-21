package com.android.runmate.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "runmate.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        // users (유저)
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nickname TEXT NOT NULL,
                level TEXT NOT NULL DEFAULT '초보',
                profile_img TEXT
            )
            """.trimIndent()
        )

        // meetings (모임)
        db.execSQL(
            """
            CREATE TABLE meetings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                host_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                date TEXT NOT NULL,
                time TEXT NOT NULL,
                location_name TEXT NOT NULL,
                lat REAL,
                lng REAL,
                max_people INTEGER NOT NULL,
                is_public INTEGER NOT NULL DEFAULT 1,
                invite_code TEXT,
                description TEXT,
                FOREIGN KEY(host_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // meeting_participants (모임 참여)
        db.execSQL(
            """
            CREATE TABLE meeting_participants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meeting_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'joined',
                FOREIGN KEY(meeting_id) REFERENCES meetings(id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // running_records (러닝 인증)
        db.execSQL(
            """
            CREATE TABLE running_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                meeting_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                photo_path TEXT,
                distance REAL,
                time REAL,
                date TEXT NOT NULL,
                FOREIGN KEY(meeting_id) REFERENCES meetings(id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // diet_challenges (식단 챌린지)
        db.execSQL(
            """
            CREATE TABLE diet_challenges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                creator_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT NOT NULL,
                description TEXT,
                FOREIGN KEY(creator_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // diet_records (식단 인증)
        db.execSQL(
            """
            CREATE TABLE diet_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                challenge_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                photo_path TEXT,
                memo TEXT,
                date TEXT NOT NULL,
                FOREIGN KEY(challenge_id) REFERENCES diet_challenges(id),
                FOREIGN KEY(user_id) REFERENCES users(id)
            )
            """.trimIndent()
        )

        // 홈 화면이 빈 화면으로 보이지 않도록 넣어두는 테스트용 더미데이터
        // (실제 회원가입/모임생성 기능이 붙으면 지워도 됩니다)
        insertSampleData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS diet_records")
        db.execSQL("DROP TABLE IF EXISTS diet_challenges")
        db.execSQL("DROP TABLE IF EXISTS running_records")
        db.execSQL("DROP TABLE IF EXISTS meeting_participants")
        db.execSQL("DROP TABLE IF EXISTS meetings")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    private fun insertSampleData(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO users (id, nickname, level) VALUES (1, '김러너', '초보')")

        db.execSQL(
            """
            INSERT INTO meetings
                (host_id, title, date, time, location_name, lat, lng, max_people, is_public, description)
            VALUES
                (1, '여의도 저녁 러닝', '2026-07-24', '19:30', '여의도한강공원', 37.5285, 126.9330, 6, 1, '가볍게 5km 뛰어요')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO meetings
                (host_id, title, date, time, location_name, lat, lng, max_people, is_public, invite_code, description)
            VALUES
                (1, '반포 새벽 러닝 (초보 환영)', '2026-07-23', '06:00', '반포한강공원', 37.5100, 126.9970, 4, 0, 'RUN2026', '초보만 모여요')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO meetings
                (host_id, title, date, time, location_name, lat, lng, max_people, is_public, description)
            VALUES
                (1, '뚝섬 주말 러닝크루', '2026-07-26', '10:00', '뚝섬한강공원', 37.5310, 127.0670, 10, 1, '주말마다 같이 뛰어요')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO meetings
                (host_id, title, date, time, location_name, lat, lng, max_people, is_public, description)
            VALUES
                (1, '잠실 야경 러닝 8K', '2026-07-25', '20:00', '잠실한강공원', 37.5175, 127.0837, 8, 1, '롯데타워 야경 보며 뛰어요')
            """.trimIndent()
        )
    }

    /**
     * 홈 화면용: 모집 중인 모임 목록을 반환합니다.
     * 기본 정렬은 "날짜/시간이 가까운 순(마감임박순)"이고, publicOnly가 true면 공개 모임만 보여줍니다.
     * locationKeyword는 지하철역 근처 한강공원 칩(여의도/반포/뚝섬/잠실 등) 필터,
     * searchQuery는 상단 검색창(모임명/장소명 검색)에 대응합니다.
     * 참여인원(joinedCount)은 meeting_participants 테이블에서 집계합니다.
     */
    fun getRecruitingMeetings(
        locationKeyword: String? = null,
        publicOnly: Boolean = false,
        searchQuery: String? = null
    ): List<Meeting> {
        val list = mutableListOf<Meeting>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!locationKeyword.isNullOrBlank()) {
            whereClauses.add("m.location_name LIKE ?")
            args.add("%$locationKeyword%")
        }
        if (publicOnly) {
            whereClauses.add("m.is_public = 1")
        }
        if (!searchQuery.isNullOrBlank()) {
            whereClauses.add("(m.title LIKE ? OR m.location_name LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        val whereSql = if (whereClauses.isEmpty()) "" else "WHERE " + whereClauses.joinToString(" AND ")

        val query = """
            SELECT m.id, m.title, m.date, m.time, m.location_name, m.description, m.max_people, m.is_public,
                   u.nickname AS host_nickname,
                   (SELECT COUNT(*) FROM meeting_participants p WHERE p.meeting_id = m.id) AS joined_count
            FROM meetings m
            LEFT JOIN users u ON u.id = m.host_id
            $whereSql
            ORDER BY m.date ASC, m.time ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, args.toTypedArray())
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Meeting(
                        id = getInt(getColumnIndexOrThrow("id")),
                        title = getString(getColumnIndexOrThrow("title")),
                        date = getString(getColumnIndexOrThrow("date")),
                        time = getString(getColumnIndexOrThrow("time")),
                        locationName = getString(getColumnIndexOrThrow("location_name")),
                        description = getString(getColumnIndexOrThrow("description")),
                        maxPeople = getInt(getColumnIndexOrThrow("max_people")),
                        isPublic = getInt(getColumnIndexOrThrow("is_public")) == 1,
                        joinedCount = getInt(getColumnIndexOrThrow("joined_count")),
                        hostNickname = getString(getColumnIndexOrThrow("host_nickname")) ?: "러너"
                    )
                )
            }
            close()
        }
        return list
    }
}