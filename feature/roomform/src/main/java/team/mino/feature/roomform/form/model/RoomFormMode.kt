package team.mino.feature.roomform.form.model

import androidx.compose.runtime.Immutable

/** `roomId`의 유무가 곧 모드다 — 상단 타이틀·CTA 라벨·저장 확인 모달 표출 여부·이탈 모달 종류가 이 값으로 갈린다. */
@Immutable
internal sealed interface RoomFormMode {
    data object Create : RoomFormMode

    data class Edit(val roomId: String) : RoomFormMode
}
