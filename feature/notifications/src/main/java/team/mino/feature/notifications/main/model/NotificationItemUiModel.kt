package team.mino.feature.notifications.main.model

import androidx.compose.runtime.Immutable
import team.mino.feature.notifications.main.util.ElapsedTime

/**
 * 알림 행 한 줄이 그리는 것 전부(`docs/specs/notifications/data-model.md` §2.1, spec FR-002).
 *
 * 도메인 `Notification`을 그대로 흘리지 않는다. 화면이 필요로 하는 것은 **어느 구간으로 끊을지 정해진 경과
 * 시간**([elapsed])과 **어떤 썸네일을 그릴지 정해진 상태**([thumbnail])이고, 그 판정을 그리는 자리에서 다시
 * 하면 리컴포지션마다 결과가 흔들린다.
 *
 * **유형과 대상 참조를 담지 않는다.** 화면이 하는 일은 행을 그리고 탭을 알리는 것뿐이라 탭 의도는 [id]만
 * 싣고, 도착지 판정은 UseCase가 한다(`research.md` D8,
 * `docs/specs/notifications/contracts/notification-ui.md` §2).
 */
@Immutable
internal data class NotificationItemUiModel(
    /** 서버 UUID. 목록 키이자 탭 의도가 싣는 값이다. */
    val id: String,
    /** 서버가 완성해 준 유형 문구 — 클라이언트가 조립하지 않는다(`research.md` D4). */
    val typeLabel: String,
    /** 장소명 또는 공동방 이름. 저장 오류 알림은 서버가 안내 문구를 이 자리에 실어 준다. */
    val targetName: String,
    /**
     * 발생 시각을 FR-003의 네 구간 중 하나로 끊은 결과.
     *
     * 문구가 아니라 갈래다 — 문자열로 굳혀 두면 그것을 만드는 자리가 문자열 리소스를 잡게 되고, 목록을 받은
     * 시점에 한 번 끊는다는 규칙(spec EC-005)도 흐려진다.
     */
    val elapsed: ElapsedTime,
    val thumbnail: NotificationThumbnail,
)
