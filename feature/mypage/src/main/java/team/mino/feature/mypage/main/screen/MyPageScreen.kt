package team.mino.feature.mypage.main.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.component.dialog.MinoDialog
import team.mino.core.designsystem.component.switch.MinoSwitch
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.PencilFill
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.domain.model.PermissionType
import team.mino.feature.mypage.R
import team.mino.feature.mypage.main.vm.MyPageIntent
import team.mino.feature.mypage.main.vm.MyPageUiState
import team.mino.feature.mypage.profile.component.AvatarGlyph

@Composable
internal fun MyPageScreen(
    state: MyPageUiState,
    onIntent: (MyPageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 프로필 요약 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MyPageScreenTokens.SectionSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ProfileAvatar(avatarId = state.avatarId)

            Spacer(modifier = Modifier.height(MyPageScreenTokens.AvatarNicknameSpacing))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.nickname,
                    style = MinoAndroidTheme.typography.heading2Bold,
                    color = MinoAndroidTheme.colors.labelNormal,
                )
                Spacer(modifier = Modifier.width(MyPageScreenTokens.NicknameEditIconSpacing))
                Icon(
                    imageVector = MinoIcons.PencilFill,
                    contentDescription = stringResource(R.string.mypage_main_edit_profile_content_description),
                    tint = MinoAndroidTheme.colors.labelNormal,
                    modifier = Modifier
                        .size(MyPageScreenTokens.EditIconSize)
                        .rippleSingleClickable(
                            onClickLabel = stringResource(R.string.mypage_main_edit_profile_content_description),
                            role = Role.Button,
                            onClick = { onIntent(MyPageIntent.OnEditProfileClick) },
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.height(MyPageScreenTokens.SectionSpacing))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MyPageScreenTokens.DividerStripHeight)
                .background(MinoAndroidTheme.colors.backgroundNormalAlternative),
        )
        Spacer(modifier = Modifier.height(MyPageScreenTokens.SectionSpacing))

        // 앱 설정 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyPageScreenTokens.SectionHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MyPageScreenTokens.SectionTitleSpacing),
        ) {
            Text(
                text = stringResource(R.string.mypage_main_app_settings_title),
                style = MinoAndroidTheme.typography.headline1Bold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
            Column(verticalArrangement = Arrangement.spacedBy(MyPageScreenTokens.RowSpacing)) {
                SettingsSwitchRow(
                    label = stringResource(R.string.mypage_main_notification_setting_label),
                    checked = state.isNotificationSwitchOn,
                    // Route가 shouldShowRequestPermissionRationale로 canShowSystemDialog를 다시 계산해
                    // 덮어쓰므로 여기서는 값 자체는 의미가 없다.
                    onCheckedChange = { onIntent(MyPageIntent.OnNotificationSwitchClick(canShowSystemDialog = true)) },
                )
                SettingsSwitchRow(
                    label = stringResource(R.string.mypage_main_location_setting_label),
                    checked = state.isLocationSwitchOn,
                    onCheckedChange = { onIntent(MyPageIntent.OnLocationSwitchClick(canShowSystemDialog = true)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(MyPageScreenTokens.SectionSpacing))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyPageScreenTokens.SectionHorizontalPadding)
                .height(MyPageScreenTokens.DividerLineHeight)
                .background(MinoAndroidTheme.colors.lineNormalAlternative),
        )
        Spacer(modifier = Modifier.height(MyPageScreenTokens.SectionSpacing))

        // 서비스 정보 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MyPageScreenTokens.SectionHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(MyPageScreenTokens.SectionTitleSpacing),
        ) {
            Text(
                text = stringResource(R.string.mypage_main_service_info_title),
                style = MinoAndroidTheme.typography.headline1Bold,
                color = MinoAndroidTheme.colors.labelNormal,
            )
            Column(verticalArrangement = Arrangement.spacedBy(MyPageScreenTokens.RowSpacing)) {
                ServiceInfoRow(
                    label = stringResource(R.string.mypage_main_terms_label),
                    onClick = { onIntent(MyPageIntent.OnTermsClick) },
                )
                ServiceInfoRow(
                    label = stringResource(R.string.mypage_main_app_review_label),
                    onClick = { onIntent(MyPageIntent.OnAppReviewClick) },
                )
            }
        }
    }

    val dialogTarget = state.permissionSettingsDialogTarget
    if (dialogTarget != null) {
        PermissionSettingsDialog(
            target = dialogTarget,
            onConfirmClick = { onIntent(MyPageIntent.OnPermissionSettingsDialogConfirmed) },
            onDismiss = { onIntent(MyPageIntent.OnPermissionSettingsDialogDismissed) },
        )
    }
}

@Composable
private fun ProfileAvatar(
    avatarId: Int?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(MyPageScreenTokens.AvatarSize)
            .clip(CircleShape)
            .border(
                width = MyPageScreenTokens.AvatarBorderWidth,
                color = MinoAndroidTheme.colors.lineNormalAlternative,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarId != null) {
            AvatarGlyph(avatarId = avatarId, size = MyPageScreenTokens.AvatarSize)
        } else {
            Box(
                modifier = Modifier
                    .size(MyPageScreenTokens.AvatarSize)
                    .background(MinoAndroidTheme.colors.backgroundNormalAlternative),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MinoIcons.PersonFill,
                    contentDescription = null,
                    tint = MinoAndroidTheme.colors.labelAlternative,
                    modifier = Modifier.size(MyPageScreenTokens.AvatarSize / 2),
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MyPageScreenTokens.SwitchRowHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MinoAndroidTheme.typography.body1ReadingRegular,
            color = MinoAndroidTheme.colors.labelNeutral,
        )
        MinoSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ServiceInfoRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        style = MinoAndroidTheme.typography.body1ReadingRegular,
        color = MinoAndroidTheme.colors.labelNeutral,
        modifier = modifier
            .fillMaxWidth()
            .rippleSingleClickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun PermissionSettingsDialog(
    target: PermissionType,
    onConfirmClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = when (target) {
        PermissionType.NOTIFICATION -> R.string.mypage_main_notification_permission_dialog_title
        PermissionType.LOCATION -> R.string.mypage_main_location_permission_dialog_title
    }
    val messageRes = when (target) {
        PermissionType.NOTIFICATION -> R.string.mypage_main_notification_permission_dialog_message
        PermissionType.LOCATION -> R.string.mypage_main_location_permission_dialog_message
    }

    MinoDialog(
        title = stringResource(titleRes),
        message = stringResource(messageRes),
        confirmLabel = stringResource(R.string.mypage_main_permission_dialog_confirm),
        onConfirmClick = onConfirmClick,
        cancelLabel = stringResource(R.string.mypage_main_permission_dialog_cancel),
        onCancelClick = onDismiss,
        onDismissRequest = onDismiss,
        modifier = modifier,
    )
}

/**
 * 화면 전용 spacing·치수 값. `:core:design-system`에 spacing 토큰 foundation이 아직 없어
 * 실측값을 직접 든다.
 */
private object MyPageScreenTokens {
    // Figma 변수(`xxs`·`md`·`base`·`base lg`·`xxl`)에 대응.
    val SectionSpacing = 32.dp
    val SectionTitleSpacing = 16.dp
    val SectionHorizontalPadding = 20.dp
    val RowSpacing = 12.dp
    val AvatarNicknameSpacing = 12.dp
    val NicknameEditIconSpacing = 2.dp

    // 실측값(변수 미바인딩).
    val AvatarSize = 100.dp
    val AvatarBorderWidth = 5.dp
    val EditIconSize = 20.dp
    val DividerStripHeight = 12.dp
    val DividerLineHeight = 2.dp
    val SwitchRowHeight = 32.dp
}
