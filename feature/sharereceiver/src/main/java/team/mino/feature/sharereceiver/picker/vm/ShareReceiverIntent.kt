package team.mino.feature.sharereceiver.picker.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.feature.sharereceiver.picker.model.SheetStep

/**
 * 방 선택 시트에서 사용자가 일으키는 일.
 *
 * 시트를 닫는 경로는 뒤로가기·딤 탭·아래 드래그 셋이지만 결과가 같아 [Dismiss] 하나로 받는다.
 */
internal sealed interface ShareReceiverIntent : Intent {
    /** 카드 영역 어디를 눌러도 들어온다. 이미 고른 방이면 해제다. */
    data class ToggleRoom(
        val roomId: String,
    ) : ShareReceiverIntent

    /** 드래그가 끝나 시트가 어느 단계에 멈췄는지 알린다. 어느 단계로 멈출지는 시트가 판정한 결과다. */
    data class ChangeStep(
        val step: SheetStep,
    ) : ShareReceiverIntent

    /** 하단 [저장하기]. 선택이 비어 있으면 버튼이 비활성이라 도달하지 않는다. */
    data object Save : ShareReceiverIntent

    /** 저장하지 않고 시트를 닫는다. */
    data object Dismiss : ShareReceiverIntent

    /**
     * 시트가 떠 있는 동안 새 공유가 도착해 저장 대상 링크가 갈렸다.
     *
     * 사용자가 일으킨 것이 아니라 진입점이 `onNewIntent`로 받아 올리는 유일한 의도다
     * (EC-013 · `docs/specs/shared-link-receiver/research.md` R-024).
     */
    data class SharedUrlReplaced(
        val url: String,
    ) : ShareReceiverIntent
}
