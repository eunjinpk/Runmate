package com.android.runmate.ui.proof

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.android.runmate.R
import com.android.runmate.data.DBHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunProofActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEETING_ID = "meeting_id"
        const val EXTRA_MEETING_NAME = "meeting_name"
    }

    private lateinit var dbHelper: DBHelper
    private var meetingId: Int = -1

    private lateinit var etDistance: EditText
    private lateinit var etTime: EditText
    private lateinit var etPace: EditText
    private lateinit var etMemo: EditText
    private lateinit var ivPhoto: ImageView
    private lateinit var layoutPhotoHint: LinearLayout
    private lateinit var tvRetake: TextView

    private var photoUri: Uri? = null      // 촬영한 사진 주소
    private var hasPhoto = false           // 촬영 성공 여부

    /**
     * 카메라 앱을 열어 사진 촬영.
     * success 가 true 면 photoUri 위치에 사진이 저장된 것.
     */
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                showPhoto()
            } else {
                // 사용자가 촬영을 취소한 경우
                photoUri = null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_proof)

        dbHelper = DBHelper(this)
        meetingId = intent.getIntExtra(EXTRA_MEETING_ID, -1)

        etDistance = findViewById(R.id.etDistance)
        etTime = findViewById(R.id.etTime)
        etPace = findViewById(R.id.etPace)
        etMemo = findViewById(R.id.etMemo)
        ivPhoto = findViewById(R.id.ivPhoto)
        layoutPhotoHint = findViewById(R.id.layoutPhotoHint)
        tvRetake = findViewById(R.id.tvRetake)

        // 이전 화면에서 모임 이름을 넘겨받으면 표시
        intent.getStringExtra(EXTRA_MEETING_NAME)?.let {
            findViewById<TextView>(R.id.tvMeetingName).text = it
        }

        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }

        // 사진 영역을 누르면 카메라 실행
        findViewById<FrameLayout>(R.id.layoutPhoto).setOnClickListener { openCamera() }
        tvRetake.setOnClickListener { openCamera() }

        findViewById<TextView>(R.id.btnComplete).setOnClickListener { submit() }
    }

    /** 카메라 앱 실행 */
    private fun openCamera() {
        try {
            val photoFile = File.createTempFile(
                "run_${System.currentTimeMillis()}",
                ".jpg",
                cacheDir
            )

            // null이 아닌 지역 변수로 받아서 사용
            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.provider",
                photoFile
            )

            photoUri = uri
            takePicture.launch(uri)     // ← uri 를 넘김

        } catch (e: Exception) {
            e.printStackTrace()
            toast("에러: ${e}")
        }
    }

    /** 촬영한 사진을 화면에 표시 */
    private fun showPhoto() {
        ivPhoto.setImageURI(null)          // 같은 화면에 다시 찍을 때 갱신되도록 초기화
        ivPhoto.setImageURI(photoUri)
        ivPhoto.visibility = View.VISIBLE
        layoutPhotoHint.visibility = View.GONE
        tvRetake.visibility = View.VISIBLE
        hasPhoto = true
    }

    private fun submit() {
        val distanceText = etDistance.text.toString().trim()
        val timeText = etTime.text.toString().trim()
        val paceText = etPace.text.toString().trim()
        val memo = etMemo.text.toString().trim()

        if (!hasPhoto) {
            toast("러닝 사진을 촬영해주세요.")
            return
        }
        if (distanceText.isEmpty() || timeText.isEmpty() || paceText.isEmpty()) {
            toast("거리·시간·페이스를 모두 입력해주세요.")
            return
        }
        if (meetingId == -1) {
            toast("모임 정보를 찾을 수 없어요.")
            return
        }

        val distance = distanceText.toDoubleOrNull()
        if (distance == null) {
            toast("거리는 숫자로 입력해주세요. 예) 5.2")
            return
        }
        // 시간은 "32:15"(분:초) 형식이면 분 단위 소수로 변환, 그냥 숫자만 입력했으면 그대로 분으로 처리
        val timeInMinutes = parseTimeToMinutes(timeText)
        if (timeInMinutes == null) {
            toast("시간 형식을 확인해주세요. 예) 32:15 또는 32.5")
            return
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())

        dbHelper.insertRunningRecord(
            meetingId = meetingId,
            userId = DBHelper.CURRENT_USER_ID,
            photoPath = photoUri?.toString(),
            distance = distance,
            time = timeInMinutes,
            date = today
        )
        // 페이스("$paceText")와 한줄소감("$memo")은 현재 running_records 스키마에
        // 저장할 컬럼이 없어서 이번엔 DB에는 안 들어갑니다. 필요하면 팀 확인 후 컬럼 추가하면 됩니다.

        toast("인증이 완료되었습니다! (${distance}km)")
        finish()
    }

    /** "32:15"(분:초) 또는 "32.5"(분) 형식을 분 단위 소수로 변환 */
    private fun parseTimeToMinutes(text: String): Double? {
        return if (text.contains(":")) {
            val parts = text.split(":")
            if (parts.size != 2) return null
            val minutes = parts[0].trim().toIntOrNull() ?: return null
            val seconds = parts[1].trim().toIntOrNull() ?: return null
            minutes + (seconds / 60.0)
        } else {
            text.toDoubleOrNull()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}