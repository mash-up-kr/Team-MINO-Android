package team.mino.feature.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_FROM_HOME
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_GREETING
import team.mino.core.navigation.activity.launcher.EXTRA_SAMPLE_RESULT_CONFIRMED
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.core.navigation.activity.launcher.SampleLauncher
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var sampleLauncher: SampleLauncher

    // 실제 진입점 feature가 생기면 폼 진입·결과 수신 배선을 그쪽으로 옮기고 여기서 걷어낸다
    // (→ docs/specs/group-room-form/plan.md §범위 경계).
    @Inject
    lateinit var roomFormLauncher: RoomFormLauncher

    private var roomFormResult by mutableStateOf<String?>(null)

    /** 폼이 마지막으로 돌려준 방 id. 편집 경로는 이 방을 연다. */
    private var lastRoomId by mutableStateOf<String?>(null)

    private val sampleResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val confirmed = result.data?.getBooleanExtra(EXTRA_SAMPLE_RESULT_CONFIRMED, false) ?: false
            Toast.makeText(this, "Sample 결과: confirmed=$confirmed", Toast.LENGTH_SHORT).show()
        }

    private val roomFormResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.resultRoomId()?.let { lastRoomId = it }
            roomFormResult = result.describe()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinoAndroidAppTheme {
                MainShell(
                    onNavigateToSample = {
                        sampleLauncher.launch(this) { putExtra(EXTRA_SAMPLE_FROM_HOME, true) }
                    },
                    onRequestSampleResult = {
                        sampleLauncher.launch(this, resultLauncher = sampleResultLauncher) {
                            putExtra(EXTRA_SAMPLE_GREETING, "결과를 부탁해요")
                        }
                    },
                    roomFormEntryPoint =
                        RoomFormEntryPoint(
                            lastResult = roomFormResult,
                            lastRoomId = lastRoomId,
                            onCreate = { launchRoomForm() },
                            onCreateWithOnboarding = { launchRoomForm(isOnboarding = true) },
                            onEditLastRoom = { roomId -> launchRoomForm(roomId = roomId) },
                        ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    /**
     * 폼을 연다. `roomId`가 있으면 편집, 없으면 생성이다 — 모드를 가르는 값은 그 하나뿐이다
     * (→ docs/specs/group-room-form/contracts/room-form-launcher.md §2).
     */
    private fun launchRoomForm(
        roomId: String? = null,
        isOnboarding: Boolean = false,
    ) {
        // 결과를 받아야 하므로 withFinish를 쓰지 않는다(같은 계약 §2).
        roomFormLauncher.launch(this, resultLauncher = roomFormResultLauncher) {
            roomId?.let { putExtra(EXTRA_ROOM_FORM_ROOM_ID, it) }
            putExtra(EXTRA_ROOM_FORM_ONBOARDING, isOnboarding)
        }
    }

    /**
     * 결과 계약의 네 갈래를 눈으로 확인할 수 있는 한 줄로 옮긴다.
     *
     * 이탈만 결과 상수가 없다 — `RESULT_CANCELED`에 추가 extra 없이 오므로 코드로 읽는다
     * (→ docs/specs/group-room-form/contracts/room-form-launcher.md §3).
     */
    private fun ActivityResult.describe(): String =
        if (resultCode != RESULT_OK) {
            "outcome=cancelled, roomId=(없음)"
        } else {
            val outcome = data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_OUTCOME) ?: "(없음)"
            "outcome=$outcome, roomId=${resultRoomId() ?: "(없음)"}"
        }

    /**
     * 결과가 실어 온 방 id. `created`·`updated`만 이 값을 갖는다
     * (→ docs/specs/group-room-form/contracts/room-form-launcher.md §3).
     */
    private fun ActivityResult.resultRoomId(): String? =
        data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID).takeIf { resultCode == RESULT_OK }
}
