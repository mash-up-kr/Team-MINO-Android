package team.mino.feature.profile.main.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.ActionAreaVariant
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatarImage
import team.mino.core.designsystem.component.profileavatar.MinoProfileAvatarSize
import team.mino.core.designsystem.component.textinput.MinoTextField
import team.mino.core.designsystem.component.textinput.MinoTextFieldStatus
import team.mino.core.designsystem.component.topnavigation.MinoTopNavigation
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.feature.profile.main.component.ProfileAvatarGrid
import team.mino.feature.profile.main.vm.ProfileIntent
import team.mino.feature.profile.main.vm.ProfileUiState

/**
 * 프로필 설정 화면. 온보딩과 마이페이지 두 진입점이 같은 화면을 쓰고, 진입점에 따라 달라지는
 * 것은 [ProfileUiState]가 이미 판정해 둔 값으로만 갈린다.
 *
 * 상단 바와 하단 액션은 제자리에 고정되고 그 사이 본문만 세로로 스크롤한다. 스크롤은 그 하나뿐이라
 * 아바타 그리드는 고정 높이로 놓이고 스크롤이 중첩되지 않는다. `Scaffold`는 셸이 열고 이 화면은
 * 열지 않는다.
 *
 * @param onBackClick 상단 바의 뒤로가기. 뒤로가기를 노출할지는 상태가 정한다.
 */
@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MinoTopNavigation(
            title = "프로필 설정",
            // 뒤로 갈 수 없는 진입점에서는 버튼 자리를 비운다. 비활성 버튼을 보여 주지 않는다.
            onBackClick = if (state.isBackEnabled) onBackClick else null,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ContentPadding),
            verticalArrangement = Arrangement.spacedBy(ContentSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "친구들에게 어떻게 보일까요?",
                style = MinoAndroidTheme.typography.title3Bold,
                color = MinoAndroidTheme.colors.primaryNormal,
            )

            MinoProfileAvatarImage(
                avatar = state.displayedAvatar,
                size = MinoProfileAvatarSize.Thumbnail,
                contentDescription = "현재 프로필 이미지",
            )

            MinoTextField(
                value = state.nickname,
                onValueChange = { onIntent(ProfileIntent.NicknameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = "이름 또는 닉네임",
                required = true,
                placeholder = "한글·영문 2글자 이상",
                helperText = "한글·영문 2글자 이상을 입력해주세요.",
                status = if (state.isNicknameErrorVisible) {
                    MinoTextFieldStatus.Negative
                } else {
                    MinoTextFieldStatus.Normal
                },
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AvatarSectionSpacing),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "프로필 이미지 선택",
                    style = MinoAndroidTheme.typography.label1NormalBold,
                    color = MinoAndroidTheme.colors.labelNeutral,
                )
                ProfileAvatarGrid(
                    selectedAvatar = state.selectedAvatar,
                    onAvatarSelect = { avatar -> onIntent(ProfileIntent.AvatarSelected(avatar)) },
                )
            }
        }

        MinoActionArea(
            mainAction = ActionAreaAction(
                text = "저장",
                onClick = { onIntent(ProfileIntent.SaveClicked) },
                enabled = state.isSaveEnabled,
            ),
            variant = ActionAreaVariant.Neutral,
            alternativeAction = ActionAreaAction(
                text = "지우기",
                onClick = { onIntent(ProfileIntent.ClearClicked) },
                enabled = state.isClearEnabled,
            ),
        )
    }
}

// 액션 영역이 자기 위쪽 여백을 이미 갖고 있어 본문 아래쪽은 비운다.
private val ContentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp)

private val ContentSpacing = 24.dp

private val AvatarSectionSpacing = 16.dp
