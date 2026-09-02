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
    private val _pending = MutableStateFlow<String?>(null)

    /**
     * 열어야 할 핀. 소비되면 `null`로 돌아간다.
     *
     * 비우지 않으면 사용자가 [나가기]로 닫은 장소가 탭을 오갈 때마다 다시 열린다 — 저장 탭이
     * 이 값을 구독하는 이상, 남아 있는 요청은 탭에 들어올 때마다 유효한 요청으로 읽힌다.
     */
    val pending: StateFlow<String?> = _pending.asStateFlow()

    /** 다른 탭이 장소 상세를 요청한다. */
    fun request(pinId: String) {
        _pending.value = pinId
    }

    /** 저장 탭이 요청을 받아 갔다. */
    fun consume() {
        _pending.value = null
    }
}
