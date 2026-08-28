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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 실제 진입점 feature가 생기면 폼 진입·결과 수신 배선을 그쪽으로 옮기고 여기서 걷어낸다
    // (→ docs/specs/group-room-form/plan.md §범위 경계).
    @Inject
    lateinit var roomFormLauncher: RoomFormLauncher

    private var roomFormResult by mutableStateOf<String?>(null)

    private val roomFormResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            roomFormResult = result.describe()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinoAndroidAppTheme {
                MainShell(
                    // [SCR-006] 장소 상세 feature가 아직 없다. 그 모듈이 생기면 런처 호출로 바꾼다
                    // (→ docs/specs/home-deck-exploration/spec.md FR-007).
                    onNavigateToPlaceDetail = { pinId ->
                        Toast.makeText(this, "장소 상세: pinId=$pinId", Toast.LENGTH_SHORT).show()
                    },
                    onNavigateToRoomForm = { launchRoomForm() },
                    // 결과가 바뀔 때만 새로 만든다. 매 리컴포지션마다 새 묶음을 넘기면 셸 아래의
                    // `NavHost`가 그래프 생성 키를 잃어 그래프를 통째로 다시 만든다.
                    roomFormEntryPoint =
                        remember(roomFormResult) {
                            RoomFormEntryPoint(
                                lastResult = roomFormResult,
                                onCreate = { launchRoomForm() },
                                onCreateWithOnboarding = { launchRoomForm(isOnboarding = true) },
                                onEditSeedRoom = { launchRoomForm(roomId = SEED_ROOM_ID) },
                            )
                        },
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
            val roomId = data?.getStringExtra(EXTRA_ROOM_FORM_RESULT_ROOM_ID) ?: "(없음)"
            "outcome=$outcome, roomId=$roomId"
        }

    private companion object {
        /** mock 저장소의 시드 공동방(`야호`). 편집 경로를 손으로 확인하는 데만 쓴다. */
        const val SEED_ROOM_ID = "room-1"
    }
}
