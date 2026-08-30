package team.mino.feature.main

import android.os.Bundle
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
import team.mino.core.navigation.activity.launcher.EXTRA_PLACE_DETAIL_PIN_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.PlaceDetailLauncher
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 실제 진입점 feature가 생기면 폼 진입·결과 수신 배선을 그쪽으로 옮기고 여기서 걷어낸다
    // (→ docs/specs/group-room-form/plan.md §범위 경계).
    @Inject
    lateinit var roomFormLauncher: RoomFormLauncher

    @Inject
    lateinit var placeDetailLauncher: PlaceDetailLauncher

    private var roomFormResult by mutableStateOf<String?>(null)

    /** 폼이 마지막으로 돌려준 방 id. 편집 경로는 이 방을 연다. */
    private var lastRoomId by mutableStateOf<String?>(null)

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
                    onNavigateToPlaceDetail = ::launchPlaceDetail,
                    onNavigateToRoomForm = { launchRoomForm() },
                    // 결과가 바뀔 때만 새로 만든다. 매 리컴포지션마다 새 묶음을 넘기면 셸 아래의
                    // `NavHost`가 그래프 생성 키를 잃어 그래프를 통째로 다시 만든다.
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
     * 장소 상세를 연다. 홈 카드가 지목한 핀 하나가 진입 인자 전부다
     * (→ docs/specs/place-detail/contracts/place-detail-launcher.md §2).
     *
     * 결과를 받지 않으므로 `resultLauncher`를 넘기지 않는다(같은 계약 §3). 그래서 상세에서 나가면 홈으로
     * 되돌아온다 — 「지금 보고 있는 방의 [SCR-005] 방 상세로 나간다」(place-detail spec FR-009)와 어긋난
     * 채 남는 부채이며, 방 상세(#161)가 머지된 뒤 결과 반환으로 닫는다.
     */
    private fun launchPlaceDetail(pinId: String) {
        placeDetailLauncher.launch(this) { putExtra(EXTRA_PLACE_DETAIL_PIN_ID, pinId) }
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
