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
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.navigation.activity.launcher.EXTRA_MAIN_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_PROFILE_ENTRY_POINT
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_ID
import team.mino.core.navigation.activity.launcher.EXTRA_PUSH_DESTINATION_TYPE
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ONBOARDING
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_OUTCOME
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_RESULT_ROOM_ID
import team.mino.core.navigation.activity.launcher.EXTRA_ROOM_FORM_ROOM_ID
import team.mino.core.navigation.activity.launcher.PROFILE_ENTRY_POINT_EDIT
import team.mino.core.navigation.activity.launcher.PUSH_DESTINATION_TYPE_PLACE
import team.mino.core.navigation.activity.launcher.PUSH_DESTINATION_TYPE_ROOM
import team.mino.core.navigation.activity.launcher.ProfileLauncher
import team.mino.core.navigation.activity.launcher.RoomFormLauncher
import team.mino.core.navigation.deeplink.SplashDeepLinkIntentFactory
import team.mino.core.navigation.entry.MainEntryGate
import team.mino.core.navigation.entry.PlaceDetailEntryOrigin
import team.mino.core.navigation.entry.PlaceDetailRequestHolder
import team.mino.core.navigation.entry.RoomDetailRequestHolder
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

    /**
     * 푸시 딥링크와 알림 탭이 저장 탭에 남기는 방 상세 요청. 장소 상세와 같은 이유로 Route 인자가 아니라
     * 홀더를 지난다(→ docs/specs/push-notification/contracts/push-deeplink-contract.md §4,
     * docs/specs/notifications/contracts/notification-ui.md §1).
     */
    @Inject
    lateinit var roomDetailRequestHolder: RoomDetailRequestHolder

    /** 이 프로세스에서 스플래시가 Main 진입을 확정했는지. 콜드 우회 판정에만 읽는다(같은 계약 §3). */
    @Inject
    lateinit var mainEntryGate: MainEntryGate

    /** 콜드 우회용 스플래시 Intent. 플래그는 팩토리가 걸고 여기서는 푸시 extra만 싣는다(같은 계약 §2). */
    @Inject
    lateinit var splashDeepLinkIntentFactory: SplashDeepLinkIntentFactory

    @Inject
    lateinit var profileLauncher: ProfileLauncher

    private var roomFormResult by mutableStateOf<String?>(null)

    /**
     * 웜 경로(`onNewIntent`)가 남기는 대기 중인 도착지 탭. NavHost가 이미 떠 있어 시작 목적지로는 못 옮기므로
     * 상태로 들고 `MainShell`이 명령형으로 소비한 뒤 비운다(research.md D15).
     */
    private var pendingTab by mutableStateOf<MainTab?>(null)

    /** 폼이 마지막으로 돌려준 방 id. 편집 경로는 이 방을 연다. */
    private var lastRoomId by mutableStateOf<String?>(null)

    private val roomFormResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.resultRoomId()?.let { lastRoomId = it }
            roomFormResult = result.describe()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 콜드 우회는 반드시 setContent 전이어야 한다. 그 뒤로 미루면 ViewModel이 만들어져 첫 서버 요청이
        // 세션 없이 나간다(research.md D13). 판정 범위는 푸시 extra가 있을 때뿐이다 — 최근 앱 복원은 건드리지 않는다.
        if (bypassToSplashIfSessionNotReady()) return
        enableEdgeToEdge()

        // 하단 시스템 내비게이션 바 — 앱은 라이트 테마 하나로만 동작해(PRD, 다크모드 비목표) 그 바로 위는
        // 항상 앱 자체의 흰 배경(바텀 네비게이션·시트)인데, 시스템이 대비 확보용으로 얹는 기본 스크림 때문에
        // 실기기에서 그 흰 배경과 안 어울리게 회색으로 떠 있었다(#290 QA). `SheetParts.kt`의
        // `LightStatusBarIcons`가 상태바에 쓰는 것과 같은 `WindowInsetsControllerCompat` API를 여기서는
        // 시트별이 아니라 앱 전역에 한 번만 적용한다 — 내비게이션 바 아래는 시트 유무와 무관하게 항상 밝다.
        window.isNavigationBarContrastEnforced = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

        // 콜드 (C): 홀더 요청을 먼저 남기고 시작 목적지로 탭을 정한다(research.md D9).
        val startTab = resolvePendingPushDestination() ?: MainTab.HOME

        setContent {
            MinoAndroidAppTheme {
                MainShell(
                    onRequestPlaceDetail = placeDetailRequestHolder::request,
                    onRequestRoomDetail = roomDetailRequestHolder::request,
                    onOpenExternalMap = ::openExternalMap,
                    onOpenSourceLink = ::openSourceLink,
                    onNavigateToRoomForm = { launchRoomForm() },
                    onNavigateToProfileEdit = ::launchProfileEdit,
                    // 초대 딥링크(SYS-010)로 참여까지 끝난 방. 콜드 스타트 진입 인자라 컴포저블로
                    // 드릴링하지 않고 시작 라우트에 실어 보낸다(feature-navigation.md 2장).
                    initialRoomId = intent.getStringExtra(EXTRA_MAIN_ROOM_ID),
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
                    startTab = startTab,
                    pendingTab = pendingTab,
                    onPendingTabConsumed = { pendingTab = null },
                )
            }
        }
    }

    /**
     * 웜 경로 — 매니페스트가 `singleTask`라 살아 있는 인스턴스에 알림 Intent가 이리로 온다(research.md D14).
     * `setIntent`로 갈아 끼워야 [resolvePendingPushDestination]이 새 extra를 읽고, 재생성 시에도 같은 Intent를 본다.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolvePendingPushDestination()?.let { pendingTab = it }
    }

    /**
     * (B) 콜드 우회. 프로세스가 죽은 채 알림을 누르면 스플래시 없이 Main이 뜨므로, 게이트가 아직 통과 표시가
     * 아니면 TYPE·ID 두 extra만 그대로 실어 `SplashActivity`로 넘기고 끝낸다 — 시작 경로(세션 확보)를 그대로
     * 지나게 하려는 것이다(spec FR-010). 값은 여기서 해석하지 않는다. 해석은 (C) 한 곳뿐이다(계약 §1).
     *
     * @return 우회했으면 `true`. 호출자는 그 자리에서 `onCreate`를 끝내야 한다.
     */
    private fun bypassToSplashIfSessionNotReady(): Boolean {
        val pushType = intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE) ?: return false
        if (mainEntryGate.isPassed) return false

        // 스플래시가 이미 진행 중이면 팩토리가 건 CLEAR_TOP이 이 Main을 정리하고 그 스플래시가 onNewIntent로 받는다(D16).
        startActivity(
            splashDeepLinkIntentFactory.create(this).apply {
                putExtra(EXTRA_PUSH_DESTINATION_TYPE, pushType)
                intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)?.let { putExtra(EXTRA_PUSH_DESTINATION_ID, it) }
            },
        )
        finish()
        return true
    }

    /**
     * (C) 소비. 푸시 extra를 읽고 지운 뒤, 홀더에 요청을 남기고 갈 탭을 돌려준다. extra가 없으면 `null`.
     *
     * 지우는 이유는 회전 등 재생성 시 같은 요청이 중복 소비되지 않게 하기 위해서다(계약 §1). 홀더 요청은 탭 전환
     * **전에** 끝나야 저장 탭이 들어오면서 요청을 본다(계약 §5). `PLACE`·`ROOM`인데 id가 없는 경우와 모르는
     * 유형은 알림 탭으로 낙하한다(spec EC-009) — `PUSH_DESTINATION_TYPE_NOTIFICATION_TAB`도 같은 갈래다.
     */
    private fun resolvePendingPushDestination(): MainTab? {
        val type = intent.getStringExtra(EXTRA_PUSH_DESTINATION_TYPE) ?: return null
        val id = intent.getStringExtra(EXTRA_PUSH_DESTINATION_ID)
        intent.removeExtra(EXTRA_PUSH_DESTINATION_TYPE)
        intent.removeExtra(EXTRA_PUSH_DESTINATION_ID)
        return when (type) {
            PUSH_DESTINATION_TYPE_PLACE ->
                id?.let {
                    placeDetailRequestHolder.request(it, PlaceDetailEntryOrigin.NOTIFICATION)
                    MainTab.SAVED
                } ?: MainTab.NOTIFICATION
            PUSH_DESTINATION_TYPE_ROOM ->
                id?.let {
                    roomDetailRequestHolder.request(it)
                    MainTab.SAVED
                } ?: MainTab.NOTIFICATION
            else -> MainTab.NOTIFICATION
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
