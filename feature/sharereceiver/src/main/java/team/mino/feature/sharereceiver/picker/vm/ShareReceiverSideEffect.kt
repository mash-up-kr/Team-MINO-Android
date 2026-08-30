package team.mino.feature.sharereceiver.picker.vm

import team.mino.core.common.android.architecture.SideEffect

/**
 * 이 화면은 끝나는 방법이 둘뿐이라 SideEffect도 그 둘이다.
 *
 * 저장 완료를 어떻게 알릴지(토스트 문구·배치)는 진입점이 정한다 — 시트는 저장하고 끝났다는
 * 사실만 알린다.
 */
internal sealed interface ShareReceiverSideEffect : SideEffect {
    /** 저장을 예약하고 끝났다. 완료 알림이 사라진 뒤 Activity가 물러난다. */
    data object SavedAndFinish : ShareReceiverSideEffect

    /** 아무것도 저장하지 않고 끝났다. 알림 없이 곧바로 물러난다. */
    data object Finish : ShareReceiverSideEffect
}
