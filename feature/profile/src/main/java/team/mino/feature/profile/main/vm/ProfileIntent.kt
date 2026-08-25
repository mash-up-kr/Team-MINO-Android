package team.mino.feature.profile.main.vm

import team.mino.core.common.android.architecture.Intent
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar

/**
 * 프로필 설정 화면에서 사용자가 일으키는 일.
 *
 * 비활성 상태의 버튼은 인텐트를 만들지 않는다 — 활성 여부는 화면이
 * [ProfileUiState.isSaveEnabled]·[ProfileUiState.isClearEnabled]로 판정한다.
 */
internal sealed interface ProfileIntent : Intent {
    data class NicknameChanged(
        val value: String,
    ) : ProfileIntent

    /** 아바타는 항상 단일 선택이라 이전 선택을 교체한다. */
    data class AvatarSelected(
        val avatar: MinoProfileAvatar,
    ) : ProfileIntent

    data object ClearClicked : ProfileIntent

    data object SaveClicked : ProfileIntent
}
