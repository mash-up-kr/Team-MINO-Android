package team.mino.feature.mypage.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.PersonFill
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.feature.mypage.R

/**
 * 프로필 이미지로 고를 수 있는 아바타 12종을 4열 그리드로 보여준다.
 *
 * `avatarId`는 백엔드와 매핑이 확정되지 않아 임시로 0~11 순번을 쓴다
 * (`contracts/profile-setup-contract.md` "아바타 카탈로그" `[TBD]`).
 */
@Composable
internal fun AvatarGrid(
    selectedAvatarId: Int?,
    onAvatarSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AvatarGridRowSpacing),
    ) {
        AvatarCatalogIds.chunked(AvatarGridColumns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                row.forEach { avatarId ->
                    AvatarGridItem(
                        avatarId = avatarId,
                        selected = avatarId == selectedAvatarId,
                        onClick = { onAvatarSelected(avatarId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarGridItem(
    avatarId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) MinoAndroidTheme.colors.primaryNormal else MinoAndroidTheme.colors.lineNormalAlternative
    val borderWidth = if (selected) AvatarSelectedBorderWidth else AvatarDefaultBorderWidth

    AvatarGlyph(
        avatarId = avatarId,
        size = AvatarItemSize,
        modifier = modifier
            .border(width = borderWidth, color = borderColor, shape = CircleShape)
            .rippleSingleClickable(
                onClickLabel = stringResource(R.string.mypage_profile_avatar_content_description, avatarId + 1),
                role = Role.RadioButton,
                onClick = onClick,
            ),
    )
}

/**
 * 아바타 하나를 원형 배경 + [MinoIcons.PersonFill] 글리프로 그린다.
 * [AvatarGrid]의 선택 그리드와 마이페이지 메인의 프로필 요약이 같은 placeholder 표현을 쓴다([AvatarPalette] 참고).
 */
@Composable
internal fun AvatarGlyph(
    avatarId: Int,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = AvatarPalette[avatarId % AvatarPalette.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(palette.background),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = MinoIcons.PersonFill,
            contentDescription = null,
            tint = palette.foreground,
            modifier = Modifier.size(size / 2),
        )
    }
}

private data class AvatarColorPalette(val background: Color, val foreground: Color)

/**
 * 12종 아바타의 실제 일러스트 자산은 아직 없다(백엔드 `avatarId` 매핑 확정 전, [T026] 범위 밖).
 * 자산이 준비되기 전까지 색으로만 구분되는 placeholder 글리프를 쓴다.
 */
private val AvatarPalette: List<AvatarColorPalette>
    @Composable get() = MinoAndroidTheme.colors.let { colors ->
        listOf(
            AvatarColorPalette(colors.accentBackgroundRedOrange, colors.accentForegroundRedOrange),
            AvatarColorPalette(colors.accentBackgroundLime, colors.accentForegroundLime),
            AvatarColorPalette(colors.accentBackgroundCyan, colors.accentForegroundCyan),
            AvatarColorPalette(colors.accentBackgroundLightBlue, colors.accentForegroundLightBlue),
            AvatarColorPalette(colors.accentBackgroundViolet, colors.accentForegroundViolet),
            AvatarColorPalette(colors.accentBackgroundPurple, colors.accentForegroundPurple),
            AvatarColorPalette(colors.accentBackgroundPink, colors.accentForegroundPink),
        )
    }

private val AvatarCatalogIds = 0..11

private const val AvatarGridColumns = 4
private val AvatarGridRowSpacing = 10.dp
private val AvatarItemSize = 70.dp
private val AvatarDefaultBorderWidth = 1.25.dp
private val AvatarSelectedBorderWidth = 2.dp
