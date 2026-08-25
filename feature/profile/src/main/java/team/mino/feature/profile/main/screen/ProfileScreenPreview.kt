package team.mino.feature.profile.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatar
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.profile.main.model.ProfileEntryPoint
import team.mino.feature.profile.main.vm.ProfileUiState

/*
 * ProfileScreen의 상태별 렌더 프리뷰.
 *
 * 화면은 stateless라 상태를 직접 만들어 넘긴다. 파생 값(썸네일·버튼 활성·오류 표시·뒤로가기)은
 * ProfileUiState가 계산하므로 여기서는 그 계산을 흉내 내지 않고 입력 필드만 조합한다.
 *
 * 앞의 네 장은 진입점을 온보딩으로 고정한 채 화면 상태만 바꾼다. 뒤의 두 장은 반대로 상태를
 * 고정하고 진입점만 바꿔, 상단 바 뒤로가기의 유무만 차이로 남긴다.
 */

/** 아무것도 입력하지 않은 진입 직후. 두 버튼이 비활성이고 오류 문구는 아직 뜨지 않는다. */
@UiModePreviews
@Composable
private fun ProfileScreenInitialPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = ProfileUiState(entryPoint = ProfileEntryPoint.Onboarding),
            onIntent = {},
            onBackClick = {},
        )
    }
}

/** 닉네임과 아바타를 모두 채운 상태. `저장`·`지우기`가 함께 활성이다. */
@UiModePreviews
@Composable
private fun ProfileScreenFilledPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = filledState(entryPoint = ProfileEntryPoint.Onboarding),
            onIntent = {},
            onBackClick = {},
        )
    }
}

/** 유효하지 않은 닉네임을 입력한 뒤. 필드가 오류 상태가 되고 `저장`이 잠긴다. */
@UiModePreviews
@Composable
private fun ProfileScreenNicknameErrorPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = ProfileUiState(
                entryPoint = ProfileEntryPoint.Onboarding,
                nickname = INVALID_NICKNAME,
                isNicknameValid = false,
                isNicknameTouched = true,
            ),
            onIntent = {},
            onBackClick = {},
        )
    }
}

/** 저장이 진행 중인 상태. 값은 그대로 남고 두 버튼만 잠겨 중복 저장을 막는다. */
@UiModePreviews
@Composable
private fun ProfileScreenSavingPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = filledState(entryPoint = ProfileEntryPoint.Onboarding).copy(isSaving = true),
            onIntent = {},
            onBackClick = {},
        )
    }
}

/** 마이페이지에서 들어온 화면. 상단 바에 뒤로가기가 놓인다. */
@UiModePreviews
@Composable
private fun ProfileScreenMyPageEntryPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = entryPointComparisonState(entryPoint = ProfileEntryPoint.MyPage),
            onIntent = {},
            onBackClick = {},
        )
    }
}

/** 온보딩에서 들어온 화면. 같은 값인데 뒤로가기 자리가 비어 있다 — 위 장과 견줘 볼 차이다. */
@UiModePreviews
@Composable
private fun ProfileScreenOnboardingEntryPreview() {
    ProfileScreenPreviewContainer {
        ProfileScreen(
            state = entryPointComparisonState(entryPoint = ProfileEntryPoint.Onboarding),
            onIntent = {},
            onBackClick = {},
        )
    }
}

private fun filledState(entryPoint: ProfileEntryPoint) =
    ProfileUiState(
        entryPoint = entryPoint,
        nickname = VALID_NICKNAME,
        selectedAvatar = SELECTED_AVATAR,
        isNicknameValid = true,
        isNicknameTouched = true,
    )

// 진입점 두 장은 앞의 네 장과 다른 값을 써서, 같은 그림이 두 번 나오지 않게 한다.
private fun entryPointComparisonState(entryPoint: ProfileEntryPoint) =
    ProfileUiState(
        entryPoint = entryPoint,
        nickname = PREFILLED_NICKNAME,
        selectedAvatar = PREFILLED_AVATAR,
        isNicknameValid = true,
        isNicknameTouched = true,
    )

@Composable
private fun ProfileScreenPreviewContainer(content: @Composable () -> Unit) {
    MinoAndroidAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MinoAndroidTheme.colors.backgroundNormalNormal),
        ) {
            content()
        }
    }
}

private const val VALID_NICKNAME = "민호"
private const val INVALID_NICKNAME = "민"
private const val PREFILLED_NICKNAME = "이민호"

private val SELECTED_AVATAR = MinoProfileAvatar.Person3
private val PREFILLED_AVATAR = MinoProfileAvatar.Person7
