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
import java.io.File

class RunProofActivity : AppCompatActivity() {

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

        etDistance = findViewById(R.id.etDistance)
        etTime = findViewById(R.id.etTime)
        etPace = findViewById(R.id.etPace)
        etMemo = findViewById(R.id.etMemo)
        ivPhoto = findViewById(R.id.ivPhoto)
        layoutPhotoHint = findViewById(R.id.layoutPhotoHint)
        tvRetake = findViewById(R.id.tvRetake)

        // 이전 화면에서 모임 이름을 넘겨받으면 표시
        intent.getStringExtra("meeting_name")?.let {
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
        val distance = etDistance.text.toString().trim()
        val time = etTime.text.toString().trim()
        val pace = etPace.text.toString().trim()
        val memo = etMemo.text.toString().trim()

        if (!hasPhoto) {
            toast("러닝 사진을 촬영해주세요.")
            return
        }
        if (distance.isEmpty() || time.isEmpty() || pace.isEmpty()) {
            toast("거리·시간·페이스를 모두 입력해주세요.")
            return
        }

        // TODO: 서버 또는 DB에 저장 (photoUri, distance, time, pace, memo)
        toast("인증이 완료되었습니다! (${distance}km)")
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}