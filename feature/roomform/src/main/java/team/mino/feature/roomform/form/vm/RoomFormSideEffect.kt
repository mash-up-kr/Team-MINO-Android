package team.mino.feature.roomform.form.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 이 화면의 SideEffect는 종료 신호 하나뿐이다.
 *
 * 스낵바 표시와 화면 전환은 폼이 정하지 않는다 — 폼은 무슨 일이 일어났는지만 알리고,
 * 그 다음에 어디로 갈지는 진입점이 결정한다.
 */
internal sealed interface RoomFormSideEffect : SideEffect {
    data class Finish(val outcome: RoomFormOutcome) : RoomFormSideEffect
}

/** 폼이 끝난 이유. 진입점이 이 값을 보고 다음 화면을 정한다. */
internal sealed interface RoomFormOutcome {
    data class Created(val roomId: String) : RoomFormOutcome

    data class Updated(val roomId: String) : RoomFormOutcome

    /** 온보딩에서 건너뛰기로 방을 만들지 않고 끝냈다. */
    data object Skipped : RoomFormOutcome

    /** 사용자가 저장하지 않고 나갔다. */
    data object Cancelled : RoomFormOutcome
}
