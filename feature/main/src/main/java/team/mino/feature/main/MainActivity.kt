package team.mino.feature.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_PROFILE_ENTRY_POINT
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_EDIT
import team.mino.core.navigation.activity.launcher.ProfileLauncher
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.core.navigation.entry.PlaceDetailRequestHolder
import team.mino.feature.main.placeholder.RoomFormEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 실제 진입점 feature가 생기면 폼 진입·결과 수신 배선을 그쪽으로 옮기고 여기서 걷어낸다
    // (→ docs/specs/group-room-form/plan.md §범위 경계).
    @Inject
    lateinit var roomFormLauncher: RoomFormLauncher

    /**
     * 다른 탭에서 올라온 장소 상세 요청을 저장 탭에 남기는 자리. 장소 상세는 별도 Activity가 아니라 저장 탭
     * 안의 화면이라 여는 값을 홀더에 싣는다
     * (→ docs/specs/place-detail/contracts/place-detail-entry.md §3).
     */
    @Inject
    lateinit var placeDetailRequestHolder: PlaceDetailRequestHolder

    @Inject
    lateinit var profileLauncher: ProfileLauncher

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
                    onRequestPlaceDetail = placeDetailRequestHolder::request,
                    onOpenExternalMap = ::openExternalMap,
                    onOpenSourceLink = ::openSourceLink,
                    onNavigateToRoomForm = { launchRoomForm() },
                    onNavigateToProfileEdit = ::launchProfileEdit,
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
     * 외부 지도로 장소를 연다(장소 상세 spec FR-016).
     *
     * **`geo:` 후보를 맨 앞에 둔다.** FR-016이 갈래를 가르는 조건은 「외부 지도 앱이 있는가」 하나인데, 설치된
     * 지도 앱만 받고 브라우저는 받지 않는 표준 스킴이 `geo:`뿐이다. 서버가 준 [mapUrl]을 먼저 열면 그 링크를
     * 어느 앱이 검증(App Links)해 뒀느냐에 따라 지도 앱이 있는데도 브라우저로 새어 TS-028과 어긋난다.
     * 지도 앱의 패키지명을 박지 않으므로 매니페스트 `<queries>`도 필요 없다.
     *
     * 지도 앱이 없으면 브라우저로 대체한다(spec TS-029) — [mapUrl]이 있으면 그것을, 없으면 장소명으로 만든
     * 지도 검색 URL을 연다. 두 후보를 차례로 시도하므로 아무 반응 없이 끝나지 않는다(spec SC-004).
     */
    private fun openExternalMap(
        mapUrl: String?,
        query: String,
    ) {
        val encodedQuery = Uri.encode(query)
        if (startViewIntent(GEO_SEARCH_URI_PREFIX + encodedQuery)) return
        startViewIntent(mapUrl?.takeIf { it.isNotBlank() } ?: (WEB_MAP_SEARCH_URL_PREFIX + encodedQuery))
    }

    /**
     * 장소의 원문 링크를 연다(장소 상세 spec FR-017).
     *
     * 후보를 하나만 두는 것은 원문이 [SYS-002] 링크 분석이 수집한 그 주소여야 하기 때문이다 — 대신 열 만한
     * 다른 주소가 없다. 열린 뒤 게시글이 삭제돼 오류 화면이 뜨는 것은 앱이 처리하지 않는다(spec EC-018).
     */
    private fun openSourceLink(url: String) {
        startViewIntent(url)
    }

    /**
     * 후보 하나를 외부에 넘긴다. 받을 앱이 없으면 `ActivityNotFoundException`이 나므로, 그것을 「이 후보로는
     * 열리지 않는다」는 신호로 바꿔 호출부가 다음 후보로 넘어가게 한다.
     */
    private fun startViewIntent(uri: String): Boolean =
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
            true
        } catch (notFound: ActivityNotFoundException) {
            false
        }

    /**
     * 마이페이지에서 프로필 편집을 연다. 결과가 필요 없다 — 돌아오면 구독 중인 `observeProfile()`
     * Flow가 새 값을 흘린다(`docs/specs/profile/contracts/profile-launcher-contract.md` §호출 방법).
     */
    private fun launchProfileEdit() {
        profileLauncher.launch(this) { putExtra(EXTRA_PROFILE_ENTRY_POINT, PROFILE_ENTRY_POINT_EDIT) }
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

    private companion object {
        /** 좌표가 아니라 검색어로 여는 형태다. 어느 지도 앱이 받을지는 사용자의 기본 설정이 정한다. */
        const val GEO_SEARCH_URI_PREFIX = "geo:0,0?q="

        /** 지도 앱이 없을 때의 웹 검색. `:core:map`이 Google Maps를 쓰므로 브라우저 쪽도 같은 서비스로 맞춘다. */
        const val WEB_MAP_SEARCH_URL_PREFIX = "https://www.google.com/maps/search/?api=1&query="
    }
}
