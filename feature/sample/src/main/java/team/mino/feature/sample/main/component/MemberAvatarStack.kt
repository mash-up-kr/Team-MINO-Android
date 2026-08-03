package team.mino.feature.sample.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import team.mino.core.designsystem.component.avatar.MinoAvatar
import team.mino.core.designsystem.component.avatar.MinoAvatarDefaults
import team.mino.core.designsystem.component.avatar.MinoAvatarVariant
import team.mino.core.designsystem.foundation.icons.MinoIcons
import team.mino.core.designsystem.foundation.icons.icons.Plus
import team.mino.core.designsystem.theme.MinoAndroidAppTheme
import team.mino.core.designsystem.theme.MinoAndroidTheme
import team.mino.core.designsystem.util.modifier.clickable.rippleSingleClickable
import team.mino.core.designsystem.util.modifier.surface.surface
import team.mino.core.designsystem.util.preview.UiModePreviews
import team.mino.feature.sample.main.component.token.MemberAvatarStackTokens

/**
 * 옅은 pill 안에 멤버 아바타를 겹쳐 담는 스택. 끝에 멤버 추가 버튼이나 초과 인원 뱃지를 붙인다.
 *
 * Figma `Avatar`(15852-88488, state=add·default·more) 스펙을 따른다. 이름은 같은 "Avatar"지만
 * **디자인 시스템 컴포넌트가 아니다** — DS 컴포넌트 영역 밖에 있고, 추가 버튼도 Menu의 아이콘 버튼
 * 리소스를 가져다 조립한 화면 레벨 패턴이다. 그래서 `core:design-system`이 아니라 여기에 둔다.
 * 배경 없이 아바타만 겹치는 디자인 시스템 쪽 형태가 필요하면
 * [team.mino.core.designsystem.component.avatar.MinoAvatarGroup]을 쓴다.
 *
 * @param imageUrls 표시할 아바타들의 이미지 URL 목록(각각 null이면 placeholder).
 * @param onAddClick 멤버 추가 버튼 콜백(Figma `state=add`). null이면 버튼을 숨긴다.
 * @param overflowLabel 아바타 스택 끝에 겹쳐 붙는 초과 인원 뱃지 문구(Figma `state=more`, 예: "99+").
 *   포맷 규칙은 호출부가 정한다. null이면 뱃지를 숨긴다.
 */
@Composable
fun MemberAvatarStack(
    imageUrls: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    onAddClick: (() -> Unit)? = null,
    overflowLabel: String? = null,
) {
    // 아바타 겹침을 끊어 주는 링. 디자인 시스템 Avatar Group과 같은 색을 쓴다.
    val ringColor = MinoAvatarDefaults.groupRingColor
    val slotShape = MinoAvatarDefaults.shape(MinoAvatarVariant.Person)

    Row(
        modifier = modifier
            .surface(
                shape = MemberAvatarStackTokens.ContainerShape,
                containerColor = MinoAndroidTheme.colors.fillNormal,
            ).padding(MemberAvatarStackTokens.ContainerPadding),
        horizontalArrangement = Arrangement.spacedBy(MemberAvatarStackTokens.TrailingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-MemberAvatarStackTokens.Overlap)) {
            imageUrls.forEach { url ->
                RingedSlot(ringColor = ringColor, shape = slotShape) {
                    MinoAvatar(
                        variant = MinoAvatarVariant.Person,
                        size = MemberAvatarStackTokens.AvatarSize,
                        imageUrl = url,
                    )
                }
            }

            if (overflowLabel != null) {
                RingedSlot(ringColor = ringColor, shape = slotShape) {
                    AvatarSlot(
                        containerColor = MinoAndroidTheme.colors.backgroundElevatedAlternative,
                        shape = slotShape,
                    ) {
                        Text(
                            text = overflowLabel,
                            style = MinoAndroidTheme.typography.label2Bold,
                            color = MinoAndroidTheme.colors.labelAlternative,
                        )
                    }
                }
            }
        }

        if (onAddClick != null) {
            AvatarSlot(
                containerColor = MinoAndroidTheme.colors.primaryNormal,
                shape = slotShape,
                onClick = onAddClick,
            ) {
                Icon(
                    imageVector = MinoIcons.Plus,
                    contentDescription = "멤버 추가",
                    tint = MinoAndroidTheme.colors.inversePrimary,
                    modifier = Modifier.size(MemberAvatarStackTokens.AddButtonIconSize),
                )
            }
        }
    }
}

/** 아바타·뱃지를 흰 링으로 감싼다. 링이 바깥에 붙어 슬롯이 아바타보다 링 두께만큼 커진다. */
@Composable
private fun RingedSlot(
    ringColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .surface(shape = shape, containerColor = ringColor)
            .padding(MemberAvatarStackTokens.RingWidth),
    ) {
        content()
    }
}

/** 아바타와 같은 크기·모양의 원형 자리. 초과 인원 뱃지와 추가 버튼이 공유한다. */
@Composable
private fun AvatarSlot(
    containerColor: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(MemberAvatarStackTokens.AvatarSize.dp)
            .surface(shape = shape, containerColor = containerColor)
            .then(if (onClick != null) Modifier.rippleSingleClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@UiModePreviews
@Composable
private fun MemberAvatarStackPreview() {
    MinoAndroidAppTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MemberAvatarStack(imageUrls = persistentListOf(null, null, null, null))
            MemberAvatarStack(imageUrls = persistentListOf(null), onAddClick = {})
            MemberAvatarStack(
                imageUrls = persistentListOf(null, null, null),
                overflowLabel = "99+",
            )
        }
    }
}
