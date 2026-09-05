package team.mino.core.domain.model

/**
 * 알림 하나를 탭했을 때 실제로 열 화면 (`docs/specs/notifications/data-model.md` §1.5).
 * `ResolveNotificationDestinationUseCase`가 [Notification.target] 하나만 보고 돌려주는 판정 결과다.
 *
 * [NotificationTarget]과 갈래가 거의 같지만 타입을 나눠 둔다 — 저장 오류가 대상으로는
 * [NotificationTarget.None]이면서 도착지로는 화면 하나([SaveErrorGuide])라 대응이 어긋나고, 서버에서
 * 받은 참조와 화면이 열 것을 같은 타입으로 부르면 한쪽 규칙이 바뀔 때 다른 쪽이 함께 흔들린다.
 *
 * **`Unreachable`을 두지 않는다.** 대상이 이미 사라졌는지는 알림함이 이동 전에 되묻지 않고 도착지 화면이
 * 판정한다(spec 7.0.0 UX-006·EC-009·EC-010 ·
 * `docs/specs/notifications/contracts/notification-repository.md` §2).
 *
 * 푸시 쪽 [PushDestination]과 합치지 않는다 — 그쪽은 도착지 식별자가 없을 때의 기본 도착지를 갖지만
 * 알림함은 목록에 실린 알림에서만 출발하므로 그런 갈래가 없다.
 */
sealed interface NotificationDestination {
    /**
     * 장소 상세. 장소 대상 3종이 여기로 온다(`docs/specs/notifications/research.md` D14).
     *
     * 나갈 방을 함께 싣지 않는다 — 호출부가 배선하는 `PlaceDetailRequestHolder`가 받는 것은 [pinId]와
     * 진입 출처뿐이고, 어느 방으로 나갈지는 저장 탭이 그 핀에서 스스로 정한다.
     */
    data class PlaceDetail(val pinId: String) : NotificationDestination

    /** 방 상세. 공동방 참가 2종이 여기로 온다(`docs/specs/notifications/research.md` D10). */
    data class RoomDetail(val roomId: String) : NotificationDestination

    /** 저장 오류 안내. 다른 둘과 달리 알림함 그래프 안에서 전환한다(spec FR-010). */
    data object SaveErrorGuide : NotificationDestination
}
