package team.mino.feature.mypage.profile.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.actionarea.ActionAreaAction
import team.mino.core.designsystem.component.actionarea.ActionAreaVariant
import team.mino.core.designsystem.component.actionarea.MinoActionArea
import team.mino.core.designsystem.component.textinput.MinoTextField
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.ArrowLeftThick
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.feature.mypage.R
import team.mino.feature.mypage.profile.component.AvatarGlyph
import team.mino.feature.mypage.profile.component.AvatarGrid
import team.mino.feature.mypage.profile.vm.ProfileIntent
import team.mino.feature.mypage.profile.vm.ProfileUiState

@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ProfileTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(ProfileScreenContentPadding),
            verticalArrangement = Arrangement.spacedBy(ProfileScreenSectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.mypage_profile_headline),
                style = MinoAndroidTheme.typography.title3Bold,
                color = MinoAndroidTheme.colors.primaryNormal,
            )

            ProfileAvatarPreview(avatarId = state.avatarId)

            MinoTextField(
                value = state.nickname,
                onValueChange = { onIntent(ProfileIntent.OnNicknameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.mypage_profile_nickname_label),
                required = true,
                helperText = stringResource(R.string.mypage_profile_nickname_helper),
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ProfileScreenAvatarSectionSpacing),
            ) {
                Text(
                    text = stringResource(R.string.mypage_profile_avatar_section_label),
                    style = MinoAndroidTheme.typography.label1NormalBold,
                    color = MinoAndroidTheme.colors.labelNeutral,
                )
                AvatarGrid(
                    selectedAvatarId = state.avatarId,
                    onAvatarSelected = { onIntent(ProfileIntent.OnAvatarSelected(it)) },
                )
            }
        }

        MinoActionArea(
            modifier = Modifier.navigationBarsPadding(),
            variant = ActionAreaVariant.Neutral,
            mainAction = ActionAreaAction(
                text = stringResource(R.string.mypage_profile_save_button),
                onClick = { onIntent(ProfileIntent.OnSaveClick) },
                enabled = state.isSaveEnabled,
            ),
            alternativeAction = ActionAreaAction(
                text = stringResource(R.string.mypage_profile_clear_button),
                onClick = { onIntent(ProfileIntent.OnClearClick) },
            ),
        )
    }
}

/** 뒤로가기 아이콘 + 중앙 타이틀. `core:design-system`에 대응 컴포넌트가 없어 이 화면 전용으로 최소 구현한다. */
@Composable
private fun ProfileTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ProfileTopBarHorizontalPadding, vertical = ProfileTopBarVerticalPadding),
    ) {
        val backContentDescription = stringResource(R.string.mypage_profile_back_content_description)
        Icon(
            imageVector = MinoIcons.ArrowLeftThick,
            contentDescription = backContentDescription,
            tint = MinoAndroidTheme.colors.labelStrong,
            modifier = Modifier
                .size(ProfileTopBarIconSize)
                .align(Alignment.CenterStart)
                .rippleSingleClickable(
                    onClickLabel = backContentDescription,
                    role = Role.Button,
                    onClick = onBack,
                ),
        )
        Text(
            text = stringResource(R.string.mypage_profile_top_bar_title),
            style = MinoAndroidTheme.typography.headline2Bold,
            color = MinoAndroidTheme.colors.labelStrong,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/** 현재 선택된 아바타를 120dp 원형으로 미리 보여준다. 글리프는 [AvatarGrid]와 같은 [AvatarGlyph]를 공유한다. */
@Composable
private fun ProfileAvatarPreview(
    avatarId: Int?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ProfileAvatarPreviewSize)
            .border(
                width = ProfileAvatarPreviewBorderWidth,
                color = MinoAndroidTheme.colors.lineNormalAlternative,
                shape = CircleShape,
            ),
    ) {
        if (avatarId != null) {
            AvatarGlyph(
                avatarId = avatarId,
                size = ProfileAvatarPreviewSize,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private val ProfileScreenContentPadding = 20.dp
private val ProfileScreenSectionSpacing = 24.dp
private val ProfileScreenAvatarSectionSpacing = 16.dp

private val ProfileTopBarHorizontalPadding = 16.dp
private val ProfileTopBarVerticalPadding = 10.dp
private val ProfileTopBarIconSize = 24.dp

private val ProfileAvatarPreviewSize = 120.dp
private val ProfileAvatarPreviewBorderWidth = 5.dp
