package team.mino.feature.roomform.form.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.core.domain.model.RoomColor

/**
 * 공동방 폼에서 사용자가 일으키는 일.
 *
 * 뒤로가기는 모달이 먼저다 — 모달이 떠 있는 동안의 뒤로가기는 [BackClicked]가 아니라
 * [DialogDismissed]로 들어온다. 그 순서를 뒤집으면 저장 확인 모달 위의 뒤로가기가
 * 이탈 확인 모달을 띄운다.
 */
internal sealed interface RoomFormIntent : Intent {
    /** 입력된 원본 값. 상한을 넘긴 부분을 잘라내는 것은 ViewModel의 몫이다. */
    data class NameChanged(
        val value: String,
    ) : RoomFormIntent

    /** 상한은 입력 컴포넌트가 이미 잘랐다. 받은 값을 그대로 반영한다. */
    data class DescriptionChanged(
        val value: String,
    ) : RoomFormIntent

    /** 색은 항상 단일 선택이라 이전 선택을 교체한다. 같은 칩을 다시 눌러도 해제되지 않는다. */
    data class ColorSelected(
        val color: RoomColor,
    ) : RoomFormIntent

    /** 하단 CTA. 생성은 확인 모달을 거치고 편집은 곧바로 제출한다. */
    data object SubmitClicked : RoomFormIntent

    /** 저장 확인 모달의 [저장하기]. */
    data object SaveConfirmed : RoomFormIntent

    /** 상단 뒤로가기와 OS 뒤로 제스처. 모달이 없을 때만 도달한다. */
    data object BackClicked : RoomFormIntent

    /** 이탈 확인 모달의 [나가기]. */
    data object ExitConfirmed : RoomFormIntent

    /** 모달의 [취소]·딤 바깥 탭·모달 위의 뒤로가기. 모달만 닫고 다른 상태를 건드리지 않는다. */
    data object DialogDismissed : RoomFormIntent

    /** 온보딩 상단의 [건너뛰기]. 확인 없이 끝낸다. */
    data object SkipClicked : RoomFormIntent

    /** 편집 진입 조회 실패 화면의 재시도. */
    data object RetryLoad : RoomFormIntent
}
