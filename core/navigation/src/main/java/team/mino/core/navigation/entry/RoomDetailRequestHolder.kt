package team.mino.core.navigation.entry

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 다른 자리(알림 탭·푸시 딥링크)가 저장 탭에게 「이 방의 방 상세를 열어 달라」고 요청하는 자리.
 *
 * 방 상세는 저장 탭 안의 로컬 상태(`selectedRoomId`)로 열리므로, 탭 밖에서 여는 경로는
 * [PlaceDetailRequestHolder]와 같은 이유로 Route 인자가 아니라 공유 상태를 지난다 — 탭 전환이
 * 백스택을 `saveState`/`restoreState`로 복원하면 저장 당시의 인자가 되살아나 새 값이 무시된다.
 *
 * [PlaceDetailRequestHolder]와 달리 진입 출처(`origin`)를 싣지 않는다. 방 상세의 [나가기]는
 * 진입 경로에 따라 갈리지 않기 때문이다(`docs/specs/push-notification/research.md` D8).
 *
 * 스코프가 `ActivityRetainedComponent`인 이유는 탭 전환이 같은 Activity 안의 일이고, 구성
 * 변경(회전)에도 요청이 살아남아야 하기 때문이다.
 */
@ActivityRetainedScoped
class RoomDetailRequestHolder @Inject constructor() {
    private val _pending = MutableStateFlow<String?>(null)

    /**
     * 열어야 할 방의 `roomId`. 소비되면 `null`로 돌아간다.
     *
     * 비우지 않으면 사용자가 닫은 방이 탭을 오갈 때마다 다시 열린다 — 저장 탭이 이 값을 구독하는
     * 이상, 남아 있는 요청은 탭에 들어올 때마다 유효한 요청으로 읽힌다.
     */
    val pending: StateFlow<String?> = _pending.asStateFlow()

    /** 다른 자리가 방 상세를 요청한다. */
    fun request(roomId: String) {
        _pending.value = roomId
    }

    /** 저장 탭이 요청을 받아 갔다. */
    fun consume() {
        _pending.value = null
    }
}
