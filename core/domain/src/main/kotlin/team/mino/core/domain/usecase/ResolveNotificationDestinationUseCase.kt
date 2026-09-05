package team.mino.core.domain.usecase

import team.mino.core.domain.model.Notification
import team.mino.core.domain.model.NotificationDestination
import team.mino.core.domain.model.NotificationTarget
import javax.inject.Inject

/**
 * 알림 하나를 실제로 열 화면으로 바꾼다(FR-005·FR-022 ·
 * `docs/specs/notifications/contracts/notification-repository.md` §2).
 *
 * **`suspend`가 아니고 저장소를 주입받지 않는다.** 서버 `payload`가 도착지 핀을 직접 주므로 조회할 것이
 * 없다(`docs/specs/notifications/research.md` D8). 같은 모듈의 [ResolvePushDestinationUseCase]가 푸시
 * 쪽에서 같은 모양으로 서 있다.
 *
 * 이 UseCase가 하지 않는 것 — 접근 기록, 표시 기준 방 조회, 방 선택 질의, 네트워크 호출, 그리고 대상이
 * 아직 살아 있는지의 확인이다. 마지막 것은 spec 7.0.0이 도착지 화면의 몫으로 옮겼고, 그래서
 * [NotificationDestination]에 `Unreachable` 갈래가 없다.
 */
class ResolveNotificationDestinationUseCase @Inject constructor() {
    /**
     * [notification]의 대상 하나로 도착지가 전부 갈린다. **분기 조건이 더 없다** — 유형은 이미
     * [Notification.target]의 갈래에 반영돼 있어 여기서 다시 보지 않는다.
     *
     * 저장 오류는 대상이 [NotificationTarget.None]이면서 도착지로는 화면 하나
     * ([NotificationDestination.SaveErrorGuide])다 — 두 타입을 나눠 둔 이유가 이 어긋남이다.
     */
    operator fun invoke(notification: Notification): NotificationDestination =
        when (val target = notification.target) {
            is NotificationTarget.Pin -> NotificationDestination.PlaceDetail(target.pinId)
            is NotificationTarget.Room -> NotificationDestination.RoomDetail(target.roomId)
            NotificationTarget.None -> NotificationDestination.SaveErrorGuide
        }
}
