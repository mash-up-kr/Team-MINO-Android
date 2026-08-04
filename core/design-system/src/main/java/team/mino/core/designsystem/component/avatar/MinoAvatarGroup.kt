package team.mino.core.designsystem.component.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import team.mino.core.designsystem.component.avatar.token.AvatarTokens
import team.mino.core.designsystem.component.avatar.token.avatarSize
import team.mino.core.designsystem.component.avatar.token.overlap
import team.mino.core.designsystem.component.avatar.token.trailingSpacing
import team.mino.core.designsystem.util.modifier.surface.surface

/**
 * 여러 Avatar를 일부 겹쳐 나열하는 Avatar Group.
 *
 * Figma(MU_Wanted / Montage)의 `Avatar/Avatar Group`(16215-26148) 스펙을 따른다. 배경 없이
 * 아바타만 겹쳐 놓고, 각 아바타는 흰 링으로 경계를 구분한다. 링은 아바타 **바깥**에 붙어
 * 슬롯이 아바타보다 링 두께만큼 커진다 — 아바타 자체 보더 위에 덧그리면 두 선이 같은 자리를
 * 먹어 경계가 사라지기 때문이다.
 *
 * 그래서 폭이 Figma 심볼(XSmall 96 / Small 128, 각 5개 = 링을 아바타 안쪽에 덮은 값)보다
 * 링 두께 두 배만큼 크다. 의도한 차이다.
 *
 * 끝에 붙는 [trailingContent]는 Figma가 "외 0명" 같은 텍스트 버튼을 기본 프리셋으로 두지만,
 * Custom도 허용하므로 슬롯으로 연다. 기본 프리셋과 같은 모양이 필요하면
 * `MinoTextButton(size = Small, style = Assistive)`를 넣는다.
 *
 * 옅은 배경의 pill 안에 담고 멤버 추가 버튼·초과 인원 뱃지를 붙이는 형태는 **디자인 시스템이 아니라
 * 화면 레벨 조합**(Figma `15852:88488`)이라 여기 있지 않다. `feature`의 해당 컴포넌트를 쓴다.
 *
 * @param imageUrls 표시할 아바타들의 이미지 URL 목록(각각 null이면 placeholder).
 * @param variant 공통 형태.
 * @param size 공통 크기([MinoAvatarGroupSize]). 겹침 폭과 [trailingContent] 간격이 함께 바뀐다.
 * @param trailingContent 아바타 스택 오른쪽에 붙는 슬롯. null이면 표시하지 않는다.
 */
@Composable
fun MinoAvatarGroup(
    imageUrls: ImmutableList<String?>,
    modifier: Modifier = Modifier,
    variant: MinoAvatarVariant = MinoAvatarVariant.Person,
    size: MinoAvatarGroupSize = MinoAvatarGroupSize.XSmall,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val shape = MinoAvatarDefaults.shape(variant)
    val ringColor = MinoAvatarDefaults.groupRingColor

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size.trailingSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(-size.overlap)) {
            imageUrls.forEach { url ->
                MinoAvatar(
                    modifier = Modifier
                        .surface(shape = shape, containerColor = ringColor)
                        .padding(AvatarTokens.GroupRingWidth),
                    variant = variant,
                    size = size.avatarSize,
                    imageUrl = url,
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

/**
 * Avatar Group의 크기. Figma `Avatar/Avatar Group`의 `Size` 축에 대응한다.
 *
 * **[MinoAvatarSize]와 값이 다르다.** 그룹은 다섯 크기 중 두 개만 정의돼 있고, 크기가 아바타
 * 지름뿐 아니라 겹침 폭·트레일링 간격까지 함께 가른다. 아바타 단독으로 쓸 때는 [MinoAvatarSize]다.
 *
 * 실측값 세 가지는 `token/AvatarTokens.kt`가 소유한다.
 */
enum class MinoAvatarGroupSize {
    /** Figma `Size=XSmall`. 아바타 24, 겹침 6, 트레일링 간격 8. */
    XSmall,

    /** Figma `Size=Small`. 아바타 32, 겹침 8, 트레일링 간격 10. */
    Small,
}
