package com.android.runmate.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * 1차 회의에서 확정한 6개 테이블 구조를 그대로 반영한 DBHelper입니다.
 * 컬럼명은 회의록 기준으로 임의 변경 없이 작성했습니다.
 * 팀원들이 각자 화면을 만들 때 이 클래스를 공용으로 사용하면 됩니다.
 */
class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "runmate.db"
        private const val DATABASE_VERSION = 3 // v3: 벌금 제도 폐지로 meetings.fine_amount 컬럼 제거 (팀 확정)

        // 로그인/회원가입 화면이 아직 없어서, 지금은 항상 이 유저(id=1)로 동작합니다.
        // 로그인 기능이 붙으면 이 값을 실제 로그인한 유저 id로 바꿔주면 됩니다.
        const val CURRENT_USER_ID = 1
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
        // v2에서 pace(목표 페이스) 컬럼 추가, v3에서 벌금 제도 폐지로 fine_amount 제거 (팀 확정)
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
                pace TEXT,
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
                (host_id, title, date, time, location_name, lat, lng, max_people, is_public, description, pace)
            VALUES
                (1, '여의도 저녁 러닝', '2026-07-24', '19:30', '여의도한강공원', 37.5285, 126.9330, 6, 1, '가볍게 5km 뛰어요', '6~7')
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

        // 모임 상세 화면 참여자 리스트 확인용 더미 참여자 (미니 유저 2명 + 참여 기록)
        db.execSQL("INSERT INTO users (id, nickname, level) VALUES (2, '민지', '초보')")
        db.execSQL("INSERT INTO users (id, nickname, level) VALUES (3, '서연', '초보')")
        db.execSQL("INSERT INTO meeting_participants (meeting_id, user_id, status) VALUES (1, 2, 'joined')")
        db.execSQL("INSERT INTO meeting_participants (meeting_id, user_id, status) VALUES (1, 3, 'joined')")
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

    /**
     * 모임 만들기(#2) 화면에서 사용. meetings 테이블에 새 모임을 추가하고 새 id를 반환합니다.
     */
    fun insertMeeting(
        hostId: Int,
        title: String,
        date: String,
        time: String,
        locationName: String,
        lat: Double?,
        lng: Double?,
        maxPeople: Int,
        isPublic: Boolean,
        inviteCode: String?,
        description: String?,
        pace: String? = null
    ): Long {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("host_id", hostId)
            put("title", title)
            put("date", date)
            put("time", time)
            put("location_name", locationName)
            if (lat != null) put("lat", lat)
            if (lng != null) put("lng", lng)
            put("max_people", maxPeople)
            put("is_public", if (isPublic) 1 else 0)
            put("invite_code", inviteCode)
            put("description", description)
            put("pace", pace)
        }
        return db.insert("meetings", null, values)
    }

    /**
     * 모임 상세(#3) 화면용: 모임 정보 + 호스트 닉네임을 함께 반환합니다.
     */
    fun getMeetingDetail(meetingId: Int): MeetingDetail? {
        val db = readableDatabase
        val query = """
            SELECT m.id, m.host_id, m.title, m.date, m.time, m.location_name, m.description,
                   m.max_people, m.is_public, m.pace,
                   u.nickname AS host_nickname,
                   (SELECT COUNT(*) FROM meeting_participants p WHERE p.meeting_id = m.id) AS joined_count
            FROM meetings m
            LEFT JOIN users u ON u.id = m.host_id
            WHERE m.id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(meetingId.toString()))
        var result: MeetingDetail? = null
        with(cursor) {
            if (moveToFirst()) {
                result = MeetingDetail(
                    id = getInt(getColumnIndexOrThrow("id")),
                    hostId = getInt(getColumnIndexOrThrow("host_id")),
                    hostNickname = getString(getColumnIndexOrThrow("host_nickname")) ?: "러너",
                    title = getString(getColumnIndexOrThrow("title")),
                    date = getString(getColumnIndexOrThrow("date")),
                    time = getString(getColumnIndexOrThrow("time")),
                    locationName = getString(getColumnIndexOrThrow("location_name")),
                    description = getString(getColumnIndexOrThrow("description")),
                    maxPeople = getInt(getColumnIndexOrThrow("max_people")),
                    isPublic = getInt(getColumnIndexOrThrow("is_public")) == 1,
                    joinedCount = getInt(getColumnIndexOrThrow("joined_count")),
                    pace = getString(getColumnIndexOrThrow("pace"))
                )
            }
            close()
        }
        return result
    }

    /**
     * 모임 상세 화면의 참여자 목록. 참여율은 저장된 컬럼이 아니라
     * "이 유저가 참여했던 전체 모임 중 no_show가 아닌 비율"로 그때그때 계산합니다.
     */
    fun getParticipants(meetingId: Int): List<Participant> {
        val db = readableDatabase
        val query = """
            SELECT u.id AS user_id, u.nickname, u.level
            FROM meeting_participants p
            JOIN users u ON u.id = p.user_id
            WHERE p.meeting_id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(meetingId.toString()))
        val list = mutableListOf<Participant>()
        with(cursor) {
            while (moveToNext()) {
                val userId = getInt(getColumnIndexOrThrow("user_id"))
                list.add(
                    Participant(
                        userId = userId,
                        nickname = getString(getColumnIndexOrThrow("nickname")),
                        level = getString(getColumnIndexOrThrow("level")),
                        participationRate = getParticipationRate(userId)
                    )
                )
            }
            close()
        }
        return list
    }

    private fun getParticipationRate(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS total,
                   SUM(CASE WHEN status = 'no_show' THEN 0 ELSE 1 END) AS attended
            FROM meeting_participants
            WHERE user_id = ?
            """.trimIndent(),
            arrayOf(userId.toString())
        )
        var rate = 100
        with(cursor) {
            if (moveToFirst()) {
                val total = getInt(getColumnIndexOrThrow("total"))
                val attended = getInt(getColumnIndexOrThrow("attended"))
                if (total > 0) rate = (attended * 100) / total
            }
            close()
        }
        return rate
    }

    /** 이미 참여 중인지 확인 (참여하기 버튼 상태 결정용) */
    fun hasUserJoined(meetingId: Int, userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM meeting_participants WHERE meeting_id = ? AND user_id = ?",
            arrayOf(meetingId.toString(), userId.toString())
        )
        val joined = cursor.moveToFirst()
        cursor.close()
        return joined
    }

    /** 참여하기 버튼: meeting_participants에 참여 기록 추가 */
    fun joinMeeting(meetingId: Int, userId: Int) {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put("meeting_id", meetingId)
            put("user_id", userId)
            put("status", "joined")
        }
        db.insert("meeting_participants", null, values)
    }

    /** 취소하기 버튼: meeting_participants에서 참여 기록 삭제 */
    fun leaveMeeting(meetingId: Int, userId: Int) {
        val db = writableDatabase
        db.delete(
            "meeting_participants",
            "meeting_id = ? AND user_id = ?",
            arrayOf(meetingId.toString(), userId.toString())
        )
    }

    /** 모임 삭제(호스트 전용): 참여자 기록 먼저 지우고 모임 자체를 삭제 */
    fun deleteMeeting(meetingId: Int) {
        val db = writableDatabase
        db.delete("meeting_participants", "meeting_id = ?", arrayOf(meetingId.toString()))
        db.delete("meetings", "id = ?", arrayOf(meetingId.toString()))
    }
}