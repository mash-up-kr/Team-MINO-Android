package team.mino.core.navigation.entry

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 다른 탭이 저장 탭에게 「이 핀의 장소 상세를 열어 달라」고 요청하는 자리.
 *
 * 장소 상세는 저장 탭 안의 화면이라 탭 밖(홈·알림)에서 여는 경로가 탭 전환을 거친다. 그런데 탭
 * 전환은 떠난 탭의 백스택을 `saveState`/`restoreState`로 저장·복원하므로, 복원된 목적지는 저장
 * 당시의 Route 인자를 그대로 들고 되살아나 새 `pinId`가 반영되지 않는다. 그래서 여는 값을 Route
 * 인자가 아니라 이 홀더에 싣는다. 셸과 탭이 서로의 구체 타입을 모른 채 공유 상태로 합의한다는
 * 점에서 `LocalBottomNavVisibility`·[team.mino.core.navigation.screen.ImmersiveRouteRegistry]와
 * 같은 형태다.
 *
 * 스코프가 `ActivityRetainedComponent`인 이유는 탭 전환이 같은 Activity 안의 일이고, 구성
 * 변경(회전)에도 요청이 살아남아야 하기 때문이다.
 */
@ActivityRetainedScoped
class PlaceDetailRequestHolder @Inject constructor() {
    private val _pending = MutableStateFlow<PlaceDetailRequest?>(null)

    /**
     * 열어야 할 핀과 그것을 요청한 탭. 소비되면 `null`로 돌아간다.
     *
     * 비우지 않으면 사용자가 [나가기]로 닫은 장소가 탭을 오갈 때마다 다시 열린다 — 저장 탭이
     * 이 값을 구독하는 이상, 남아 있는 요청은 탭에 들어올 때마다 유효한 요청으로 읽힌다.
     */
    val pending: StateFlow<PlaceDetailRequest?> = _pending.asStateFlow()

    /** 다른 탭이 장소 상세를 요청한다. */
    fun request(
        pinId: String,
        origin: PlaceDetailEntryOrigin,
    ) {
        _pending.value = PlaceDetailRequest(pinId = pinId, origin = origin)
    }

    /** 저장 탭이 요청을 받아 갔다. */
    fun consume() {
        _pending.value = null
    }
}

/**
 * 탭 간 장소 상세 요청 한 건.
 *
 * [origin]을 함께 싣는 이유는 [나가기]의 목적지가 진입 경로에 따라 갈리기 때문이다
 * (`docs/specs/place-detail/spec.md` FR-009). 탭 전환이 끝나면 「어디서 들어왔는가」가 어디에도
 * 남지 않아 저장 탭이 나중에 되물을 수 없으므로, 요청과 함께 실어 보낸다.
 */
data class PlaceDetailRequest(
    val pinId: String,
    val origin: PlaceDetailEntryOrigin,
)

/**
 * 장소 상세를 연 탭 — [나가기]가 어느 자리로 나갈지를 가르는 유일한 값
 * (`docs/specs/place-detail/contracts/place-detail-entry.md` §4).
 *
 * [HOME]만 홈 탭으로 되돌리고 나머지는 「지금 보고 있는 방」의 방 상세로 나간다. 저장 탭 안에서
 * 여는 경로(지도 마커·방 상세 목록)는 이 홀더를 지나지 않아 출처 자체가 없다.
 */
enum class PlaceDetailEntryOrigin {
    /** [SCR-003] 홈 카드 덱. 방을 바꾸지 않았다면 [나가기]가 홈 탭으로 되돌린다. */
    HOME,

    /**
     * [SCR-007] 알림. 화면이 아직 없어 지금은 쓰이지 않지만, 기본 동작(방 상세로 나감)이
     * [HOME]과 갈린다는 사실을 타입으로 남겨 둔다 — 값이 하나뿐이면 홈이 아닌 탭 간 진입이
     * 생길 때 [HOME]으로 잘못 적어 넣기 쉽다.
     */
    NOTIFICATION,
}
