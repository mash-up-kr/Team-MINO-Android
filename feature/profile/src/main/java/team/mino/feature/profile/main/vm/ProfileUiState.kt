package team.mino.feature.profile.main.vm

import team.mino.core.common.android.architecture.UiState
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.feature.profile.main.model.DefaultProfileAvatar
import team.mino.feature.profile.main.model.ProfileEntryPoint

/**
 * 프로필 설정 화면의 상태.
 *
 * 저장이 어디로 나가는지는 담지 않는다 — 그 뒤가 로컬인지 원격인지가 이 타입에 흔적을 남기면
 * 저장 경로가 바뀔 때 화면까지 함께 흔들린다.
 *
 * 화면이 그리려고 계산하는 값(썸네일·버튼 활성·오류 표시·뒤로가기)은 필드가 아니라 아래의
 * 파생 프로퍼티다. 같은 조건을 화면과 ViewModel이 각자 계산해 어긋나는 것을 막는다.
 *
 * @property selectedAvatar `null`은 "고르지 않음"이다. 기본 아바타로 초기화하지 않는다 —
 *  그러면 [isClearEnabled]가 첫 화면부터 참이 된다.
 * @property isNicknameTouched 진입 직후에는 오류 문구가 뜨지 않아야 해서 둔다. 첫 입력에서 참이 되고
 *  `지우기`로 거짓으로 돌아간다.
 */
internal data class ProfileUiState(
    val entryPoint: ProfileEntryPoint = ProfileEntryPoint.Onboarding,
    val nickname: String = "",
    val selectedAvatar: MinoProfileAvatar? = null,
    val isNicknameValid: Boolean = false,
    val isNicknameTouched: Boolean = false,
    val isSaving: Boolean = false,
) : UiState {
    /** 상단 썸네일에 그릴 아바타. 고르지 않았으면 기본 아바타가 자리를 채운다. */
    val displayedAvatar: MinoProfileAvatar
        get() = selectedAvatar ?: DefaultProfileAvatar

    /** 아바타는 선택 입력이라 닉네임만 유효하면 저장할 수 있다. */
    val isSaveEnabled: Boolean
        get() = isNicknameValid && !isSaving

    /** 닉네임이 유효하고 아바타도 고른, 둘 다 채워진 상태에서만 활성이다. */
    val isClearEnabled: Boolean
        get() = isNicknameValid && selectedAvatar != null && !isSaving

    /** 한 번도 입력하지 않은 닉네임은 무효여도 오류로 보이지 않는다. */
    val isNicknameErrorVisible: Boolean
        get() = isNicknameTouched && !isNicknameValid

    /** 뒤로갈 수 있는 진입점에서만 상단 바에 뒤로가기가 놓인다. 온보딩에서는 버튼 자리를 비운다. */
    val isBackEnabled: Boolean
        get() = entryPoint == ProfileEntryPoint.MyPage
}
