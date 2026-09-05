package team.mino.feature.notifications.main.vm

import team.mino.core.common.android.architecture.Intent

/**
 * 알림 탭에서 사용자 조작 하나가 하나씩 대응한다 —
 * `docs/specs/notifications/contracts/notification-ui.md` §2가 목록을 소유한다.
 */
internal sealed interface NotificationIntent : Intent {
    /** 화면 최초 진입. 첫 페이지만 조회하고 방 목록을 함께 부르지 않는다(`research.md` D5). */
    data object Load : NotificationIntent

    /** 오류 상태의 재시도. [Load]와 같되 목록을 처음부터 다시 세운다. */
    data object Retry : NotificationIntent

    /**
     * 목록 끝 도달. `더 보기` 버튼이 없으므로 끝 감지가 곧 의도다(spec UX-011).
     *
     * 더 받을 것이 없거나 이미 이어 붙이는 중이면 버린다(spec EC-018).
     */
    data object ReachedEnd : NotificationIntent

    /** 목록 끝의 추가 로드 실패 표시 탭. 실패 표시를 내리고 같은 페이지를 다시 요청한다. */
    data object RetryAppend : NotificationIntent

    /**
     * 알림 행 탭. 유형·대상은 ViewModel이 들고 있는 도메인 목록에서 [id]로 찾는다 —
     * 화면 모델에는 그 필드가 없다(`data-model.md` §2.1).
     *
     * 처리 중에 들어온 같은 의도는 버린다(spec EC-011). 홀더 적재와 탭 전환이 두 번 나가면 어긋난다.
     */
    data class NotificationClicked(
        val id: String,
    ) : NotificationIntent

    /** 저장 오류 안내 화면의 뒤로가기(spec FR-011). */
    data object SaveErrorGuideBackClicked : NotificationIntent
}
